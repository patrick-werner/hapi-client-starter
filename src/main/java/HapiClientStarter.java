import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class HapiClientStarter {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "https://vonk.fire.ly/r4/";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);


  }
}
