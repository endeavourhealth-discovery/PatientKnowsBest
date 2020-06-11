package com.fhir.scheduler.service;

import com.fhir.scheduler.repo.History_repo;
import com.fhir.scheduler.entity.History;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JobsListener implements JobListener{
	@Autowired
	CheckInterrupted checkInterrupted;

	@Autowired
	History_repo repo;



	@Override
	public String getName() {
		return "globalJob";
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		System.out.println("JobsListener.jobToBeExecuted()");
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		System.out.println("JobsListener.jobExecutionVetoed()");
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		checkInterrupted.deleteIfExists(context.getJobDetail().getKey().getName());
    	History history = new History();
		history.setJob_name(context.getJobDetail().getKey().getName());

		history.setStatus(context.getJobDetail().getJobDataMap().getString("status"));
		history.setInformation(context.getJobDetail().getJobDataMap().getString("information"));
        history.setJob_start_time(context.getFireTime());
        history.setJob_complete_time(new Date((context.getFireTime().getTime()+context.getJobRunTime())));
		repo.save(history);

		System.out.println("JobsListener.jobWasExecuted()");
	}

}
