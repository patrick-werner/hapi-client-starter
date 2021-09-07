import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.fhir.ucum.BaseUnit;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;


public class HapiClientStarterTransactionBundle {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "http://hapi.fhir.org/baseR4";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    IParser parser = ctx.newJsonParser().setPrettyPrint(true);

    Patient patient = createPatient();

    Encounter encounter = createEncounter(patient);

    Condition condition = createCondition(patient, encounter);

    Bundle bundle = createTransactionBundle(List.of(patient, encounter, condition));
    System.out.println(parser.encodeResourceToString(bundle));
    Bundle respBundle = client.transaction().withBundle(bundle).execute();
    System.out.println(parser.encodeResourceToString(respBundle));
  }

  private static Bundle createTransactionBundle(List<Resource> resources) {
    Bundle bundle = new Bundle();
    bundle.setType(BundleType.TRANSACTION);
    resources.forEach(r -> {
      BundleEntryComponent bundleEntryComponent = bundle.addEntry();
      bundleEntryComponent.setFullUrl(r.getId());
      bundleEntryComponent.setResource(r);
      bundleEntryComponent.getRequest().setUrl(r.getResourceType().name()).setMethod(HTTPVerb.POST);
    });
    return  bundle;
  }

  private static Condition createCondition(Patient patient, Encounter encounter) {
    Condition condition = new Condition();
    condition.setId(IdType.newRandomUuid());
    condition.setRecordedDate(new Date());
    condition
        .getClinicalStatus()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
        .setCode("active");
    condition.setSubject(new Reference(patient));
    condition.setEncounter(new Reference(encounter));
    condition
        .getCode()
        .addCoding()
        .setSystem("http://snomed.info/sct")
        .setCode("389145006")
        .setDisplay("allergisches Asthma");
    return condition;
  }

  private static Encounter createEncounter(Patient patient) {
    Encounter enc = new Encounter();
    enc.setId(IdType.newRandomUuid());
    Identifier identifier = enc.addIdentifier();
    identifier
        .getType()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
        .setCode("VN");
    identifier.setSystem("http://meinkrankenhaus.de/sid/Fallnummer").setValue("0815");
    enc.getServiceType()
        .addCoding()
        .setSystem("http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel")
        .setCode("0300")
        .setDisplay("Kardiologie");
    enc.getPeriod().setStart(new Date());
    enc.setSubject(new Reference(patient));
    enc.getClass_()
        .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
        .setCode("IMP")
        .setDisplay("inpatient encounter");
    enc.getHospitalization()
        .getAdmitSource()
        .addCoding()
        .setSystem("http://fhir.de/CodeSystem/dgkev/Aufnahmeanlass")
        .setCode("N")
        .setDisplay("Notfall");
    return enc;
  }

  private static Patient createPatient() {
    Patient pat = new Patient();
    pat.setActive(true);
    pat.setId(IdType.newRandomUuid());
    pat.addName().setUse(NameUse.OFFICIAL).setFamily("Werner").addGiven("Patrick");
    pat.addName().setUse(NameUse.MAIDEN).setFamily("TestName");
    Identifier identifier = pat.addIdentifier();
    identifier
        .getType()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
        .setCode("MR");
    identifier.setSystem("http://meinkrankenhaus.de/sid/Patientnummer").setValue("007");
    pat.setGender(AdministrativeGender.MALE);
    DateType date = new DateType("1982-04-03");
    pat.setBirthDateElement(date);
    return pat;
  }
}
