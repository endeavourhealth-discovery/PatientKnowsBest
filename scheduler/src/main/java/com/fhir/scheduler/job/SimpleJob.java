package com.fhir.scheduler.job;


import com.fhir.scheduler.entity.Available_jobs;
import com.fhir.scheduler.repo.Jobs_repo;
import com.fhir.scheduler.util.JobResponseCode;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.fhir.scheduler.service.JobService;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@DisallowConcurrentExecution
public class SimpleJob extends QuartzJobBean implements InterruptableJob {

    JobExecutionContext jobExecutionContext_;

    ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    String jobName;

    Object instance;
    Class myClass;

    @Autowired
    Jobs_repo repo;

    Available_jobs job;

    String jobType;

    @Autowired
    JobService jobService;

    RestTemplate temp;

    JobDataMap jobDataMap;


    Boolean stop = false;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        /**
         *
         * **/
        jobDataMap = new JobDataMap();
        jobExecutionContext_ = jobExecutionContext;
        jobType = jobExecutionContext.getJobDetail().getJobDataMap().getString("jobType");
        jobName = jobExecutionContext.getJobDetail().getKey().getName();
        job = repo.findAvailable_jobsByJob_name(jobName);

        if (jobType.equalsIgnoreCase("CLASS") || jobType == "CLASS") {
            /**Call the class dynamically
             *
             * if the method has no parameters call the class without parameters else call the class with parameters 
             *
             */
            String parameter = job.getParameters();
            if (parameter == null) {
                executeFromClass();

            } else {
                executeFromClassWithParameters(parameter);
            }


        } else if (jobType.equalsIgnoreCase("HTTP") || jobType == "HTTP") {
            /**
             * CALL THE HTTP METHOD GET URL
             *
             * */
            executeHttp();

        }

        System.out.println("Initiated Stop successful for " + Thread.currentThread().getName());
        repo.updateStatus(false, jobExecutionContext.getJobDetail().getKey().getName());
    }

    private void executeHttp() {
            temp = new RestTemplate();

            try {
                ResponseEntity<String> response = temp.getForEntity(job.getStart_url(), String.class);
                if (stop == false) {

                    System.out.println(response.getStatusCodeValue() + " from execute");

                    System.out.println(response.getBody() + " From Execute");
                    if (JobResponseCode.SUCCESS==response.getStatusCodeValue()) {
                        jobExecutionContext_.getJobDetail().getJobDataMap().put("status", JobResponseCode.SUCCESSFUL);
                        jobExecutionContext_.getJobDetail().getJobDataMap().put("information", JobResponseCode.COMPLETE);
                    }else{
                        jobExecutionContext_.getJobDetail().getJobDataMap().put("status", JobResponseCode.SUCCESSFUL);
                        jobExecutionContext_.getJobDetail().getJobDataMap().put("information", JobResponseCode.UNKNOWN_ERROR);
                    }

                }

            }catch (ResourceAccessException e){
                jobExecutionContext_.getJobDetail().getJobDataMap().put("status", JobResponseCode.FAILURE);
                jobExecutionContext_.getJobDetail().getJobDataMap().put("information", JobResponseCode.INVALID_URL);
            }catch (RestClientException e) {
                jobExecutionContext_.getJobDetail().getJobDataMap().put("status", JobResponseCode.FAILURE);
                jobExecutionContext_.getJobDetail().getJobDataMap().put("information", JobResponseCode.UNKNOWN_ERROR);

            }catch (Exception e){

                jobExecutionContext_.getJobDetail().getJobDataMap().put("status", JobResponseCode.FAILURE);
                jobExecutionContext_.getJobDetail().getJobDataMap().put("information", JobResponseCode.UNKNOWN_ERROR);
            }
    }

    private void executeFromClassWithParameters(String parameter) {

        try {


            myClass = classLoader.loadClass(job.getClass_path());
            instance = myClass.getConstructor().newInstance();

            Method method = myClass.getMethod(job.getStart_method(), new Class[]{String.class});


            method.invoke(instance, new Object[]{parameter});
            if (stop==false){
                jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.SUCCESSFUL);
                jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.COMPLETE);
            }



        } catch (ClassNotFoundException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_CLASS_PATH);
        } catch (InstantiationException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_CLASS_PATH);
        } catch (IllegalAccessException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNABLE_TO_ACCESS);
        } catch (NoSuchMethodException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_STOP_METHOD_NAME);
        } catch (InvocationTargetException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information","Unable to Invoke Stop Method");
        } catch (Exception e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNKNOWN_ERROR);
        }

    }

    private void executeFromClass() {

        try {
            job = repo.findAvailable_jobsByJob_name(jobName);
            myClass = classLoader.loadClass(job.getClass_path());
            instance = myClass.getConstructor().newInstance();

            Method method = myClass.getMethod(job.getStart_method());


            method.invoke(instance);
            if (stop==false){
                jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.SUCCESSFUL);
                jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.COMPLETE);
            }


        } catch (ClassNotFoundException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_CLASS_PATH);
        } catch (InstantiationException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_CLASS_PATH);
        } catch (IllegalAccessException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNABLE_TO_ACCESS);
        } catch (NoSuchMethodException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_STOP_METHOD_NAME);
        } catch (InvocationTargetException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information","Unable to Invoke Stop Method");
        }catch (Exception e){
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.FAILURE);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNKNOWN_ERROR);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {

//        System.out.println(jobExecutionContext_.getJobDetail().getJobDataMap().getString("jobType"));
        String name = jobExecutionContext_.getJobDetail().getJobDataMap().getString("jobType");

        if (jobType.equalsIgnoreCase("CLASS") || jobType == "CLASS") {
            this.classStop();
        } else if (jobType.equalsIgnoreCase("HTTP") || jobType == "HTTP") {

            this.httpStop();
        }


    }

    private void httpStop() {
           stop = true;
      try {
          ResponseEntity<String> response = temp.getForEntity(job.getStop_url(), String.class);

          if(response.getStatusCodeValue()== JobResponseCode.SUCCESS){
              jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPTED);
              jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INTERRUPT_SUCCESSFUL);
          }
          System.out.println(response.getBody()+" From the stop URL ");
          System.out.println(response.getStatusCodeValue()+" From the Stop URL ");

      }catch(ResourceAccessException e){
          jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPT_FAILED);
          jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_STOP_URL);
      }catch (RestClientException e){
          jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPT_FAILED);
          jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.REQUEST_STOP_ERROR);
      }catch(Exception e){
          jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPT_FAILED);
          jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNKNOWN_ERROR);
      }



    }

    private void classStop() {
        stop = true;
        try {
            Method method = myClass.getMethod(job.getStop_method());
            method.invoke(instance);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPTED);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INTERRUPT_SUCCESSFUL);
        } catch (InvocationTargetException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPT_FAILED);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNKNOWN_ERROR);
        } catch (NoSuchMethodException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPT_FAILED);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.INVALID_STOP_METHOD_NAME);
        } catch (IllegalAccessException e) {
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPT_FAILED);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNKNOWN_ERROR);

        }catch (Exception e){
            jobExecutionContext_.getJobDetail().getJobDataMap().put("status",JobResponseCode.INTERRUPT_FAILED);
            jobExecutionContext_.getJobDetail().getJobDataMap().put("information",JobResponseCode.UNKNOWN_ERROR);
        }

    }

}