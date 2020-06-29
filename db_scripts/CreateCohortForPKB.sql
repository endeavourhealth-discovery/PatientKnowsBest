CREATE DEFINER=`endeavour`@`%` PROCEDURE `createCohortForPKB`()
BEGIN

    /*  
		This procedure loops down every organisation in our DB and figures out, one org at a time which patients
        fit our criteria for PKB.  That is having a read code in our code set and being correctly
        registered and over 17.
        
        This procedure must be run to completion for it to work.  If it is stopped prematurely, then it will 
        need to process all the data again next time it runs.
        
        Should complete in roughly 30 - 60 mins
        
        Follow progress by running the below.  It generates 2 lines per org.
        select * from data_extracts_pkb.bulkProcessingTiming order by event_time asc;
		
    */

	DECLARE startTime DATETIME;
	DECLARE endTime DATETIME;
	DECLARE orgId BIGINT;
	
	
	SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
	
	-- reset the timing table...comment this out if you want to preserve the timings
	truncate table data_extracts_pkb.bulkProcessingTiming;    
    
	DROP TABLE IF EXISTS data_extracts_pkb.cohortDelta;  
	
	CREATE TEMPORARY TABLE data_extracts_pkb.cohortDelta (
	 patient_id BIGINT,
	 organization_id BIGINT
	);
	
    insert into data_extracts_pkb.bulkProcessingTiming
	select now(), null, 'Starting the Cohort process', 0;
	
	set startTime = (now()); 
	
    -- get all orgs in our DB
	DROP TEMPORARY TABLE IF EXISTS data_extracts_pkb.org_tmp;
	CREATE TEMPORARY TABLE data_extracts_pkb.org_tmp (
	 row_id      INT,
	 organization_id   BIGINT, PRIMARY KEY(row_id)
	) AS
	SELECT (@row_no := @row_no + 1) AS row_id,
		   p.organization_id
	FROM subscriber_pi_pkb.patient p, (SELECT @row_no := 0) t
	group by p.organization_id;
	
	set endTime = (now());
		
	insert into data_extracts_pkb.bulkProcessingTiming
	select now(), null, 'Cohort get all organisations', TIMESTAMPDIFF(SECOND, startTime, endTime);
	
	SET @row_id = 1;
	   
	   
		  -- process 1 org at a time 
		run_batch: WHILE EXISTS (SELECT row_id from data_extracts_pkb.org_tmp WHERE row_id = @row_id ) DO
			
			SET orgId = (SELECT organization_id FROM data_extracts_pkb.org_tmp WHERE row_id = @row_id);

			set startTime = (now()); 
    
			-- get all valid registered patients for this org
			insert ignore into data_extracts_pkb.cohortDelta
			SELECT DISTINCT 
				   p.id AS patient_id,
                   p.organization_id as organization_id
			FROM subscriber_pi_pkb.patient p 
			JOIN subscriber_pi_pkb.episode_of_care e ON e.patient_id = p.id 
			JOIN subscriber_pi_pkb.concept eocc ON eocc.dbid = e.registration_type_concept_id 
			WHERE  p.organization_id = orgId and TIMESTAMPDIFF(YEAR,p.date_of_birth,CURDATE()) > 17
			AND eocc.code = 'R' -- currently registered
			AND p.date_of_death IS NULL 
			AND e.date_registered <= now() 
			AND (e.date_registered_end > now() or e.date_registered_end IS NULL);
			
			
			set endTime = (now());
				
			insert into data_extracts_pkb.bulkProcessingTiming
			select now(), null, concat('Cohort get all valid registered patients for single org : ', orgId), TIMESTAMPDIFF(SECOND, startTime, endTime);
			
	
			SET @row_id = @row_id + 1; 

		END WHILE run_batch;
		
		ALTER TABLE data_extracts_pkb.cohortDelta
		ADD INDEX ix_cohort_patient_id (patient_id);
	
		 -- insert all patients into the subscriber_cohort table if they are not there already setting the bulk flag to 0
		insert ignore into data_extracts_pkb.subscriber_cohort
		select 2, patient_id, 0, 0 ,organization_id from cohortDelta;
		
		set endTime = (now());
			
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), null, 'Cohort Inserting all patients into the subscriber_cohort table ', TIMESTAMPDIFF(SECOND, startTime, endTime);
		
	    set startTime = (now()); 
		
        -- set needsDelete for any patients that are no longer in the cohort
		update data_extracts_pkb.subscriber_cohort sc 
		left outer join cohortDelta d on d.patient_id = sc.patientId and sc.extractId = 2
		set sc.needsDelete = 1 where d.patient_id is null;
		
		set endTime = (now());
		
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), null, 'Cohort update the cohort table for patients needing delete ', TIMESTAMPDIFF(SECOND, startTime, endTime);
	
	drop table if exists data_extracts_pkb.cohortDelta;
   
END
