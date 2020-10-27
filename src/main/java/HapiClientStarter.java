import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

public class HapiClientStarter {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "https://vonk.fire.ly/r4/";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    //Patient erstellen
    Patient patient = new Patient();
    //Profil setzen
    patient.getMeta().addProfile("http://fhir.gematik.de/isik/StructureDefinition/IsikPatient");
    //Patient.active true setzen
    patient.setActive(true);
    //Geschlecht Divers setzen
    patient.setGender(AdministrativeGender.OTHER);
    patient.getGenderElement().addExtension("http://fhir.de/StructureDefinition/gender-amtlich-de",
        new CodeableConcept().addCoding().setSystem("http://fhir.de/CodeSystem/gender-amtlich-de")
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

// If the OperationOutcome has any issues with a severity of ERROR or SEVERE,
// the validation failed.
    for (OperationOutcome.OperationOutcomeIssueComponent nextIssue : oo.getIssue()) {
      System.out.println(nextIssue.getSeverity() + ": " + nextIssue.getDetails().getText());
    }
  }
}
