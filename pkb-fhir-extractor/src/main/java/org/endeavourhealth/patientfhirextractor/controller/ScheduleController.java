package org.endeavourhealth.patientfhirextractor.controller;

import org.endeavourhealth.patientfhirextractor.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class ScheduleController {

    @Autowired
    PatientRecordController patientRecordController;
    @Autowired
    PatientService patientService;

    @RequestMapping("start")
    ResponseEntity<Object> start() {
        try {
            patientRecordController.setStop(false);
            patientRecordController.publishPatients();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Object>(
                    e.getCause(), null, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/procedure/cohort/start")
    ResponseEntity<Object> startCohortProcedure() {
        try {
            patientService.executeProcedureCohort();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getCause(), null, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/procedure/delta/start")
    ResponseEntity<Object> startDeltaProcedures() {
        try {
            patientService.executeProceduresDelta();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getCause(), null, HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping("stop")
    ResponseEntity<String> stop() {
        patientRecordController.setStop(true);
        return new ResponseEntity<>("The schedular is not running", HttpStatus.OK);
    }

}
