package ec.huertos.service;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Access {
  private final AccountRepository accounts;
  private final ReviewerAssignmentRepository assignments;

  public Access(AccountRepository accounts, ReviewerAssignmentRepository assignments) {
    this.accounts = accounts;
    this.assignments = assignments;
  }

  public Account currentOrNull() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth == null
        ? null
        : accounts.findByUsername(auth.getName()).filter(a -> a.active).orElse(null);
  }

  public Account current() {
    var a = currentOrNull();
    if (a == null) throw new AccessDeniedException("Debes iniciar sesión");
    return a;
  }

  public Account manage(Long community) {
    var a = current();
    if (!a.role.equals("GESTOR") || !community.equals(a.communityId))
      throw new AccessDeniedException("Solo puedes gestionar tu comunidad");
    return a;
  }

  public Account review(Long community) {
    var a = current();
    if (!a.role.equals("REVISOR") || !assigned(a.id, community))
      throw new AccessDeniedException("Comunidad no asignada para revisión");
    return a;
  }

  public boolean assigned(Long user, Long community) {
    return assignments.findAll().stream()
        .anyMatch(x -> x.reviewerId.equals(user) && x.communityId.equals(community));
  }

  public Account member() {
    var a = current();
    if (a.communityId == null || !a.role.matches("GESTOR|PARTICIPANTE"))
      throw new AccessDeniedException("Se requiere pertenecer a una comunidad");
    return a;
  }

  public void evidence(Activity activity) {
    var a = current();
    if (activity.communityId.equals(a.communityId)
        || a.role.equals("REVISOR") && assigned(a.id, activity.communityId)) return;
    throw new AccessDeniedException("Evidencia privada de otra comunidad");
  }
}
