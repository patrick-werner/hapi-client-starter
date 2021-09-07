import ca.uhn.fhir.context.FhirContext;

public class HapiClientStarter {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "http://hapi.fhir.org/baseR4";
  }
}
