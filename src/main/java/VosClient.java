import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Date;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IIdType;


public class VosClient {

  private static IParser parser;
  private Patient pat;
  private Practitioner practitioner;
  private Medication medication;
  private Organization organization;

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forDstu3();
    String serverBase = "http://hapi.fhir.org/baseDstu3";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    Patient patient = getVosPat();
    patient.setId("Patient/1234");
    Practitioner practitioner = getVosPract();
    practitioner.setId("Practitioner/123");
    Medication medication = getVosMedication();
    medication.setId("Medication/4654");
    Organization organization = getOrganization();
    organization.setId("Organization/234345");

    MedicationRequest request = createMedicationRequest(patient, practitioner, medication,
        organization);

    parser = ctx.newJsonParser().setPrettyPrint(true);
    System.out.println(parser.encodeResourceToString(request));

    createReadUpdateDelete(patient, client);
  }

  private static void createReadUpdateDelete(Patient patient, IGenericClient client) {
    patient.setId("");
    MethodOutcome outcome = client.create().resource(patient).execute();
    IIdType id = outcome.getId();
    System.out.println("Got ID: " + id.getValue());
    patient.setId(id.getValue());

    client.update().resource(patient).execute();
    //client.delete().resource(patient).execute();
    Patient newPatient = client.read().resource(Patient.class).withId(id.getIdPart()).execute();
    System.out.println(parser.encodeResourceToString(newPatient));

    System.out.println(id.getIdPart());
  }


  private static MedicationRequest createMedicationRequest(Patient patient,
      Practitioner practitioner,
      Medication medication, Organization organization) {
    MedicationRequest request = new MedicationRequest();
//    request.setSubject(new Reference(pat));
    request.setSubject(new Reference(patient));
    request.getRequester().setAgent(new Reference(practitioner))
        .setOnBehalfOf(new Reference(organization));
    request.setMedication(new Reference(medication));
    request.getSubstitution().setAllowed(true);
    return request;
  }

  private static Organization getOrganization() {
    Organization orga = new Organization();
    orga.addIdentifier().setSystem("http://fhir.de/NamingSystem/kbv/bsnr").setValue("12345");
    orga.setName("Organization");
    orga.addAddress().setCity("Stadt");
    return orga;
  }

  private static Medication getVosMedication() {
    Medication med = new Medication();
    Coding coding = new Coding().setSystem("http://fhir.de/CodeSystem/ifa/pzn")
        .setCode("9621117").setDisplay("RITALIN 10 mg Tabletten");
    med.setCode(new CodeableConcept().addCoding(coding));
    return med;
  }

  private static Practitioner getVosPract() {
    Practitioner practitioner = new Practitioner();
    practitioner.addIdentifier().setSystem("http://fhir.de/NamingSystem/kbv/lanr")
        .setValue("12345");
    practitioner.addName().setFamily("von Practi").addGiven("PractiGiven");
    practitioner.getNameFirstRep().getFamilyElement().addExtension()
        .setUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name")
        .setValue(new StringType("Practi"));
    practitioner.addTelecom().setSystem(ContactPointSystem.PHONE).setValue("+49123");
    return practitioner;
  }

  private static Patient getVosPat() {
    Patient pat = new Patient();
    pat.addIdentifier().setSystem("http://kbv-pvs-system/pid").setValue("12345");
    pat.addName().setFamily("von Family").addGiven("Vorname");
    pat.getNameFirstRep().getFamilyElement().addExtension()
        .setUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name")
        .setValue(new StringType("Family"));
    pat.setGender(AdministrativeGender.FEMALE);
    pat.setBirthDate(new Date());
    pat.addAddress().addLine("Straße 1").setCity("Stadt").setPostalCode("12345").setState("DE-HH")
        .setCountry("DE");
    return pat;
  }


}
