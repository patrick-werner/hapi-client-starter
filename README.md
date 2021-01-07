# hapi-client-starter
Client Excercise:

- Create a Patient, fill out (at least): Identifier, Name, Address,gender and add its birthplace and cadavericDonorStatus.
- Create an Observation: Blood Glucose with the profile: http://hl7.org/fhir/uv/ips/2018Sep/StructureDefinition-observation-uv-ips.html
- Create an Observation: Blood Pressure with the profile: http://hl7.org/fhir/uv/ips/2018Sep/StructureDefinition-observation-uv-ips.html (pay attention to the needed vital-signs profile as well: https://www.hl7.org/fhir/bp.html)
- Create the rest of needed Ressources according to the IPS Profile: e.g. Practitioner 
- Create a Transaction Bundle containing all the resources
- Send the Transaction Bundle to the test server
