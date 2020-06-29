CREATE DEFINER=`endeavour`@`%` PROCEDURE `extractPatientsForPKB`()
BEGIN

    DECLARE startTime DATETIME;
	DECLARE endTime DATETIME;
	SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
	
	 -- reset the timing table...comment this out if you want to preserve the timings
	truncate table data_extracts_pkb.bulkProcessingTiming;
    
    insert into data_extracts_pkb.bulkProcessingTiming
	select now(), null, 'Starting the bulk process', 0;
    
    
	set startTime = (now());
    DROP TEMPORARY TABLE IF EXISTS data_extracts_pkb.cohort_tmp;
 	CREATE TEMPORARY TABLE data_extracts_pkb.cohort_tmp (
		 row_id      INT,
		 patientId   BIGINT,
         organization_id   BIGINT, PRIMARY KEY(row_id)
		) AS
		SELECT (@row_no := @row_no + 1) AS row_id,
			   coh.patientId,coh.organization_id
		FROM data_extracts_pkb.subscriber_cohort coh, (SELECT @row_no := 0) t
		where coh.isBulked = 0
          and coh.extractId = 2
		GROUP BY coh.patientId;
     -- here group by is not needed , once reviewed we can remove it   
    set endTime = (now());
    
    insert into data_extracts_pkb.bulkProcessingTiming
	select now(), null, 'Gathering patients to bulk', TIMESTAMPDIFF(SECOND, startTime, endTime);
    
	set startTime = (now());
	
   SET @row_id = 0;
	
	  -- process 1000 rows at a time 
		run_batch: WHILE EXISTS (SELECT row_id from data_extracts_pkb.cohort_tmp WHERE row_id > @row_id AND row_id <= @row_id + 1000) DO

                     
			set startTime = (now());    
    
	
	        replace into data_extracts_pkb.pkbPatients
			-- select all patients that have never been sent
			select p.id ,p.organization_id
			from data_extracts_pkb.cohort_tmp q
			join data_extracts_pkb.subscriber_cohort coh on q.patientId = coh.patientId		
			join subscriber_pi_pkb.patient p on p.id = coh.patientId
			where coh.isBulked = 0 and coh.needsDelete = 0 and q.row_id > @row_id AND q.row_id <= @row_id + 1000;
	        
			
			replace into data_extracts_pkb.pkbDeletions
			-- select all patients that that are no longer in the cohort
			
			select p.id,2
			from data_extracts_pkb.cohort_tmp q
			join data_extracts_pkb.subscriber_cohort coh on q.patientid = coh.patientId		
			join subscriber_pi_pkb.patient p on p.id = coh.patientId
			where coh.isBulked = 0 and coh.needsDelete = 1 and q.row_id > @row_id AND q.row_id <= @row_id + 1000;
	
			set endTime = (now());
			
			insert into data_extracts_pkb.bulkProcessingTiming
			select now(), null, 'Bulk patients', TIMESTAMPDIFF(SECOND, startTime, endTime);
			
			-- set the bulk patients to 1 so this can be stopped and started at will
			
            update data_extracts_pkb.subscriber_cohort sc
            join data_extracts_pkb.cohort_tmp q on q.patientId = sc.patientId
            set sc.isBulked = 1
            where q.row_id > @row_id AND q.row_id <= @row_id + 1000;
            
            		            
            SET @row_id = @row_id + 1000; 

		END WHILE run_batch;
	    
END
