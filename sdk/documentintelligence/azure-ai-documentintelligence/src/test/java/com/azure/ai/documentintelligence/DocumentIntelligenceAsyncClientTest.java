// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeBatchDocumentsOptions;
import com.azure.ai.documentintelligence.models.AnalyzeBatchOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeBatchResult;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierOptions;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ClassifyDocumentOptions;
import com.azure.ai.documentintelligence.models.DocumentClassifierBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.azure.ai.documentintelligence.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.documentintelligence.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.documentintelligence.TestUtils.DEFAULT_TIMEOUT;
import static com.azure.ai.documentintelligence.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.documentintelligence.TestUtils.INVOICE_PDF;
import static com.azure.ai.documentintelligence.TestUtils.IRS_1040;
import static com.azure.ai.documentintelligence.TestUtils.LAYOUT_SAMPLE;
import static com.azure.ai.documentintelligence.TestUtils.LICENSE_PNG;
import static com.azure.ai.documentintelligence.TestUtils.MULTIPAGE_INVOICE_PDF;
import static com.azure.ai.documentintelligence.TestUtils.RECEIPT_CONTOSO_JPG;
import static com.azure.ai.documentintelligence.TestUtils.W2_JPG;
import static com.azure.ai.documentintelligence.TestUtils.getData;
import static com.azure.ai.documentintelligence.TestUtils.urlSource;
import static com.azure.ai.documentintelligence.models.AnalyzeOutputFormat.FIGURES;
import static com.azure.ai.documentintelligence.models.AnalyzeOutputFormat.PDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DocumentIntelligenceAsyncClientTest extends DocumentIntelligenceClientTestBase {

    private DocumentIntelligenceAsyncClient client;

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).skipRequest((ignored1, ignored2) -> false)
            .assertAsync()
            .build();
    }

    private DocumentIntelligenceAsyncClient getDocumentAnalysisAsyncClient(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        return getDocumentAnalysisBuilder(
            buildAsyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion).buildAsyncClient();
    }

    private DocumentIntelligenceAdministrationAsyncClient getDocumentAdminAsyncClient(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(
            buildAsyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion).buildAsyncClient();
    }

    // Receipt recognition

    // Receipt - non-URL

    /**
     * Verifies receipt data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeReceiptData(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-receipt", new AnalyzeDocumentOptions(getData(RECEIPT_CONTOSO_JPG)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        syncPoller.waitForCompletion();
        validateJpegReceiptData(syncPoller.getFinalResult());
    }

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument("prebuilt-receipt", new AnalyzeDocumentOptions(urlSource(RECEIPT_CONTOSO_JPG)))
            .setPollInterval(durationTestMode)
            .getSyncPoller();
        syncPoller.waitForCompletion();
        validateJpegReceiptData(syncPoller.getFinalResult());
    }

    // Content Recognition

    // Content - non-URL

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLayout(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(getData(CONTENT_FORM_JPG)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        syncPoller.waitForCompletion();
        validateContentData(syncPoller.getFinalResult());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLayoutWithPages(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument("prebuilt-layout",
                new AnalyzeDocumentOptions(getData(MULTIPAGE_INVOICE_PDF)).setPages(Arrays.asList("1, 2")))
            .setPollInterval(durationTestMode)
            .getSyncPoller();
        syncPoller.waitForCompletion();
        AnalyzeResult analyzeResult = syncPoller.getFinalResult();
        Assertions.assertEquals(2, analyzeResult.getPages().size());
    }

    // Content - URL

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeContentFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(urlSource(CONTENT_FORM_JPG)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        syncPoller.waitForCompletion();
        validateContentData(syncPoller.getFinalResult());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeGermanContentFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument("prebuilt-layout",
                new AnalyzeDocumentOptions(getData(CONTENT_GERMAN_PDF)).setLocale("de"))
            .setPollInterval(durationTestMode)
            .getSyncPoller();
        syncPoller.waitForCompletion();
        validateGermanContentData(syncPoller.getFinalResult());
    }

    /**
     * Verifies invoice data recognition  for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeInvoiceData(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(getData(INVOICE_PDF)).setLocale("de"))
            .setPollInterval(durationTestMode)
            .getSyncPoller();
        syncPoller.waitForCompletion();

        validateInvoiceData(syncPoller.getFinalResult());
    }

    // invoice - URL

    /**
     * Verifies invoice card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeInvoiceSourceUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(urlSource(INVOICE_PDF)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        syncPoller.waitForCompletion();
        validateInvoiceData(syncPoller.getFinalResult());
    }

    // identity document Recognition

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLicenseCardData(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-idDocument", new AnalyzeDocumentOptions(getData(LICENSE_PNG)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        syncPoller.waitForCompletion();

        validateIdentityData(syncPoller.getFinalResult());
    }

    // Identity document - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLicenseSourceUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-idDocument", new AnalyzeDocumentOptions(urlSource(LICENSE_PNG)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        syncPoller.waitForCompletion();
        validateIdentityData(syncPoller.getFinalResult());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeW2Data(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-tax.us.w2", new AnalyzeDocumentOptions(getData(W2_JPG)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        syncPoller.waitForCompletion();
        validateW2Data(syncPoller.getFinalResult());
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void testClassifyAnalyzeFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion)
        throws RuntimeException {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        String classifierId1 = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        DocumentIntelligenceAdministrationAsyncClient adminClient
            = getDocumentAdminAsyncClient(httpClient, serviceVersion);
        String trainingFilesUrl = getClassifierTrainingFilesContainerUrl();
        Map<String, ClassifierDocumentTypeDetails> documentTypes = new HashMap<>();
        documentTypes.put("IRS-1040-A", createBlobContentSource(trainingFilesUrl, "IRS-1040-A/train"));
        documentTypes.put("IRS-1040-B", createBlobContentSource(trainingFilesUrl, "IRS-1040-B/train"));
        documentTypes.put("IRS-1040-C", createBlobContentSource(trainingFilesUrl, "IRS-1040-C/train"));
        documentTypes.put("IRS-1040-D", createBlobContentSource(trainingFilesUrl, "IRS-1040-D/train"));
        documentTypes.put("IRS-1040-E", createBlobContentSource(trainingFilesUrl, "IRS-1040-E/train"));
        SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
            = adminClient.beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId1, documentTypes))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        buildModelPoller.waitForCompletion();
        DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();

        if (documentClassifierDetails != null) {
            String classifierId = documentClassifierDetails.getClassifierId();
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginClassifyDocument(classifierId, new ClassifyDocumentOptions(getData(IRS_1040)))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            assertNotNull(analyzeResult);
            // TODO: (service bug) AnalyzedDocument count should be 3
            Assertions.assertEquals(1, analyzeResult.getDocuments().size());
            Assertions.assertEquals(analyzeResult.getModelId(), classifierId);
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void getAnalyzePdf(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        String modelID = "prebuilt-read";
        PollerFlux<AnalyzeOperationDetails, AnalyzeResult> resultPollerFlux = client
            .beginAnalyzeDocument(modelID,
                new AnalyzeDocumentOptions(getData(LAYOUT_SAMPLE)).setOutput(Collections.singletonList(PDF)))
            .setPollInterval(durationTestMode);

        StepVerifier
            .create(resultPollerFlux.last()
                .flatMap(response -> client.getAnalyzeResultPdf(modelID, response.getValue().getResultId())))
            .assertNext(pdf -> {
                byte[] pdfHeader = Arrays.copyOfRange(pdf.toBytes(), 0, 5);

                // A PDF's header is expected to be: %PDF-
                Assertions.assertArrayEquals(new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D }, pdfHeader);
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void getAnalyzeFigures(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        String modelID = "prebuilt-layout";
        PollerFlux<AnalyzeOperationDetails, AnalyzeResult> resultPollerFlux = client
            .beginAnalyzeDocument(modelID,
                new AnalyzeDocumentOptions(getData(LAYOUT_SAMPLE)).setOutput(Collections.singletonList(FIGURES)))
            .setPollInterval(durationTestMode);

        StepVerifier.create(resultPollerFlux.last()
            .flatMap(response -> client.getAnalyzeResultFigure(modelID, response.getValue().getResultId(),
                response.getValue().getAnalyzeResult().getFigures().get(0).getId())))
            .assertNext(figures -> {
                byte[] figuresHeader = Arrays.copyOfRange(figures.toBytes(), 0, 5);

                // A PNG's header is expected to start with: ‰PNG
                Assertions.assertArrayEquals(new byte[] { (byte) -119, 80, 78, 71, 13 }, figuresHeader);
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeBatchDocuments(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        String trainingFilesUrl = getBatchTrainingFilesContainerUrl();
        String trainingFilesResultUrl = getBatchTrainingFilesResultContainerUrl();
        SyncPoller<AnalyzeBatchOperationDetails, AnalyzeBatchResult> syncPoller
            = client
                .beginAnalyzeBatchDocuments("prebuilt-layout",
                    new AnalyzeBatchDocumentsOptions(new AzureBlobContentSource(trainingFilesUrl),
                        trainingFilesResultUrl).setResultPrefix("result/").setOverwriteExisting(true))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
        AnalyzeBatchResult analyzeResult = syncPoller.getFinalResult();
        assertNotNull(analyzeResult);
        assertEquals(6, analyzeResult.getSucceededCount());
    }
}
