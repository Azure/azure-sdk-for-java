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
    <version>1.0.0</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Health Insights Radiology Insights service, you'll need to create an instance of the [`RadiologyInsightsClient`][radiology_insights_client_class] class.  You can either use the **DefaultAzureCredential** or an **API key** to instantiate a client object.

You need an **endpoint**, which you can obtain from the resource information in the [Azure Portal][azure_portal]. Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the endpoint from the Health Insights resource.

```bash
az cognitiveservices account show --resource-group <your-resource-group-name> --name <your-resource-name> | jq -r .properties.endpoint
```

#### Authenticate and create a RadiologyInsightsClient with DefaultAzureCredential

This sample code shows how to to use DefaultAzureCredential to authenticate with the service. More documentation about this authentication method can be found [here][azure_credential].

```java com.azure.health.insights.radiologyinsights.defaultazurecredential
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");

DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
RadiologyInsightsClientBuilder clientBuilder = new RadiologyInsightsClientBuilder()
        .endpoint(endpoint)
        .credential(credential);
RadiologyInsightsClient radiologyInsightsClient = clientBuilder.buildClient();
```

Build a **synchronous** client:

```java com.azure.health.insights.radiologyinsights.buildsyncclient
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");

DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
RadiologyInsightsClientBuilder clientBuilder = new RadiologyInsightsClientBuilder()
        .endpoint(endpoint)
        .credential(credential);
RadiologyInsightsClient radiologyInsightsClient = clientBuilder.buildClient();
```

Build an **asynchronous** client:

```java com.azure.health.insights.radiologyinsights.buildasyncclient
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");

DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
RadiologyInsightsClientBuilder clientBuilder = new RadiologyInsightsClientBuilder()
        .endpoint(endpoint)
        .credential(credential);
RadiologyInsightsAsyncClient radiologyInsightsAsyncClient = clientBuilder.buildAsyncClient();
```

## Key concepts

Radiology Insights currently supports one document from one patient. Please take a look [here][radiology_insights_inferences] for more detailed information about the inferences this service produces.  

## Examples

### Create a RadiologyInsights request and retrieve the result

Infer radiology insights from a patient's radiology report using a **synchronous** client.
- [SampleCriticalResultInferenceSync.java][ri_sync_sample]

```java com.azure.health.insights.radiologyinsights.inferradiologyinsightssync
RadiologyInsightsInferenceResult riJobResponse = radiologyInsightsClient.beginInferRadiologyInsights(UUID.randomUUID().toString(), createRadiologyInsightsJob()).getFinalResult();
```

Infer radiology insights from a patient's radiology report using an **asynchronous** client.
- [SampleCriticalResultInferenceAsync.java][ri_async_sample]

```java com.azure.health.insights.radiologyinsights.inferradiologyinsights
PollerFlux<RadiologyInsightsData, RadiologyInsightsInferenceResult> asyncPoller = radiologyInsightsAsyncClient
        .beginInferRadiologyInsights(UUID.randomUUID().toString(), createRadiologyInsightsJob());
```

Create the request.

```java com.azure.health.insights.radiologyinsights.createrequest
private static RadiologyInsightsData createRadiologyInsightsJob() {
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
    
    patientRecord.setDetails(patientDetails);

    PatientEncounter encounter = new PatientEncounter("encounterid1");

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
    OrderedProcedure orderedProcedure = new OrderedProcedure();

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
    patientDocument.setCreatedAt(createdDateTime);

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

private static Mono<RadiologyInsightsInferenceResult> mono = null;

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
    List<RadiologyInsightsInference> inferences = patientResult.getInferences();
    for (RadiologyInsightsInference inference : inferences) {
        if (inference instanceof CriticalResultInference) {
            CriticalResultInference criticalResultInference = (CriticalResultInference) inference;
            String description = criticalResultInference.getResult().getDescription();
            System.out.println("Critical Result Inference found: " + description);                    
        }
    }
}
```

### Get Age Mismatch Inference information

Display age mismatch inferences from the example request results. See [SampleAgeMismatchInferenceAsync.java][ri_async_sample_agemismatch] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.agemismatch
private static void displayAgeMismatches(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof AgeMismatchInference) {
                AgeMismatchInference ageMismatchInference = (AgeMismatchInference) inference;
                System.out.println("Age Mismatch Inference found");
                List<FhirR4Extension> extensions = ageMismatchInference.getExtension();
                System.out.println("   Evidence: " + extractEvidence(extensions));

            }
        }
    }
}

private static String extractEvidence(List<FhirR4Extension> extensions) {
    String evidence = "";
    for (FhirR4Extension extension : extensions) {
        List<FhirR4Extension> subExtensions = extension.getExtension();
        if (subExtensions != null) {
            evidence += extractEvidenceToken(subExtensions) + " ";
        }
    }
    return evidence;
}

private static String extractEvidenceToken(List<FhirR4Extension> subExtensions) {
    String evidence = "";
    int offset = -1;
    int length = -1;
    for (FhirR4Extension iExtension : subExtensions) {
        if (iExtension.getUrl().equals("offset")) {
            offset = iExtension.getValueInteger();
        }
        if (iExtension.getUrl().equals("length")) {
            length = iExtension.getValueInteger();
        }
    }
    if (offset > 0 && length > 0) {
        evidence = DOC_CONTENT.substring(offset, Math.min(offset + length, DOC_CONTENT.length()));
    }
    return evidence; 
}
```

### Get Complete Order Discrepancy Inference information

Display complete order inferences from the example request results. See [SampleCompleteOrderDiscrepancyInferenceAsync.java][ri_async_sample_completeorderdiscrepancy] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.completeorderdiscrepancy
private static void displayCompleteOrderDiscrepancies(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof CompleteOrderDiscrepancyInference) {
                CompleteOrderDiscrepancyInference completeOrderDiscrepancyInference = (CompleteOrderDiscrepancyInference) inference;
                System.out.println("Complete Order Discrepancy Inference found: ");
                FhirR4CodeableConcept orderType = completeOrderDiscrepancyInference.getOrderType();
                displayCodes(orderType, 1);
                List<FhirR4CodeableConcept> missingBodyParts = completeOrderDiscrepancyInference.getMissingBodyParts();
                System.out.println("   Missing body parts:");
                for (FhirR4CodeableConcept missingBodyPart : missingBodyParts) {
                    displayCodes(missingBodyPart, 2);
                }
                List<FhirR4CodeableConcept> missingBodyPartMeasurements = completeOrderDiscrepancyInference.getMissingBodyPartMeasurements();
                System.out.println("   Missing body part measurements:");
                for (FhirR4CodeableConcept missingBodyPartMeasurement : missingBodyPartMeasurements) {
                    displayCodes(missingBodyPartMeasurement, 2);
                }
            }
        }
    }
}

private static void displayCodes(FhirR4CodeableConcept codeableConcept, int indentation) {
    String initialBlank = "";
    for (int i = 0; i < indentation; i++) {
        initialBlank += "   ";
    }
    if (codeableConcept != null) {
        List<FhirR4Coding> codingList = codeableConcept.getCoding();
        if (codingList != null) {
            for (FhirR4Coding fhirR4Coding : codingList) {
                System.out.println(initialBlank + "Coding: " + fhirR4Coding.getCode() + ", " + fhirR4Coding.getDisplay() + " (" + fhirR4Coding.getSystem() + ")");
            }
        }
    }
}
```

### Get Finding Inference information

Display finding inferences from the example request results. See [SampleFindingInferenceAsync.java][ri_async_sample_finding] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.finding
private static void displayFindings(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof FindingInference) {
                FindingInference findingInference = (FindingInference) inference;
                System.out.println("Finding Inference found");
                FhirR4Observation finding = findingInference.getFinding();
                System.out.println("   Code: ");
                FhirR4CodeableConcept code = finding.getCode();
                displayCodes(code, 2);
                System.out.println("   Interpretation: ");
                List<FhirR4CodeableConcept> interpretationList = finding.getInterpretation();
                if (interpretationList != null) {
                    for (FhirR4CodeableConcept interpretation : interpretationList) {
                        displayCodes(interpretation, 2);
                    }
                }
                System.out.println("   Component: ");
                List<FhirR4ObservationComponent> componentList = finding.getComponent();
                for (FhirR4ObservationComponent component : componentList) {
                    FhirR4CodeableConcept componentCode = component.getCode();
                    displayCodes(componentCode, 2);
                    System.out.println("      Value codeable concept: ");
                    FhirR4CodeableConcept valueCodeableConcept = component.getValueCodeableConcept();
                    displayCodes(valueCodeableConcept, 4);
                }
                displaySectionInfo(findingInference);
            }
        }
    }
}

private static void displaySectionInfo(FindingInference findingInference) {
    List<FhirR4Extension> extensionList = findingInference.getExtension();
    if (extensionList != null) {
        for (FhirR4Extension extension : extensionList) {
            if (extension.getUrl() != null && extension.getUrl().equals("section")) {
                System.out.println("   Section:");
                List<FhirR4Extension> subextensionList = extension.getExtension();
                if (subextensionList != null) {
                    for (FhirR4Extension subextension : subextensionList) {
                        System.out.println("      " + subextension.getUrl() + ": " + subextension.getValueString());
                    }
                }
            }
        }
    }
}

private static void displayCodes(FhirR4CodeableConcept codeableConcept, int indentation) {
    String initialBlank = "";
    for (int i = 0; i < indentation; i++) {
        initialBlank += "   ";
    }
    if (codeableConcept != null) {
        List<FhirR4Coding> codingList = codeableConcept.getCoding();
        if (codingList != null) {
            for (FhirR4Coding fhirR4Coding : codingList) {
                System.out.println(initialBlank + "Coding: " + fhirR4Coding.getCode() + ", " + fhirR4Coding.getDisplay() + " (" + fhirR4Coding.getSystem() + ")");
            }
        }
    }
}
```

### Get Followup Communication Inference information

Display followup communication inferences from the example request results. See [SampleFollowupCommunicationAsync.java][ri_async_sample_followupcommunication] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.followupcommunication
private static void displayFollowupCommunications(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof FollowupCommunicationInference) {
                System.out.println("Followup Communication Inference found");
                FollowupCommunicationInference followupCommunicationInference = (FollowupCommunicationInference) inference;
                System.out.println("   Communicated at: ");
                List<OffsetDateTime> dateTimeList = followupCommunicationInference.getCommunicatedAt();
                if (dateTimeList != null) {
                    for (OffsetDateTime dateTime : dateTimeList) {
                        System.out.println("      " + dateTime);
                    }
                }
                System.out.println("   Recipient: ");
                List<MedicalProfessionalType> recipientList = followupCommunicationInference.getRecipient();
                for (MedicalProfessionalType recipient : recipientList) {
                    System.out.println("      " + recipient);
                }
                System.out.println("   Acknowledged: " + followupCommunicationInference.isAcknowledged());
            }
        }
    }
}
```

### Get Followup Recommendation Inference information

Display followup recommendation inferences from the example request results. See [SampleFollowupRecommendationInferenceAsync.java][ri_async_sample_followuprecommendation] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.followuprecommendation
private static void displayFollowUpRecommendations(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            
            if (inference instanceof FollowupRecommendationInference) {
                FollowupRecommendationInference followupRecommendationInference = (FollowupRecommendationInference) inference;
                System.out.println("Follow Up Recommendation Inference found: ");
                List<FhirR4Extension> extensions = followupRecommendationInference.getExtension();
                System.out.println("   Evidence: " + extractEvidence(extensions));
                System.out.println("   Is conditional: " + followupRecommendationInference.isConditional());
                System.out.println("   Is guideline: " + followupRecommendationInference.isGuideline());
                System.out.println("   Is hedging: " + followupRecommendationInference.isHedging());
                System.out.println("   Is option: " + followupRecommendationInference.isOption());
                
                ProcedureRecommendation recommendedProcedure = followupRecommendationInference.getRecommendedProcedure();
                if (recommendedProcedure instanceof GenericProcedureRecommendation) {
                    System.out.println("   Generic procedure recommendation:");
                    GenericProcedureRecommendation genericProcedureRecommendation = (GenericProcedureRecommendation) recommendedProcedure;
                    System.out.println("      Procedure codes: ");
                    FhirR4CodeableConcept code = genericProcedureRecommendation.getCode();
                    displayCodes(code, 3);
                }
                if (recommendedProcedure instanceof ImagingProcedureRecommendation) {
                    System.out.println("   Imaging procedure recommendation: ");
                    ImagingProcedureRecommendation imagingProcedureRecommendation = (ImagingProcedureRecommendation) recommendedProcedure;
                    System.out.println("      Procedure codes: ");
                    List<FhirR4CodeableConcept> procedureCodes = imagingProcedureRecommendation.getProcedureCodes();
                    if (procedureCodes != null) {
                        for (FhirR4CodeableConcept codeableConcept : procedureCodes) {
                            displayCodes(codeableConcept, 3);    
                        }
                    }
                    
                    System.out.println("      Imaging procedure: ");
                    List<ImagingProcedure> imagingProcedures = imagingProcedureRecommendation.getImagingProcedures();
                    for (ImagingProcedure imagingProcedure : imagingProcedures) {
                        System.out.println("         Modality");
                        FhirR4CodeableConcept modality = imagingProcedure.getModality();
                        displayCodes(modality, 4);
                        System.out.println("            Evidence: " + extractEvidence(modality.getExtension()));
                        
                        System.out.println("         Anatomy");
                        FhirR4CodeableConcept anatomy = imagingProcedure.getAnatomy();
                        displayCodes(anatomy, 4);
                        System.out.println("            Evidence: " + extractEvidence(anatomy.getExtension()));
                    }
                } 
            }
        }
    }
}

private static void displayCodes(FhirR4CodeableConcept codeableConcept, int indentation) {
    String initialBlank = "";
    for (int i = 0; i < indentation; i++) {
        initialBlank += "   ";
    }
    if (codeableConcept != null) {
        List<FhirR4Coding> codingList = codeableConcept.getCoding();
        if (codingList != null) {
            for (FhirR4Coding fhirR4Coding : codingList) {
                System.out.println(initialBlank + "Coding: " + fhirR4Coding.getCode() + ", " + fhirR4Coding.getDisplay() + " (" + fhirR4Coding.getSystem() + ")");
            }
        }
    }
}

private static String extractEvidence(List<FhirR4Extension> extensions) {
    String evidence = "";
    if (extensions != null) {
        for (FhirR4Extension extension : extensions) {
            List<FhirR4Extension> subExtensions = extension.getExtension();
            if (subExtensions != null) {
                evidence += extractEvidenceToken(subExtensions) + " ";
            }
        }
    }
    return evidence;
}

private static String extractEvidenceToken(List<FhirR4Extension> subExtensions) {
    String evidence = "";
    int offset = -1;
    int length = -1;
    for (FhirR4Extension iExtension : subExtensions) {
        if (iExtension.getUrl().equals("offset")) {
            offset = iExtension.getValueInteger();
        }
        if (iExtension.getUrl().equals("length")) {
            length = iExtension.getValueInteger();
        }
    }
    if (offset > 0 && length > 0) {
        //System.out.println("Offset: " + offset + ", length: " + length);
        evidence = DOC_CONTENT.substring(offset, Math.min(offset + length, DOC_CONTENT.length()));
    }
    return evidence; 
}
```

### Get Laterality Discrepancy Inference information

Display laterality inferences from the example request results. See [SampleLateralityDiscrepancyInferenceAsync.java][ri_async_sample_lateralitydiscrepancy] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.lateralitydiscrepancy
private static void displayLateralityDiscrepancies(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof LateralityDiscrepancyInference) {
                LateralityDiscrepancyInference lateralityDiscrepancyInference = (LateralityDiscrepancyInference) inference;
                FhirR4CodeableConcept lateralityIndication = lateralityDiscrepancyInference.getLateralityIndication();
                List<FhirR4Coding> codingList = lateralityIndication.getCoding();
                System.out.println("Laterality Discrepancy Inference found: ");
                LateralityDiscrepancyType discrepancyType = lateralityDiscrepancyInference.getDiscrepancyType();
                for (FhirR4Coding fhirR4Coding : codingList) {
                    System.out.println("   Coding: " + fhirR4Coding.getCode() + ", " + fhirR4Coding.getDisplay() + " (" + fhirR4Coding.getSystem() + "), type: " + discrepancyType.toString());
                }
            }
        }
    }
}
```

### Get Limited Order Discrepancy Inference information

Display limited order inferences from the example request results. See [SampleLimitedOrderDiscrepancyInferenceAsync.java][ri_async_sample_limitedorderdiscrepancy] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.limitedorderdiscrepancy
private static void displayLimitedOrderDiscrepancies(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof LimitedOrderDiscrepancyInference) {
                LimitedOrderDiscrepancyInference limitedOrderDiscrepancyInference = (LimitedOrderDiscrepancyInference) inference;
                System.out.println("Limited Order Discrepancy Inference found: ");
                FhirR4CodeableConcept orderType = limitedOrderDiscrepancyInference.getOrderType();
                displayCodes(orderType, 1);
                List<FhirR4CodeableConcept> missingBodyParts = limitedOrderDiscrepancyInference.getPresentBodyParts();
                System.out.println("   Present body parts:");
                for (FhirR4CodeableConcept missingBodyPart : missingBodyParts) {
                    displayCodes(missingBodyPart, 2);
                }
                List<FhirR4CodeableConcept> missingBodyPartMeasurements = limitedOrderDiscrepancyInference.getPresentBodyPartMeasurements();
                System.out.println("   Present body part measurements:");
                for (FhirR4CodeableConcept missingBodyPartMeasurement : missingBodyPartMeasurements) {
                    displayCodes(missingBodyPartMeasurement, 2);
                }
            }
        }
    }
}

private static void displayCodes(FhirR4CodeableConcept codeableConcept, int indentation) {
    String initialBlank = "";
    for (int i = 0; i < indentation; i++) {
        initialBlank += "   ";
    }
    if (codeableConcept != null) {
        List<FhirR4Coding> codingList = codeableConcept.getCoding();
        if (codingList != null) {
            for (FhirR4Coding fhirR4Coding : codingList) {
                System.out.println(initialBlank + "Coding: " + fhirR4Coding.getCode() + ", " + fhirR4Coding.getDisplay() + " (" + fhirR4Coding.getSystem() + ")");
            }
        }
    }
}
```

### Get Radiology Procedure Inference information

Display radiology procedure inferences from the example request results. See [SampleRadiologyProcedureInferenceAsync.java][ri_async_sample_radiologyprocedure] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.radiologyprocedure
private static void displayRadiologyProcedures(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof RadiologyProcedureInference) {
                RadiologyProcedureInference radiologyProcedureInference = (RadiologyProcedureInference) inference;
                System.out.println("Radiology Procedure Inference found");
                System.out.println("   Procedure codes:");
                List<FhirR4CodeableConcept> procedureCodes = radiologyProcedureInference.getProcedureCodes();
                for (FhirR4CodeableConcept procedureCode : procedureCodes) {
                    displayCodes(procedureCode, 2);
                }
                System.out.println("   Imaging procedures:");
                List<ImagingProcedure> imagingProcedures = radiologyProcedureInference.getImagingProcedures();
                
                for (ImagingProcedure imagingProcedure : imagingProcedures) {
                    System.out.println("      Modality: ");
                    FhirR4CodeableConcept modality = imagingProcedure.getModality();
                    displayCodes(modality, 3);
                    System.out.println("      Anatomy: ");
                    FhirR4CodeableConcept anatomy = imagingProcedure.getAnatomy();
                    displayCodes(anatomy, 3);
                    System.out.println("      Laterality: ");
                    FhirR4CodeableConcept laterality = imagingProcedure.getLaterality();
                    displayCodes(laterality, 3);
                }
                System.out.println("   Ordered procedures:");
                OrderedProcedure orderedProcedure = radiologyProcedureInference.getOrderedProcedure();
                FhirR4CodeableConcept code = orderedProcedure.getCode();
                displayCodes(code, 2);
                System.out.println("   Description: " + orderedProcedure.getDescription());
            }
        }
    }
}

private static void displayCodes(FhirR4CodeableConcept codeableConcept, int indentation) {
    String initialBlank = "";
    for (int i = 0; i < indentation; i++) {
        initialBlank += "   ";
    }
    if (codeableConcept != null) {
        List<FhirR4Coding> codingList = codeableConcept.getCoding();
        if (codingList != null) {
            for (FhirR4Coding fhirR4Coding : codingList) {
                System.out.println(initialBlank + "Coding: " + fhirR4Coding.getCode() + ", " + fhirR4Coding.getDisplay() + " (" + fhirR4Coding.getSystem() + ")");
            }
        }
    }
}
```

### Get Sex Mismatch Inference information

Display sex mismatch inferences from the example request results. See [SampleSexMismatchInferenceAsync.java][ri_async_sample_sexmismatch] for the complete code

```java com.azure.health.insights.radiologyinsights.displayresults.sexmismatch
private static void displaySexMismatches(RadiologyInsightsInferenceResult radiologyInsightsResult) {
    List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
    for (RadiologyInsightsPatientResult patientResult : patientResults) {
        List<RadiologyInsightsInference> inferences = patientResult.getInferences();
        for (RadiologyInsightsInference inference : inferences) {
            if (inference instanceof SexMismatchInference) {
                SexMismatchInference sexMismatchInference = (SexMismatchInference) inference;
                FhirR4CodeableConcept sexIndication = sexMismatchInference.getSexIndication();
                List<FhirR4Coding> codingList = sexIndication.getCoding();
                System.out.println("Sex Mismatch Inference found: ");
                for (FhirR4Coding fhirR4Coding : codingList) {
                    System.out.println("   Coding: " + fhirR4Coding.getSystem() + ", " + fhirR4Coding.getCode() + ", " + fhirR4Coding.getDisplay());
                }
            }
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
[ri_async_sample_agemismatch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleAgeMismatchInferenceAsync.java
[ri_async_sample_completeorderdiscrepancy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleCompleteOrderDiscrepancyInferenceAsync.java
[ri_async_sample_finding]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleFindingInferenceAsync.java
[ri_async_sample_followupcommunication]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleFollowupCommunicationAsync.java
[ri_async_sample_followuprecommendation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleFollowupRecommendationInferenceAsync.java
[ri_async_sample_lateralitydiscrepancy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleLateralityDiscrepancyInferenceAsync.java
[ri_async_sample_limitedorderdiscrepancy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleLimitedOrderDiscrepancyInferenceAsync.java
[ri_async_sample_radiologyprocedure]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleRadiologyProcedureInferenceAsync.java
[ri_async_sample_sexmismatch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples/java/com/azure/health/insights/radiologyinsights/SampleSexMismatchInferenceAsync.java
[product_documentation]: https://learn.microsoft.com/azure/azure-health-insights/radiology-insights/
[radiology_insights_inferences]: https://learn.microsoft.com/azure/azure-health-insights/radiology-insights/inferences
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_portal]: https://portal.azure.com
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/
[package]: https://central.sonatype.com/artifact/com.azure/azure-health-insights-radiologyinsights
[samples_location]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthinsights/azure-health-insights-radiologyinsights/src/samples
[azure_credential]: https://learn.microsoft.com/java/api/com.azure.identity.defaultazurecredential

![Impressions]: https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fhealthinsights%2Fazure-health-insights-radiologyinsights%2FREADME.png

