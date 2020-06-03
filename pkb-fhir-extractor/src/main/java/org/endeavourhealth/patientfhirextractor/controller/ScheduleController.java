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
    ResponseEntity<Void> start() throws Exception {
        patientRecordController.setStop(false);
        patientService.executeProcedures();
        patientRecordController.publishPatients();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("stop")
    ResponseEntity<String> stop() {
        patientRecordController.setStop(true);
        return new ResponseEntity<>("The schedular is not running", HttpStatus.OK);
    }

}
