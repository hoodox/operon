-- TODO Add Comments

SET timezone = 'UTC';

CREATE TABLE config_repository (
  APPLICATION_NAME varchar(50) NOT NULL,
  SERVER_ID varchar(50) NOT NULL,
  FILE_NAME varchar(50) NOT NULL,
  CONTENT text NOT NULL,
  UPDATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CREATED_DATE timestamp with time zone NOT NULL,
  PRIMARY KEY  (APPLICATION_NAME,SERVER_ID,FILE_NAME)
);
COMMENT ON COLUMN config_repository.application_name IS 'The application name';
COMMENT ON COLUMN config_repository.server_id IS 'The server the JVM is running in';
COMMENT ON COLUMN config_repository.file_name IS 'The filename';
COMMENT ON COLUMN config_repository."content" IS 'Stores the XML content';
COMMENT ON COLUMN config_repository.updated_date IS 'The time stamp of when this was updated';
COMMENT ON COLUMN config_repository.created_date IS 'The time stamp of when this was created';

create table operon_case_sequence (id bigint not null); 
insert into operon_case_sequence values(0);

CREATE TABLE operon_case (
  CASE_ID bigint NOT NULL,
  ROOT_CASE_ID bigint NOT NULL,
  PARENT_CASE_ID bigint NULL,
  CASE_TYPE_REF varchar(50) NOT NULL,
  ROOT_CASE_TYPE_REF varchar(50) NOT NULL,
  CASE_STATUS varchar(9) NOT NULL,
  EXPIRY_DATE timestamp with time zone NULL,
  LOCK_VERSION integer NOT NULL default '0',
  UPDATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CREATED_DATE timestamp with time zone NOT NULL,
  PRIMARY KEY  (CASE_ID),
  CONSTRAINT OPERON_CASE_OPERON_CASE_FK2 FOREIGN KEY (ROOT_CASE_ID) REFERENCES operon_case (CASE_ID),
  CONSTRAINT OPERON_CASE_OPERON_CASE_FK1 FOREIGN KEY (PARENT_CASE_ID) REFERENCES operon_case (CASE_ID)
);


CREATE TABLE operon_ttl_scheduler (
  CASE_ID bigint NOT NULL,
  SCHEDULER_REF varchar(50) NOT NULL,
  CRON_EXP varchar(80) NOT NULL,
  UPDATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CREATED_DATE timestamp with time zone NOT NULL,
  PRIMARY KEY  (CASE_ID,SCHEDULER_REF),
  CONSTRAINT TTL_SCHEDULER_OPERON_CASE_FK1 FOREIGN KEY (CASE_ID) REFERENCES operon_case (CASE_ID)
);

create table  operon_task_sequence (id bigint not null);
insert into operon_task_sequence values(0);

CREATE TABLE operon_task (
  TASK_ID bigint NOT NULL,
  CASE_ID bigint NOT NULL,
  TASK_STATUS varchar(14) NOT NULL,
  TRIGGER_TIME timestamp with time zone default NULL,
  WFNET_TRANSITION_REF varchar(50) NOT NULL,
  START_AT_STARTUP varchar(1) NOT NULL default 'N',
  IN_PROGRESS_TIMEOUT_DATE timestamp with time zone NULL,
  RETRY_COUNT smallint NOT NULL default '0',
  PRIORITY_WEIGHTING smallint NOT NULL default '0',
  EXPECTED_COMPLETION_DATE timestamp with time zone NOT NULL,
  ACTUAL_COMPLETION_DATE timestamp with time zone NULL,  
  LOCK_VERSION integer NOT NULL default '0',
  UPDATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CREATED_DATE timestamp with time zone NOT NULL,
  PRIMARY KEY  (TASK_ID),
  CONSTRAINT OPERON_TASK_OPERON_CASE_FK1 FOREIGN KEY (CASE_ID) REFERENCES operon_case (CASE_ID)
);

create table  operon_token_sequence (id bigint not null);
insert into operon_token_sequence values(0);

CREATE TABLE operon_token (
  TOKEN_ID bigint NOT NULL,
  TOKEN_STATUS varchar(9) NOT NULL,
  LOCK_BY_TASK_ID bigint default NULL,
  LOCK_VERSION integer NOT NULL default '0',
  UPDATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CREATED_DATE timestamp with time zone NOT NULL,
  PRIMARY KEY  (TOKEN_ID)
 );

CREATE TABLE operon_token_place_ref (
  TOKEN_ID bigint NOT NULL,
  CASE_ID bigint NOT NULL,
  PLACE_REF varchar(50) NOT NULL,
  PLACE_REF_TYPE varchar(8) NOT NULL,
  UPDATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CREATED_DATE timestamp with time zone NOT NULL,
  CONSTRAINT OPERON_TOKEN_PLACE_REF_OPERON_CASE_FK1 FOREIGN KEY (CASE_ID) REFERENCES operon_case (CASE_ID)
);

CREATE TABLE operon_token_enabled_task (
  TOKEN_ID bigint NOT NULL,
  TASK_ID bigint NOT NULL,
  CREATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CONSTRAINT TOKEN_ENABLED_TO_TOKEN_FK1 FOREIGN KEY (TOKEN_ID) REFERENCES operon_token (TOKEN_ID),
  CONSTRAINT TOKEN_ENABLED_TO_TASK_FK1 FOREIGN KEY (TASK_ID) REFERENCES operon_task (TASK_ID)
);



create table  operon_event_audit_sequence (id bigint not null);
insert into operon_event_audit_sequence values(0);

CREATE TABLE operon_event_audit (
  EVENT_AUDIT_ID bigint NOT NULL,
  CASE_ID bigint NOT NULL,
  TASK_ID bigint default NULL,
  EVENT varchar(12) NOT NULL,
  INITIAL_STATUS varchar(14) NOT NULL,
  FINAL_STATUS varchar(14) NOT NULL,
  SUCCESS_IND smallint NOT NULL,
  RESOURCE_ID varchar(50) NOT NULL,
  ERROR_CODE varchar(50) default NULL,
  ERROR_DETAIL varchar(500) default NULL,
  CREATED_DATE timestamp with time zone NOT NULL,
  PRIMARY KEY  (EVENT_AUDIT_ID),
  CONSTRAINT OPERON_EVENT_AUDIT_OPERON_TASK_FK1 FOREIGN KEY (TASK_ID) REFERENCES operon_task (TASK_ID),
  CONSTRAINT OPERON_EVENT_AUDIT_OPERON_CASE_FK1 FOREIGN KEY (CASE_ID) REFERENCES operon_case (CASE_ID)
);

CREATE TABLE operon_time_trigger_scheduler (
  TASK_ID bigint NOT NULL,
  SCHEDULER_REF varchar(50) NOT NULL,
  CRON_EXP varchar(80) NOT NULL,  
  UPDATED_DATE timestamp with time zone NOT NULL default CURRENT_TIMESTAMP,
  CREATED_DATE timestamp with time zone NOT NULL,
  PRIMARY KEY  (TASK_ID,SCHEDULER_REF),
  CONSTRAINT TIME_TRIGGER_SCHEDULER_OPERON_wORKITEM_FK1 FOREIGN KEY (TASK_ID) REFERENCES operon_task (TASK_ID)
);


CREATE FUNCTION update_timestamp() RETURNS trigger AS $update_timestamp$
    BEGIN
	    IF (NEW != OLD) THEN
        	NEW.UPDATED_DATE = CURRENT_TIMESTAMP;
        	RETURN NEW;
    	END IF;
    	RETURN OLD;    
    END;
$update_timestamp$ LANGUAGE plpgsql;

CREATE TRIGGER update_timestamp BEFORE UPDATE ON config_repository
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE UPDATE ON operon_case
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE UPDATE ON operon_ttl_scheduler
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE UPDATE ON operon_time_trigger_scheduler
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE UPDATE ON operon_token_place_ref
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE UPDATE ON operon_token
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
CREATE TRIGGER update_timestamp BEFORE UPDATE ON operon_task
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
	

    