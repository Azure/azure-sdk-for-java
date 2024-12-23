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
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.documentintelligence.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.documentintelligence.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.documentintelligence.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.documentintelligence.TestUtils.INVOICE_PDF;
import static com.azure.ai.documentintelligence.TestUtils.IRS_1040;
import static com.azure.ai.documentintelligence.TestUtils.LAYOUT_SAMPLE;
import static com.azure.ai.documentintelligence.TestUtils.LICENSE_PNG;
import static com.azure.ai.documentintelligence.TestUtils.MULTIPAGE_INVOICE_PDF;
import static com.azure.ai.documentintelligence.TestUtils.RECEIPT_CONTOSO_JPG;
import static com.azure.ai.documentintelligence.TestUtils.urlRunner;
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
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", new AnalyzeDocumentOptions(data))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
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
        urlRunner((urlSource) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", new AnalyzeDocumentOptions(urlSource))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
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
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(data))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult());
        }, CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLayoutWithPages(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client
                    .beginAnalyzeDocument("prebuilt-layout",
                        new AnalyzeDocumentOptions(data).setPages(Arrays.asList("1, 2")))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            assertEquals(2, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    // Content - URL

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLayoutFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(urlSource -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(urlSource))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult());
        }, CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeGermanContentFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", new AnalyzeDocumentOptions(data))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateGermanContentData(syncPoller.getFinalResult());
        }, CONTENT_GERMAN_PDF);
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
        dataRunner((data, dataLength) -> buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller = adminClient
                .beginBuildDocumentModel(
                    new BuildDocumentModelOptions("modelID" + UUID.randomUUID(), DocumentBuildMode.TEMPLATE)
                        .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            String modelId = buildModelPoller.getFinalResult().getModelId();

            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(modelId, new AnalyzeDocumentOptions(data))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();

            adminClient.deleteModel(modelId);
            validateJpegCustomDocument(syncPoller.getFinalResult());
        }), CONTENT_FORM_JPG);
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
        urlRunner((urlSource) -> buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller = adminClient
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId1, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(modelId, new AnalyzeDocumentOptions(urlSource))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            adminClient.deleteModel(modelId);

            validateJpegCustomDocument(syncPoller.getFinalResult());
        }), CONTENT_FORM_JPG);
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
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(data))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    // invoice - URL

    /**
     * Verifies invoice card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeInvoiceSourceUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((urlSource) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(urlSource))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void invoiceValidLocale(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", new AnalyzeDocumentOptions(data))
                    .setPollInterval(durationTestMode);
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    // Identity AnalyzedDocument Recognition

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLicenseCardData(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", new AnalyzeDocumentOptions(data))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_PNG);
    }

    // Identity AnalyzedDocument - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeLicenseSourceUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(urlSource -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", new AnalyzeDocumentOptions(urlSource))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_PNG);
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void testClassifyAnalyzeFromUrl(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String classifierId1 = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        DocumentIntelligenceAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        AtomicReference<DocumentClassifierDetails> documentClassifierDetails = new AtomicReference<>();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
            documentTypeDetailsMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
            documentTypeDetailsMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
            documentTypeDetailsMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
            documentTypeDetailsMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
                = adminClient
                    .beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId1, documentTypeDetailsMap))
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            documentClassifierDetails.set(buildModelPoller.getFinalResult());

        });

        if (documentClassifierDetails.get() != null) {
            String classifierId = documentClassifierDetails.get().getClassifierId();
            dataRunner((data, dataLength) -> {
                SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                    = client
                        .beginClassifyDocument(documentClassifierDetails.get().getClassifierId(),
                            new ClassifyDocumentOptions(data))
                        .setPollInterval(durationTestMode);
                AnalyzeResult analyzeResult = syncPoller.getFinalResult();
                Assertions.assertNotNull(analyzeResult);
                // TODO: (service bug) AnalyzedDocument count should be 3
                assertEquals(1, analyzeResult.getDocuments().size());
                assertEquals(analyzeResult.getModelId(), classifierId);
            }, IRS_1040);
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void testClassifyAnalyze(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentIntelligenceAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        AtomicReference<DocumentClassifierDetails> documentClassifierDetails = new AtomicReference<>();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
            documentTypeDetailsMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
            documentTypeDetailsMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
            documentTypeDetailsMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
            documentTypeDetailsMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
                = adminClient
                    .beginBuildClassifier(
                        new BuildDocumentClassifierOptions("classifierId" + UUID.randomUUID(), documentTypeDetailsMap))
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            documentClassifierDetails.set(buildModelPoller.getFinalResult());

        });

        if (documentClassifierDetails.get() != null) {
            String classifierId = documentClassifierDetails.get().getClassifierId();
            dataRunner((data, dataLength) -> {
                SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                    = client
                        .beginClassifyDocument(documentClassifierDetails.get().getClassifierId(),
                            new ClassifyDocumentOptions(data))
                        .setPollInterval(durationTestMode);
                AnalyzeResult analyzeResult = syncPoller.getFinalResult();
                Assertions.assertNotNull(analyzeResult);
                // TODO: (service bug) AnalyzedDocument count should be 3
                assertEquals(1, analyzeResult.getDocuments().size());
                assertEquals(analyzeResult.getModelId(), classifierId);
            }, IRS_1040);
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void getAnalyzePdf(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String modelID = "prebuilt-read";
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller = client
                .beginAnalyzeDocument(modelID,
                    new AnalyzeDocumentOptions(data).setOutput(Collections.singletonList(PDF)))
                .setPollInterval(durationTestMode);
            String resultId = syncPoller.poll().getValue().getResultId();
            syncPoller.waitForCompletion();

            BinaryData pdf = client.getAnalyzeResultPdf(modelID, resultId);
            byte[] pdfBytes = pdf.toBytes();
            byte[] pdfHeader = { pdfBytes[0], pdfBytes[1], pdfBytes[2], pdfBytes[3], pdfBytes[4] };

            // A PDF's header is expected to be: %PDF-
            Assertions.assertArrayEquals(new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D }, pdfHeader);
        }, LAYOUT_SAMPLE);
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void getAnalyzeFigures(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        String modelID = "prebuilt-layout";
        dataRunner((data, dataLength) -> {
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> syncPoller
                = client
                    .beginAnalyzeDocument(modelID,
                        new AnalyzeDocumentOptions(data)
                            .setOutput(Collections.singletonList(AnalyzeOutputFormat.FIGURES)))
                    .setPollInterval(durationTestMode);
            String resultId = syncPoller.poll().getValue().getResultId();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertFalse(analyzeResult.getFigures().isEmpty());
            String figureId = analyzeResult.getFigures().get(0).getId();
            BinaryData figures = client.getAnalyzeResultFigure(modelID, resultId, figureId);
            byte[] figuresBytes = figures.toBytes();
            byte[] figuresHeader
                = { figuresBytes[0], figuresBytes[1], figuresBytes[2], figuresBytes[3], figuresBytes[4] };

            // A PNG's header is expected to start with: ‰PNG
            Assertions.assertArrayEquals(new byte[] { (byte) -119, 80, 78, 71, 13 }, figuresHeader);
        }, LAYOUT_SAMPLE);

    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void analyzeBatchDocuments(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        buildBatchModelRunner((trainingFilesUrl, trainingFilesResultUrl) -> {

            SyncPoller<AnalyzeBatchOperationDetails, AnalyzeBatchResult> syncPoller = client
                .beginAnalyzeBatchDocuments("prebuilt-layout",
                    new AnalyzeBatchDocumentsOptions(new AzureBlobContentSource(trainingFilesUrl),
                        trainingFilesResultUrl).setResultPrefix("result/").setOverwriteExisting(true))
                .setPollInterval(durationTestMode);

            AnalyzeBatchResult analyzeBatchResult = syncPoller.getFinalResult();
            assertNotNull(analyzeBatchResult.getDetails().get(0).getResultUrl());
        });
    }
}
