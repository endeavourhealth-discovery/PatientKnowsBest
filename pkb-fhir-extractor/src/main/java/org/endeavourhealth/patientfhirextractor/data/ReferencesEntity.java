package org.endeavourhealth.patientfhirextractor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class ReferencesEntity {

    public ReferencesEntity(String resource, String location){
        this.resource = resource;
        this.location = location;
    }

    public ReferencesEntity(String resource, long an_id, String location){
        this.resource = resource;
        this.an_id = an_id;
        this.location = location;
    }

    @Id
    private long an_id;
    private String strid;
    private String resource;
    private String response;
    private String location;
    private String datesent;
    private String type_id;
    private String json;
    private String patientId;

}
