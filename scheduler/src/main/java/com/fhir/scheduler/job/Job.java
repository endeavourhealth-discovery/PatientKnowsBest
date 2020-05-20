package com.fhir.scheduler.job;

import com.fhir.scheduler.entity.Available_jobs;
import com.fhir.scheduler.repo.Jobs_repo;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class Job extends QuartzJobBean implements InterruptableJob {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    Object instance;
    Class myclass;
    @Autowired
    Jobs_repo repo;

    Available_jobs jobs ;

    @Override
    public void interrupt() throws UnableToInterruptJobException {

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

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            jobs = repo.findAvailable_jobsByJob_name(jobExecutionContext.getJobDetail().getKey().getName());
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
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
