# Azure Cognitive Services Health Insights Radiology Insights client library for Java


[Health Insights][health_insights] is an Azure Applied AI Service built with the Azure Cognitive Services Framework, that leverages multiple Cognitive Services, Healthcare API services and other Azure resources.

[Radiology Insights][radiology_insights_docs] is a model that aims to provide quality checks as feedback on errors and inconsistencies (mismatches) and ensures critical findings are identified and communicated using the full context of the report. Follow-up recommendations and clinical findings with measurements (sizes) documented by the radiologist are also identified.

[Source code][source_code] | [Package (Maven)][package] | API reference documentation | [Product Documentation][product_documentation] | [Samples][samples_location]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Cognitive Services Health Insights instance.

For more information about creating the resource or how to get the location and sku information see [here][cognitive_resource_cli].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-health-insights-radiologyinsights;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-health-insights-radiologyinsights</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Health Insights Radiology Insights service, you'll need to create an instance of the [`RadiologyInsightsClient`][radiology_insights_client_class] class.  You will need an **endpoint** and an **API key** to instantiate a client object.

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Health Insights resource.

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a RadiologyInsightsClient with an API Key Credential

Once you have the value for the API key, you can pass it as a string into an instance of **AzureKeyCredential**. Use the key as the credential parameter
to authenticate the client. You may choose to build a **synchronous** or **asynchronous** client.

Build a **synchronous** client:

```java com.azure.health.insights.radiologyinsights.buildsyncclient
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");
String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_API_KEY");

RadiologyInsightsClient radiologyInsightsClient = new RadiologyInsightsClientBuilder()
        .endpoint(endpoint).serviceVersion(RadiologyInsightsServiceVersion.getLatest())
        .credential(new AzureKeyCredential(apiKey)).buildClient();
```

Build an **asynchronous** client:

```java com.azure.health.insights.radiologyinsights.buildasyncclient
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");
String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_API_KEY");

RadiologyInsightsAsyncClient radiologyInsightsAsyncClient = new RadiologyInsightsClientBuilder()
        .endpoint(endpoint).serviceVersion(RadiologyInsightsServiceVersion.getLatest())
        .credential(new AzureKeyCredential(apiKey)).buildAsyncClient();
```

## Key concepts

Radiology Insights currently supports one document from one patient. Please take a look [here][radiology_insights_inferences] for more detailed information about the inferences this service produces.  

## Examples

### Create a RadiologyInsights request and retrieve the result

Infer radiology insights from a patient's radiology report using a **synchronous** client.
- [SampleCriticalResultInferenceSync.java][ri_sync_sample]

```java com.azure.health.insights.radiologyinsights.inferradiologyinsightssync
RadiologyInsightsInferenceResult riResults = radiologyInsightsClient.beginInferRadiologyInsights(createRadiologyInsightsRequest()).getFinalResult();
```

Infer radiology insights from a patient's radiology report using an **asynchronous** client.
- [SampleCriticalResultInferenceAsync.java][ri_async_sample]

```java com.azure.health.insights.radiologyinsights.inferradiologyinsights
PollerFlux<PollOperationDetails, RadiologyInsightsInferenceResult> asyncPoller = radiologyInsightsAsyncClient
        .beginInferRadiologyInsights(createRadiologyInsightsRequest());
```

Create the request.

```java com.azure.health.insights.radiologyinsights.createrequest
private static RadiologyInsightsData createRadiologyInsightsRequest() {
    List<PatientRecord> patientRecords = createPatientRecords();
    RadiologyInsightsData radiologyInsightsData = new RadiologyInsightsData(patientRecords);
    RadiologyInsightsModelConfiguration modelConfiguration = createRadiologyInsightsModelConfig();
    radiologyInsightsData.setConfiguration(modelConfiguration);
    return radiologyInsightsData;
}

/**
 * Creates a list of patient records.
 *
 * @return A list of PatientRecord objects that represent patient information.
 */
private static List<PatientRecord> createPatientRecords() {
    List<PatientRecord> patientRecords = new ArrayList<>();
    // Patients
    PatientRecord patientRecord = new PatientRecord("Sharona");

    PatientDetails patientDetails = new PatientDetails();
    patientDetails.setSex(PatientSex.FEMALE);

    // Use LocalDate to set Date
    patientDetails.setBirthDate(LocalDate.of(1959, 11, 11));
    
    patientRecord.setInfo(patientDetails);

    Encounter encounter = new Encounter("encounterid1");

    TimePeriod period = new TimePeriod();

    OffsetDateTime startTime = OffsetDateTime.parse("2021-08-28T00:00:00Z");
    OffsetDateTime endTime = OffsetDateTime.parse("2021-08-28T00:00:00Z");

    period.setStart(startTime);
    period.setEnd(endTime);

    encounter.setPeriod(period);
    encounter.setClassProperty(EncounterClass.IN_PATIENT);

    patientRecord.setEncounters(Arrays.asList(encounter));

    PatientDocument patientDocument = getPatientDocument();
    patientDocument.setClinicalType(ClinicalDocumentType.RADIOLOGY_REPORT);
    patientDocument.setLanguage("EN");

    DocumentAuthor author = new DocumentAuthor();
    author.setId("authorid1");
    author.setFullName("authorname1");

    patientDocument.setAuthors(Arrays.asList(author));
    patientDocument.setSpecialtyType(SpecialtyType.RADIOLOGY);

    DocumentAdministrativeMetadata adminMetadata = new DocumentAdministrativeMetadata();
    FhirR4Extendible orderedProcedure = new FhirR4Extendible();

    FhirR4CodeableConcept procedureCode = new FhirR4CodeableConcept();
    FhirR4Coding procedureCoding = new FhirR4Coding();
    procedureCoding.setSystem("Http://hl7.org/fhir/ValueSet/cpt-all");
    procedureCoding.setCode("USPELVIS");
    procedureCoding.setDisplay("US PELVIS COMPLETE");

    procedureCode.setCoding(Arrays.asList(procedureCoding));
    orderedProcedure.setCode(procedureCode);
    orderedProcedure.setDescription("US PELVIS COMPLETE");

    adminMetadata.setOrderedProcedures(Arrays.asList(orderedProcedure));
    adminMetadata.setEncounterId("encounterid1");

    patientDocument.setAdministrativeMetadata(adminMetadata);

    // Define a formatter to handle milliseconds
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    OffsetDateTime createdDateTime = OffsetDateTime.parse("2021-06-01T00:00:00.000" + "+00:00", formatter);
    patientDocument.setCreatedDateTime(createdDateTime);

    patientRecord.setPatientDocuments(Arrays.asList(patientDocument));
    patientRecords.add(patientRecord);
    return patientRecords;
}

/**
 * Retrieves the patient document.
 *
 * @return The patient document.
 */
private static PatientDocument getPatientDocument() {
    DocumentContent documentContent = new DocumentContent(DocumentContentSourceType.INLINE, DOC_CONTENT);
    return new PatientDocument(DocumentType.NOTE, "docid1", documentContent);
}

/**
 * Creates a RadiologyInsightsModelConfiguration object with the specified
 * configuration settings.
 *
 * @return A new instance of RadiologyInsightsModelConfiguration.
 */
private static RadiologyInsightsModelConfiguration createRadiologyInsightsModelConfig() {
    RadiologyInsightsModelConfiguration configuration = new RadiologyInsightsModelConfiguration();
    RadiologyInsightsInferenceOptions inferenceOptions = getRadiologyInsightsInferenceOptions();
    configuration.setInferenceOptions(inferenceOptions);
    configuration.setInferenceTypes(Arrays.asList(RadiologyInsightsInferenceType.FINDING,
            RadiologyInsightsInferenceType.AGE_MISMATCH, RadiologyInsightsInferenceType.LATERALITY_DISCREPANCY,
            RadiologyInsightsInferenceType.SEX_MISMATCH, RadiologyInsightsInferenceType.COMPLETE_ORDER_DISCREPANCY,
            RadiologyInsightsInferenceType.LIMITED_ORDER_DISCREPANCY,
            RadiologyInsightsInferenceType.CRITICAL_RESULT, RadiologyInsightsInferenceType.FOLLOWUP_RECOMMENDATION,
            RadiologyInsightsInferenceType.FOLLOWUP_COMMUNICATION,
            RadiologyInsightsInferenceType.RADIOLOGY_PROCEDURE));
    configuration.setLocale("en-US");
    configuration.setVerbose(false);
    configuration.setIncludeEvidence(true);
    return configuration;
}

/**
 * Retrieves the RadiologyInsightsInferenceOptions object with the specified
 * options.
 *
 * @return The RadiologyInsightsInferenceOptions object with the specified
 *         options.
 */
private static RadiologyInsightsInferenceOptions getRadiologyInsightsInferenceOptions() {
    RadiologyInsightsInferenceOptions inferenceOptions = new RadiologyInsightsInferenceOptions();
    FollowupRecommendationOptions followupOptions = new FollowupRecommendationOptions();
    FindingOptions findingOptions = new FindingOptions();
    followupOptions.setIncludeRecommendationsWithNoSpecifiedModality(true);
    followupOptions.setIncludeRecommendationsInReferences(true);
    followupOptions.setProvideFocusedSentenceEvidence(true);
    findingOptions.setProvideFocusedSentenceEvidence(true);
    inferenceOptions.setFollowupRecommendationOptions(followupOptions);
    inferenceOptions.setFindingOptions(findingOptions);
    return inferenceOptions;
}
```

### Get Critical Result Inference information

Display critical result inferences from the example request results.

```java com.azure.health.insights.radiologyinsights.displayresults
List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
for (RadiologyInsightsPatientResult patientResult : patientResults) {
    List<FhirR4Extendible1> inferences = patientResult.getInferences();
    for (FhirR4Extendible1 inference : inferences) {
        if (inference instanceof CriticalResultInference) {
            CriticalResultInference criticalResultInference = (CriticalResultInference) inference;
            String description = criticalResultInference.getResult().getDescription();
            System.out.println("Critical Result Inference found: " + description);
        }
    }
}
```

## Troubleshooting

## Next steps
Explore the complete set of [sample source code files][samples_location].

## Additional documentation
For more extensive documentation on Azure Health Insights Radiology Insights, see the [Radiology Insights documentation][radiology_insights_docs] on learn.microsoft.com.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[health_insights]: https://learn.microsoft.com/azure/azure-health-insights/overview
[radiology_insights_docs]: https://learn.microsoft.com/azure/azure-health-insights/radiology-insights/overview
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[cognitive_resource_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli
[azure_cli]: https://docs.microsoft.com/cli/azure
[radiology_insights_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/main/java/com/azure/health/insights/radiologyinsights/RadiologyInsightsClient.java
[ri_sync_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleCriticalResultInferenceSync.java
[ri_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleCriticalResultInferenceAsync.java
[product_documentation]: https://learn.microsoft.com/azure/azure-health-insights/radiology-insights/
[radiology_insights_inferences]: https://learn.microsoft.com/azure/azure-health-insights/radiology-insights/inferences
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_portal]: https://portal.azure.com
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/
[package]: https://central.sonatype.com/artifact/com.azure/azure-health-insights-radiologyinsights
[samples_location]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples

![Impressions]: https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fhealthinsights%2Fazure-health-insights-radiologyinsights%2FREADME.png
