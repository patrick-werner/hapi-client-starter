import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;

public class RrObservation extends Observation {

  public static void main(String[] args) {
    RrObservation obs = new RrObservation();
    obs.setBodyTemp(36.8f);
    FhirContext fhirContext = FhirContext.forR4();
    IParser iParser = fhirContext.newJsonParser().setPrettyPrint(true);
    System.out.println(iParser.encodeResourceToString(obs.getResource()));
  }

  public RrObservation() {
    setStatus(ObservationStatus.FINAL);
  }

  public IBaseResource getResource() {
    return copy();
  }

  public void setBodyTemp (float temp) {
    Quantity quantity = new Quantity();
    quantity.setValue(temp).setUnit("°C").setSystem("http://unitsofmeasure.org")
        .setCode("°C");
    setValue(quantity);
  }
}
