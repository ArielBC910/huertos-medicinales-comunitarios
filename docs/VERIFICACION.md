# Verificación de la entrega

Fecha local de trabajo: 23 de octubre de 2026, Ecuador continental / Colombia (UTC−5).

## Compilación y pruebas

- Compilación dirigida a Java 21 y JAR ejecutable generado con Spring Boot.
- Suite local con JDK 21: 17 pruebas ejecutadas correctamente; 16 pruebas de PostgreSQL omitidas por no existir Docker local.
- Se repitió la verificación local con zona horaria UTC para evitar diferencias con CI; los cálculos de fecha de negocio y las pruebas usan `America/Guayaquil`.
- [GitHub Actions con Java 21 y PostgreSQL real: ejecución correcta](https://github.com/ArielBC910/huertos-medicinales-comunitarios/actions/runs/40052086647).
- Pruebas de aceptación: roles, CSRF, privacidad entre comunidades, límites de integrantes, participantes fijos, reenvíos, revisión desactualizada, recompensas independientes, archivos falsificados, moderación y escape HTML.
- Prueba concurrente: dos intentos simultáneos de aprobar una actividad producen una sola decisión exitosa y un único conjunto de movimientos.

## Ejecución real

Se inició el JAR con Java 21 en Windows y se confirmó HTTP 200 en la página principal. Se recorrió mediante HTTP real, con cookies de sesión y CSRF, el inicio de sesión de gestor y revisor, carga multipart de una imagen, rechazo con observación, corrección y reenvío, aprobación y rechazo de una decisión repetida. La actividad local #1 está identificada como prueba técnica; su imagen es una captura del prototipo y no documenta una jornada agrícola real. Estos datos locales no se suben al repositorio.

En el entorno aislado de Windows, la ruta TEMP de los sockets Unix del JDK provocó un fallo interno de Tomcat. Se comprobó el arranque con `-Djdk.net.unixdomain.tmpdir` apuntando a una carpeta del proyecto. El lanzador PowerShell incorpora ese ajuste; no modifica la configuración global del equipo.

## Inspección de interfaz

Revisión en navegador de escritorio y viewport móvil de 390 × 844. Se comprobaron inicio de sesión, navegación por rol, disposición móvil, mapa base y marcadores de Leaflet. Las teselas cargan desde OpenStreetMap con atribución visible y política de referencia compatible con el proveedor.

- [Inicio](screenshots/inicio.png)
- [Móvil](screenshots/movil.png)
- [Mapa y marcador comunitario](screenshots/mapa.png)

## Límites de esta verificación

No se ejecutaron una prueba de carga de tres segundos, auditoría formal WCAG, auditoría de seguridad ni un ensayo de restauración Docker. El calendario lunar, fuentes editoriales definitivas, valores de recompensas y umbrales siguen requiriendo validación del equipo. La cobertura y los diferidos se detallan en `TRAZABILIDAD.md`.
