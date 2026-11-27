# Plan: A PostgreSQL database in Docker for development and the test suite

**Status:** DONE
**Loop:** [Modernise Operon](../261126-0754-modernise-operon-loop.md)

## Context

Operon stores cases, tasks, tokens and audit rows in a relational database. Its database tests (`WorkflowEngineTest`, 5 tests)
expect a MySQL server on `localhost:3306` with a database `tailorit` and user `tailorituser`
(`operon/src/test/resources/META-INF/jdbc.properties:12-15`), which nobody has had installed for years, so those tests cannot run.

The user decided that Operon moves from MySQL to **PostgreSQL, run in Docker for development and testing**. This plan provides
only the database: one `docker compose` service with a pinned Postgres major version, a named volume, a health check, and the
repo's existing Postgres schema script loaded on first start, plus a small script and README section to start, stop and reset it.
Switching the code, configuration and seed data to Postgres is
[the conversion plan](../261125-2112-convert-operon-from-mysql-to-postgres.md), which uses this database to run the tests.

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

**Health check** `pg_isready -h 127.0.0.1 -U tailorituser -d tailorit`; `docker compose up --wait` returns only once it passes,
so a script or test run can start straight after. The `-h 127.0.0.1` matters: on first start the image runs the init scripts on a
temporary server started with `-c listen_addresses=''` (Unix socket only; `/usr/local/bin/docker-entrypoint.sh` in `postgres:19`),
then stops it and starts the real server, which listens on TCP. Without `-h`, `pg_isready` checks the socket, so it reports
healthy while the init scripts are still running and before the host can connect. Over TCP it passes only once the real server is
up, that is after every init script has run (the schema now, and Plan 3's `20-...` seed later). The retry budget (30 x 2 s, about
a minute) is far more than both scripts need.

**Credentials.** The user, password and database are throwaway local-development values. They are already committed in
`jdbc.properties`; nothing else secret is added. The port is bound to `127.0.0.1` only, so the database is not reachable from
other machines.

*Before:* no compose file, no script. *After* (`compose.yaml`, new; built in `baa1030` with the health check line
`test: ["CMD-SHELL", "pg_isready -U tailorituser -d tailorit"]`, which the re-review of 2026-11-27 changes to the line below,
see *Change after the re-review*):

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
      test: ["CMD-SHELL", "pg_isready -h 127.0.0.1 -U tailorituser -d tailorit"]
      interval: 2s
      timeout: 3s
      retries: 30
volumes:
  operon-pgdata:
```

**Change after the re-review (2026-11-27), on top of `baa1030`.** One line in `compose.yaml`:

*Before* (`compose.yaml:18`, as built in `baa1030`):
```yaml
      test: ["CMD-SHELL", "pg_isready -U tailorituser -d tailorit"]
```
*After:*
```yaml
      test: ["CMD-SHELL", "pg_isready -h 127.0.0.1 -U tailorituser -d tailorit"]
```
`scripts/operon-db.sh` and the README section need no change.

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
  [Plan 3](../261125-2112-convert-operon-from-mysql-to-postgres.md)); a CI service; production databases.

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
4. Health check fix (after the 2026-11-27 re-review; tasks 1-3 are done in `baa1030`)
   - edit: `compose.yaml:18` — add `-h 127.0.0.1` to the `pg_isready` command (see Approach, *Change after the re-review*),
     then run all of Verification again, the new slow-init item included

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
Expected: after `down`/`up` the count is `1` (data kept); after `reset` `Did not find any tables named "reset_probe".` and the
13 schema tables are back (fresh volume). The probe table lives only in this local throwaway database.

**`up` waits for first-start initialisation** (new after the 2026-11-27 re-review; this is the check that would have caught the
gap). A throwaway init script that sleeps 8 seconds is mounted next to the schema through a temporary compose override, standing in
for a slower init such as Plan 3's `20-...` seed. Nothing in the repo is changed:

```bash
d=$(mktemp -d)
echo 'select pg_sleep(8);' > "$d/20-slow.sql"
printf 'services:\n  postgres:\n    volumes:\n      - %s/20-slow.sql:/docker-entrypoint-initdb.d/20-slow.sql:ro\n' "$d" > "$d/slow.yaml"
COMPOSE_FILE="compose.yaml:$d/slow.yaml" scripts/operon-db.sh reset \
  && PGPASSWORD=tailorituser psql -h 127.0.0.1 -p 15432 -U tailorituser -d tailorit -Atc \
     "select count(*) from pg_tables where schemaname = 'public';"
echo "rc=$?"
rm -r "$d"
scripts/operon-db.sh reset && docker inspect operon-postgres --format '{{range .Mounts}}{{.Destination}} {{end}}'
```
Expected: `reset` takes more than 8 seconds, then the host query prints `13` and `rc=0`. With the health check as built in
`baa1030` (no `-h`), `reset` returns after about 3 seconds and the query fails with `server closed the connection unexpectedly`
(`rc=2`). The last line puts the database back with only the repo's mounts: `/var/lib/postgresql
/docker-entrypoint-initdb.d/10-operon-schema.sql`.

```bash
OPERON_DB_PORT=25432 scripts/operon-db.sh reset && PGPASSWORD=tailorituser psql -h 127.0.0.1 -p 25432 -U tailorituser -d tailorit -c 'select 1'
scripts/operon-db.sh reset
```
Expected: the port override works; the last line puts it back on 15432.

`scripts/operon-db.sh bogus` prints the usage table and exits with code 2.

Checked on 2026-11-26 (throwaway `postgres:19` container, same mount and volume path): today's schema script loads without error,
`\dt` lists the 13 tables above, and the image keeps its data in `/var/lib/postgresql/19/docker`, so the volume path above is
right. If a later image does not load it, record the error in `## Status` and mark
[Plan 3](../261125-2112-convert-operon-from-mysql-to-postgres.md) `/ NEEDS RE-REVIEW` (its schema rewrite then has a known input).

## Dependencies

### Blocks this plan (must be done first)
None.

### Blocked by this plan (cannot start until this is done)
- [Convert Operon from MySQL to PostgreSQL](../261125-2112-convert-operon-from-mysql-to-postgres.md) — APPROVED; **unblocked**:
  this plan is DONE (its verification runs the test suite against this database)
- [Upgrade Java, libraries and Maven to latest](../261125-2113-upgrade-java-libraries-and-maven-to-latest.md) — APPROVED / RE-REVIEW DONE;
  **blocked** only through Plan 3 (its database characterization tests run against this database)

## Status

- 2026-11-25 (operon): PLANNED — written by the owning team after reading the schema scripts, `jdbc.properties` and
  `operon-dataAccessContext-local.xml`; Postgres 19 GA and the `postgres:19` tag checked on 2026-11-25; no container started.
  Awaiting the user's approval.
- 2026-11-26 (operon): PLANNED — joined the loop [Modernise Operon](../261126-0754-modernise-operon-loop.md), created at the user's request.
- 2026-11-26 (operon): PLANNED — owning-team re-review under the user's rule "modernisation keeps every feature behaving as
  today": this plan adds only local tooling and changes no feature code; nothing removed. Dependency status tags of 2112 and
  2113 updated to `PLANNED / NEEDS RE-REVIEW`. No change to how this plan is implemented.
- 2026-11-26 (operon): PLANNED — independent owning-team re-review. Checked in a throwaway `postgres:19` container: today's
  schema script loads cleanly (13 tables) and the data folder is `/var/lib/postgresql/19/docker`; `docker compose exec` works
  without a terminal (as an agent runs it); host `psql` 16 is installed for the host-side check. Verification's note updated;
  2112's tag now `PLANNED`. No change to how this plan is implemented.
- 2026-11-26 (operon): APPROVED — approved by the user ("approve the plans"). Still in `backlog/`, so nothing may be built until the loop is picked into `todos/`.
- 2026-11-26 (operon): APPROVED — picked from `backlog/` into `todos/` with the Modernise Operon loop, on the user's go-ahead. Not started: the user is clearing the session first; the new session dispatches.
- 2026-11-27 (operon): IN PROGRESS — dispatched to a builder subagent (sonnet, high) on branch `modernise-operon-2-postgres-docker`.
- 2026-11-27 (operon): IN PROGRESS — implemented as written and committed locally as `baa1030` (`compose.yaml`,
  `scripts/operon-db.sh`, README section); not pushed. All Verification items pass with `postgres:19` (19.0): 13 tables owned by
  `tailorituser`, no `ERROR` in the init log, data kept across `down`/`up` and dropped by `reset`, port override works, `bogus`
  exits 2. One wording difference: `psql` prints `Did not find any tables named "reset_probe".` (not "relation"). The database
  is left running and healthy on 15432, freshly reset.
- 2026-11-27 (operon): IN PROGRESS — finding from an extra check, **not fixed** (the plan sets the health check command). On
  first start the image runs the init scripts on a temporary server that listens on its Unix socket only, and
  `pg_isready` without `-h` checks that socket. With an extra `select pg_sleep(8)` init script (mounted from a scratch folder
  through a compose override), `up` returned `Healthy` while initialisation was still running, and a host connection made
  straight after failed (`server closed the connection unexpectedly`). With the repo's schema script alone, the host
  connections made right after a `reset` succeeded. Possible fix, untested: add `-h 127.0.0.1` to the `pg_isready` command.
  For the driver to decide.
- 2026-11-27 (operon): IN PROGRESS / NEEDS RE-REVIEW — built (`baa1030`, local); the planned health check can report healthy during first-start initialisation, before the database accepts connections from the host (shown with a slowed init script). This breaks the plan's intended outcome, so the check is re-reviewed before any further code change.
- 2026-11-27 (operon): IN PROGRESS / RE-REVIEW DONE — owning-team re-review of the health check. Cause confirmed: the
  `postgres:19` entrypoint starts its temporary init server with `-c listen_addresses=''` (socket only), and `pg_isready` without
  `-h` checks the socket, so it passes during initialisation. Reproduced with the repo script and an 8-second init script mounted
  through a temporary override: as built, `reset` returned after 3 s and a host connection failed. Fix chosen:
  `pg_isready -h 127.0.0.1 ...` (Approach, *Change after the re-review*). Proved with the same override on the fixed check:
  `reset` waited (22 s) and the host query right after found the 13 tables; a normal `reset` (3 s, 13 tables from the host),
  `down`/`up` (probe row kept, healthy) and `reset` (probe gone) behave as before. New Verification item added for the slow
  init; the expected `psql` message corrected to `Did not find any tables named "reset_probe".` (seen in the build and again
  here). Plan 3 needs no change: its `20-...` seed runs before the real server starts, so the fixed check also waits for it.
  Builder: Implementation task 4, then all of Verification. The database was left freshly reset with only the repo's mounts.
- 2026-11-27 (operon): IN PROGRESS / RE-REVIEW DONE — Implementation task 4 applied (`-h 127.0.0.1` in the health check) and
  committed locally as `9979bec` on top of `baa1030`; not pushed. All of Verification was run again, starting from a removed
  `operon` volume so that `up` was a first start, and every item passes: `up` returned healthy; 13 tables owned by
  `tailorituser`; `10-operon-schema.sql` ran with no `ERROR`, `FATAL` or `PANIC` in the full log; the host sees
  `PostgreSQL 19.0`; the probe row survives `down`/`up` and `reset` removes it; with the 8-second init script (scratch folder,
  temporary override) `reset` took 11.5 s and the host query right after printed `13`, `rc=0`; the restored database mounts
  only `/var/lib/postgresql` and `10-operon-schema.sql`; the port override works on 25432 and `reset` returns to 15432;
  `bogus` exits 2. The database is left running and healthy on 15432, freshly reset with only the repo's mounts.
- 2026-11-27 (operon): DONE — independent audit of `baa1030` and `9979bec`: no code violations; every Verification item
  re-run by the auditor and passed. Its two bookkeeping findings (loop row and these dependency tags out of date) are fixed.
  Optional nits left, because none is a violation and each is beyond what the plan asks: after a failed first start a later
  `up` reports healthy on a partly loaded database (official image behaviour; `reset` clears it); `OPERON_DB_PORT` must be
  set on each call; an exported `COMPOSE_PROJECT_NAME` or `COMPOSE_FILE` redirects the script; piping SQL into the `psql`
  command may need no terminal on output; small README wording; no `.gitattributes` line-ending rule. Archived to `todos/done/`.
- 2026-11-27 (operon): DONE — PR raised: https://github.com/hoodox/operon/pull/2.
