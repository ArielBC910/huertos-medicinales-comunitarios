package ec.huertos.config;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Operator-only bootstrap; deliberately no public registration or role-changing endpoint. */
@Component
@Profile("provision")
public class ProvisionAccount implements CommandLineRunner {
  private final CommunityRepository communities;
  private final AccountRepository accounts;
  private final ReviewerAssignmentRepository assignments;
  private final PasswordEncoder passwords;

  public ProvisionAccount(
      CommunityRepository c,
      AccountRepository a,
      ReviewerAssignmentRepository r,
      PasswordEncoder p) {
    communities = c;
    accounts = a;
    assignments = r;
    passwords = p;
  }

  @Override
  @Transactional
  public void run(String... args) {
    var env = System.getenv();
    String username = required(env, "PROVISION_USER"),
        password = required(env, "PROVISION_PASSWORD"),
        role = required(env, "PROVISION_ROLE");
    if (!Set.of("GESTOR", "REVISOR").contains(role)
        || !username.matches("[a-z0-9._-]{3,40}")
        || password.length() < 12
        || password.length() > 72
        || accounts.findByUsername(username).isPresent())
      throw new IllegalArgumentException(
          "Datos de aprovisionamiento inválidos o usuario existente");
    var a = new Account();
    a.username = username;
    a.password = passwords.encode(password);
    a.displayName = required(env, "PROVISION_NAME");
    a.role = role;
    a.active = true;
    if (role.equals("GESTOR")) {
      var c = new Community();
      c.name = required(env, "PROVISION_COMMUNITY");
      c.province = required(env, "PROVISION_PROVINCE");
      c.parish = required(env, "PROVISION_PARISH");
      c.description = "Comunidad registrada. El gestor completará su información.";
      communities.save(c);
      a.communityId = c.id;
    }
    accounts.save(a);
    if (role.equals("REVISOR")) {
      for (String value : required(env, "PROVISION_COMMUNITY_IDS").split(",")) {
        Long id = Long.valueOf(value.strip());
        communities.findById(id).orElseThrow();
        var r = new ReviewerAssignment();
        r.reviewerId = a.id;
        r.communityId = id;
        assignments.save(r);
      }
    }
  }

  private String required(Map<String, String> e, String key) {
    String v = e.get(key);
    if (v == null || v.isBlank()) throw new IllegalArgumentException("Falta " + key);
    return v;
  }
}
