package org.endeavourhealth.patientfhirextractor.controller;

import org.endeavourhealth.patientfhirextractor.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
class ScheduleController {

    @Autowired
    PatientRecordController patientRecordController;
    @Autowired
    PatientService patientService;
    private String queueId;

    @RequestMapping("start")
    ResponseEntity<Object> start(@RequestParam("queue") String queueId) {
        try {
            if (queueId == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            patientRecordController.setStop(false);
            this.queueId = queueId;
            processPatient();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getCause(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void processPatient() throws Exception {
        patientRecordController.publishPatients(queueId);
    }

    @RequestMapping("/procedure/cohort/start")
    ResponseEntity<Object> startCohortProcedure() {
        try {
            patientService.executeProcedureCohort();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getCause(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/procedure/delta/start")
    ResponseEntity<Object> startDeltaProcedures() {
        try {
            patientService.executeProceduresDelta();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getCause(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping("stop")
    ResponseEntity<String> stop() {
        patientRecordController.setStop(true);
        return new ResponseEntity<>("The schedular is not running", HttpStatus.OK);
    }

}
