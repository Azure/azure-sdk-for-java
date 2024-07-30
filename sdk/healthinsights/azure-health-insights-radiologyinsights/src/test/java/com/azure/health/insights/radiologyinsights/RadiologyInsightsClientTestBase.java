// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Configuration;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentType;
import com.azure.health.insights.radiologyinsights.models.DocumentAdministrativeMetadata;
import com.azure.health.insights.radiologyinsights.models.DocumentAuthor;
import com.azure.health.insights.radiologyinsights.models.DocumentContent;
import com.azure.health.insights.radiologyinsights.models.DocumentContentSourceType;
import com.azure.health.insights.radiologyinsights.models.DocumentType;
import com.azure.health.insights.radiologyinsights.models.Encounter;
import com.azure.health.insights.radiologyinsights.models.EncounterClass;
import com.azure.health.insights.radiologyinsights.models.FhirR4CodeableConcept;
import com.azure.health.insights.radiologyinsights.models.FhirR4Coding;
import com.azure.health.insights.radiologyinsights.models.FindingOptions;
import com.azure.health.insights.radiologyinsights.models.FollowupRecommendationOptions;
import com.azure.health.insights.radiologyinsights.models.OrderedProcedure;
import com.azure.health.insights.radiologyinsights.models.PatientDetails;
import com.azure.health.insights.radiologyinsights.models.PatientDocument;
import com.azure.health.insights.radiologyinsights.models.PatientRecord;
import com.azure.health.insights.radiologyinsights.models.PatientSex;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsData;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceOptions;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsModelConfiguration;
import com.azure.health.insights.radiologyinsights.models.SpecialtyType;
import com.azure.health.insights.radiologyinsights.models.TimePeriod;

/**
 * Base class for Radiology Insights clients test.
 */

class RadiologyInsightsClientTestBase extends TestProxyTestBase {

    private static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    void testRadiologyInsightsgWithResponse(Consumer<RadiologyInsightsData> testRunner) {
        testRunner.accept(createRadiologyInsightsRequest());
    }

    RadiologyInsightsClientBuilder getClientBuilder() {
        String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTHINSIGHTS_API_KEY", FAKE_API_KEY);
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTHINSIGHTS_ENDPOINT", "https://localhost:8080");

        RadiologyInsightsClientBuilder builder = new RadiologyInsightsClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey));

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("repeatability-first-sent", "repeatability-request-id"))));
        }
        if (!interceptorManager.isLiveMode()) {
            // Remove `operation-location` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK2030");
        }
        return builder;
    }

    private static RadiologyInsightsData createRadiologyInsightsRequest() {
        List<PatientRecord> patientRecords = createPatientRecords();
        RadiologyInsightsData radiologyInsightsData = new RadiologyInsightsData(patientRecords);
        RadiologyInsightsModelConfiguration modelConfiguration = createRadiologyInsightsModelConfig();
        radiologyInsightsData.setConfiguration(modelConfiguration);
        return radiologyInsightsData;
    }

    private static List<PatientRecord> createPatientRecords() {
        List<PatientRecord> patientRecords = new ArrayList<>();
        // Patients
        PatientRecord patientRecord = new PatientRecord("Sharona");

        PatientDetails patientDetails = new PatientDetails();
        patientDetails.setSex(PatientSex.FEMALE);

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
        OrderedProcedure orderedProcedure = new OrderedProcedure();

        FhirR4CodeableConcept procedureCode = new FhirR4CodeableConcept();
        FhirR4Coding procedureCoding = new FhirR4Coding();
        procedureCoding.setSystem("Http://hl7.org/fhir/ValueSet/cpt-all");
        procedureCoding.setCode("MVLW");
        procedureCoding.setDisplay("IH Hip 1 View Left");

        procedureCode.setCoding(Arrays.asList(procedureCoding));
        orderedProcedure.setCode(procedureCode);
        orderedProcedure.setDescription("IH Hip 1 View Left");

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

    private static PatientDocument getPatientDocument() {
        DocumentContent documentContent = new DocumentContent(DocumentContentSourceType.INLINE, "Findings: There is a total hip prosthesis.");
        return new PatientDocument(DocumentType.NOTE, "docid1", documentContent);
    }

    private static RadiologyInsightsModelConfiguration createRadiologyInsightsModelConfig() {
        RadiologyInsightsModelConfiguration configuration = new RadiologyInsightsModelConfiguration();
        RadiologyInsightsInferenceOptions inferenceOptions = getRadiologyInsightsInferenceOptions();
        configuration.setInferenceOptions(inferenceOptions);
        configuration.setInferenceTypes(Arrays.asList(RadiologyInsightsInferenceType.LATERALITY_DISCREPANCY));
        configuration.setLocale("en-US");
        configuration.setVerbose(false);
        configuration.setIncludeEvidence(true);
        return configuration;
    }

    private static RadiologyInsightsInferenceOptions getRadiologyInsightsInferenceOptions() {
        RadiologyInsightsInferenceOptions inferenceOptions = new RadiologyInsightsInferenceOptions();
        FollowupRecommendationOptions followupOptions = new FollowupRecommendationOptions();
        FindingOptions findingOptions = new FindingOptions();
        followupOptions.setIncludeRecommendationsWithNoSpecifiedModality(false);
        followupOptions.setIncludeRecommendationsInReferences(false);
        followupOptions.setProvideFocusedSentenceEvidence(false);
        findingOptions.setProvideFocusedSentenceEvidence(false);
        inferenceOptions.setFollowupRecommendationOptions(followupOptions);
        inferenceOptions.setFindingOptions(findingOptions);
        return inferenceOptions;
    }
}
