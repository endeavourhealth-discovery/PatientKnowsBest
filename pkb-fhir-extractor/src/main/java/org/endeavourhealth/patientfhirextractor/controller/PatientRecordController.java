package org.endeavourhealth.patientfhirextractor.controller;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.constants.AvailableResources;
import org.endeavourhealth.patientfhirextractor.data.PatientEntity;
import org.endeavourhealth.patientfhirextractor.data.ReferencesEntity;
import org.endeavourhealth.patientfhirextractor.repository.PatientRepository;
import org.endeavourhealth.patientfhirextractor.resource.MessageHeader;
import org.endeavourhealth.patientfhirextractor.resource.Patient;
import org.endeavourhealth.patientfhirextractor.service.CreateOrUpdateService;
import org.endeavourhealth.patientfhirextractor.service.PatientService;
import org.hl7.fhir.dstu3.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class PatientRecordController {
    Logger logger = LoggerFactory.getLogger(PatientRecordController.class);

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    ExporterProperties exporterProperties;

    @Autowired
    CreateOrUpdateService createOrUpdateService;

    @Autowired
    PatientService patientService;

    Patient patient;
    MessageHeader messageHeader;

    public void publishPatients() throws Exception {
        logger.info("Entering publishPatients() method");
        processPatientData();
        logger.info("End of publishPatients() method");
    }

    public void processPatientData() throws Exception {
        logger.info("Entering getUserDetails() method");
        Map<Long, String> orgIdList = new HashMap<>();
        Map<Long, PatientEntity> patientEntities = patientService.processPatients();
        Long globalOrgId = StringUtils.isNotEmpty(exporterProperties.getOrganization()) ? Long.parseLong(exporterProperties.getOrganization()) : null;
        if (CollectionUtils.isEmpty(patientEntities)) {
            return;
        }

        postOrganizationIfNeeded(globalOrgId);
        patient = new Patient();
        messageHeader = new MessageHeader();
        FileWriter file = new FileWriter(exporterProperties.getOutputFHIR());

        if (globalOrgId != null) {
            String orgLocation = patientService.getLocationForResource(globalOrgId, AvailableResources.ORGANIZATION);
            if (StringUtils.isEmpty(orgLocation)) {
                logger.info("Organization location empty " + orgLocation);
                return;
            } else {
                orgIdList.put(globalOrgId, orgLocation);
            }
        }

        patientService.referenceEntry(new ReferencesEntity("Start", "dum"));
        try {
            for (Map.Entry<Long, PatientEntity> patientData : patientEntities.entrySet()) {
                PatientEntity patientItem = patientData.getValue();
                String patientOrgId = patientItem.getOrglocation();
                if (orgIdList.get(patientOrgId) == null) {
                    postOrganizationIfNeeded(Long.parseLong(patientData.getValue().getOrglocation()));
                }

                String patientLocation = patientService.getLocationForResource(patientItem.getId(), AvailableResources.PATIENT);

                Bundle bundle = new Bundle();
                bundle.setType(Bundle.BundleType.MESSAGE);

                bundle.addEntry().setResource(messageHeader.getMessageHeader());
                org.hl7.fhir.dstu3.model.Patient patientResource = patient.getPatientResource(patientItem, patientLocation, patientService);
                bundle.addEntry().setResource(patientResource);
                CompletableFuture<String> output = createOrUpdateService.createOrUpdatePatient(patientResource);
                //TODO:  output entry to reference table
                FhirContext ctx = FhirContext.forDstu3();
                String json = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
                file.write(json);
            }
            file.flush();
            patientService.referenceEntry(new ReferencesEntity("End", "dum"));
            logger.info("End of getUserDetails() method");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            file.close();
        }
    }

    public void postOrganizationIfNeeded(Long organizationId) {
        logger.info("Entering postOrganizationIfNeeded() method");

        if (organizationId == null) return;
        boolean organizationExist = patientService.resourceExist(organizationId, AvailableResources.ORGANIZATION);

        if (!organizationExist) {
            //TODO: POST organizaton
            String url = "http://localhost:8080/fhir/STU3/Organization";
            patientService.referenceEntry(new ReferencesEntity(AvailableResources.ORGANIZATION.toString(), organizationId, url));
        }
        //TODO: Add newly organization to reference table
        logger.info("End of postOrganizationIfNeeded() method");
    }

    public void postBundle(Bundle bundle)
            throws IOException {
        FhirContext ctx = FhirContext.forDstu3();
        String json = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request =
                new HttpEntity<>(json, headers);
    }


}
