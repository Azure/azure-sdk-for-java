// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.formrecognizer.TestUtils.CUSTOM_FORM_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.FORM_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_LOCAL_URL;
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
        FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint()).httpClient(httpClient == null
                ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion).addPolicy(interceptorManager.getRecordPolicy());
        AzureKeyCredential credential = (getTestMode() == TestMode.PLAYBACK) ? new AzureKeyCredential(INVALID_KEY)
            : new AzureKeyCredential(getApiKey());
        builder.credential(credential);
        return builder.buildAsyncClient();
    }

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptSourceUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies receipt data for a document using source as file url and include content when includeTextDetails is
     * true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptSourceUrlTextDetails(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), includeTextDetails);
        });
    }

    /**
     * Verifies receipt data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(toFluxByteBuffer(data), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG,
                    false, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResultData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataTextDetailsWithNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        assertThrows(RuntimeException.class, () -> client.beginRecognizeReceipts(null, RECEIPT_FILE_LENGTH,
            FormContentType.IMAGE_JPEG, false, null).getSyncPoller());
    }

    /**
     * Verifies receipt data from a document using file data as source.
     * And the content type is not given. The content type will be auto detected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
            client.beginRecognizeReceipts(getReplayableBufferData(RECEIPT_LOCAL_URL), RECEIPT_FILE_LENGTH, null,
                false, null).getSyncPoller();
        syncPoller.waitForCompletion();
        validateReceiptResultData(syncPoller.getFinalResult(), false);
    }

    /**
     * Verifies receipt data from a document using file data as source and including text content details.
     */
    // Turn off the tests as there is service regression on the media type.
    // Issue link: https://github.com/Azure/azure-sdk-for-java/issues/11036
//    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
//    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
//    public void recognizeReceiptDataTextDetails(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
//        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
//        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
//            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
//                client.beginRecognizeReceipts(toFluxByteBuffer(data), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG,
//                    includeTextDetails, null).getSyncPoller();
//            syncPoller.waitForCompletion();
//            validateReceiptResultData(syncPoller.getFinalResult(), true);
//        });
//    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl) -> assertThrows(ErrorResponseException.class,
            () -> client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller()));
    }

    /**
     * Verifies receipt data is correctly transformed to USReceipt type.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptAsUSReceipt(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(toFluxByteBuffer(data), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG,
                    includeTextDetails, null).getSyncPoller();
            syncPoller.waitForCompletion();
            syncPoller.getFinalResult().forEach(recognizedReceipt -> validateUSReceiptData(ReceiptExtensions.asUSReceipt(recognizedReceipt), includeTextDetails));
        });
    }

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLayoutData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        layoutDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(toFluxByteBuffer(data), LAYOUT_FILE_LENGTH, FormContentType.IMAGE_JPEG,
                    null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateLayoutDataResults(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLayoutDataWithNullData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        layoutDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(toFluxByteBuffer(data), LAYOUT_FILE_LENGTH, FormContentType.IMAGE_JPEG,
                    null).getSyncPoller();
            syncPoller.waitForCompletion();

            assertThrows(RuntimeException.class, () -> client.beginRecognizeContent(null, LAYOUT_FILE_LENGTH,
                FormContentType.IMAGE_JPEG, null).getSyncPoller());
        });
    }

    /**
     * Verifies layout data for a document using source as input stream data.
     * And the content type is not given. The content type will be auto detected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLayoutDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        layoutDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(getReplayableBufferData(LAYOUT_LOCAL_URL), LAYOUT_FILE_LENGTH, null,
                    null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateLayoutDataResults(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLayoutSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        layoutSourceUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContentFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateLayoutDataResults(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLayoutInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> assertThrows(ErrorResponseException.class,
            () -> client.beginRecognizeContentFromUrl(invalidSourceUrl).getSyncPoller()));
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.getFormTrainingAsyncClient().beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();
            StepVerifier.create(client.beginRecognizeCustomFormsFromUrl(INVALID_URL, createdModel.getModelId()))
                .verifyErrorSatisfies(throwable -> assertEquals(throwable.getMessage(), INVALID_SOURCE_URL_ERROR));
        });
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner(data -> beginTrainingLabeledRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                client.getFormTrainingAsyncClient().beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            trainingPoller.waitForCompletion();

            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(toFluxByteBuffer(data), trainingPoller.getFinalResult().getModelId(),
                    CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, true, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateRecognizedResult(syncPoller.getFinalResult(), true, true);
        }));
    }

    /**
     * Verifies an exception thrown for a document using null data value or null model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithNullValues(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner(data -> beginTrainingLabeledRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.getFormTrainingAsyncClient().beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            syncPoller.waitForCompletion();

            assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(null,
                syncPoller.getFinalResult().getModelId(), CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF,
                true, null).getSyncPoller());

            assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomForms(toFluxByteBuffer(data), null,
                CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, true, null).getSyncPoller());
        }));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     * And the content type is not given. The content type will be auto detected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner(data -> beginTrainingLabeledRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                client.getFormTrainingAsyncClient().beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            trainingPoller.waitForCompletion();

            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(getReplayableBufferData(FORM_LOCAL_URL),
                    trainingPoller.getFinalResult().getModelId(), CUSTOM_FORM_FILE_LENGTH, null, true, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateRecognizedResult(syncPoller.getFinalResult(), true, true);
        }));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerAsyncClient(httpClient, serviceVersion);
        customFormDataRunner(data -> beginTrainingUnlabeledRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> trainingPoller =
                client.getFormTrainingAsyncClient().beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            trainingPoller.waitForCompletion();

            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(toFluxByteBuffer(data), trainingPoller.getFinalResult().getModelId(),
                    CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, false, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateRecognizedResult(syncPoller.getFinalResult(), false, false);
        }));
    }
}
