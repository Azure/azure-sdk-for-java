// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import com.azure.core.util.Configuration;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;

import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentAuthor;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentContent;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentContentType;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentType;
import com.azure.health.insights.radiologyinsights.models.DocumentAdministrativeMetadata;
import com.azure.health.insights.radiologyinsights.models.DocumentContentSourceType;
import com.azure.health.insights.radiologyinsights.models.EncounterClass;
import com.azure.health.insights.radiologyinsights.models.FhirR4CodeableConcept;
import com.azure.health.insights.radiologyinsights.models.FhirR4Coding;
import com.azure.health.insights.radiologyinsights.models.FhirR4Extension;
import com.azure.health.insights.radiologyinsights.models.FhirR4Observation;
import com.azure.health.insights.radiologyinsights.models.FhirR4ObservationComponent;
import com.azure.health.insights.radiologyinsights.models.FindingInference;
import com.azure.health.insights.radiologyinsights.models.FindingOptions;
import com.azure.health.insights.radiologyinsights.models.FollowupRecommendationInference;
import com.azure.health.insights.radiologyinsights.models.FollowupRecommendationOptions;
import com.azure.health.insights.radiologyinsights.models.GenericProcedureRecommendation;
import com.azure.health.insights.radiologyinsights.models.GuidanceInference;
import com.azure.health.insights.radiologyinsights.models.GuidanceRankingType;
import com.azure.health.insights.radiologyinsights.models.ImagingProcedure;
import com.azure.health.insights.radiologyinsights.models.ImagingProcedureRecommendation;
import com.azure.health.insights.radiologyinsights.models.OrderedProcedure;
import com.azure.health.insights.radiologyinsights.models.PatientDetails;
import com.azure.health.insights.radiologyinsights.models.PatientDocument;
import com.azure.health.insights.radiologyinsights.models.PatientEncounter;
import com.azure.health.insights.radiologyinsights.models.PatientRecord;
import com.azure.health.insights.radiologyinsights.models.PatientSex;
import com.azure.health.insights.radiologyinsights.models.PresentGuidanceInformation;
import com.azure.health.insights.radiologyinsights.models.ProcedureRecommendation;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsData;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceOptions;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsJob;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsModelConfiguration;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;
import com.azure.health.insights.radiologyinsights.models.SpecialtyType;
import com.azure.health.insights.radiologyinsights.models.TimePeriod;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * The SampleGuidanceInferenceAsync class processes a sample radiology document
 * with the Radiology Insights service. It will initialize an asynchronous
 * RadiologyInsightsAsyncClient, build a Radiology Insights request with the sample document, poll the
 * results and display the Guidance extracted by the Radiology Insights service.
 */
public class SampleGuidanceInferenceAsync {

    private static final String DOC_CONTENT = "EXAM: CT CHEST WO CONTRAST\r\n"
        + "\r\n"
        + "INDICATION: abnormal lung findings. History of emphysema.\r\n"
        + "\r\n"
        + "TECHNIQUE: Helical CT images through the chest, without contrast. This exam was performed using one or more of the following dose reduction techniques: Automated exposure control, adjustment of the mA and/or kV according to patient size, and/or use of iterative reconstruction technique. \r\n"
        + "\r\n"
        + "COMPARISON: Chest CT dated 6/21/2022.\r\n"
        + "\r\n"
        + "Number of previous CT examinations or cardiac nuclear medicine (myocardial perfusion) examinations performed in the preceding 12-months: 2\r\n"
        + "\r\n"
        + "FINDINGS: \r\n"
        + "\r\n"
        + "Heart size is normal. No pericardial effusion. Thoracic aorta as well as pulmonary arteries are normal in caliber. There are dense coronary artery calcifications. No enlarged axillary, mediastinal, or hilar lymph nodes by CT size criteria. Central airways are widely patent. No bronchial wall thickening. No pneumothorax, pleural effusion or pulmonary edema. The previously identified posterior right upper lobe nodules are no longer seen. However, there are multiple new small pulmonary nodules. An 8 mm nodule in the right upper lobe, image #15 series 4. New posterior right upper lobe nodule measuring 6 mm, image #28 series 4. New 1.2 cm pulmonary nodule, right upper lobe, image #33 series 4. New 4 mm pulmonary nodule left upper lobe, image #22 series 4. New 8 mm pulmonary nodule in the left upper lobe adjacent to the fissure, image #42 series 4. A few new tiny 2 to 3 mm pulmonary nodules are also noted in the left lower lobe. As before there is a background of severe emphysema. No evidence of pneumonia.\r\n"
        + "\r\n"
        + "Limited evaluation of the upper abdomen shows no concerning abnormality.\r\n"
        + "\r\n"
        + "Review of bone windows shows no aggressive appearing osseous lesions.\r\n"
        + "\r\n"
        + "\r\n"
        + "IMPRESSION:\r\n"
        + "\r\n"
        + "1. Previously identified small pulmonary nodules in the right upper lobe have resolved, but there are multiple new small nodules scattered throughout both lungs. Recommend short-term follow-up with noncontrast chest CT in 3 months as per current  Current guidelines (2017 Fleischner Society).\r\n"
        + "2. Severe emphysema.\r\n"
        + "\r\n"
        + "Findings communicated to Dr. Jane Smith.";


    /**
     * The main method is the entry point for the application. It initializes and uses
     * the RadiologyInsightsAsyncClient to perform Radiology Insights operations.
     *
     * @param args The command-line arguments passed to the program.
     */
    public static void main(final String[] args) throws InterruptedException {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        RadiologyInsightsClientBuilder clientBuilder = new RadiologyInsightsClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .serviceVersion(RadiologyInsightsServiceVersion.getLatest());
        RadiologyInsightsAsyncClient radiologyInsightsAsyncClient = clientBuilder.buildAsyncClient();

        PollerFlux<RadiologyInsightsJob, RadiologyInsightsInferenceResult> asyncPoller = radiologyInsightsAsyncClient
            .beginInferRadiologyInsights(UUID.randomUUID().toString(), createRadiologyInsightsJob());

        CountDownLatch latch = new CountDownLatch(1);

        asyncPoller
            .takeUntil(isComplete)
            .doFinally(signal -> {
                latch.countDown();
            })
            .subscribe(completedResult -> {
                if (completedResult.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
                    System.out.println("Completed poll response, status: " + completedResult.getStatus());
                    mono = completedResult.getFinalResult();
                }
            }, error -> {
                System.err.println(error.getMessage());
                error.printStackTrace();
            });

        latch.await();
        mono.subscribe(radiologyInsightsResult -> {
            // Process the result asynchronously
            displayGuidanceInference(radiologyInsightsResult);
        }, error -> {
            // Handle any errors
            System.err.println("Error occurred: " + error.getMessage());
            error.printStackTrace();
        });
        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Mono<RadiologyInsightsInferenceResult> mono = null;

    /**
     * Display the guidance of the Radiology Insights request.
     *
     * @param radiologyInsightsResult The response for the Radiology Insights
     *                                request.
     */
    // BEGIN: com.azure.health.insights.radiologyinsights.displayresults.guidance
    private static void displayGuidanceInference(RadiologyInsightsInferenceResult radiologyInsightsResult) {
        List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
        for (RadiologyInsightsPatientResult patientResult : patientResults) {
            List<RadiologyInsightsInference> inferences = patientResult.getInferences();
            for (RadiologyInsightsInference inference : inferences) {
                if (inference instanceof GuidanceInference) {
                    GuidanceInference guidanceInference = (GuidanceInference) inference;
                    System.out.println("Guidance Inference found");
                    // Extract identifier
                    FhirR4CodeableConcept identifier = guidanceInference.getIdentifier();
                    System.out.println("Identifier: ");
                    displayCodes(identifier, 1);
                    GuidanceRankingType guidanceRanking = guidanceInference.getRanking();
                    System.out.println("Ranking: " + guidanceRanking.toString());
                    // Extract presentGuidanceInformation
                    if (guidanceInference.getPresentGuidanceInformation() != null) {
                        List<PresentGuidanceInformation> presentGuidanceInformation = guidanceInference.getPresentGuidanceInformation();
                        for (PresentGuidanceInformation presentGuidance : presentGuidanceInformation) {
                            System.out.println("Present Guidance Information: " + presentGuidance.getPresentGuidanceItem());
                        }
                    } else {
                        System.out.println("No Present Guidance Information");
                    }
                    // Extract missingGuidanceInformation
                    if (guidanceInference.getMissingGuidanceInformation() != null) {
                        List<String> missingGuidanceInformation = guidanceInference.getMissingGuidanceInformation();
                        for (String missingGuidance : missingGuidanceInformation) {
                            System.out.println("Missing Guidance Information: " + missingGuidance);
                        }
                    } else {
                        System.out.println("No Missing Guidance Information");
                    }
                    // Extract recommendationProposal
                    List<FollowupRecommendationInference> recommendationProposals = guidanceInference.getRecommendationProposals();
                    if (recommendationProposals != null) {
                        displayFollowUpRecommendations(recommendationProposals);
                    } else {
                        System.out.println("No Recommendation Proposals");
                    }
                    // Extract finding
                    FindingInference finding = guidanceInference.getFinding();
                    if (finding != null) {
                        displayFinding(finding);
                    } else {
                        System.out.println("No Finding Inference");
                    }
                    if (guidanceInference.getFinding() != null) {
                        FindingInference findingInference = guidanceInference.getFinding();
                        displayFinding(findingInference);
                    }
                    if (guidanceInference.getRecommendationProposals() != null) {
                        displayFollowUpRecommendations(guidanceInference.getRecommendationProposals());
                    }
                    List<FhirR4Extension> extensions = guidanceInference.getExtension();
                    if (extensions != null) {
                        System.out.println("   Evidence: " + extractEvidence(extensions));
                    }
                }
            }
        }
    }


    private static void displayFollowUpRecommendations(List<FollowupRecommendationInference> recommendationProposals) {
        for (FollowupRecommendationInference followupRecommendationInference : recommendationProposals) {
            List<FhirR4Extension> extensions = followupRecommendationInference.getExtension();
            if (extensions != null) {
                System.out.println("   Evidence: " + extractEvidence(extensions));
            } else {
                System.out.println("   No evidence found.");
            }
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

    private static void displayFinding(FindingInference findingInference) {
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
    }
    // END: com.azure.health.insights.radiologyinsights.displayresults.guidance

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
        if (extensions == null) {
            evidence = "No evidence found.";
            return evidence;
        }
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

    /**
     * Creates a RadiologyInsightsJob object to use in the Radiology Insights job
     * request.
     *
     * @return A RadiologyInsightsJob object with the created patient records and
     * model configuration.
     */
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
        // Define a formatter that matches the input pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        // Parse the string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse("1959-11-11T19:00:00+00:00", formatter);
        patientDetails.setBirthDate(dateTime.toLocalDate());

        patientRecord.setDetails(patientDetails);

        PatientEncounter encounter = new PatientEncounter("encounterid1");

        TimePeriod period = new TimePeriod();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-M-d'T'HH:mm:ssXXX");

        OffsetDateTime startTime = OffsetDateTime.parse("2021-8-28T00:00:00" + "+00:00", formatter2);
        OffsetDateTime endTime = OffsetDateTime.parse("2021-8-28T00:00:00" + "+00:00", formatter2);

        period.setStart(startTime);
        period.setEnd(endTime);

        encounter.setPeriod(period);
        encounter.setClassProperty(EncounterClass.IN_PATIENT);

        patientRecord.setEncounters(Arrays.asList(encounter));

        PatientDocument patientDocument = getPatientDocument();
        patientDocument.setClinicalType(ClinicalDocumentType.RADIOLOGY_REPORT);
        patientDocument.setLanguage("EN");

        ClinicalDocumentAuthor author = new ClinicalDocumentAuthor();
        author.setId("authorid1");
        author.setFullName("authorname1");

        patientDocument.setAuthors(Arrays.asList(author));
        patientDocument.setSpecialtyType(SpecialtyType.RADIOLOGY);

        DocumentAdministrativeMetadata adminMetadata = new DocumentAdministrativeMetadata();
        OrderedProcedure orderedProcedure = new OrderedProcedure();

        FhirR4CodeableConcept procedureCode = new FhirR4CodeableConcept();
        FhirR4Coding procedureCoding = new FhirR4Coding();
        procedureCoding.setSystem("http://loinc.org");
        procedureCoding.setCode("CTCHWO");
        procedureCoding.setDisplay("CT CHEST WO CONTRAST");

        procedureCode.setCoding(Arrays.asList(procedureCoding));
        orderedProcedure.setCode(procedureCode);
        orderedProcedure.setDescription("CT CHEST WO CONTRAST");

        adminMetadata.setOrderedProcedures(Arrays.asList(orderedProcedure));
        adminMetadata.setEncounterId("encounterid1");

        patientDocument.setAdministrativeMetadata(adminMetadata);

        // Define a formatter to handle milliseconds
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        OffsetDateTime createdDateTime = OffsetDateTime.parse("2021-06-01T00:00:00.000" + "+00:00", formatter3);
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
        ClinicalDocumentContent documentContent = new ClinicalDocumentContent(DocumentContentSourceType.INLINE, DOC_CONTENT);
        return new PatientDocument(ClinicalDocumentContentType.NOTE, "docid1", documentContent);
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
            RadiologyInsightsInferenceType.RADIOLOGY_PROCEDURE, RadiologyInsightsInferenceType.GUIDANCE));
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
     * options.
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

    private static Predicate<AsyncPollResponse<RadiologyInsightsJob, RadiologyInsightsInferenceResult>> isComplete = response -> {
        return response.getStatus() != LongRunningOperationStatus.IN_PROGRESS
            && response.getStatus() != LongRunningOperationStatus.NOT_STARTED;
    };
}
