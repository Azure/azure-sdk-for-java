// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentType;
import com.azure.health.insights.radiologyinsights.models.DocumentAdministrativeMetadata;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentAuthor;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentContent;
import com.azure.health.insights.radiologyinsights.models.DocumentContentSourceType;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentContentType;
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
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceOptions;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsModelConfiguration;
import com.azure.health.insights.radiologyinsights.models.SpecialtyType;
import com.azure.health.insights.radiologyinsights.models.TimePeriod;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsInputDataTest {

    @Test
    public void test() {
        setDocumentContent("Test");
        setInferenceType(RadiologyInsightsInferenceType.AGE_MISMATCH);
        setOrderCode("MVLW");
        setOrderDescription("IH Hip 1 View Left");
        RadiologyInsightsData radiologyInsightsRequest = createRadiologyInsightsRequest();

        RadiologyInsightsModelConfiguration configuration = radiologyInsightsRequest.getConfiguration();

        assertTrue(configuration.isIncludeEvidence());
        assertFalse(configuration.isVerbose());
        assertEquals("en-US", configuration.getLocale());

        RadiologyInsightsInferenceOptions inferenceOptions = configuration.getInferenceOptions();

        FindingOptions findingOptions = inferenceOptions.getFindingOptions();
        assertFalse(findingOptions.isProvideFocusedSentenceEvidence());

        FollowupRecommendationOptions followupRecommendationOptions
            = inferenceOptions.getFollowupRecommendationOptions();
        assertFalse(followupRecommendationOptions.isIncludeRecommendationsInReferences());
        assertFalse(followupRecommendationOptions.isIncludeRecommendationsWithNoSpecifiedModality());
        assertFalse(followupRecommendationOptions.isProvideFocusedSentenceEvidence());

        List<RadiologyInsightsInferenceType> inferenceTypes = configuration.getInferenceTypes();
        assertEquals(1, inferenceTypes.size());
        RadiologyInsightsInferenceType inferenceType = inferenceTypes.get(0);
        assertEquals(RadiologyInsightsInferenceType.AGE_MISMATCH, inferenceType);

        List<PatientRecord> patients = radiologyInsightsRequest.getPatients();
        assertEquals(1, patients.size());
        PatientRecord patientRecord = patients.get(0);

        assertEquals("Sharona", patientRecord.getId());

        PatientDetails info = patientRecord.getDetails();
        assertEquals(PatientSex.FEMALE, info.getSex());
        assertNull(info.getClinicalInfo());
        assertEquals(LocalDate.of(1959, 11, 11), info.getBirthDate());

        List<PatientEncounter> encounters = patientRecord.getEncounters();
        assertEquals(1, encounters.size());

        PatientEncounter encounter = encounters.get(0);
        assertEquals(EncounterClass.IN_PATIENT, encounter.getClassProperty());
        assertEquals("encounterid1", encounter.getId());
        assertEquals(OffsetDateTime.parse("2021-08-28T00:00:00Z"), encounter.getPeriod().getStart());
        assertEquals(OffsetDateTime.parse("2021-08-28T00:00:00Z"), encounter.getPeriod().getEnd());

        List<PatientDocument> patientDocuments = patientRecord.getPatientDocuments();
        assertEquals(1, patientDocuments.size());

        PatientDocument patientDocument = patientDocuments.get(0);
        DocumentAdministrativeMetadata administrativeMetadata = patientDocument.getAdministrativeMetadata();
        assertEquals("encounterid1", administrativeMetadata.getEncounterId());

        List<OrderedProcedure> orderedProcedures = administrativeMetadata.getOrderedProcedures();
        assertEquals(1, orderedProcedures.size());

        OrderedProcedure procedure = orderedProcedures.get(0);
        assertEquals("IH Hip 1 View Left", procedure.getDescription());
        assertNull(procedure.getExtension());

        FhirR4CodeableConcept code = procedure.getCode();
        assertNull(code.getExtension());
        assertNull(code.getId());
        assertNull(code.getText());

        List<FhirR4Coding> codingList = code.getCoding();
        assertEquals(1, codingList.size());

        FhirR4Coding coding = codingList.get(0);
        assertEquals("MVLW", coding.getCode());
        assertEquals("IH Hip 1 View Left", coding.getDisplay());
        assertNull(coding.getExtension());
        assertNull(coding.getId());
        assertEquals("Http://hl7.org/fhir/ValueSet/cpt-all", coding.getSystem());
        assertNull(coding.getVersion());

        List<ClinicalDocumentAuthor> authors = patientDocument.getAuthors();
        assertEquals(1, authors.size());

        ClinicalDocumentAuthor documentAuthor = authors.get(0);
        assertEquals("authorname1", documentAuthor.getFullName());
        assertEquals("authorid1", documentAuthor.getId());

        assertEquals(ClinicalDocumentType.RADIOLOGY_REPORT, patientDocument.getClinicalType());
        assertEquals(DocumentContentSourceType.INLINE, patientDocument.getContent().getSourceType());
        assertEquals("Test", patientDocument.getContent().getValue());

        assertEquals(OffsetDateTime.parse("2021-06-01T00:00:00.000" + "+00:00",
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")), patientDocument.getCreatedAt());

        assertEquals("docid1", patientDocument.getId());
        assertEquals("EN", patientDocument.getLanguage());
        assertEquals(SpecialtyType.RADIOLOGY, patientDocument.getSpecialtyType());
        assertEquals(ClinicalDocumentContentType.NOTE, patientDocument.getType());
    }

    private String documentContent;
    private RadiologyInsightsInferenceType inferenceType;
    private String orderCode;
    private String orderDescription;

    void testRadiologyInsightsWithResponse(Consumer<RadiologyInsightsData> testRunner) {
        testRunner.accept(createRadiologyInsightsRequest());
    }

    private RadiologyInsightsData createRadiologyInsightsRequest() {
        List<PatientRecord> patientRecords = createPatientRecords();
        RadiologyInsightsData radiologyInsightsData = new RadiologyInsightsData(patientRecords);
        RadiologyInsightsModelConfiguration modelConfiguration = createRadiologyInsightsModelConfig();
        radiologyInsightsData.setConfiguration(modelConfiguration);
        return radiologyInsightsData;
    }

    private List<PatientRecord> createPatientRecords() {
        List<PatientRecord> patientRecords = new ArrayList<>();
        // Patients
        PatientRecord patientRecord = new PatientRecord("Sharona");

        PatientDetails patientDetails = new PatientDetails();
        patientDetails.setSex(PatientSex.FEMALE);

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

        ClinicalDocumentAuthor author = new ClinicalDocumentAuthor();
        author.setId("authorid1");
        author.setFullName("authorname1");

        patientDocument.setAuthors(Arrays.asList(author));
        patientDocument.setSpecialtyType(SpecialtyType.RADIOLOGY);

        DocumentAdministrativeMetadata adminMetadata = new DocumentAdministrativeMetadata();
        OrderedProcedure orderedProcedure = new OrderedProcedure();

        FhirR4CodeableConcept procedureCode = new FhirR4CodeableConcept();
        FhirR4Coding procedureCoding = new FhirR4Coding();
        procedureCoding.setSystem("Http://hl7.org/fhir/ValueSet/cpt-all");
        procedureCoding.setCode(getOrderCode());
        procedureCoding.setDisplay(getOrderDescription());

        procedureCode.setCoding(Arrays.asList(procedureCoding));
        orderedProcedure.setCode(procedureCode);
        orderedProcedure.setDescription(getOrderDescription());

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

    private PatientDocument getPatientDocument() {
        ClinicalDocumentContent documentContent
            = new ClinicalDocumentContent(DocumentContentSourceType.INLINE, this.getDocumentContent());
        return new PatientDocument(ClinicalDocumentContentType.NOTE, "docid1", documentContent);
    }

    private RadiologyInsightsModelConfiguration createRadiologyInsightsModelConfig() {
        RadiologyInsightsModelConfiguration configuration = new RadiologyInsightsModelConfiguration();
        RadiologyInsightsInferenceOptions inferenceOptions = getRadiologyInsightsInferenceOptions();
        configuration.setInferenceOptions(inferenceOptions);
        configuration.setInferenceTypes(Arrays.asList(this.getInferenceType()));
        configuration.setLocale("en-US");
        configuration.setVerbose(false);
        configuration.setIncludeEvidence(true);
        return configuration;
    }

    private RadiologyInsightsInferenceOptions getRadiologyInsightsInferenceOptions() {
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

    public String getDocumentContent() {
        return documentContent;
    }

    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }

    public RadiologyInsightsInferenceType getInferenceType() {
        return inferenceType;
    }

    public void setInferenceType(RadiologyInsightsInferenceType inferenceType) {
        this.inferenceType = inferenceType;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }

}
