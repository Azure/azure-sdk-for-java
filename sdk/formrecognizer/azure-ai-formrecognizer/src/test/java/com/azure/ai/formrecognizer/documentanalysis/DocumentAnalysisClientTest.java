// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BlobContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentWord;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.ResponseError;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.BUSINESS_CARD_JPG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.BUSINESS_CARD_PNG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.EXAMPLE_DOCX;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.EXAMPLE_HTML;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.EXAMPLE_PPTX;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.EXAMPLE_XLSX;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVOICE_6_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVOICE_NO_SUB_LINE_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVOICE_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.IRS_1040;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.LICENSE_PNG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_BUSINESS_CARD_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_INVOICE_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_RECEIPT_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_VENDOR_INVOICE_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.NON_EXIST_MODEL_ID;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.RECEIPT_CONTOSO_JPG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.RECEIPT_CONTOSO_PNG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.SELECTION_MARK_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.damagedPdfDataRunner;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.encodedBlankSpaceSourceUrlRunner;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.invalidSourceUrlRunner;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.urlRunner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentAnalysisClientTest extends DocumentAnalysisClientTestBase {
    private DocumentAnalysisClient client;

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertSync()
            .build();
    }
    private DocumentAnalysisClient getDocumentAnalysisClient(HttpClient httpClient,
                                                             DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentAnalysisBuilder(
            buildSyncAssertingClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion,
            true)
            .buildClient();
    }

    private DocumentModelAdministrationClient getDocumentModelAdminClient(HttpClient httpClient,
                                                                          DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(
            buildSyncAssertingClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion,
            true)
            .buildClient();
    }

    // Receipt recognition
    // Receipt - non-URL

    /**
     * Verifies receipt data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies content type will be auto-detected when using receipt API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);

            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data from a document using PNG file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithPngFile(HttpClient httpClient,
                                              DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validatePngReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_PNG);
    }

    /**
     * Verifies receipt data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithBlankPdf(HttpClient httpClient,
                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptFromDataMultiPage(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        }, MULTIPAGE_RECEIPT_PDF);
    }

    /**
     * Verify that receipt recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptFromDamagedPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException
                = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
            ResponseError responseError =
                (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    // Receipt - URL

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
    }


    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize receipt from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException
                = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                        .setPollInterval(durationTestMode));
            validateEncodedUrlExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptInvalidSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl) ->
            Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                    .setPollInterval(durationTestMode)));
    }

    /**
     * Verifies receipt data for a document using source as PNG file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrlWithPngFile(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validatePngReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_PNG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(receiptUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", receiptUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        }, MULTIPAGE_RECEIPT_PDF);
    }

    // Content Recognition

    // Content - non-URL

    /**
     * Verifies layout/content data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContent(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult());
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies content type will be auto-detected when using content/layout API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult());
        }, CONTENT_FORM_JPG);

    }

    /**
     * Verifies blank form file is still a valid file to process
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentResultWithBlankPdf(HttpClient httpClient,
                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromDataMultiPage(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            assertEquals(3, analyzeResult.getPages().size());
            validateMultipageLayoutContent(analyzeResult);
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verify that content recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromDamagedPdf(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException errorResponseException
                = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
            ResponseError responseError =
                (ResponseError) errorResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithSelectionMarks(HttpClient httpClient,
                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength),
                    new AnalyzeDocumentOptions().setPages(Collections.singletonList("1")), Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(1, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithPages(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength),
                    new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "2")), Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(2, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    // Content - URL

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult());
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize a content from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException
                = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                        .setPollInterval(durationTestMode));
            validateEncodedUrlExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies layout data for a pdf url
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromUrlWithPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validatePdfContentData(syncPoller.getFinalResult());
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies that an exception is thrown for invalid source url for recognizing content/layout information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentInvalidSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl)
            -> Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-layout", invalidSourceUrl)
                    .setPollInterval(durationTestMode)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl).setPollInterval(durationTestMode);

            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            assertEquals(3, analyzeResult.getPages().size());
            validateMultipageLayoutContent(analyzeResult);
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithSelectionMarksFromUrl(HttpClient httpClient,
                                                        DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeGermanContentFromUrl(HttpClient httpClient,
                                            DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout",
                    sourceUrl,
                    new AnalyzeDocumentOptions().setLocale("de"),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateGermanContentData(syncPoller.getFinalResult());
        }, CONTENT_GERMAN_PDF);
    }

    // Custom Document recognition

    /**
     * Verifies custom form data for a document using source as input stream data and valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocument(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller =
                    adminClient
                        .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<OperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        BinaryData.fromStream(data,
                        dataLength))
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();

                adminClient.deleteDocumentModel(modelId);
                validateJpegCustomDocument(syncPoller.getFinalResult());
            }), CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for a blank PDF content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentBlankPdf(HttpClient httpClient,
                                              DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller =
                    adminClient
                        .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<OperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        BinaryData.fromStream(data,
                        dataLength))
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                adminClient.deleteDocumentModel(modelId);

                validateBlankPdfData(syncPoller.getFinalResult());
            }), BLANK_PDF);
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentWithContentTypeAutoDetection(HttpClient httpClient,
                                                                  DocumentAnalysisServiceVersion serviceVersion) {

        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller =
                    adminClient
                        .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<OperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        BinaryData.fromStream(data,
                        dataLength))
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                adminClient.deleteDocumentModel(modelId);

                validateJpegCustomDocument(syncPoller.getFinalResult());
            }), CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentMultiPage(HttpClient httpClient,
                                               DocumentAnalysisServiceVersion serviceVersion) {

        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> multipageTrainingRunner((trainingFilesUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller
                = adminClient
                .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();

            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(
                    modelId,
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            adminClient.deleteDocumentModel(modelId);

            validateMultiPagePdfData(syncPoller.getFinalResult());
        }), MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentWithSelectionMark(HttpClient httpClient,
                                                       DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            selectionMarkTrainingRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller =
                    adminClient
                        .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<OperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        BinaryData.fromStream(data,
                        dataLength))
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                adminClient.deleteDocumentModel(modelId);
                validateCustomDocumentWithSelectionMarks(syncPoller.getFinalResult());
            }), SELECTION_MARK_PDF);
    }

    // Custom Document - URL

    /**
     * Verifies custom form data for an URL document data without labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentUrl(HttpClient httpClient,
                                         DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        urlRunner((fileUrl) -> buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller
                = adminClient
                .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl(
                    modelId,
                    fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            adminClient.deleteDocumentModel(modelId);

            validateJpegCustomDocument(syncPoller.getFinalResult());
        }), CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentUrlMultiPage(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        testingContainerUrlRunner((fileUrl) -> multipageTrainingRunner((trainingFilesUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller
                = adminClient
                .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();

            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl(
                    modelId, fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            adminClient.deleteDocumentModel(modelId);

            validateMultiPagePdfData(syncPoller.getFinalResult());
        }), MULTIPAGE_INVOICE_PDF);
    }

    // Custom Document - URL

    /**
     * Verifies that an exception is thrown for invalid training data source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentInvalidSourceUrl(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> syncPoller
                = getDocumentModelAdminClient(httpClient, serviceVersion).beginBuildDocumentModel(trainingFilesUrl,
                    DocumentModelBuildMode.TEMPLATE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModelDetails createdModel = syncPoller.getFinalResult();

            HttpResponseException httpResponseException = Assertions.assertThrows(
                HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl(
                        createdModel.getModelId(),
                        INVALID_URL)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            final ResponseError responseError =
                (ResponseError) httpResponseException.getValue();

            adminClient.deleteDocumentModel(createdModel.getModelId());

            Assertions.assertEquals("InvalidArgument", responseError.getCode());
        });
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with \
     * encoded blank space as input data to recognize a custom form from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                           DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl(NON_EXIST_MODEL_ID, sourceUrl)
                    .setPollInterval(durationTestMode));
            validateEncodedUrlExceptionSource(errorResponseException);
        });
    }

    /**
     * Verify that custom document with invalid model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentUrlNonExistModelId(HttpClient httpClient,
                                                        DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl(NON_EXIST_MODEL_ID, fileUrl)
                    .setPollInterval(durationTestMode));
            ResponseError responseError = (ResponseError) errorResponseException.getValue();
            Assertions.assertEquals("NotFound", responseError.getCode());
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verify that custom form with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentDamagedPdf(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl -> {
                SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller
                    = adminClient
                    .beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                    .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();
                String modelId = buildModelPoller.getFinalResult().getModelId();

                HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocument(modelId,
                            BinaryData.fromStream(data, dataLength))
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
                adminClient.deleteDocumentModel(modelId);

                ResponseError responseError = (ResponseError) httpResponseException.getValue();
                Assertions.assertEquals("InvalidRequest", responseError.getCode());
            })));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeCustomDocumentUrlWithSelectionMark(HttpClient httpClient,
                                                          DocumentAnalysisServiceVersion serviceVersion) {
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> selectionMarkTrainingRunner((trainingFilesUrl) -> {
            client = getDocumentAnalysisClient(httpClient, serviceVersion);

            SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller
                = adminClient.beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();

            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl(modelId,
                    fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();

            adminClient.deleteDocumentModel(modelId);
            validateCustomDocumentWithSelectionMarks(syncPoller.getFinalResult());
        }), SELECTION_MARK_PDF);
    }

    // Business card recognition

    // Business card - non-URL

    /**
     * Verifies business card data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies content type will be auto-detected when using business card API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data from a document using PNG file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithPngFile(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_PNG);
    }

    /**
     * Verifies business card data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithBlankPdf(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that business card recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardFromDamagedPdf(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    /**
     * Verify business card recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeMultipageBusinessCard(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageBusinessData(syncPoller.getFinalResult());
        }, MULTIPAGE_BUSINESS_CARD_PDF);
    }

    // business card - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize business card from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                         DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                    .setPollInterval(durationTestMode));
            validateEncodedUrlExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardInvalidSourceUrl(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl) -> Assertions.assertThrows(HttpResponseException.class,
            () -> client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                .setPollInterval(durationTestMode)));
    }


    /**
     * Verifies business card data for a document using source as PNG file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
                                                        DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_PNG);
    }

    /**
     * Verify business card recognition with multipage pdf url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeMultipageBusinessCardUrl(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageBusinessData(syncPoller.getFinalResult());
        }, MULTIPAGE_BUSINESS_CARD_PDF);
    }

    // Invoice recognition

    // Invoice - non-URL

    /**
     * Verifies invoice data recognition  for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void analyzeInvoiceData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    /**
     * Verifies content type will be auto-detected when using invoice API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void analyzeInvoiceDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    /**
     * Verifies invoice data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeInvoiceDataWithBlankPdf(HttpClient httpClient,
                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that invoice recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeInvoiceFromDamagedPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    /**
     * Verify invoice data recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void analyzeMultipageInvoice(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        // confirm if pageResults should be returned for prebuilt model recognition
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageInvoiceData(syncPoller.getFinalResult());
        }, MULTIPAGE_VENDOR_INVOICE_PDF);
    }

    // invoice - URL

    /**
     * Verifies invoice card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void analyzeInvoiceSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize invoice card from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeInvoiceFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl)
                    .setPollInterval(durationTestMode));
            validateEncodedUrlExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeInvoiceInvalidSourceUrl(HttpClient httpClient,
                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl)
            -> Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl).setPollInterval(durationTestMode)));
    }

    /**
     * Verifies invoice data for a document using source as file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void analyzeInvoiceFromUrlIncludeFieldElements(HttpClient httpClient,
                                                          DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void invoiceValidLocale(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    /**
     * Verify SDK returns empty object and array for null sub line items field.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void invoiceSubLineItemsNull(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            AnalyzeResult analyzeResult = client.beginAnalyzeDocument("prebuilt-invoice",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getFinalResult();

            AnalyzedDocument analyzedDocument = analyzeResult.getDocuments().get(0);
            DocumentField itemFieldList = analyzedDocument.getFields().get("Items").getValueAsList().get(0);
            Map<String, DocumentField> documentFieldMap = itemFieldList.getValueAsMap();

            Assertions.assertNull(documentFieldMap);
            Assertions.assertEquals(String.valueOf(1), itemFieldList.getContent());

        }, INVOICE_NO_SUB_LINE_PDF);
    }

    // Identity Document Recognition

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeLicenseCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_PNG);
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeLicenseDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_PNG);
    }

    /**
     * Verifies identity document data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeIDDocumentWithBlankPdf(HttpClient httpClient,
                                              DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that identity document recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeIDDocumentFromDamagedPdf(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-idDocument",
                        BinaryData.fromStream(data,
                        dataLength))
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    // Identity Document - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeLicenseSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_PNG);
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeIDDocumentInvalidSourceUrl(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            ResponseError responseError = (ResponseError) errorResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled
    public void testGetWordsInALine(HttpClient httpClient,
                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-document", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            List<DocumentWord> actualWords =
                analyzeResult.getPages().get(0).getLines().get(2).getWords();
            List<String> expectedWords = Arrays.stream("1 Redmond way Suite".split(" ")).collect(Collectors.toList());
            int expectedWordCount = 4;
            assertEquals(expectedWordCount, actualWords.size());
            AtomicInteger i = new AtomicInteger(0);
            actualWords.forEach(documentWord -> assertEquals(expectedWords.get(i.getAndIncrement()), documentWord.getContent()));
        }, INVOICE_PDF);
    }

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @DoNotRecord(skipInPlayback = true)
    public void analyzeDataWithInvalidLength(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> client.beginAnalyzeDocument("prebuilt-idDocument", BinaryData.fromStream(data, null))
                .setPollInterval(durationTestMode));
            Assertions.assertEquals("'document length' is required and cannot be null", illegalArgumentException.getMessage());
        }, LICENSE_PNG);
    }

    /**
     * Verifies support for pptx when using "prebuilt-read".
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testPptDocumentPrebuiltRead(HttpClient httpClient,
                                            DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertEquals("This is a pptx example.", analyzeResult.getContent());
        }, EXAMPLE_PPTX);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testHtmlDocumentPrebuiltRead(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertTrue(analyzeResult.getContent().contains("html example."));
        }, EXAMPLE_HTML);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testDocxDocumentPrebuiltRead(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertEquals("This is a docx example.", analyzeResult.getContent());
        }, EXAMPLE_DOCX);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testXlsxDocumentPrebuiltRead(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode);
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertTrue(analyzeResult.getContent().contains("This is a xlsx example."));
        }, EXAMPLE_XLSX);
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testClassifyAnalyzeFromUrl(HttpClient httpClient,
                                           DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        AtomicReference<DocumentClassifierDetails> documentClassifierDetails = new AtomicReference<>();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-A/train")));
            documentTypeDetailsMap.put("IRS-1040-B",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-B/train")));
            documentTypeDetailsMap.put("IRS-1040-C",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-C/train")));
            documentTypeDetailsMap.put("IRS-1040-D",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-D/train")));
            documentTypeDetailsMap.put("IRS-1040-E",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-E/train")));
            SyncPoller<OperationResult, DocumentClassifierDetails> buildModelPoller =
                adminClient.beginBuildDocumentClassifier(documentTypeDetailsMap)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            documentClassifierDetails.set(buildModelPoller.getFinalResult());

        });

        if (documentClassifierDetails.get() != null) {
            String classifierId = documentClassifierDetails.get().getClassifierId();
            dataRunner((data, dataLength) -> {
                SyncPoller<OperationResult, AnalyzeResult>
                    syncPoller
                    = client.beginClassifyDocument(documentClassifierDetails.get().getClassifierId(),
                        BinaryData.fromStream(data, dataLength), Context.NONE)
                    .setPollInterval(durationTestMode);
                AnalyzeResult analyzeResult = syncPoller.getFinalResult();
                Assertions.assertNotNull(analyzeResult);
                Assertions.assertEquals(3, analyzeResult.getDocuments().size());
                Assertions.assertEquals(analyzeResult.getModelId(), classifierId);
            }, IRS_1040);
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testClassifyAnalyze(HttpClient httpClient,
                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        AtomicReference<DocumentClassifierDetails> documentClassifierDetails = new AtomicReference<>();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-A/train")));
            documentTypeDetailsMap.put("IRS-1040-B",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-B/train")));
            documentTypeDetailsMap.put("IRS-1040-C",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-C/train")));
            documentTypeDetailsMap.put("IRS-1040-D",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-D/train")));
            documentTypeDetailsMap.put("IRS-1040-E",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl)
                    .setPrefix("IRS-1040-E/train")));
            SyncPoller<OperationResult, DocumentClassifierDetails> buildModelPoller =
                adminClient.beginBuildDocumentClassifier(documentTypeDetailsMap)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            documentClassifierDetails.set(buildModelPoller.getFinalResult());

        });

        if (documentClassifierDetails.get() != null) {
            String classifierId = documentClassifierDetails.get().getClassifierId();
            dataRunner((data, dataLength) -> {
                SyncPoller<OperationResult, AnalyzeResult>
                    syncPoller
                    = client.beginClassifyDocument(documentClassifierDetails.get().getClassifierId(),
                        BinaryData.fromStream(data, dataLength), Context.NONE)
                    .setPollInterval(durationTestMode);
                AnalyzeResult analyzeResult = syncPoller.getFinalResult();
                Assertions.assertNotNull(analyzeResult);
                Assertions.assertEquals(3, analyzeResult.getDocuments().size());
                Assertions.assertEquals(analyzeResult.getModelId(), classifierId);
            }, IRS_1040);
        }
    }
}
