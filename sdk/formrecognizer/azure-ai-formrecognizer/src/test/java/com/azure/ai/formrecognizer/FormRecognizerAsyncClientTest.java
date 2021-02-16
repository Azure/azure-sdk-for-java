// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.FormRecognizerLanguage;
import com.azure.ai.formrecognizer.models.FormRecognizerLocale;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeBusinessCardsOptions;
import com.azure.ai.formrecognizer.models.RecognizeContentOptions;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizeInvoicesOptions;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.TextStyleName;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.PrebuiltType.BUSINESS_CARD;
import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.PrebuiltType.INVOICE;
import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.PrebuiltType.RECEIPT;
import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_IMAGE_URL_ERROR_CODE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_SOURCE_URL_ERROR_CODE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.NON_EXIST_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.SELECTION_MARK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.getContentDetectionFileData;
import static com.azure.ai.formrecognizer.TestUtils.validateExceptionSource;
import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;
import static com.azure.ai.formrecognizer.models.FormContentType.APPLICATION_PDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FormRecognizerAsyncClientTest extends FormRecognizerClientTestBase {

    private FormRecognizerAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private FormRecognizerAsyncClient getFormRecognizerAsyncClient(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        return getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    private FormTrainingAsyncClient getFormTrainingAsyncClient(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        return getFormTrainingClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Receipt recognition

    // Receipt - non-URL

    /**
     * Verifies receipt data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeReceipts(toFluxByteBuffer(data), dataLength, new RecognizeReceiptsOptions()
                .setContentType(FormContentType.IMAGE_JPEG).setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, RECEIPT);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class, () -> client.beginRecognizeReceipts(null, 0)
            .getSyncPoller());
    }

    /**
     * Verifies content type will be auto detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceipts(toFluxByteBuffer(getContentDetectionFileData(filePath)), dataLength,
                new RecognizeReceiptsOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, RECEIPT);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data from a document using file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataIncludeFieldElements(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeReceipts(
                toFluxByteBuffer(data), dataLength, new RecognizeReceiptsOptions()
                    .setContentType(FormContentType.IMAGE_JPEG).setFieldElementsIncluded(true)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, RECEIPT);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data from a document using PNG file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeReceipts(toFluxByteBuffer(data), dataLength, new RecognizeReceiptsOptions()
                    .setContentType(FormContentType.IMAGE_PNG).setFieldElementsIncluded(true)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, RECEIPT);
        }, RECEIPT_CONTOSO_PNG);
    }

    /**
     * Verifies receipt data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithBlankPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeReceipts(toFluxByteBuffer(data), dataLength, new RecognizeReceiptsOptions()
                    .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfResultData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeReceipts(
                toFluxByteBuffer(data), dataLength, new RecognizeReceiptsOptions()
                    .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verify that receipt recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromDamagedPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeReceipts(toFluxByteBuffer(data), dataLength, new RecognizeReceiptsOptions()
                    .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller().getFinalResult());
            FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
        });
    }

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl,
                    new RecognizeReceiptsOptions().setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, RECEIPT);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize receipt from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeReceiptsFromUrl(sourceUrl, new RecognizeReceiptsOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeReceiptsFromUrl(invalidSourceUrl, new RecognizeReceiptsOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_IMAGE_URL_ERROR_CODE, errorInformation.getErrorCode());
        });
    }

    /**
     * Verifies receipt data for a document using source as file url and include content when includeFieldElements is
     * true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeReceiptsFromUrl(
                sourceUrl, new RecognizeReceiptsOptions().setFieldElementsIncluded(true)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, RECEIPT);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data for a document using source as PNG file url and include element references when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptSourceUrlWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl,
                    new RecognizeReceiptsOptions().setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, RECEIPT);
        }, RECEIPT_CONTOSO_PNG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeReceiptsFromUrl(
                fileUrl, new RecognizeReceiptsOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        }, MULTIPAGE_INVOICE_PDF);
    }

    // Content Recognition

    // Content - non-URL

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContent(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                toFluxByteBuffer(data), dataLength, new RecognizeContentOptions()
                    .setContentType(FormContentType.IMAGE_JPEG).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, FORM_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithNullData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        assertThrows(NullPointerException.class, () -> client.beginRecognizeContent(null, 0)
                                                           .getSyncPoller());
    }

    /**
     * Verifies content type will be auto detected when using content/layout API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                toFluxByteBuffer(getContentDetectionFileData(filePath)), dataLength, new RecognizeContentOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, FORM_JPG);
    }

    /**
     * Verifies blank form file is still a valid file to process
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithBlankPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength)  -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                toFluxByteBuffer(data), dataLength, new RecognizeContentOptions()
                    .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                toFluxByteBuffer(data), dataLength, new RecognizeContentOptions()
                    .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verify that content recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromDamagedPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeContent(toFluxByteBuffer(data), dataLength, new RecognizeContentOptions()
                    .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller().getFinalResult());
            FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_IMAGE_ERROR_CODE, errorInformation.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithSelectionMarks(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                toFluxByteBuffer(data), dataLength, new RecognizeContentOptions()
                    .setContentType(FormContentType.APPLICATION_PDF)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContent(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeContentOptions()
                        .setContentType(APPLICATION_PDF)
                        .setPollInterval(durationTestMode)
                        .setPages(Collections.singletonList("1")))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentResultData(formPages, false);
            assertEquals(1, formPages.size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithPages(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContent(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeContentOptions()
                        .setContentType(APPLICATION_PDF)
                        .setPollInterval(durationTestMode)
                        .setPages(Arrays.asList("1", "2")))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentResultData(formPages, false);
            assertEquals(2, formPages.size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithPageRange(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContent(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeContentOptions()
                        .setContentType(APPLICATION_PDF)
                        .setPollInterval(durationTestMode)
                        .setPages(Arrays.asList("1-2", "3")))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentResultData(formPages, false);
            assertEquals(3, formPages.size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentAppearance(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                toFluxByteBuffer(data), dataLength, new RecognizeContentOptions()
                    .setContentType(FormContentType.IMAGE_JPEG).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentResultData(formPages, false);
            assertEquals(TextStyleName.OTHER,
                formPages.get(0).getLines().get(0).getAppearance().getStyle().getName());
        }, FORM_JPG);
    }

    // Content - URL

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContentFromUrl(sourceUrl, new RecognizeContentOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, FORM_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize a content from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeContentFromUrl(sourceUrl, new RecognizeContentOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies layout data for a pdf url
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlWithPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContentFromUrl(sourceUrl, new RecognizeContentOptions().setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeContentFromUrl(invalidSourceUrl, new RecognizeContentOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_IMAGE_URL_ERROR_CODE, errorInformation.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner((formUrl) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContentFromUrl(
                formUrl, new RecognizeContentOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithSelectionMarksFromUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContentFromUrl(sourceUrl,
                    new RecognizeContentOptions().setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeGermanContentFromUrl(HttpClient httpClient,
                                              FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContentFromUrl(sourceUrl,
                    new RecognizeContentOptions().setPollInterval(durationTestMode)
                        .setLanguage(FormRecognizerLanguage.DE))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
            validateNetworkCallRecord("language", "de");
        }, CONTENT_GERMAN_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentIncorrectLanguageFromUrl(HttpClient httpClient,
                                              FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            HttpResponseException exception
                = assertThrows(HttpResponseException.class,
                    () -> client.beginRecognizeContentFromUrl(sourceUrl,
                        new RecognizeContentOptions().setPollInterval(durationTestMode)
                            .setLanguage(FormRecognizerLanguage.fromString("language")))
                        .getSyncPoller());
            assertEquals(((FormRecognizerErrorInformation) exception.getValue()).getErrorCode(), "NotSupportedLanguage");
        }, CONTENT_GERMAN_PDF);
    }

    // Custom form recognition

    // Custom form - non-URL - labeled data

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                    client.beginRecognizeCustomForms(trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data),
                        dataLength, new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF).setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, true);
            }), INVOICE_6_PDF);
    }

    /**
     * Verifies custom form data for a JPG content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithJpgContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.IMAGE_JPEG)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }), FORM_JPG);
    }

    /**
     * Verifies custom form data for a blank PDF content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithBlankPdfContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }), BLANK_PDF);
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id,
     * excluding element references.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataExcludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }), INVOICE_6_PDF);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithNullFormData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                syncPoller.waitForCompletion();

                assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(
                    syncPoller.getFinalResult().getModelId(), null, dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setFieldElementsIncluded(true).setPollInterval(durationTestMode)).getSyncPoller());
            }), INVOICE_6_PDF);
    }

    /**
     * Verifies an exception thrown for a document using null model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithNullModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            Exception ex = assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(
                null, toFluxByteBuffer(data), dataLength,
                new RecognizeCustomFormsOptions().setContentType(FormContentType.APPLICATION_PDF).setFieldElementsIncluded(true)
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies an exception thrown for an empty model id when recognizing custom form from URL.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithEmptyModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            Exception ex = assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(
                "", toFluxByteBuffer(data), dataLength, new RecognizeCustomFormsOptions()
                    .setContentType(FormContentType.APPLICATION_PDF).setFieldElementsIncluded(true)
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
        }, INVOICE_6_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) ->
            beginTrainingLabeledRunner((training, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(training, useTrainingLabels,
                        new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                CustomFormModel createdModel = syncPoller.getFinalResult();
                FormRecognizerException formRecognizerException = assertThrows(FormRecognizerException.class,
                    () -> client.beginRecognizeCustomFormsFromUrl(
                        createdModel.getModelId(), invalidSourceUrl, new RecognizeCustomFormsOptions()
                            .setPollInterval(durationTestMode))
                        .getSyncPoller().getFinalResult());
                FormRecognizerErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
                assertEquals(URL_BADLY_FORMATTED_ERROR_CODE, errorInformation.getErrorCode());
            }));
    }

    /**
     * Verifies content type will be auto detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> beginTrainingLabeledRunner(
            (trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                    client.beginRecognizeCustomForms(trainingPoller.getFinalResult().getModelId(),
                        toFluxByteBuffer(getContentDetectionFileData(filePath)), dataLength,
                        new RecognizeCustomFormsOptions()
                            .setFieldElementsIncluded(true).setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, true);
            }), INVOICE_6_PDF);
    }

    /**
     * Verify custom form for a data stream of multi-page labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageLabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        true, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
                trainingPoller.waitForCompletion();
                String modelId = trainingPoller.getFinalResult().getModelId();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    modelId, toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataLabeled(syncPoller.getFinalResult(), modelId);
            }), MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithSelectionMark(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginSelectionMarkTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                    client.beginRecognizeCustomForms(trainingPoller.getFinalResult().getModelId(),
                        toFluxByteBuffer(data),
                        dataLength,
                        new RecognizeCustomFormsOptions()
                            .setContentType(FormContentType.APPLICATION_PDF).setFieldElementsIncluded(true)
                            .setPollInterval(durationTestMode))
                        .getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, true);
            }), SELECTION_MARK_PDF);
    }

    // Custom form - non-URL - unlabeled data

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }), INVOICE_6_PDF);
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid include field elements
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setFieldElementsIncluded(true).setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, false);
            }), INVOICE_6_PDF);
    }

    /**
     * Verify custom form for a data stream of multi-page unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageUnlabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion)
                        .beginTraining(trainingFilesUrl, false,
                            new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataUnlabeled(syncPoller.getFinalResult());
            }), MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies custom form data for a JPG content type with unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataWithJpgContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.IMAGE_JPEG)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }), FORM_JPG);
    }

    /**
     * Verifies custom form data for a blank PDF content type with unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataWithBlankPdfContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }), BLANK_PDF);
    }

    // Custom form - URL - unlabeled data

    /**
     * Verifies custom form data for an URL document data without labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlUnlabeledData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);

        urlRunner(fileUrl ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomFormsFromUrl(
                    trainingPoller.getFinalResult().getModelId(), fileUrl, new RecognizeCustomFormsOptions()
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }), FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data without labeled data and include element references.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlUnlabeledDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomFormsFromUrl(
                    trainingPoller.getFinalResult().getModelId(), fileUrl, new RecognizeCustomFormsOptions()
                        .setFieldElementsIncluded(true).setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, false);
            }), FORM_JPG);
    }

    /**
     * Verify custom form for an URL of multi-page unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlMultiPageUnlabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        testingContainerUrlRunner(fileUrl ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        false, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                    client.beginRecognizeCustomFormsFromUrl(trainingPoller.getFinalResult().getModelId(), fileUrl,
                        new RecognizeCustomFormsOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataUnlabeled(syncPoller.getFinalResult());
            }), MULTIPAGE_INVOICE_PDF);
    }

    // Custom form - URL - labeled data

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                    useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();
            StepVerifier.create(client.beginRecognizeCustomFormsFromUrl(createdModel.getModelId(), INVALID_URL,
                new RecognizeCustomFormsOptions().setPollInterval(durationTestMode)))
                .verifyErrorSatisfies(throwable -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) throwable;
                    final FormRecognizerErrorInformation errorInformation =
                        (FormRecognizerErrorInformation) httpResponseException.getValue();
                    assertEquals(INVALID_SOURCE_URL_ERROR_CODE, errorInformation.getErrorCode());
                });
        });
    }

    /**
     * Verifies an exception thrown for a null model id when recognizing custom form from URL.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormFromUrlLabeledDataWithNullModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> {
            Exception ex = assertThrows(RuntimeException.class, () ->
                client.beginRecognizeCustomFormsFromUrl(null, fileUrl, new RecognizeCustomFormsOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies an exception thrown for an empty model id for recognizing custom forms from URL.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormFromUrlLabeledDataWithEmptyModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                client.beginRecognizeCustomFormsFromUrl("", fileUrl, new RecognizeCustomFormsOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies custom form data for an URL document data with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomFormsFromUrl(
                    trainingPoller.getFinalResult().getModelId(), fileUrl, new RecognizeCustomFormsOptions()
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }), FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data with labeled data and include element references.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomFormsFromUrl(
                    trainingPoller.getFinalResult().getModelId(), fileUrl, new RecognizeCustomFormsOptions()
                        .setFieldElementsIncluded(true).setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, true);
            }), FORM_JPG);
    }

    /**
     * Verify custom form for an URL of multi-page labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlMultiPageLabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion)
                        .beginTraining(trainingFilesUrl, true,
                            new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
                trainingPoller.waitForCompletion();
                String modelId = trainingPoller.getFinalResult().getModelId();
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomFormsFromUrl(
                    modelId, fileUrl, new RecognizeCustomFormsOptions()
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataLabeled(syncPoller.getFinalResult(), modelId);
            }), MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with \
     * encoded blank space as input data to recognize a custom form from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeCustomFormsFromUrl(NON_EXIST_MODEL_ID, sourceUrl, new RecognizeCustomFormsOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verify that custom form with invalid model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlNonExistModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeCustomFormsFromUrl(NON_EXIST_MODEL_ID, fileUrl,
                    new RecognizeCustomFormsOptions().setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_MODEL_ID_ERROR_CODE, errorInformation.getErrorCode());
        }, FORM_JPG);
    }

    /**
     * Verify that custom form with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormDamagedPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                FormRecognizerException errorResponseException = assertThrows(FormRecognizerException.class,
                    () -> client.beginRecognizeCustomForms(trainingPoller.getFinalResult().getModelId(),
                        toFluxByteBuffer(data), dataLength, new RecognizeCustomFormsOptions()
                            .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                        .getSyncPoller().getFinalResult());
                assertEquals(UNABLE_TO_READ_FILE_ERROR_CODE, errorResponseException.getErrorInformation().get(0).getErrorCode());
            }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledDataWithSelectionMark(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl -> beginSelectionMarkTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                    useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                    .getSyncPoller();
            trainingPoller.waitForCompletion();

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomFormsFromUrl(trainingPoller.getFinalResult().getModelId(), fileUrl,
                    new RecognizeCustomFormsOptions().setFieldElementsIncluded(true).setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateRecognizedResult(syncPoller.getFinalResult(), true, true);
        }), SELECTION_MARK_PDF);
    }

    // Business Card Recognition

    /**
     * Verifies business card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCards(toFluxByteBuffer(data), dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(FormContentType.IMAGE_JPEG)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, BUSINESS_CARD);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class, () -> client.beginRecognizeBusinessCards(
            null, 0).getSyncPoller());
    }

    /**
     * Verifies content type will be auto detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCards(toFluxByteBuffer(getContentDetectionFileData(filePath)), dataLength,
                    new RecognizeBusinessCardsOptions().setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, BUSINESS_CARD);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data from a document using file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCards(toFluxByteBuffer(data), dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(FormContentType.IMAGE_JPEG)
                        .setFieldElementsIncluded(true).setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, BUSINESS_CARD);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data from a document using PNG file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCards(toFluxByteBuffer(data), dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(FormContentType.IMAGE_PNG)
                        .setFieldElementsIncluded(true).setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, BUSINESS_CARD);
        }, BUSINESS_CARD_PNG);
    }

    /**
     * Verifies business card data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataWithBlankPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCards(toFluxByteBuffer(data), dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfResultData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that business card recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardFromDamagedPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeBusinessCards(toFluxByteBuffer(data), dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            FormRecognizerErrorInformation errorInformation =
                (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
        });
    }

    /**
     * Verify business card recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeMultipageBusinessCard(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCards(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeBusinessCardsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode))
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
    public void recognizeBusinessCardSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions().setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, BUSINESS_CARD);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize business card from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions().setPollInterval(durationTestMode))
                          .getSyncPoller().getFinalResult());
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardInvalidSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeBusinessCardsFromUrl(invalidSourceUrl,
                    new RecognizeBusinessCardsOptions().setPollInterval(durationTestMode))
                          .getSyncPoller().getFinalResult());
            FormRecognizerErrorInformation errorInformation =
                (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_IMAGE_URL_ERROR_CODE, errorInformation.getErrorCode());
        });
    }

    /**
     * Verifies business card data for a document using source as file url and include content when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardFromUrlIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions().setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, BUSINESS_CARD);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data for a document using source as PNG file url and include element references when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions().setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, BUSINESS_CARD);
        }, BUSINESS_CARD_PNG);
    }

    /**
     * Verify business card recognition with multipage pdf url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeMultipageBusinessCardUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions()
                        .setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultipageBusinessData(syncPoller.getFinalResult());
        }, MULTIPAGE_BUSINESS_CARD_PDF);
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void receiptValidLocale(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl,
                    new RecognizeReceiptsOptions()
                        .setLocale(FormRecognizerLocale.EN_US)
                        .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.getFinalResult();
            validateNetworkCallRecord("locale", "en-US");
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verify locale parameter passed when specified by user for business cards API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void businessCardValidLocale(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions()
                        .setLocale(FormRecognizerLocale.EN_US)
                        .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.getFinalResult();
            validateNetworkCallRecord("locale", "en-US");
        }, RECEIPT_CONTOSO_JPG);
    }

    // Invoice recognition

    // Invoice - non-URL

    /**
     * Verifies invoice data recognition  for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeInvoices(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeInvoicesOptions()
                        .setContentType(APPLICATION_PDF)
                        .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, INVOICE);
        }, INVOICE_PDF);
    }

    /**
     * Verifies content type will be auto detected when using invoice API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeInvoices(toFluxByteBuffer(getContentDetectionFileData(filePath)),
                    dataLength,
                    new RecognizeInvoicesOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, INVOICE);
        }, INVOICE_PDF);
    }

    /**
     * Verifies invoice data for a document using source as as input stream data and text content when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeInvoices(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeInvoicesOptions()
                        .setContentType(APPLICATION_PDF)
                        .setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, INVOICE);
        }, INVOICE_PDF);
    }


    /**
     * Verifies invoice data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceDataWithBlankPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeInvoices(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeInvoicesOptions()
                        .setContentType(APPLICATION_PDF)
                        .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfResultData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that invoice recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceFromDamagedPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeInvoices(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeInvoicesOptions()
                        .setContentType(APPLICATION_PDF)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller()
                    .getFinalResult());
            FormRecognizerErrorInformation errorInformation =
                (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
        });
    }

    /**
     * Verify invoice data recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeMultipageInvoice(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeInvoices(toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeInvoicesOptions()
                        .setContentType(APPLICATION_PDF)
                        .setFieldElementsIncluded(true)
                        .setPollInterval(durationTestMode)).getSyncPoller();
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
    public void recognizeInvoiceSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeInvoicesFromUrl(sourceUrl,
                    new RecognizeInvoicesOptions()
                        .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), false, INVOICE);
        }, INVOICE_PDF);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize invoice card from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeInvoicesFromUrl(sourceUrl,
                    new RecognizeInvoicesOptions()
                        .setPollInterval(durationTestMode)).getSyncPoller());
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceInvalidSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl)
            -> assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeInvoicesFromUrl(sourceUrl,
                    new RecognizeInvoicesOptions()
                        .setPollInterval(durationTestMode)).getSyncPoller()));
    }

    /**
     * Verifies invoice data for a document using source as file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceFromUrlIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoicesFromUrl(sourceUrl,
                new RecognizeInvoicesOptions()
                    .setFieldElementsIncluded(true)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validatePrebuiltResultData(syncPoller.getFinalResult(), true, INVOICE);
        }, INVOICE_PDF);
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void invoiceValidLocale(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            final SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeInvoicesFromUrl(sourceUrl,
                new RecognizeInvoicesOptions().setPollInterval(durationTestMode)
                    .setLocale(FormRecognizerLocale.EN_US))
                .getSyncPoller();
            syncPoller.getFinalResult();
            // need to update this method for removing flakiness
            validateNetworkCallRecord("locale", "en-US");
        }, INVOICE_PDF);
    }
}
