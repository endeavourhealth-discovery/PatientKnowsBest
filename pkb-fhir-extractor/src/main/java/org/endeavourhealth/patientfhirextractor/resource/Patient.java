package org.endeavourhealth.patientfhirextractor.resource;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.patientfhirextractor.constants.ResourceConstants;
import org.endeavourhealth.patientfhirextractor.data.PatientEntity;
import org.endeavourhealth.patientfhirextractor.service.PatientService;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class Patient implements ResourceConstants {
    Logger logger = LoggerFactory.getLogger(Patient.class);

    PatientService patientService;

    public org.hl7.fhir.dstu3.model.Patient getPatientResource(PatientEntity patientResult, String patientLocation, PatientService patientService) throws Exception {
        logger.info("Entering getPatientResource() method");

        this.patientService = patientService;
        String ods_code = replaceNull(patientResult.getCode());
        String nhsNumber = replaceNull(patientResult.getNhsNumber());
        String gender = replaceNull(patientResult.getGender());
        String lastname = replaceNull(patientResult.getLastname());
        String title = replaceNull(patientResult.getTitle());
        String firstname = replaceNull(patientResult.getFirstname());
        String dob = replaceNull(patientResult.getDob());
        String telecom = replaceNull(patientResult.getTelecom());
        String adduse = replaceNull(patientResult.getAdduse());
        String add1 = replaceNull(patientResult.getAdd1());
        String add2 = replaceNull(patientResult.getAdd2());
        String add3 = replaceNull(patientResult.getAdd3());
        String add4 = replaceNull(patientResult.getAdd4());
        String postcode = replaceNull(patientResult.getPostcode());
        String city = replaceNull(patientResult.getCity());

        org.hl7.fhir.dstu3.model.Patient patient = new org.hl7.fhir.dstu3.model.Patient();

        patient.setId(StringUtils.isNotEmpty(patientLocation) ? patientLocation : UUID.randomUUID().toString());

        Meta meta = new Meta();
        Coding coding = new Coding();
        coding.setSystem(META_SYSTEM);
        coding.setCode(ods_code);
        meta.setTag(Arrays.asList(coding));
        patient.setMeta(meta);


        Identifier identifier = new Identifier();
        Extension extension = new Extension();
        extension.setUrl(IDENTIFIER_URL);
        CodeableConcept codeableConcept = (CodeableConcept) extension.addChild(VALUE_CODEABLE_CONCEPT);
        List<Coding> codingIdentifier = new ArrayList<>();
        codingIdentifier.add(new Coding(CODING_SYSTEM, "01", ""));
        codeableConcept.setCoding(codingIdentifier);

        identifier.setExtension(Arrays.asList(extension));
        identifier.setValue(nhsNumber);
        identifier.setSystem(NHS_NUMBER_IDENTIFIER);
        patient.addIdentifier(identifier);

        patient.addName()
                .setFamily(lastname)
                .addPrefix(title)
                .addGiven(firstname)
                .setUse(HumanName.NameUse.OFFICIAL);

        // contact_type`contact_use`contact_value|
        if (telecom.length() > 0) {
            String[] ss = telecom.split("\\|");
            String z = "";
            for (int i = 0; i < ss.length; i++) {
                z = ss[i];
                String[] contact = z.split("\\`");
                ContactPoint t = new ContactPoint();

                t.setValue(contact[0]);

                if (contact[2].equals("Mobile")) t.setUse(ContactPoint.ContactPointUse.MOBILE);
                if (contact[2].equals("Home")) t.setUse(ContactPoint.ContactPointUse.HOME);

                if (contact[1].equals("Email")) t.setSystem(ContactPoint.ContactPointSystem.EMAIL);
                if (contact[1].equals("Phone")) t.setSystem(ContactPoint.ContactPointSystem.PHONE);

                patient.addTelecom(t);
            }
        }

        switch (gender) {
            case "Other":
                patient.setGender(Enumerations.AdministrativeGender.OTHER);
                break;
            case "Male":
                patient.setGender(Enumerations.AdministrativeGender.MALE);
                break;
            case "Female":
                patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                break;
            case "Unknown":
                patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                break;
            default:
                // code block
        }

        if (!dob.isEmpty()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            patient.setBirthDate(format.parse(dob));
        }

        //TODO: Country, State information is needed but not available
        Address address = new Address();

        if (adduse.equals("HOME")) {
            address.setUse(Address.AddressUse.HOME);
        }
        if (adduse.equals("TEMP")) {
            address.setUse(Address.AddressUse.TEMP);
        }
        if (adduse.equals("OLD")) {
            address.setUse(Address.AddressUse.OLD);
        }

        address.addLine(add1);
        address.addLine(add2);
        address.addLine(add3);
        address.addLine(add4);
        address.setPostalCode(postcode);
        address.setCity(city);

        patient.addAddress(address);

        patient.setActive(patientService.isPatientActive(patientResult.getId()));
        logger.info("End of getPatientResource() method");
        return patient;
    }

    public static String replaceNull(String input) {
        return input == null ? "" : input;
    }

}
