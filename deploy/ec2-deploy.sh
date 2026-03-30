#!/usr/bin/env bash
set -euo pipefail

DEPLOY_PATH="${EC2_DEPLOY_PATH:-/opt/youwo-homework/deploy}"
PROJECT_NAME="${EC2_DOCKER_PROJECT_NAME:-youwo-homework}"
WEB_PORT="${EC2_WEB_LOCAL_PORT:-18180}"
API_PORT="${EC2_API_LOCAL_PORT:-18181}"
DB_URL="${SPRING_DATASOURCE_URL}"

echo "[deploy] Start deployment on $(hostname)"
for key in API_IMAGE CLIENT_IMAGE GHCR_USERNAME GHCR_PULL_TOKEN \
           SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; do
  if [ -z "${!key:-}" ]; then
    echo "Required env is empty: ${key}" >&2
    exit 1
  fi
done

# Normalize DB URL scheme to jdbc:postgresql://
echo "[deploy] Normalize datasource URL"
DB_URL="$(printf '%s' "${DB_URL}" | tr -d '\r')"
DB_URL="${DB_URL#\"}"
DB_URL="${DB_URL%\"}"

case "${DB_URL}" in
  jdbc:postgresql://*)
    ;;
  postgresql://*)
    DB_URL="jdbc:${DB_URL}"
    ;;
  postgres://*)
    DB_URL="jdbc:postgresql://${DB_URL#postgres://}"
    ;;
  *://*)
    echo "Invalid SPRING_DATASOURCE_URL scheme." >&2
    exit 1
    ;;
  *)
    DB_URL="jdbc:postgresql://${DB_URL}"
    ;;
esac
echo "[deploy] Datasource URL normalized"

if [ "${WEB_PORT}" = "80" ] || [ "${WEB_PORT}" = "443" ] || \
   [ "${API_PORT}" = "80" ] || [ "${API_PORT}" = "443" ]; then
  echo "Refuse to deploy: local ports cannot use 80/443 on shared EC2." >&2
  exit 1
fi

echo "[deploy] Prepare directory: ${DEPLOY_PATH}"
mkdir -p "${DEPLOY_PATH}"
cd "${DEPLOY_PATH}"

if [ ! -d "${DEPLOY_PATH}/db/migration" ]; then
  echo "Migration directory not found: ${DEPLOY_PATH}/db/migration" >&2
  exit 1
fi

cat > .env <<ENVFILE
API_IMAGE=${API_IMAGE}
CLIENT_IMAGE=${CLIENT_IMAGE}
WEB_LOCAL_PORT=${WEB_PORT}
API_LOCAL_PORT=${API_PORT}
SPRING_DATASOURCE_URL=${DB_URL}
SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENVFILE

echo "[deploy] Login ghcr.io as ${GHCR_USERNAME}"
echo "${GHCR_PULL_TOKEN}" | docker login ghcr.io -u "${GHCR_USERNAME}" --password-stdin

echo "[deploy] Run Flyway migration"
docker run --rm \
  -v "${DEPLOY_PATH}/db/migration:/flyway/sql:ro" \
  flyway/flyway:10 \
  -url="${DB_URL}" \
  -user="${SPRING_DATASOURCE_USERNAME}" \
  -password="${SPRING_DATASOURCE_PASSWORD}" \
  -connectRetries=60 \
  migrate

echo "[deploy] Pull compose images"
docker compose \
  --project-name "${PROJECT_NAME}" \
  -f docker-compose.ec2.yml \
  --env-file .env \
  pull

echo "[deploy] Start compose services"
docker compose \
  --project-name "${PROJECT_NAME}" \
  -f docker-compose.ec2.yml \
  --env-file .env \
  up -d --remove-orphans

echo "[deploy] Run health checks"
curl -fsS -I "http://127.0.0.1:${WEB_PORT}" >/dev/null
curl -fsS "http://127.0.0.1:${API_PORT}/api/health" >/dev/null

echo "[deploy] Deployment complete"
docker ps --format 'table {{.Names}}\t{{.Ports}}'
