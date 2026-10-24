# Análisis de las fuentes y decisiones aplicadas

Se leyeron íntegramente los párrafos y tablas de ambos DOCX y las siete hojas del XLSX proporcionados. Se trataron como material de requisitos; sus notas sobre siguientes pasos no sustituyen la petición del usuario de construir y subir el prototipo.

| Fuente                                                                 | Papel en el diseño                                                                                                                     |
| ---------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| Documento_de_cambios_arquitectura_Huertos_Medicinales.docx             | Stack Java 21 / Spring Boot, monolito por capas, Spring Security, Flyway, PostgreSQL, almacenamiento abstraído, consistencia y pruebas |
| Propuesta_Alcance_V1_Huertos_Medicinales_Comunitarios_actualizada.docx | Flujo general, módulos, requisitos RF01–RF26 y RNF01–RNF12, propuesta inicial de roles y alcance                                       |
| Requerimientos_Huertos_Medicinales_Comunitarios.xlsx                   | Estados de revisión, matriz de actores, reglas aprobadas, catálogo de 16 actividades y prioridades originales                          |

## Resolución de diferencias

1. **Progreso virtual:** la propuesta DOCX y RF10 del XLSX hablan de avance provisional, pero RB05 y RB09 aprobadas en el mismo XLSX disponen que pendientes y rechazos no cambien el huerto. RB09 dice expresamente que reemplaza el comportamiento anterior. Se calcula progreso solo al aprobar.
2. **Actores:** se preservan gestor, participante, administrador de evidencias y visitante. La hoja de roles confirma visitante público sin cuenta y rol único por usuario. No se convierten en ADMIN/COORDINADOR/MIEMBRO.
3. **Ámbito:** el gestor administra únicamente su comunidad; el revisor solo revisa comunidades asignadas. Se conserva la prohibición de autorevisión aunque el rol único ya reduce ese riesgo.
4. **Participantes:** RB03 aprobada fija la selección original para siempre. La edición permitida en RB02 afecta descripción y fotografía; un reenvío no modifica la lista ni crea una nueva actividad recompensable.
5. **Comunidad:** máximo 25 integrantes activos, contando al gestor. Las bajas liberan cupo; se bloquea la fila comunitaria para serializar incorporaciones/reactivaciones concurrentes.
6. **Puntos:** RB06 sigue pendiente de decisión. Se implementan reglas de ejemplo configurables, sin presentarlas como aprobadas. RB07 y RB08 sí exigen recompensa igual entre seleccionados y separación de puntos personales/comunitarios.
7. **Foro:** la matriz asigna moderación al gestor de su comunidad. Un participante crea y elimina su contenido; no lo edita. El prototipo incluye reportes y decisiones humanas auditadas; filtro temático automático y procedimiento de apelación siguen sin definir.
8. **Alcance adicional:** aunque la revisión arquitectónica coloca foro y bodega como opcionales, la matriz los aprueba. Se incluyen con operaciones básicas. RF24 está pendiente de revisión y RF25 se reserva expresamente para un próximo incremento.

## Decisiones técnicas asumidas para el prototipo

No son acuerdos nuevos del equipo: un huerto por comunidad, alta de comunidades/gestores y revisores por operador, fotos PNG/JPEG hasta 5 MB y 20 megapíxeles, empates de ranking por orden alfabético, fases lunares pendientes de validación y configuración numérica de ejemplo. Se documentan para que el equipo pueda reemplazarlas sin rehacer el flujo principal.

Rachas: días consecutivos de actividad realizada en Ecuador continental (`America/Guayaquil`), solo con participación aprobada; una racha sigue vigente si la última participación fue hoy o ayer. Las insignias se derivan de primera actividad, cinco actividades y primera cosecha aprobadas. El hito visual alcanza el 100% con cinco actividades. Estas reglas no se atribuyen a los documentos.

No se incorporaron IA, aprobaciones automáticas, PWA, modo sin conexión, notificaciones push, APIs astronómicas, aplicación nativa, inventario empresarial, GPS avanzado ni prescripción médica.

## Referencias técnicas consultadas

- [Compatibilidad de Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Protección CSRF de Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Integración Thymeleaf y Spring Security](https://www.thymeleaf.org/doc/articles/springsecurity.html)

El proyecto apunta a Java 21. La compilación y pruebas locales usan el JDK 25 instalado con `--release 21`; CI y Docker usan JDK 21.
