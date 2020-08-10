package org.endeavourhealth.patientfhirextractor.service;

import org.endeavourhealth.patientfhirextractor.configuration.ClientProperties;
import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.constants.AvailableResources;
import org.endeavourhealth.patientfhirextractor.data.DeleteEntity;
import org.endeavourhealth.patientfhirextractor.data.PatientEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
public class DeleteService {
    Logger logger = LoggerFactory.getLogger(DeleteService.class);

    @Autowired
    private ExporterProperties exporterProperties;

    @Autowired
    private ClientProperties clientProperties;

    @Autowired
    private ReferencesService referencesService;

    @Autowired
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;


    @Autowired
    PatientService patientService;

    public void deletePatients() {
        logger.info("Entering deletePatients() method");
        List<DeleteEntity> patientIds = patientService.getDeletePatientIds();
        for (DeleteEntity deleteEntity :  patientIds) {
            processPatientDelete(deleteEntity);
        }
        logger.info("End of publishPatients() method");
    }

    private void processPatientDelete(DeleteEntity deleteEntity){
        String orgLocation = patientService.getLocationForResource(deleteEntity.getRecord_id(), AvailableResources.PATIENT);
        if(orgLocation != null) {
                //Delete api call
            deletePatient(String.valueOf(deleteEntity.getRecord_id()), "Patient", orgLocation);

        }
    }

    private void deletePatient(String patientId, String patientResource, String location) {
        logger.info("Entering deletePatient() method");
        final String baseUrl = clientProperties.getBaseUrl() + "Patient";
        try {
           /* HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(clientProperties.getToken());
            HttpEntity entity = new HttpEntity(patientId, headers);
            RestTemplate restTemplate = new RestTemplate();
            String url = baseUrl + "/" + location;
            ResponseEntity<Object> response = restTemplate
                    .exchange(url, HttpMethod.DELETE, entity, Object.class);*/

         //   if (response.getStatusCode().equals(204)) {
             //   referencesService.saveReference(patientId, "DEL:" + patientResource, location, String.valueOf(response.getStatusCodeValue()), orgId, entityManagerFactory);
                deletePatientId(patientId);
          //  }



        } catch (Exception e) {
            logger.error("Problem in save or update" + e.getMessage());
        }

    }

    private void deletePatientId(String patientId) {
        logger.info("Entering deletePatientId() method");
        Session session = null;
        String dbReferences = exporterProperties.getDbreferences();
        try {

            String sql = "DELETE FROM "+ dbReferences+ ".pkbDeletions where record_id=:recordId AND table_id=2";
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Transaction transaction = session.beginTransaction();
            Query q = session.createSQLQuery(sql).addEntity(DeleteEntity.class);
            q.setParameter("recordId", patientId);
            q.executeUpdate();
            transaction.commit();
           logger.info("End of deletePatientId() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
        } finally {
            session.close();
        }
    }
}
