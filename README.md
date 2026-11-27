# operon
## Operon Design
See [Operon Design](operon/documents/Operon_TechnicalDesign.pdf)

## Building
You need JDK 21 or later and Maven 3.9 or later. From the repo root:

    mvn -B clean install

The database tests are left out of the normal build. `-Pdb-tests` adds them; it needs the database from
[Plan 2](todos/done/261125-2111-docker-postgres-database-for-dev-and-test.md) and passes once
[Plan 3](todos/261125-2112-convert-operon-from-mysql-to-postgres.md) is done.

## Local database (PostgreSQL in Docker)
You need Docker with Compose v2. From the repo root:

    scripts/operon-db.sh up       # start; loads the schema on first start
    scripts/operon-db.sh down     # stop; keeps the data
    scripts/operon-db.sh reset    # drop the data and start again with a fresh schema
    scripts/operon-db.sh psql     # open psql on the database

Connect with `jdbc:postgresql://localhost:15432/tailorit`, user and password `tailorituser` (throwaway values, for local
use only). To use another port, set `OPERON_DB_PORT`, for example `OPERON_DB_PORT=25432 scripts/operon-db.sh up`.

The schema loads only on first start, so run `scripts/operon-db.sh reset` after editing a script under
`operon/src/resources/META-INF/db/postgres/`. `reset` deletes all data in this database.

Once [Plan 3](todos/261125-2112-convert-operon-from-mysql-to-postgres.md) is done, the tests use this database with
`mvn -B -Pdb-tests verify`.
