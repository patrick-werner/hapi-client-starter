import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Date;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

public class HapiClientStarterCreateUpdateDelete {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = "http://hapi.fhir.org/baseR4";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    IParser parser = ctx.newJsonParser().setPrettyPrint(true);

    Patient patient = createPatient();
    client.update().resource(patient).execute();

    Encounter encounter = createEncounter(patient);
    client.update().resource(encounter).execute();

    Condition condition = createCondition(patient, encounter);
    MethodOutcome execute = client.update().resource(condition).execute();
    IIdType conditionId = execute.getId();

    Patient readPatient = client.read().resource(Patient.class).withId(patient.getId()).execute();

    readPatient.setGender(AdministrativeGender.FEMALE);

    MethodOutcome methodOutcome = client.update().resource(readPatient).execute();
    System.out.println(methodOutcome.getId());

    // client.delete().resource(readPatient).execute();

    //passt evtl.
    client.search().forResource(Condition.class).where(Condition.RES_ID.exactly().code(conditionId.getIdPart())).execute();

    Bundle returnBundle =
        client
            .search()
            .byUrl(
                "Condition?_id="
                    + conditionId.getIdPart()
                    + "&_include=Condition:subject&_include=Condition:encounter")
            .returnBundle(Bundle.class)
            .execute();

    System.out.println(parser.encodeResourceToString(returnBundle));

    // TODO: validation in client broken at the moment
    //    MethodOutcome execute = client.validate().resource(patient).execute();
    //    OperationOutcome operationOutcome = (OperationOutcome) execute.getOperationOutcome();
    //    operationOutcome.getIssue().forEach(i -> {
    //      System.out.println(i.getSeverity() + ": " + i.getDiagnostics());
    //    });
  }

  private static Condition createCondition(Patient patient, Encounter encounter) {
    Condition condition = new Condition();
    condition.setId("condition01");
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
    enc.setId("encounter01");
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
    pat.setId("patient01");
    pat.addName().setUse(NameUse.OFFICIAL).setFamily("Werner").addGiven("Patrick");
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
