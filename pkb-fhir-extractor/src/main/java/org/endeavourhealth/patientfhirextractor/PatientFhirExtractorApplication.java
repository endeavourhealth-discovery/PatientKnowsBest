package org.endeavourhealth.patientfhirextractor;

import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.controller.PatientRecordController;
import org.endeavourhealth.patientfhirextractor.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PatientFhirExtractorApplication {

    @Autowired
    ExporterProperties exporterProperties;

    @Autowired
    PatientService patientService;

    @Autowired
    PatientRecordController patientRecordController;

    public static void main(String[] args) {
        SpringApplication.run(PatientFhirExtractorApplication.class, args);
    }

}

