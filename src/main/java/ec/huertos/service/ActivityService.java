package ec.huertos.service;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import ec.huertos.storage.PhotoStorage;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ActivityService {
  public static final Map<String, String> TYPES = new LinkedHashMap<>();

  static {
    TYPES.put("PRE-01", "Preparación de la tierra");
    TYPES.put("PRE-02", "Estructuras de cultivo");
    TYPES.put("SIE-01", "Selección de plantas");
    TYPES.put("SIE-02", "Ritual de siembra");
    TYPES.put("SIE-03", "Siembra de plantas");
    TYPES.put("SIE-04", "Señalética");
    TYPES.put("CUI-01", "Deshierbe");
    TYPES.put("CUI-02", "Replante");
    TYPES.put("CUI-03", "Aporque");
    TYPES.put("CUI-04", "Riego");
    TYPES.put("COS-01", "Baño previo a la cosecha");
    TYPES.put("COS-02", "Ritual de gratitud");
    TYPES.put("COS-03", "Cosecha");
    TYPES.put("COS-04", "Limpieza");
    TYPES.put("COS-05", "Secado");
    TYPES.put("COS-06", "Empaquetado, etiquetado y almacenaje");
  }

  private final ActivityRepository activities;
  private final ParticipationRepository participants;
  private final SubmissionRepository submissions;
  private final ReviewRepository reviews;
  private final PointMovementRepository points;
  private final AccountRepository accounts;
  private final NotificationRepository notifications;
  private final Access access;
  private final PhotoStorage photos;

  @Value("${huertos.points.community}")
  int communityReward;

  @Value("${huertos.points.preparation}")
  int preparation;

  @Value("${huertos.points.sowing}")
  int sowing;

  @Value("${huertos.points.care}")
  int care;

  @Value("${huertos.points.harvest}")
  int harvest;

  public ActivityService(
      ActivityRepository a,
      ParticipationRepository p,
      SubmissionRepository s,
      ReviewRepository r,
      PointMovementRepository l,
      AccountRepository u,
      NotificationRepository n,
      Access x,
      PhotoStorage f) {
    activities = a;
    participants = p;
    submissions = s;
    reviews = r;
    points = l;
    accounts = u;
    notifications = n;
    access = x;
    photos = f;
  }

  public Activity get(Long id) {
    return activities
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
  }

  public static String text(String value, int min, int max) {
    if (value == null || value.strip().length() < min || value.length() > max)
      throw new IllegalArgumentException("Revisa los campos obligatorios y su longitud");
    return value.strip();
  }

  @Transactional
  public Activity create(
      String type, String description, LocalDate date, List<Long> selected, MultipartFile photo) {
    var actor = access.current();
    access.manage(actor.communityId);
    if (!TYPES.containsKey(type))
      throw new IllegalArgumentException("Tipo de actividad no permitido");
    if (date == null || date.isAfter(LocalDate.now(ZoneId.of("America/Guayaquil"))))
      throw new IllegalArgumentException("La fecha no puede estar en el futuro");
    String clean = text(description, 10, 2000);
    if (selected == null || selected.isEmpty())
      throw new IllegalArgumentException("Selecciona al menos un integrante activo");
    var ids = new HashSet<>(selected);
    var members = accounts.findAllById(ids);
    if (members.size() != ids.size()
        || members.stream().anyMatch(u -> !u.active || !actor.communityId.equals(u.communityId)))
      throw new IllegalArgumentException("Los participantes deben estar activos en tu comunidad");
    String key = photos.save(photo);
    var a = new Activity();
    a.authorId = actor.id;
    a.communityId = actor.communityId;
    a.type = type;
    a.description = clean;
    a.date = date;
    a.status = "PENDIENTE";
    a.communityReward = communityReward;
    a.individualReward =
        type.startsWith("PRE")
            ? preparation
            : type.startsWith("SIE") ? sowing : type.startsWith("CUI") ? care : harvest;
    activities.save(a);
    for (Long id : ids) {
      var p = new Participation();
      p.activityId = a.id;
      p.accountId = id;
      participants.save(p);
    }
    saveSubmission(a, key, actor.id);
    return a;
  }

  private void saveSubmission(Activity a, String key, Long author) {
    var s = new Submission();
    s.activityId = a.id;
    s.authorId = author;
    s.description = a.description;
    s.photoKey = key;
    s.createdAt = Instant.now();
    submissions.save(s);
  }

  @Transactional
  public void revise(Long id, String description, MultipartFile photo) {
    var a = activities.lockById(id).orElseThrow();
    var actor = access.manage(a.communityId);
    if (a.status.equals("APROBADA"))
      throw new IllegalArgumentException("Una actividad aprobada no puede modificarse");
    a.description = text(description, 10, 2000);
    String key = photo != null && !photo.isEmpty() ? photos.save(photo) : latest(a.id).photoKey;
    saveSubmission(a, key, actor.id);
    a.status = "PENDIENTE";
    activities.save(a);
  }

  public Submission latest(Long id) {
    return submissions.findAll().stream()
        .filter(s -> s.activityId.equals(id))
        .max(Comparator.comparing(s -> s.id))
        .orElseThrow();
  }

  @Transactional
  public void decide(Long id, Long submissionId, String decision, String observations) {
    var a = activities.lockById(id).orElseThrow();
    var actor = access.review(a.communityId);
    if (a.authorId.equals(actor.id))
      throw new IllegalArgumentException("No puedes revisar tu propia actividad");
    if (!a.status.equals("PENDIENTE"))
      throw new IllegalArgumentException("Esta actividad ya fue revisada");
    var latest = latest(id);
    if (!latest.id.equals(submissionId))
      throw new IllegalArgumentException("Hay un envío más reciente. Recarga antes de revisar");
    if (!Set.of("APROBADA", "RECHAZADA").contains(decision))
      throw new IllegalArgumentException("Decisión no permitida");
    String note = observations == null ? "" : observations.strip();
    if (note.length() > 2000 || decision.equals("RECHAZADA") && note.length() < 5)
      throw new IllegalArgumentException(
          "El rechazo requiere una observación de 5 a 2000 caracteres");
    var r = new Review();
    r.activityId = id;
    r.submissionId = latest.id;
    r.reviewerId = actor.id;
    r.decision = decision;
    r.observations = note;
    r.createdAt = Instant.now();
    reviews.save(r);
    a.status = decision;
    activities.save(a);
    if (decision.equals("APROBADA")) {
      award(a, null, "C:" + a.communityId, a.communityReward);
      participants.findAll().stream()
          .filter(p -> p.activityId.equals(id))
          .forEach(p -> award(a, p.accountId, "U:" + p.accountId, a.individualReward));
    }
    var n = new Notification();
    n.accountId = a.authorId;
    n.message = "Actividad #" + id + " " + decision.toLowerCase() + ". " + note;
    n.createdAt = Instant.now();
    notifications.save(n);
  }

  private void award(Activity a, Long account, String key, int amount) {
    var l = new PointMovement();
    l.activityId = a.id;
    l.communityId = a.communityId;
    l.accountId = account;
    l.beneficiaryKey = key;
    l.amount = amount;
    l.createdAt = Instant.now();
    points.save(l);
  }
}
