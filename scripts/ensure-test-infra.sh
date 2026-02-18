#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="${TEST_DB_SERVICE_NAME:-mysql}"
MAX_ATTEMPTS="${TEST_DB_WAIT_ATTEMPTS:-30}"
SLEEP_SECONDS="${TEST_DB_WAIT_SECONDS:-2}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed or not available in PATH." >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose is not available (expected 'docker compose')." >&2
  exit 1
fi

echo "Ensuring docker compose service '${SERVICE_NAME}' is running..."
docker compose up -d "${SERVICE_NAME}" >/dev/null

container_id="$(docker compose ps -q "${SERVICE_NAME}")"
if [ -z "${container_id}" ]; then
  echo "Unable to locate container for service '${SERVICE_NAME}'." >&2
  exit 1
fi

for attempt in $(seq 1 "${MAX_ATTEMPTS}"); do
  status="$(
    docker inspect \
      --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
      "${container_id}" 2>/dev/null || true
  )"

  if [ "${status}" = "healthy" ] || [ "${status}" = "running" ]; then
    echo "Service '${SERVICE_NAME}' is ready (status: ${status})."
    exit 0
  fi

  if [ "${attempt}" -eq "${MAX_ATTEMPTS}" ]; then
    echo "Service '${SERVICE_NAME}' was not ready after ${MAX_ATTEMPTS} attempts." >&2
    docker compose ps "${SERVICE_NAME}" || true
    exit 1
  fi

  sleep "${SLEEP_SECONDS}"
done
