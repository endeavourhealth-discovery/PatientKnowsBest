package com.fhir.scheduler.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fhir.scheduler.entity.Available_jobs;
import com.fhir.scheduler.repo.History_repo;
import com.fhir.scheduler.repo.Jobs_repo;
import com.fhir.scheduler.entity.History;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;


@Service
public class JobServiceImpl implements JobService {

    @Autowired
    @Lazy
    SchedulerFactoryBean schedulerFactoryBean;


    @Autowired
    CheckInterrupted interrupted;


    @Autowired
    private ApplicationContext context;


    @Autowired
    private Jobs_repo jobs_repo;

    @Autowired
    private History_repo history_repo;


    /**
     * Schedule a job by jobName at given date.
     */
    @Override
    public boolean scheduleOneTimeJob(String jobName, Class<? extends QuartzJobBean> jobClass, Date date, String jobType) {
//		System.out.println("Request received to scheduleJob");

        String jobKey = jobName;
        String groupKey = "SampleGroup";
        String triggerKey = jobName;

        JobDetail jobDetail = JobUtil.createJob(jobClass, false, context, jobKey, groupKey, jobType);

        System.out.println("creating trigger for key :" + jobKey + " at date :" + date);
        Trigger cronTriggerBean = JobUtil.createSingleTrigger(triggerKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
        //Trigger cronTriggerBean = JobUtil.createSingleTrigger(triggerKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);

        try {

            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Date dt = scheduler.scheduleJob(jobDetail, cronTriggerBean);
            System.out.println("Job with key jobKey :" + jobKey + " and group :" + groupKey + " scheduled successfully for date :" + dt);


            jobs_repo.updateStatus(true, triggerKey);
            return true;
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while scheduling job with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Schedule a job by jobName at given date.
     */
    @Override
    public boolean scheduleCronJob(String jobName, Class<? extends QuartzJobBean> jobClass, Date date, String cronExpression, String jobType) {
        System.out.println("Request received to scheduleJob");

        String jobKey = jobName;
        String groupKey = "SampleGroup";
        String triggerKey = jobName;

        JobDetail jobDetail = JobUtil.createJob(jobClass, false, context, jobKey, groupKey, jobType);

        System.out.println("creating trigger for key :" + jobKey + " at date :" + date);
        Trigger cronTriggerBean = JobUtil.createCronTrigger(triggerKey, date, cronExpression, CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);

        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Date dt = scheduler.scheduleJob(jobDetail, cronTriggerBean);
            System.out.println("Job with key jobKey :" + jobKey + " and group :" + groupKey + " scheduled successfully for date :" + dt);
            jobs_repo.updateStatus(true, triggerKey);
            return true;
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while scheduling job with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update one time scheduled job.
     */
    @Override
    public boolean updateOneTimeJob(String jobName, Date date) {
        System.out.println("Request received for updating one time job.");

        String jobKey = jobName;

        System.out.println("Parameters received for updating one time job : jobKey :" + jobKey + ", date: " + date);
        try {
            //Trigger newTrigger = JobUtil.createSingleTrigger(jobKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
            Trigger newTrigger = JobUtil.createSingleTrigger(jobKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);

            Date dt = schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
            System.out.println("Trigger associated with jobKey :" + jobKey + " rescheduled successfully for date :" + dt);
            return true;
        } catch (Exception e) {
            System.out.println("SchedulerException while updating one time job with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update scheduled cron job.
     */
    @Override
    public boolean updateCronJob(String jobName, Date date, String cronExpression) {
        System.out.println("Request received for updating cron job.");

        String jobKey = jobName;

        System.out.println("Parameters received for updating cron job : jobKey :" + jobKey + ", date: " + date);
        try {
            //Trigger newTrigger = JobUtil.createSingleTrigger(jobKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
            Trigger newTrigger = JobUtil.createCronTrigger(jobKey, date, cronExpression, CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);

            Date dt = schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
            System.out.println("Trigger associated with jobKey :" + jobKey + " rescheduled successfully for date :" + dt);
            return true;
        } catch (Exception e) {
            System.out.println("SchedulerException while updating cron job with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove the indicated Trigger from the scheduler.
     * If the related job does not have any other triggers, and the job is not durable, then the job will also be deleted.
     */
    @Override
    public boolean unScheduleJob(String jobName) {
        System.out.println("Request received for Unscheduleding job.");

        String jobKey = jobName;

        TriggerKey tkey = new TriggerKey(jobKey);
        System.out.println("Parameters received for unscheduling job : tkey :" + jobKey);
        try {
            boolean status = schedulerFactoryBean.getScheduler().unscheduleJob(tkey);
            System.out.println("Trigger associated with jobKey :" + jobKey + " unscheduled with status :" + status + "pppppppppppppppppppppppppppppppppppppppppp");
            return status;
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while unscheduling job with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete the identified Job from the Scheduler - and any associated Triggers.
     */
    @Override
    public boolean deleteJob(String jobName) {
        System.out.println("Request received for deleting job.");

        String jobKey = jobName;
        String groupKey = "SampleGroup";

        JobKey jkey = new JobKey(jobKey, groupKey);
        System.out.println("Parameters received for deleting job : jobKey :" + jobKey);

        try {
            boolean status = schedulerFactoryBean.getScheduler().deleteJob(jkey);
            System.out.println("Job with jobKey :" + jobKey + " deleted with status :" + status);
          /*
            update fhir_Scheduler.available_jobs set status=false
            where fhir_Scheduler.available_jobs.job_name = old.job_name  ;
            */
            jobs_repo.updateStatus(false, jobKey);
            return status;
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while deleting job with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pause a job
     */
    @Override
    public boolean pauseJob(String jobName) {
        System.out.println("Request received for pausing job.");

        String jobKey = jobName;
        String groupKey = "SampleGroup";
        JobKey jkey = new JobKey(jobKey, groupKey);
        System.out.println("Parameters received for pausing job : jobKey :" + jobKey + ", groupKey :" + groupKey);

        try {
            schedulerFactoryBean.getScheduler().pauseJob(jkey);
            System.out.println("Job with jobKey :" + jobKey + " paused succesfully.");
            return true;
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while pausing job with key :" + jobName + " message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Resume paused job
     */
    @Override
    public boolean resumeJob(String jobName) {
        System.out.println("Request received for resuming job.");

        String jobKey = jobName;
        String groupKey = "SampleGroup";

        JobKey jKey = new JobKey(jobKey, groupKey);
        System.out.println("Parameters received for resuming job : jobKey :" + jobKey);
        try {
            schedulerFactoryBean.getScheduler().resumeJob(jKey);
            System.out.println("Job with jobKey :" + jobKey + " resumed succesfully.");
            return true;
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while resuming job with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Start a job now
     */
    @Override
    public boolean startJobNow(String jobName) {
        System.out.println("Request received for starting job now.");

        String jobKey = jobName;
        String groupKey = "SampleGroup";

        JobKey jKey = new JobKey(jobKey, groupKey);
        System.out.println("Parameters received for starting job now : jobKey :" + jobKey);
        try {
            schedulerFactoryBean.getScheduler().triggerJob(jKey);
            System.out.println("Job with jobKey :" + jobKey + " started now succesfully.");
            return true;
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while starting job now with key :" + jobKey + " message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if job is already running
     */
    @Override
    public boolean isJobRunning(String jobName) {
//		System.out.println("Request received to check if job is running");

        String jobKey = jobName;
        String groupKey = "SampleGroup";

//		System.out.println("Parameters received for checking job is running now : jobKey :"+jobKey);
        try {

            List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
            if (currentJobs != null) {
                for (JobExecutionContext jobCtx : currentJobs) {
                    String jobNameDB = jobCtx.getJobDetail().getKey().getName();
                    String groupNameDB = jobCtx.getJobDetail().getKey().getGroup();
                    if (jobKey.equalsIgnoreCase(jobNameDB) && groupKey.equalsIgnoreCase(groupNameDB)) {
                        return true;
                    }
                }
            }
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while checking job with key :" + jobKey + " is running. error message :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Get all jobs
     */
    @Override
    public List<Map<String, Object>> getAllJobs() {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();


                    //get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Date scheduleTime = triggers.get(0).getStartTime();
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    Date lastFiredTime = triggers.get(0).getPreviousFireTime();

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("jobName", jobName);
                    map.put("groupName", jobGroup);
                    map.put("scheduleTime", scheduleTime);
                    map.put("lastFiredTime", lastFiredTime);
                    map.put("nextFireTime", nextFireTime);

                    if (isJobRunning(jobName) && !interrupted.isJobInterrupted(jobName)) {
                        map.put("jobStatus", "RUNNING");
                    } else if (isJobRunning(jobName) && interrupted.isJobInterrupted(jobName)) {
                        map.put("jobStatus", "STOP INITIATED");
                    } else {

//                        interrupted.deleteIfExists(jobName);
                        String jobState = getJobState(jobName);
                        map.put("jobStatus", jobState);
                    }
                    map.put("Stop",checkStop(jobName));

					/*					Date currentDate = new Date();
					if (scheduleTime.compareTo(currentDate) > 0) {
						map.put("jobStatus", "scheduled");

					} else if (scheduleTime.compareTo(currentDate) < 0) {
						map.put("jobStatus", "Running");

					} else if (scheduleTime.compareTo(currentDate) == 0) {
						map.put("jobStatus", "Running");
					}*/

                    list.add(map);
//					System.out.println("Job details:");
//					System.out.println("Job Name:"+jobName + ", Group Name:"+ groupName + ", Schedule Time:"+scheduleTime);
                }

            }
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while fetching all jobs. error message :" + e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            System.out.println(e);
        }
        return list;
    }

    private boolean checkStop(String jobName) {

        Available_jobs job = jobs_repo.getOne(jobName);

        if(job.getStop_method()==null && job.getStop_url() == null){
            System.out.println(job.getStop_method());
            System.out.println(job.getStop_method());
            return false;
        }

    return true;
    }

    /**
     * Check job exist with given name
     */
    @Override
    public boolean isJobWithNamePresent(String jobName) {
        try {
            String groupKey = "SampleGroup";
            JobKey jobKey = new JobKey(jobName, groupKey);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            if (scheduler.checkExists(jobKey)) {
                return true;
            }
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while checking job with name and group exist:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the current state of job
     */
    public String getJobState(String jobName) {
//		System.out.println("JobServiceImpl.getJobState()");

        try {
            String groupKey = "SampleGroup";
            JobKey jobKey = new JobKey(jobName, groupKey);

            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) {
                    TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());

                    if (TriggerState.PAUSED.equals(triggerState)) {
                        return "PAUSED";
                    } else if (TriggerState.BLOCKED.equals(triggerState)) {
                        return "BLOCKED";
                    } else if (TriggerState.COMPLETE.equals(triggerState)) {
                        return "COMPLETE";
                    } else if (TriggerState.ERROR.equals(triggerState)) {
                        return "ERROR";
                    } else if (TriggerState.NONE.equals(triggerState)) {
                        return "NONE";
                    } else if (TriggerState.NORMAL.equals(triggerState)) {
                        return "SCHEDULED";
                    }
                }
            }
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while checking job with name and group exist:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Stop a job
     */
    @Override
    public boolean stopJob(String jobName) {
        System.out.println("JobServiceImpl.stopJob()");
        boolean b;
        try {
            String jobKey = jobName;
            String groupKey = "SampleGroup";

            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobKey jkey = new JobKey(jobKey, groupKey);
            b = scheduler.interrupt(jkey);
//			adds intrrupted status into map of CheckInteruptted
            interrupted.interruptedJobs.put(jobKey, b);

            return b;

        } catch (SchedulerException e) {
            System.out.println("SchedulerException while stopping job. error message :" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    /**
     * This method returns all the available jobs in mysql database that need to be scheduled
     */
    @Override
    public List<Available_jobs> getAvailableJobs() {

        return jobs_repo.findAvailable_jobsByStatusFalse();

    }

    /**
     * This method checks if any of the details present
     * like start_url , stop_url
     * or class_path and all method names
     */
    @Override
    public boolean checkJobDetailsExists(String jobName) {

        Available_jobs job = jobs_repo.findAvailable_jobsByJob_name(jobName);
        if (job != null) {
            //check for start url and start url
            if (job.getStart_url() != null ) {
                return true;
            } else if (job.getClass_path() != null && job.getStart_method() != null) {
                return true;
            } else {
//               System.out.println(job.);
                return false;
            }
        } else {
            return false;
        }
    }


    /***
     *
     * If url is present then returns HTTP
     * else
     * returns class based
     * */

    @Override
    public String getJobType(String jobName) {

        Available_jobs job = jobs_repo.findAvailable_jobsByJob_name(jobName);

        if (job.getStart_url() != null) {
            return "HTTP";
        } else {
            return "CLASS";
        }

    }


    @Override
    public boolean checkValidDate(Date date) throws ParseException {

        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        String date_ = sd.format(new Date());
        Date curr_date = sd.parse(date_);

        if (curr_date.after(date)) {
            return false;
        } else {
            return true;
        }


    }

    @Override
    public List<History> getLog() {
        return history_repo.getAll();
    }


    public boolean addHttpJob(String jobName, String startUrl, String stopUrl) {

        int exists = jobs_repo.findByJob_name(jobName);
        if (exists > 0) {
            return false;
        } else {
            Available_jobs job = new Available_jobs();
            job.setJob_name(jobName.trim().toUpperCase());
            job.setStart_url(startUrl);
            if (stopUrl == ""){
                job.setStop_url(null);
            }else{
                job.setStop_url(stopUrl);
            }

            job.setStatus(false);
            jobs_repo.save(job);
            return true;
        }


    }


    @Override
    public boolean addClassJob(String jobName, String startmethod, String stopmethod, String classPath, String parameters) {
        int exists = jobs_repo.findByJob_name(jobName);
        if (exists > 0) {
            return false;
        } else {
            Available_jobs job = new Available_jobs();
            job.setJob_name(jobName.trim().toUpperCase());
            job.setClass_path(classPath.trim());
            job.setStart_method(startmethod.trim());
            if(stopmethod == ""){
                job.setStop_method(null);
            }else{
                job.setStop_method(stopmethod.trim());
            }
            job.setParameters(parameters.trim());
            job.setStatus(false);
            jobs_repo.save(job);
            return true;
        }

    }

    @Override
    public List<Available_jobs> getConfiguredJobs() {
        return this.jobs_repo.findAll();
    }

    @Override
    public boolean deleteConfiguredJob(String jobName) {
        this.jobs_repo.delete(jobName);

        return true;
    }


    @Override
    public boolean updateHttpJob(String jobName, String startUrl, String stopUrl) {

        if (jobName != null){
            try {
                Available_jobs job = jobs_repo.getOne(jobName);
                job.setParameters(null);
                job.setStop_method(null);
                job.setStart_method(null);
                if (stopUrl == ""){
                    job.setStop_url(null);
                }else{
                    job.setStop_url(stopUrl);
                }
                job.setStart_url(startUrl.trim());
                job.setClass_path(null);
                jobs_repo.save(job);

                return true;
            }catch (EntityNotFoundException e){
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean updateClassJob(String jobName, String startmethod, String stopmethod, String classPath, String parameters) {


        if (jobName != null){
            try {
                Available_jobs job = jobs_repo.getOne(jobName);
                job.setParameters(parameters);

                if(stopmethod == ""){
                    job.setStop_method(null);
                }else{
                    job.setStop_method(stopmethod.trim());
                }
                job.setStart_method(startmethod.trim());
                job.setClass_path(classPath.trim());
                job.setStop_url(null);
                job.setStart_url(null);
                jobs_repo.save(job);
                return true;
            }catch (EntityNotFoundException e){
                return false;
            }
        }


        return false;
    }
}



