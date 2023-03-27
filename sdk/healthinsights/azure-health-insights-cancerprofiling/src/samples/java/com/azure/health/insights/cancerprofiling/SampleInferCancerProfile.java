// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.cancerprofiling;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

import com.azure.core.util.Configuration;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.health.insights.cancerprofiling.models.PatientInfoSex;
import com.azure.health.insights.cancerprofiling.models.ClinicalCodedElement;
import com.azure.health.insights.cancerprofiling.models.PatientInfo;
import com.azure.health.insights.cancerprofiling.models.PatientRecord;
import com.azure.health.insights.cancerprofiling.models.PatientDocument;
import com.azure.health.insights.cancerprofiling.models.ClinicalDocumentType;
import com.azure.health.insights.cancerprofiling.models.DocumentType;
import com.azure.health.insights.cancerprofiling.models.DocumentContentSourceType;
import com.azure.health.insights.cancerprofiling.models.DocumentContent;
import com.azure.health.insights.cancerprofiling.models.OncoPhenotypeResults;
import com.azure.health.insights.cancerprofiling.models.OncoPhenotypeModelConfiguration;
import com.azure.health.insights.cancerprofiling.models.OncoPhenotypeData;
import com.azure.health.insights.cancerprofiling.models.OncoPhenotypeResult;
import com.azure.health.insights.cancerprofiling.models.ClinicalNoteEvidence;


public class SampleInferCancerProfile {
     public static void main(final String[] args) throws InterruptedException {
        // BEGIN: com.azure.health.insights.cancerprofiling.buildasyncclient
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");
        String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_API_KEY");

        CancerProfilingAsyncClient asyncClient = new CancerProfilingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();
        // END: com.azure.health.insights.cancerprofiling.buildasyncclient

        // BEGIN: com.azure.health.insights.cancerprofiling.infercancerprofile
        // Construct Patient
        PatientRecord patient1 = new PatientRecord("patient_id");
        PatientInfo patientInfo = new PatientInfo();
        patientInfo.setBirthDate(LocalDate.parse("1965-12-26"));
        patientInfo.setSex(PatientInfoSex.FEMALE);
        patient1.setInfo(patientInfo);
        LinkedList<PatientDocument> patientDocuments = new LinkedList<>();
        patient1.setData(patientDocuments);

        // Add imaging document
        final String docContent1 = String.join(System.getProperty("line.separator"),
            "15.8.2021",
            "Jane Doe 091175-8967",
            "42 year old female, married with 3 children, works as a nurse. ",
            "Healthy, no medications taken on a regular basis.",
            "PMHx is significant for migraines with aura, uses Mirena for contraception.",
            "Smoking history of 10 pack years (has stopped and relapsed several times).",
            "She is in c/o 2 weeks of productive cough and shortness of breath.",
            "She has a fever of 37.8 and general weakness. ",
            "Denies night sweats and rash. She denies symptoms of rhinosinusitis, asthma, and heartburn. ",
            "On PE:",
            "GENERAL: mild pallor, no cyanosis. Regular breathing rate. ",
            "LUNGS: decreased breath sounds on the base of the right lung. Vesicular breathing.",
            " No crackles, rales, and wheezes. Resonant percussion. ",
            "PLAN: ",
            "Will be referred for a chest x-ray. ",
            "======================================",
            "CXR showed mild nonspecific opacities in right lung base. ",
            "PLAN:",
            "Findings are suggestive of a working diagnosis of pneumonia. The patient is referred to a follow-up CXR in 2 weeks.");

        PatientDocument patientDocument1 = new PatientDocument(
            DocumentType.NOTE,
            "doc1",
            new DocumentContent(DocumentContentSourceType.INLINE, docContent1));

        patientDocument1.setClinicalType(ClinicalDocumentType.IMAGING);
        patientDocument1.setLanguage("en");
        patientDocument1.setCreatedDateTime(OffsetDateTime.parse("2021-08-15T10:15:30+01:00"));
        patient1.getData().add(patientDocument1);

        // Add Pathology documents
        String docContent2 = String.join(System.getProperty("line.separator"),
            "Oncology Clinic ",
            "20.10.2021",
            "Jane Doe 091175-8967",
            "42-year-old healthy female who works as a nurse in the ER of this hospital. ",
            "First menstruation at 11 years old. First delivery- 27 years old. She has 3 children.",
            "Didn’t breastfeed. ",
            "Contraception- Mirena.",
            "Smoking- 10 pack years. ",
            "Mother- Belarusian. Father- Georgian. ",
            "About 3 months prior to admission, she stated she had SOB and was febrile. ",
            "She did a CXR as an outpatient which showed a finding in the base of the right lung- possibly an infiltrate.",
            "She was treated with antibiotics with partial response. ",
            "6 weeks later a repeat CXR was performed- a few solid dense findings in the right lung. ",
            "Therefore, she was referred for a PET-CT which demonstrated increased uptake in the right breast, lymph nodes on the right a few areas in the lungs and liver. ",
            "On biopsy from the lesion in the right breast- triple negative adenocarcinoma. Genetic testing has not been done thus far. ",
            "Genetic counseling- the patient denies a family history of breast, ovary, uterus, and prostate cancer. Her mother has chronic lymphocytic leukemia (CLL). ",
            "She is planned to undergo genetic tests because the aggressive course of the disease, and her young age. ",
            "Impression:",
            "Stage 4 triple negative breast adenocarcinoma. ",
            "Could benefit from biological therapy. ",
            "Different treatment options were explained- the patient wants to get a second opinion.");

        PatientDocument patientDocument2 = new PatientDocument(DocumentType.NOTE,
            "doc2",
            new DocumentContent(DocumentContentSourceType.INLINE, docContent2));
        patientDocument2.setClinicalType(ClinicalDocumentType.PATHOLOGY);
        patientDocument2.setLanguage("en");
        patientDocument2.setCreatedDateTime(OffsetDateTime.parse("2021-10-20T22:00:00.00Z"));
        patient1.getData().add(patientDocument2);

        String docContent3 = String.join(System.getProperty("line.separator"),
            "PATHOLOGY REPORT",
            "                          Clinical Information",
            "Ultrasound-guided biopsy; A. 18 mm mass; most likely diagnosis based on imaging:  IDC",
            "                               Diagnosis",
            " A.  BREAST, LEFT AT 2:00 4 CM FN; ULTRASOUND-GUIDED NEEDLE CORE BIOPSIES:",
            " - Invasive carcinoma of no special type (invasive ductal carcinoma), grade 1",
            " Nottingham histologic grade:  1/3 (tubules 2; nuclear grade 2; mitotic rate 1; total score;  5/9)",
            " Fragments involved by invasive carcinoma:  2",
            " Largest measurement of invasive carcinoma on a single fragment:  7 mm",
            " Ductal carcinoma in situ (DCIS):  Present",
            " Architectural pattern:  Cribriform",
            " Nuclear grade:  2-",
            "                  -intermediate",
            " Necrosis:  Not identified",
            " Fragments involved by DCIS:  1",
            " Largest measurement of DCIS on a single fragment:  Span 2 mm",
            " Microcalcifications:  Present in benign breast tissue and invasive carcinoma",
            " Blocks with invasive carcinoma:  A1",
            " Special studies: Pending");

        PatientDocument patientDocument3 = new PatientDocument(DocumentType.NOTE,
                                                            "doc3",
                                                            new DocumentContent(DocumentContentSourceType.INLINE, docContent3));
        patientDocument3.setClinicalType(ClinicalDocumentType.PATHOLOGY);
        patientDocument3.setLanguage("en");
        patientDocument3.setCreatedDateTime(OffsetDateTime.parse("2022-01-01T10:15:30+01:00"));

        patient1.getData().add(patientDocument3);

        // Set configuration to include evidence for the cancer staging inferences
        OncoPhenotypeModelConfiguration configuration = new OncoPhenotypeModelConfiguration();
        configuration.setIncludeEvidence(true);

        // Construct the request with the patient and configration
        OncoPhenotypeData oncoPhenotypeData = new OncoPhenotypeData(Arrays.asList(patient1));
        oncoPhenotypeData.setConfiguration(configuration);

        PollerFlux<OncoPhenotypeResult, OncoPhenotypeResult> asyncPoller = asyncClient.beginInferCancerProfile(oncoPhenotypeData);
        // END: com.azure.health.insights.cancerprofiling.infercancerprofile
        asyncPoller
          .takeUntil(isComplete)
          .subscribe(completedResult -> {
            System.out.println("Completed poll response, status: " + completedResult.getStatus());
            printResults(completedResult.getValue());
            latch.countDown();
        });

        latch.await();
    }

    private static final void printResults(OncoPhenotypeResult result) {
        OncoPhenotypeResults oncoResults = result.getResults();
        oncoResults.getPatients().forEach(patient_result -> {
            System.out.println("\n==== Inferences of Patient " + patient_result.getId() + " ====");
            patient_result.getInferences().forEach(onco_inference -> {
                System.out.println("\n=== Clinical Type: " + onco_inference.getType() + "  Value: " + onco_inference.getValue() + "   ConfidenceScore: " + onco_inference.getConfidenceScore() + " ===");
                onco_inference.getEvidence().forEach(evidence -> {
                    if (evidence.getPatientDataEvidence() != null) {
                        ClinicalNoteEvidence dataEvidence = evidence.getPatientDataEvidence();
                        System.out.println("Evidence " + dataEvidence.getId() + " " + dataEvidence.getOffset() + " " + dataEvidence.getLength() + " " + dataEvidence.getText());
                    }
                    if (evidence.getPatientInfoEvidence() != null) {
                        ClinicalCodedElement infoEvidence = evidence.getPatientInfoEvidence();
                        System.out.println("Evidence " + infoEvidence.getSystem() + " " + infoEvidence.getCode() + " " + infoEvidence.getName() + " " + infoEvidence.getValue());
                    }
                });
            });
        });
    }

    private static final Predicate<AsyncPollResponse<OncoPhenotypeResult, OncoPhenotypeResult>> isComplete = response -> {
        return response.getStatus() != LongRunningOperationStatus.IN_PROGRESS
            && response.getStatus() != LongRunningOperationStatus.NOT_STARTED;
    };

    private static final CountDownLatch latch = new CountDownLatch(1);
}
