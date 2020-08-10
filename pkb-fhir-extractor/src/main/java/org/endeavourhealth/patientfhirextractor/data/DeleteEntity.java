package org.endeavourhealth.patientfhirextractor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.Id;
import javax.persistence.Entity;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class DeleteEntity {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteEntity.class);

    @Id
    private long record_id;
    private int table_id;
}
