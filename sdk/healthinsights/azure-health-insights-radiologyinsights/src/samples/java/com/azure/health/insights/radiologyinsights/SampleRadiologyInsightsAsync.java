// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.PollerFlux;
import com.azure.health.insights.radiologyinsights.models.AgeMismatchInference;
import com.azure.health.insights.radiologyinsights.models.ClinicalDocumentType;
import com.azure.health.insights.radiologyinsights.models.CodeableConcept;
import com.azure.health.insights.radiologyinsights.models.Coding;
import com.azure.health.insights.radiologyinsights.models.CompleteOrderDiscrepancyInference;
import com.azure.health.insights.radiologyinsights.models.CriticalResultInference;
import com.azure.health.insights.radiologyinsights.models.DocumentAdministrativeMetadata;
import com.azure.health.insights.radiologyinsights.models.DocumentAuthor;
import com.azure.health.insights.radiologyinsights.models.DocumentContent;
import com.azure.health.insights.radiologyinsights.models.DocumentContentSourceType;
import com.azure.health.insights.radiologyinsights.models.DocumentType;
import com.azure.health.insights.radiologyinsights.models.Encounter;
import com.azure.health.insights.radiologyinsights.models.EncounterClass;
import com.azure.health.insights.radiologyinsights.models.Extension;
import com.azure.health.insights.radiologyinsights.models.FindingInference;
import com.azure.health.insights.radiologyinsights.models.FindingOptions;
import com.azure.health.insights.radiologyinsights.models.FollowupCommunicationInference;
import com.azure.health.insights.radiologyinsights.models.FollowupRecommendationInference;
import com.azure.health.insights.radiologyinsights.models.FollowupRecommendationOptions;
import com.azure.health.insights.radiologyinsights.models.LateralityDiscrepancyInference;
import com.azure.health.insights.radiologyinsights.models.LimitedOrderDiscrepancyInference;
import com.azure.health.insights.radiologyinsights.models.OrderedProcedure;
import com.azure.health.insights.radiologyinsights.models.PatientDocument;
import com.azure.health.insights.radiologyinsights.models.PatientInfo;
import com.azure.health.insights.radiologyinsights.models.PatientInfoSex;
import com.azure.health.insights.radiologyinsights.models.PatientRecord;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsData;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceOptions;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsModelConfiguration;
import com.azure.health.insights.radiologyinsights.models.RadiologyProcedureInference;
import com.azure.health.insights.radiologyinsights.models.Resource;
import com.azure.health.insights.radiologyinsights.models.SexMismatchInference;
import com.azure.health.insights.radiologyinsights.models.SpecialtyType;
import com.azure.health.insights.radiologyinsights.models.TimePeriod;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The SampleRadiologyInsightsSync class is responsible for performing Radiology
 * Insights operations. It contains various methods to initialize, use, and
 * print the results of the RadiologyInsightsClient.
 */
public class SampleRadiologyInsightsAsync {

    private static final String DOCUMENT_CONTENT = "CLINICAL HISTORY:   \r\n20-year-old female presenting with abdominal pain. Surgical history \r\nsignificant for appendectomy.\r\n \r\nCOMPARISON:   \r\nRight upper quadrant sonographic performed 1 day prior.\r\n \r\nTECHNIQUE:   \r\nTransabdominal grayscale pelvic sonography with duplex color Doppler \r\nand spectral waveform analysis of the ovaries.\r\n \r\nFINDINGS:   \r\nThe uterus is unremarkable given the transabdominal technique with \r\nendometrial echo complex within physiologic normal limits. The \r\novaries are symmetric in size, measuring 2.5 x 1.2 x 3.0 cm and the \r\nleft measuring 2.8 x 1.5 x 1.9 cm.\n \r\nOn duplex imaging, Doppler signal is symmetric.\r\n \r\nIMPRESSION:   \r\n1. Normal pelvic sonography. Findings of testicular torsion.\r\n\nA new US pelvis within the next 6 months is recommended.\n\nThese results have been discussed with Dr. Jones at 3 PM on November 5 2020.\n \r\n";

    static class Offset {
        private int offset;
        private int length;

        Offset(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        Offset() {

        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    private static final Map<String, List<Offset>> INFERENCE_OFFSET_MAPPINGS = new HashMap<>();

    /**
     * The main method is the entry point for the program. It initializes and uses
     * the RadiologyInsightsClient to perform Radiology Insights operations.
     *
     * @param args The command-line arguments passed to the program.
     */
    public static void main(final String[] args) throws InterruptedException {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_RADIOLOGY_INSIGHTS_ENDPOINT");
        String apiKey = Configuration.getGlobalConfiguration().get("AZURE_RADIOLOGY_INSIGHTS_ENDPOINT_API_KEY");

        RadiologyInsightsAsyncClient radiologyInsightsAsyncClient = new RadiologyInsightsClientBuilder()
                .endpoint(endpoint).serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
                .credential(new AzureKeyCredential(apiKey)).buildAsyncClient();

        PollerFlux<PollOperationDetails, RadiologyInsightsInferenceResult> asyncPoller = radiologyInsightsAsyncClient
                .beginInferRadiologyInsights(createRadiologyInsightsRequest());

        asyncPoller.takeUntil(isComplete).filter(isComplete).subscribe(completedResult -> {
            System.out.println("Completed poll response, status: " + completedResult.getStatus());
            if (completedResult.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
                mono = completedResult.getFinalResult();
            } else {
                System.out.println(
                        "Poll Request Failed with message: " + completedResult.getValue().getError().getMessage());
            }
            latch.countDown();
        });

        latch.await();
        printResults(mono.block());
    }

    private static Mono<RadiologyInsightsInferenceResult> mono = null;

    /**
     * Prints the results of the Radiology Insights request.
     *
     * @param radiologyInsightsResult The response for the Radiology Insights
     *                                request.
     */
    private static void printResults(RadiologyInsightsInferenceResult radiologyInsightsResult) {
        radiologyInsightsResult.getPatientResults().forEach(patientResult -> {
            System.out.println("Inferences of Patient Id: " + patientResult.getPatientId());
            patientResult.getInferences().forEach(SampleRadiologyInsightsAsync::printInferenceData);
        });
        printTokens();
    }

    /**
     * Prints the extension data of a RadiologyInsightsInference object.
     *
     * @param obj The RadiologyInsightsInference object.
     */
    private static void printInferenceData(RadiologyInsightsInference obj) {
        if (obj != null) {
            if (obj instanceof AgeMismatchInference) {
                AgeMismatchInference ageMismatchInference = (AgeMismatchInference) obj;
                printExtensionData(ageMismatchInference.getExtension(), "AgeMismatchInference");
            } else if (obj instanceof SexMismatchInference) {
                SexMismatchInference sexMismatchInference = (SexMismatchInference) obj;
                printExtensionData(sexMismatchInference.getExtension(), "SexMismatchInference");
            } else if (obj instanceof LateralityDiscrepancyInference) {
                LateralityDiscrepancyInference lateralityDiscrepancyInference = (LateralityDiscrepancyInference) obj;
                printExtensionData(lateralityDiscrepancyInference.getExtension(), "LateralityDiscrepancyInference");
            } else if (obj instanceof CompleteOrderDiscrepancyInference) {
                CompleteOrderDiscrepancyInference completeOrderDiscrepancyInference = (CompleteOrderDiscrepancyInference) obj;
                printExtensionData(completeOrderDiscrepancyInference.getExtension(),
                        "CompleteOrderDiscrepancyInference");
            } else if (obj instanceof LimitedOrderDiscrepancyInference) {
                LimitedOrderDiscrepancyInference limitedOrderDiscrepancyInference = (LimitedOrderDiscrepancyInference) obj;
                printExtensionData(limitedOrderDiscrepancyInference.getExtension(), "LimitedOrderDiscrepancyInference");
            } else if (obj instanceof FindingInference) {
                FindingInference findingInference = (FindingInference) obj;
                printExtensionData(findingInference.getExtension(), "FindingInference");
            } else if (obj instanceof CriticalResultInference) {
                CriticalResultInference criticalResultInference = (CriticalResultInference) obj;
                printExtensionData(criticalResultInference.getExtension(), "CriticalResultInference");
            } else if (obj instanceof RadiologyProcedureInference) {
                RadiologyProcedureInference radiologyProcedureInference = (RadiologyProcedureInference) obj;
                printExtensionData(radiologyProcedureInference.getExtension(), "RadiologyProcedureInference");
            } else if (obj instanceof FollowupRecommendationInference) {
                FollowupRecommendationInference followupRecommendationInference = (FollowupRecommendationInference) obj;
                printExtensionData(followupRecommendationInference.getExtension(), "FollowupRecommendationInference");
            } else if (obj instanceof FollowupCommunicationInference) {
                FollowupCommunicationInference followupCommunicationInference = (FollowupCommunicationInference) obj;
                printExtensionData(followupCommunicationInference.getExtension(), "FollowupCommunicationInference");
            }
        } else {
            System.out.println("Object is not an instance of RadiologyInsightsInference or any of its subclasses");
        }
    }

    /**
     * Prints the tokens for each inference type in the INFERENCE_OFFSET_MAPPINGS.
     * Tokens are retrieved by using the offset and length values stored in the
     * INFERENCE_OFFSET_MAPPINGS.
     */
    private static void printTokens() {
        if (!INFERENCE_OFFSET_MAPPINGS.isEmpty()) {
            for (String inferenceType : INFERENCE_OFFSET_MAPPINGS.keySet()) {
                List<Offset> offsets = INFERENCE_OFFSET_MAPPINGS.get(inferenceType);
                if (offsets != null) {
                    List<String> tokens = new ArrayList<>();
                    for (Offset offset : offsets) {
                        tokens.add(mySubString(offset.offset, offset.length));
                    }
                    System.out.println("Tokens for Inference Type: " + inferenceType + " : " + tokens);
                }
            }
        }
    }

    /**
     * Returns a substring of the given string starting from the specified index and
     * extending to the specified length. If the length extends beyond the end of
     * the string, the substring is truncated to the end of the string.
     *
     * @param start  the starting index of the substring, inclusive.
     * @param length the length of the substring to be returned.
     * @return the substring of the given string.
     */
    static String mySubString(int start, int length) {
        return DOCUMENT_CONTENT.substring(start, Math.min(start + length, DOCUMENT_CONTENT.length()));
    }

    private static final String OFFSET = "offset";
    private static final String LENGTH = "length";

    /**
     * Prints the extension data of a given list of Extensions for a specific kind.
     *
     * @param extensions The list of Extensions to print their data.
     * @param kind       The specific kind of extension data to print.
     */
    private static void printExtensionData(List<Extension> extensions, String kind) {
        if (extensions != null) {
            for (Extension extension : extensions) {
                Offset offset = new Offset();
                List<Extension> filteredExtensions = getFilteredExtensions(extension);
                for (Extension innerExtension : filteredExtensions) {
                    printExtensionValue(innerExtension, offset);
                }
                List<Offset> offsets = INFERENCE_OFFSET_MAPPINGS.get(kind);
                if (offset.offset == 0 && offset.length == 0) {
                    continue;
                }
                if (offsets != null) {
                    offsets.add(offset);
                } else {
                    offsets = new ArrayList<>();
                    offsets.add(offset);
                }
                INFERENCE_OFFSET_MAPPINGS.put(kind, offsets);
            }
        }
    }

    /**
     * Returns a filtered list of extensions based on the specified Extension
     * object.
     *
     * @param extension The Extension object to filter.
     * @return A List of Extension objects that satisfy the filtering condition.
     */
    private static List<Extension> getFilteredExtensions(Extension extension) {
        if (extension.getExtension() != null) {
            return extension.getExtension().stream().filter(SampleRadiologyInsightsAsync::isOffsetOrLength)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Checks if a given Extension object's URL is equal to either OFFSET or LENGTH.
     *
     * @param extension The Extension object to check.
     * @return true if the Extension object's URL is equal to OFFSET or LENGTH,
     *         false otherwise.
     */
    private static boolean isOffsetOrLength(Extension extension) {
        return extension.getUrl().equals(OFFSET) || extension.getUrl().equals(LENGTH);
    }

    /**
     * Prints the extension value of an Extension object based on its URL. If the
     * extension URL is OFFSET, it sets the offset value of the given Offset object.
     * If the extension URL is LENGTH, it sets the length value of the given Offset
     * object.
     *
     * @param extension The Extension object to retrieve the value from.
     * @param offset    The Offset object to update with the retrieved value.
     */
    private static void printExtensionValue(Extension extension, Offset offset) {
        if (extension.getUrl().equals(OFFSET)) {
            // System.out.println("Offset: " + extension.getValueInteger());
            offset.setOffset(extension.getValueInteger());
        } else if (extension.getUrl().equals(LENGTH)) {
            // System.out.println("Length: " + extension.getValueInteger());
            offset.setLength(extension.getValueInteger());
        }
    }

    /**
     * Creates a RadiologyInsightsData object to use in the Radiology Insights
     * request.
     *
     * @return A RadiologyInsightsData object with the created patient records and
     *         model configuration.
     */
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

        PatientInfo patientInfo = new PatientInfo();
        patientInfo.setSex(PatientInfoSex.FEMALE);
        // Define a formatter that matches the input pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        // Parse the string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse("1959-11-11T19:00:00+00:00", formatter);
        patientInfo.setBirthDate(dateTime.toLocalDate());

        List<Resource> clinicalInfoList = new ArrayList<>();
        Resource clinicalInfo = new Resource("Observation");
        clinicalInfoList.add(clinicalInfo);

        CodeableConcept code = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://www.nlm.nih.gov/research/umls");
        coding.setCode("C0018802");
        coding.setDisplay("MalignantNeoplasms");
        code.setCoding(Arrays.asList(coding));

        patientInfo.setClinicalInfo(Arrays.asList(clinicalInfo));
        patientRecord.setInfo(patientInfo);

        Encounter encounter = new Encounter("encounterid1");

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
        patientDocument.setLanguage("en-US");

        DocumentAuthor author = new DocumentAuthor();
        author.setId("authorid1");
        author.setFullName("authorname1");

        patientDocument.setAuthors(Arrays.asList(author));
        patientDocument.setSpecialtyType(SpecialtyType.RADIOLOGY);

        DocumentAdministrativeMetadata adminMetadata = new DocumentAdministrativeMetadata();
        OrderedProcedure orderedProcedure = new OrderedProcedure();

        CodeableConcept procedureCode = new CodeableConcept();
        Coding procedureCoding = new Coding();
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
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        OffsetDateTime createdDateTime = OffsetDateTime.parse("2021-06-01T00:00:00.000" + "+00:00", formatter3);
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
        DocumentContent documentContent = new DocumentContent(DocumentContentSourceType.INLINE, DOCUMENT_CONTENT);
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
        inferenceOptions.setFollowupRecommendation(followupOptions);
        inferenceOptions.setFinding(findingOptions);
        return inferenceOptions;
    }

    private static Predicate<AsyncPollResponse<PollOperationDetails, RadiologyInsightsInferenceResult>> isComplete = response -> {
        return response.getStatus() != LongRunningOperationStatus.IN_PROGRESS
                && response.getStatus() != LongRunningOperationStatus.NOT_STARTED;
    };

    private static CountDownLatch latch = new CountDownLatch(1);
}
