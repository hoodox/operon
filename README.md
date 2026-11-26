# operon
## Operon Design
See [Operon Design](operon/documents/Operon_TechnicalDesign.pdf)

## Building
You need JDK 21 or later and Maven 3.9 or later. From the repo root:

    mvn -B clean install

The database tests are left out of the normal build. `-Pdb-tests` adds them; it needs the database from
[Plan 2](todos/261125-2111-docker-postgres-database-for-dev-and-test.md) and passes once
[Plan 3](todos/261125-2112-convert-operon-from-mysql-to-postgres.md) is done.
