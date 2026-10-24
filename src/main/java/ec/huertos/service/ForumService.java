package ec.huertos.service;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForumService {
  private final ForumPostRepository posts;
  private final ContentReportRepository reports;
  private final Access access;

  public ForumService(ForumPostRepository p, ContentReportRepository r, Access a) {
    posts = p;
    reports = r;
    access = a;
  }

  @Transactional
  public void publish(Long parent, String title, String body) {
    var actor = access.member();
    var p = new ForumPost();
    if (parent != null) {
      var t = posts.findById(parent).orElseThrow();
      if (!t.communityId.equals(actor.communityId) || t.hidden || t.parentId != null)
        throw new IllegalArgumentException("Publicación no disponible");
    }
    p.communityId = actor.communityId;
    p.authorId = actor.id;
    p.parentId = parent;
    p.title = ActivityService.text(title, 3, 160);
    p.body = ActivityService.text(body, 5, 3000);
    p.createdAt = Instant.now();
    posts.save(p);
  }

  @Transactional
  public void delete(Long id) {
    var actor = access.member();
    var p = posts.findById(id).orElseThrow();
    if (!p.authorId.equals(actor.id))
      throw new org.springframework.security.access.AccessDeniedException(
          "Solo puedes eliminar tu propio contenido");
    p.hidden = true;
    posts.save(p);
  }

  @Transactional
  public void report(Long id, String reason) {
    var actor = access.member();
    var p = posts.findById(id).orElseThrow();
    if (!p.communityId.equals(actor.communityId) || p.hidden)
      throw new IllegalArgumentException("Contenido no disponible");
    var r = new ContentReport();
    r.postId = id;
    r.reporterId = actor.id;
    r.reason = ActivityService.text(reason, 5, 1000);
    r.status = "PENDIENTE";
    r.createdAt = Instant.now();
    reports.save(r);
  }

  @Transactional
  public void moderate(Long id, String resolution, boolean hide) {
    var r = reports.findById(id).orElseThrow();
    var p = posts.findById(r.postId).orElseThrow();
    var actor = access.manage(p.communityId);
    if (!r.status.equals("PENDIENTE")) throw new IllegalArgumentException("Reporte ya resuelto");
    r.resolution = ActivityService.text(resolution, 5, 1000);
    r.moderatorId = actor.id;
    r.status = hide ? "OCULTADO" : "CONSERVADO";
    if (hide) {
      p.hidden = true;
      posts.save(p);
    }
    reports.save(r);
  }
}
