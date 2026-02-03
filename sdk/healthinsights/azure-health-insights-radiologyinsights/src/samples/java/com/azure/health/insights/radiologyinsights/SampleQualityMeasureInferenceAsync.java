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
import com.azure.health.insights.radiologyinsights.models.FindingOptions;
import com.azure.health.insights.radiologyinsights.models.FollowupRecommendationOptions;
import com.azure.health.insights.radiologyinsights.models.GuidanceOptions;
import com.azure.health.insights.radiologyinsights.models.OrderedProcedure;
import com.azure.health.insights.radiologyinsights.models.PatientDetails;
import com.azure.health.insights.radiologyinsights.models.PatientDocument;
import com.azure.health.insights.radiologyinsights.models.PatientEncounter;
import com.azure.health.insights.radiologyinsights.models.PatientRecord;
import com.azure.health.insights.radiologyinsights.models.PatientSex;
import com.azure.health.insights.radiologyinsights.models.QualityMeasureComplianceType;
import com.azure.health.insights.radiologyinsights.models.QualityMeasureInference;
import com.azure.health.insights.radiologyinsights.models.QualityMeasureOptions;
import com.azure.health.insights.radiologyinsights.models.QualityMeasureType;
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
 * The SampleQualityMeasureInferenceAsync class processes a sample radiology document
 * with the Radiology Insights service. It will initialize an asynchronous
 * RadiologyInsightsAsyncClient, build a Radiology Insights request with the sample document, poll the
 * results and display the Quality Measures extracted by the Radiology Insights service.
 */
public class SampleQualityMeasureInferenceAsync {

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

    private static Mono<RadiologyInsightsInferenceResult> mono = null;

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
            displayQualityMeasureInference(radiologyInsightsResult);
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
        LocalDateTime dateTime = LocalDateTime.parse("1939-05-25T19:00:00+00:00", formatter);
        patientDetails.setBirthDate(dateTime.toLocalDate());

        patientRecord.setDetails(patientDetails);

        PatientEncounter encounter = new PatientEncounter("encounterid1");

        TimePeriod period = new TimePeriod();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-M-d'T'HH:mm:ssXXX");

        OffsetDateTime startTime = OffsetDateTime.parse("2014-2-20T00:00:00" + "+00:00", formatter2);
        OffsetDateTime endTime = OffsetDateTime.parse("2021-2-20T00:00:00" + "+00:00", formatter2);

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

        OffsetDateTime createdDateTime = OffsetDateTime.parse("2014-02-20T00:00:00.000" + "+00:00", formatter3);
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
            RadiologyInsightsInferenceType.RADIOLOGY_PROCEDURE, RadiologyInsightsInferenceType.GUIDANCE,
            RadiologyInsightsInferenceType.QUALITY_MEASURE, RadiologyInsightsInferenceType.SCORING_AND_ASSESSMENT));
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
        GuidanceOptions guidanceOptions = new GuidanceOptions(true);
        QualityMeasureOptions qualityMeasureOptions = new QualityMeasureOptions(Arrays.asList(QualityMeasureType.MIPS_364, QualityMeasureType.MIPS_360, QualityMeasureType.MIPS_436));
        followupOptions.setIncludeRecommendationsWithNoSpecifiedModality(true);
        followupOptions.setIncludeRecommendationsInReferences(true);
        followupOptions.setProvideFocusedSentenceEvidence(true);
        findingOptions.setProvideFocusedSentenceEvidence(true);
        inferenceOptions.setFollowupRecommendationOptions(followupOptions);
        inferenceOptions.setFindingOptions(findingOptions);
        inferenceOptions.setGuidanceOptions(guidanceOptions);
        inferenceOptions.setQualityMeasureOptions(qualityMeasureOptions);
        return inferenceOptions;
    }

    /**
     * Display the quality measures  of the Radiology Insights request.
     *
     * @param radiologyInsightsResult The response for the Radiology Insights
     *                                request.
     */
    // BEGIN: com.azure.health.insights.radiologyinsights.displayresults.qualitymeasure
    private static void displayQualityMeasureInference(RadiologyInsightsInferenceResult radiologyInsightsResult) {
        List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
        for (RadiologyInsightsPatientResult patientResult : patientResults) {
            List<RadiologyInsightsInference> inferences = patientResult.getInferences();
            for (RadiologyInsightsInference inference : inferences) {
                if (inference instanceof QualityMeasureInference) {
                    QualityMeasureInference qualityMeasureInference = (QualityMeasureInference) inference;
                    System.out.println("Quality Measure Inference found");
                    // Extract qualityMeasureDenominator
                    String qualityMeasureDenominator = qualityMeasureInference.getQualityMeasureDenominator();
                    System.out.println("QualityMeasureDenominator: " + qualityMeasureDenominator);
                    // Extract qualityMeasureComplianceType
                    QualityMeasureComplianceType qualityMeasureComplianceType = qualityMeasureInference.getComplianceType();
                    System.out.println("QualityMeasureComplianceType: " + qualityMeasureComplianceType.getValue());
                    // Extract Quality Criteria
                    List<String> qualityCriteriaList = qualityMeasureInference.getQualityCriteria();
                    qualityCriteriaList.forEach(qualityCriteria -> System.out.println("QualityCriteria: " + qualityCriteria));
                }
            }
        }
    }
    // END: com.azure.health.insights.radiologyinsights.displayresults.qualitymeasure

    private static Predicate<AsyncPollResponse<RadiologyInsightsJob, RadiologyInsightsInferenceResult>> isComplete = response -> {
        return response.getStatus() != LongRunningOperationStatus.IN_PROGRESS
            && response.getStatus() != LongRunningOperationStatus.NOT_STARTED;
    };
}
