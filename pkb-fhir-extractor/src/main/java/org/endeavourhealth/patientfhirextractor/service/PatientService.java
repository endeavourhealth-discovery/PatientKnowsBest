package org.endeavourhealth.patientfhirextractor.service;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.constants.AvailableResources;
import org.endeavourhealth.patientfhirextractor.data.PatientEntity;
import org.endeavourhealth.patientfhirextractor.data.ReferencesEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
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
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    public Map<Long, PatientEntity> processPatients() {
        logger.info("Entering processPatients() method");
        List<String> patientIds = getPatientIds();
        Map<Long, PatientEntity> patientEntities = null;
        if (patientIds.size() > 0) {
            patientEntities = getPatientFull(patientIds);
        }
        logger.info("End of publishPatients() method");
        return patientEntities;
    }

    private List<String> getPatientIds() {
        logger.info("Entering getPatientIds() method");
        String sql;
        String dbSchema = exporterProperties.getDbschema();
        String dbReference = exporterProperties.getDbreferences();
        List<String> patientIds = null;
        Session session = null;
        try {
            if (StringUtils.isNotEmpty(exporterProperties.getOrganization())) {
                sql = "SELECT p.id FROM " + dbReference + ".pkbPatients pk join " + dbSchema + ".patient p on p.id = pk.id where p.organization_id=" + exporterProperties.getOrganization();
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

    public void executeProcedures() {
        logger.info("Entering executeProcedures() method");
        Session session = null;
        String dbReferences = exporterProperties.getDbreferences();
        try {

            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Transaction txn = session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".initialiseTablesPKB()").executeUpdate();
            txn.commit();
            txn =  session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".createCohortforPKB()").executeUpdate();
            txn.commit();
            txn =  session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".extractPatientsForPKB()").executeUpdate();
            txn.commit();
            txn = session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".extractDeletionsForPKB()").executeUpdate();
            txn.commit();
            txn = session.beginTransaction();
            session.createSQLQuery("call " + dbReferences + ".finaliseExtractForPKB()").executeUpdate();
            txn.commit();
            logger.info("End of executeProcedures() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
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
            deleteProcessedPatientId(referencesEntity.getAn_id());
        }
        logger.info("End of referenceEntry() method");
        return true;
    }

    public void deleteProcessedPatientId(Long patientId) {
        logger.info("Entering deleteProcessedPatientId() method");
        Session session = null;
        try {
            String sql = "delete from pkbpatients where id = :id";
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Query q = session.createSQLQuery(sql).addEntity(ReferencesEntity.class);
            q.setParameter("id", patientId);
            logger.info("End of deleteProcessedPatientId() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
        } finally {
            session.close();
        }
    }

}
