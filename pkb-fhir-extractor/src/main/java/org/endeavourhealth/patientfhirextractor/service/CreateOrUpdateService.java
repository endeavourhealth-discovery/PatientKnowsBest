package org.endeavourhealth.patientfhirextractor.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreateOrUpdateService {
    Logger logger = LoggerFactory.getLogger(CreateOrUpdateService.class);
    FhirContext ctx = FhirContext.forDstu3();

    @Async
    public CompletableFuture<String> createOrUpdatePatient(Patient patientResource) {
        //Testing
        // Create a client and post the transaction to the server
        IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseDstu3");
        MethodOutcome resp = client.create().resource(patientResource).execute();
        // Log the response
               /* if (StringUtils.isEmpty(patientLocation)) {
                    //TODO: POST
                } else {
                    //TODO: PUT
                }*/
        return CompletableFuture.completedFuture(resp.getId().toString());
    }


}
