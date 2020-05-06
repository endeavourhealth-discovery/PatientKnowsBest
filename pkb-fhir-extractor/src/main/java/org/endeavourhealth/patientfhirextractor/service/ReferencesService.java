package org.endeavourhealth.patientfhirextractor.service;

import org.endeavourhealth.patientfhirextractor.configuration.ExporterProperties;
import org.endeavourhealth.patientfhirextractor.data.ReferencesEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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
}
