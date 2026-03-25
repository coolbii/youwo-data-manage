#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.dev.yml"

usage() {
  echo "Usage: ./scripts/db-import.sh --table=<table> --csv=<csv-file>"
  echo ""
  echo "Tables:"
  echo "  people       Import people from CSV"
  echo "  pin-rules    Import pin rules from CSV"
  echo ""
  echo "Example:"
  echo "  ./scripts/db-import.sh --table=people --csv=db/people_import_template.csv"
  exit 1
}

TABLE=""
CSV_PATH=""

for arg in "$@"; do
  case "$arg" in
    --table=*) TABLE="${arg#*=}" ;;
    --csv=*)   CSV_PATH="${arg#*=}" ;;
    *)         echo "Unknown argument: ${arg}" >&2; usage ;;
  esac
done

[[ -z "${TABLE}" ]] && { echo "Error: --table is required" >&2; usage; }
[[ -z "${CSV_PATH}" ]] && { echo "Error: --csv is required" >&2; usage; }

# Resolve SQL file from table name
case "${TABLE}" in
  people)    IMPORT_SQL="${ROOT_DIR}/db/import_people_batch.sql" ;;
  pin-rules) IMPORT_SQL="${ROOT_DIR}/db/import_pin_rules_batch.sql" ;;
  *)         echo "Error: unknown table '${TABLE}'. Supported: people, pin-rules" >&2; exit 1 ;;
esac

if [[ ! -f "${CSV_PATH}" ]]; then
  echo "CSV file not found: ${CSV_PATH}" >&2
  exit 1
fi

if [[ ! -f "${IMPORT_SQL}" ]]; then
  echo "Import SQL not found: ${IMPORT_SQL}" >&2
  echo "Table '${TABLE}' import is not yet implemented." >&2
  exit 1
fi

cd "${ROOT_DIR}"

if [[ -f .env ]]; then
  set -a
  # shellcheck source=/dev/null
  . ./.env
  set +a
fi

DB_USER="${SPRING_DATASOURCE_USERNAME:-postgres}"
DB_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5433/youwo_homework}"
DB_NAME="$(printf '%s' "${DB_URL}" | sed -E 's#^jdbc:postgresql://[^/]+/([^?]+).*$#\1#')"
DB_NAME="${DB_NAME:-youwo_homework}"

docker compose -f "${COMPOSE_FILE}" up -d postgres >/dev/null

CONTAINER_ID="$(docker compose -f "${COMPOSE_FILE}" ps -q postgres)"
if [[ -z "${CONTAINER_ID}" ]]; then
  echo "Could not find running postgres container." >&2
  exit 1
fi

REMOTE_CSV="/tmp/${TABLE}_import_$(date +%s).csv"
cleanup() {
  docker exec "${CONTAINER_ID}" rm -f "${REMOTE_CSV}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker cp "${CSV_PATH}" "${CONTAINER_ID}:${REMOTE_CSV}"
docker exec "${CONTAINER_ID}" sh -lc "chown postgres:postgres '${REMOTE_CSV}' && chmod 0644 '${REMOTE_CSV}'"

echo "Importing ${TABLE} from ${CSV_PATH} ..."

docker compose -f "${COMPOSE_FILE}" exec -T postgres \
  psql --no-psqlrc -U "${DB_USER}" -d "${DB_NAME}" \
  -v ON_ERROR_STOP=1 -v "csv_file=${REMOTE_CSV}" < "${IMPORT_SQL}"
