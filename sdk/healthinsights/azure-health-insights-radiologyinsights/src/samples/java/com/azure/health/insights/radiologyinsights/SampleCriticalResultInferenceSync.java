// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.azure.core.util.Configuration;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentType;
import com.azure.health.insights.radiologyinsights.models.CriticalResultInference;
import com.azure.health.insights.radiologyinsights.models.DocumentAdministrativeMetadata;
import com.azure.health.insights.radiologyinsights.models.DocumentAuthor;
import com.azure.health.insights.radiologyinsights.models.DocumentContent;
import com.azure.health.insights.radiologyinsights.models.DocumentContentSourceType;
import com.azure.health.insights.radiologyinsights.models.DocumentType;
import com.azure.health.insights.radiologyinsights.models.EncounterClass;
import com.azure.health.insights.radiologyinsights.models.FhirR4CodeableConcept;
import com.azure.health.insights.radiologyinsights.models.FhirR4Coding;
import com.azure.health.insights.radiologyinsights.models.FindingOptions;
import com.azure.health.insights.radiologyinsights.models.FollowupRecommendationOptions;
import com.azure.health.insights.radiologyinsights.models.OrderedProcedure;
import com.azure.health.insights.radiologyinsights.models.PatientDetails;
import com.azure.health.insights.radiologyinsights.models.PatientDocument;
import com.azure.health.insights.radiologyinsights.models.PatientEncounter;
import com.azure.health.insights.radiologyinsights.models.PatientRecord;
import com.azure.health.insights.radiologyinsights.models.PatientSex;
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

/**
 * The SampleCriticalResultInferenceSync class processes a sample radiology document 
 * with the Radiology Insights service. It will initialize a synchronous 
 * RadiologyInsightsClient, build a Radiology Insights job request with the sample document, submit it to the client 
 * and display the Critical Results extracted by the Radiology Insights service.  
 * 
 */
public class SampleCriticalResultInferenceSync {

    private static final String DOC_CONTENT = "CLINICAL HISTORY:   "
            + "\r\n20-year-old female presenting with abdominal pain. Surgical history significant for appendectomy."
            + "\r\n "
            + "\r\nCOMPARISON:   "
            + "\r\nRight upper quadrant sonographic performed 1 day prior."
            + "\r\n "
            + "\r\nTECHNIQUE:   "
            + "\r\nTransabdominal grayscale pelvic sonography with duplex color Doppler "
            + "\r\nand spectral waveform analysis of the ovaries."
            + "\r\n "
            + "\r\nFINDINGS:   "
            + "\r\nThe uterus is unremarkable given the transabdominal technique with "
            + "\r\nendometrial echo complex within physiologic normal limits. The "
            + "\r\novaries are symmetric in size, measuring 2.5 x 1.2 x 3.0 cm and the "
            + "\r\nleft measuring 2.8 x 1.5 x 1.9 cm.\n \r\nOn duplex imaging, Doppler signal is symmetric."
            + "\r\n "
            + "\r\nIMPRESSION:   "
            + "\r\n1. Normal pelvic sonography. Findings of testicular torsion."
            + "\r\n\nA new US pelvis within the next 6 months is recommended."
            + "\n\nThese results have been discussed with Dr. Jones at 3 PM on November 5 2020.\n "
            + "\r\n";

    /**
     * The main method is the entry point for the application. It initializes and uses
     * the RadiologyInsightsClient to perform Radiology Insights operations.
     *
     * @param args The command-line arguments passed to the program.
     */
    public static void main(final String[] args) throws InterruptedException {
        // BEGIN: com.azure.health.insights.radiologyinsights.buildsyncclient
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");
        
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        RadiologyInsightsClientBuilder clientBuilder = new RadiologyInsightsClientBuilder()
                .endpoint(endpoint)
                .credential(credential);
        RadiologyInsightsClient radiologyInsightsClient = clientBuilder.buildClient();
        // END: com.azure.health.insights.radiologyinsights.buildsyncclient
        
        // BEGIN: com.azure.health.insights.radiologyinsights.inferradiologyinsightssync
        RadiologyInsightsInferenceResult riJobResponse = radiologyInsightsClient.beginInferRadiologyInsights(UUID.randomUUID().toString(), createRadiologyInsightsJob()).getFinalResult();
        // END: com.azure.health.insights.radiologyinsights.inferradiologyinsightssync

        displayCriticalResults(riJobResponse);
    }

    /**
     * Display the critical results of the Radiology Insights job request.
     *
     * @param radiologyInsightsResult The response for the Radiology Insights
     *                                request.
     */
    private static void displayCriticalResults(RadiologyInsightsInferenceResult radiologyInsightsResult) {
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
    }

    /**
     * Creates a RadiologyInsightsJob object to use in the Radiology Insights job
     * request.
     *
     * @return A RadiologyInsightsJob object with the created patient records and
     *         model configuration.
     */
    private static RadiologyInsightsJob createRadiologyInsightsJob() {
        List<PatientRecord> patientRecords = createPatientRecords();
        RadiologyInsightsData radiologyInsightsData = new RadiologyInsightsData(patientRecords);
        RadiologyInsightsModelConfiguration modelConfiguration = createRadiologyInsightsModelConfig();
        radiologyInsightsData.setConfiguration(modelConfiguration);
        RadiologyInsightsJob radiologyInsightsJob = new RadiologyInsightsJob();
        radiologyInsightsJob.setJobData(radiologyInsightsData);
        return radiologyInsightsJob;
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
}
