// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.AzureBlobContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisFeature;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.ResponseError;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.GERMAN_PNG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVOICE_6_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVOICE_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.IRS_1040;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.LICENSE_PNG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_BUSINESS_CARD_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_INVOICE_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_RECEIPT_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.MULTIPAGE_VENDOR_INVOICE_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.RECEIPT_CONTOSO_JPG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.RECEIPT_CONTOSO_PNG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.SELECTION_MARK_PDF;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.W2_JPG;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.damagedPdfDataRunner;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.encodedBlankSpaceSourceUrlRunner;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.getContentDetectionFileData;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.invalidSourceUrlRunner;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.localFilePathRunner;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.urlRunner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentAnalysisAsyncClientTest extends DocumentAnalysisClientTestBase {

    private DocumentAnalysisAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertAsync()
            .build();
    }

    private DocumentAnalysisAsyncClient getDocumentAnalysisAsyncClient(HttpClient httpClient,
                                                                       DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentAnalysisBuilder(
            buildAsyncAssertingClient(httpClient == null ? interceptorManager.getPlaybackClient()
                : httpClient),
            serviceVersion,
            false)
            .buildAsyncClient();
    }

    private DocumentModelAdministrationAsyncClient getDocumentAdminAsyncClient(HttpClient httpClient,
                                                                               DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(
            buildAsyncAssertingClient(httpClient == null ? interceptorManager.getPlaybackClient()
                : httpClient),
            serviceVersion,
            false)
            .buildAsyncClient();
    }

    // Receipt recognition

    // Receipt - non-URL

    /**
     * Verifies receipt data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(
                    "prebuilt-receipt",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptDataNullData(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-receipt", null)
                .setPollInterval(durationTestMode).getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(
                    "prebuilt-receipt",
                    BinaryData.fromStream(getContentDetectionFileData(filePath), dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data from a document using PNG file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithPngFile(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptFromDataMultiPage(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-receipt", BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller().getFinalResult());
            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            validateEncodedUrlExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptInvalidSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    /**
     * Verifies receipt data for a document using source as PNG file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrlWithPngFile(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePngReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_PNG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("Until file available on github main")
    public void analyzeReceiptFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(documentUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", documentUrl).setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        }, MULTIPAGE_RECEIPT_PDF);
    }


    // Content Recognition

    // Content - non-URL

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContent(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult());
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentResultWithNullData(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-layout", null)
                .setPollInterval(durationTestMode)
                .getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using content/layout API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
                                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout",
                    BinaryData.fromStream(getContentDetectionFileData(filePath), dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromDataMultiPage(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller().getFinalResult());
            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithSelectionMarks(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();

            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout",
                    BinaryData.fromStream(data, dataLength),
                    new AnalyzeDocumentOptions().setPages(Collections.singletonList("1")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(1, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithPages(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout",
                    BinaryData.fromStream(data, dataLength),
                    new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "2")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(2, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithPageRange(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout",
                    BinaryData.fromStream(data, dataLength),
                    new AnalyzeDocumentOptions().setPages(Arrays.asList("1-2", "3")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(3, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    // Content - URL

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                    .setPollInterval(durationTestMode).getSyncPoller().getFinalResult());
            validateEncodedUrlExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies layout data for a pdf url
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromUrlWithPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePdfContentData(syncPoller.getFinalResult());
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentInvalidSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-layout", invalidSourceUrl)
                    .setPollInterval(durationTestMode).getSyncPoller().getFinalResult());
            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner((documentUrl) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", documentUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeGermanContentFromUrl(HttpClient httpClient,
                                              DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl,
                    new AnalyzeDocumentOptions().setLocale("de"))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateGermanContentData(syncPoller.getFinalResult());
        }, CONTENT_GERMAN_PDF);
    }

    // Business Card Recognition

    /**
     * Verifies business card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataNullData(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-businessCard", null)
                .setPollInterval(durationTestMode).getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller =
                client.beginAnalyzeDocument("prebuilt-businessCard",
                        BinaryData.fromStream(getContentDetectionFileData(filePath), dataLength))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data from a document using PNG file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithPngFile(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();

            syncPoller.waitForCompletion();
            validateMultipageBusinessData(syncPoller.getFinalResult());
        }, MULTIPAGE_BUSINESS_CARD_PDF);
    }

    // Business Card - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();

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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                    .setPollInterval(durationTestMode).getSyncPoller().getFinalResult());
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    /**
     * Verifies business card data for a document using source as PNG file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
                                                          DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();

            validateMultipageBusinessData(syncPoller.getFinalResult());
        }, MULTIPAGE_BUSINESS_CARD_PDF);
    }

    /**
     * Verify pages parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void receiptWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl,
                    new AnalyzeDocumentOptions().setPages(Collections.singletonList("1")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();

            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(1, analyzeResult.getPages().size());
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verify pages parameter passed when specified by user for business cards API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void businessCardWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl,
                    new AnalyzeDocumentOptions().setPages(Collections.singletonList("1")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();

            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(1, analyzeResult.getPages().size());
        }, BUSINESS_CARD_JPG);
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice",
                    BinaryData.fromStream(getContentDetectionFileData(filePath), dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller());
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl)
            -> assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()));
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("until service regression is fixed #33187")
    public void invoiceValidLocale(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            final SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl,
                    new AnalyzeDocumentOptions().setLocale("en-US"))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.getFinalResult();
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeInvoiceWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            final SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", sourceUrl,
                    new AnalyzeDocumentOptions()
                        .setLocale("en-US")
                        .setPages(Collections.singletonList("1")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();

            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(1, analyzeResult.getPages().size());
        }, INVOICE_PDF);
    }

    // identity document Recognition

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeLicenseCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();

            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_PNG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeIDDocumentDataNullData(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-idDocument", null)
                .setPollInterval(durationTestMode).getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeLicenseDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument",
                    BinaryData.fromStream(getContentDetectionFileData(filePath), dataLength))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode).getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-idDocument",
                        BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    // Identity document - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeLicenseSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
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
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException
                = assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", invalidSourceUrl)
                        .setPollInterval(durationTestMode).getSyncPoller().getFinalResult());
            ResponseError responseError = (ResponseError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    /**
     * Verifies that languages are returned on analyze result when using "prebuilt-read".
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testDocumentLanguagePrebuiltRead(HttpClient httpClient,
                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-read", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertNotNull(analyzeResult.getLanguages());
        }, INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testGermanDocumentLanguagePrebuiltRead(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode).getSyncPoller();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertNotNull("de", analyzeResult.getLanguages().get(0).getLocale());
        }, GERMAN_PNG);
    }
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeW2Data(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-tax.us.w2",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller.waitForCompletion();
            validateW2Data(syncPoller.getFinalResult());
        }, W2_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @DoNotRecord(skipInPlayback = true)
    public void analyzeDocumentInvalidLength(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> client.beginAnalyzeDocument("prebuilt-tax.us.w2", BinaryData.fromStream(data))
                .setPollInterval(durationTestMode).getSyncPoller());
            Assertions.assertEquals("'document length' is required and cannot be null", illegalArgumentException.getMessage());
        }, W2_JPG);
    }

    /**
     * Verifies support for pptx when using "prebuilt-read".
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testPptDocumentPrebuiltRead(HttpClient httpClient,
                                            DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode).getSyncPoller();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertEquals("This is a pptx example.", analyzeResult.getContent());
        }, EXAMPLE_PPTX);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testHtmlDocumentPrebuiltRead(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode).getSyncPoller();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertTrue(analyzeResult.getContent().contains("html example."));
        }, EXAMPLE_HTML);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testDocxDocumentPrebuiltRead(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode).getSyncPoller();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertEquals("This is a docx example.", analyzeResult.getContent());
        }, EXAMPLE_DOCX);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void testXlsxDocumentPrebuiltRead(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-read",
                    BinaryData.fromStream(data, dataLength))
                .setPollInterval(durationTestMode).getSyncPoller();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertNotNull(analyzeResult);
            Assertions.assertTrue(analyzeResult.getContent().contains("This is a xlsx example."));
        }, EXAMPLE_XLSX);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentWithQueryFields(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-document",
                    BinaryData.fromStream(data, dataLength),
                    new AnalyzeDocumentOptions().setDocumentAnalysisFeatures(Collections.singletonList(
                        DocumentAnalysisFeature.QUERY_FIELDS_PREMIUM)).setQueryFields(Collections.singletonList("Charges")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals("$56,651.49", analyzeResult.getDocuments().get(0).getFields().get("Charges").getValueAsString());
        }, INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void analyzeContentUrlWithQueryFields(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-document",
                    sourceUrl,
                    new AnalyzeDocumentOptions().setDocumentAnalysisFeatures(Collections.singletonList(
                        DocumentAnalysisFeature.QUERY_FIELDS_PREMIUM)).setQueryFields(Collections.singletonList("Charges")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals("$56,651.49",
                analyzeResult.getDocuments().get(0).getFields().get("Charges").getValueAsString());
        }, INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/34365")
    public void testClassifyAnalyzeFromUrl(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        DocumentModelAdministrationAsyncClient adminClient = getDocumentAdminAsyncClient(httpClient, serviceVersion);
        AtomicReference<DocumentClassifierDetails> documentClassifierDetails = new AtomicReference<>();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap
                = new HashMap<String, ClassifierDocumentTypeDetails>();
            documentTypeDetailsMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
            documentTypeDetailsMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
            documentTypeDetailsMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
            documentTypeDetailsMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
            documentTypeDetailsMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));
            SyncPoller<OperationResult, DocumentClassifierDetails> buildModelPoller =
                adminClient.beginBuildDocumentClassifier(documentTypeDetailsMap)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            buildModelPoller.waitForCompletion();
            documentClassifierDetails.set(buildModelPoller.getFinalResult());

        });

        if (documentClassifierDetails.get() != null) {
            dataRunner((data, dataLength) -> {
                SyncPoller<OperationResult, AnalyzeResult>
                    syncPoller
                    = client.beginClassifyDocument(documentClassifierDetails.get().getClassifierId(),
                        BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode).getSyncPoller();
                AnalyzeResult analyzeResult = syncPoller.getFinalResult();
                Assertions.assertNotNull(analyzeResult);
                Assertions.assertTrue(analyzeResult.getContent().contains("This is a xlsx example."));
                Assertions.assertEquals(2, analyzeResult.getDocuments().size());
            }, IRS_1040);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/34365")
    public void testClassifyAnalyze(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        DocumentModelAdministrationAsyncClient adminClient = getDocumentAdminAsyncClient(httpClient, serviceVersion);
        AtomicReference<DocumentClassifierDetails> documentClassifierDetails = new AtomicReference<>();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap
                = new HashMap<String, ClassifierDocumentTypeDetails>();
            documentTypeDetailsMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
            documentTypeDetailsMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
            documentTypeDetailsMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
            documentTypeDetailsMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
            documentTypeDetailsMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));
            SyncPoller<OperationResult, DocumentClassifierDetails> buildModelPoller =
                adminClient.beginBuildDocumentClassifier(documentTypeDetailsMap)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            buildModelPoller.waitForCompletion();
            documentClassifierDetails.set(buildModelPoller.getFinalResult());

        });

        if (documentClassifierDetails.get() != null) {
            dataRunner((data, dataLength) -> {
                SyncPoller<OperationResult, AnalyzeResult>
                    syncPoller
                    = client.beginClassifyDocument(documentClassifierDetails.get().getClassifierId(),
                        BinaryData.fromStream(data, dataLength))
                    .setPollInterval(durationTestMode).getSyncPoller();
                AnalyzeResult analyzeResult = syncPoller.getFinalResult();
                Assertions.assertNotNull(analyzeResult);
                Assertions.assertEquals(2, analyzeResult.getDocuments().size());
            }, IRS_1040);
        }
    }
}
