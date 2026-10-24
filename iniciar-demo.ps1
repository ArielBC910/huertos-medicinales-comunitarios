param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if ($JavaHome) {
    $env:JAVA_HOME = $JavaHome
    $java = Join-Path $JavaHome 'bin/java.exe'
} else {
    $java = (Get-Command java -ErrorAction Stop).Source
}
$jar = Join-Path $PSScriptRoot 'target/huertos-medicinales-0.1.0.jar'
if (-not (Test-Path -LiteralPath $jar)) {
    & "$PSScriptRoot/mvnw.cmd" '-DskipTests' package
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar. Revisa JAVA_HOME y la conexión a internet.' }
}
# Windows puede fallar si usa una ruta TEMP muy larga para los sockets del JDK.
$socketDir = Join-Path $PSScriptRoot '.runtime-tmp'
New-Item -ItemType Directory -Path $socketDir -Force | Out-Null
Write-Host 'Abrir http://localhost:8080 — detener con Ctrl+C.'
& $java "-Djdk.net.unixdomain.tmpdir=$socketDir" -jar $jar '--spring.profiles.active=demo' '--server.address=127.0.0.1'
exit $LASTEXITCODE
