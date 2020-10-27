import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class HapiClientStarter {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "https://dit-fhir.molit.eu/fhir";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);
  }
}
