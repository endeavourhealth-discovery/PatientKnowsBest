package org.endeavourhealth.patientfhirextractor;

import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.controller.PatientRecordController;
import org.endeavourhealth.patientfhirextractor.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableScheduling
public class PatientFhirExtractorApplication implements CommandLineRunner {

    @Autowired
    ExporterProperties exporterProperties;

    @Autowired
    PatientService patientService;

    @Autowired
    PatientRecordController patientRecordController;

    public static void main(String[] args) {
        SpringApplication.run(PatientFhirExtractorApplication.class, args);
    }

    @Bean
    TaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void run(String... strings) throws Exception {
        if (exporterProperties.getProcrun() > 0) {
            for(int i=0;i< exporterProperties.getProcrun();i++) {
                patientService.executeProcedures();
            }
        }
    }

}

