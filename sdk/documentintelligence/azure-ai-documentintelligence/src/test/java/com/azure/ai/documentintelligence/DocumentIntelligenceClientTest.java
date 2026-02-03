// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeBatchDocumentsOptions;
import com.azure.ai.documentintelligence.models.AnalyzeBatchOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeBatchResult;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeOutputFormat;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierOptions;
import com.azure.ai.documentintelligence.models.BuildDocumentModelOptions;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ClassifyDocumentOptions;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentClassifierBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.azure.ai.documentintelligence.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.documentintelligence.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.documentintelligence.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.documentintelligence.TestUtils.INVOICE_PDF;
import static com.azure.ai.documentintelligence.TestUtils.IRS_1040;
import static com.azure.ai.documentintelligence.TestUtils.LAYOUT_SAMPLE;
import static com.azure.ai.documentintelligence.TestUtils.LICENSE_PNG;
import static com.azure.ai.documentintelligence.TestUtils.MULTIPAGE_INVOICE_PDF;
import static com.azure.ai.documentintelligence.TestUtils.RECEIPT_CONTOSO_JPG;
import static com.azure.ai.documentintelligence.TestUtils.getData;
import static com.azure.ai.documentintelligence.TestUtils.urlSource;
import static com.azure.ai.documentintelligence.models.AnalyzeOutputFormat.PDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DocumentIntelligenceClientTest extends DocumentIntelligenceClientTestBase {
    private DocumentIntelligenceClient client;

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).skipRequest((ignored1, ignored2) -> false)
            .assertSync()
            .build();
    }

    private DocumentIntelligenceClient getDocumentAnalysisClient(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        return getDocumentAnalysisBuilder(
            buildSyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion).buildClient();
    }

    private DocumentIntelligenceAdministrationClient getDocumentModelAdminClient(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(
            buildSyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion).buildClient();
    }

    // Receipt recognition
    // Receipt - non-URL

    /**
     * Verifies receipt data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeReceiptData(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-receipt", new AnalyzeDocumentOptions(getData(RECEIPT_CONTOSO_JPG)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateJpegReceiptData(syncPoller.getFinalResult());
    }

    // Receipt - URL

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument("prebuilt-receipt", new AnalyzeDocumentOptions(urlSource(RECEIPT_CONTOSO_JPG)))
            .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateJpegReceiptData(syncPoller.getFinalResult());
    }

    // Content Recognition

    // Content - non-URL

    /**
     * Verifies layout/content data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLayout(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(getData(CONTENT_FORM_JPG)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateContentData(syncPoller.getFinalResult());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLayoutWithPages(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument("prebuilt-layout",
                new AnalyzeDocumentOptions(getData(MULTIPAGE_INVOICE_PDF)).setPages(Arrays.asList("1, 2")))
            .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        AnalyzeResult analyzeResult = syncPoller.getFinalResult();
        assertEquals(2, analyzeResult.getPages().size());
    }

    // Content - URL

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLayoutFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(urlSource(CONTENT_FORM_JPG)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateContentData(syncPoller.getFinalResult());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeGermanContentFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(getData(CONTENT_GERMAN_PDF)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateGermanContentData(syncPoller.getFinalResult());
    }

    // Custom AnalyzedDocument recognition

    /**
     * Verifies custom form data for a document using source as input stream data and valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeCustomDocument(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentIntelligenceAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        String trainingFilesUrl = getTrainingFilesContainerUrl();
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller = adminClient
            .beginBuildDocumentModel(
                new BuildDocumentModelOptions("modelID" + UUID.randomUUID(), DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
            .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();

        String modelId = buildModelPoller.getFinalResult().getModelId();

        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument(modelId, new AnalyzeDocumentOptions(getData(CONTENT_FORM_JPG)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();

        adminClient.deleteModel(modelId);
        validateJpegCustomDocument(syncPoller.getFinalResult());
    }

    // Custom AnalyzedDocument - URL

    /**
     * Verifies custom form data for an URL document data without labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeCustomDocumentUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String modelId1 = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        DocumentIntelligenceAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        String trainingFilesUrl = getTrainingFilesContainerUrl();
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller
            = adminClient
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId1, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();
        String modelId = buildModelPoller.getFinalResult().getModelId();
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument(modelId, new AnalyzeDocumentOptions(urlSource(CONTENT_FORM_JPG)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        adminClient.deleteModel(modelId);

        validateJpegCustomDocument(syncPoller.getFinalResult());
    }

    // Invoice recognition

    // Invoice - non-URL

    /**
     * Verifies invoice data recognition  for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeInvoiceData(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(getData(INVOICE_PDF)))
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(urlSource(INVOICE_PDF)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateInvoiceData(syncPoller.getFinalResult());
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void invoiceValidLocale(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(getData(INVOICE_PDF)))
                .setPollInterval(durationTestMode);
        validateInvoiceData(syncPoller.getFinalResult());
    }

    // Identity AnalyzedDocument Recognition

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLicenseCardData(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-idDocument", new AnalyzeDocumentOptions(getData(LICENSE_PNG)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateIdentityData(syncPoller.getFinalResult());
    }

    // Identity AnalyzedDocument - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLicenseSourceUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
            = client.beginAnalyzeDocument("prebuilt-idDocument", new AnalyzeDocumentOptions(urlSource(LICENSE_PNG)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        validateIdentityData(syncPoller.getFinalResult());
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void testClassifyAnalyzeFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String classifierId1 = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        DocumentIntelligenceAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        String trainingFilesUrl = getClassifierTrainingFilesContainerUrl();
        Map<String, ClassifierDocumentTypeDetails> documentTypes = new HashMap<>();
        documentTypes.put("IRS-1040-A", createBlobContentSource(trainingFilesUrl, "IRS-1040-A/train"));
        documentTypes.put("IRS-1040-B", createBlobContentSource(trainingFilesUrl, "IRS-1040-B/train"));
        documentTypes.put("IRS-1040-C", createBlobContentSource(trainingFilesUrl, "IRS-1040-C/train"));
        documentTypes.put("IRS-1040-D", createBlobContentSource(trainingFilesUrl, "IRS-1040-D/train"));
        documentTypes.put("IRS-1040-E", createBlobContentSource(trainingFilesUrl, "IRS-1040-E/train"));
        SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
            = adminClient.beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId1, documentTypes))
                .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();
        DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();

        if (documentClassifierDetails != null) {
            String classifierId = documentClassifierDetails.getClassifierId();
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client
                    .beginClassifyDocument(documentClassifierDetails.getClassifierId(),
                        new ClassifyDocumentOptions(getData(IRS_1040)))
                    .setPollInterval(durationTestMode);
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            // TODO: (service bug) AnalyzedDocument count should be 3
            assertEquals(1, analyzeResult.getDocuments().size());
            assertEquals(analyzeResult.getModelId(), classifierId);
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void testClassifyAnalyze(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentIntelligenceAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        String trainingFilesUrl = getClassifierTrainingFilesContainerUrl();
        Map<String, ClassifierDocumentTypeDetails> documentTypes = new HashMap<>();
        documentTypes.put("IRS-1040-A", createBlobContentSource(trainingFilesUrl, "IRS-1040-A/train"));
        documentTypes.put("IRS-1040-B", createBlobContentSource(trainingFilesUrl, "IRS-1040-B/train"));
        documentTypes.put("IRS-1040-C", createBlobContentSource(trainingFilesUrl, "IRS-1040-C/train"));
        documentTypes.put("IRS-1040-D", createBlobContentSource(trainingFilesUrl, "IRS-1040-D/train"));
        documentTypes.put("IRS-1040-E", createBlobContentSource(trainingFilesUrl, "IRS-1040-E/train"));
        SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller = adminClient
            .beginBuildClassifier(new BuildDocumentClassifierOptions("classifierId" + UUID.randomUUID(), documentTypes))
            .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();
        DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();

        if (documentClassifierDetails != null) {
            String classifierId = documentClassifierDetails.getClassifierId();
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client
                    .beginClassifyDocument(documentClassifierDetails.getClassifierId(),
                        new ClassifyDocumentOptions(getData(IRS_1040)))
                    .setPollInterval(durationTestMode);
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            // TODO: (service bug) AnalyzedDocument count should be 3
            assertEquals(1, analyzeResult.getDocuments().size());
            assertEquals(analyzeResult.getModelId(), classifierId);
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void getAnalyzePdf(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String modelID = "prebuilt-read";
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument(modelID,
                new AnalyzeDocumentOptions(getData(LAYOUT_SAMPLE)).setOutput(Collections.singletonList(PDF)))
            .setPollInterval(durationTestMode);
        String resultId = syncPoller.poll().getValue().getResultId();
        syncPoller.waitForCompletion();

        BinaryData pdf = client.getAnalyzeResultPdf(modelID, resultId);
        byte[] pdfHeader = Arrays.copyOfRange(pdf.toBytes(), 0, 5);

        // A PDF's header is expected to be: %PDF-
        Assertions.assertArrayEquals(new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D }, pdfHeader);
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void getAnalyzeFigures(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String modelID = "prebuilt-layout";
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
            .beginAnalyzeDocument(modelID,
                new AnalyzeDocumentOptions(getData(LAYOUT_SAMPLE))
                    .setOutput(Collections.singletonList(AnalyzeOutputFormat.FIGURES)))
            .setPollInterval(durationTestMode);
        String resultId = syncPoller.poll().getValue().getResultId();
        syncPoller.waitForCompletion();
        AnalyzeResult analyzeResult = syncPoller.getFinalResult();
        Assertions.assertNotNull(analyzeResult);
        Assertions.assertFalse(analyzeResult.getFigures().isEmpty());
        String figureId = analyzeResult.getFigures().get(0).getId();
        BinaryData figures = client.getAnalyzeResultFigure(modelID, resultId, figureId);
        byte[] figuresHeader = Arrays.copyOfRange(figures.toBytes(), 0, 5);

        // A PNG's header is expected to start with: ‰PNG
        Assertions.assertArrayEquals(new byte[] { (byte) -119, 80, 78, 71, 13 }, figuresHeader);

    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeBatchDocuments(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String trainingFilesUrl = getBatchTrainingFilesContainerUrl();
        String trainingFilesResultUrl = getBatchTrainingFilesResultContainerUrl();
        SyncPoller<AnalyzeBatchOperationDetails, AnalyzeBatchResult> syncPoller
            = client
                .beginAnalyzeBatchDocuments("prebuilt-layout",
                    new AnalyzeBatchDocumentsOptions(new AzureBlobContentSource(trainingFilesUrl),
                        trainingFilesResultUrl).setResultPrefix("result/").setOverwriteExisting(true))
                .setPollInterval(durationTestMode);

        AnalyzeBatchResult analyzeBatchResult = syncPoller.getFinalResult();
        assertNotNull(analyzeBatchResult.getDetails().get(0).getResultUrl());
    }
}
