USE data_extracts_pkb;
-- table to hold the progress of the procs...as they are long running you can keep an eye 
-- on the progress and how long each section took...useful for targetting any long running sql in the process
CREATE TABLE data_extracts_pkb.bulkProcessingTiming (
    event_time DATETIME,
    currentProcessingDate DATETIME,
    description VARCHAR(100),
    durationinSeconds INT
);

	CREATE TABLE IF NOT EXISTS data_extracts_pkb.subscriber_extracts (
    extractId INT NOT NULL PRIMARY KEY,
    extractName VARCHAR(100) NOT NULL,
    transactionDate DATETIME(3) NULL,
    cohortCodeSetId INT NULL,
    observationCodeSetId INT NULL
);
    
	INSERT IGNORE INTO data_extracts_pkb.subscriber_extracts
	SELECT 2, 'Patient Knows Best', DATE_SUB(NOW(), INTERVAL 50 YEAR), NULL, NULL;
    
        
CREATE TABLE IF NOT EXISTS data_extracts_pkb.subscriber_cohort (
    extractId INT NOT NULL,
    patientId BIGINT NOT NULL,
    isBulked BOOLEAN,
    needsDelete BOOLEAN,
    PRIMARY KEY pk_extract_patient (extractId , patientId),
    INDEX ix_cohort_patientId (patientId),
    INDEX ix_cohort_patientId_bulked (patientId , isBulked)
);

CREATE TABLE IF NOT EXISTS data_extracts_pkb.references (
  an_id bigint(20) DEFAULT NULL,
  strid varchar(255) DEFAULT NULL,
  resource varchar(255) DEFAULT NULL,
  organization_id bigint(20) DEFAULT NULL,
  response varchar(10) DEFAULT NULL,
  location varchar(100) DEFAULT NULL,
  datesent datetime NOT NULL,
  json text,
  patient_id bigint(20) DEFAULT NULL,
  type_id tinyint(1) DEFAULT NULL,
  runguid varchar(50) DEFAULT NULL,
  KEY ix_references_an_id (an_id),
  KEY ix_references_strid (strid),
  KEY ix_references_location (location),
  KEY ix_references_patient_id (patient_id),
  KEY ix_references_resource (resource)
);

    
CREATE TABLE IF NOT EXISTS data_extracts_pkb.pkb_org_queue (
  id int(11) DEFAULT NULL,
  organization_id int(11) DEFAULT NULL
  );
  

CREATE TABLE IF NOT EXISTS data_extracts_pkb.pkbPatients (id BIGINT(20) PRIMARY KEY , organization_id BIGINT(20));
    
CREATE TABLE IF NOT EXISTS data_extracts_pkb.pkbDeletions (
    record_id BIGINT(20) PRIMARY KEY,
    table_id TINYINT(4)
);
    
    /*  Not needed for phase 1
	create table if not exists data_extracts_pkb.snomed_code_set (
		codeSetId int not null,
		codeSetName varchar(200) not null
	);

	create table if not exists data_extracts_pkb.snomed_code_set_codes (
		codeSetId int not null,
		snomedCode bigint not null,
        
        primary key pk_codes_codeset_code (codeSetId, snomedCode)
	);
    
    create table if not exists data_extracts_pkb.pkbObservations (
		id bigint(20) primary key,
        `organization_id` bigint(20) DEFAULT NULL,
        KEY `ix_filtered_obs_organization` (`organization_id`)
    );
    
    create table if not exists data_extracts_pkb.pkbAllergies (
		id bigint(20) primary key
    );
    
    create table if not exists data_extracts_pkb.pkbMedications (
		id bigint(20) primary key
    );
    
/*