#!/usr/bin/env bash
# Local development and test database: PostgreSQL in Docker (compose.yaml at the repo root).
set -euo pipefail
cd "$(dirname "$0")/.."

usage() {
  cat >&2 <<'EOF'
Usage: scripts/operon-db.sh <command>

  Command  Does
  up       docker compose up -d --wait postgres (creates and loads the schema on first run)
  down     docker compose stop postgres (keeps the data)
  reset    docker compose down -v then up (drops the volume: empty database, schema reloaded)
  psql     docker compose exec postgres psql -U tailorituser -d tailorit "$@"
  status   docker compose ps postgres
  logs     docker compose logs --tail=100 postgres
EOF
}

up() {
  docker compose up -d --wait postgres
}

command="${1:-}"
case "$command" in
  up)
    up
    ;;
  down)
    docker compose stop postgres
    ;;
  reset)
    docker compose down -v
    up
    ;;
  psql)
    shift
    docker compose exec postgres psql -U tailorituser -d tailorit "$@"
    ;;
  status)
    docker compose ps postgres
    ;;
  logs)
    docker compose logs --tail=100 postgres
    ;;
  *)
    usage
    exit 2
    ;;
esac
