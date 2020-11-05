import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Date;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;


public class VosClient {

  private static Patient pat;
  private static Practitioner practitioner;
  private static Medication medication;
  private static Organization organization;

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forDstu3();
    String serverBase = "http://hapi.fhir.org/baseDstu3";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    pat = getVosPat();
    pat.setId("Patient/1234");
    practitioner = getVosPract();
    practitioner.setId("Practitioner/123");
    medication = getVosMedication();
    medication.setId("Medication/4654");
    organization = getOrganization();
    organization.setId("Organization/234345");

    MedicationRequest request = createMedicationRequest(practitioner, medication, organization);

    IParser parser = ctx.newJsonParser().setPrettyPrint(true);
    System.out.println(parser.encodeResourceToString(request));
  }

  private static MedicationRequest createMedicationRequest(Practitioner practitioner,
      Medication medication, Organization organization) {
    MedicationRequest request = new MedicationRequest();
//    request.setSubject(new Reference(pat));
    request.setSubject(new Reference(pat));
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
