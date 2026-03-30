#!/usr/bin/env bash
set -euo pipefail

IMPORT_ROOT="${EC2_IMPORT_DIR:-/opt/youwo-homework/import}"
CSV_FILE="${IMPORT_ROOT}/${CSV_PATH}"
SQL_FILE="${IMPORT_ROOT}/db/import_people_batch.sql"

if [ ! -f "${CSV_FILE}" ]; then
  echo "CSV file not found on EC2: ${CSV_FILE}" >&2
  exit 1
fi
if [ ! -f "${SQL_FILE}" ]; then
  echo "SQL file not found on EC2: ${SQL_FILE}" >&2
  exit 1
fi

JDBC_BODY="${SPRING_DATASOURCE_URL#jdbc:postgresql://}"
HOST_PORT="${JDBC_BODY%%/*}"
DB_AND_QS="${JDBC_BODY#*/}"
DB_NAME="${DB_AND_QS%%\?*}"
DB_HOST="${HOST_PORT%%:*}"
DB_PORT="${HOST_PORT##*:}"

if [ "${DB_HOST}" = "${DB_PORT}" ]; then
  DB_PORT="5432"
fi

if [ -z "${DB_HOST}" ] || [ -z "${DB_NAME}" ]; then
  echo "Cannot parse SPRING_DATASOURCE_URL" >&2
  exit 1
fi

SSLMODE=""
if [[ "${DB_AND_QS}" == *"?"* ]]; then
  QUERY_STRING="${DB_AND_QS#*\?}"
  SSLMODE="$(printf '%s' "${QUERY_STRING}" | tr '&' '\n' | sed -n 's/^sslmode=//p' | head -n1)"
fi

PSQL_CONN="host=${DB_HOST} port=${DB_PORT} dbname=${DB_NAME} user=${SPRING_DATASOURCE_USERNAME}"
if [ -n "${SSLMODE}" ]; then
  PSQL_CONN="${PSQL_CONN} sslmode=${SSLMODE}"
fi

echo "[import] Running bulk import: ${CSV_FILE}"
docker run --rm \
  -v "${IMPORT_ROOT}:/work:ro" \
  -e PGPASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
  postgres:16-alpine \
  psql "${PSQL_CONN}" \
  -v ON_ERROR_STOP=1 \
  -v "csv_file=/work/${CSV_PATH}" \
  -f /work/db/import_people_batch.sql

echo "[import] Done. Verifying row count..."
docker run --rm \
  -e PGPASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
  postgres:16-alpine \
  psql "${PSQL_CONN}" \
  -c "select count(*) as people_count from people;"
