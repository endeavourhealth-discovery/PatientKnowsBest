USE data_extracts_pkb;
DROP PROCEDURE IF EXISTS PKBPatientDeltaBatched;
DELIMITER //
CREATE PROCEDURE PKBPatientDeltaBatched()

BEGIN
	/*  
		This procedure loops down 10 minutes of the event log at a time since it was last run.
        
        This procedure updates after every loop of 10 minutes so it can be interuppted and only current batch
        of 10 minutes would need to be processed again when it is restarted. 
        
        Should complete a 1 days worth of deltas in roughly 5 - 10 minute so it will take roughly
        
        number_of_days * 10 minute to process
        
        eg 20 days would take ~ 200 minutes
        
        Follow progress by running the below.  It generates many lines per batch of 10 minutes but each
        line contains the current Processing Date so you can see where you are up to at all times
        
        select * from data_extracts_pkb.bulkProcessingTiming order by event_time asc;
		
    */
    
    DECLARE current_event_log_date DATETIME;
	DECLARE current_max_date DATETIME;
	DECLARE max_event_log_date DATETIME;
	DECLARE startTime DATETIME;
	DECLARE endTime DATETIME;

	SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; 
    
    -- reset the timing table...comment this out if you want to preserve the timings
	truncate table data_extracts_pkb.bulkProcessingTiming;
    
    insert into data_extracts_pkb.bulkProcessingTiming
	select now(), current_event_log_date, 'Starting the batching process', 0;
    
  
	SET current_event_log_date = (SELECT transactionDate FROM data_extracts_pkb.subscriber_extracts WHERE extractId = 2);
    
    -- as so far behind, just run 1 day at a time for now
	SET max_event_log_date = DATE_ADD(current_event_log_date, INTERVAL 1 DAY); 
    
    -- once caught up it can be run to the end every time
    -- SET max_event_log_date = select max(dt_change) from subscriber_pi_pkb.event_log;);
  	
    SET current_max_date = DATE_ADD(current_event_log_date, INTERVAL 10 MINUTE);   
    
    insert into data_extracts_pkb.bulkProcessingTiming
	select now(), current_event_log_date, 'Starting the batching process', 0;
    
	processLoop: WHILE (current_max_date <= max_event_log_date) DO

		set startTime = (now());    
        
        -- get 10 minutes worth of the event log for the tables we are interested in
		DROP TEMPORARY TABLE IF EXISTS data_extracts_pkb.evnt_tmp;
		CREATE TEMPORARY TABLE data_extracts_pkb.evnt_tmp as
		SELECT distinct record_id, table_id, change_type
						  FROM subscriber_pi_pkb.event_log e
						  WHERE e.table_id in (2)
						  AND e.dt_change > current_event_log_date AND e.dt_change <= current_max_date;
		
		ALTER TABLE data_extracts_pkb.evnt_tmp ADD INDEX evnt_idx(record_id, table_id);
		
		set endTime = (now());
		
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), current_event_log_date, 'Getting event log data', TIMESTAMPDIFF(SECOND, startTime, endTime);
						  
		-- if it is already in our filteredTable, we don't need to process it again		
		set startTime = (now());
		
		delete ev
		from data_extracts_pkb.evnt_tmp ev
		join data_extracts_pkb.pkbPatients patients on patients.id = ev.record_id and ev.table_id = 2;
		
		set endTime = (now());
		
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), current_event_log_date, 'Deleting patients from event table that are already in our list', TIMESTAMPDIFF(SECOND, startTime, endTime);
		
		set startTime = (now());
		
		-- remove false deletions for patients
		delete ev
		from data_extracts_pkb.evnt_tmp ev
		join subscriber_pi_pkb.patient o on o.id = ev.record_id and ev.table_id = 2
		where ev.change_type = 2;

		set endTime = (now());

		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), current_event_log_date, 'Removing false deletions for patients', TIMESTAMPDIFF(SECOND, startTime, endTime);

		set startTime = (now());

		-- Process the patient data for the 10 minute period
		set startTime = (now());

		DROP TEMPORARY TABLE IF EXISTS qry_tmp;
		CREATE TEMPORARY TABLE qry_tmp (
		 row_id      INT,
		 patientId   BIGINT, PRIMARY KEY(row_id)
		) AS
		SELECT (@row_no := @row_no + 1) AS row_id,
			   coh.patientId
		FROM data_extracts_pkb.subscriber_cohort coh
		join data_extracts_pkb.evnt_tmp log on log.record_id = coh.patientId and log.table_id = 2, (SELECT @row_no := 0) t
		where coh.isBulked = 1
		  and coh.extractId = 1
          and log.change_type <> 2
		GROUP BY coh.patientId;
		
		set endTime = (now());
		
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), current_event_log_date, 'Extracting patient data', TIMESTAMPDIFF(SECOND, startTime, endTime);

		SET @row_id = 0;
	   
	   
		set startTime = (now());
		  -- process 1000 rows at a time 
		run_batch: WHILE EXISTS (SELECT row_id from qry_tmp WHERE row_id > @row_id AND row_id <= @row_id + 1000) DO

					  REPLACE INTO data_extracts_pkb.pkbPatients SELECT q.patientId FROM qry_tmp q WHERE q.row_id > @row_id AND q.row_id <= @row_id + 1000;
					  SET @row_id = @row_id + 1000; 

		END WHILE run_batch;
		
		set endTime = (now());
		
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), current_event_log_date, 'Storing patient data', TIMESTAMPDIFF(SECOND, startTime, endTime);
		
		-- Process the deletion data for the 10 minute period
		set startTime = (now());
		
		DROP TEMPORARY TABLE IF EXISTS qry_tmp;
		CREATE TEMPORARY TABLE qry_tmp (
		 row_id      INT,
		 record_id   BIGINT, 
		 table_id tinyint(4),
		 PRIMARY KEY(row_id)
		) AS
		SELECT (@row_no := @row_no + 1) AS row_id,
			   log.record_id,
			   log.table_id
		FROM data_extracts_pkb.`references` r
		join data_extracts_pkb.evnt_tmp log on log.record_id = r.an_id and log.table_id = r.type_id, (SELECT @row_no := 0) t
        where log.change_type = 2
		GROUP BY log.record_id, log.table_id;
		
		set endTime = (now());
		
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), current_event_log_date, 'Extracting deletion data', TIMESTAMPDIFF(SECOND, startTime, endTime);

		SET @row_id = 0;
	   
	   
		set startTime = (now());
		  -- process 1000 rows at a time 
		run_batch: WHILE EXISTS (SELECT row_id from qry_tmp WHERE row_id > @row_id AND row_id <= @row_id + 1000) DO

					  REPLACE INTO data_extracts_pkb.pkbDeletions SELECT q.record_id, q.table_id FROM qry_tmp q WHERE q.row_id > @row_id AND q.row_id <= @row_id + 1000;
					  SET @row_id = @row_id + 1000; 

		END WHILE run_batch;
		
		set endTime = (now());
		
		insert into data_extracts_pkb.bulkProcessingTiming
		select now(), current_event_log_date, 'Storing deletion data', TIMESTAMPDIFF(SECOND, startTime, endTime);
        
		-- set the current extract date so if proc was stopped it can be started from where it left off
		update data_extracts_pkb.subscriber_extracts
		set transactionDate = current_max_date 
		where extractId = 1;

	   -- reset current event log date
	   SET current_event_log_date = current_max_date;
	   -- add 10 mins to event log date

	   IF current_max_date < max_event_log_date THEN
		  
			SET current_max_date = DATE_ADD(current_max_date, INTERVAL 10 MINUTE);

			IF current_max_date > max_event_log_date  THEN
				SET current_max_date = max_event_log_date;
			END IF;

		ELSE 
			insert into data_extracts_pkb.bulkProcessingTiming
			select now(), current_event_log_date, 'Reached end time...finishing', 0;
			
			LEAVE processLoop;

		END IF;
   
	END WHILE processLoop;
         
END//
DELIMITER ;