# Plan: A PostgreSQL database in Docker for development and the test suite

**Status:** APPROVED
**Loop:** [Modernise Operon](261126-0754-modernise-operon-loop.md)

## Context

Operon stores cases, tasks, tokens and audit rows in a relational database. Its database tests (`WorkflowEngineTest`, 5 tests)
expect a MySQL server on `localhost:3306` with a database `tailorit` and user `tailorituser`
(`operon/src/test/resources/META-INF/jdbc.properties:12-15`), which nobody has had installed for years, so those tests cannot run.

The user decided that Operon moves from MySQL to **PostgreSQL, run in Docker for development and testing**. This plan provides
only the database: one `docker compose` service with a pinned Postgres major version, a named volume, a health check, and the
repo's existing Postgres schema script loaded on first start, plus a small script and README section to start, stop and reset it.
Switching the code, configuration and seed data to Postgres is
[the conversion plan](261125-2112-convert-operon-from-mysql-to-postgres.md), which uses this database to run the tests.

Docker 29.4.1 with Compose v2.40.3 is installed on the development machine. Port 5432 is already taken there by another
Postgres (`ss -ltn` shows a listener on `0.0.0.0:5432`), so this database listens on **15432** by default.

Intended outcome: one command gives a developer or a test run a fresh Operon database on `localhost:15432`, with the schema
already in place, and one command throws it away and starts again.

## Approach

**One compose service at the repo root.** `compose.yaml` defines `postgres` from the official `postgres:19` image. PostgreSQL 19
became generally available on 2026-11-19 (postgresql.org announcement, checked 2026-11-25) and the Docker Hub tag `19` exists
(checked 2026-11-25), so the latest major is pinned by its major number: minor updates come with `docker compose pull`, a major
change needs an edit here. The environment keeps the database, user and password the test configuration already uses
(`tailorit` / `tailorituser` / `tailorituser`), so the conversion plan only changes the driver and URL.

**Schema from the repo, not a copy.** The script `operon/src/resources/META-INF/db/postgres/operon-postgres-schema.sql` is mounted
read-only into `/docker-entrypoint-initdb.d/10-operon-schema.sql`. The image runs those scripts once, when the data volume is
empty, with `psql` set to stop on the first error. So "reset" means delete the volume and start again; the conversion plan adds its
seed script as `20-...` the same way.

**Named volume** `operon-pgdata` mounted at `/var/lib/postgresql`. From the 18 image on, the official image keeps its data in a
version-numbered folder under `/var/lib/postgresql` and expects the volume there, not at the older `/var/lib/postgresql/data`
(check the image README for 19 when building; if it differs, follow it).

**Health check** `pg_isready -U tailorituser -d tailorit`; `docker compose up --wait` returns only once it passes, so a script or
test run can start straight after.

**Credentials.** The user, password and database are throwaway local-development values. They are already committed in
`jdbc.properties`; nothing else secret is added. The port is bound to `127.0.0.1` only, so the database is not reachable from
other machines.

*Before:* no compose file, no script. *After* (`compose.yaml`, new):

```yaml
name: operon
services:
  postgres:
    image: postgres:19
    container_name: operon-postgres
    environment:
      POSTGRES_DB: tailorit
      POSTGRES_USER: tailorituser
      POSTGRES_PASSWORD: tailorituser   # throwaway local dev/test credential, same as jdbc.properties
      TZ: UTC
      PGTZ: UTC
    ports:
      - "127.0.0.1:${OPERON_DB_PORT:-15432}:5432"
    volumes:
      - operon-pgdata:/var/lib/postgresql
      - ./operon/src/resources/META-INF/db/postgres/operon-postgres-schema.sql:/docker-entrypoint-initdb.d/10-operon-schema.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tailorituser -d tailorit"]
      interval: 2s
      timeout: 3s
      retries: 30
volumes:
  operon-pgdata:
```

`scripts/operon-db.sh` (new) wraps it, always from the repo root:

| Command | Does |
|---|---|
| `up` | `docker compose up -d --wait postgres` (creates and loads the schema on first run) |
| `down` | `docker compose stop postgres` (keeps the data) |
| `reset` | `docker compose down -v` then `up` (drops the volume: empty database, schema reloaded) |
| `psql` | `docker compose exec postgres psql -U tailorituser -d tailorit "$@"` |
| `status` | `docker compose ps postgres` |
| `logs` | `docker compose logs --tail=100 postgres` |

Unknown or missing command: print the table above and exit 2. `set -euo pipefail`; `cd "$(dirname "$0")/.."`.

## Scope

- **In:** `compose.yaml`, `scripts/operon-db.sh`, a README section.
- **Out:** any change to the schema script, the seed data, `jdbc.properties`, the Spring files or the pom (all
  [Plan 3](261125-2112-convert-operon-from-mysql-to-postgres.md)); a CI service; production databases.

## Implementation

**Folders touched** (this repo has no `.claude/rules/`, so no scoped rule applies):
- repo root (`compose.yaml`, `README.md`) → no scoped rule
- `scripts/` (new) → no scoped rule

**Flow:** none — local developer tooling (a container definition and a wrapper script); no application code or runtime path changes.

1. Compose file
   - new: `compose.yaml` (see Approach)
2. Wrapper script
   - new: `scripts/operon-db.sh` (see Approach), committed executable (`git update-index --chmod=+x`)
3. Docs
   - edit: `README.md` — new section "Local database (PostgreSQL in Docker)": needs Docker with Compose v2; `scripts/operon-db.sh up`
     / `down` / `reset` / `psql`; connection `jdbc:postgresql://localhost:15432/tailorit`, user and password `tailorituser`
     (throwaway, local only); change the port with `OPERON_DB_PORT=...`; the schema loads only on first start, so run `reset` after
     editing a script under `operon/src/resources/META-INF/db/postgres/`; tests use it via `mvn -B -Pdb-tests verify` once
     Plan 3 is done

## Verification

Run from the repo root.

```bash
scripts/operon-db.sh up && scripts/operon-db.sh status
```
Expected: the `operon-postgres` container is `running (healthy)`; `up` returned only after the health check passed.

```bash
scripts/operon-db.sh psql -c '\dt'
```
Expected: 13 tables owned by `tailorituser`: `config_repository`, `operon_case`, `operon_case_sequence`, `operon_event_audit`,
`operon_event_audit_sequence`, `operon_task`, `operon_task_sequence`, `operon_time_trigger_scheduler`, `operon_token`,
`operon_token_enabled_task`, `operon_token_place_ref`, `operon_token_sequence`, `operon_ttl_scheduler` (the sequence tables are
the schema script as it is today; Plan 3 replaces them). `scripts/operon-db.sh logs` shows `10-operon-schema.sql` ran without
`ERROR`.

```bash
PGPASSWORD=tailorituser psql -h 127.0.0.1 -p 15432 -U tailorituser -d tailorit -c 'select version();'
```
Expected: `PostgreSQL 19.x` (reached from the host, as the tests will).

```bash
scripts/operon-db.sh psql -c "create table reset_probe(x int); insert into reset_probe values (1);"
scripts/operon-db.sh down && scripts/operon-db.sh up && scripts/operon-db.sh psql -c 'select count(*) from reset_probe;'
scripts/operon-db.sh reset && scripts/operon-db.sh psql -c '\dt reset_probe'
```
Expected: after `down`/`up` the count is `1` (data kept); after `reset` `Did not find any relation named "reset_probe"` and the
13 schema tables are back (fresh volume). The probe table lives only in this local throwaway database.

```bash
OPERON_DB_PORT=25432 scripts/operon-db.sh reset && PGPASSWORD=tailorituser psql -h 127.0.0.1 -p 25432 -U tailorituser -d tailorit -c 'select 1'
scripts/operon-db.sh reset
```
Expected: the port override works; the last line puts it back on 15432.

`scripts/operon-db.sh bogus` prints the usage table and exits with code 2.

Checked on 2026-11-26 (throwaway `postgres:19` container, same mount and volume path): today's schema script loads without error,
`\dt` lists the 13 tables above, and the image keeps its data in `/var/lib/postgresql/19/docker`, so the volume path above is
right. If a later image does not load it, record the error in `## Status` and mark
[Plan 3](261125-2112-convert-operon-from-mysql-to-postgres.md) `/ NEEDS RE-REVIEW` (its schema rewrite then has a known input).

## Dependencies

### Blocks this plan (must be done first)
None.

### Blocked by this plan (cannot start until this is done)
- [Convert Operon from MySQL to PostgreSQL](261125-2112-convert-operon-from-mysql-to-postgres.md) — PLANNED; **blocked** (its
  verification runs the test suite against this database)
- [Upgrade Java, libraries and Maven to latest](261125-2113-upgrade-java-libraries-and-maven-to-latest.md) — PLANNED / NEEDS RE-REVIEW; indirectly,
  through Plan 3 (its database characterization tests run against this database)

## Status

- 2026-11-25 (operon): PLANNED — written by the owning team after reading the schema scripts, `jdbc.properties` and
  `operon-dataAccessContext-local.xml`; Postgres 19 GA and the `postgres:19` tag checked on 2026-11-25; no container started.
  Awaiting the user's approval.
- 2026-11-26 (operon): PLANNED — joined the loop [Modernise Operon](261126-0754-modernise-operon-loop.md), created at the user's request.
- 2026-11-26 (operon): PLANNED — owning-team re-review under the user's rule "modernisation keeps every feature behaving as
  today": this plan adds only local tooling and changes no feature code; nothing removed. Dependency status tags of 2112 and
  2113 updated to `PLANNED / NEEDS RE-REVIEW`. No change to how this plan is implemented.
- 2026-11-26 (operon): PLANNED — independent owning-team re-review. Checked in a throwaway `postgres:19` container: today's
  schema script loads cleanly (13 tables) and the data folder is `/var/lib/postgresql/19/docker`; `docker compose exec` works
  without a terminal (as an agent runs it); host `psql` 16 is installed for the host-side check. Verification's note updated;
  2112's tag now `PLANNED`. No change to how this plan is implemented.
- 2026-11-26 (operon): APPROVED — approved by the user ("approve the plans"). Still in `backlog/`, so nothing may be built until the loop is picked into `todos/`.
