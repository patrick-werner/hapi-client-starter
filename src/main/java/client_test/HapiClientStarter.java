package client_test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Date;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Patient;

public class HapiClientStarter {

  public static void main(String[] args) {

    FhirContext ctx = FhirContext.forR4();
    String serverBase = "http://hapi.fhir.org/baseR4";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    IParser parser = ctx.newJsonParser().setPrettyPrint(true);

    Patient pat = createPatient();
    System.out.println(parser.encodeResourceToString(pat));
  }

  private static Patient createPatient() {
    Patient patient = new Patient();
    CodeableConcept cc = new CodeableConcept();
    cc.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR");
    patient
        .addIdentifier()
        .setSystem("http://meinkrankhaus.de/fhir/sid/patientId")
        .setValue("0123456789")
        .setType(cc);
    patient.setId("testId");
    patient.setActive(true);
    patient.addName().setUse(NameUse.OFFICIAL).setFamily("Nachname").addGiven("Vorname");
    patient.setGender(AdministrativeGender.OTHER);
    patient.setBirthDate(new Date());
    return patient;
  }
}
