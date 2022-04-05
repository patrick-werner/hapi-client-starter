package client_test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Patient;

public class FhirPathTester {
  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    IParser iParser = ctx.newJsonParser().setPrettyPrint(true);

    Patient patient = HapiClientStarter.createPatient();
  }
}
