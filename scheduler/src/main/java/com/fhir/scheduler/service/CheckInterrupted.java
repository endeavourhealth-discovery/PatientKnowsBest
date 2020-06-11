package com.fhir.scheduler.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class CheckInterrupted {
    public Map<String,Boolean> interruptedJobs = new HashMap<>();


    public boolean isJobInterrupted(String JobName){
     Boolean b =   this.interruptedJobs.get(JobName);
     if (b!=null){
         return b;
     }
        return false ;
    }



    public void deleteIfExists(String jobName){

        if((interruptedJobs.containsKey(jobName))) {
            interruptedJobs.remove(jobName);
        }
        return;

    }
}
