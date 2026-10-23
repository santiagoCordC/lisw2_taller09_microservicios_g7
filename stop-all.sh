#!/usr/bin/env bash
# Detiene los microservicios iniciados con run-all.sh (RabbitMQ sigue en Docker).
cd "$(dirname "$0")"
for pidfile in logs/*.pid; do
  [ -f "$pidfile" ] || continue
  pid=$(cat "$pidfile")
  pkill -P "$pid" 2>/dev/null || true
  kill "$pid" 2>/dev/null || true
  rm -f "$pidfile"
done
echo "Microservicios detenidos. Para detener RabbitMQ: docker compose down"
