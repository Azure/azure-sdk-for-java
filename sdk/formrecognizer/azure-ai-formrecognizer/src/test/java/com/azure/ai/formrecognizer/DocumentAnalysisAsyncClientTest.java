// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.util.Utility;
import com.azure.ai.formrecognizer.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.BUSINESS_CARD_JPG;
import static com.azure.ai.formrecognizer.TestUtils.BUSINESS_CARD_PNG;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVOICE_6_PDF;
import static com.azure.ai.formrecognizer.TestUtils.INVOICE_PDF;
import static com.azure.ai.formrecognizer.TestUtils.LICENSE_CARD_JPG;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_BUSINESS_CARD_PDF;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_INVOICE_PDF;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_RECEIPT_PDF;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_VENDOR_INVOICE_PDF;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_CONTOSO_JPG;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_CONTOSO_PNG;
import static com.azure.ai.formrecognizer.TestUtils.SELECTION_MARK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.damagedPdfDataRunner;
import static com.azure.ai.formrecognizer.TestUtils.encodedBlankSpaceSourceUrlRunner;
import static com.azure.ai.formrecognizer.TestUtils.getContentDetectionFileData;
import static com.azure.ai.formrecognizer.TestUtils.invalidSourceUrlRunner;
import static com.azure.ai.formrecognizer.TestUtils.localFilePathRunner;
import static com.azure.ai.formrecognizer.TestUtils.urlRunner;
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

    private DocumentAnalysisAsyncClient getDocumentAnalysisAsyncClient(HttpClient httpClient,
                                                                       DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentAnalysisBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Receipt recognition

    // Receipt - non-URL

    /**
     * Verifies receipt data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(
                    "prebuilt-receipt",
                    Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptDataNullData(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-receipt", null, 0)
                .setPollInterval(durationTestMode).getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(
                    "prebuilt-receipt",
                    Utility.toFluxByteBuffer(getContentDetectionFileData(filePath)),
                    dataLength).setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data from a document using PNG file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithPngFile(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithBlankPdf(HttpClient httpClient,
                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", Utility.toFluxByteBuffer(data), dataLength)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptFromDataMultiPage(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt",
                    Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptFromDamagedPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-receipt", Utility.toFluxByteBuffer(data), dataLength)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller().getFinalResult());
            FormRecognizerError errorInformation =
                (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptInvalidSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            FormRecognizerError errorInformation = (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    /**
     * Verifies receipt data for a document using source as PNG file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrlWithPngFile(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl,
                    new AnalyzeDocumentOptions())
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePngReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_PNG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(documentUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContent(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout",
                    Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentResultWithNullData(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-layout", null, 0)
                .setPollInterval(durationTestMode)
                .getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using content/layout API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
                                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout",
                    Utility.toFluxByteBuffer(getContentDetectionFileData(filePath)), dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentResultWithBlankPdf(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", Utility.toFluxByteBuffer(data), dataLength)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromDataMultiPage(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", Utility.toFluxByteBuffer(data), dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromDamagedPdf(HttpClient httpClient,
                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-layout", Utility.toFluxByteBuffer(data), dataLength)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller().getFinalResult());
            FormRecognizerError errorInformation = (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithSelectionMarks(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", Utility.toFluxByteBuffer(data), dataLength)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();

            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", Utility.toFluxByteBuffer(data),
                    dataLength, new AnalyzeDocumentOptions().setPages(Collections.singletonList("1")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(1, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithPages(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", Utility.toFluxByteBuffer(data),
                    dataLength, new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "2")))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(2, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithPageRange(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", Utility.toFluxByteBuffer(data),
                    dataLength, new AnalyzeDocumentOptions().setPages(Arrays.asList("1-2", "3")))
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromUrlWithPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentInvalidSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-layout", invalidSourceUrl)
                    .setPollInterval(durationTestMode).getSyncPoller().getFinalResult());
            FormRecognizerError errorInformation =
                (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner((documentUrl) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithSelectionMarksFromUrl(HttpClient httpClient,
                                                          DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeGermanContentFromUrl(HttpClient httpClient,
                                              DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataNullData(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-businessCard", null, 0)
                .setPollInterval(durationTestMode).getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller =
                client.beginAnalyzeDocument("prebuilt-businessCard",
                        Utility.toFluxByteBuffer(getContentDetectionFileData(filePath)),
                        dataLength,
                        new AnalyzeDocumentOptions())
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithPngFile(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithBlankPdf(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardFromDamagedPdf(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-businessCard", Utility.toFluxByteBuffer(data), dataLength)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            FormRecognizerError errorInformation =
                (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    /**
     * Verify business card recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeMultipageBusinessCard(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl,
                    new AnalyzeDocumentOptions())
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardInvalidSourceUrl(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            FormRecognizerError errorInformation =
                (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    /**
     * Verifies business card data for a document using source as PNG file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
                                                          DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl,
                    new AnalyzeDocumentOptions())
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeMultipageBusinessCardUrl(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", sourceUrl,
                    new AnalyzeDocumentOptions())
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void receiptWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void businessCardWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice",
                    Utility.toFluxByteBuffer(getContentDetectionFileData(filePath)),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceDataWithBlankPdf(HttpClient httpClient,
                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceFromDamagedPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-invoice", Utility.toFluxByteBuffer(data),
                        dataLength)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());
            FormRecognizerError errorInformation =
                (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    /**
     * Verify invoice data recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeMultipageInvoice(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", Utility.toFluxByteBuffer(data),
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void invoiceValidLocale(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            final SyncPoller<DocumentOperationResult, AnalyzeResult>
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            final SyncPoller<DocumentOperationResult, AnalyzeResult>
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeLicenseCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", Utility.toFluxByteBuffer(data), dataLength)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();

            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_CARD_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeIDDocumentDataNullData(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-idDocument", null, 0)
                .setPollInterval(durationTestMode).getSyncPoller());
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeLicenseDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument",
                    Utility.toFluxByteBuffer(getContentDetectionFileData(filePath)),
                    dataLength)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();

            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_CARD_JPG);
    }

    /**
     * Verifies identity document data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeIDDocumentWithBlankPdf(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument",
                    Utility.toFluxByteBuffer(data),
                    dataLength)
                .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller.waitForCompletion();

            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that identity document recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeIDDocumentFromDamagedPdf(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-idDocument",
                        Utility.toFluxByteBuffer(data),
                        dataLength)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            FormRecognizerError errorInformation = (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }

    // Identity document - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeLicenseSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult>
                syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", sourceUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult());
        }, LICENSE_CARD_JPG);
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeIDDocumentInvalidSourceUrl(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException
                = assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", invalidSourceUrl)
                        .setPollInterval(durationTestMode).getSyncPoller().getFinalResult());
            FormRecognizerError errorInformation =
                (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidRequest", errorInformation.getCode());
        });
    }
}
