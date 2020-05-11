package org.endeavourhealth.patientfhirextractor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ScheduledFuture;

@Controller
class ScheduleController {

    public static final long FIXED_RATE = 5000;

    @Autowired
    PatientRecordController patientRecordController;

    @Autowired
    TaskScheduler taskScheduler;

    ScheduledFuture<?> scheduledFuture;

    @RequestMapping("start")
    ResponseEntity<Void> start() throws Exception {
        scheduledFuture = taskScheduler.scheduleAtFixedRate(patientRecordController.publishPatients(), FIXED_RATE);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("status")
    ResponseEntity<String> status() {
        boolean statusDone = scheduledFuture.isDone();
        boolean statusCancelled = scheduledFuture.isCancelled();
        if (statusDone || statusCancelled){
            return new ResponseEntity<>("The schedular is not running", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("The schedular is running", HttpStatus.OK);
        }

    }

    @RequestMapping("stop")
    ResponseEntity<Void> stop() {
        scheduledFuture.cancel(false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
