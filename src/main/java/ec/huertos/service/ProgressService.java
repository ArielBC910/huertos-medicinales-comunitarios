package ec.huertos.service;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {
  private final PointMovementRepository points;
  private final ActivityRepository activities;
  private final ParticipationRepository participation;

  @Value("${huertos.levels.official}")
  int official;

  @Value("${huertos.levels.master}")
  int master;

  public ProgressService(
      PointMovementRepository p, ActivityRepository a, ParticipationRepository r) {
    points = p;
    activities = a;
    participation = r;
  }

  public int community(Long id) {
    return points.findAll().stream()
        .filter(p -> p.communityId.equals(id) && p.accountId == null)
        .mapToInt(p -> p.amount)
        .sum();
  }

  public int individual(Long id) {
    return points.findAll().stream()
        .filter(p -> id.equals(p.accountId))
        .mapToInt(p -> p.amount)
        .sum();
  }

  public String level(Long id) {
    int n = individual(id);
    return n >= master ? "Maestro" : n >= official ? "Oficial" : "Aprendiz";
  }

  public long confirmed(Long id) {
    return activities.findAll().stream()
        .filter(a -> a.communityId.equals(id) && a.status.equals("APROBADA"))
        .count();
  }

  public int growth(Long id) {
    return (int) Math.min(100, confirmed(id) * 20);
  }

  public List<Activity> approvedFor(Long user) {
    var ids =
        participation.findAll().stream()
            .filter(p -> p.accountId.equals(user))
            .map(p -> p.activityId)
            .toList();
    return activities.findAllById(ids).stream().filter(a -> a.status.equals("APROBADA")).toList();
  }

  public List<String> badges(Long user) {
    var a = approvedFor(user);
    List<String> result = new ArrayList<>();
    if (!a.isEmpty()) result.add("Primera semilla");
    if (a.size() >= 5) result.add("Manos a la tierra");
    if (a.stream().anyMatch(x -> x.type.startsWith("COS"))) result.add("Primera cosecha");
    return result;
  }

  public int streak(Long user) {
    var dates =
        approvedFor(user).stream().map(a -> a.date).collect(java.util.stream.Collectors.toSet());
    LocalDate d = LocalDate.now(ZoneId.of("America/Guayaquil"));
    if (!dates.contains(d)) d = d.minusDays(1);
    int count = 0;
    while (dates.contains(d)) {
      count++;
      d = d.minusDays(1);
    }
    return count;
  }
}
