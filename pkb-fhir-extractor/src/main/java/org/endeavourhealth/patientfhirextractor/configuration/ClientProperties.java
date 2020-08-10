package org.endeavourhealth.patientfhirextractor.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("config")
public class ClientProperties {

    String clientId;
    String baseUrl;
    String tokenUrl;
    String scope;
    String clientSecret;
    String grantType;
    String token;
}
