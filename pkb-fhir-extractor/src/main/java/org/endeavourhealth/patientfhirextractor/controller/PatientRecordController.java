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
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Map<Integer, List<String>> organizationQueue = new HashMap<>();

    Patient patient;
    MessageHeader messageHeader;
    private boolean stop;

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    public Boolean getStop() {
        return stop;
    }

    public void publishPatients(String queueId) throws Exception {
        logger.info("Entering publishPatients() method");
        processPatientData(queueId);
        logger.info("End of publishPatients() method");
    }

    public void processPatientData(String queueId) throws Exception {
        logger.info("Entering getUserDetails() method");
        Map<String, String> orgIdList = new HashMap<>();
        List<Integer> organizationIds = patientService.getQueueData(queueId);

            while (!getStop()) {
                for (Integer organizationId : organizationIds) {
                    Map<Long, PatientEntity> patientEntities = patientService.processPatients(organizationId);
                    Long patientOrganizationId = Long.valueOf(organizationId);
                    if (CollectionUtils.isEmpty(patientEntities)) {
                        return;
                    }

                    postOrganizationIfNeeded(patientOrganizationId);
                    patient = new Patient();
                    messageHeader = new MessageHeader();

                    String orgLocation = patientService.getLocationForResource(patientOrganizationId, AvailableResources.ORGANIZATION);
                    if (StringUtils.isEmpty(orgLocation)) {
                        logger.info("Organization location empty " + orgLocation);
                        return;
                    } else {
                        orgIdList.put(String.valueOf(patientOrganizationId), orgLocation);
                    }

                    patientService.referenceEntry(new ReferencesEntity("Start" + organizationId, "dum"));
                    try {
                        FhirContext ctx = FhirContext.forDstu3();
                        String json = null;
                        int i = 0;

                        for (Map.Entry<Long, PatientEntity> patientData : patientEntities.entrySet()) {
                            if (!getStop()) {
                                 PatientEntity patientItem = patientData.getValue();
                                if (i > 6) {
                                    break;
                                }

                                patientService.patientUpdate(orgIdList, patientItem, ctx);
                                i = i + 1;
                            } else {
                                break;
                            }
                        }
                        patientService.referenceEntry(new ReferencesEntity("End" + organizationId, "dum"));
                        logger.info("End of getUserDetails() method");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }

    @Async
    public void patientProcessing(Map<String, String> orgIdList, Map<Long, PatientEntity> patientEntities, FhirContext ctx) throws Exception {
        try {
            Thread.sleep(15 * 1000);
            System.out.println("Processing complete");
        } catch (InterruptedException ie) {
            logger.error("Error in ProcessServiceImpl.process(): {}", ie.getMessage());
        }
           /* Map.Entry<Long, PatientEntity> entry = patientEntities.entrySet().iterator().next();
            PatientEntity patientItem = entry.getValue();
            String patientOrgId = patientItem.getOrglocation();
            if (orgIdList.get(patientOrgId) == null) {
                postOrganizationIfNeeded(Long.parseLong(patientEntities.get(0).getOrglocation()));
            }

            String patientLocation = patientService.getLocationForResource(patientItem.getId(), AvailableResources.PATIENT);

            Bundle bundle = new Bundle();
            bundle.setType(Bundle.BundleType.MESSAGE);

            bundle.addEntry().setResource(messageHeader.getMessageHeader());
            org.hl7.fhir.dstu3.model.Patient patientResource = patient.getPatientResource(patientItem, patientLocation, patientService);
            boolean update = false;
            if (patientLocation == null) {
                patientLocation = patientResource.getId();
            } else {
                update = true;
            }
            bundle.addEntry().setResource(patientResource);
            String token = createOrUpdateService.getToken();

            String json = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patientResource);
            createOrUpdateService.createOrUpdatePatient(String.valueOf(patientItem.getId()), token, json, patientLocation, update, patientItem.getOrglocation());*/
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
