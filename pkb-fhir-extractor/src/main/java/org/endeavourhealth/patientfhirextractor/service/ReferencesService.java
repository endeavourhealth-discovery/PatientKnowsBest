package org.endeavourhealth.patientfhirextractor.service;

import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.constants.AvailableResources;
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
import java.util.Calendar;

@Service
public class ReferencesService {
    Logger logger = LoggerFactory.getLogger(ReferencesService.class);

    @Autowired
    private ExporterProperties exporterProperties;

    public boolean enterReference(ReferencesEntity referencesEntity, EntityManagerFactory entityManagerFactory) {
        logger.info("Entering enterReference() method");
        String sql = "insert into " + exporterProperties.getDbreferences() + ".references (an_id,strid,resource,response,location,datesent,json,patient_id,type_id,runguid) values (?,?,?,?,?,?,?,?,?,?)";
        Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
        Transaction transaction = session.beginTransaction();
        long timeNow = Calendar.getInstance().getTimeInMillis();
        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
        try {
            session.createNativeQuery(sql)
                    .setParameter(1, referencesEntity.getAn_id())
                    .setParameter(2, referencesEntity.getStrid())
                    .setParameter(3, referencesEntity.getResource())
                    .setParameter(4, referencesEntity.getResponse())
                    .setParameter(5, referencesEntity.getLocation())
                    .setParameter(6, ts)
                    .setParameter(7, referencesEntity.getJson())
                    .setParameter(8, referencesEntity.getPatientId())
                    .setParameter(9, 0)
                    .setParameter(10, exporterProperties.getRunguid())
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Problem while inserting to reference table for anid " + referencesEntity.getAn_id());
            logger.info("End of enterReference() method");
            return false;
        } finally {
            session.close();
        }
        logger.info("End of enterReference() method");
        return true;
    }

    public boolean saveReference(String patientId,String json,String location, String responseCode, String orgId, EntityManagerFactory entityManagerFactory) {
        logger.info("Entering saveReference() method");
        String sql = "insert into " + exporterProperties.getDbreferences() + ".references (an_id, resource,response,location,organization_id, datesent,json,patient_id,type_id,runguid) values (?,?,?,?,?,?,?,?,?,?)";
        Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
        Transaction transaction = session.beginTransaction();
        long timeNow = Calendar.getInstance().getTimeInMillis();
        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
        try {
            session.createNativeQuery(sql)
                    .setParameter(1, patientId)
                    .setParameter(2, AvailableResources.PATIENT.toString())
                    .setParameter(3, responseCode)
                    .setParameter(4, location)
                    .setParameter(5, Long.valueOf(orgId))
                    .setParameter(6, ts)
                    .setParameter(7, json)
                    .setParameter(8, Long.valueOf(patientId))
                    .setParameter(9, 2)
                    .setParameter(10, exporterProperties.getRunguid())
                    .executeUpdate();
            transaction.commit();
            deleteProcessedPatientId(Long.valueOf(patientId), entityManagerFactory);
        } catch (Exception e) {
            logger.error("Problem while inserting to reference table for anid " + patientId);
            logger.info("End of saveReference() method");
            return false;
        } finally {
            session.close();
        }
        logger.info("End of saveReference() method");
        return true;
    }

    public void deleteProcessedPatientId(Long patientId, EntityManagerFactory entityManagerFactory) {
        logger.info("Entering deleteProcessedPatientId() method :" + patientId);
        Session session = null;
        try {
            String sql = "delete from " + exporterProperties.getDbreferences() + ".pkbPatients where id = :id";
            session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
            Transaction transaction = session.beginTransaction();
            Query q = session.createSQLQuery(sql).addEntity(ReferencesEntity.class);
            q.setParameter("id", patientId);
            q.executeUpdate();
            transaction.commit();
            logger.info("End of deleteProcessedPatientId() method");
        } catch (Exception ex) {
            logger.error("", ex.getCause());
        } finally {
            session.close();
        }
    }



    public boolean updateReference(String response, String json, String patientId, EntityManagerFactory entityManagerFactory) {
        logger.info("Entering updateReference() method");
        String sql = "update " + exporterProperties.getDbreferences() + ".references set response = ?, datesent = ?, json = ? where an_id = ? and resource=?";
        Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
        Transaction transaction = session.beginTransaction();
        long timeNow = Calendar.getInstance().getTimeInMillis();
        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
        try {
            session.createNativeQuery(sql)
                    .setParameter(1, response)
                    .setParameter(2, ts)
                    .setParameter(3, json)
                    .setParameter(4, Long.parseLong(patientId))
                    .setParameter(5, AvailableResources.PATIENT.toString())
                    .executeUpdate();
            transaction.commit();
            deleteProcessedPatientId(Long.valueOf(patientId), entityManagerFactory);
        } catch (Exception e) {
            logger.error("Problem while inserting to reference table for anid " + patientId);
            logger.info("End of updateReference() method");
            return false;
        } finally {
            session.close();
        }
        logger.info("End of updateReference() method");
        return true;
    }
}
