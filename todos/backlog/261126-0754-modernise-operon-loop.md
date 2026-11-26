# Loop: Modernise Operon

**Initiating plan:** none — opened over existing plans
**Owner:** operon
**Tail entries:** waived — no deployment: Operon is a library with no test or live environment (agreed by the user, 2026-11-26)
**Status:** ACTIVE

Run-list for this effort — one or many repos. **Load the `h:todo-plans`
skill before acting on this file — every time; it is the contract here.**

- Each repo works its next entry whose blockers are all `DONE`, from a driver
  already in that entry's own repo, under all the skill's gates (`APPROVED`
  before code, diagram approval, cross-repo edit rules). Never dispatch
  across repos; a genuine exception needs the user's approval three separate
  times, asked one at a time.
- **Any repo — in the loop or not — edits this file directly, even from
  another repo; no approval needed:** your own row, Status notes, a new row
  for work this effort needs (not just work found while on an entry; unsure
  → ask the user), a stale blocker status you verified (fix it before
  relying on it). Only the owner reorders or removes rows.
- **Update your entry every time something is done:** `IN PROGRESS` + dated
  note *before* starting; `DONE` + `[x]` + outcome *after* verification and
  the MR (where the repo requires one); MR raised, merged, deployed — as each
  lands. The loop never trails the work.
- `DONE` = implemented + verified + MR raised (where the repo requires one);
  it never waits on the merge. Keep the MR/Merged/In prod cells current and
  **pick up your next workable entry straight away** (none while the loop is
  parked — parking or picking moves this file and every entry not yet in
  `done/` together; see the skill's *Picking and parking*). Same-repo
  entries may share an MR whose description covers them all.
- The list is living: add new work at its dependency position. The two tail
  entries — nonprod smoke (full CRUD, every loop scenario at desktop and
  mobile width; add your scenarios when you join) and prod deployment (repo
  release order + readonly prod smoke) — always stay last, unless waived
  above. Done only when every entry, late additions included, is `DONE`.

**Closing the loop:** keep watching for new entries; whoever completes the
last one checks every entry is `DONE`, brings every `MR`/`Merged`/`In prod`
cell current (verified, not inferred), confirms every entry's plan is already
in its repo's `todos/done/` (plans are archived when they reach `DONE` —
`git mv` any straggler now, fixing links), sets **Status:** `DONE`, and
`git mv`s this file to the initiating repo's `todos/done/`.

## Run order
(Dependency order — blockers above. Work an entry only once everything it
depends on is DONE.)

| # | Plan | Repo | Status | MR | Merged | In prod | What |
|---|---|---|---|---|---|---|---|
| 1 | [rebuild-hoodox-commons-module](261125-2110-rebuild-hoodox-commons-module.md) | operon | APPROVED | not raised | no | n/a | Rebuild the missing jar as a module; build on JDK 21 with today's test results as the baseline (2 known failures, confirmed on the original toolchain). Waits on nothing |
| 2 | [docker-postgres-database](261125-2111-docker-postgres-database-for-dev-and-test.md) | operon | APPROVED | not raised | no | n/a | Postgres in Docker for dev and test. Waits on nothing |
| 3 | [convert-mysql-to-postgres](261125-2112-convert-operon-from-mysql-to-postgres.md) | operon | APPROVED | not raised | no | n/a | Move schema, SQL map, config and seed data to Postgres, matching what MySQL does. Waits on #1, #2 |
| 4 | [upgrade-java-libraries-maven](261125-2113-upgrade-java-libraries-and-maven-to-latest.md) | operon | APPROVED | not raised | no | n/a | OpenJDK 25, latest libraries and Maven, MyBatis 3, behaviour kept, except BeanShell 2.1.1's own expression differences, accepted by the user (BSF dropped). Version number still open (step 11 only). Waits on #1, #3 (#2 through #3) |

## Status
- 2026-11-26 (operon): created at the user's request over the four modernisation plans. The whole loop is in `todos/backlog/`; nothing is approved or started. The retry-timer plan is deliberately not an entry: no entry waits on it and it is not modernisation work. Tail entries are awaiting the user's decision.
- 2026-11-26 (operon): both tail entries (nonprod smoke test, prod deployment) waived with the user's agreement — no deployment: Operon is a library with no test or live environment. Each plan's own build and test checks still apply. Publishing the built library to a new Maven repository is not part of this loop.
- 2026-11-26 (operon): #4 marked PLANNED / NEEDS RE-REVIEW — its BeanShell choice is being reviewed.
- 2026-11-26 (operon): #4 back to PLANNED (re-review done) — BeanShell target is now `io.github.beanshell:bsh` 2.1.1 (newest stable; 3.0.0 is still a release candidate), tested on Java 21 and 25. No change to the run order or to any other entry.
- 2026-11-26 (operon): owning-team re-review of every entry under the user's rule "modernisation keeps every feature behaving
  exactly as the code does today; no bug fixes" (after reading the design document `operon/documents/Operon_TechnicalDesign.pdf`).
  #1: the two `WopedJAXBHelper.java` fixes removed; the 2 failing tests are now a recorded baseline (re-run in a scratch copy).
  #3: the `Case.xml:230` typo fix and two non-required changes removed; trigger and seed now follow MySQL exactly. #4: the
  BeanShell step no longer flips behaviour and is on hold for the user; Spring step keeps today's proxies. #2: reviewed, only
  dependency status tags changed. #1, #3, #4 are `PLANNED / NEEDS RE-REVIEW` until the user answers #1's Open question 1 (and #4's Open question 1).
  Not in the loop and now on hold by the same rule: the two retry-timer plans (261124-1715, 261126-0816). No row order changed.
- 2026-11-26 (operon): handover for a fresh session written at [HANDOVER-modernise-operon.md](../HANDOVER-modernise-operon.md); read it before acting on this loop.
- 2026-11-26 (operon): independent owning-team re-review of all four entries. #1's Open question 1 answered by evidence: on the
  original toolchain (Maven 3.0.5 on Java 6, 7 and 8, the poms' own library versions, MySQL 5.5 with the repo's schema and seed)
  the same 2 tests fail and the 5 database tests stop at the same cast, so the recorded baseline is today's behaviour. #1 and #3
  back to `PLANNED` (re-review done; #3 also got two Verification fixes); #2 `PLANNED` (schema checked on `postgres:19`); #4 stays
  `PLANNED / NEEDS RE-REVIEW` for its BeanShell question only (plus test-suite and grep fixes). No row order changed.
- 2026-11-26 (operon): #4 back to `PLANNED` (re-review done). The user chose BeanShell `io.github.beanshell:bsh` 2.1.1 ("lets
  move to 2.1.1"; fixes CVE-2016-2510): a deliberate exception to the no-behaviour-change rule, limited to BeanShell 2.1.1's own
  three differences (`==` on strings, methods of non-public JDK classes, error text), each pinned by a characterization test
  and named in a README release note; BSF and `bsh-bsf` are dropped; no adapters. #4's Open question 2 (version number) stays
  open and affects only its step 11. Every entry is now `PLANNED`; nothing is approved or picked. No row order changed.
- 2026-11-26 (operon): the user agreed to version 2.0.0 for #4 (its step 11). No user decision is outstanding on any entry.
- 2026-11-26 (operon): the user approved #1-#4 (APPROVED). The loop stays in `backlog/`; nothing is picked or started.
