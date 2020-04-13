// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import static com.azure.ai.formrecognizer.TestUtils.FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_STATUS_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.VALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedFormPages;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedReceipts;
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

    // Extract Receipt
    @Test
    void extractReceiptSourceUrl() {
        receiptSourceUrlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(includeTextDetails, getExpectedReceipts(includeTextDetails), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptData() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data, FILE_LENGTH, FormContentType.IMAGE_JPEG, false, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptDataTextDetails() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(data, FILE_LENGTH, FormContentType.IMAGE_PNG, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(includeTextDetails), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractLayoutValidSourceUrl() {
        layoutValidSourceUrlRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller =
                client.beginRecognizeContent(data, FILE_LENGTH, FormContentType.IMAGE_PNG, null);
            syncPoller.waitForCompletion();
            validateLayoutResult(getExpectedFormPages(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractLayoutInValidSourceUrl() {
        invalidSourceUrlRunner((invalidSourceUrl) -> assertThrows(HttpResponseException.class, () ->
            client.beginRecognizeContentFromUrl(invalidSourceUrl)));
    }

    @Test
    void extractCustomFormValidSourceUrl() {
        // TODO (savaity) : Error writing recording [Async]
    }

    @Test
    void extractCustomFormInValidSourceUrl() {
        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> client.beginExtractCustomFormsFromUrl(INVALID_URL, VALID_MODEL_ID).getFinalResult());

        assertEquals(illegalArgumentException.getMessage(), (INVALID_STATUS_ERROR));
    }
}
