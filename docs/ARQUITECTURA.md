# Arquitectura

Aplicación monolítica modular por responsabilidades, una base PostgreSQL y un adaptador de almacenamiento de fotografías. Los servicios delimitan las transacciones y vuelven a verificar los permisos y la comunidad del usuario.

```mermaid
flowchart LR
  U[Navegador responsive] --> S[Spring Security y CSRF]
  S --> C[Controladores MVC]
  C --> T[Thymeleaf y recursos locales]
  C --> B[Servicios de comunidades, actividades, foro y progreso]
  B --> J[Repositorios JPA]
  J --> P[(PostgreSQL)]
  F[Flyway] --> P
  B --> I[PhotoStorage]
  I --> D[Volumen persistente de fotografías]
```

```mermaid
erDiagram
  COMMUNITY ||--o{ USER_ACCOUNT : integra
  COMMUNITY ||--o| GARDEN : tiene
  COMMUNITY ||--o{ PLANT : cultiva
  USER_ACCOUNT ||--o{ REVIEWER_ASSIGNMENT : revisa
  COMMUNITY ||--o{ REVIEWER_ASSIGNMENT : asignada
  COMMUNITY ||--o{ GARDEN_ACTIVITY : registra
  GARDEN_ACTIVITY ||--|{ PARTICIPATION : fija
  USER_ACCOUNT ||--o{ PARTICIPATION : participa
  GARDEN_ACTIVITY ||--|{ SUBMISSION : conserva
  SUBMISSION ||--o{ EVIDENCE_REVIEW : recibe
  GARDEN_ACTIVITY ||--o{ POINT_MOVEMENT : recompensa
  COMMUNITY ||--o{ FORUM_POST : conversa
  FORUM_POST ||--o{ CONTENT_REPORT : reporta
  COMMUNITY ||--o{ INVENTORY_ITEM : conserva
  USER_ACCOUNT ||--o{ NOTIFICATION : recibe
```

## Aprobación e idempotencia

`ActivityService.decide` bloquea la actividad mediante `PESSIMISTIC_WRITE`, verifica rol y asignación del revisor, impide autorevisión y exige que la decisión corresponda al último envío. Rechazar requiere observaciones. Solo se revisan actividades PENDIENTES.

En la misma transacción se conserva la decisión, cambia el estado, se crean movimientos comunitarios/personales y se genera un aviso al gestor. Una excepción revierte todos los cambios de base de datos. La representación virtual se calcula de actividades APROBADAS; no hay un contador de avance independiente que pueda desincronizarse.

Cada movimiento tiene una clave de beneficiario `C:{comunidad}` o `U:{integrante}`. La restricción única `(activity_id, beneficiary_key)` impide duplicaciones incluso si otro código intenta repetir una recompensa. Los puntos comunitarios filtran los movimientos sin cuenta; los individuales filtran por cuenta. La recompensa capturada en la actividad permanece fija aunque cambie la configuración posterior.

El reenvío bloquea la misma actividad, añade una nueva versión de evidencia y vuelve a PENDIENTE. Nunca reemplaza la fotografía o revisión anterior ni modifica participantes. No se admite editar actividades aprobadas. Una revisión sobre una versión antigua se rechaza para impedir aprobar una fotografía distinta a la consultada.

## Permisos

| Rol                  | Alcance                                                                                              |
| -------------------- | ---------------------------------------------------------------------------------------------------- |
| Visitante sin cuenta | Mapa, comunidades públicas, plantas, calendario y ranking comunitario                                |
| Gestor               | Datos, huerto, integrantes, plantas, actividades, bodega y reportes de su comunidad                  |
| Participante         | Lectura privada de su comunidad, progreso personal, publicaciones/comentarios y reportes             |
| Revisor              | Evidencias e historial únicamente de comunidades asignadas; aprueba/rechaza, no administra el huerto |

Un solo rol por cuenta; no hay ADMIN genérico. La cuenta inactiva no puede autenticarse ni ejecutar operaciones mediante una sesión anterior. El control de ámbito existe también en servicios y descarga de evidencias, además de restricciones HTTP.

## Extensión recomendada

Mantener los mismos límites transaccionales al separar paquetes funcionales más finos. Añadir DTOs validados y paginación, repositorios con agregados SQL para rankings, registro explícito de adjudicación de misiones, gestión institucional de cuentas y asignaciones, y un adaptador de almacenamiento de objetos con enlaces privados. No se requiere convertir el prototipo en microservicios.
