if (typeof L !== "undefined") {
  const map = L.map("map").setView([-1.3, -78.5], 7);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 18,
    attribution:
      '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  }).addTo(map);
  const points = [];
  document.querySelectorAll("[data-lat]").forEach((card) => {
    const lat = Number(card.dataset.lat),
      lng = Number(card.dataset.lng);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) return;
    const popup = document.createElement("div"),
      title = document.createElement("strong"),
      link = document.createElement("a");
    title.textContent = card.dataset.title;
    link.textContent = "Conocer comunidad →";
    link.href = card.dataset.url;
    popup.append(title, document.createElement("br"), link);
    L.marker([lat, lng]).addTo(map).bindPopup(popup);
    points.push([lat, lng]);
  });
  if (points.length) map.fitBounds(points, { padding: [45, 45], maxZoom: 11 });
}
