package client_test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Date;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

public class HapiClientStarter {

  public static void main(String[] args) {

    FhirContext ctx = FhirContext.forR4();
    String serverBase = "http://hapi.fhir.org/baseR4";
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    IParser parser = ctx.newJsonParser().setPrettyPrint(true);

    Patient pat = createPatient();
    System.out.println(parser.encodeResourceToString(pat));
    Encounter enc = createEncounter(pat);
    System.out.println(parser.encodeResourceToString(enc));
    Condition cond = createCondition(pat, enc);
  }

  private static Patient createPatient() {
    Patient patient = new Patient();
    CodeableConcept cc = new CodeableConcept();
    cc.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR");
    patient
        .addIdentifier()
        .setSystem("http://meinkrankhaus.de/fhir/sid/patientId")
        .setValue("0123456789")
        .setType(cc);
    patient.setId("testId");
    patient.setActive(true);
    patient.addName().setUse(NameUse.OFFICIAL).setFamily("Nachname").addGiven("Vorname");
    patient.setGender(AdministrativeGender.OTHER);
    patient.setBirthDate(new Date());
    return patient;
  }

  private static Condition createCondition(Patient patient, Encounter encounter) {
    Condition condition = new Condition();
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
    Extension extension = enc.addExtension();
    extension.setUrl("http://fhir.de/StructureDefinition/Aufnahmegrund");
    extension
        .addExtension()
        .setUrl("ErsteUndZweiteStelle")
        .setValue(
            new Coding()
                .setSystem("http://fhir.de/CodeSystem/dkgev/AufnahmegrundErsteUndZweiteStelle")
                .setCode("01")
                .setDisplay("Krankenhausbehandlung, vollstationär"));
    extension
        .addExtension()
        .setUrl("DritteStelle")
        .setValue(
            new Coding()
                .setSystem("http://fhir.de/CodeSystem/dkgev/AufnahmegrundDritteStelle")
                .setCode("0"));
    extension
        .addExtension()
        .setUrl("VierteStelle")
        .setValue(
            new Coding()
                .setSystem("http://fhir.de/CodeSystem/dkgev/AufnahmegrundVierteStelle")
                .setCode("1")
                .setDisplay("Normalfall"));

    return enc;
  }
}
