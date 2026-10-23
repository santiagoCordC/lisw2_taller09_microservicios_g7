# Arranca RabbitMQ (Docker) y abre una ventana de PowerShell por microservicio (Windows).
# Requisitos: Docker Desktop, JDK 17+ y Maven en el PATH.
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

docker compose up -d rabbitmq
Write-Host "Esperando a RabbitMQ..."
do {
    Start-Sleep -Seconds 2
    docker exec saberpro-rabbitmq rabbitmq-diagnostics -q ping *> $null
} until ($LASTEXITCODE -eq 0)

$services = @("user-service", "validation-service", "lifecycle-service", "notification-service", "question-service", "review-service")
foreach ($s in $services) {
    Write-Host "Iniciando $s..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\$s'; `$host.UI.RawUI.WindowTitle='$s'; mvn spring-boot:run"
}
Write-Host "Servicios iniciándose en ventanas separadas (puertos 8081-8086)."
