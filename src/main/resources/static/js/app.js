document.querySelectorAll("[data-confirm]").forEach((button) =>
  button.addEventListener("click", (event) => {
    if (!confirm(button.dataset.confirm)) event.preventDefault();
  }),
);
document.querySelectorAll(".sidebar nav a").forEach((a) => {
  if (a.pathname === location.pathname) {
    a.classList.add("active");
    a.setAttribute("aria-current", "page");
  }
});
document
  .querySelectorAll('form[enctype="multipart/form-data"]')
  .forEach((form) => {
    const token = form.querySelector('input[name="_csrf"]');
    // CSRF is checked before the servlet reads the multipart body. Put the token in
    // the same-origin form action. Cross-origin referrers contain only the origin.
    if (token)
      form.action +=
        (form.action.includes("?") ? "&" : "?") +
        "_csrf=" +
        encodeURIComponent(token.value);
    form.addEventListener("submit", (e) => {
      const photo = form.querySelector("input[type=file]");
      if (photo?.files[0]?.size > 5 * 1024 * 1024) {
        e.preventDefault();
        alert("La fotografía supera los 5 MB.");
      }
    });
  });
