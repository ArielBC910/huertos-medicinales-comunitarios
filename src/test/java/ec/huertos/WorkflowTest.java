package ec.huertos;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import ec.huertos.service.*;
import ec.huertos.storage.PhotoStorage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:workflow;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "huertos.storage=target/test-uploads"
    })
@ActiveProfiles("demo")
@AutoConfigureMockMvc
@Transactional
public class WorkflowTest {
  @Autowired MockMvc mvc;
  @Autowired AccountRepository accounts;
  @Autowired CommunityRepository communities;
  @Autowired ActivityRepository activities;
  @Autowired ActivityService service;
  @Autowired ProgressService progress;
  @Autowired CommunityService communityService;
  @Autowired PointMovementRepository points;
  @Autowired ReviewRepository reviews;
  @Autowired SubmissionRepository submissions;
  @Autowired PhotoStorage photos;
  @Autowired ForumService forum;
  @Autowired ForumPostRepository posts;
  @Autowired ContentReportRepository reports;
  @Autowired ReviewerAssignmentRepository assignments;
  @Autowired ParticipationRepository participation;

  protected Account account(String username) {
    return accounts.findByUsername(username).orElseThrow();
  }

  protected void actor(String username) {
    var a = account(username);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                a.username, "", AuthorityUtils.createAuthorityList("ROLE_" + a.role)));
  }

  protected MockMultipartFile photo() throws IOException {
    var out = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB), "png", out);
    return new MockMultipartFile("photo", "huerto.png", "image/png", out.toByteArray());
  }

  protected Activity create() throws Exception {
    actor("gestor.demo");
    return service.create(
        "SIE-03",
        "Jornada comunitaria de siembra y cuidado.",
        LocalDate.now(java.time.ZoneId.of("America/Guayaquil")).minusDays(1),
        List.of(account("participante.demo").id),
        photo());
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void publicScreensRender() throws Exception {
    for (String path :
        List.of(
            "/",
            "/mapa",
            "/plantas",
            "/plantas?q=menta",
            "/ranking",
            "/calendario",
            "/login",
            "/comunidades/" + account("gestor.demo").communityId))
      mvc.perform(get(path)).andExpect(status().isOk());
  }

  @Test
  void privateScreensRenderForCorrectRoles() throws Exception {
    for (String path :
        List.of("/gestion", "/gestion/plantas", "/actividades", "/bodega", "/foro", "/perfil"))
      mvc.perform(get(path).with(user("gestor.demo").roles("GESTOR"))).andExpect(status().isOk());
    mvc.perform(get("/revision").with(user("revisor.demo").roles("REVISOR")))
        .andExpect(status().isOk());
  }

  @Test
  void loginUsesStoredPassword() throws Exception {
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", "gestor.demo")
                .param("password", "HuertosDemo2026!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  void unauthorizedAndCsrfDenied() throws Exception {
    mvc.perform(get("/gestion")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/gestion").with(user("participante.demo").roles("PARTICIPANTE")))
        .andExpect(status().isForbidden());
    mvc.perform(post("/gestion/comunidad").with(user("gestor.demo").roles("GESTOR")))
        .andExpect(status().isForbidden());
  }

  @Test
  void approvalAtomicAndNoDuplicateReward() throws Exception {
    var a = create();
    Long community = a.communityId, member = account("participante.demo").id;
    assertEquals(0, progress.community(community));
    assertEquals(0, progress.growth(community));
    actor("revisor.demo");
    service.decide(a.id, service.latest(a.id).id, "APROBADA", "Cultivo documentado");
    assertEquals(10, progress.community(community));
    assertEquals(20, progress.individual(member));
    assertEquals(20, progress.growth(community));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.decide(a.id, service.latest(a.id).id, "APROBADA", "Reintento"));
    assertEquals(2, points.count());
    assertEquals(1, reviews.count());
    mvc.perform(get("/actividades/" + a.id).with(user("revisor.demo").roles("REVISOR")))
        .andExpect(status().isOk());
  }

  @Test
  void rejectionResubmissionKeepsAuditAndFixedParticipants() throws Exception {
    var a = create();
    Long original = service.latest(a.id).id;
    actor("revisor.demo");
    service.decide(a.id, original, "RECHAZADA", "La fotografía necesita más detalle");
    assertEquals(0, progress.community(a.communityId));
    assertEquals(0, progress.growth(a.communityId));
    actor("gestor.demo");
    service.revise(a.id, "Descripción corregida con información de la jornada", photo());
    assertNotEquals(original, service.latest(a.id).id);
    assertEquals(1, participation.count());
    assertEquals(2, submissions.count());
    actor("revisor.demo");
    service.decide(a.id, service.latest(a.id).id, "APROBADA", "Correcto");
    assertEquals(2, reviews.count());
    assertEquals(10, progress.community(a.communityId));
  }

  @Test
  void staleSubmissionCannotBeReviewed() throws Exception {
    var a = create();
    Long original = service.latest(a.id).id;
    service.revise(a.id, "Nueva descripción pendiente para revisión", photo());
    actor("revisor.demo");
    assertThrows(
        IllegalArgumentException.class, () -> service.decide(a.id, original, "APROBADA", ""));
    assertEquals(0, points.count());
  }

  @Test
  void rejectionRequiresExplanation() throws Exception {
    var a = create();
    actor("revisor.demo");
    assertThrows(
        IllegalArgumentException.class,
        () -> service.decide(a.id, service.latest(a.id).id, "RECHAZADA", ""));
    assertEquals("PENDIENTE", service.get(a.id).status);
  }

  @Test
  void reviewerAssignmentAndSelfReviewEnforced() throws Exception {
    var a = create();
    actor("gestor.demo");
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> service.decide(a.id, service.latest(a.id).id, "APROBADA", ""));
    var reviewer = account("revisor.demo");
    assignments.deleteAll();
    assignments.flush();
    actor("revisor.demo");
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> service.decide(a.id, service.latest(a.id).id, "APROBADA", ""));
    var r = new ReviewerAssignment();
    r.communityId = a.communityId;
    r.reviewerId = reviewer.id;
    assignments.save(r);
    a.authorId = reviewer.id;
    activities.save(a);
    assertThrows(
        IllegalArgumentException.class,
        () -> service.decide(a.id, service.latest(a.id).id, "APROBADA", ""));
  }

  @Test
  void membershipLimitAndInactiveSpace() {
    actor("gestor.demo");
    var gestor = account("gestor.demo");
    for (int i = 0; i < 23; i++) {
      var a = new Account();
      a.username = "extra" + i;
      a.password = "unused";
      a.displayName = "Integrante";
      a.role = "PARTICIPANTE";
      a.communityId = gestor.communityId;
      a.active = true;
      accounts.save(a);
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> communityService.addMember("nuevo", "Nuevo integrante", "ContrasenaDemo2026!"));
    communityService.toggleMember(account("participante.demo").id);
    assertNotNull(communityService.addMember("nuevo", "Nuevo integrante", "ContrasenaDemo2026!"));
  }

  @Test
  void foreignAndInactiveParticipantsDenied() throws Exception {
    actor("gestor.demo");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.create(
                "SIE-03",
                "Una actividad válida de prueba",
                LocalDate.now(java.time.ZoneId.of("America/Guayaquil")),
                List.of(account("revisor.demo").id),
                photo()));
    var member = account("participante.demo");
    member.active = false;
    accounts.save(member);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.create(
                "SIE-03",
                "Una actividad válida de prueba",
                LocalDate.now(java.time.ZoneId.of("America/Guayaquil")),
                List.of(member.id),
                photo()));
  }

  @Test
  void crossCommunityManagementAndEvidenceDenied() throws Exception {
    var a = create();
    var other =
        communities.findAll().stream()
            .filter(c -> !c.id.equals(a.communityId))
            .findFirst()
            .orElseThrow();
    var gestor = account("gestor.demo");
    gestor.communityId = other.id;
    accounts.save(gestor);
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> service.revise(a.id, "No debería modificarse esta actividad", null));
    mvc.perform(
            get("/evidencias/" + service.latest(a.id).id).with(user("gestor.demo").roles("GESTOR")))
        .andExpect(status().isForbidden());
  }

  @Test
  void uploadRejectsDisguisedHtml() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            photos.save(
                new MockMultipartFile(
                    "photo", "foto.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes())));
  }

  @Test
  void futureDateAndInvalidTypeDenied() throws Exception {
    actor("gestor.demo");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.create(
                "SIE-03",
                "Actividad de prueba inválida",
                LocalDate.now(java.time.ZoneId.of("America/Guayaquil")).plusDays(2),
                List.of(account("participante.demo").id),
                photo()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.create(
                "OTRO",
                "Actividad de prueba inválida",
                LocalDate.now(java.time.ZoneId.of("America/Guayaquil")),
                List.of(account("participante.demo").id),
                photo()));
  }

  @Test
  void forumOwnershipModerationAndEscaping() throws Exception {
    actor("participante.demo");
    forum.publish(null, "Prueba de siembra", "<script>alert('xss')</script>");
    var p = posts.findAll().stream().max(Comparator.comparing(x -> x.id)).orElseThrow();
    forum.report(p.id, "Contenido fuera de tema");
    actor("gestor.demo");
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class, () -> forum.delete(p.id));
    mvc.perform(get("/foro").with(user("gestor.demo").roles("GESTOR")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")));
    actor("gestor.demo");
    forum.moderate(reports.findAll().getFirst().id, "Se oculta por estar fuera de tema", true);
    assertTrue(posts.findById(p.id).orElseThrow().hidden);
  }

  @Test
  void personalAwardsDoNotMultiplyCommunityPoints() throws Exception {
    actor("gestor.demo");
    var a =
        service.create(
            "CUI-04",
            "Riego comunitario de prueba",
            LocalDate.now(java.time.ZoneId.of("America/Guayaquil")),
            List.of(account("participante.demo").id, account("gestor.demo").id),
            photo());
    actor("revisor.demo");
    service.decide(a.id, service.latest(a.id).id, "APROBADA", "");
    assertEquals(10, progress.community(a.communityId));
    assertEquals(10, progress.individual(account("gestor.demo").id));
    assertEquals(10, progress.individual(account("participante.demo").id));
    assertEquals(List.of("Primera semilla"), progress.badges(account("participante.demo").id));
  }
}
