// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.ai.formrecognizer.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.BUSINESS_CARD_JPG;
import static com.azure.ai.formrecognizer.TestUtils.BUSINESS_CARD_PNG;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.INVOICE_6_PDF;
import static com.azure.ai.formrecognizer.TestUtils.INVOICE_NO_SUB_LINE_PDF;
import static com.azure.ai.formrecognizer.TestUtils.INVOICE_PDF;
import static com.azure.ai.formrecognizer.TestUtils.LICENSE_CARD_JPG;
import static com.azure.ai.formrecognizer.TestUtils.MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_BUSINESS_CARD_PDF;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_INVOICE_PDF;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_RECEIPT_PDF;
import static com.azure.ai.formrecognizer.TestUtils.MULTIPAGE_VENDOR_INVOICE_PDF;
import static com.azure.ai.formrecognizer.TestUtils.NON_EXIST_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_CONTOSO_JPG;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_CONTOSO_PNG;
import static com.azure.ai.formrecognizer.TestUtils.SELECTION_MARK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.damagedPdfDataRunner;
import static com.azure.ai.formrecognizer.TestUtils.encodedBlankSpaceSourceUrlRunner;
import static com.azure.ai.formrecognizer.TestUtils.invalidSourceUrlRunner;
import static com.azure.ai.formrecognizer.TestUtils.urlRunner;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentAnalysisClientTest extends DocumentAnalysisClientTestBase {

    private DocumentAnalysisClient client;


    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private DocumentAnalysisClient getDocumentAnalysisClient(HttpClient httpClient,
                                                             DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentAnalysisBuilder(httpClient, serviceVersion).buildClient();
    }

    private DocumentModelAdministrationClient getDocumentModelAdminClient(HttpClient httpClient,
                                                                          DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Receipt recognition
    // Receipt - non-URL

    /**
     * Verifies receipt data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt",
                    data,
                    dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        Assertions.assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument(null, null, 0));
    }

    /**
     * Verifies content type will be auto-detected when using receipt API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", data, dataLength)
                .setPollInterval(durationTestMode);

            syncPoller.waitForCompletion();
            validateJpegReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data from a document using PNG file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptDataWithPngFile(HttpClient httpClient,
                                              DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", data, dataLength)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptFromDataMultiPage(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-receipt", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException
                = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocument("prebuilt-receipt", data, dataLength)
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
            FormRecognizerError errorInformation
                = (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidContent", errorInformation.getInnerError().getCode());
        });
    }

    // Receipt - URL

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptSourceUrlWithPngFile(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", sourceUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validatePngReceiptData(syncPoller.getFinalResult());
        }, RECEIPT_CONTOSO_PNG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeReceiptFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(receiptUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContent(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        Assertions.assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-layout", null, 0)
                .setPollInterval(durationTestMode));
    }

    /**
     * Verifies content type will be auto-detected when using content/layout API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", data, dataLength)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromDataMultiPage(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", data, dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromDamagedPdf(HttpClient httpClient,
                                             DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException errorResponseException
                = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocument("prebuilt-layout", data, dataLength)
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
            FormRecognizerError errorInformation
                = (FormRecognizerError) errorResponseException.getValue();
            Assertions.assertEquals("InvalidContent", errorInformation.getInnerError().getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithSelectionMarks(HttpClient httpClient,
                                                 DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", data, dataLength)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", data, dataLength,
                    new AnalyzeDocumentOptions().setPages(Collections.singletonList("1")), Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(1, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentWithPages(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-layout", data, dataLength,
                    new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "2")), Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            AnalyzeResult analyzeResult = syncPoller.getFinalResult();
            Assertions.assertEquals(2, analyzeResult.getPages().size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    // Content - URL

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromUrlWithPdf(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentInvalidSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl)
            -> Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-layout", invalidSourceUrl)
                    .setPollInterval(durationTestMode)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeContentFromUrlMultiPage(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl).setPollInterval(durationTestMode);

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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout", sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateSelectionMarkContentData(syncPoller.getFinalResult());
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeGermanContentFromUrl(HttpClient httpClient,
                                            DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-layout",
                    sourceUrl,
                    new AnalyzeDocumentOptions().setLocale(""),
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocument(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                    adminClient
                        .beginBuildModel(trainingFilesUrl, null)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        data,
                        dataLength)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();

                adminClient.deleteModel(modelId);
                validateJpegCustomDocument(syncPoller.getFinalResult(), modelId);
            }), CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for a blank PDF content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentBlankPdf(HttpClient httpClient,
                                              DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                    adminClient
                        .beginBuildModel(trainingFilesUrl, null)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        data,
                        dataLength)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                adminClient.deleteModel(modelId);

                validateBlankPdfData(syncPoller.getFinalResult());
            }), BLANK_PDF);
    }

    /**
     * Verifies an exception thrown for a document using null form data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentWithNullData(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                    adminClient
                        .beginBuildModel(trainingFilesUrl, null)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                Assertions.assertThrows(RuntimeException.class,
                    () -> client.beginAnalyzeDocument(modelId,
                            (InputStream) null,
                            dataLength)
                        .setPollInterval(durationTestMode));
                adminClient.deleteModel(modelId);

            }), INVOICE_6_PDF);
    }

    /**
     * Verifies an exception thrown for a document using null model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeCustomDocumentWithNullModelId(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            Exception ex = Assertions.assertThrows(RuntimeException.class, () -> client.beginAnalyzeDocument(
                    null,
                    data,
                    dataLength)
                .setPollInterval(durationTestMode));
            Assertions.assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies an exception thrown for an empty model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentWithEmptyModelId(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);

        dataRunner((data, dataLength) -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("",
                        data,
                        dataLength)
                    .setPollInterval(durationTestMode));
            FormRecognizerError errorInformation
                = (FormRecognizerError) errorResponseException.getValue();
            Assertions.assertEquals(404, errorResponseException.getResponse().getStatusCode());
            Assertions.assertEquals("ModelNotFound", errorInformation.getInnerError().getCode());
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentWithContentTypeAutoDetection(HttpClient httpClient,
                                                                  DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                    adminClient
                        .beginBuildModel(trainingFilesUrl, null)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        data,
                        dataLength)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                adminClient.deleteModel(modelId);

                validateJpegCustomDocument(syncPoller.getFinalResult(), modelId);
            }), CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentMultiPage(HttpClient httpClient,
                                               DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> multipageTrainingRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller
                = adminClient
                .beginBuildModel(trainingFilesUrl, null)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();

            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument(
                    modelId,
                    data,
                    dataLength)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            adminClient.deleteModel(modelId);

            validateMultiPagePdfData(syncPoller.getFinalResult(), modelId);
        }), MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentWithSelectionMark(HttpClient httpClient,
                                                       DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            selectionMarkTrainingRunner((trainingFilesUrl) -> {
                SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                    adminClient
                        .beginBuildModel(trainingFilesUrl, null)
                        .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();

                String modelId = buildModelPoller.getFinalResult().getModelId();

                SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                    = client.beginAnalyzeDocument(
                        modelId,
                        data,
                        dataLength)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                adminClient.deleteModel(modelId);
                validateCustomDocumentWithSelectionMarks(syncPoller.getFinalResult());
            }), SELECTION_MARK_PDF);
    }

    // Custom Document - URL

    /**
     * Verifies custom form data for an URL document data without labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentUrl(HttpClient httpClient,
                                         DocumentAnalysisServiceVersion serviceVersion) {
        // null values reported
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        urlRunner((fileUrl) -> multipageTrainingRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller
                = adminClient
                .beginBuildModel(trainingFilesUrl, null)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl(
                    modelId,
                    fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            adminClient.deleteModel(modelId);

            validateJpegCustomDocument(syncPoller.getFinalResult(), modelId);
        }), CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeCustomDocumentUrlMultiPage(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        testingContainerUrlRunner((fileUrl) -> multipageTrainingRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller
                = adminClient
                .beginBuildModel(trainingFilesUrl, null)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();

            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl(
                    modelId, fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            adminClient.deleteModel(modelId);

            validateMultiPagePdfData(syncPoller.getFinalResult(), modelId);
        }), MULTIPAGE_INVOICE_PDF);
    }

    // Custom Document - URL

    /**
     * Verifies that an exception is thrown for invalid training data source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeCustomDocumentInvalidSourceUrl(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller
                = getDocumentModelAdminClient(httpClient, serviceVersion).beginBuildModel(trainingFilesUrl, null)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModel createdModel = syncPoller.getFinalResult();

            HttpResponseException httpResponseException = Assertions.assertThrows(
                HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl(
                        createdModel.getModelId(),
                        INVALID_URL)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            final FormRecognizerError errorInformation
                = (FormRecognizerError) httpResponseException.getValue();

            adminClient.deleteModel(createdModel.getModelId());

            Assertions.assertEquals("InvalidContentSourceFormat", errorInformation.getInnerError().getCode());
        });
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with \
     * encoded blank space as input data to recognize a custom form from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeCustomDocumentUrlNonExistModelId(HttpClient httpClient,
                                                        DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl(NON_EXIST_MODEL_ID, fileUrl)
                    .setPollInterval(durationTestMode));
            FormRecognizerError errorInformation
                = (FormRecognizerError) errorResponseException.getValue();
            Assertions.assertEquals("ModelNotFound", errorInformation.getInnerError().getCode());
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verify that custom form with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentDamagedPdf(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) ->
            buildModelRunner((trainingFilesUrl -> {
                SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller
                    = adminClient
                    .beginBuildModel(trainingFilesUrl, null)
                    .setPollInterval(durationTestMode);
                buildModelPoller.waitForCompletion();
                String modelId = buildModelPoller.getFinalResult().getModelId();

                HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                    () -> client.beginAnalyzeDocument(modelId,
                            data,
                            dataLength)
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
                adminClient.deleteModel(modelId);

                FormRecognizerError errorInformation
                    = (FormRecognizerError) httpResponseException.getValue();
                Assertions.assertEquals("Invalid input file.", errorInformation.getMessage());
            })));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void analyzeCustomDocumentUrlWithSelectionMark(HttpClient httpClient,
                                                          DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        DocumentModelAdministrationClient adminClient = getDocumentModelAdminClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> selectionMarkTrainingRunner((trainingFilesUrl) -> {
            client = getDocumentAnalysisClient(httpClient, serviceVersion);

            SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller
                = adminClient.beginBuildModel(trainingFilesUrl, null)
                .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            String modelId = buildModelPoller.getFinalResult().getModelId();

            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl(modelId,
                    fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();

            adminClient.deleteModel(modelId);
            validateCustomDocumentWithSelectionMarks(syncPoller.getFinalResult());
        }), SELECTION_MARK_PDF);
    }

    // Business card recognition

    // Business card - non-URL

    /**
     * Verifies business card data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", data,
                    dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        Assertions.assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-businessCard", null, 0));
    }

    /**
     * Verifies content type will be auto-detected when using business card API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", data, dataLength)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult());
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data from a document using PNG file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardDataWithPngFile(HttpClient httpClient,
                                                   DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-businessCard", data, dataLength)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            FormRecognizerError errorInformation
                = (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidContent", errorInformation.getInnerError().getCode());
        });
    }

    /**
     * Verify business card recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeMultipageBusinessCard(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-businessCard", data,
                    dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
                                                        DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeMultipageBusinessCardUrl(HttpClient httpClient,
                                                DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", data,
                    dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-invoice", data, dataLength)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            FormRecognizerError errorInformation
                = (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidContent", errorInformation.getInnerError().getCode());
        });
    }

    /**
     * Verify invoice data recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeMultipageInvoice(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        // confirm if pageResults should be returned for prebuilt model recognition
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice", data, dataLength)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/21687")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeInvoiceFromUrlIncludeFieldElements(HttpClient httpClient,
                                                          DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void invoiceValidLocale(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-invoice",
                    data,
                    dataLength)
                .setPollInterval(durationTestMode);
            validateInvoiceData(syncPoller.getFinalResult());
        }, INVOICE_PDF);
    }

    /**
     * Verify SDK returns empty object and array for null sub line items field.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void invoiceSubLineItemsNull(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            AnalyzeResult analyzeResult = client.beginAnalyzeDocument("prebuilt-invoice",
                    data,
                    dataLength)
                .setPollInterval(durationTestMode)
                .getFinalResult();

            AnalyzedDocument analyzedDocument = analyzeResult.getDocuments().get(0);
            DocumentField itemFieldList = analyzedDocument.getFields().get("Items").getValueList().get(0);
            Map<String, DocumentField> documentFieldMap = itemFieldList.getValueMap();

            Assertions.assertNull(documentFieldMap);
            Assertions.assertEquals(String.valueOf(1), itemFieldList.getContent());

        }, INVOICE_NO_SUB_LINE_PDF);
    }

    // Identity Document Recognition

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeLicenseCardData(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        Assertions.assertThrows(NullPointerException.class,
            () -> client.beginAnalyzeDocument("prebuilt-idDocument", null, 0));
    }

    /**
     * Verifies content type will be auto-detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeLicenseDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                               DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocument("prebuilt-idDocument", data, dataLength)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocument("prebuilt-idDocument",
                        data,
                        dataLength)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            FormRecognizerError errorInformation
                = (FormRecognizerError) httpResponseException.getValue();
            Assertions.assertEquals("InvalidContent", errorInformation.getInnerError().getCode());
        });
    }

    // Identity Document - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void analyzeLicenseSourceUrl(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller
                = client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", sourceUrl)
                .setPollInterval(durationTestMode);
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
        client = getDocumentAnalysisClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException errorResponseException = Assertions.assertThrows(HttpResponseException.class,
                () -> client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
            FormRecognizerError errorInformation
                = (FormRecognizerError) errorResponseException.getValue();
            Assertions.assertEquals("InvalidContent", errorInformation.getInnerError().getCode());
        });
    }
}
