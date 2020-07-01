// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.ErrorInformation;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizeOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.FORM_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.IMAGE_URL_IS_BADLY_FORMATTED_ERROR_MESSAGE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_IMAGE_URL_ERROR_CODE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_PNG_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.getReplayableBufferData;
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
        receiptDataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(toFluxByteBuffer(data), dataLength, FormContentType.IMAGE_JPEG)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        assertThrows(RuntimeException.class, () -> client.beginRecognizeReceipts(null, RECEIPT_FILE_LENGTH,
            FormContentType.IMAGE_JPEG).getSyncPoller());
    }

    /**
     * Verifies content type will be auto detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);

        SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller = client.beginRecognizeReceipts(
            new RecognizeOptions(getReplayableBufferData(RECEIPT_LOCAL_URL), RECEIPT_FILE_LENGTH)
                .setPollInterval(durationTestMode)).getSyncPoller();

        syncPoller.waitForCompletion();
        validateReceiptResultData(syncPoller.getFinalResult(), false);
    }

    /**
     * Verifies receipt data from a document using file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataTextDetails(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptDataRunnerTextDetails((data, includeTextContent) -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller = client.beginRecognizeReceipts(
                new RecognizeOptions(toFluxByteBuffer(data), RECEIPT_FILE_LENGTH)
                    .setFormContentType(FormContentType.IMAGE_JPEG).setIncludeFieldElements(includeTextContent)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), includeTextContent);
        });
    }

    /**
     * Verifies receipt data from a document using PNG file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptPngDataRunnerTextDetails((data, includeTextContent) -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(new RecognizeOptions(toFluxByteBuffer(data), RECEIPT_PNG_FILE_LENGTH)
                    .setFormContentType(FormContentType.IMAGE_PNG).setIncludeFieldElements(includeTextContent)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), true);
        });
    }

    /**
     * Verifies receipt data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithBlankPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        blankPdfDataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(new RecognizeOptions(toFluxByteBuffer(data), dataLength)
                    .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfResultData(syncPoller.getFinalResult());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        multipageFromDataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller = client.beginRecognizeReceipts(
                new RecognizeOptions(toFluxByteBuffer(data), dataLength)
                    .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
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
        receiptSourceUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), false);
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
                () -> client.beginRecognizeReceiptsFromUrl(new RecognizeOptions(invalidSourceUrl)
                    .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            ErrorInformation errorInformation = (ErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_IMAGE_URL_ERROR_CODE, errorInformation.getCode());
            assertEquals(IMAGE_URL_IS_BADLY_FORMATTED_ERROR_MESSAGE, errorInformation.getMessage());
        });
    }

    /**
     * Verifies receipt data for a document using source as file url and include content when includeTextContent is
     * true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlTextContent(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextContent) -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller = client.beginRecognizeReceiptsFromUrl(
                new RecognizeOptions(sourceUrl).setIncludeFieldElements(includeTextContent)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), includeTextContent);
        });
    }

    /**
     * Verifies receipt data for a document using source as PNG file url and include content when includeTextContent is
     * true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptSourceUrlWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptPngSourceUrlRunnerTextDetails((sourceUrl, includeTextContent) -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller = client.beginRecognizeReceiptsFromUrl(
                new RecognizeOptions(sourceUrl).setIncludeFieldElements(includeTextContent)
                    .setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), includeTextContent);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        multipageFromUrlRunner(fileUrl -> {
            SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller = client.beginRecognizeReceipts(
                new RecognizeOptions(fileUrl).setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        });
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
        contentFromDataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                toFluxByteBuffer(data), dataLength, FormContentType.IMAGE_JPEG).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithNullData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        assertThrows(RuntimeException.class, () -> client.beginRecognizeContent(null, LAYOUT_FILE_LENGTH,
            FormContentType.IMAGE_JPEG).getSyncPoller());
    }

    /**
     * Verifies content type will be auto detected when using content/layout API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        contentFromDataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                new RecognizeOptions(getReplayableBufferData(LAYOUT_LOCAL_URL), dataLength)
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies blank form file is still a valid file to process
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithBlankPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        blankPdfDataRunner((data, dataLength)  -> {
            SyncPoller<OperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                new RecognizeOptions(toFluxByteBuffer(data), dataLength)
                    .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        multipageFromDataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                new RecognizeOptions(toFluxByteBuffer(data), dataLength)
                    .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
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
        contentFromUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContentFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies layout data for a pdf url
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlWithPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        pdfContentFromUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, List<FormPage>> syncPoller =
                client.beginRecognizeContent(new RecognizeOptions(sourceUrl).setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        });
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
                () -> client.beginRecognizeContent(new RecognizeOptions(invalidSourceUrl)
                    .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
            ErrorInformation errorInformation = (ErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_IMAGE_URL_ERROR_CODE, errorInformation.getCode());
            assertEquals(IMAGE_URL_IS_BADLY_FORMATTED_ERROR_MESSAGE, errorInformation.getMessage());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        multipageFromUrlRunner((fileUrl) -> {
            SyncPoller<OperationResult, List<FormPage>> syncPoller = client.beginRecognizeContent(
                new RecognizeOptions(fileUrl).setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            validateContentResultData(syncPoller.getFinalResult(), false);
        });
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
        customFormDataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller =
                    client.beginRecognizeCustomForms(new RecognizeCustomFormsOptions(toFluxByteBuffer(data),
                        dataLength, trainingPoller.getFinalResult().getModelId())
                        .setFormContentType(FormContentType.APPLICATION_PDF).setIncludeFieldElements(true)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, true);
            }));
    }

    /**
     * Verifies custom form data for a JPG content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithJpgContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormJpgDataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId()).setFormContentType(FormContentType.IMAGE_JPEG)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }));
    }

    /**
     * Verifies custom form data for a blank PDF content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithBlankPdfContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        blankPdfDataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId()).setFormContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id,
     * excluding text content.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataExcludeTextContent(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId()).setFormContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithNullFormData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> syncPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                syncPoller.waitForCompletion();

                assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions((InputStream) null, dataLength,
                    syncPoller.getFinalResult().getModelId()).setFormContentType(FormContentType.APPLICATION_PDF)
                    .setIncludeFieldElements(true).setPollInterval(durationTestMode)).getSyncPoller());
            }));
    }

    /**
     * Verifies an exception thrown for a document using null model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithNullModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner((data, dataLength) -> {
            Exception ex = assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(
                new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                    null).setFormContentType(FormContentType.APPLICATION_PDF).setIncludeFieldElements(true)
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(EXPECTED_MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
        });
    }

    /**
     * Verifies an exception thrown for an empty model id when recognizing custom form from URL.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithEmptyModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner((data, dataLength) -> {
            Exception ex = assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(
                new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength, "")
                    .setFormContentType(FormContentType.APPLICATION_PDF).setIncludeFieldElements(true)
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(EXPECTED_INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) ->
            beginTrainingLabeledRunner((training, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> syncPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(training, useTrainingLabels,
                        null, durationTestMode).getSyncPoller();
                syncPoller.waitForCompletion();
                CustomFormModel createdModel = syncPoller.getFinalResult();
                FormRecognizerException formRecognizerException = assertThrows(FormRecognizerException.class,
                    () -> client.beginRecognizeCustomForms(new RecognizeCustomFormsOptions(
                        invalidSourceUrl, createdModel.getModelId()).setPollInterval(durationTestMode))
                        .getSyncPoller().getFinalResult());
                ErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
                // TODO: Service bug https://github.com/Azure/azure-sdk-for-java/issues/12046
                // assertEquals(EXPECTED_INVALID_URL_ERROR_CODE, errorInformation.getCode());
                // assertEquals(OCR_EXTRACTION_INVALID_URL_ERROR, errorInformation.getMessage());
                // assertEquals(EXPECTED_INVALID_ANALYZE_EXCEPTION_MESSAGE, formRecognizerException.getMessage())
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
        customFormDataRunner((data, dataLength) ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(getReplayableBufferData(FORM_LOCAL_URL), dataLength,
                        trainingPoller.getFinalResult().getModelId())
                        .setIncludeFieldElements(true).setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, true);
            }));
    }

    /**
     * Verify custom form for a data stream of multi-page labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageLabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        multipageFromDataRunner((data, dataLength) ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        true, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId())
                        .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataLabeled(syncPoller.getFinalResult());
            }));
    }

    // Custom form - non-URL - unlabeled data

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId())
                        .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid include text content
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataIncludeTextContent(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId())
                        .setFormContentType(FormContentType.APPLICATION_PDF)
                        .setIncludeFieldElements(true).setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), true, false);
            })
        );
    }

    /**
     * Verify custom form for a data stream of multi-page unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageUnlabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        multipageFromDataRunner((data, dataLength) ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion)
                        .beginTraining(trainingFilesUrl, false, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId())
                        .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataUnlabeled(syncPoller.getFinalResult());
            })
        );
    }

    /**
     * Verifies custom form data for a JPG content type with unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataWithJpgContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormJpgDataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId()).setFormContentType(FormContentType.IMAGE_JPEG)
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }));
    }

    /**
     * Verifies custom form data for a blank PDF content type with unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataWithBlankPdfContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        blankPdfDataRunner((data, dataLength) ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(toFluxByteBuffer(data), dataLength,
                        trainingPoller.getFinalResult().getModelId())
                        .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(durationTestMode))
                    .getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }));
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
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(fileUrl, trainingPoller.getFinalResult().getModelId())
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, false);
            }), FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data without labeled data and include text content
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlUnlabeledDataIncludeTextContent(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl ->
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(fileUrl, trainingPoller.getFinalResult().getModelId())
                        .setIncludeFieldElements(true).setPollInterval(durationTestMode)).getSyncPoller();
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
        multipageFromUrlRunner(fileUrl ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        false, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(fileUrl, trainingPoller.getFinalResult().getModelId())
                        .setPollInterval(durationTestMode)).getSyncPoller();
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
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                    useTrainingLabels, null, durationTestMode).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();
            StepVerifier.create(client.beginRecognizeCustomFormsFromUrl(INVALID_URL, createdModel.getModelId()))
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
        multipageFromUrlRunner(fileUrl -> {
            Exception ex = assertThrows(RuntimeException.class, () ->
                client.beginRecognizeCustomForms(new RecognizeCustomFormsOptions(fileUrl, null)
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(EXPECTED_MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
        });
    }

    /**
     * Verifies an exception thrown for an empty model id for recognizing custom forms from URL.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormFromUrlLabeledDataWithEmptyModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        multipageFromUrlRunner(fileUrl -> {
            Exception ex = assertThrows(RuntimeException.class, () ->
                client.beginRecognizeCustomForms(new RecognizeCustomFormsOptions(fileUrl, "")
                    .setPollInterval(durationTestMode)).getSyncPoller());
            assertEquals(EXPECTED_INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
        });
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
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(fileUrl, trainingPoller.getFinalResult().getModelId())
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateRecognizedResult(syncPoller.getFinalResult(), false, true);
            }), FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data with labeled data and include text content
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledDataIncludeTextContent(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        urlRunner(fileUrl ->
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion).beginTraining(trainingFilesUrl,
                        useTrainingLabels, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(fileUrl, trainingPoller.getFinalResult().getModelId())
                        .setIncludeFieldElements(true).setPollInterval(durationTestMode)).getSyncPoller();
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
        multipageFromUrlRunner(fileUrl ->
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingAsyncClient(httpClient, serviceVersion)
                        .beginTraining(trainingFilesUrl, true, null, durationTestMode).getSyncPoller();
                trainingPoller.waitForCompletion();

                SyncPoller<OperationResult, List<RecognizedForm>> syncPoller = client.beginRecognizeCustomForms(
                    new RecognizeCustomFormsOptions(fileUrl, trainingPoller.getFinalResult().getModelId())
                        .setPollInterval(durationTestMode)).getSyncPoller();
                syncPoller.waitForCompletion();
                validateMultiPageDataLabeled(syncPoller.getFinalResult());
            }));
    }
}
