import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.ArrayList;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

public class HapiClientStarter_new {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "https://vonk.fire.ly/r4/";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    //Patient erstellen
    Patient patient = new Patient();

    //Adresse
    Address address = patient.getAddressFirstRep();
    ArrayList<StringType> lines = new ArrayList<>();
    StringType line = new StringType("Street 1");
    line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName",
        new StringType("Street"));
    line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber",
        new StringType("1"));
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
