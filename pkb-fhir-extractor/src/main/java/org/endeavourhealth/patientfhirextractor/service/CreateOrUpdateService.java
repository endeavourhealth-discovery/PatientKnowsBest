package org.endeavourhealth.patientfhirextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.patientfhirextractor.configuration.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.Map;

@Service
public class CreateOrUpdateService {
    Logger logger = LoggerFactory.getLogger(CreateOrUpdateService.class);

    @Autowired
    private ClientProperties clientProperties;

    @Autowired
    private ReferencesService referencesService;

    @Autowired
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    public void createOrUpdatePatient(String patientId, String token, String patientResource, String location, boolean update, String orgId) {
        saveOrUpdatePatient(patientId, patientResource, token, location, update, orgId);
    }


    public String getToken() {
        try {
            final String baseUrl = clientProperties.getTokenUrl();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientProperties.getUsername(),clientProperties.getPassword());

            HttpEntity entity = new HttpEntity(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(baseUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonMap = mapper.readValue(result, Map.class);
            String token = (String) jsonMap.get("access_token");
            clientProperties.setToken(token);
            return token;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
     //http://10.0.101.59:8080/procedure/cohort/start
    private void saveOrUpdatePatient(String patientId, String patientResource, String token, String location, boolean update, String orgId) {
        logger.info("Entering saveOrUpdatePatient() method");
        final String baseUrl = clientProperties.getBaseUrl();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Org-Public-Id","2851ea3a-d3b7-44ab-9221-ba6e40342539");
            headers.setBearerAuth(token);
            HttpEntity entity = new HttpEntity(patientResource, headers);
            RestTemplate restTemplate = new RestTemplate();
            //String url = update ? baseUrl + "/" + location : baseUrl;
            String url =  baseUrl;
           // ResponseEntity<Object> response = restTemplate
                  //  .exchange(url, update ? HttpMethod.PUT : HttpMethod.POST, entity, Object.class);
             ResponseEntity<Object> response = restTemplate
             .exchange(url, HttpMethod.POST, entity, Object.class);
           // if (update) {
              //  referencesService.updateReference(String.valueOf(response.getStatusCodeValue()), patientResource, patientId,  entityManagerFactory);
           // } else {
               referencesService.saveReference(patientId, patientResource, location, String.valueOf(response.getStatusCodeValue()), orgId, entityManagerFactory);
          //  }
            logger.info(response.getStatusCode().toString());
        } catch (Exception e) {
            logger.error("Problem in save or update" + e.getMessage());
        }

    }


}
