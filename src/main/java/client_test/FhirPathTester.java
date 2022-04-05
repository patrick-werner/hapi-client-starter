package client_test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.util.List;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Type;

public class FhirPathTester {
  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    IParser iParser = ctx.newJsonParser().setPrettyPrint(true);

    Patient patient = HapiClientStarter.createPatient();
    FhirPathR4 fhirpath = new FhirPathR4(ctx);
    List<Base> evaluate = fhirpath.evaluate(patient, "Patient.name", Base.class);
    System.out.println(evaluate.size());
    evaluate.forEach(
        e -> {
          Extension ext = new Extension();
          ext.setUrl("http://example.org");
          ext.setValue((Type) e);
          Observation obs = new Observation();
          obs.addExtension(ext);
          System.out.println(iParser.encodeResourceToString(obs));
        });
  }
}
