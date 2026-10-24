# Operación del prototipo

## Registrar una comunidad con su gestor

El operador del despliegue ejecuta este comando de mantenimiento una vez por nueva comunidad. No se incorpora un rol administrador adicional. Debe obtener autorización institucional para designar gestores y revisores. Las credenciales se transmiten por variables de entorno, nunca como argumentos del JAR ni dentro del repositorio.

Con PostgreSQL ya iniciado mediante Docker Compose, en PowerShell:

```powershell
$env:PROVISION_USER='gestor.nueva'
$env:PROVISION_PASSWORD=Read-Host 'Contraseña inicial (mínimo 12 caracteres)' -MaskInput
$env:PROVISION_NAME='Nombre del gestor'
$env:PROVISION_ROLE='GESTOR'
$env:PROVISION_COMMUNITY='Nombre de la comunidad'
$env:PROVISION_PROVINCE='Pichincha'
$env:PROVISION_PARISH='Parroquia'
docker compose run --rm -e PROVISION_USER -e PROVISION_PASSWORD -e PROVISION_NAME -e PROVISION_ROLE -e PROVISION_COMMUNITY -e PROVISION_PROVINCE -e PROVISION_PARISH app --spring.profiles.active=provision --spring.main.web-application-type=none
Remove-Item Env:PROVISION_PASSWORD
```

El gestor puede iniciar sesión, completar los datos del huerto e incorporar participantes hasta 25 integrantes activos contando al propio gestor. Las bajas conservan el historial y liberan cupo; reactivar también verifica el límite.

## Registrar un revisor

Ejecutar con `PROVISION_ROLE=REVISOR`, `PROVISION_USER`, `PROVISION_PASSWORD`, `PROVISION_NAME` y `PROVISION_COMMUNITY_IDS` (IDs de comunidades separados por comas). Mantener las mismas opciones `--spring.profiles.active=provision --spring.main.web-application-type=none` y pasar las variables con `docker compose run --rm -e ...`. El comando no permite sobrescribir usuarios existentes. Los IDs se consultan en los enlaces públicos `/comunidades/{id}` o con una consulta SQL del operador.

La designación efectiva por el MSP no se simula: este procedimiento registra técnicamente una autorización que el operador debe haber recibido. La V1 no incluye gestión web de asignaciones ni autoservicio de contraseña. Para cambiar una contraseña, el operador debe aplicar un hash BCrypt nuevo mediante un procedimiento de mantenimiento auditado; nunca guardar texto plano en la base.

## Respaldo y restauración

Respaldar base de datos y fotografías como una unidad. En una ventana de mantenimiento detener `app` para evitar inconsistencias entre ambos. Mantener copias fuera del host, con acceso restringido y retención definida por el equipo.

```bash
docker compose stop app
docker compose exec -T db pg_dump -U huertos -d huertos -Fc -f /tmp/huertos.dump
docker compose cp db:/tmp/huertos.dump ./huertos.dump
docker compose cp app:/app/uploads ./respaldo-fotos
docker compose start app
```

Restaurar primero en un entorno de ensayo aislado con la misma versión del código. Nunca ejecutar `--clean` sobre una base que se quiera conservar:

```bash
docker compose stop app
docker compose cp ./huertos.dump db:/tmp/huertos.dump
docker compose exec -T db pg_restore -U huertos -d huertos --clean --if-exists /tmp/huertos.dump
docker compose cp ./respaldo-fotos/. app:/app/uploads/
docker compose start app
```

El operador debe comprobar la propiedad de los archivos para el usuario del contenedor, una imagen de evidencia, los estados de revisión y los totales de puntos tras restaurar. El procedimiento está documentado; la restauración Docker no se ha podido ensayar en el equipo de desarrollo, que no tiene Docker.

## Almacenamiento y mantenimiento

Las fotografías se verifican por firma y decodificación, se limitan a 5 MB / 20 megapíxeles y se recodifican a PNG eliminando metadatos. El adaptador guarda archivos con UUID y nunca usa nombres proporcionados por el usuario. Las versiones anteriores siguen disponibles a usuarios autorizados. Por ahora una falla de base posterior a guardar una foto puede dejar un archivo huérfano: antes de eliminarlo debe compararse con las referencias de `submission`, preservando todas las versiones referenciadas. No hay tarea automática de limpieza.

Al publicar detrás de un proxy HTTPS, configurar `server.servlet.session.cookie.secure=true`, el manejo de cabeceras del proxy de confianza y almacenamiento persistente. No habilitar consola H2. No exponer datos demo con credenciales conocidas. Los volúmenes Docker persisten al recrear contenedores; `docker compose down -v` destruye esos volúmenes y no forma parte del procedimiento normal.

## Migraciones y datos editoriales

No editar `V1__initial_schema.sql` después de usarla en un entorno compartido. Agregar `V2__...sql` para cada evolución. Calendario, fuentes, catálogos y designación de revisores requieren revisión humana. El calendario de demostración no contiene fases lunares verificadas; incorporar nuevas entradas mediante una migración SQL con fecha, fase, sugerencia y fuente verificadas.
