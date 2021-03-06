DROP TRIGGER IF EXISTS update_timestamp ON config_repository;
DROP TRIGGER IF EXISTS update_timestamp ON operon_case;
DROP TRIGGER IF EXISTS update_timestamp ON operon_ttl_scheduler;
DROP TRIGGER IF EXISTS update_timestamp ON operon_time_trigger_scheduler;
DROP TRIGGER IF EXISTS update_timestamp ON operon_token_place_ref;
DROP TRIGGER IF EXISTS update_timestamp ON operon_token;
DROP TRIGGER IF EXISTS update_timestamp ON operon_task;
DROP FUNCTION IF EXISTS update_timestamp();

drop table if exists config_repository;
drop table if exists operon_ttl_scheduler;
drop table if exists operon_event_audit;
drop table if exists operon_time_trigger_scheduler;
drop table if exists operon_token_enabled_task;
drop table if exists operon_token_place_ref;
drop table if exists operon_token;
drop table if exists operon_task;
drop table if exists operon_case;
drop table if exists operon_case_sequence;
drop table if exists operon_task_sequence;
drop table if exists operon_token_sequence;
drop table if exists operon_event_audit_sequence;
