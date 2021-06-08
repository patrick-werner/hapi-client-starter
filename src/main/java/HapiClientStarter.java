import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

public class HapiClientStarter {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "http://hapi.fhir.org/baseR4";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);
    client.registerInterceptor(new LoggingInterceptor());

    Patient patient = new Patient();

    patient.setId("2161215");

    patient.setActive(true);
    patient.setGender(AdministrativeGender.OTHER);
    patient.setBirthDateElement(new DateType("1980-05-03"));
    // identisch: patient.setBirthDate(new Date("1980-05-03"));

    Identifier identifier = patient.addIdentifier();
    identifier.getType().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
        .setCode("MR");
    identifier.setSystem("http://exampleHospital.com/fhir/sid/Patientennummer")
        .setValue("123456789");

    HumanName name = patient.addName();
    name.setUse(NameUse.OFFICIAL);
    name.setFamily("Nachname");
    name.getFamilyElement().addExtension()
        .setUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name")
        .setValue(new StringType("Nachname"));
    name.addGiven("Vorname");
    name.setText("Vorname Nachname");

    patient.getMeta().addProfile("https://gematik.de/fhir/ISiK/StructureDefinition/ISiKPatient");

    IParser jsonParser = ctx.newJsonParser().setPrettyPrint(true);
    System.out.println(jsonParser.encodeResourceToString(patient));

    MethodOutcome execute = client.create().resource(patient).execute();

//    System.out.println(execute.getId());

//    client.delete().resourceById(new IdType("Patient/2161215")).execute();
  }
}
