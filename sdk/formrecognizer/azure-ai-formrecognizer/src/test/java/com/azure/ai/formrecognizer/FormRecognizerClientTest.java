// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

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
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.VALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedFormPages;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedReceipts;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedRecognizedForms;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedRecognizedLabeledForms;
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
    void extractReceiptSourceUrl() {
        receiptSourceUrlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data for a document using source as file url and include content when includeTextDetails is true.
     */
    @Test
    void extractReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(includeTextDetails, getExpectedReceipts(includeTextDetails), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data for a document using source as input stream data.
     */
    @Test
    void extractReceiptData() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data.block(), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG, false, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    void beginRecognizeReceiptsWithNullData() {
        assertThrows(RuntimeException.class, () ->
            client.beginRecognizeReceipts(null, RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG, false, null));
    }

    /**
     * Verifies receipt data for a document using source as input stream data.
     * And the content type is not given. The content will be auto detected.
     */
    @Test
    void extractReceiptDataWithContentTypeAutoDetection() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data.block(), RECEIPT_FILE_LENGTH, null, false, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data for a document using source as as input stream data and text content when includeTextDetails is true.
     */
    @Test
    void extractReceiptDataTextDetails() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data.block(), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_PNG, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(includeTextDetails), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies layout/content data for a document using source as input stream data.
     */
    @Test
    void extractLayoutValidSourceUrl() {
        layoutValidSourceUrlRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(data.block(), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_PNG, null);
            syncPoller.waitForCompletion();
            validateLayoutResult(getExpectedFormPages(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url for recognizing content information.
     */
    @Test
    void extractLayoutInValidSourceUrl() {
        invalidSourceUrlRunner((invalidSourceUrl) -> assertThrows(ErrorResponseException.class, () ->
            client.beginRecognizeContentFromUrl(invalidSourceUrl)));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid unlabeled model Id.
     */
    @Test
    void extractCustomFormValidSourceUrl() {
        customFormValidSourceUrlRunner((data, validModelId) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(data.block(), validModelId,
                CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF);
            syncPoller.waitForCompletion();
            validateRecognizedFormResult(getExpectedRecognizedForms(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for invalid training data source.
     */
    @Test
    void extractCustomFormInValidSourceUrl() {
        ErrorResponseException httpResponseException = assertThrows(
            ErrorResponseException.class,
            () -> client.beginRecognizeCustomFormsFromUrl(INVALID_URL, VALID_MODEL_ID).getFinalResult());

        assertEquals(httpResponseException.getMessage(), (INVALID_SOURCE_URL_ERROR));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @Test
    void extractCustomFormLabeledData() {
        customFormLabeledDataRunner((data, validModelId) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(data.block(), validModelId,
                CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, true, null);
            syncPoller.waitForCompletion();
            validateRecognizedFormResult(getExpectedRecognizedLabeledForms(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies an exception thrown for a document using null data value or null model id.
     */
    @Test
    void beginRecognizeCustomFormsWithNullValues() {
        customFormLabeledDataRunner((data, validModelId) -> {
            assertThrows(RuntimeException.class, () ->
                client.beginRecognizeCustomForms(null, validModelId, CUSTOM_FORM_FILE_LENGTH,
                    FormContentType.APPLICATION_PDF, true, null));

            assertThrows(RuntimeException.class, () ->
                client.beginRecognizeCustomForms(data.block(), null, CUSTOM_FORM_FILE_LENGTH,
                    FormContentType.APPLICATION_PDF, true, null));
        });
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     * And the content type is not given. The content will be auto detected.
     */
    @Test
    void extractCustomFormLabeledDataWithContentTypeAutoDetection() {
        customFormLabeledDataRunner((data, validModelId) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(data.block(), validModelId,
                CUSTOM_FORM_FILE_LENGTH, null, true, null);
            syncPoller.waitForCompletion();
            validateRecognizedFormResult(getExpectedRecognizedLabeledForms(), syncPoller.getFinalResult());
        });
    }
}
