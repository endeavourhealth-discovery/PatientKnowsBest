package org.endeavourhealth.patientfhirextractor.configuration;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("fhir")
public class ExporterProperties {
    String outputFHIR;
    String dbschema;
    String dbreferences;
    String config;
    String scope;
    String granttype;
    String tokenurl;
    String token;
    String runguid;
    Integer scaletotal;
    String organization;
    Integer procrun;
    Integer testobs;
    String resendpats;
    Integer deletesdone;
    String nominatedOrganization;
}
