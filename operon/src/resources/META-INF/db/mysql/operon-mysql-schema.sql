CREATE TABLE `config_repository` (
  `APPLICATION_NAME` varchar(50) NOT NULL COMMENT 'The application name',
  `SERVER_ID` varchar(50) NOT NULL COMMENT 'The server the JVM is running in',
  `FILE_NAME` varchar(50) NOT NULL COMMENT 'The filename',
  `CONTENT` text NOT NULL COMMENT 'Stores the XML content',
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'The time stamp of when this was updated',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp of when this was created',
  PRIMARY KEY  (`APPLICATION_NAME`,`SERVER_ID`,`FILE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create table if not exists operon_case_sequence (id int not null) 
type=MYISAM;
insert into operon_case_sequence values(0);

CREATE TABLE `operon_case` (
  `CASE_ID` int(30) NOT NULL COMMENT 'A unique key to identify a Case',
  `ROOT_CASE_ID` int(30) NOT NULL COMMENT 'The root parent Case if this is a subnet',
  `PARENT_CASE_ID` int(30) NULL COMMENT 'The parent Case if this Case is a subnet',
  `CASE_TYPE_REF` varchar(50) NOT NULL COMMENT 'A reference to the name of the Wfnet (net id) defined in the operon configuration file. In the case of a Subnet this references the <Page id> tag',
  `ROOT_CASE_TYPE_REF` varchar(50) NOT NULL COMMENT 'A reference to the name of the Wfnet (net id) of the root case type defined in the operon configuration file. In the case of a Subnet this references the <Page id> tag',
  `CASE_STATUS` varchar(9) NOT NULL COMMENT 'The possible options are: OPEN, CLOSED, SUSPENDED, EXPIRED and ERRORED. See Case Statuses for details',
  `EXPIRY_DATE` datetime NULL COMMENT 'The date when this case expires, this is computed from the current sys date + the time to live in the XML config',
  `LOCK_VERSION` int(20) NOT NULL default '0' COMMENT 'Used for optimistic locking',
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'The time stamp when this case was updated',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp when this case was created',
  PRIMARY KEY  (`CASE_ID`),
  KEY `OPERON_CASE_OPERON_CASE_FK1` (`PARENT_CASE_ID`),
  KEY `OPERON_CASE_OPERON_CASE_FK2` (`ROOT_CASE_ID`),
  CONSTRAINT `OPERON_CASE_OPERON_CASE_FK2` FOREIGN KEY (`ROOT_CASE_ID`) REFERENCES `operon_case` (`CASE_ID`),
  CONSTRAINT `OPERON_CASE_OPERON_CASE_FK1` FOREIGN KEY (`PARENT_CASE_ID`) REFERENCES `operon_case` (`CASE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This maps to the domain model Case, a Case is an instance of';


CREATE TABLE `operon_ttl_scheduler` (
  `CASE_ID` int(30) NOT NULL COMMENT 'FK to the associated case',
  `SCHEDULER_REF` varchar(50) NOT NULL COMMENT 'Points to a registered scheduler within the XML config file',
  `CRON_EXP` varchar(80) NOT NULL COMMENT 'The Crontab expression. This is going to be used by the scheduler as the reference id to execute the WorkItem. E.g. If the registered scheduler with CronExp 0 0/2 * * * ? then every time it executes it will search ',
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'The time stamp when this case was updated',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp when this case was created',
  PRIMARY KEY  (`CASE_ID`,`SCHEDULER_REF`),
  KEY `TTL_SCHEDULER_OPERON_CASE_FK1` (`CASE_ID`),
  CONSTRAINT `TTL_SCHEDULER_OPERON_CASE_FK1` FOREIGN KEY (`CASE_ID`) REFERENCES `operon_case` (`CASE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Time To Live Scheduler - This is only used if the OPERON_CASE expiry date is implicitly triggered';

create table if not exists operon_task_sequence (id int not null) 
type=MYISAM;
insert into operon_task_sequence values(0);

CREATE TABLE `operon_task` (
  `TASK_ID` int(30) NOT NULL COMMENT 'A unique key to identify the WorkItem',
  `CASE_ID` int(30) NOT NULL COMMENT 'FK to OPERON_CASE',
  `TASK_STATUS` varchar(14) NOT NULL COMMENT 'Possible options are: 1. ENABLED - In Wfnet terms the Transition is enabled and is ready to be fired 2. IN_PROGRESS - in Wfnet terms the Transition has started (fired) and is now called an Activity 3. FINISHED - in Wfnet terms the Activity has finished executing 4. REDUNDANT - for an XOR Transition if another Transition is executed before this one then it will make all the other transitions in the XOR branch redundant 5. ERRORED - see WorkItem Statuses 6. SUSPENDED - see WorkItem Statuses 7. AWAIT_RETRY - see WorkItem Statuses',
  `TRIGGER_TIME` datetime default NULL COMMENT 'If the TRANSITION trigger_type = TIME then this is the date and time when the transition will trigger',
  `WFNET_TRANSITION_REF` varchar(50) NOT NULL COMMENT 'The reference to the Wfnet Transition defined in the Operon configuration file',
  `START_AT_STARTUP` varchar(1) NOT NULL default 'N' COMMENT 'Valid values are N or Y. This is  used for the Automatic and Time triggered Transitions after downtime periods such as power failures. When the application restarts all Automatic and Time Triggered ENABLED Workitems will be restarted. in the case of the Time Triggered Transitions that has passed it''s due Triggered Time and the WorkItem is still Enabled then it will be automatically started',
  `IN_PROGRESS_TIMEOUT_DATE` datetime NULL COMMENT 'This is used for Automatic and Time Triggered Transitions. If the IN_PROGRESS status expires then the Workitem will AWAIT_RETRY execution until the maximum rety is reached',
  `RETRY_COUNT` int(20) NOT NULL default '0' COMMENT 'Number of retries',
  `PRIORITY_WEIGHTING` int(20) NOT NULL default '0' COMMENT 'The priority rating for this task. The higher the number the higher the priority',
  `EXPECTED_COMPLETION_DATE` datetime NOT NULL COMMENT 'The expected completion time',
  `ACTUAL_COMPLETION_DATE` datetime NULL COMMENT 'The actual completion time',  
  `LOCK_VERSION` int(20) NOT NULL default '0' COMMENT 'used for optimistic locking',
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'The time stamp when this case was updated',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp when this case was created',
  PRIMARY KEY  (`TASK_ID`),
  KEY `OPERON_TASK_OPERON_CASE_FK1` (`CASE_ID`),
  CONSTRAINT `OPERON_TASK_OPERON_CASE_FK1` FOREIGN KEY (`CASE_ID`) REFERENCES `operon_case` (`CASE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This maps to the domain model Workitem. Note: An Acitivity i';

create table if not exists operon_token_sequence (id int not null) 
type=MYISAM;
insert into operon_token_sequence values(0);

CREATE TABLE `operon_token` (
  `TOKEN_ID` int(30) NOT NULL COMMENT 'The token PK',
  `TOKEN_STATUS` varchar(9) NOT NULL COMMENT 'Possible options are: 1. FREE - the produce token in the places is free 2. LOCKED - the Token in the place is locked by an Activity 3. CONSUMED - the Activity finishes and has consumed the Token 4. CANCELLED - token is no longer available',
  `LOCK_BY_TASK_ID` int(30) default NULL COMMENT 'the Workitem ID that has locked this Token. NULL if Token status is FREE or CANCELLED',
  `LOCK_VERSION` int(20) NOT NULL default '0' COMMENT 'Used for optimistic locking',
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'The time stamp when this case was updated',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp when this case was created',
  PRIMARY KEY  (`TOKEN_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `operon_token_place_ref` (
  `TOKEN_ID` int(30) NOT NULL COMMENT 'The token FK',
  `CASE_ID` int(30) NOT NULL COMMENT 'FK to the associated Case',
  `PLACE_REF` varchar(50) NOT NULL COMMENT 'A reference place in the Wfnet defined in the Operon configuration file',
  `PLACE_REF_TYPE` varchar(8) NOT NULL COMMENT 'There are 5 types of reference: 1. SOURCE - indicates that this is an starting place 2. SINK - indicates that this is an end place 3. INTERMED - indicates that this is just a normal place reference 4. INREF - indicates that this is the id of an IN Place of a subnet 5. OUTREF - indicates that this is the ID of an OUT Place of a subnet',
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'The time stamp when this case was updated',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp when this case was created',
   KEY `OPERON_TOKEN_PLACE_REF_OPERON_CASE_FK1` (`CASE_ID`),
  CONSTRAINT `OPERON_TOKEN_PLACE_REF_OPERON_CASE_FK1` FOREIGN KEY (`CASE_ID`) REFERENCES `operon_case` (`CASE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table is in support of subnets, with subnets a Place ca';

CREATE TABLE `operon_token_enabled_task` (
  `TOKEN_ID` int(30) NOT NULL COMMENT 'The token FK',
  `TASK_ID` int(30) NOT NULL COMMENT 'FK to the associated WorkItem',
  `CREATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP COMMENT 'The time stamp when this case was created',
   KEY `TOKEN_ENABLED_TO_TOKEN_FK1` (`TOKEN_ID`),
   KEY `TOKEN_ENABLED_TO_TASK_FK1` (`TASK_ID`),
  CONSTRAINT `TOKEN_ENABLED_TO_TOKEN_FK1` FOREIGN KEY (`TOKEN_ID`) REFERENCES `operon_token` (`TOKEN_ID`),
  CONSTRAINT `TOKEN_ENABLED_TO_TASK_FK1` FOREIGN KEY (`TASK_ID`) REFERENCES `operon_task` (`TASK_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table is in support of subnets, with subnets a Place ca';



create table if not exists operon_event_audit_sequence (id int not null) 
type=MYISAM;
insert into operon_event_audit_sequence values(0);

CREATE TABLE `operon_event_audit` (
  `EVENT_AUDIT_ID` int(30) NOT NULL COMMENT 'The event audit primary key',
  `CASE_ID` int(30) NOT NULL COMMENT 'FK to associated case',
  `TASK_ID` int(30) default NULL COMMENT 'FK to associated WorkItem. If this is null then this implies that this is a Case event audit. If not null then this implies that this is a WorkItem event audit',
  `EVENT` varchar(12) NOT NULL COMMENT 'There are two event types Case statuses and WorkItem statuses diagram for events. Case Events: 1. NEW - creating a new case 2. CLOSE - to finish a case 3. M_CANCEL - manually cancels a case 4. EXPIRE - expiring a Case because the time limit has reached 5. ERROR - putting the Case into error due to an Activity (WorkItem in execution) error 6. M_SUSPEND - user manually suspends Case 7. M_RESUME - user resumes a suspended Case WorkItem events: 1. NEW - creating new WorkItem 2. FIRE - token is fired by the WorkItem associated trigger 3. FINISH - to finish an activity (executing Workitem) 4. ERROR - puts the activity into ERROR because an error occurred while processing the activity 5. TIMEOUT - puts the Activity into AWAIT_RETRY status because it has reached it''s execution time limit 7. MAX_RETRY - puts the Activity into suspended state after the maximum retry is reached 8. M_RETRY - user manually puts the Activity to AWAIT_RETRY so that the Activity can be re-executed 9. OR_CANCEL - an implicit OR cancellation. See Transition Types for implicit OR 9. M_UNDO user can manually undo a Manually Triggered Activity. All implicit OR redundant WorkItems associated with undo Activity will also be RE-ENABLED',
  `INITIAL_STATUS` varchar(14) NOT NULL COMMENT 'The initial status',
  `FINAL_STATUS` varchar(14) NOT NULL COMMENT 'The final status',
  `SUCCESS_IND` int(1) NOT NULL COMMENT '0=failure 1=success',
  `RESOURCE_ID` varchar(50) NOT NULL COMMENT 'The resource id that triggered this event',
  `ERROR_CODE` varchar(50) default NULL COMMENT 'The error code if this event resulted in an error',
  `ERROR_DETAIL` varchar(500) default NULL COMMENT 'The error details if this event resulted in an error',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp when this case was created',
  PRIMARY KEY  (`EVENT_AUDIT_ID`),
  KEY `OPERON_EVENT_AUDIT_OPERON_CASE_FK1` (`CASE_ID`),
  KEY `OPERON_EVENT_AUDIT_OPERON_TASK_FK1` (`TASK_ID`),
  CONSTRAINT `OPERON_EVENT_AUDIT_OPERON_TASK_FK1` FOREIGN KEY (`TASK_ID`) REFERENCES `operon_task` (`TASK_ID`),
  CONSTRAINT `OPERON_EVENT_AUDIT_OPERON_CASE_FK1` FOREIGN KEY (`CASE_ID`) REFERENCES `operon_case` (`CASE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `operon_time_trigger_scheduler` (
  `TASK_ID` int(30) NOT NULL COMMENT 'FK to associated WorkItem',
  `SCHEDULER_REF` varchar(50) NOT NULL COMMENT 'Points to a registered scheduler within the XML config file',
  `CRON_EXP` varchar(80) NOT NULL COMMENT 'The Crontab expression. This is going to be used by the scheduler as the reference id to execute the WorkItem. E.g. If the registered scheduler with CronExp 0 0/2 * * * ? then every time it executes it will search ',  
  `UPDATED_DATE` timestamp NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'The time stamp when this case was updated',
  `CREATED_DATE` timestamp NOT NULL COMMENT 'The time stamp when this case was created',
  PRIMARY KEY  (`TASK_ID`,`SCHEDULER_REF`),
  KEY `TIME_TRIGGER_SCHEDULER_OPERON_wORKITEM_FK1` (`TASK_ID`),
  CONSTRAINT `TIME_TRIGGER_SCHEDULER_OPERON_wORKITEM_FK1` FOREIGN KEY (`TASK_ID`) REFERENCES `operon_task` (`TASK_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This is only used if the OPERON_TASK.TRIGGER_TIME is not';
