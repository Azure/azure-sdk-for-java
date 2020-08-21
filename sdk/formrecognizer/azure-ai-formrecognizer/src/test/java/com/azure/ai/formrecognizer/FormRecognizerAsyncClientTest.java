// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeContentOptions;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
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
import java.util.List;

import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.FAILED_TO_DOWNLOAD_IMAGE_CODE;
import static com.azure.ai.formrecognizer.TestUtils.FAILED_TO_DOWNLOAD_IMAGE_ERROR_MESSAGE;
import static com.azure.ai.formrecognizer.TestUtils.FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.IMAGE_URL_IS_BADLY_FORMATTED_ERROR_MESSAGE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_IMAGE_URL_ERROR_CODE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.INVOICE_6_PDF_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_1_JPG;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.NON_EXIST_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_JPG_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.getReplayableBufferData;
import static com.azure.ai.formrecognizer.TestUtils.validateExceptionSource;
import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;
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
            validateReceiptResultData(syncPoller.getFinalResult(), false);
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
        assertThrows(RuntimeException.class, () -> client.beginRecognizeReceipts(null, RECEIPT_FILE_LENGTH)
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

        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeReceipts(
            getReplayableBufferData(RECEIPT_JPG_LOCAL_URL), RECEIPT_FILE_LENGTH, new RecognizeReceiptsOptions()
                .setPollInterval(durationTestMode)).getSyncPoller();

        syncPoller.waitForCompletion();
        validateReceiptResultData(syncPoller.getFinalResult(), false);
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
            validateReceiptResultData(syncPoller.getFinalResult(), true);
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
            validateReceiptResultData(syncPoller.getFinalResult(), true);
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
            assertEquals(EXPECTED_BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
            assertEquals(EXPECTED_BAD_ARGUMENT_ERROR_MESSAGE, errorInformation.getMessage());
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
                client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), false);
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
            FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(FAILED_TO_DOWNLOAD_IMAGE_CODE, errorInformation.getErrorCode());
            assertEquals(FAILED_TO_DOWNLOAD_IMAGE_ERROR_MESSAGE, errorInformation.getMessage());
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
            assertEquals(IMAGE_URL_IS_BADLY_FORMATTED_ERROR_MESSAGE, errorInformation.getMessage());
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
            validateReceiptResultData(syncPoller.getFinalResult(), true);
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
            validateReceiptResultData(syncPoller.getFinalResult(), true);
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
        assertThrows(RuntimeException.class, () -> client.beginRecognizeContent(null, LAYOUT_FILE_LENGTH)
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
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                getReplayableBufferData(LOCAL_FILE_PATH + LAYOUT_1_JPG), dataLength, new RecognizeContentOptions()
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
            assertEquals(EXPECTED_INVALID_IMAGE_CODE, errorInformation.getErrorCode());
            assertEquals(EXPECTED_INVALID_IMAGE_ERROR_MESSAGE, errorInformation.getMessage());
        });
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
            FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(FAILED_TO_DOWNLOAD_IMAGE_CODE, errorInformation.getErrorCode());
            assertEquals(FAILED_TO_DOWNLOAD_IMAGE_ERROR_MESSAGE, errorInformation.getMessage());
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
            assertEquals(IMAGE_URL_IS_BADLY_FORMATTED_ERROR_MESSAGE, errorInformation.getMessage());
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
            assertEquals(EXPECTED_MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
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
            assertEquals(EXPECTED_INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
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
                assertEquals(EXPECTED_URL_BADLY_FORMATTED_ERROR_CODE, errorInformation.getErrorCode());
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
        dataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                        .getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                    client.beginRecognizeCustomForms(trainingPoller.getFinalResult().getModelId(),
                        getReplayableBufferData(INVOICE_6_PDF_LOCAL_URL), dataLength,
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

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    trainingPoller.getFinalResult().getModelId(), toFluxByteBuffer(data), dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataLabeled(syncPoller.getFinalResult());
            }), MULTIPAGE_INVOICE_PDF);
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
        urlPdfUnlabeledRunner(fileUrl ->
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
            }));
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
            StepVerifier.create(client.beginRecognizeCustomFormsFromUrl(createdModel.getModelId(), INVALID_URL))
                .verifyErrorSatisfies(throwable -> assertEquals(throwable.getMessage(), INVALID_SOURCE_URL_ERROR));
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
            assertEquals(EXPECTED_MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
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
            Exception ex = assertThrows(RuntimeException.class, () ->
                client.beginRecognizeCustomFormsFromUrl("", fileUrl, new RecognizeCustomFormsOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(EXPECTED_INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
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

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomFormsFromUrl(
                    trainingPoller.getFinalResult().getModelId(), fileUrl, new RecognizeCustomFormsOptions()
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataLabeled(syncPoller.getFinalResult());
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
            assertEquals(EXPECTED_INVALID_MODEL_ID_ERROR_CODE, errorInformation.getErrorCode());
            assertEquals(EXPECTED_INVALID_MODEL_ID_ERROR_MESSAGE, errorInformation.getMessage());
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
                assertEquals(EXPECTED_UNABLE_TO_READ_FILE, errorResponseException.getMessage());
            }));
    }
}
