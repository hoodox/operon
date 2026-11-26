# Plan: Rebuild the missing hoodox-commons library as a module in this repo and make a clean checkout build green

**Status:** APPROVED
**Loop:** [Modernise Operon](261126-0754-modernise-operon-loop.md)

## Context

Operon cannot be built from a fresh clone today. Its `operon` and `maven-operon-woped-merge-plugin` modules depend on
`com.hoodox.commons:hoodox-commons:1.0.2`, a private library that lived only on `http://mavenrepo.hoodox.com/repo/`. That server
is gone and the library's source code is not in this repo or anywhere we can reach, so Maven stops with
`com.hoodox.commons:hoodox-commons:pom:1.0.2 (absent)` before compiling anything.

The user decided to **rebuild that library as a new Maven module, `hoodox-commons`, inside this repo**. Operon uses only 7 of its
classes, so the rebuild is small. Because the original source is lost, each class is rebuilt from what its callers need
(listed in *Approach → Inferred contract*), and the plan says plainly where that is a guess.

Rebuilding the library is not enough on its own. A trial build on the machine's current tools (OpenJDK 21.0.13, Maven 3.9.9, run
on 2026-11-25 in a scratch copy of the repo with an empty local Maven repository) found eight more blockers. This plan fixes
**only what the new platform needs to build and run the code on the installed JDK 21**, keeping the old library versions wherever
they still work. Everything else (Java 25, Spring, Quartz, MyBatis, Jakarta JAXB and the rest) belongs to
[the upgrade plan](261125-2113-upgrade-java-libraries-and-maven-to-latest.md).

**Rule for this whole effort (user, 2026-11-26): modernisation keeps every feature behaving exactly as the code does today.** No
feature code changes here. Two of the repo's own non-database tests do not pass on the code as committed (see *Approach → Test
results today*); this plan records them as the baseline and does not change the code they exercise. They fail the same way on
the original toolchain (Java 6/7/8, Maven 3.0.5, the poms' own library versions, MySQL 5.5), so the baseline is today's real
behaviour (Open question 1, answered 2026-11-26).

Intended outcome: `git clone` + one `mvn -B clean install` at the repo root builds all modules; the `operon` tests run and give
exactly the baseline result (7 of the 9 non-database TestNG tests pass, the same 2 fail the same way); the new `hoodox-commons`
tests pass. The 5 database tests move to an opt-in Maven profile used by
[the Postgres conversion plan](261125-2112-convert-operon-from-mysql-to-postgres.md).

## Approach

### What blocks a clean build today, and who fixes it

Measured with `mvn -B -Dmaven.repo.local=<scratch>/m2 ...` on a `git archive HEAD` copy (nothing written to the repo tree).

| # | Blocker (observed) | Fixed here? |
|---|---|---|
| 1 | No root `pom.xml`: `parent-pom` has no `<modules>`, so each module must be installed by hand in order (parent, opnmlJAXB, operon, plugin). | **Yes**: root aggregator pom. |
| 2 | `hoodox-commons:1.0.2` absent. Maven 3.8+ also blocks every `http://` repository in the poms (`maven-default-http-blocker`); all other dependencies resolve from Maven Central (a `dependency:resolve` with hoodox-commons removed was `BUILD SUCCESS`). | **Yes**: new module; dead repository lists removed. |
| 3 | `javac` 21: `error: Source option 5 is no longer supported. Use 8 or later.` (parent-pom source/target 1.5). | **Yes**: 1.8 (lowest level JDK 21 accepts). Java 25 is Plan 4. |
| 4 | `jaxb2-maven-plugin:1.5` (xjc 2.1.13) crashes: `InaccessibleObjectException ... ClassLoader.defineClass ... module java.base does not "opens java.lang"`. | **Yes**: `.mvn/jvm.config` with `--add-opens` (verified: generates 98 classes). Plugin upgrade is Plan 4. |
| 5 | Tests die at start: `NoSuchMethodError: org.slf4j.impl.StaticLoggerBinder.getSingleton()`; `slf4j-log4j12 1.5.2` binding vs `slf4j-api 1.6.0` pulled in by `quartz 1.8.6`. | **Yes**: binding and API both 1.6.6 (verified). slf4j 2 / logback is Plan 4. |
| 6 | No JAXB runtime in JDK 11+: `JAXBContext` fails. Tried: `jaxb-impl 2.1.13` fails (same `defineClass` error), `2.2.11` fails (`ClassNotFoundException ...AnnotationReader`), **`2.3.9` works** (still the `javax.xml.bind` namespace). | **Yes**: `com.sun.xml.bind:jaxb-impl:2.3.9`, runtime scope. Jakarta is Plan 4. |
| 7 | iBatis maps live under `src/main/java` and `DefaultOperonToolSpecifics.pnml` + db scripts under `src/resources`, so Maven never copies them: `FileNotFoundException: class path resource [com/hoodox/operon/persistence/ibatis/sqlmap/sql-map-config.xml]`. | **Yes**: `<resources>` in `operon/pom.xml`. |
| 8 | Plugin: `package org.codehaus.plexus.util does not exist` (`OperonWopedMergeMojo.java:33`; Maven 2 used to supply it). | **Yes**: `plexus-utils 1.5.15` (verified `BUILD SUCCESS`, 1 mojo descriptor). Annotations/Maven 3 API is Plan 4. |
| 9 | With rows 1-8 done, 2 of the 9 non-database tests fail on the committed code (see *Test results today*). With failing tests, `mvn install` stops at `operon` and never builds the plugin module. | **Build only**: the failures are recorded as the baseline, not fixed; `operon`'s surefire gets `testFailureIgnore` so the reactor finishes, and Verification checks the exact baseline. Open question 1. |
| 10 | The 5 `WorkflowEngineTest` tests need a running database (MySQL today). Against no database they stalled for about 2.5 minutes each. | **Partly**: moved to an opt-in `db-tests` profile (Plan 3 points it at Postgres). |
| - | Spring 2.5.6.SEC03, iBatis 2, Quartz 1.8.6, BSF/BeanShell, commons-*, log4j 1.2, TestNG 5.8, surefire 2.4.3, `wagon-ssh` + dead `scp://` deploy target, plugin tests in the non-standard `src/main/test/java` (never compiled), Eclipse `.classpath` files pointing at `hoodox-commons-1.0.1.jar`. | **No**: they work on JDK 21 or are not part of the build; Plan 4. |

### Test results today (baseline, left as is)

Re-checked by running on 2026-11-26 (scratch copy from `git archive HEAD`, rows 1-8 applied, the prototype `hoodox-commons`, no
change to any `.java` file, OpenJDK 21.0.13, Maven 3.9.9): `Tests run: 9, Failures: 2`. Same result with the JAXB runtime the
code was written for (`jaxb-impl` 2.1.13, run with `--add-opens java.base/java.lang=ALL-UNNAMED`) as with 2.3.9.

| Test | Loads | Result today | Why (read from the code) |
|---|---|---|---|
| 7 of the 8 `OpnmlJaxHelperTest` methods (`testLoadNet`, `testLoadWopedNet`, XOR-Join, Subnets, XOR-split-join, XOR-join-AND-split, AND-join-XOR-split) | the matching `META-INF/*.pnml` / `ExampleNetNonWopedFormat.xml` | pass | |
| `OpnmlJaxHelperTest.testCleanupWoped_OR_Split_forOperon` | `META-INF/OR-Split.pnml` | fails, `AssertionError` at `OpnmlJaxHelperTest.java:100` (expects 1 transition) | the WoPeD clean-up list at `WopedJAXBHelper.java:110-114` does not include operator type 104 (`XOR_split`), which this file uses, so its two transitions stay separate |
| `CaseTypeManagerTest.testLoadCaseTypeManager` | `META-INF/OperonRegistery.xml` → `ExampleNetNonWopedFormat.xml` (the design's sample net, section 5.5, with the `MarketSurvey` subnet) | fails, `ClassCastException: TransitionType cannot be cast to PlaceType` at `WopedJAXBHelper.java:324` | on the normal net-loading path (`CaseTypeManager` → `OpnmlJAXBHelper.createCase:197` → `prepareWopedForOperon`): subnet arc `sa1 source="Sin"` (`ExampleNetNonWopedFormat.xml:545`) starts at a `referencePlace`, which JAXB gives as `ReferencePlaceType` (not a `PlaceType` subclass), so the `else` branch casts the target transition |
| 5 `WorkflowEngineTest` tests (database) | Spring test context; registry and net read from `config_repository` (seed rows hold the same net with the same subnet) | cannot start: the engine's constructor loads that net through the same path | same as above |

Why this is not the scratch setup: the stand-in `ConfigurationHelper` only hands over the file's text; both failures happen after
JAXB has built the objects, decided by the XSD-generated classes (`opnmlJAXB`, unchanged generator `jaxb2-maven-plugin` 1.5) and
the operator list in the code. JDK, JAXB runtime and slf4j versions were varied without a different result. The original
`hoodox-commons` jar was not found (not on Maven Central, not in the Internet Archive, not in `~/.m2` on this machine; checked
2026-11-26).

**Original toolchain, re-run 2026-11-26** (scratch copy of `git archive HEAD`, throwaway containers, nothing in the repo
touched): Maven 3.0.5 (2013) on OpenJDK 8u282, Azul Zulu 6u119 and OpenJDK 7u221, every library at the version in the
committed poms (all on Maven Central, including `spring` 2.5.6.SEC03 and `org.apache.ibatis:ibatis-sqlmap` 2.3.4.726), the
JAXB built into each JDK (no `jaxb-impl`), `jaxb2-maven-plugin` 1.5, source/target 1.5, and MySQL 5.5.62 loaded with
`operon-mysql-schema.sql` and `sample-test-data.sql` (the database tests read the registry and net from `config_repository`).
- With the poms exactly as committed, the suite cannot start on Java 8:
  `NoSuchMethodError: org.slf4j.impl.StaticLoggerBinder.getSingleton()` (row 5; dependency resolution is the same on any JDK).
- With only that fixed (`slf4j-api` 1.5.2, as in `operon/.classpath`) and `src/main/java` XML + `src/resources` on the classpath
  (as the Eclipse project `operon/.classpath` has them; row 7), all three JDKs give the same result: `Tests run: 14, Failures: 7`
  — `testCleanupWoped_OR_Split_forOperon` (`OpnmlJaxHelperTest.java:100`), `testLoadCaseTypeManager` and all 5
  `WorkflowEngineTest` tests (`ClassCastException` at `WopedJAXBHelper.java:324`, after the net was read from MySQL).
- What still differed from the original: a stand-in `hoodox-commons` (Java 5 syntax; returns the file text from the classpath or
  from `config_repository`), Maven 3.0.5 instead of Maven 2, `ENGINE=MYISAM` for MySQL 5.5's removed `type=MYISAM`, a mirror to
  Maven Central for the dead repositories. The OR-split failure does not depend on any of these (it is the operator list in the
  code); the cast failure happens after JAXB has built the objects.
- `operon/.classpath` shows the developer's IDE used other jars than the committed poms (`quartz` 1.5.2, `slf4j` 1.5.2,
  `hoodox-commons` 1.0.1, an `opnmlJAXB-1.0.1.jar` from the local repository), so the committed poms were not what last ran the
  tests; an older `opnmlJAXB` jar or net file could explain a memory of a green suite, but no such file exists to check.

Observed, left as is: the OR-split clean-up list and the reference-place cast above.

### Inferred contract of the rebuilt classes

Every caller was read (`grep -rn "com.hoodox.commons"`: 37 operon main/test files, `OperonWopedMergeMojo.java`, and the Spring
file `operon/src/test/resources/META-INF/applicationContext.xml`). A prototype of exactly this contract, built in the scratch copy,
compiled all of `operon` (75 main + 10 test files) and the plugin with no source change.

| Class | Contract the callers need (evidence) |
|---|---|
| `exceptions.ErrorCode` | `new ErrorCode(String)` (e.g. `CaseTypeManager.java:99`); `getCode()` returns the string. Serializable. |
| `exceptions.IBaseException` | interface: `String getErrorCode()` (`AuditImpl.java:68` passes it to `EventAuditVo.setErrorCode(String)`), `String getMessage()`, `boolean isLogged()` (`Activity.java:105`), `void setLogged()` with **no argument** (27 calls, e.g. `CaseTypeManager.java:104`). |
| `exceptions.BaseSystemException` | `extends RuntimeException implements IBaseException` (thrown without `throws`, `CaseTypeManager.java:105`; `AuditImpl.java:21-22` builds one for a `RuntimeException`). Constructors `(String)`, `(ErrorCode, String)`, `(ErrorCode, String, Throwable)`, `(Throwable)` (all four used by `operon/.../exceptions/*`). |
| `exceptions.BaseAppException` | `extends Exception implements IBaseException` (checked: declared in `throws` clauses, caught at `StartWorkItemJob.java:52`, built for non-runtime causes at `AuditImpl.java:24`). Same four constructors. |
| `exceptions.XMLAccessException` | Thrown by `ConfigurationHelper.createDomFromFile`; caught next to, and after, `IOException` (`CaseTypeManager.java:98-114`), so it is **not** an `IOException` subclass. Rebuilt as a checked `BaseAppException` subclass. |
| `configurable.ConfigurationHelper` | No-arg constructor (tests, Mojo). Setters from `applicationContext.xml:45-55`: `applicationName`, `dataSource` (`javax.sql.DataSource`), `defaultServerId`, `useDatabase` (boolean), and a commented-out `configRootDir`. `void createDomFromFile(String fileName) throws IOException, XMLAccessException`; `String getDomAsString()` returns the XML of the last file loaded (`CaseTypeManager.java:90-91`). |
| `util.ClassCache` | `static ClassCache getInstance()`; `Object getObject(String className)` creates the class once with its no-arg constructor and returns the same object afterwards (13 calls in `WorkflowCoreEngineImpl.java`, e.g. line 544, to get the net's `ResourceManager`). |

How `ConfigurationHelper` finds a file (inferred):

- `useDatabase=false` (the default, used by the tests and the Mojo, whose comments say "default will use class path"): try the file
  system under `configRootDir` if set, then the classpath name, then `META-INF/<name>` on the classpath, then the plain file path.
  The `META-INF/` step is needed because `CaseTypeManagerTest` asks for `"OperonRegistery.xml"` and the file is at
  `src/test/resources/META-INF/OperonRegistery.xml`. Missing everywhere: `FileNotFoundException` (an `IOException`).
- `useDatabase=true` (the Spring test context): read `CONTENT` from `config_repository` where `APPLICATION_NAME` =
  `applicationName`, `SERVER_ID` = `defaultServerId` and `FILE_NAME` = the name. That table (key
  `APPLICATION_NAME, SERVER_ID, FILE_NAME`, `operon-mysql-schema.sql:1-9`) and its seed rows (`sample-test-data.sql`,
  `('tailorit','default','OperonRegistery.xml', ...)`) only make sense as this helper's store. No row: `XMLAccessException`
  naming the three values (no silent fall-back, per the comment at `applicationContext.xml:53`). SQL errors: `XMLAccessException`.
  Plain JDBC on the `DataSource`, so the module needs no Spring.
- Either way the text is parsed into a DOM (namespace-aware); a parse error is an `XMLAccessException`. `getDomAsString()`
  serialises that DOM back to a UTF-8 string.

**Risk that the original differed:** server id resolution (the name `defaultServerId` suggests the original could pick a
per-machine id first), the exact text `getDomAsString()` produced, error codes, caching, and any classes Operon never called. Only
behaviour Operon depends on is rebuilt; a host application that used other parts of the old jar will not compile against this one.

### Code changes

**Root aggregator (new `pom.xml`)** — *Before:* no root pom. *After:*

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.hoodox.operon</groupId>
  <artifactId>operon-build</artifactId>
  <version>1.0.1</version>
  <packaging>pom</packaging>
  <modules>
    <module>parent-pom</module>
    <module>hoodox-commons</module>
    <module>opnmlJAXB</module>
    <module>operon</module>
    <module>maven-operon-woped-merge-plugin</module>
  </modules>
</project>
```

**`.mvn/jvm.config` (new)** — lets the old xjc run on JDK 21; Plan 4 deletes it with the JAXB upgrade:

```
--add-opens java.base/java.lang=ALL-UNNAMED
```

**`parent-pom/pom.xml:53-60`** — *Before:*

```xml
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>1.5</source>
					<target>1.5</target>
				</configuration>
			</plugin>
```

*After:* version pinned to what Maven 3.9.9 picks today, level 1.8:

```xml
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.13.0</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
```

**`parent-pom/pom.xml:85-152`** (the `<repositories>` block: codehaus snapshots, people.apache.org, `http://repo1.maven.org`,
ibiblio, `http://mavenrepo.hoodox.com/repo/`) and **`opnmlJAXB/pom.xml:163-198`** (`<repositories>` and `<pluginRepositories>` on
`download.java.net`, incl. the `legacy` layout that Maven warns about) — *Before:* those blocks; *After:* removed (Maven Central,
the built-in default, serves everything).

**`operon/pom.xml:32`** and **`:143-147`** — *Before:*

```xml
		<slf4j-log4j12.version>1.5.2</slf4j-log4j12.version>
...
		<dependency>
		    <groupId>org.slf4j</groupId>
		    <artifactId>slf4j-log4j12</artifactId>
		    <version>${slf4j-log4j12.version}</version>
		</dependency>
```

*After:*

```xml
		<slf4j.version>1.6.6</slf4j.version>
		<jaxb-impl.version>2.3.9</jaxb-impl.version>
...
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
			<version>${slf4j.version}</version>
		</dependency>
		<dependency>
		    <groupId>org.slf4j</groupId>
		    <artifactId>slf4j-log4j12</artifactId>
		    <version>${slf4j.version}</version>
		</dependency>
		<dependency>
			<!-- JAXB runtime: no longer in the JDK since Java 11 -->
			<groupId>com.sun.xml.bind</groupId>
			<artifactId>jaxb-impl</artifactId>
			<version>${jaxb-impl.version}</version>
			<scope>runtime</scope>
		</dependency>
```

**`operon/pom.xml:223-236`** — *Before:*

```xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>${maven-surefire-plugin.version}</version>
				<configuration>
					<suiteXmlFiles>
						<suiteXmlFile>${basedir}/src/test/resources/testng.xml</suiteXmlFile>
					</suiteXmlFiles>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

*After:* resources added; surefire reports failures without stopping the reactor (row 9; the two baseline failures are not
fixed in this effort); a
`db-tests` profile runs the database suite as well:

```xml
	<build>
		<resources>
			<resource>
				<directory>src/main/java</directory>
				<includes><include>**/*.xml</include></includes>
			</resource>
			<resource>
				<directory>src/resources</directory>
			</resource>
		</resources>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>${maven-surefire-plugin.version}</version>
				<configuration>
					<!-- known baseline failures, see the plan's "Test results today" -->
					<testFailureIgnore>true</testFailureIgnore>
					<suiteXmlFiles>
						<suiteXmlFile>${basedir}/src/test/resources/testng.xml</suiteXmlFile>
					</suiteXmlFiles>
				</configuration>
			</plugin>
		</plugins>
	</build>
	<profiles>
		<profile>
			<id>db-tests</id>
			<build>
				<plugins>
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-surefire-plugin</artifactId>
						<configuration>
							<suiteXmlFiles combine.self="override">
								<suiteXmlFile>${basedir}/src/test/resources/testng.xml</suiteXmlFile>
								<suiteXmlFile>${basedir}/src/test/resources/testng-db.xml</suiteXmlFile>
							</suiteXmlFiles>
						</configuration>
					</plugin>
				</plugins>
			</build>
		</profile>
	</profiles>
```

**`operon/src/test/resources/testng.xml:11-13`** — *Before:*

```xml
			<class
				name="com.hoodox.operon.workflow.WorkflowEngineTest">
			</class>
```

*After:* removed here; a new `operon/src/test/resources/testng-db.xml` (suite `operon-db`) lists only
`com.hoodox.operon.workflow.WorkflowEngineTest`.

**No `.java` file under `operon/` or `maven-operon-woped-merge-plugin/` changes** (in particular `WopedJAXBHelper.java` stays as
it is; see *Test results today*).

**`maven-operon-woped-merge-plugin/pom.xml:28-33`** — *Before:*

```xml
	<dependencies>
		<dependency>
			<groupId>org.apache.maven</groupId>
			<artifactId>maven-plugin-api</artifactId>
			<version>${maven-plugin-api.version}</version>
		</dependency>
```

*After:* `plexus-utils` added after `maven-plugin-api`:

```xml
		<dependency>
			<groupId>org.codehaus.plexus</groupId>
			<artifactId>plexus-utils</artifactId>
			<version>1.5.15</version>
		</dependency>
```

The `com.hoodox.commons:hoodox-commons:1.0.2` dependency lines in `operon/pom.xml:104-108` and
`maven-operon-woped-merge-plugin/pom.xml:40-44` stay as they are: the new module keeps those coordinates, so the reactor resolves it.

## Scope

- **In:** everything marked "Yes" in the blocker table; the 7 rebuilt classes with tests; a build section in `README.md`.
- **Out:** every "No" row (Plan 4); the database itself and making `WorkflowEngineTest` pass (Plans 2 and 3); the stray
  `operon/documents/pom.xml` (not a module, wrong parent); Eclipse `.classpath`/`.project` files.

## Implementation

**Folders touched** (this repo has no `.claude/rules/`, so no scoped rule applies to any of them):
- repo root (`pom.xml`, `.mvn/`, `README.md`) → no scoped rule
- `parent-pom/` → no scoped rule
- `hoodox-commons/` (new) → no scoped rule
- `opnmlJAXB/` → no scoped rule
- `operon/` (`pom.xml`, `src/test/resources/`) → no scoped rule
- `maven-operon-woped-merge-plugin/` → no scoped rule

**Flow:** none — a build fix and a library rebuilt to the call shapes its callers already use; no feature code changes.

1. Root build
   - new: `pom.xml` (aggregator, see Approach)
   - new: `.mvn/jvm.config` (see Approach)
2. Java level and dead repositories
   - edit: `parent-pom/pom.xml` — compiler plugin 3.13.0, source/target 1.8; delete lines 85-152 (`<repositories>` and its comment)
   - edit: `opnmlJAXB/pom.xml` — delete lines 163-198 (`<repositories>`, `<pluginRepositories>`)
3. New module `hoodox-commons/`
   - new: `hoodox-commons/pom.xml` — parent `com.hoodox.operon:operon-parent:1.0.1` (`relativePath ../parent-pom`), own coordinates
     `com.hoodox.commons:hoodox-commons:1.0.2`, packaging jar; no compile dependencies; test: `org.testng:testng:5.8:jdk15`
     (same as operon) and `com.h2database:h2:1.4.200` (in-memory database for the `useDatabase=true` tests); surefire
     `${maven-surefire-plugin.version}` (2.4.3, from the parent)
   - new: `hoodox-commons/src/main/java/com/hoodox/commons/exceptions/{ErrorCode,IBaseException,BaseSystemException,BaseAppException,XMLAccessException}.java`
     per *Inferred contract*; `getErrorCode()` returns `errorCode == null ? null : errorCode.getCode()`; `logged` is a plain field
   - new: `hoodox-commons/src/main/java/com/hoodox/commons/configurable/ConfigurationHelper.java` per *How ConfigurationHelper
     finds a file*; read with UTF-8; DOM via `DocumentBuilderFactory` (namespace-aware), string via `Transformer` with
     `encoding=UTF-8`; SQL `select CONTENT from config_repository where APPLICATION_NAME = ? and SERVER_ID = ? and FILE_NAME = ?`
   - new: `hoodox-commons/src/main/java/com/hoodox/commons/util/ClassCache.java` — singleton, `ConcurrentHashMap<String,Object>`,
     `Class.forName(name, true, contextClassLoader).getDeclaredConstructor().newInstance()`, `putIfAbsent` so two threads get one
     object; any failure → `BaseSystemException(new ErrorCode("class_cache_error"), "Cannot create " + name, e)`
   - new tests in `hoodox-commons/src/test/java/com/hoodox/commons/`:
     - `exceptions/BaseExceptionsTest.java` — all four constructors of both base classes keep message, cause and error code;
       `getErrorCode()` null without a code; `isLogged()` false, then true after `setLogged()`; `BaseSystemException` is a
       `RuntimeException`, `BaseAppException` is not; `XMLAccessException` is a `BaseAppException`
     - `configurable/ConfigurationHelperTest.java` — finds `sample.xml` at the classpath root and `meta-only.xml` under
       `META-INF/`; `getDomAsString()` parses back to the same root element and attribute values; missing file →
       `FileNotFoundException`; malformed XML → `XMLAccessException`; `useDatabase=true` with an H2 `config_repository` table and
       one row returns that row's XML; a missing row → `XMLAccessException` whose message names application, server and file
     - `util/ClassCacheTest.java` — same instance on two calls; distinct classes give distinct objects; unknown class →
       `BaseSystemException`; 8 threads asking at once all get one instance
   - new: `hoodox-commons/src/test/resources/sample.xml`, `hoodox-commons/src/test/resources/META-INF/meta-only.xml`,
     `hoodox-commons/src/test/resources/malformed.xml`
4. `operon` build fixes
   - edit: `operon/pom.xml` — slf4j 1.6.6 (api + binding), `jaxb-impl` 2.3.9 runtime, `<resources>`, surefire
     `testFailureIgnore`, `db-tests` profile (see Approach)
   - edit: `operon/src/test/resources/testng.xml` — remove `WorkflowEngineTest` (lines 11-13)
   - new: `operon/src/test/resources/testng-db.xml` — suite `operon-db` with `WorkflowEngineTest` only
5. (removed 2026-11-26: no feature code changes; the two failing tests are the baseline, see *Test results today*)
6. Plugin
   - edit: `maven-operon-woped-merge-plugin/pom.xml` — add `plexus-utils` 1.5.15
7. Docs
   - edit: `README.md` — "Building": JDK 21 or later, Maven 3.9+, `mvn -B clean install` at the root; `-Pdb-tests` needs the
     database from [Plan 2](261125-2111-docker-postgres-database-for-dev-and-test.md) and passes once
     [Plan 3](261125-2112-convert-operon-from-mysql-to-postgres.md) is done

## Verification

From a fresh clone and an empty local Maven repository, so nothing cached can hide a missing artifact:

```bash
T=$(mktemp -d) && git clone <this repo> "$T/operon" && cd "$T/operon" && git checkout <branch>
mvn -B -Dmaven.repo.local="$T/m2" clean install 2>&1 | tee "$T/build.log"
grep -E "Reactor Summary|SUCCESS|FAILURE|Tests run:" "$T/build.log"
```

Expected:
- `BUILD SUCCESS`; the reactor summary lists `operon-build`, `Operon Workflow Framework` (parent), `hoodox-commons`,
  `Jaxb xml petrinet reader for Operon`, `Operon Workflow Framework` (operon), `maven-operon-woped-merge-plugin`, each `SUCCESS`.
- `hoodox-commons`: all tests of the three new test classes pass, `Failures: 0, Errors: 0`.
- `operon`: exactly the baseline of *Test results today*: `Tests run: 9, Failures: 2, Errors: 0, Skipped: 0`, and
  `operon/target/surefire-reports/TestSuite.txt` names only `testCleanupWoped_OR_Split_forOperon` (`AssertionError` at
  `OpnmlJaxHelperTest.java:100`) and `testLoadCaseTypeManager` (`ClassCastException` at `WopedJAXBHelper.java:324`). Any other
  failure, or either of these passing, is a difference from today and stops the plan.
- `git diff --stat -- '*.java'` lists only files under `hoodox-commons/`.
- `unzip -l "$T/m2/com/hoodox/operon/maven/pnml/maven-operon-woped-merge-plugin/1.0.1/maven-operon-woped-merge-plugin-1.0.1.jar" | grep plugin.xml`
  shows `META-INF/maven/plugin.xml`.
- `unzip -l operon/target/operon-1.0.1.jar | grep -E "sqlmap/(Case|sql-map-config).xml|DefaultOperonToolSpecifics.pnml"` lists all three.
- No network fetch from a removed repository: `grep -cE "mavenrepo.hoodox.com|http-blocker" "$T/build.log"` prints `0`.
- `mvn -B -Pdb-tests -pl operon test` is *expected to fail* (no Postgres configuration yet, and the context load of
  *Test results today*); it must at least show that `WorkflowEngineTest` was picked up (`WorkflowEngineTest` in the output).
- `git status` shows only the files named in *Implementation*.

## Risks & rollback

- The rebuilt `ConfigurationHelper` and `ClassCache` are inferred (see *Risk that the original differed*). Mitigation: the operon
  tests that load real nets through it pass; anything beyond Operon's own calls is not promised.
- `testFailureIgnore` lets a new failure through the build unnoticed. Mitigation: Verification checks the exact failing set; it
  stays as long as the two baseline failures exist (they are not fixed in this effort).
- `.mvn/jvm.config` opens `java.lang` to unnamed modules for the whole Maven JVM. Build-time only; Plan 4 removes it.
- Rollback: revert the commit; nothing outside the repo changes.

## Open questions

1. **The two failing tests (and the 5 database tests behind the same cause).** On the committed code, `CaseTypeManagerTest` and
   `testCleanupWoped_OR_Split_forOperon` fail, and `WorkflowEngineTest` cannot load its Spring context, because loading the
   sample net with its subnet stops at `WopedJAXBHelper.java:324` (details in *Test results today*). This was reproduced on JDK 21
   with both the old and the new JAXB runtime; nothing in the build setup changed it. The user has said the tests work. What is
   blocked: no plan in this effort can show a green test suite, and the database tests cannot exercise the engine on any
   database. The user decides one of:
   - (a) you have a source, jar or environment where these tests passed (for example the original `hoodox-commons` jar or newer
     source than this commit): share it and this plan is re-checked against it;
   - (b) accept today's result as the baseline: the plan builds as written (`testFailureIgnore`, exact-baseline check);
   - (c) treat it as a feature change and file a separate plan for it, outside this modernisation effort.

   **Answered by evidence, 2026-11-26:** on the original toolchain (see *Original toolchain, re-run* above) the same tests fail
   the same way, so (b) is today's behaviour and the plan follows it. No setup that passes them was found. This blocks nothing:
   only if the user produces a jar, source or environment where they pass (a) is the plan re-checked against it.

## Dependencies

### Blocks this plan (must be done first)
None.

### Blocked by this plan (cannot start until this is done)
- [Convert Operon from MySQL to PostgreSQL](261125-2112-convert-operon-from-mysql-to-postgres.md) — PLANNED;
  **blocked** (needs a building repo and the `db-tests` profile)
- [Upgrade Java, libraries and Maven to latest](261125-2113-upgrade-java-libraries-and-maven-to-latest.md) — PLANNED / NEEDS
  RE-REVIEW; **blocked** (upgrades start from this baseline)
- [Give a step that is restarted at server startup a fresh retry timer](backlog/261126-0816-reset-the-retry-timer-when-a-step-is-restarted-at-startup.md)
  — PLANNED, **on hold** (user rule, no feature behaviour changes, 2026-11-26); not waiting to be built. A standalone plan, not in
  the loop.
- [Start the retry timer when the step starts](backlog/261124-1715-start-the-retry-timer-when-the-step-starts.md) — PLANNED, **on hold**
  (same user rule, 2026-11-26); not waiting to be built. A standalone plan, not in the loop.

## Status

- 2026-11-25 (operon): PLANNED — written by the owning team. Blockers measured on a scratch copy (OpenJDK 21.0.13, Maven 3.9.9,
  empty local repository); a throw-away prototype of the inferred contract compiled `operon` and the plugin unchanged, and with
  the two bug fixes the 9 non-database tests passed. No code in the repo changed. Awaiting the user's approval.
- 2026-11-26 (operon): PLANNED — joined the loop [Modernise Operon](261126-0754-modernise-operon-loop.md), created at the user's request.
- 2026-11-26 (operon): PLANNED — the standalone plan
  [261126-0816](backlog/261126-0816-reset-the-retry-timer-when-a-step-is-restarted-at-startup.md) waits on this one; added under
  *Blocked by this plan*. No change to this plan.
- 2026-11-26 (operon): PLANNED / NEEDS RE-REVIEW — owning-team re-review under the user's rule "modernisation keeps every feature
  behaving as today; no bug fixes". Removed both `WopedJAXBHelper.java` changes (old Implementation step 5). Re-ran the evidence
  in a scratch copy (rows 1-8 applied, no `.java` change): `Tests run: 9, Failures: 2` with `jaxb-impl` 2.3.9 and with 2.1.13;
  traced both failures to the committed code on the normal net-loading path (new *Test results today*), not to the stand-in
  library or the JDK. Added: surefire `testFailureIgnore` and an exact-baseline check so the build still completes; Open
  question 1 for the user. 0816 dependency line set to on hold. Stays `NEEDS RE-REVIEW` until Open question 1 is answered.
- 2026-11-26 (operon): PLANNED / RE-REVIEW DONE — independent owning-team re-review. Open question 1 answered by evidence: on the
  original toolchain (Maven 3.0.5 on Java 6, 7 and 8, the poms' own library versions, built-in JAXB, MySQL 5.5 with the repo's
  schema and seed; throwaway containers and a scratch copy) the committed poms cannot start the tests (slf4j), and with that
  alone fixed the same 2 tests fail and the 5 database tests stop at the same cast (new *Original toolchain, re-run*). The
  original `hoodox-commons` jar was searched for and not found. The plan's implementation is unchanged; `testFailureIgnore` now
  stays for the whole effort. Added the on-hold 1715 under *Blocked by this plan* (it waits on this plan); 2112's tag updated.
- 2026-11-26 (operon): APPROVED — approved by the user ("approve the plans"). Still in `backlog/`, so nothing may be built until the loop is picked into `todos/`.
- 2026-11-26 (operon): APPROVED — picked from `backlog/` into `todos/` with the Modernise Operon loop, on the user's go-ahead. Not started: the user is clearing the session first; the new session dispatches.
