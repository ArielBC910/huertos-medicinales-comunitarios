package ec.huertos.service;

import ec.huertos.domain.*;
import ec.huertos.persistence.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityService {
  private final CommunityRepository communities;
  private final GardenRepository gardens;
  private final AccountRepository accounts;
  private final PlantRepository plants;
  private final InventoryItemRepository items;
  private final Access access;
  private final PasswordEncoder passwords;

  public CommunityService(
      CommunityRepository c,
      GardenRepository g,
      AccountRepository a,
      PlantRepository p,
      InventoryItemRepository i,
      Access x,
      PasswordEncoder e) {
    communities = c;
    gardens = g;
    accounts = a;
    plants = p;
    items = i;
    access = x;
    passwords = e;
  }

  public Community community(Long id) {
    return communities
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Comunidad no encontrada"));
  }

  @Transactional
  public void update(String name, String province, String parish, String description) {
    var a = access.current();
    access.manage(a.communityId);
    var c = community(a.communityId);
    c.name = ActivityService.text(name, 3, 120);
    c.province = ActivityService.text(province, 3, 100);
    c.parish = ActivityService.text(parish, 3, 100);
    c.description = ActivityService.text(description, 10, 2000);
    communities.save(c);
  }

  @Transactional
  public Account addMember(String username, String name, String password) {
    var actor = access.current();
    access.manage(actor.communityId);
    communities.lockById(actor.communityId).orElseThrow();
    if (accounts.findAll().stream()
            .filter(a -> actor.communityId.equals(a.communityId) && a.active)
            .count()
        >= 25) throw new IllegalArgumentException("La comunidad alcanzó 25 integrantes activos");
    if (username == null
        || !username.matches("[a-z0-9._-]{3,40}")
        || accounts.findByUsername(username).isPresent())
      throw new IllegalArgumentException(
          "Usuario ocupado o inválido. Usa 3 a 40 letras minúsculas, números, punto o guion");
    if (password == null || password.length() < 12 || password.length() > 72)
      throw new IllegalArgumentException("La contraseña requiere entre 12 y 72 caracteres");
    var a = new Account();
    a.username = username;
    a.displayName = ActivityService.text(name, 3, 100);
    a.password = passwords.encode(password);
    a.role = "PARTICIPANTE";
    a.communityId = actor.communityId;
    a.active = true;
    return accounts.save(a);
  }

  @Transactional
  public void toggleMember(Long id) {
    var a = accounts.findById(id).orElseThrow();
    access.manage(a.communityId);
    communities.lockById(a.communityId).orElseThrow();
    if (!a.role.equals("PARTICIPANTE"))
      throw new IllegalArgumentException("El gestor no se puede dar de baja desde esta pantalla");
    if (!a.active
        && accounts.findAll().stream()
                .filter(u -> a.communityId.equals(u.communityId) && u.active)
                .count()
            >= 25) throw new IllegalArgumentException("Límite de 25 integrantes activos");
    a.active = !a.active;
    accounts.save(a);
  }

  @Transactional
  public void garden(
      String name,
      String address,
      double lat,
      double lng,
      double area,
      String design,
      String soil,
      String water,
      String compost,
      String fence) {
    var actor = access.current();
    access.manage(actor.communityId);
    communities.lockById(actor.communityId).orElseThrow();
    if (!Double.isFinite(lat)
        || !Double.isFinite(lng)
        || lat < -5.1
        || lat > 1.6
        || lng < -92
        || lng > -75
        || !Double.isFinite(area)
        || area <= 0
        || area > 1000000)
      throw new IllegalArgumentException("Área o coordenadas de Ecuador inválidas");
    var g =
        gardens.findAll().stream()
            .filter(x -> x.communityId.equals(actor.communityId))
            .findFirst()
            .orElseGet(Garden::new);
    g.communityId = actor.communityId;
    g.name = ActivityService.text(name, 3, 120);
    g.address = ActivityService.text(address, 3, 200);
    g.latitude = lat;
    g.longitude = lng;
    g.area = area;
    g.design = ActivityService.text(design, 2, 100);
    g.soil = ActivityService.text(soil, 2, 100);
    g.water = ActivityService.text(water, 2, 100);
    g.compost = ActivityService.text(compost, 2, 100);
    g.fence = ActivityService.text(fence, 2, 100);
    gardens.save(g);
  }

  @Transactional
  public void plant(
      Long id,
      String name,
      String scientific,
      String aliases,
      String cycle,
      String use,
      String source) {
    var actor = access.current();
    access.manage(actor.communityId);
    var p = id == null ? new Plant() : plants.findById(id).orElseThrow();
    if (id != null) access.manage(p.communityId);
    p.communityId = actor.communityId;
    p.name = ActivityService.text(name, 2, 100);
    p.scientificName = ActivityService.text(scientific, 3, 150);
    p.otherNames = ActivityService.text(aliases, 0, 150);
    p.cycle = ActivityService.text(cycle, 2, 150);
    p.traditionalUse = ActivityService.text(use, 5, 2000);
    p.source = ActivityService.text(source, 3, 1000);
    plants.save(p);
  }

  @Transactional
  public void item(Long id, String name, String category, int quantity, String unit) {
    var actor = access.current();
    access.manage(actor.communityId);
    var i = id == null ? new InventoryItem() : items.findById(id).orElseThrow();
    if (id != null) access.manage(i.communityId);
    if (!Set.of("Herramientas", "Semillas y plántulas", "Abonos y compost").contains(category)
        || quantity < 0
        || quantity > 1000000) throw new IllegalArgumentException("Categoría o cantidad inválida");
    i.communityId = actor.communityId;
    i.name = ActivityService.text(name, 2, 100);
    i.category = category;
    i.quantity = quantity;
    i.unit = ActivityService.text(unit, 1, 30);
    items.save(i);
  }

  @Transactional
  public void deleteItem(Long id) {
    var item = items.findById(id).orElseThrow();
    access.manage(item.communityId);
    items.delete(item);
  }
}
