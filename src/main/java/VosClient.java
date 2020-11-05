import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Date;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;


public class VosClient {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forDstu3();
    String serverBase = "http://hapi.fhir.org/baseDstu3";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    Patient pat = getVosPat();
    IParser parser = ctx.newJsonParser().setPrettyPrint(true);
    System.out.println(parser.encodeResourceToString(pat));
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
    pat.addAddress().addLine("Straße 1").setCity("STadt").setPostalCode("12345").setState("DE-HH")
        .setCountry("DE");
    return pat;
  }


}
