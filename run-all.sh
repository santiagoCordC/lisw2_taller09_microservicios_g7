#!/usr/bin/env bash
# Arranca RabbitMQ (Docker) y los 6 microservicios con Maven en segundo plano.
# Los logs quedan en ./logs/<servicio>.log. Para detenerlos: ./stop-all.sh
set -e
cd "$(dirname "$0")"
mkdir -p logs

docker compose up -d rabbitmq
echo "Esperando a RabbitMQ..."
until docker exec saberpro-rabbitmq rabbitmq-diagnostics -q ping >/dev/null 2>&1; do sleep 2; done

# El orden importa poco (las colas son durables), pero Notification y Lifecycle declaran sus colas al arrancar.
for s in user-service validation-service lifecycle-service notification-service question-service review-service; do
  echo "Iniciando $s..."
  (cd "$s" && nohup mvn -q spring-boot:run > "../logs/$s.log" 2>&1 & echo $! > "../logs/$s.pid")
done

echo "Servicios iniciándose. Revise ./logs/*.log  (puertos 8081-8086)"
