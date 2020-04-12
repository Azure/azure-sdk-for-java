// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.formrecognizer.TestUtils.FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_STATUS_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.VALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedFormPages;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedReceipts;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedRecognizedForms;
import static com.azure.ai.formrecognizer.TestUtils.getFileBufferData;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildAsyncClient());
    }

    // Extract receipts
    @Test
    void extractReceiptSourceUrl() {
        receiptSourceUrlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(true, getExpectedReceipts(true), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptData() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(getFileBufferData(data), FILE_LENGTH, FormContentType.IMAGE_JPEG, false,
                    null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptDataTextDetails() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller
                = client.beginRecognizeReceipts(getFileBufferData(data), FILE_LENGTH, FormContentType.IMAGE_JPEG, includeTextDetails,
                null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false),
                syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptInvalidSourceUrl() {
        invalidSourceUrlRunner((sourceUrl) -> {
            assertThrows(HttpResponseException.class, () ->
                client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller());
        });
    }

    @Test
    void extractLayoutValidSourceUrl() {
        layoutValidSourceUrlRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller
                = client.beginRecognizeContent(getFileBufferData(data),
                FormContentType.IMAGE_JPEG, FILE_LENGTH, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateLayoutResult(getExpectedFormPages(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractLayoutInValidSourceUrl() {
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            assertThrows(HttpResponseException.class, () ->
                client.beginRecognizeContentFromUrl(invalidSourceUrl).getSyncPoller());
        });
    }

    @Test
    void extractCustomFormValidSourceUrl() {
        customFormValidSourceUrlRunner((data, validModelId) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(getFileBufferData(data), validModelId,
                FILE_LENGTH, FormContentType.APPLICATION_PDF).getSyncPoller();
            syncPoller.waitForCompletion();
            validateRecognizedFormResult(getExpectedRecognizedForms(false), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractCustomFormInValidSourceUrl() {
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> client.beginRecognizeCustomFormsFromUrl(INVALID_URL, VALID_MODEL_ID).getSyncPoller().getFinalResult());

        assertTrue(thrown.getMessage().equals(INVALID_MODEL_STATUS_ERROR));
    }
}
