import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

public class HapiClientStarter {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "https://vonk.fire.ly/r4/";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    //Patient erstellen
    Patient patient = new Patient();
    //Profile setzen
    patient.getMeta().addProfile("http://fhir.gematik.de/isik/StructureDefinition/IsikPatient");
    patient.getMeta().addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Patient");
    patient.getMeta().addProfile(
        "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient");
    //Patient.active true setzen
    patient.setActive(true);
    //Geschlecht Divers setzen
    patient.setGender(AdministrativeGender.OTHER);
    patient.getGenderElement().addExtension("http://fhir.de/StructureDefinition/gender-amtlich-de",
        new Coding().setSystem("http://fhir.de/CodeSystem/gender-amtlich-de")
            .setCode("D").setDisplay("divers"));
    //Geburtsdatum
    LocalDate localDate = LocalDate.of(1980, 02, 20);
    Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    patient.setBirthDate(date);

    //Namen setzen
    HumanName name = patient.getNameFirstRep();
    name.setUse(NameUse.OFFICIAL);
    name.setFamily("Gräf:in van Familienname").addGiven("EinVorname");
    //Namenszusatz
    name.addExtension("http://fhir.de/StructureDefinition/humanname-namenszusatz",
        new StringType("Gräf:in"));
    //Vorsatzwort
    name.addExtension("http://hl7.org/fhir/StructureDefinition/humanname-own-prefix",
        new StringType("van"));
    //Alleinstehender Nachname
    name.addExtension("http://hl7.org/fhir/StructureDefinition/humanname-own-name",
        new StringType("Familienname"));

    //PID Identifier setzen
    CodeableConcept mrConcept = new CodeableConcept();
    mrConcept.addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR");
    patient.addIdentifier().setType(mrConcept)
        .setSystem("http://superKrankenhaus/fhir/NamingSystem/patientenID").setValue("0123456789");

    //GKV Identifier
    CodeableConcept gkvPatient = new CodeableConcept();
    gkvPatient.addCoding()
        .setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis").setCode("GKV");
    Identifier identifier = new Identifier().setSystem("http://fhir.de/NamingSystem/arge-ik/iknr")
        .setValue("0123456789");
    Reference reference = new Reference().setIdentifier(identifier);
    patient.addIdentifier().setType(gkvPatient)
        .setSystem("http://fhir.de/NamingSystem/gkv/kvid-10").setValue("0123456789")
        .setAssigner(reference);

    //ID setzen ++++ ACHTUNG!!! IDs werden normalerweise VOM SERVER gesetzt ++++
    patient.setId("superID");

    //Adresse
    Address address = patient.getAddressFirstRep();
    address.setType(AddressType.PHYSICAL);
    ArrayList<StringType> lines = new ArrayList<>();
    StringType line = new StringType("StraßenStraße 12A");
    line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName",
        new StringType("StraßenStraße"));
    line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber",
        new StringType("12A"));
    lines.add(line);
    address.setLine(lines);
    address.setPostalCode("12345");
    address.setCity("Venusheim");
    address.setCountry("AUS");

    //encoden in JSON und XML
    IParser jsonParser = ctx.newJsonParser().setPrettyPrint(true);
    IParser xmlParser = ctx.newXmlParser().setPrettyPrint(true);
    System.out.println(jsonParser.encodeResourceToString(patient));
    System.out.println(xmlParser.encodeResourceToString(patient));

    //Validieren
    MethodOutcome outcome = client.validate()
        .resource(patient)
        .execute();

// The returned object will contain an operation outcome resource
    OperationOutcome oo = (OperationOutcome) outcome.getOperationOutcome();

    for (OperationOutcome.OperationOutcomeIssueComponent nextIssue : oo.getIssue()) {
      System.out.println(nextIssue.getSeverity() + ": " + nextIssue.getDetails().getText());
    }
  }
}
