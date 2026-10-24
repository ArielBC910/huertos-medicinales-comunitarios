package ec.huertos.config;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import java.time.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoData implements CommandLineRunner {
  private final CommunityRepository communities;
  private final AccountRepository accounts;
  private final GardenRepository gardens;
  private final PlantRepository plants;
  private final ReviewerAssignmentRepository assignments;
  private final InventoryItemRepository inventory;
  private final ForumPostRepository posts;
  private final CalendarEntryRepository calendar;
  private final PasswordEncoder encoder;

  public DemoData(
      CommunityRepository c,
      AccountRepository a,
      GardenRepository g,
      PlantRepository p,
      ReviewerAssignmentRepository r,
      InventoryItemRepository i,
      ForumPostRepository f,
      CalendarEntryRepository ca,
      PasswordEncoder e) {
    communities = c;
    accounts = a;
    gardens = g;
    plants = p;
    assignments = r;
    inventory = i;
    posts = f;
    calendar = ca;
    encoder = e;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (communities.count() > 0) return;
    String[] names = {"Kawsay", "Semillas del Valle", "Raíces de la Amazonía"};
    String[] provinces = {"Pichincha", "Imbabura", "Napo"};
    String[] parishes = {"Calderón", "San Pablo del Lago", "Archidona"};
    double[] lat = {-0.094, 0.209, -0.909};
    double[] lng = {-78.423, -78.218, -77.808};
    Long first = null;
    for (int j = 0; j < 3; j++) {
      var c = new Community();
      c.name = names[j];
      c.province = provinces[j];
      c.parish = parishes[j];
      c.description =
          "Comunidad ficticia para explorar el cuidado colectivo del huerto y compartir saberes"
              + " ancestrales. Ubicación aproximada de demostración.";
      communities.save(c);
      if (j == 0) first = c.id;
      var g = new Garden();
      g.communityId = c.id;
      g.name = "Huerto " + names[j];
      g.address = "Sector comunitario de demostración";
      g.latitude = lat[j];
      g.longitude = lng[j];
      g.area = 120 + j * 30;
      g.design = "Camadas y senderos";
      g.soil = "Franco";
      g.water = "Recolección de lluvia";
      g.compost = "Compost comunitario";
      g.fence = "Cerca viva";
      gardens.save(g);
    }
    account("gestor.demo", "Elena · Gestora", "GESTOR", first);
    var participant = account("participante.demo", "Inti · Participante", "PARTICIPANTE", first);
    var reviewer = account("revisor.demo", "Revisión MSP · Demo", "REVISOR", null);
    for (var c : communities.findAll()) {
      var r = new ReviewerAssignment();
      r.reviewerId = reviewer.id;
      r.communityId = c.id;
      assignments.save(r);
    }
    String[][] data = {
      {
        "Manzanilla",
        "Matricaria chamomilla",
        "Camomila",
        "Anual",
        "Ficha pendiente de validación comunitaria. Se registrará el saber tradicional con su"
            + " fuente."
      },
      {
        "Hierbabuena",
        "Mentha spicata",
        "Menta verde",
        "Perenne",
        "Espacio para documentar los saberes locales sobre esta planta."
      },
      {
        "Toronjil",
        "Melissa officinalis",
        "Melisa",
        "Perenne",
        "Espacio para documentar relatos y prácticas de cultivo de la comunidad."
      }
    };
    for (var d : data) {
      var p = new Plant();
      p.communityId = first;
      p.name = d[0];
      p.scientificName = d[1];
      p.otherNames = d[2];
      p.cycle = d[3];
      p.traditionalUse = d[4];
      p.source = "Contenido de demostración. Validación y fuentes comunitarias pendientes.";
      plants.save(p);
    }
    var item = new InventoryItem();
    item.communityId = first;
    item.name = "Regaderas";
    item.category = "Herramientas";
    item.quantity = 3;
    item.unit = "unidades";
    inventory.save(item);
    var post = new ForumPost();
    post.communityId = first;
    post.authorId = participant.id;
    post.title = "¿Cómo organizamos el próximo día de siembra?";
    post.body =
        "Compartamos propuestas de turnos y preparación de las camadas. Este foro es una"
            + " demostración con datos ficticios.";
    post.createdAt = Instant.now();
    posts.save(post);
    for (int j = 0; j < 4; j++) {
      var e = new CalendarEntry();
      e.date = LocalDate.of(2026, 11, 2 + j * 7);
      e.phase = "Fase por validar";
      e.suggestion =
          new String[] {
                "Preparar la tierra y revisar el compost",
                "Planificar una jornada de siembra",
                "Organizar el cuidado y el riego",
                "Documentar la cosecha y el secado"
              }
              [j];
      e.source =
          "Agenda didáctica de noviembre de 2026. Sin efemérides lunares verificadas. Sustituir por"
              + " datos agrícolas y lunares aprobados por el equipo.";
      calendar.save(e);
    }
  }

  private Account account(String username, String name, String role, Long community) {
    var a = new Account();
    a.username = username;
    a.displayName = name;
    a.role = role;
    a.communityId = community;
    a.password = encoder.encode("HuertosDemo2026!");
    a.active = true;
    return accounts.save(a);
  }
}
