package com.fhir.scheduler.job;


import com.fhir.scheduler.entity.Available_jobs;
import com.fhir.scheduler.repo.Jobs_repo;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.fhir.scheduler.service.JobService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SimpleJob extends QuartzJobBean implements InterruptableJob {

    JobExecutionContext jobExecutionContext_;

    ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    Object instance;
    Class myclass;

    @Autowired
    Jobs_repo repo;

    Available_jobs jobs;

    String jobType;

    @Autowired
    JobService jobService;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        /**
         * **/
        jobExecutionContext_ = jobExecutionContext;
        jobType = jobExecutionContext.getJobDetail().getJobDataMap().getString("jobType");

        if (jobType.equalsIgnoreCase("CLASS") || jobType == "CLASS") {
            /**Call the class dynamically
             *
             */
            executeFromClass(jobExecutionContext.getJobDetail().getKey().getName());


        } else if (jobType.equalsIgnoreCase("HTTP") || jobType == "HTTP") {
            /**
             * CALL THE HTTP METHOD GET URL
             * */


        }


        System.out.println("Initiated Stop successful for " + Thread.currentThread().getName());


        repo.updateStatus(false, jobExecutionContext.getJobDetail().getKey().getName());
    }

    private void executeFromClass(String name) {

        try {
            jobs = repo.findAvailable_jobsByJob_name(name);
            myclass = classLoader.loadClass(jobs.getClass_path());
            instance = myclass.newInstance();

            Method method = myclass.getMethod(jobs.getStart_method());


            method.invoke(instance);


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {

        System.out.println(jobExecutionContext_.getJobDetail().getJobDataMap().getString("jobType"));
        String name = jobExecutionContext_.getJobDetail().getJobDataMap().getString("jobType");

        if (jobType.equalsIgnoreCase("CLASS") || jobType == "CLASS") {
            classStop();
        } else if (jobType.equalsIgnoreCase("HTTP") || jobType == "HTTP") {


        }


    }

    private void classStop() {
        try {
            Method method = myclass.getMethod(jobs.getStop_method());

            method.invoke(instance);


        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();

        }
    }

}