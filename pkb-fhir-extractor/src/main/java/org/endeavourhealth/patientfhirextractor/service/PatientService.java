package org.endeavourhealth.patientfhirextractor.service;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.constants.AvailableResources;
import org.endeavourhealth.patientfhirextractor.data.DeleteEntity;
import org.endeavourhealth.patientfhirextractor.data.PatientEntity;
import org.endeavourhealth.patientfhirextractor.data.ReferencesEntity;
import org.endeavourhealth.patientfhirextractor.resource.MessageHeader;
import org.endeavourhealth.patientfhirextractor.resource.Patient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hl7.fhir.dstu3.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {
    Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private ExporterProperties exporterProperties;

    @Autowired
    private ReferencesService referencesService;

    @Autowired
    private CreateOrUpdateService createOrUpdateService;

    Patient patient = new Patient();
    MessageHeader messageHeader = new MessageHeader();

    @Autowired
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    public Map<Long, PatientEntity> processPatients(Long organizationId) {
        logger.info("Entering processPatients() method");
        List<String> patientIds = getPatientIds(organizationId);
        Map<Long, PatientEntity> patientEntities = null;
        if (patientIds.size() > 0) {
            patientEntities = getPatientFull(patientIds);
        }
        logger.info("End of publishPatients() method");
        return patientEntities;
    }

    @Async
    public void patientUpdate(Map<String, String> orgIdList,PatientEntity patientItem, FhirContext ctx) throws Exception {
        logger.info("Entering patientUpdate() method");
        String patientOrgId = patientItem.getOrglocation();
        if (orgIdList.get(patientOrgId) == null) {
            postOrganizationIfNeeded(Long.parseLong(patientOrgId));
        }

        String patientLocation = getLocationForResource(patientItem.getId(), AvailableResources.PATIENT);

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.MESSAGE);

        bundle.addEntry().setResource(messageHeader.getMessageHeader());
        org.hl7.fhir.dstu3.model.Patient patientResource = patient.getPatientResource(patientItem, patientLocation, this);
        boolean update = false;
        if (patientLocation == null) {
            patientLocation = patientResource.getId();
        } else {
            update = true;
        }
        bundle.addEntry().setResource(patientResource);
        String token = createOrUpdateService.getToken();

        String json = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patientResource);
        createOrUpdateService.createOrUpdatePatient(String.valueOf(patientItem.getId()), token, json, patientLocation, update, patientItem.getOrglocation());
        logger.info("End of patientUpdate() method");
    }

    public void postOrganizationIfNeeded(Long organizationId) {
        logger.info("Entering postOrganizationIfNeeded() method");

        if (organizationId == null) return;
        boolean organizationExist = resourceExist(organizationId, AvailableResources.ORGANIZATION);

        if (!organizationExist) {
            //TODO: POST organizaton
            String url = "http://localhost:8080/fhir/STU3/Organization";
            referenceEntry(new ReferencesEntity(AvailableResources.ORGANIZATION.toString(), organizationId, url));
        }
        //TODO: Add newly organization to reference table
        logger.info("End of postOrganizationIfNeeded() method");
    }


    private List<String> getPatientIds(Long organizationId) {
        logger.info("Entering getPatientIds() method");
        String sql;
        String dbSchema = exporterProperties.getDbschema();
        String dbReference = exporterProperties.getDbreferences();
        List<String> patientIds = null;
        Session session = null;
        try {
            if (organizationId != 0) {
                sql = "SELECT p.id FROM " + dbReference + ".pkbPatients pk join " + dbSchema + ".patient p on p.id = pk.id where p.organization_id=" + organizationId;
            } else {
                sql = "select * from " + dbReference + ".pkbPatients";
            }
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            patientIds = session.createSQLQuery(sql).list();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("", e.getCause());
        } finally {
            session.close();
        }
        logger.info("End of getPatientIds() method");
        return patientIds;
    }

    private Map<Long, PatientEntity> getPatientFull(List patientIds) {
        logger.info("Entering getPatientFull() method");
        String dbSchema = exporterProperties.getDbschema();
        Map<Long, PatientEntity> patientMap = new HashMap<>();

        String sql = "SELECT p.id," +
                "coalesce(p.organization_id,'') as orglocation," +
                "coalesce(p.date_of_birth,'') as dob," +
                "p.date_of_death as dod," +
                "coalesce(o.ods_code,'') as code," +
                "coalesce(c.name,'') as gender," +
                "coalesce(p.nhs_number,'') as nhs_number," +
                "coalesce(p.last_name,'') as lastname," +
                "coalesce(p.first_names,'') as firstname," +
                "coalesce(p.title,'') as title," +
                "coalesce(a.address_line_1,'') as add1," +
                "coalesce(a.address_line_2,'') as add2," +
                "coalesce(a.address_line_3,'') as add3," +
                "coalesce(a.address_line_4,'') as add4," +
                "coalesce(a.city,'') as city," +
                "coalesce(a.postcode,'') as postcode," +
                "coalesce(e.date_registered,'') as startdate," +
                "'HOME' as adduse," +
                "'' as telecom," +
                "'' as otheraddresses " +
                "FROM " + dbSchema + "." + "patient p " +
                "join " + dbSchema + "." + "patient_address a on a.id = p.current_address_id " +
                "join " + dbSchema + "." + "concept c on c.dbid = p.gender_concept_id " +
                "join " + dbSchema + "." + "episode_of_care e on e.patient_id = p.id " +
                "join " + dbSchema + "." + "organization o on o.id = p.organization_id " +
                "join " + dbSchema + "." + "concept c2 on c2.dbid = e.registration_type_concept_id " +
                "where c2.code = 'R' " +
                "and p.date_of_death IS NULL " +
                "and e.date_registered <= now() " +
                "and (e.date_registered_end > now() or e.date_registered_end IS NULL) and p.id in (" + StringUtils.join(patientIds, ',') + ") ";
        List<PatientEntity> patients = null;

        Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
        patients = session.createSQLQuery(sql).addEntity(PatientEntity.class).list();

        patients.forEach(patient -> {
            patientMap.put(patient.getId(), patient);
        });
        session.close();
        logger.info("End of  getPatientFull() method");
        return patientMap;
    }

    public String getLocationForResource(Long id, AvailableResources resourceName) {
        logger.info("Entering getLocationForResource() method");
        ReferencesEntity referencesEntity = new ReferencesEntity();
        Session session = null;
        Session session2 = null;
        try {
            String sql = "SELECT * FROM " + exporterProperties.getDbreferences() + ".references WHERE an_id=:id AND resource=:resource";
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Query q = session.createSQLQuery(sql).addEntity(ReferencesEntity.class);
            q.setParameter("id", id);
            q.setParameter("resource", resourceName.toString());
            referencesEntity = (ReferencesEntity) q.getSingleResult();
            if (referencesEntity != null) {
                session2 = entityManagerFactory.unwrap(SessionFactory.class).openSession();
                Query q2 = session2.createSQLQuery(sql).addEntity(ReferencesEntity.class);
                q2.setParameter("id", id);
                q2.setParameter("resource", "DEL:" + resourceName.toString());
                q2.getSingleResult();
            }
        } catch (NoResultException e) {
            logger.info("No result for id " + id);
            //resource not deleted
            logger.info("End of getLocationForResource() method");
            return referencesEntity.getLocation();
        } finally {
            if (session != null) {
                session.close();
            }
            if (session2 != null) {
                session2.close();
            }
        }
        logger.info("End of getLocationForResource() method");
        //Location does not exist or it is deleted
        return "";
    }

    public boolean resourceExist(Long id, AvailableResources resourceName) {
        logger.info("Entering resourceExist() method");
        Session session = null;
        try {
            String sql = "SELECT * FROM " + exporterProperties.getDbreferences() + ".references WHERE an_id=:id AND resource=:resource";
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Query q = session.createSQLQuery(sql).addEntity(ReferencesEntity.class);
            q.setParameter("id", id);
            q.setParameter("resource", resourceName.toString());
            q.getSingleResult();
            logger.info("End of getLocationForResource() method");
            return true;
        } catch (NoResultException e) {
            e.printStackTrace();
            logger.error("No result for id " + id + " resource name " + resourceName);
            logger.info("End of getLocationForResource() method");
            return false;
        } finally {
            session.close();
        }
    }

    public boolean isPatientActive(Long patientId) {
        logger.info("Entering isPatientActive() method");
        Session session = null;
        try {
            String sql = "SELECT patientId FROM " + exporterProperties.getDbreferences() + ".subscriber_cohort WHERE patientId=:id and needsDelete=0";
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Query q = session.createSQLQuery(sql);
            q.setParameter("id", patientId);
            q.getSingleResult();
            logger.info("End of isPatientActive() method");
            return true;
        } catch (NoResultException e) {
            logger.info("No result for id " + patientId);
            return false;
        } finally {
           session.close();
        }
    }

    public boolean referenceEntry(ReferencesEntity referencesEntity) {
        logger.info("Entering referenceEntry() method");
        try {
            referencesService.enterReference(referencesEntity, entityManagerFactory);
        } catch (Exception e) {
            logger.error("Problem while inserting to reference table for anid " + referencesEntity.getAn_id());
            return false;
        }
        if (referencesEntity.getAn_id() > 0) {
            referencesService.deleteProcessedPatientId(referencesEntity.getAn_id(), entityManagerFactory);
        }
        logger.info("End of referenceEntry() method");
        return true;
    }

    public void executeProcedureCohort() {
        logger.info("Entering executeProcedureCohort() method");
        Session session = null;
        String dbReferences = exporterProperties.getDbreferences();
        try {
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Transaction txn = session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".createCohortforPKB()").executeUpdate();
            txn.commit();
            logger.info("End of executeProcedureCohort() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
        } finally {
            session.close();
        }
    }

    public void executeProceduresDelta() {
        logger.info("Entering executeProceduresDelta() method");
        Session session = null;
        String dbReferences = exporterProperties.getDbreferences();
        try {

            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Transaction txn = session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".extractPatientsForPKB()").executeUpdate();
            txn.commit();
            txn = session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".PKBPatientDeltaBatched()").executeUpdate();
            txn.commit();
            logger.info("End of executeProceduresDelta() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
        } finally {
            session.close();
        }
    }


    public List<BigInteger> getQueueData(String id) {
        logger.info("Entering getQueueData() big integer method");
        Session session = null;
        List<BigInteger> organization_ids = null;
        String dbReferences = exporterProperties.getDbreferences();
        try {
            String sql = "select organization_id from " + dbReferences +".pkb_org_queue where id = " + id;
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            organization_ids = session.createSQLQuery(sql).list();
            logger.info("End of getQueueData() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
        } finally {
            session.close();
        }
        return organization_ids;
    }

    public List<DeleteEntity> getDeletePatientIds() {
        logger.info("Entering getDeletePatientRow() big integer method");
        Session session = null;

        List<DeleteEntity> deleteEntityList = null;
        String dbReferences = exporterProperties.getDbreferences();
        try {
            String sql = "select * from " + dbReferences + ".pkbDeletions where table_id=2";
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Query q = session.createSQLQuery(sql).addEntity(DeleteEntity.class);
            deleteEntityList =  q.getResultList();
            logger.info("End of getDeletePatientIds() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
        } finally {
            session.close();
        }
        return deleteEntityList;
    }
}
