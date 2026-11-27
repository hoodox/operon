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
| 1 | [x] [rebuild-hoodox-commons-module](done/261125-2110-rebuild-hoodox-commons-module.md) | operon | DONE | [#1](https://github.com/hoodox/operon/pull/1) | yes (`a51b5c0`) | n/a | Rebuild the missing jar as a module; build on JDK 21 with today's test results as the baseline (2 known failures, confirmed on the original toolchain). Waits on nothing |
| 2 | [x] [docker-postgres-database](done/261125-2111-docker-postgres-database-for-dev-and-test.md) | operon | DONE | [#2](https://github.com/hoodox/operon/pull/2) | no | n/a | Postgres in Docker for dev and test. Waits on nothing |
| 3 | [convert-mysql-to-postgres](261125-2112-convert-operon-from-mysql-to-postgres.md) | operon | APPROVED | not raised | no | n/a | Move schema, SQL map, config and seed data to Postgres, matching what MySQL does. Waits on #1, #2 |
| 4 | [upgrade-java-libraries-maven](261125-2113-upgrade-java-libraries-and-maven-to-latest.md) | operon | APPROVED / RE-REVIEW DONE | not raised | no | n/a | OpenJDK 25, latest libraries and Maven, MyBatis 3, behaviour kept, except BeanShell 2.1.1's own expression differences, accepted by the user (BSF dropped). Version 2.0.0 at step 11. Waits on #1, #3 (#2 through #3) |

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
- 2026-11-26 (operon): handover note for a fresh session written (removed 2026-11-27, once the loop was dispatched).
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
- 2026-11-26 (operon): loop picked from `backlog/` into `todos/` (loop file and #1-#4) on the user's go-ahead, which also starts the four approved plans in dependency order. Nothing dispatched yet: the new session does it.
- 2026-11-26 (operon): #1 IN PROGRESS — dispatched to a builder subagent. Builders run one at a time because #1 and #2 both edit `README.md` in the same working tree; #2 follows #1.
- 2026-11-26 (operon): the user gave standing self-approval to implement this loop to the end (`/loop` on this file: "You have self approval to get this implmented to the end"). The driver now carries on from entry to entry without asking. It covers the four entries only; the modernisation-only rule still applies.
- 2026-11-26 (operon): the user extended that approval: "this includes commit, push, mr and merge". For each entry, once its audit passes: push, raise a pull request into `master` and merge it with a merge commit. The repo is public, so the temporary handover note is to be left out of what is pushed.
- 2026-11-26 (operon): #1 IN PROGRESS / NEEDS RE-REVIEW — built (`5fbec9d`, local); the audit found a missed build blocker (generated JAXB classes only sometimes `Serializable`) and an encoding fault in the rebuilt `ConfigurationHelper`. Re-review first, then fix and re-audit. #2-#4 wait.
- 2026-11-26 (operon): #1 re-review done (opus). Root cause of the build difference proven: the old JAXB generator (xjc 2.1.13) places the `Serializable` setting by a hash of each clone's folder path, also on Java 6-8. Fix, the `xml.xsd` local copy, and the encoding, `ClassCache` and test corrections are new step 8; back to the builder. #4 got the same guard and checks (its own re-review done, `APPROVED / RE-REVIEW DONE`); its row's stale "version number still open" text corrected (2.0.0 was agreed). No row order changed.
- 2026-11-27 (operon): #1 DONE — step 8 built; the second audit found no violations and every Verification item passes. PR [#1](https://github.com/hoodox/operon/pull/1) raised. The temporary handover note was dropped from the unpushed history before the first push (new SHAs: `51ab8a9`, `8c38c6b`). Next: #2.
- 2026-11-27 (operon): #1 merged — PR #1, merge commit `a51b5c0` on `master` (checked: `8c38c6b` is an ancestor of `origin/master`).
- 2026-11-27 (operon): #2 IN PROGRESS — dispatched to a builder subagent on branch `modernise-operon-2-postgres-docker`.
- 2026-11-27 (operon): #2 IN PROGRESS / NEEDS RE-REVIEW — built (`baa1030`, local); its health check can pass before the host can connect. Re-review first.
- 2026-11-27 (operon): #2 re-review done (opus) — the health check now connects over TCP (`-h 127.0.0.1`), so it cannot pass during first-start initialisation; built as `9979bec`.
- 2026-11-27 (operon): #2 DONE — independent audit: no code violations, every Verification item re-run and passed. Next: #3.
