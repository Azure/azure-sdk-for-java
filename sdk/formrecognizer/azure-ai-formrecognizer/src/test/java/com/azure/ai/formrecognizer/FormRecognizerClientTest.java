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
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import static com.azure.ai.formrecognizer.TestUtils.CUSTOM_FORM_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedFormPages;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedReceipts;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedRecognizedForms;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedRecognizedLabeledForms;
import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FormRecognizerClientTest extends FormRecognizerClientTestBase {

    private FormRecognizerClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildClient());
    }

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @Test
    void recognizeReceiptSourceUrl() {
        receiptSourceUrlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl);
            syncPoller.waitForCompletion();
            validateReceiptResult(getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data for a document using source as file url and include content when includeTextDetails is
     * true.
     */
    @Test
    void recognizeReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(getExpectedReceipts(includeTextDetails),
                syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data for a document using source as input stream data.
     */
    @Test
    void recognizeReceiptData() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data, RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG, false, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    void recognizeReceiptDataTextDetailsWithNullData() {
        assertThrows(RuntimeException.class, () ->
            client.beginRecognizeReceipts(null, RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG, false, null));
    }

    /**
     * Verifies receipt data for a document using source as input stream data.
     * And the content type is not given. The content type will be auto detected.
     */
    @Test
    void recognizeReceiptDataWithContentTypeAutoDetection() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data, RECEIPT_FILE_LENGTH, null, false, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data for a document using source as as input stream data and text content when
     * includeTextDetails is true.
     */
    @Test
    void recognizeReceiptDataTextDetails() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data, RECEIPT_FILE_LENGTH, FormContentType.IMAGE_PNG,
                    includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(getExpectedReceipts(includeTextDetails), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies layout/content data for a document using source as input stream data.
     */
    @Test
    void recognizeLayoutData() {
        layoutDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(data, LAYOUT_FILE_LENGTH, FormContentType.IMAGE_PNG, null);
            syncPoller.waitForCompletion();
            validateLayoutResult(getExpectedFormPages(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    void recognizeLayoutDataWithNullData() {
        layoutDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(data, LAYOUT_FILE_LENGTH, FormContentType.IMAGE_PNG, null);
            syncPoller.waitForCompletion();

            assertThrows(RuntimeException.class, () ->
                client.beginRecognizeContent(null, LAYOUT_FILE_LENGTH, FormContentType.IMAGE_JPEG, null));
        });
    }

    /**
     * Verifies layout data for a document using source as input stream data.
     * And the content type is not given. The content type will be auto detected.
     */
    @Test
    void recognizeLayoutDataWithContentTypeAutoDetection() {
        layoutDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(data, LAYOUT_FILE_LENGTH, null, null);
            syncPoller.waitForCompletion();
            validateLayoutResult(getExpectedFormPages(), syncPoller.getFinalResult());
        });
    }

    @Test
    void recognizeLayoutSourceUrl() {
        layoutSourceUrlRunner(sourceUrl -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller
                = client.beginRecognizeContentFromUrl(sourceUrl);
            syncPoller.waitForCompletion();
            validateLayoutResult(getExpectedFormPages(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url for recognizing content information.
     */
    @Test
    void recognizeLayoutInvalidSourceUrl() {
        invalidSourceUrlRunner((invalidSourceUrl) -> assertThrows(ErrorResponseException.class, () ->
            client.beginRecognizeContentFromUrl(invalidSourceUrl)));
    }

    /**
     * Verifies that an exception is thrown for invalid training data source.
     */
    @Test
    void recognizeCustomFormInvalidSourceUrl() {
        beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.getFormTrainingClient().beginTraining(storageSASUrl, useLabelFile);
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            ErrorResponseException httpResponseException = assertThrows(
                ErrorResponseException.class,
                () -> client.beginRecognizeCustomFormsFromUrl(INVALID_URL, createdModel.getModelId()).getFinalResult());

            assertEquals(httpResponseException.getMessage(), (INVALID_SOURCE_URL_ERROR));
        });
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @Test
    void recognizeCustomFormLabeledData() {
        customFormLabeledDataRunner(data ->
            beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
                SyncPoller<OperationResult, CustomFormModel> syncPoller =
                    client.getFormTrainingClient().beginTraining(storageSASUrl, useLabelFile);
                syncPoller.waitForCompletion();

                SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPollers
                    = client.beginRecognizeCustomForms(data, syncPoller.getFinalResult().getModelId(),
                    CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, true, null);
                syncPoller.waitForCompletion();
                validateRecognizedFormResult(getExpectedRecognizedLabeledForms(), syncPollers.getFinalResult());
            }));
    }

    /**
     * Verifies an exception thrown for a document using null data value or null model id.
     */
    @Test
    void recognizeCustomFormLabeledDataWithNullValues() {
        customFormLabeledDataRunner(data ->
            beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
                SyncPoller<OperationResult, CustomFormModel> syncPoller =
                    client.getFormTrainingClient().beginTraining(storageSASUrl, useLabelFile);
                syncPoller.waitForCompletion();

                assertThrows(RuntimeException.class, () ->
                    client.beginRecognizeCustomForms(null, syncPoller.getFinalResult().getModelId(),
                        CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, true, null));
                assertThrows(RuntimeException.class, () ->
                    client.beginRecognizeCustomForms(data, null,
                        CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, true, null));
            })
        );
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     * And the content type is not given. The content type will be auto detected.
     */
    @Test
    void recognizeCustomFormLabeledDataWithContentTypeAutoDetection() {
        customFormLabeledDataRunner(data ->
            beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
                SyncPoller<OperationResult, CustomFormModel> syncPoller =
                    client.getFormTrainingClient().beginTraining(storageSASUrl, useLabelFile);
                syncPoller.waitForCompletion();

                SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPollers
                    = client.beginRecognizeCustomForms(data, syncPoller.getFinalResult().getModelId(),
                    CUSTOM_FORM_FILE_LENGTH, null, true, null);
                syncPoller.waitForCompletion();
                validateRecognizedFormResult(getExpectedRecognizedLabeledForms(), syncPollers.getFinalResult());
            }));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @Test
    void recognizeCustomFormUnlabeledData() {
        customFormLabeledDataRunner(data ->
            beginTrainingUnlabeledResultRunner((storageSASUrl, useLabelFile) -> {
                SyncPoller<OperationResult, CustomFormModel> syncPoller =
                    client.getFormTrainingClient().beginTraining(storageSASUrl, useLabelFile);
                syncPoller.waitForCompletion();

                SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPollers
                    = client.beginRecognizeCustomForms(data, syncPoller.getFinalResult().getModelId(),
                    CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, false, null);
                syncPoller.waitForCompletion();
                validateRecognizedFormResult(getExpectedRecognizedForms(), syncPollers.getFinalResult());
            }));
    }
}
