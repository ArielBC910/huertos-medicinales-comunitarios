package ec.huertos.web;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import ec.huertos.service.*;
import ec.huertos.storage.PhotoStorage;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WebController {
  private final CommunityRepository communities;
  private final GardenRepository gardens;
  private final PlantRepository plants;
  private final AccountRepository accounts;
  private final ActivityRepository activities;
  private final ParticipationRepository participation;
  private final SubmissionRepository submissions;
  private final ReviewRepository reviews;
  private final InventoryItemRepository inventory;
  private final ForumPostRepository posts;
  private final ContentReportRepository reports;
  private final CalendarEntryRepository calendar;
  private final NotificationRepository notifications;
  private final Access access;
  private final ActivityService activityService;
  private final CommunityService communityService;
  private final ForumService forum;
  private final ProgressService progress;
  private final PhotoStorage photos;

  public WebController(
      CommunityRepository c,
      GardenRepository g,
      PlantRepository p,
      AccountRepository u,
      ActivityRepository a,
      ParticipationRepository pa,
      SubmissionRepository s,
      ReviewRepository r,
      InventoryItemRepository i,
      ForumPostRepository f,
      ContentReportRepository cr,
      CalendarEntryRepository ca,
      NotificationRepository n,
      Access x,
      ActivityService as,
      CommunityService cs,
      ForumService fs,
      ProgressService ps,
      PhotoStorage ph) {
    communities = c;
    gardens = g;
    plants = p;
    accounts = u;
    activities = a;
    participation = pa;
    submissions = s;
    reviews = r;
    inventory = i;
    posts = f;
    reports = cr;
    calendar = ca;
    notifications = n;
    access = x;
    activityService = as;
    communityService = cs;
    forum = fs;
    progress = ps;
    photos = ph;
  }

  @ModelAttribute
  public void shared(Model m) {
    var actor = access.currentOrNull();
    m.addAttribute("actor", actor);
    m.addAttribute("communities", communities.findAll());
    m.addAttribute("progress", progress);
    m.addAttribute("types", ActivityService.TYPES);
    m.addAttribute("today", LocalDate.now(ZoneId.of("America/Guayaquil")));
  }

  @GetMapping("/")
  public String home(Model m) {
    m.addAttribute("gardens", gardens.findAll());
    m.addAttribute("plantCount", plants.count());
    m.addAttribute(
        "approved", activities.findAll().stream().filter(a -> a.status.equals("APROBADA")).count());
    return "home";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/comunidades/{id}")
  public String community(@PathVariable Long id, Model m) {
    m.addAttribute("community", communityService.community(id));
    m.addAttribute(
        "garden",
        gardens.findAll().stream().filter(g -> g.communityId.equals(id)).findFirst().orElse(null));
    m.addAttribute(
        "plants", plants.findAll().stream().filter(p -> p.communityId.equals(id)).toList());
    return "community";
  }

  @GetMapping("/plantas")
  public String plants(@RequestParam(defaultValue = "") String q, Model m) {
    String query = q.toLowerCase(Locale.ROOT);
    m.addAttribute(
        "plants",
        plants.findAll().stream()
            .filter(p -> (p.name + " " + p.scientificName).toLowerCase(Locale.ROOT).contains(query))
            .toList());
    m.addAttribute("q", q);
    return "plants";
  }

  @GetMapping("/mapa")
  public String map(Model m) {
    m.addAttribute("gardens", gardens.findAll());
    return "map";
  }

  @GetMapping("/ranking")
  public String ranking(Model m) {
    m.addAttribute(
        "ranking",
        communities.findAll().stream()
            .sorted(
                Comparator.<Community>comparingInt(c -> progress.community(c.id))
                    .reversed()
                    .thenComparing(c -> c.name))
            .toList());
    return "ranking";
  }

  @GetMapping("/calendario")
  public String calendar(Model m) {
    m.addAttribute(
        "entries", calendar.findAll().stream().sorted(Comparator.comparing(c -> c.date)).toList());
    return "calendar";
  }

  @GetMapping("/gestion")
  public String manage(Model m) {
    var a = access.current();
    access.manage(a.communityId);
    m.addAttribute("community", communityService.community(a.communityId));
    m.addAttribute(
        "garden",
        gardens.findAll().stream()
            .filter(g -> g.communityId.equals(a.communityId))
            .findFirst()
            .orElse(new Garden()));
    m.addAttribute(
        "members",
        accounts.findAll().stream().filter(u -> a.communityId.equals(u.communityId)).toList());
    return "manage";
  }

  @PostMapping("/gestion/comunidad")
  public String updateCommunity(
      @RequestParam String name,
      @RequestParam String province,
      @RequestParam String parish,
      @RequestParam String description) {
    communityService.update(name, province, parish, description);
    return "redirect:/gestion";
  }

  @PostMapping("/gestion/integrantes")
  public String member(
      @RequestParam String username, @RequestParam String name, @RequestParam String password) {
    communityService.addMember(username, name, password);
    return "redirect:/gestion";
  }

  @PostMapping("/gestion/integrantes/{id}/estado")
  public String toggle(@PathVariable Long id) {
    communityService.toggleMember(id);
    return "redirect:/gestion";
  }

  @PostMapping("/gestion/huerto")
  public String garden(
      @RequestParam String name,
      @RequestParam String address,
      @RequestParam double latitude,
      @RequestParam double longitude,
      @RequestParam double area,
      @RequestParam String design,
      @RequestParam String soil,
      @RequestParam String water,
      @RequestParam String compost,
      @RequestParam String fence) {
    communityService.garden(
        name, address, latitude, longitude, area, design, soil, water, compost, fence);
    return "redirect:/gestion";
  }

  @GetMapping("/actividades")
  public String activities(Model m) {
    var a = access.current();
    List<Activity> list =
        activities.findAll().stream()
            .filter(
                x ->
                    x.communityId.equals(a.communityId)
                        || a.role.equals("REVISOR") && access.assigned(a.id, x.communityId))
            .sorted(Comparator.comparing((Activity x) -> x.id).reversed())
            .toList();
    m.addAttribute("activities", list);
    m.addAttribute(
        "members",
        accounts.findAll().stream()
            .filter(u -> a.communityId != null && a.communityId.equals(u.communityId) && u.active)
            .toList());
    return "activities";
  }

  @PostMapping("/gestion/actividades")
  public String createActivity(
      @RequestParam String type,
      @RequestParam String description,
      @RequestParam LocalDate date,
      @RequestParam(required = false) List<Long> selected,
      @RequestParam MultipartFile photo) {
    var a = activityService.create(type, description, date, selected, photo);
    return "redirect:/actividades/" + a.id;
  }

  @GetMapping("/actividades/{id}")
  public String activity(@PathVariable Long id, Model m) {
    var a = activityService.get(id);
    access.evidence(a);
    m.addAttribute("activity", a);
    m.addAttribute("latest", activityService.latest(id));
    m.addAttribute(
        "submissions",
        submissions.findAll().stream()
            .filter(s -> s.activityId.equals(id))
            .sorted(Comparator.comparing((Submission s) -> s.id).reversed())
            .toList());
    m.addAttribute(
        "reviews", reviews.findAll().stream().filter(r -> r.activityId.equals(id)).toList());
    m.addAttribute(
        "participants",
        participation.findAll().stream()
            .filter(p -> p.activityId.equals(id))
            .map(p -> accounts.findById(p.accountId).orElseThrow().displayName)
            .toList());
    return "activity";
  }

  @PostMapping("/gestion/actividades/{id}/reenviar")
  public String resend(
      @PathVariable Long id,
      @RequestParam String description,
      @RequestParam(required = false) MultipartFile photo) {
    activityService.revise(id, description, photo);
    return "redirect:/actividades/" + id;
  }

  @GetMapping("/revision")
  public String review(Model m) {
    var a = access.current();
    m.addAttribute(
        "activities",
        activities.findAll().stream()
            .filter(x -> x.status.equals("PENDIENTE") && access.assigned(a.id, x.communityId))
            .toList());
    return "review";
  }

  @PostMapping("/revision/{id}")
  public String decide(
      @PathVariable Long id,
      @RequestParam Long submissionId,
      @RequestParam String decision,
      @RequestParam(defaultValue = "") String observations) {
    activityService.decide(id, submissionId, decision, observations);
    return "redirect:/actividades/" + id;
  }

  @GetMapping("/evidencias/{id}")
  @ResponseBody
  public ResponseEntity<byte[]> image(@PathVariable Long id) {
    var s = submissions.findById(id).orElseThrow();
    access.evidence(activityService.get(s.activityId));
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .cacheControl(CacheControl.noStore())
        .header("X-Content-Type-Options", "nosniff")
        .body(photos.read(s.photoKey));
  }

  @GetMapping("/bodega")
  public String inventory(Model m) {
    var a = access.member();
    m.addAttribute(
        "items",
        inventory.findAll().stream().filter(i -> i.communityId.equals(a.communityId)).toList());
    return "inventory";
  }

  @PostMapping("/gestion/bodega")
  public String item(
      @RequestParam(required = false) Long id,
      @RequestParam String name,
      @RequestParam String category,
      @RequestParam int quantity,
      @RequestParam String unit) {
    communityService.item(id, name, category, quantity, unit);
    return "redirect:/bodega";
  }

  @PostMapping("/gestion/bodega/{id}/eliminar")
  public String deleteItem(@PathVariable Long id) {
    communityService.deleteItem(id);
    return "redirect:/bodega";
  }

  @GetMapping("/gestion/plantas")
  public String plantManagement(@RequestParam(required = false) Long id, Model m) {
    var a = access.current();
    access.manage(a.communityId);
    Plant p = id == null ? new Plant() : plants.findById(id).orElseThrow();
    if (id != null) access.manage(p.communityId);
    m.addAttribute("plant", p);
    m.addAttribute(
        "plants",
        plants.findAll().stream().filter(x -> x.communityId.equals(a.communityId)).toList());
    return "plant-edit";
  }

  @PostMapping("/gestion/plantas")
  public String savePlant(
      @RequestParam(required = false) Long id,
      @RequestParam String name,
      @RequestParam String scientific,
      @RequestParam(defaultValue = "") String aliases,
      @RequestParam String cycle,
      @RequestParam String use,
      @RequestParam String source) {
    communityService.plant(id, name, scientific, aliases, cycle, use, source);
    return "redirect:/gestion/plantas";
  }

  @GetMapping("/foro")
  public String forum(Model m) {
    var a = access.member();
    var list =
        posts.findAll().stream()
            .filter(p -> p.communityId.equals(a.communityId) && !p.hidden)
            .sorted(Comparator.comparing((ForumPost p) -> p.id).reversed())
            .toList();
    m.addAttribute("posts", list.stream().filter(p -> p.parentId == null).toList());
    m.addAttribute("comments", list.stream().filter(p -> p.parentId != null).toList());
    m.addAttribute(
        "reports",
        reports.findAll().stream()
            .filter(
                r ->
                    posts
                        .findById(r.postId)
                        .map(p -> p.communityId.equals(a.communityId))
                        .orElse(false))
            .toList());
    return "forum";
  }

  @PostMapping("/foro/publicar")
  public String publish(
      @RequestParam(required = false) Long parent,
      @RequestParam String title,
      @RequestParam String body) {
    forum.publish(parent, title, body);
    return "redirect:/foro";
  }

  @PostMapping("/foro/{id}/eliminar")
  public String deletePost(@PathVariable Long id) {
    forum.delete(id);
    return "redirect:/foro";
  }

  @PostMapping("/foro/{id}/reportar")
  public String report(@PathVariable Long id, @RequestParam String reason) {
    forum.report(id, reason);
    return "redirect:/foro";
  }

  @PostMapping("/gestion/reportes/{id}")
  public String moderate(
      @PathVariable Long id,
      @RequestParam String resolution,
      @RequestParam(defaultValue = "false") boolean hide) {
    forum.moderate(id, resolution, hide);
    return "redirect:/foro";
  }

  @GetMapping("/perfil")
  public String profile(Model m) {
    var a = access.current();
    m.addAttribute(
        "notifications",
        notifications.findAll().stream()
            .filter(n -> n.accountId.equals(a.id))
            .sorted(Comparator.comparing((Notification n) -> n.id).reversed())
            .toList());
    m.addAttribute(
        "individualRanking",
        a.communityId == null
            ? List.of()
            : accounts.findAll().stream()
                .filter(u -> a.communityId.equals(u.communityId))
                .sorted(
                    Comparator.<Account>comparingInt(u -> progress.individual(u.id))
                        .reversed()
                        .thenComparing(u -> u.displayName))
                .toList());
    return "profile";
  }

  @PostMapping("/perfil/notificaciones/{id}")
  public String seen(@PathVariable Long id) {
    var a = access.current();
    var n = notifications.findById(id).orElseThrow();
    if (!n.accountId.equals(a.id))
      throw new org.springframework.security.access.AccessDeniedException("Notificación privada");
    n.seen = true;
    notifications.save(n);
    return "redirect:/perfil";
  }
}
