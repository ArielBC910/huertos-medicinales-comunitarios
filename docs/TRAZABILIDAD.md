# Matriz de trazabilidad

Transcripción de identificadores, prioridades y estados de revisión del XLSX. La cobertura describe el prototipo entregado; Aprobado en la fuente no equivale a verificación de producción.

## Requerimientos funcionales

| ID   | Requisito                | Prioridad | Revisión del equipo en Excel | Cobertura del prototipo                                                                                                                      |
| ---- | ------------------------ | --------- | ---------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| RF01 | Gestión de comunidades   | P1        | Aprobado                     | Implementado: alta mediante operador y actualización por gestor. CommunityService / ProvisionAccount.                                        |
| RF02 | Gestión de integrantes   | P1        | Aprobado                     | Implementado: alta, baja y reactivación, máximo 25 activos con bloqueo comunitario.                                                          |
| RF03 | Roles y permisos         | P1        | Aprobado                     | Implementado: rol único, visitante público, permisos HTTP y verificación de ámbito en servicios.                                             |
| RF04 | Registro del huerto      | P1        | Aprobado                     | Implementado: datos físicos y coordenadas validadas; un huerto por comunidad como supuesto.                                                  |
| RF05 | Catálogo de plantas      | P1        | Aprobado                     | Implementado: consulta/búsqueda, alta/edición comunitaria; fuentes de ejemplo pendientes de validación.                                      |
| RF06 | Registro de actividades  | P1        | Aprobado                     | Implementado: catálogo de 16 tipos, fecha, descripción y participantes fijos.                                                                |
| RF07 | Gestión de evidencias    | P1        | Aprobado                     | Implementado: fotografía privada PNG/JPEG, límites, versiones y estados.                                                                     |
| RF08 | Validación de evidencias | P1        | Aprobado                     | Implementado: decisión humana de revisor asignado, observación obligatoria para rechazar y auditabilidad.                                    |
| RF09 | Asignación de puntos     | P1        | Aprobado                     | Implementado: bloqueo transaccional y restricción única por actividad/beneficiario.                                                          |
| RF10 | Progreso virtual         | P1        | Aprobado                     | Implementado según RB09 aprobada: avance solo al aprobar. Se reemplaza el texto provisional de RF10.                                         |
| RF11 | Ranking comunitario      | P1        | Aprobado                     | Implementado: suma exclusiva de movimientos comunitarios y orden descendente.                                                                |
| RF12 | Mapa interactivo         | P1        | Aprobado                     | Implementado: Leaflet + OpenStreetMap, atribución visible y alternativa de listado.                                                          |
| RF13 | Puntos individuales      | P2        | Aprobado                     | Implementado: misma recompensa entre participantes seleccionados; separada de la recompensa comunitaria.                                     |
| RF14 | Niveles                  | P2        | Aprobado                     | Implementado con umbrales configurables de ejemplo; decisión numérica pendiente.                                                             |
| RF15 | Insignias                | P2        | Aprobado                     | Implementado como cálculo derivado: primera actividad, cinco actividades y primera cosecha.                                                  |
| RF16 | Misiones                 | P2        | Aprobado                     | Parcial: objetivo de cinco actividades e insignia derivada. Sin registro independiente de adjudicación ni catálogo editorial de misiones.    |
| RF17 | Foro                     | P2        | Aprobado                     | Implementado: publicaciones/comentarios comunitarios, eliminación propia y sin edición posterior.                                            |
| RF18 | Moderación               | P2        | Aprobado                     | Implementado básico: normas visibles, reportes y resolución humana auditada por el gestor. Filtro temático automático pendiente.             |
| RF19 | Bodega                   | P2        | Aprobado                     | Implementado: altas, consulta, modificación y eliminación con cantidades no negativas.                                                       |
| RF20 | Calendario agrícola      | P2        | Aprobado                     | Parcial: agenda precargada, fechas y fuentes visibles. Fases lunares marcadas por validar; faltan efemérides aprobadas.                      |
| RF21 | Historial de actividades | P2        | Aprobado                     | Implementado: versiones de envío, participantes, decisiones, observaciones, autores y fechas con acceso privado.                             |
| RF22 | Ranking individual       | P3        | Aprobado                     | Implementado en ámbito comunitario privado, con identificadores para terceros; ampliar alcance requiere decisión de privacidad.              |
| RF23 | Rachas                   | P3        | Aprobado                     | Implementado como cálculo derivado de días consecutivos con participación aprobada en America/Guayaquil; regla demo pendiente de aprobación. |
| RF24 | Historial visual         | P3        | Pendiente de revisión        | Pendiente de revisión del equipo. Existe historial de evidencias por actividad; no una galería cronológica comparativa del huerto.           |
| RF25 | Reportes                 | P3        | Para una proximo incremneto  | Diferido según la matriz: próximo incremento. No se implementan reportes exportables.                                                        |
| RF26 | Notificaciones internas  | P3        | Aprobado                     | Implementado: avisos al gestor tras cada decisión y marcado de lectura. Otros eventos requieren acuerdo.                                     |

## Requerimientos no funcionales

| ID    | Atributo             | Prioridad | Revisión del equipo en Excel | Verificación / límite                                                                                                 |
| ----- | -------------------- | --------- | ---------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| RNF01 | Seguridad y acceso   | P1        | Aprobado                     | Implementado y probado: controles por rol, comunidad y asignación de revisor.                                         |
| RNF02 | Integridad de puntos | P1        | Aprobado                     | Implementado y probado: transacción, bloqueo y clave única; prueba de aprobaciones simultáneas.                       |
| RNF03 | Integridad de datos  | P1        | Aprobado                     | Implementado: claves foráneas, relaciones y restricciones Flyway. El límite comunitario se serializa en servicios.    |
| RNF04 | Usabilidad           | P1        | Aprobado                     | Formularios en español, etiquetas, estados y errores. Prueba formal con usuarios pendiente.                           |
| RNF05 | Trazabilidad         | P1        | Aprobado                     | Implementado: autor, revisor, fechas, versión revisada y observaciones conservados.                                   |
| RNF06 | Compatibilidad       | P1        | Aprobado                     | Plantillas probadas por MockMvc y revisión en navegador de escritorio; matriz formal entre navegadores pendiente.     |
| RNF07 | Rendimiento          | P2        | Aprobado                     | No verificado bajo carga. Se requieren paginación, agregados SQL y definición de volumen para objetivo de 3 segundos. |
| RNF08 | Accesibilidad        | P2        | Aprobado                     | Etiquetas y foco visible, diseño adaptable y estados textuales. Auditoría formal de accesibilidad pendiente.          |
| RNF09 | Mantenibilidad       | P2        | Aprobado                     | Implementado: responsabilidades separadas, Maven Wrapper, Docker, documentación y CI.                                 |
| RNF10 | Respaldo             | P2        | Aprobado                     | Procedimiento documentado para base y fotos. Restauración real pendiente de ensayo con Docker.                        |
| RNF11 | Privacidad           | P1        | Aprobado                     | Implementado: evidencias e integrantes privados; ficha pública sin dirección interna ni datos de cuenta.              |
| RNF12 | Moderación           | P1        | Aprobado                     | Implementado: autor del reporte, resolución, moderador y estado conservados.                                          |

## Reglas de negocio

| ID   | Estado del equipo en Excel | Aplicación                                                           |
| ---- | -------------------------- | -------------------------------------------------------------------- |
| RB01 | Aprobado                   | Máximo 25 activos; bajas liberan cupo.                               |
| RB02 | Aprobado                   | Gestor registra, corrige pendiente y reenvía rechazada.              |
| RB03 | Aprobado                   | Participantes activos de la comunidad y selección inmutable.         |
| RB04 | Aprobado                   | Revisor asignado; rechazo con observaciones.                         |
| RB05 | Aprobado                   | Solo aprobadas generan puntos y crecimiento.                         |
| RB06 | Pendiente de decisión      | Valores pendientes: se aplican cifras configurables de demostración. |
| RB07 | Aprobado                   | Todos los seleccionados reciben igual recompensa individual.         |
| RB08 | Aprobado                   | Libro de movimientos separa comunidad y persona.                     |
| RB09 | Aprobado                   | Solo aprobación modifica el huerto virtual.                          |
| RB10 | Aprobado                   | Ranking por puntos comunitarios.                                     |
| RB11 | Aprobado                   | Nivel individual; Maestro sin cupos, umbrales demo.                  |
| RB12 | Aprobado                   | Normas y moderación del gestor comunitario.                          |
| RB13 | Aprobado                   | Reportes y decisiones humanas; sanciones y apelación pendientes.     |
| RB14 | Aprobado                   | Prohibición de revisar actividad propia, también en servicio.        |
| RB15 | Aprobado                   | Versiones de evidencia y decisiones conservadas en reenvíos.         |
