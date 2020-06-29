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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
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
        logger.info("Entering processPatientData() method");
        Map<String, String> orgIdList = new HashMap<>();
        List<BigInteger> organizationIds = patientService.getQueueData(queueId);
        for (BigInteger organizationId : organizationIds) {
            Map<Long, PatientEntity> patientEntities = patientService.processPatients(organizationId.longValue());
            Long patientOrganizationId = organizationId.longValue();
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
                // String json = null;

                for (Map.Entry<Long, PatientEntity> patientData : patientEntities.entrySet()) {
                    if (!getStop()) {
                        PatientEntity patientItem = patientData.getValue();
                        patientService.patientUpdate(orgIdList, patientItem, ctx);
                    } else {
                        break;
                    }
                }
                patientService.referenceEntry(new ReferencesEntity("End" + organizationId, "dum"));
                logger.info("End of processPatientData() method");
            } catch (IOException e) {
                e.printStackTrace();
            }
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


}
