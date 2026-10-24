package ec.huertos;

import static org.junit.jupiter.api.Assertions.*;

import ec.huertos.persistence.*;
import ec.huertos.service.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:concurrency;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "huertos.storage=target/test-uploads"
    })
@ActiveProfiles("demo")
class ConcurrencyTest {
  @Autowired ActivityService service;
  @Autowired AccountRepository accounts;
  @Autowired PointMovementRepository points;

  void actor(String user, String role) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                user, "", AuthorityUtils.createAuthorityList("ROLE_" + role)));
  }

  @Test
  void simultaneousApprovalsCreditOnlyOnce() throws Exception {
    actor("gestor.demo", "GESTOR");
    var output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
    var a =
        service.create(
            "CUI-04",
            "Riego para prueba de concurrencia",
            LocalDate.now().minusDays(1),
            List.of(accounts.findByUsername("participante.demo").orElseThrow().id),
            new MockMultipartFile("photo", "photo.png", "image/png", output.toByteArray()));
    Long submission = service.latest(a.id).id;
    SecurityContextHolder.clearContext();
    var barrier = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      Callable<Boolean> review =
          () -> {
            actor("revisor.demo", "REVISOR");
            try {
              barrier.await(5, TimeUnit.SECONDS);
              service.decide(a.id, submission, "APROBADA", "Validada en prueba");
              return true;
            } catch (IllegalArgumentException e) {
              return false;
            } finally {
              SecurityContextHolder.clearContext();
            }
          };
      var first = executor.submit(review);
      var second = executor.submit(review);
      barrier.countDown();
      int successes =
          (first.get(15, TimeUnit.SECONDS) ? 1 : 0) + (second.get(15, TimeUnit.SECONDS) ? 1 : 0);
      assertEquals(1, successes);
    }
    assertEquals(2, points.findAll().stream().filter(p -> p.activityId.equals(a.id)).count());
  }
}
