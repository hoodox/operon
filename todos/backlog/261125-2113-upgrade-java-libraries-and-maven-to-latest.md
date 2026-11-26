# Plan: Upgrade Operon to OpenJDK 25 and the latest stable libraries, Maven plugins and Maven

**Status:** APPROVED
**Loop:** [Modernise Operon](261126-0754-modernise-operon-loop.md)
**Review model:** opus, high

## Context

**Rule for this whole effort (user, 2026-11-26): modernisation keeps every feature behaving exactly as the code does today.** A
step may change code only where the new library or JDK cannot work without it, and the change must reproduce today's behaviour;
the characterization tests below pin today's behaviour and are never "flipped".
**One deliberate exception (user, 2026-11-26, "lets move to 2.1.1"):** the expression library moves to BeanShell
`io.github.beanshell:bsh` 2.1.1, which fixes CVE-2016-2510, and the user accepts the differences that come from BeanShell 2.1.1
itself (listed under *Decisions → Expressions*; the repo's own nets are unaffected). The exception covers only those
differences: no adapter tries to undo them, nothing else changes behaviour, and the rule still applies to every other step.

Operon dates from about 2009-2013. After [Plan 1](261125-2110-rebuild-hoodox-commons-module.md) it builds on the installed
OpenJDK 21 and its tests give a recorded baseline (2 known failures, see Plan 1 → *Test results today*), but only by compiling for Java 8, opening JDK internals for an old code generator, and holding on to
libraries that are long out of support: Spring 2.5.6.SEC03 (2011), iBATIS 2 (retired; its successor is MyBatis), Quartz 1.8.6,
log4j 1.2.14, BSF 2.3.0 with BeanShell 2.0b1, `javax.xml.bind` JAXB (removed from the JDK after Java 8), commons-lang 2.3,
commons-dbcp 1.2.1, TestNG 5.8, surefire 2.4.3 and a Maven 2 era plugin API. The user decided the whole project moves to the
latest Java, the latest stable libraries and the latest Maven.

Target Java is **OpenJDK 25**, the current long-term-support release (Adoptium's API lists 25 as `most_recent_lts`; 27 is newer but
not LTS; checked 2026-11-25). The user wants an OpenJDK build, not Oracle's JDK.

The safety net is thin. `operon/src/test/resources/testng.xml` runs 9 TestNG tests (JAXB loading and WoPeD clean-up in
`OpnmlJaxHelperTest`, one `CaseTypeManagerTest`; 7 pass, 2 fail today), the `db-tests` profile adds 5 `WorkflowEngineTest` tests
(they stop while loading the sample net on any database, Plan 1 Open question 1); `AppTest` asserts `true`;
`WopedJaxbMergeHelperTest` is in no suite (its 1 test passes today, run 2026-11-26);
the plugin's tests sit in `src/main/test/java` and never compile. So this plan **writes characterization tests first** (tests that
pin down what the code does today, right or wrong) for the four risky areas, and then moves one library group at a time, each step
ending with a green build.

Intended outcome: `./mvnw -B clean install` (Maven 3.10.2 via the wrapper) on OpenJDK 25 builds every module for Java 25 with the
versions in the table below, every test gives the same result as before the upgrade (Plan 1's baseline, the database baseline
of Plan 3, and the new characterization tests unchanged, except the expression test's methods for the accepted BeanShell 2.1.1
differences, which step 7 moves from today's result to 2.1.1's), and none of the retired libraries is left (BSF and `bsh-bsf`
included).

## Approach

### Target versions

Checked 2026-11-25 against Maven Central's `maven-metadata.xml` for each artifact, taking the highest version that is not an
alpha, beta, milestone or release candidate; Java from the Adoptium API and Ubuntu's apt; Maven from `dlcdn.apache.org`.

| Area | Today | Target (checked 2026-11-25) |
|---|---|---|
| Java (compile `release` and runtime) | 1.8 (Plan 1) on OpenJDK 21.0.13 | **OpenJDK 25**, Ubuntu package `openjdk-25-jdk` 25.0.5.1+2-1~24.04.1 |
| Maven | 3.9.9, installed by hand, no wrapper | **3.10.2** via Maven Wrapper (`maven-wrapper-plugin` 3.3.4); Maven 4 is still 4.0.0-rc-7 |
| Spring Framework (`spring-context`, `-beans`, `-core`, `-aop`, `-tx`, `-jdbc`, `-test`) | 2.5.6.SEC03 (+ aggregate `spring`, `spring-orm`, `spring-context-support`) | **7.1.0**; aggregate jar, `spring-orm`, `spring-context-support` removed |
| SQL mapping | iBATIS `ibatis-sqlmap` 2.3.4.726 | **MyBatis `org.mybatis:mybatis` 3.6.0** + `mybatis-spring` 4.1.0 (built against Spring 7.0.8, Java 17+) |
| Connection pool | `commons-dbcp` 1.2.1 + `commons-pool` 1.3 | **HikariCP 7.1.0** |
| JDBC driver | `postgresql` 42.7.15 (Plan 3) | 42.7.15 (latest) |
| Scheduler | Quartz 1.8.6 | **Quartz 2.5.2** |
| Logging | log4j 1.2.14 + `slf4j-log4j12` 1.6.6 (Plan 1) | **slf4j-api 2.0.20**; tests: **logback-classic 1.6.8** |
| XML binding | `javax.xml.bind:jaxb-api` 2.1 + `jaxb-impl` 2.3.9 (Plan 1); `jaxb2-maven-plugin` 1.5 | **`jakarta.xml.bind-api` 4.0.5** + **`org.glassfish.jaxb:jaxb-runtime` 4.0.9**; **`org.codehaus.mojo:jaxb2-maven-plugin` 4.3.0** |
| Expressions | `bsf` 2.3.0 + `bsh` 2.0b1 + `bsh-bsf` 1.2b7 | **`io.github.beanshell:bsh` 2.1.1** (newest stable; user's decision 2026-11-26, Open question 1 option (b)); `bsf` and `bsh-bsf` removed. Accepted behaviour differences listed in *Decisions* |
| commons | `commons-lang` 2.3 | **`commons-lang3` 3.22.0** |
| Removed, unused | `commons-beanutils` 1.7.0, `commons-collections` 3.2, `commons-dbutils` 1.1, `commons-digester` 2.0, `xercesImpl` 2.7.1, `activation` 1.1 | none (see step 4) |
| Tests | TestNG 5.8 `jdk15`, JUnit 3.8.1, surefire 2.4.3; `h2` 1.4.200 (Plan 1) | **TestNG 7.12.0**, **surefire 3.6.1**; `h2` **2.5.252**; plugin tests on **JUnit Jupiter 6.1.3** |
| Maven plugins | compiler 3.13.0 / 2.5.1, others Maven defaults | **compiler 3.16.0**, **resources 3.5.0**, **jar 3.5.1**, **install 3.2.0**, **deploy 3.2.0**, **clean 3.5.0**, **enforcer 3.7.0**, **versions 2.25.3** |
| Plugin module | `maven-plugin-api` 2.0, javadoc `@goal` tags, `plexus-utils` 1.5.15, harness 1.2 | **`maven-plugin-api` 3.10.2** (provided), **`maven-plugin-annotations` 3.16.0**, **`maven-plugin-plugin` 3.16.0**, **harness 3.5.1**, no plexus-utils |
| Deploy | `wagon-ssh` 2.5 extension, `scp://mavenrepo.hoodox.com` | removed (the server is gone; nothing deploys anywhere) |

Facts that correct the brief this plan was written from: `javax.xml.bind` is imported in **5 files** (19 import lines:
`CaseTypeManager.java`, `OpnmlJAXBHelper.java`, `OperonWopedMergeMojo.java`, `OpnmlJaxHelperTest.java`,
`WopedJaxbMergeHelperTest.java`); the plugin has **1** `org.codehaus.plexus` import (`OperonWopedMergeMojo.java:33`); and iBATIS's
`enhancementEnabled`/`cacheModelsEnabled` settings are inside an XML comment (`sql-map-config.xml:7-28`) and no `cglib` is on the
classpath, so bytecode enhancement is not in use and is not what forces the MyBatis move.

### Decisions

- **OpenJDK distribution: Ubuntu's `openjdk-25-jdk` from apt.** The installed JDK 21 is Ubuntu's OpenJDK build, the 25 package is
  in the standard archive for this Ubuntu 24.04 (candidate `25.0.5.1+2-1~24.04.1`), and it needs no extra apt repository. A future
  CI image can use Eclipse Temurin 25 (also OpenJDK); the build does not depend on which.
- **Spring Framework 7.1.0, no Spring Boot.** Operon is a library: host applications start it with a `ClassPathXmlApplicationContext`
  over XML bean files (see `Operon.java:30-31`) and the engine looks beans up by name. Boot would add auto-configuration and its own
  runtime model, and change how hosts embed Operon, for nothing this code needs. Spring 7 is the current generation, runs on Java
  25, and is what `mybatis-spring` 4.x is built for. Moving the two Spring XML files to the current schema-based form is in scope.
- **MyBatis 3 (the user's choice), done together with Spring 7.** It cannot be done alone in either order: Spring removed its
  iBATIS support (`org.springframework.orm.ibatis`, used by `CaseSqlMapDao` and `SqlMapClientFactoryBean`) in Spring 4.0, and
  `mybatis-spring` 4.1.0 needs Spring 7. So step 9 swaps Spring, the mapper and the pool in one step, guarded by the
  `CaseDao` characterization test. What can move first (the XML syntax, same beans and proxies) moves in step 8 on the
  old Spring.
- **Logging: slf4j 2 with logback for tests.** Operon is a library, so it depends on `slf4j-api` only and leaves the backend to the
  host; tests use logback (slf4j's native backend, one jar, no bridge). log4j 1.2 and the `slf4j-log4j12` binding go.
- **JAXB: move to the `jakarta.xml.bind` namespace.** `javax.xml.bind` is the retired Java EE name; Spring 7 and every current
  runtime are Jakarta. Consumers that use the generated classes (`NetType`, `PnmlType`, ...) see the package of the annotations
  change, not the classes' own packages (`com.hoodox.operon.jaxb.*` stay).
- **Expressions: BeanShell `io.github.beanshell:bsh` 2.1.1 called directly (`bsh.Interpreter`); BSF 2.3.0, `bsh` 2.0b1 and
  `bsh-bsf` 1.2b7 removed. Decided by the user 2026-11-26 ("lets move to 2.1.1", Open question 1 option (b)), as a deliberate
  exception to the no-behaviour-change rule limited to BeanShell 2.1.1's own differences (listed below as "Accepted
  differences").** One class
  (`BsfExpressionEvaluator.java`) with two call sites (`WorkflowCoreEngineImpl.java:251, 288`) uses BSF only to reach BeanShell.
  Calling BeanShell directly keeps the expression language of every existing net unchanged, which `javax.script` would only wrap.
  How expressions are used today: the only expressions are `<guardExpression>` strings on out-arcs (free `xs:string` in
  `opnmlJAXB/xsd/operon-common.xsd:153` and `Operonml-v1.0.xsd:209`); every one in the repo's nets and examples is a bare
  variable name (`success`, `failure`, `failed`, `cancelled`, `updated`, `complete`, `incomplete`, `in_stock`, `out_of_stock`);
  the variables are the case attributes (`TriggerContext.getCaseAttributes()`, a `HashMap`), set by actions to `Boolean` values
  (e.g. `CheckCreditCardAction.java:26-31`); the result is mapped to `int` (`null` → 0, `Number` → `intValue()`, `Boolean` → 1/0,
  anything else → `EvaluationException`), and the engine takes the arc when it is 1.
  Candidates (re-checked 2026-11-26 against Maven Central `maven-metadata.xml`, the jars' POMs, OSV.dev, the GitHub advisory
  database, NVD and the `beanshell/beanshell` GitHub releases):

  | Coordinates | Newest | Status / activity | Known advisories | Java, licence |
  |---|---|---|---|---|
  | `bsh:bsh` (today) | 2.0b1 | 2003-era beta | CVE-2016-2510 range is "before 2.0b6" (OSV/GHSA map it only to `org.apache-extras` coordinates) | class files Java 1.2; POM has no licence |
  | `org.beanshell:bsh` | 2.0b5 | beta, metadata last updated 2012 | same CVE range | LGPL (POM) |
  | `org.apache-extras.beanshell:bsh` | 2.0b6 | beta, security-only release 2016-02, metadata last updated 2016-02-18 | none for 2.0b6 (fixes CVE-2016-2510) | class files Java 5; Apache-2.0 |
  | `io.github.beanshell:bsh` | **2.1.1** (stable) | final 2.x release (GitHub 2022-12-02; file on Central dated 2026-09-24): 2.0b6 plus backports, "no other work will be done on the 2.x branch" | none (OSV, GHSA, NVD) | class files Java 6; Apache-2.0 |
  | `io.github.beanshell:bsh` | 3.0.0RC3 | release candidate 2026-11-05, active (b1 2026-09-23, RC1 10-12, RC2 10-20) | none (OSV, GHSA, NVD) | Java 8+, "tested on Java 8 through 25"; Apache-2.0 |

  There is no `org.beanshell:beanshell` or `bsh-core` artifact and no 2.1.x outside `io.github.beanshell` (2.1.1 is its only 2.x).
  NVD's other BeanShell CVEs (CVE-2017-5586, CVE-2021-31599, CVE-2022-26111, CVE-2023-40313, CVE-2025-34039) are about products
  that expose a BeanShell interpreter or servlet to users, not about the library; Operon's guard expressions come from the net
  files the host deploys, so the same rule applies to it: net files must be trusted (a guard expression can run any Java).
  **Tested 2026-11-26**, jars from Maven Central (SHA-1 checked), harness outside the repo: 84 cases (the 29 repo cases — each
  guard name true, false and unset, plus the `CheckCreditCardAction` pair and empty attributes — and 55 plausible user
  expressions: arithmetic, comparisons, `&&`/`!`/ternary, `Long`/`Double` truncation, string methods, map/list calls, statements,
  `return`, syntax errors, undefined variables, `/ 0`, a `null`-valued attribute), run through today's BSF path and through the
  planned `bsh.Interpreter` path on 2.0b1, 2.0b6, 2.1.1 and 3.0.0RC3, each on OpenJDK 21.0.13 and on Eclipse Temurin 25.0.5
  (container, no network). Results: Java 21 and 25 gave identical output for every version; all 29 repo cases give the same
  result as today on every candidate; every error is still a `bsh.EvalError` (parse errors are `ParseException`, script
  exceptions `TargetError`, both subclasses), so it still becomes `EvaluationException`. Three differences from today, the same
  on 2.0b6, 2.1.1 and 3.0.0RC3: (1) `status == "APPROVED"` with a `String` attribute is 1 (today 0); (2) a method on a JDK object
  whose class is not public, e.g. `Collections.unmodifiableMap(..).get("k")`, now works (today an `EvaluationException`);
  (3) a `null`-valued attribute reads as `null` → 0 (today `declareBean(id, value, value.getClass())` throws an uncaught
  `NullPointerException`). 3.0.0RC3 also parses lambdas (still an `EvaluationException` here, as a lambda is not a number).
  **Can today's behaviour be kept? (re-review 2026-11-26, from the same test output, re-compared):**
  - Today's own path (BSF 2.3.0 + `bsh` 2.0b1 + `bsh-bsf` 1.2b7) gives identical results on Java 21 and Java 25 for all 84 cases,
    so keeping it is a working option on the new JDK.
  - Difference (3) is Operon's own code, not the library: `_declare` calls `value.getClass()` before handing the value over.
    The port keeps that existing call (`value.getClass();` then `interpreter.set(id, value)`), so a `null` attribute still
    throws the same uncaught `NullPointerException` on 2.1.1.
  - Differences (1) and (2) are BeanShell's own semantics and appear on every newer version tested (2.0b6, 2.1.1, 3.0.0RC3);
    no setting or wrapper in Operon changes how BeanShell evaluates `==` or resolves a method.
  - Also changed on every newer version, including `bsh.Interpreter` on 2.0b1 itself: the text of the error.
    `EvaluationException`'s message embeds `e.getMessage()` (`BsfExpressionEvaluator.java:65`), today
    `BeanShell script error: Parse error at line 1, column 14. Encountered: and BSF info: Operon ...`, on 2.1.1
    `In file: inline evaluation of: ``amount > 100 and flag;'' Encountered "and" ...`. Only BSF produces today's text.
  - So **on 2.1.1 today's behaviour cannot be fully kept.** The only tested way to keep it exactly was to keep today's three
    jars (2.0b1 is inside the CVE-2016-2510 range).

  **The user's decision (2026-11-26): move to 2.1.1.** They were told it fixes CVE-2016-2510 and that some conditions behave
  differently (`status == "APPROVED"` on a `String` is false today and true on 2.1.1), and that the repo's own nets are
  unaffected. **Accepted differences** (BeanShell 2.1.1's own; each pinned by its own characterization test method, see
  step 1 and step 7):
  - **D1 — `==` on equal strings:** `status == "APPROVED"` with `status` = `"APPROVED"` (a `String`): today 0, on 2.1.1 1.
  - **D2 — methods of non-public JDK classes:** `Collections.unmodifiableMap(m).get("k")` with `m` = a `HashMap` holding
    `"k"` → `Integer` 7: today an `EvaluationException`, on 2.1.1 the call works and gives 7.
  - **D3 — error text:** every error is still an `EvaluationException` with the same error code
    (`Const.ERROR_CODE_bsf_evaulation_error`) and the same `"Error in the expression <expr> : "` prefix, but the part after it
    is BeanShell's own message (`e.getMessage()`, `BsfExpressionEvaluator.java:65`), which reads differently. Example, the
    parse error `amount > 100 and flag`: today `Error in the expression amount > 100 and flag : BeanShell script error: Parse
    error at line 1, column 14. Encountered: and BSF info: Operon ...`; on 2.1.1 `Error in the expression amount > 100 and
    flag : In file: inline evaluation of: ``amount > 100 and flag;'' Encountered "and" ...`. Hosts that parse the message
    text see the change; the exception type, error code and prefix do not change.

  **Not a difference, kept as today:** a `null`-valued attribute. That is Operon's own code, not BeanShell's: today's
  `_declare` calls `value.getClass()` (`BsfExpressionEvaluator.java:110`) before BeanShell sees the value, which throws an
  uncaught `NullPointerException` from the constructor. The step 7 port keeps that same call, so the `NullPointerException`
  stays (on 2.1.1 without it, the value would read as `null` → 0, a change the exception does not cover). Nothing else is
  added: no wrapper or setting tries to undo D1-D3.

  **Why 2.1.1 and not 3.0.0RC3:** the plan takes the newest *stable* version; 3.0.0 is a release candidate whose semantics are
  still moving between candidates (RC1 changed truthiness and string-to-number conversion, RC2 numeric narrowing, RC3 what an
  unknown field or missing name gives — exactly the area guard expressions depend on), while 2.1.1 behaves the same as 2.0b6 and
  3.0.0RC3 on every repo expression, has no known advisory and runs on Java 25. **Revisit** when `io.github.beanshell:bsh` 3.0.0
  (final) is on Maven Central: at step 11's versions check, and whenever this plan is resumed; moving to it then is its own
  dated note, with this plan's expression characterization test as the gate (and the user's say-so for any new difference).
- **Pool: HikariCP**, Spring's default pool, a drop-in `DataSource` bean.
- **Tests stay on TestNG** (7.12.0): all real tests are TestNG; `AppTest` (JUnit 3, `assertTrue(true)`) is deleted. Only the Maven
  plugin's harness test uses JUnit Jupiter.

### Code changes per step (before/after)

**Step 2, `parent-pom/pom.xml:53-60`** (as left by Plan 1) — *Before:*

```xml
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.13.0</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
```

*After:*

```xml
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.16.0</version>
				<configuration>
					<release>25</release>
				</configuration>
```

`opnmlJAXB/pom.xml:16` and `:26-35` (its own `maven-compiler-plugin` 2.5.1 pin, which cannot do `release`) are deleted.

**Step 5, `opnmlJAXB/xsd/operonml_binding.xjb:1`** (same for `OperonRegistry_binding.xjb`) — *Before:*

```xml
<jxb:bindings version="1.0" xmlns:jxb="http://java.sun.com/xml/ns/jaxb" xmlns:xs="http://www.w3.org/2001/XMLSchema">
```

*After:*

```xml
<jxb:bindings version="3.0" xmlns:jxb="https://jakarta.ee/xml/ns/jaxb" xmlns:xs="http://www.w3.org/2001/XMLSchema">
```

**Step 5, `CaseTypeManager.java:16-18`** (same pattern in the other 4 files) — *Before:*

```java
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
```

*After:*

```java
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
```

**Step 6, `WorkflowCoreEngineImpl.java:1294-1300`** (Quartz 1 constructors; the same change at `:1342-1355`, `:1741-1745`,
`:1801-1808`, `:2416-2424`, and `deleteJob(id, groupName)` at `:1874`) — *Before:*

```java
		JobDetail jobDetail = new JobDetail(id, groupName,FinishActivityJob.class);
...
		Trigger trigger = new SimpleTrigger(id, groupName);
```

*After:*

```java
		JobDetail jobDetail = JobBuilder.newJob(FinishActivityJob.class).withIdentity(id, groupName).build();
...
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity(id, groupName).startNow().build();
```

Cron triggers become `TriggerBuilder.newTrigger().withIdentity(id, groupName).withSchedule(CronScheduleBuilder.cronSchedule(expr))`,
timed ones `.startAt(startTime)`, and `sched.deleteJob(new JobKey(id, groupName))`. In the jobs,
`context.getJobDetail().getName()` (`StartWorkItemJob.java:36`, `FinishActivityJob.java:38, 43`) becomes
`context.getJobDetail().getKey().getName()`; `JobDataMap` access (`ExpireCaseJob.java:28` etc.) and `(CronTrigger) context.getTrigger()`
(`MonitorActivityJob.java:27`, `TriggerExpireCasesOrStartWorkItemsJob.java:36`) stay, `CronTrigger` being an interface now.

**Step 7 (BeanShell 2.1.1, user's decision 2026-11-26)** — the class keeps its name and package
(`com.hoodox.operon.helper.bsf.BsfExpressionEvaluator`, used by `WorkflowCoreEngineImpl.java:38, 251, 288`, which do not
change) and its error codes (`Const.java:25-26`).

**Step 7, `operon/pom.xml:16-18, 41-55`** — *Before:*

```xml
		<bsf.version>2.3.0</bsf.version>
		<bsh.version>2.0b1</bsh.version>
		<bsh-bsf.version>1.2b7</bsh-bsf.version>
...
		<dependency><groupId>bsf</groupId><artifactId>bsf</artifactId><version>${bsf.version}</version></dependency>
		<dependency><groupId>bsh</groupId><artifactId>bsh</artifactId><version>${bsh.version}</version></dependency>
		<dependency><groupId>bsh</groupId><artifactId>bsh-bsf</artifactId><version>${bsh-bsf.version}</version></dependency>
```

(the three dependencies are written over several lines in the file). *After* — `bsf.version` and `bsh-bsf.version` and the
`bsf:bsf` and `bsh:bsh-bsf` dependencies are deleted; one dependency remains:

```xml
		<bsh.version>2.1.1</bsh.version>
...
		<dependency>
			<groupId>io.github.beanshell</groupId>
			<artifactId>bsh</artifactId>
			<version>${bsh.version}</version>
		</dependency>
```

**Step 7, `BsfExpressionEvaluator.java:6-7, 17, 24-31, 46, 62, 85, 108-112`** — *Before:*

```java
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
...
	private BSFManager bsf;
...
		if (!BSFManager.isLanguageRegistered("beanshell")) {
			BSFManager.registerScriptingEngine("beanshell", "bsh.util.BeanShellBSFEngine",null);
		}
		bsf = new BSFManager();
		this.attributes = attributes;
		_declare(this.attributes, bsf);
...
			Object result = bsf.eval("beanshell", "Operon", 0, 0, expression);
...
		} catch (BSFException e) {
...
	private static void  _declare(Map attributes, BSFManager bsf) {
...
	private static void _declare(String id, Object value, BSFManager bsf) throws SetAttributeException {
		try {
			bsf.declareBean(id, value, value.getClass());
		} catch (BSFException e) {
```

*After* (the `isLanguageRegistered`/`registerScriptingEngine` block at `:24-27` is deleted, as there is no BSF registry; the
result mapping at `:47-60`, both error codes and both message prefixes stay exactly as they are; the Javadoc's "BSF engine"
wording at `:79, 103` may say "BeanShell"):

```java
import bsh.EvalError;
import bsh.Interpreter;
...
	private Interpreter interpreter;
...
		interpreter = new Interpreter();
		this.attributes = attributes;
		_declare(this.attributes, interpreter);
...
			Object result = interpreter.eval(expression);
...
		} catch (EvalError e) {
...
	private static void  _declare(Map attributes, Interpreter interpreter) {
...
	private static void _declare(String id, Object value, Interpreter interpreter) throws SetAttributeException {
		try {
			value.getClass(); // today's call, kept: a null attribute throws NullPointerException, as today
			interpreter.set(id, value);
		} catch (EvalError e) {
```

`interpreter.eval` returns the unwrapped Java value (`Integer`, `Boolean`, ...; `null` for an unset name), as the BSF engine
did. With the `value.getClass()` call kept, a `null`-valued attribute behaves as today. What is different on 2.1.1, and
accepted by the user: D1 (`==` on equal strings), D2 (methods of non-public JDK classes) and D3 (the BeanShell part of the
error text inside `EvaluationException`), see *Decisions*. Nothing is added to undo them.

**Step 7, `operon/src/test/java/com/hoodox/operon/helper/bsf/BsfExpressionEvaluatorCharacterizationTest.java`** (written in
step 1) — only the three accepted-difference methods change; their today's value stays as a comment. *Before* (example, D1):

```java
	@Test
	public void d1StringEqualsOperator() {
		assertEquals(eval("status == \"APPROVED\"", "status", "APPROVED"), 0); // today (bsh 2.0b1 via BSF 2.3.0)
	}
```

*After:*

```java
	@Test
	public void d1StringEqualsOperator() {
		// accepted difference D1 (user, 2026-11-26): today 0 (bsh 2.0b1 via BSF 2.3.0); BeanShell 2.1.1 gives 1
		assertEquals(eval("status == \"APPROVED\"", "status", "APPROVED"), 1);
	}
```

D2 changes the same way (from `@Test(expectedExceptions = EvaluationException.class)`, which TestNG 5.8 in step 1 supports, to
a plain `@Test` with `assertEquals(..., 7)`), D3 from today's
message assertion to 2.1.1's (see step 1 for both texts). `eval(...)` is a private helper in the test that builds the
attribute map and calls `new BsfExpressionEvaluator(attrs).eval(expr)`.

**Release note** (step 7 adds it to `README.md` as a new `## Release notes` section, below today's content):

```markdown
## Release notes

### Guard expressions now run on BeanShell 2.1.1
Guard expressions on out-arcs now run on BeanShell `io.github.beanshell:bsh` 2.1.1 instead of BSF 2.3.0 with BeanShell 2.0b1
(which falls in the range of CVE-2016-2510). The `bsf:bsf` and `bsh:bsh-bsf` jars are no longer dependencies. Guards that are a
bare Boolean attribute name, as in every net shipped with Operon, behave as before. Three things differ:
1. `==` between equal strings is now true: `status == "APPROVED"` with a String attribute `status` = "APPROVED" used to be
   false (arc not taken) and is now true (arc taken). Check any guard that compares strings with `==`.
2. A method on a JDK object whose class is not public now works, e.g. `Collections.unmodifiableMap(m).get("k")`; it used to fail
   with an `EvaluationException`.
3. The text of an `EvaluationException` after "Error in the expression <expr> : " is now BeanShell 2.1.1's own message (e.g.
   "In file: inline evaluation of: ..." instead of "BeanShell script error: Parse error ..."). The exception type, error code
   and prefix are unchanged.
Unchanged: a guard attribute whose value is `null` still fails with a `NullPointerException`; guard expressions can still run any
Java, so net files must come from a trusted source.
```

**Step 8, `operon/src/test/resources/META-INF/applicationContext.xml:2, 15-28`** — *Before:*

```xml
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
...
	<bean id="tranactionProxyAware" abstract="true">
		<property name="transactionManager" ref="transactionManager" />
		<property name="transactionAttributeSource">
			<bean
				class="org.springframework.transaction.annotation.AnnotationTransactionAttributeSource" />
		</property>
	</bean>
	
	<bean id="auditTarget" singleton="true" class="com.hoodox.operon.workflow.AuditImpl"/>
	<bean id="audit"
        class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean"
        parent="tranactionProxyAware">
        <property name="target"><ref local="auditTarget"/></property>
    </bean>
```

*After* — syntax only; the same beans, classes, names and proxies. The `TransactionProxyFactoryBean` beans **and the
unproxied `auditTarget` / `workflowCoreEngineTarget` beans stay**: the code looks up `workflowCoreEngineTarget` by name and casts
it to `WorkflowCoreEngineImpl` (`WorkflowEngineImpl.java:59`, `MonitorActivityJob.java:26`,
`TriggerExpireCasesOrStartWorkItemsJob.java:35`, via `Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngineTarget`), calling it
outside the transaction proxy. `<tx:annotation-driven/>` would proxy every `@Transactional` bean, including that one, so the cast
would fail and those calls would become transactional; it is therefore not used.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd">
	<bean id="tranactionProxyAware" abstract="true">
		<property name="transactionManager" ref="transactionManager" />
		<property name="transactionAttributeSource">
			<bean class="org.springframework.transaction.annotation.AnnotationTransactionAttributeSource" />
		</property>
	</bean>
	<bean id="auditTarget" class="com.hoodox.operon.workflow.AuditImpl"/>
	<bean id="audit" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean" parent="tranactionProxyAware">
		<property name="target" ref="auditTarget"/>
	</bean>
```

(on Spring 2.5.6 the schema location is the versioned `spring-beans-2.5.xsd`; step 9 switches it to the unversioned form
above). `workflowCoreEngineTarget` / `workflowCoreEngine` (`:31-43`) change the same way; `singleton="true"` (`:23, 31, 45, 60`)
is dropped (singleton is the default); in `operon-dataAccessContext-local.xml` every `<ref local="x"/>` becomes
`<ref bean="x"/>`. The `PropertyPlaceholderConfigurer` (`:17-24`) becomes
`<context:property-placeholder location="classpath:/META-INF/mail.properties,classpath:/META-INF/jdbc.properties" local-override="true"/>`
— `local-override="true"` keeps today's order (the files win; a system property is only a fallback), which the default of the
new placeholder (environment first) would reverse. Step 9 first checks that `TransactionProxyFactoryBean` and
`AnnotationTransactionAttributeSource` are in `spring-tx` 7.1.0 (`unzip -l`); if either is gone, stop and update this plan.

**Step 9, `operon-dataAccessContext-local.xml`** (as left by Plan 3) — *Before:*

```xml
	<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
		<property name="driverClassName"><value>${jdbc.driverClassName}</value></property>
...
	<bean id="operonSqlMapClient" class="org.springframework.orm.ibatis.SqlMapClientFactoryBean">
		<property name="configLocation"><ref bean="operonIbatisSqlMapConfig"/></property>
...
	<bean id="caseSequenceDao" class="org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer"
```

*After:*

```xml
	<bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
		<property name="driverClassName" value="${jdbc.driverClassName}"/>
		<property name="jdbcUrl" value="${jdbc.url}"/>
		<property name="username" value="${jdbc.username}"/>
		<property name="password" value="${jdbc.password}"/>
	</bean>
	<bean id="operonSqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<property name="dataSource" ref="dataSource"/>
		<property name="configLocation" value="classpath:com/hoodox/operon/persistence/mybatis/mybatis-config.xml"/>
		<property name="mapperLocations" value="classpath:com/hoodox/operon/persistence/mybatis/CaseMapper.xml"/>
	</bean>
	<bean id="caseSequenceDao" class="org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer"
```

**Step 9, `Case.xml` → `CaseMapper.xml`** — *Before* (`Case.xml:1, 8, 42-45`):

```xml
<!DOCTYPE sqlMap PUBLIC "-//iBATIS.com//DTD SQL Map 2.0//EN" "http://www.ibatis.com/dtd/sql-map-2.dtd">
<sqlMap namespace="Case">
	<select id="getCaseById" resultMap="CaseVoResult" parameterClass="java.lang.Long">
		<include refid="selectCase_fragment"/>
		where operon_case.CASE_ID = #value#
	</select>
```

*After:*

```xml
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="Case">
	<select id="getCaseById" resultMap="CaseVoResult" parameterType="java.lang.Long">
		<include refid="selectCase_fragment"/>
		where operon_case.CASE_ID = #{value}
	</select>
```

Mechanical rules for all 29 statements (18 select, 8 insert, 3 update), 7 result maps, 3 fragments: `#name#` → `#{name}`;
`parameterClass` → `parameterType`; `resultClass` → `resultType`; `<resultMap class=` → `type=`; the 7 `<typeAlias>` move to
`mybatis-config.xml` `<typeAliases>`; dotted result properties (`caseVo.caseId`, `tokenVo.tokenStatus`) are kept (MyBatis 3
supports them). There is no dynamic SQL, lazy loading or nested select to translate.

`mybatis-config.xml` settings that keep iBATIS 2's behaviour (MyBatis defaults differ): `autoMappingBehavior` `NONE` (iBATIS
fills only the properties a result map lists; MyBatis would also fill same-named unmapped ones), `localCacheScope` `STATEMENT`
(iBATIS has no per-session cache; MyBatis would return the same object for a repeated select in one transaction),
`callSettersOnNulls` `true` (iBATIS calls the setter with `null` for a NULL column), `jdbcTypeForNull` `OTHER`. The DAO
characterization test is the judge: any field that differs is fixed in configuration to today's value, never accepted.

**Step 9, `CaseSqlMapDao.java:25, 39`** — *Before:*

```java
public class CaseSqlMapDao extends SqlMapClientDaoSupport implements CaseDao {
...
		return (CaseVo) getSqlMapClientTemplate().queryForObject("getCaseById", caseId);
```

*After* (class moves to `persistence/mybatis/CaseMyBatisDao.java`):

```java
public class CaseMyBatisDao extends SqlSessionDaoSupport implements CaseDao {
...
		return getSqlSession().selectOne("Case.getCaseById", caseId);
```

`queryForList` → `selectList`, `insert`/`update` keep their names and return the row count, so the lock-version checks (`0 == ...update(...)`,
e.g. `:70`) are unchanged.

**Step 10, `OperonWopedMergeMojo.java:49-50`** — *Before:* `* @goal generate` / `* @phase generate-sources` in Javadoc. *After:*
`@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)` on the class and `@Parameter` on the 5 fields tagged
`@parameter` (`:64, 72, 80, 88, 96`, keeping each `expression`/`default-value` as `defaultValue`); `FileUtils.deleteDirectory(dir)`
(`:188`) → a private `deleteRecursively(Path)` using `Files.walk` in reverse order.

## Scope

- **In:** every version in the table; the code and XML changes they force; the Maven wrapper; enforcer rules; characterization
  tests; moving the plugin's tests to the standard folders.
- **Out:** behaviour changes (each step keeps behaviour, proven by the characterization tests), apart from the three accepted
  BeanShell 2.1.1 differences D1-D3 in step 7; any adapter or wrapper that tries to undo D1-D3; renaming public Operon classes or
  packages other than the persistence package; Spring Boot; Maven 4 (not released); a CI pipeline; `operon/documents/`.

## Implementation

**Folders touched** (this repo has no `.claude/rules/`, so no scoped rule applies to any of them):
- repo root (`pom.xml`, `.mvn/`, `mvnw`, `mvnw.cmd`, `README.md`) → no scoped rule
- `parent-pom/` → no scoped rule
- `hoodox-commons/` → no scoped rule
- `opnmlJAXB/` (`pom.xml`, `xsd/*.xjb`) → no scoped rule
- `operon/` (`pom.xml`, `src/main/java/com/hoodox/operon/**`, `src/main/resources/` (new), `src/test/**`) → no scoped rule
- `maven-operon-woped-merge-plugin/` → no scoped rule

**Flow:** none — dependency and build upgrades that keep every runtime path as it is (proven by the characterization tests); the
Spring XML keeps the same beans, names and `TransactionProxyFactoryBean` proxies, only in the schema syntax.

Every step ends with `./mvnw -B clean install` and `scripts/operon-db.sh reset && ./mvnw -B -Pdb-tests -pl operon verify`
both giving exactly the results before the step (Plan 1's baseline failures stay the same failures; every characterization test
passes; in step 7 the only changed assertions are the three accepted-difference methods), then one commit; if a step changes any result, stop, record it in `## Status` and update this plan before going on.

0. Tools
   - install: `sudo apt update && sudo apt install openjdk-25-jdk`; `sudo update-alternatives --config java` and `--config javac`
     to 25; `export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64` (README notes it); `java -version` shows `25.0.5`
   - new: Maven wrapper — `mvn -N org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper -Dmaven=3.10.2` creates `mvnw`,
     `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`; from now on only `./mvnw`
   - edit: `parent-pom/pom.xml` — `pluginManagement` with every plugin version from the table; `maven-enforcer-plugin` 3.7.0
     `requireJavaVersion [25,)` and `requireMavenVersion [3.10.2,)`
   - check: the Plan 1 build (still `source/target 1.8`) is green on JDK 25; if javac 25 refuses 8, go straight to step 2
1. Characterization tests, on today's libraries (they must pass *before* any upgrade; later steps may only change their imports)
   - new: `operon/src/test/java/com/hoodox/operon/persistence/CaseDaoCharacterizationTest.java` (in `testng-db.xml`) — every one of
     the 33 `CaseDao` methods against the Docker database: write, read back, compare every field of the value object; nulls
     (`parentCaseId`, `lockByTaskId`, `triggerTime`, `actualCompletionDate`); booleans (`startAtStartup`, `successInd`); a stale
     lock version raises `DaoLockVersionException`; each startup/cron finder returns exactly the seeded rows it should and not the
     others; ids come from the sequences; `findTokenPlaceRefVosByCaseId` is pinned to today's result, a Spring
     `DataAccessException` for the missing column `operon_token_place_PLACE_REF` (`Case.xml:230`, left as is by Plan 3, finding 4)
     — never "fixed" to return rows
   - new: `operon/src/test/java/com/hoodox/operon/quartz/QuartzSchedulingCharacterizationTest.java` — on a RAM-store scheduler,
     drive each scheduling path of `WorkflowCoreEngineImpl` (finish activity `:1294`, start work item `:1342`, expire case
     `:1741`, implicit cron `:1801`, retry monitor `:2416`, delete `:1874`) through its public or package entry point (reflection
     only where a path is private; say which in the test) and assert job names, groups, job classes, `JobDataMap` keys, trigger type,
     cron expression and start time; listed in `testng.xml`
   - new: `operon/src/test/java/com/hoodox/operon/helper/jaxb/JaxbRoundTripCharacterizationTest.java` — for every `.pnml`/`.xml` net
     and the registry under `src/test/resources/META-INF/`: unmarshal, marshal, compare with the original as normalised DOM
     (ignoring whitespace and attribute order); plus a check that the generated class list in `opnmlJAXB/target/generated-sources`
     has the same 98 class names; listed in `testng.xml`; add `WopedJaxbMergeHelperTest` to `testng.xml`
   - new: `operon/src/test/java/com/hoodox/operon/helper/bsf/BsfExpressionEvaluatorCharacterizationTest.java` — calls
     `new BsfExpressionEvaluator(attrs).eval(expr)` only (no BSF or BeanShell type in the test, so its imports survive step 7);
     add `<class name="com.hoodox.operon.helper.bsf.BsfExpressionEvaluatorCharacterizationTest"/>` to
     `operon/src/test/resources/testng.xml` (otherwise surefire never runs it). Must cover, with today's results (all pass on
     today's BSF 2.3.0 + `bsh` 2.0b1 + `bsh-bsf` 1.2b7 before step 7):
     - every guard name in the repo's nets (`success`, `failure`, `failed`, `cancelled`, `updated`, `complete`, `incomplete`,
       `in_stock`, `out_of_stock`): `Boolean.TRUE` → 1, `Boolean.FALSE` → 0, name not in the attributes → 0; empty attributes → 0;
       `success`/`failure` both set as `CheckCreditCardAction` does
     - `Integer` attribute `amount`=150: `amount > 100` → 1, `amount <= 100` → 0, `amount * 2 - 1` → 299; `Long` 5000000000 → 705032704
       (`intValue()` wraps); `Double` 2.7 → 2; `flag && amount > 100` → 1, `!flag` → 0, `flag ? 1 : 0` → 1; `null` → 0;
       empty string → 0; `x = amount + 1; x > 150` → 1; `status.equals("APPROVED")` → 1; `order.get("total") > 40` on a `HashMap` → 1
     - `EvaluationException` for: a `String` result (`status`), a syntax error (`amount >`, `amount > 100 and flag`), an unset name
       used in arithmetic (`nosuchvar > 1`), `amount / 0`, an unknown method (`status.lenght()`)
     - one method per accepted difference (see *Decisions → Accepted differences*), each asserting today's result now, with a
       comment naming the difference and the 2.1.1 result step 7 will switch it to:
       - `d1StringEqualsOperator` — `status == "APPROVED"`, `status` = `"APPROVED"`: today 0 (step 7: 1)
       - `d2NonPublicJdkClassMethod` — `Collections.unmodifiableMap(m).get("k")`, `m` = `HashMap` {`"k"` → `Integer` 7}: today
         `EvaluationException` (step 7: 7)
       - `d3ErrorMessageText` — `amount > 100 and flag`: today an `EvaluationException` whose message starts with
         `Error in the expression amount > 100 and flag : BeanShell script error: Parse error` (step 7: still an
         `EvaluationException`, message starts with `Error in the expression amount > 100 and flag : ` and contains
         `inline evaluation of` and `Encountered "and"`)
     - `nullAttributeStillThrows` — a `null`-valued attribute (`attrs.put("flag", null)`): `new BsfExpressionEvaluator(attrs)`
       throws `NullPointerException`, today and after step 7 (Operon's own `value.getClass()` call, not a BeanShell difference)
     - no later step changes any assertion except step 7's switch of `d1`, `d2` and `d3` to the 2.1.1 results above
2. Java 25 and test tooling
   - edit: `parent-pom/pom.xml` — compiler 3.16.0 `release 25` (see Approach); surefire 3.6.1
   - edit: `opnmlJAXB/pom.xml` — delete its compiler 2.5.1 pin
   - edit: `operon/pom.xml`, `hoodox-commons/pom.xml` — TestNG 7.12.0, no `jdk15` classifier; remove JUnit 3.8.1; `h2` 2.5.252
   - delete: `operon/src/test/java/com/hoodox/operon/AppTest.java`
   - edit: `operon/src/test/java/com/hoodox/operon/wfnet/CaseTypeManagerTest.java` — remove the JUnit `suite()` method and its import
   - record in `## Status` whether Spring 2.5.6, iBATIS 2.3.4, Quartz 1.8.6, `jaxb-impl` 2.3.9 and BeanShell 2.0b1 run on Java 25
     bytecode (BSF 2.3.0 + BeanShell 2.0b1, still in use until step 7, already ran the expression cases on a Java 25 runtime in
     the 2026-11-26 review, so a failure there points at the build, not the library). If old Spring cannot, keep `release` at the highest level it runs at (try 21, then 17) until step 9 and say so
3. Logging
   - edit: `operon/pom.xml` — remove `log4j` and `slf4j-log4j12`; `slf4j-api` 2.0.20 compile; `logback-classic` 1.6.8 test;
     `jcl-over-slf4j` 2.0.20 test (Spring 2.5 and iBATIS log through commons-logging until step 9)
   - replace: `operon/src/test/resources/log4j.properties` → `logback-test.xml` (same levels: root WARN, `com.hoodox.operon` DEBUG,
     `com.ibatis` DEBUG, same pattern); same for `maven-operon-woped-merge-plugin/src/main/test/resources/log4j.properties`
   - delete: `operon/src/test/resources/commons-logging.properties`
4. Small libraries
   - edit: `operon/pom.xml` — `commons-lang` → `org.apache.commons:commons-lang3` 3.22.0; remove `commons-beanutils`,
     `commons-collections`, `commons-dbutils`, `commons-digester`, `xercesImpl`, `activation` (no import of any of them in any
     `.java` file and no class of theirs in any XML file; re-check with `grep` before deleting)
   - edit: `org.apache.commons.lang.StringUtils` → `org.apache.commons.lang3.StringUtils` in `TimeDuration.java`,
     `OperonJaxbElementCreator.java`, `OpnmlJAXBHelper.java`, `CaseTypeManager.java`, `WorkflowCoreEngineImpl.java`
5. JAXB to Jakarta (guarded by `JaxbRoundTripCharacterizationTest`)
   - edit: `opnmlJAXB/pom.xml` — `jakarta.xml.bind-api` 4.0.5; `jaxb2-maven-plugin` 4.3.0 with its `xjc` goal, one execution each
     for `pnml-operon.xsd` + `operonml_binding.xjb` (package `com.hoodox.operon.jaxb.opnml`) and `OperonRegistry_v-1.0.xsd` +
     `OperonRegistry_binding.xjb` (package `com.hoodox.operon.jaxb.config`) via `<sources>`/`<xjbSources>`; delete the
     commented-out `maven-jaxb2-plugin` and woped blocks (`:37-82`, `:115-137`)
   - edit: both `.xjb` files — Jakarta namespace, `version="3.0"`
   - edit: `operon/pom.xml` — replace `com.sun.xml.bind:jaxb-impl` with `org.glassfish.jaxb:jaxb-runtime` 4.0.9 (runtime)
   - edit: `javax.xml.bind` → `jakarta.xml.bind` in the 5 files listed under *Target versions*
   - delete: `.mvn/jvm.config` (the `--add-opens` was only for the old xjc); keep `.mvn/wrapper/`
6. Quartz 2.5.2 (guarded by `QuartzSchedulingCharacterizationTest`)
   - edit: `operon/pom.xml` — `quartz` 2.5.2
   - edit: `WorkflowCoreEngineImpl.java` (the 6 places in Approach), `StartWorkItemJob.java`, `FinishActivityJob.java`,
     `ExpireCaseJob.java`, `MonitorActivityJob.java`, `TriggerExpireCasesOrStartWorkItemsJob.java`; `Operon.java:127`
     (`StdSchedulerFactory.getDefaultScheduler()`) is unchanged
   - check: `operon/src/test/resources/quartz.properties` keys are still valid for Quartz 2 (`RAMJobStore`, `SimpleThreadPool`)
7. Expressions: BeanShell 2.1.1 (user's decision 2026-11-26; guarded by `BsfExpressionEvaluatorCharacterizationTest`)
   - edit: `operon/pom.xml` — delete `bsf.version`, `bsh-bsf.version` and the `bsf:bsf` and `bsh:bsh-bsf` dependencies; the `bsh`
     dependency becomes `io.github.beanshell:bsh` with `bsh.version` 2.1.1 (see Approach)
   - edit: `operon/src/main/java/com/hoodox/operon/helper/bsf/BsfExpressionEvaluator.java` — `bsh.Interpreter` instead of
     `BSFManager`, keeping the `value.getClass()` call, the result mapping, the error codes and message prefixes (see Approach);
     class name and package unchanged; nothing added to undo D1-D3
   - edit: `BsfExpressionEvaluatorCharacterizationTest.java` — switch only `d1StringEqualsOperator`, `d2NonPublicJdkClassMethod`
     and `d3ErrorMessageText` to the 2.1.1 results written in step 1, keeping today's value as a comment (see Approach); every
     other method stays exactly as written in step 1 and must pass unchanged
   - edit: `README.md` — add the `## Release notes` section in Approach
   - check: `grep -rn "org.apache.bsf\|BSFManager\|BSFException" --include=*.java operon/src` prints nothing
   - if 2.1.1 gives any result other than the ones recorded in step 1 and in D1-D3 (for example D2 not giving 7), stop: record it
     in `## Status`, update this plan and ask the user, as that difference was not accepted
8. Spring XML to the schema form, still on Spring 2.5.6
   - edit: `operon/src/test/resources/META-INF/applicationContext.xml`, `operon-dataAccessContext-local.xml` (see Approach)
9. Spring 7 + MyBatis 3 + HikariCP, one step (guarded by `CaseDaoCharacterizationTest`; the 5 `WorkflowEngineTest` tests must
   give the same result as before the step)
   - edit: `operon/pom.xml` — Spring 7.1.0 modules (`spring-context`, `-beans`, `-core`, `-aop`, `-tx`, `-jdbc`; `-test` test);
     remove `spring`, `spring-orm`, `spring-context-support`, `ibatis-sqlmap`, `commons-dbcp`, `commons-pool`, `jcl-over-slf4j`; add
     `mybatis` 3.6.0, `mybatis-spring` 4.1.0, `HikariCP` 7.1.0
   - move + convert: `operon/src/main/java/com/hoodox/operon/persistence/ibatis/sqlmap/Case.xml` →
     `operon/src/main/resources/com/hoodox/operon/persistence/mybatis/CaseMapper.xml` (rules in Approach)
   - new: `operon/src/main/resources/com/hoodox/operon/persistence/mybatis/mybatis-config.xml` — the 7 type aliases and the four
     iBATIS-compatible settings in *Approach* (step 9); the DAO characterization test covers every field and null
   - delete: `operon/src/main/java/com/hoodox/operon/persistence/ibatis/sqlmap/sql-map-config.xml`
   - move + edit: `CaseSqlMapDao.java` → `operon/src/main/java/com/hoodox/operon/persistence/mybatis/CaseMyBatisDao.java`
     (see Approach); `persistence/iface/CaseDao.java` unchanged
   - edit: `operon-dataAccessContext-local.xml` — Hikari, `SqlSessionFactoryBean`, DAO bean class
     `com.hoodox.operon.persistence.mybatis.CaseMyBatisDao` with `sqlSessionFactory`, `PostgresSequenceMaxValueIncrementer`,
     unversioned schema locations
   - edit: `operon/pom.xml` — drop the Plan 1 `src/main/java` `**/*.xml` resource entry (no XML left there); keep `src/resources`
   - edit: `operon/src/test/resources/logback-test.xml` (from step 3) — the `com.ibatis` logger becomes `org.mybatis`, same level
     (test logging only; the final `grep` for `com.ibatis` must print nothing)
10. Maven plugin module
    - edit: `maven-operon-woped-merge-plugin/pom.xml` — `maven-plugin-api` and `maven-core` 3.10.2 `provided`,
      `maven-plugin-annotations` 3.16.0 `provided`, `maven-plugin-plugin` 3.16.0, harness 3.5.1 and JUnit Jupiter 6.1.3 test;
      remove `plexus-utils`, the unused `slf4j-log4j12.version` and `testng.version` properties
    - edit: `OperonWopedMergeMojo.java` (see Approach)
    - move: `src/main/test/java/**` → `src/test/java/**`, `src/main/test/resources/**` → `src/test/resources/**`; port
      `BasicOptionsTest` to the harness's JUnit Jupiter support (`@MojoTest`/`@InjectMojo`); if 3.5.1 does not provide it, keep
      `AbstractMojoTestCase` and record that in `## Status`
11. Clean-up and final check
    - edit: `parent-pom/pom.xml` — remove the `wagon-ssh` extension (`:62-68`), `distributionManagement` (`:71-83`) and the
      `ciManagement` continuum block (`:11-20`) (line numbers of today's file; Plan 1's added compiler `<version>` line moves the
      first two down by one)
    - run: `./mvnw -B versions:display-dependency-updates versions:display-plugin-updates` and update anything newer than this
      table that is stable, with a dated note
    - edit: every `pom.xml` — set the project version of all modules from `1.0.1` to `2.0.0` (including each `<parent>`
      version) and of `hoodox-commons` from `1.0.2` to `2.0.0`, with every property or dependency that refers to these
      versions (find them with `grep -rnE '1\.0\.(1|2)|hoodox-commons\.version|operon\.version' --include=pom.xml .`; leave
      unrelated third-party versions alone). Check: `./mvnw -B clean install` passes with today's expected test results, and
      no Operon module or `hoodox-commons` is still at an old version
    - edit: `README.md` — "Building": OpenJDK 25 (`openjdk-25-jdk`), `./mvnw -B clean install`, `-Pdb-tests` with the Docker database

## Verification

After each step (exact numbers grow as characterization tests land; no characterization test may fail or be skipped; the
original tests give the baseline of Plan 1 → *Test results today*):

```bash
./mvnw -B clean install
scripts/operon-db.sh reset && ./mvnw -B -Pdb-tests -pl operon verify
```

At the end:

```bash
java -version          # openjdk version "25.0.5..."
./mvnw -v              # Apache Maven 3.10.2, Java version: 25...
./mvnw -B clean install 2>&1 | grep -E "BUILD|Tests run:|WARNING.*deprecat"
javap -v -cp operon/target/classes com.hoodox.operon.workflow.Operon | grep "major version"   # 69 (Java 25)
grep -rnE "javax\.xml\.bind|com\.ibatis|org\.apache\.log4j|org\.apache\.commons\.lang\.|org\.quartz\.(SimpleTrigger|CronTrigger)\(|springframework\.orm\.ibatis|org\.apache\.bsf|BSFManager|BSFException|BeanShellBSFEngine" \
  --include=*.java --include=*.xml --exclude-dir=documents --exclude-dir=target .
./mvnw -B dependency:tree | grep -E "ibatis|log4j:log4j|commons-dbcp|commons-lang:|jaxb-impl|testng.*jdk15|springframework:spring[a-z-]*:jar:2\.|bsf:bsf|bsh:bsh|bsh-bsf"
./mvnw -B -pl operon dependency:tree | grep -E "bsf|bsh"   # exactly one line: io.github.beanshell:bsh:jar:2.1.1:compile
grep -c "BsfExpressionEvaluatorCharacterizationTest" operon/src/test/resources/testng.xml   # 1
grep -cE "JaxbRoundTripCharacterizationTest|QuartzSchedulingCharacterizationTest|WopedJaxbMergeHelperTest" operon/src/test/resources/testng.xml   # 3
./mvnw -B -pl operon test -Dtest=BsfExpressionEvaluatorCharacterizationTest
grep -n "## Release notes" README.md
./mvnw -B versions:display-dependency-updates versions:display-plugin-updates | grep -- "->"
```

Expected: `BUILD SUCCESS` for every module on both commands; class file major version 69; the two `grep`/`dependency:tree`
checks print nothing (no BSF type in any source, no `bsf:bsf`, old `bsh:bsh` or `bsh:bsh-bsf` jar); the `bsf|bsh` line is
exactly `io.github.beanshell:bsh:jar:2.1.1:compile`; `testng.xml` lists the expression test once and the other three classes;
the expression characterization test runs and passes with 0 failures and 0 skipped, with `d1`, `d2`, `d3` asserting the 2.1.1
results (1; 7; the 2.1.1 message) and every other method, `nullAttributeStillThrows` included, unchanged since step 1;
`README.md` has the release note naming D1-D3; a `bsh` 3.x line in the versions report is not acted on (see *Decisions*,
revisit only when 3.0.0 final exists, with the user's say-so); the versions report shows no other stable update newer than the
table (or each one is recorded in `## Status`);
`ls .mvn` shows only `wrapper`; the plugin jar has `META-INF/maven/plugin.xml` generated from annotations (no "Deprecated extractor
java-javadoc" warning) and `BasicOptionsTest` ran.

Acceptance: all characterization tests written in step 1 pass unchanged (imports aside) at the end, except the three
accepted-difference methods `d1`-`d3`, which assert the 2.1.1 results the user accepted; the 14 original TestNG
tests give the same results as Plan 1's / Plan 3's baseline (or, if the user's answer to Plan 1's Open question 1 changes that
baseline, the new one); `WopedJaxbMergeHelperTest` passes, as today.

## Risks & rollback

- **Thin tests.** Before this plan only 14 TestNG tests exercise the engine; characterization tests reduce the risk but cannot
  prove every host application's net behaves the same. Each risky step is guarded by its own characterization test and is one commit.
- **Step 9 is large** (Spring 2.5 → 7, iBATIS → MyBatis, dbcp → Hikari together, forced by the library constraints in *Decisions*).
  If it cannot be made green, revert that one commit; steps 0-8 stand on their own.
- **Quartz 2 defaults** (misfire handling, thread pool start) could change when timed steps fire. Both versions default to the
  "smart" misfire policy; any default found to differ (characterization test, or reading the Quartz 2.5.2 source for a trigger
  type) is set explicitly back to Quartz 1.8.6's value in the same step, never accepted. The test pins names and times, not
  timing under load.
- **Breaking change for consumers:** Java 25 bytecode, `jakarta.xml.bind` annotations on the generated classes, Spring 7, MyBatis
  instead of iBATIS (anyone extending `CaseSqlMapDao` breaks). See *Open questions*.
- **BeanShell 2.1.1 changes three behaviours for hosts' own nets (accepted by the user, 2026-11-26).** A host guard that
  compares strings with `==` now takes the arc where it did not (D1); a guard calling a method of a non-public JDK class now
  works instead of failing (D2); code that reads the `EvaluationException` message text sees different wording (D3). The repo's
  own nets are unaffected. Mitigation: the release note in `README.md` names all three; the characterization test pins them, so
  any further difference fails the build and stops step 7 for the user. In exchange, the 2.0b1 jar inside the CVE-2016-2510
  range (a deserialization issue) and the BSF bridge jars leave the classpath.
- **Hosts that used BSF directly** through Operon's classpath (`bsf:bsf`, `bsh:bsh-bsf`) lose those jars; part of the breaking
  change below.
- **Guard expressions can run any Java** (BeanShell has no sandbox here, as today); net files must come from a trusted source.
  Unchanged by this plan.
- Rollback: `git revert` per step; no data or deployed system is involved.

## Open questions

1. **Expression library — RESOLVED 2026-11-26: option (b), BeanShell 2.1.1** (the user: "lets move to 2.1.1"). The options
   were (a) keep today's BSF 2.3.0 + BeanShell 2.0b1 (exact behaviour, old jars, CVE-2016-2510 range) or (b) move to 2.1.1
   and accept its differences as a deliberate exception to the "no behaviour change" rule. The user was told it fixes
   CVE-2016-2510 and that some conditions behave differently (`status == "APPROVED"` on a `String`: false today, true on
   2.1.1), with the repo's own nets unaffected. The exception covers only BeanShell 2.1.1's own differences D1-D3 (see
   *Decisions*); no adapter undoes them, and the null-attribute `NullPointerException` (Operon's own code) is kept. Step 7
   now does the move.
2. **Version number — RESOLVED 2026-11-26: 2.0.0** (the user agreed). These changes break every consumer (Java 25 minimum,
   Jakarta JAXB, Spring 7, new DAO class), so all modules move from `1.0.1` to `2.0.0`, and `hoodox-commons` from `1.0.2` to
   `2.0.0`, in step 11.
3. **Database safety net.** Step 9's end-to-end guard (`WorkflowEngineTest`) cannot run past loading the sample net today
   (Plan 1, Open question 1). **Answered 2026-11-26** by Plan 1's re-run on the original toolchain: that is today's behaviour on
   MySQL too, so `CaseDaoCharacterizationTest` is the database guard and `WorkflowEngineTest` must keep giving the same 5
   failures.

## Dependencies

### Blocks this plan (must be done first)
- [Rebuild hoodox-commons and make a clean checkout build green](261125-2110-rebuild-hoodox-commons-module.md) — PLANNED;
  **blocked** (the JDK 21 baseline every step starts from)
- [Convert Operon from MySQL to PostgreSQL](261125-2112-convert-operon-from-mysql-to-postgres.md) — PLANNED;
  **blocked** (step 9 converts the Postgres-ready SQL map)
- [A PostgreSQL database in Docker](261125-2111-docker-postgres-database-for-dev-and-test.md) — PLANNED; reached through Plan 3
  (the database characterization tests run against it)

### Blocked by this plan (cannot start until this is done)
None.

## Status

- 2026-11-25 (operon): PLANNED — written by the owning team. Versions checked 2026-11-25 (Maven Central metadata, Adoptium API,
  Ubuntu apt, dlcdn.apache.org); target OpenJDK 25 via Ubuntu `openjdk-25-jdk`; MyBatis 3 per the user's choice; usages counted
  by `grep` over the repo. Nothing installed or changed. Awaiting the user's approval.
- 2026-11-26 (operon): PLANNED — joined the loop [Modernise Operon](261126-0754-modernise-operon-loop.md), created at the user's request.
- 2026-11-26 (operon): PLANNED / NEEDS RE-REVIEW — at the user's request, the BeanShell choice is being reviewed. The plan calls `org.apache-extras.beanshell:bsh` 2.0b6 the newest BeanShell on Maven Central, but `io.github.beanshell:bsh` has a newer 3.0.0RC3 (Maven Central metadata, checked 2026-11-26).
- 2026-11-26 (operon): PLANNED / RE-REVIEW DONE — BeanShell re-reviewed by the owning team (scope: the expression library only).
  Chosen: `io.github.beanshell:bsh` 2.1.1, the newest stable BeanShell on Maven Central (the earlier claim that 2.0b6 was the
  newest was wrong); 3.0.0RC3 not chosen because it is a release candidate whose expression semantics still change between
  candidates — revisit when 3.0.0 final is on Central. Evidence (Maven Central metadata and POMs, OSV.dev, GitHub advisories,
  NVD, upstream GitHub releases) and a test of 84 expression cases through today's BSF path and the planned `bsh.Interpreter`
  path on 2.0b1, 2.0b6, 2.1.1 and 3.0.0RC3, on OpenJDK 21.0.13 and Eclipse Temurin 25.0.5 (throwaway container, no network;
  image `eclipse-temurin:25-jre` pulled): all repo guard expressions unchanged, three differences recorded in *Decisions*.
  Updated: target table, *Decisions*, step 7 before/after, steps 1, 2 and 7, Verification, Risks. Nothing in the repo built
  or changed apart from this plan and the loop file. Still awaiting the user's approval; still in `backlog/`.
- 2026-11-26 (operon): PLANNED / NEEDS RE-REVIEW — owning-team re-review under the user's rule "modernisation keeps every feature
  behaving as today". Removed: step 7's deliberate flip of three expression behaviours (and the "flip" in steps 1 and the
  acceptance); BeanShell 2.1.1 is no longer chosen: it cannot keep `==` on strings, non-public-class methods or error texts,
  today's jars can (re-compared the 2026-11-26 harness output, Java 21 = Java 25) — Open question 1; step 7 on hold, the null
  attribute crash is kept by adapter code if 2.1.1 is chosen. Changed step 8: keep `TransactionProxyFactoryBean` and the
  `*Target` beans (the code looks up `workflowCoreEngineTarget` and casts it; `<tx:annotation-driven/>` would break that) and
  `local-override="true"` on the new placeholder. Added to step 9 the MyBatis settings that keep iBATIS behaviour. Verification
  and acceptance now compare against Plan 1's baseline instead of an all-green suite. Open questions 1 and 3 added. Stays
  `NEEDS RE-REVIEW` until Open question 1 and Plan 1's Open question 1 are answered.
- 2026-11-26 (operon): PLANNED / NEEDS RE-REVIEW — independent owning-team re-review. Open question 3 answered (Plan 1's Open
  question 1 settled by a re-run on the original toolchain: the baseline failures are today's behaviour). Every version in the
  target table re-checked on Maven Central (all exist; each is the newest non-pre-release) and `openjdk-25-jdk`
  25.0.5.1+2-1~24.04.1 is the apt candidate here; `mybatis-spring` 4.1.0's POM says Spring 7.0.8, Java 17. Fixed gaps that
  would have failed Verification or invited a behaviour change: the three non-database characterization tests are now listed
  in `testng.xml` (otherwise surefire's suite file never runs them); `CaseDaoCharacterizationTest` pins today's error from
  `findTokenPlaceRefVosByCaseId` instead of expecting rows; step 9 renames the test logger `com.ibatis` (the final `grep` would
  have found it); the `dependency:tree` pattern for Spring 2 now matches real tree lines; step 11's line numbers explained.
  Still `NEEDS RE-REVIEW` only for Open question 1 (BeanShell, step 7); Open question 2 (version number) blocks only step 11.
- 2026-11-26 (operon): PLANNED / RE-REVIEW DONE — owning-team re-review after the user answered Open question 1 with option (b):
  "lets move to 2.1.1" (told: fixes CVE-2016-2510; some conditions differ, e.g. `status == "APPROVED"` on a String false
  today, true on 2.1.1; the repo's own nets unaffected). A deliberate exception to the no-behaviour-change rule, limited to
  BeanShell 2.1.1's own differences, named D1 (`==` on strings), D2 (non-public JDK class methods), D3 (error text after the
  unchanged prefix); no adapter undoes them; everything else keeps the rule, including the null-attribute
  `NullPointerException` (Operon's own `value.getClass()` call, kept by the port). Updated: Context, target table, *Decisions*,
  step 7 before/after (drops `bsf` and `bsh-bsf`; new test before/after; README release note), Scope, steps 1, 2 and 7
  (step 7 no longer skipped; the expression test records today's results in step 1 and step 7 switches only `d1`-`d3` to
  2.1.1's), Verification (BSF greps, `dependency:tree` shows only `io.github.beanshell:bsh:jar:2.1.1`, `testng.xml`
  registration, release note), Risks, Open question 1 RESOLVED. Nothing else blocks how the plan is implemented: Open question
  2 (version number) stays open and affects only step 11. No code changed. Still in `backlog/`; not `APPROVED`.
- 2026-11-26 (operon): PLANNED — Open question 2 resolved: the user agreed to version 2.0.0. Step 11 now sets it.
- 2026-11-26 (operon): APPROVED — approved by the user ("approve the plans"). Still in `backlog/`, so nothing may be built until the loop is picked into `todos/`.
