import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;

public class HapiClientStarter {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "http://hapi.fhir.org/baseR4";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);
    client.registerInterceptor(new LoggingInterceptor());

    //create Patient
    Patient pat = new Patient();
    pat.addIdentifier().setSystem("http://example.com/identifier").setValue("007");
    pat.addName().setFamily("Family").addGiven("Peter");
    pat.addAddress().addLine("TestStreet 1").setPostalCode("12345").setCity("Mannheim");
    Address addr = new Address();
    pat.addExtension().setUrl("http://hl7.org/fhir/StructureDefinition/patient-birthPlace")
        .setValue(addr.setCity("Berlin"));
    pat.addExtension().setUrl("http://hl7.org/fhir/StructureDefinition/patient-cadavericDonor")
        .setValue(new BooleanType(true));
    //set TempId
    pat.setId(IdType.newRandomUuid());

    //Practitioner
    Practitioner practitioner = new Practitioner();
    practitioner.addName().setFamily("Practitioner").addGiven("Liz");
    //set TempId
    practitioner.setId(IdType.newRandomUuid());

    //Glucose Observation
    Observation gluc = new Observation();
    gluc.getMeta().addProfile(
        "http://build.fhir.org/ig/HL7/fhir-ips/StructureDefinition-Observation-results-uv-ips");
    gluc.setStatus(ObservationStatus.FINAL);
    gluc.addCategory().addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
        .setCode("laboratory");
    gluc.getCode().addCoding().setSystem("http://loinc.org").setCode("2339-0")
        .setDisplay("Glucose [Mass/volume] in Blood");
    gluc.setSubject(new Reference(pat));
    gluc.setEffective(new DateTimeType("2020-08-01"));
    gluc.addPerformer(new Reference(practitioner));
    Quantity quantity = new Quantity();
    quantity.setValue(6.3).setUnit("mmol/l").setSystem("http://unitsofmeasure.org")
        .setCode("mmol/L");
    gluc.setValue(quantity);
    //set TempId
    gluc.setId(IdType.newRandomUuid());

    //RR Observation
    Observation rr = new Observation();
    rr.getMeta().addProfile(
        "http://hl7.org/fhir/uv/ips/StructureDefinition/Observation-results-uv-ips")
        .addProfile("http://hl7.org/fhir/StructureDefinition/bp");
    rr.setStatus(ObservationStatus.FINAL);
    rr.addCategory().addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
        .setCode("vital-signs");
    rr.getCode().addCoding().setSystem("http://loinc.org").setCode("85354-9")
        .setDisplay("Blood pressure panel with all children optional");
    rr.setSubject(new Reference(pat));
    rr.setEffective(new DateTimeType("2020-08-01"));
    rr.addPerformer(new Reference(practitioner));
    //systolic
    ObservationComponentComponent compSys = rr.addComponent();
    compSys.getCode().addCoding().setSystem("http://loinc.org").setCode("8480-6");
    quantity.setUnit("mm[Hg]").setCode("mm[Hg]").setSystem("http://unitsofmeasure.org")
        .setValue(120);
    compSys.setValue(quantity);
    //diastolic
    ObservationComponentComponent compDia = rr.addComponent();
    compDia.getCode().addCoding().setSystem("http://loinc.org").setCode("8462-4");
    quantity.setUnit("mm[Hg]").setCode("mm[Hg]").setSystem("http://unitsofmeasure.org")
        .setValue(80);
    compDia.setValue(quantity);
    //set TempId
    rr.setId(IdType.newRandomUuid());

    Bundle bundle = new Bundle();
    bundle.addEntry().setFullUrl(pat.getId()).setResource(pat).getRequest().setUrl("Patient")
        .setMethod(
            HTTPVerb.POST);
    bundle.addEntry().setFullUrl(practitioner.getId()).setResource(practitioner).getRequest()
        .setUrl("Practitioner")
        .setMethod(
            HTTPVerb.POST);
    bundle.addEntry().setFullUrl(gluc.getId()).setResource(gluc).getRequest().setUrl("Observation")
        .setMethod(
            HTTPVerb.POST);
    bundle.addEntry().setFullUrl(rr.getId()).setResource(rr).getRequest().setUrl("Observation")
        .setMethod(
            HTTPVerb.POST);
    bundle.setType(Bundle.BundleType.TRANSACTION);

    //print Patient
    System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(pat));

    //persist Bundle
    Bundle execute1 = client.transaction().withBundle(bundle).execute();
    execute1.getEntry().stream()
        .map(i -> i.getResponse().getStatus() + ": " + i.getResponse().getLocation())
        .forEach(System.out::println);

    // Validate the Bundle
    MethodOutcome outcome = client.validate()
        .resource(bundle)
        .execute();

// The returned object will contain an operation outcome resource
    OperationOutcome oo = (OperationOutcome) outcome.getOperationOutcome();
    oo.getIssue().stream().map(i -> i.getDiagnostics() + ": " + i.getSeverity())
        .forEach(System.out::println);
  }
}
