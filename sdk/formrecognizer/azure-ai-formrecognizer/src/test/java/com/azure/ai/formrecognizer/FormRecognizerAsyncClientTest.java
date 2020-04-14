// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.formrecognizer.TestUtils.FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.getExtractedReceipts;
import static com.azure.ai.formrecognizer.TestUtils.getReceiptFileBufferData;
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
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceiptsFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceiptsFromUrl(sourceUrl, includeTextDetails, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(true, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptData() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceipts(getReceiptFileBufferData(), FILE_LENGTH, false,
                    FormContentType.IMAGE_JPEG, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptDataTextDetails() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller
                = client.beginExtractReceipts(getReceiptFileBufferData(), FILE_LENGTH, includeTextDetails,
                FormContentType.IMAGE_JPEG, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptInvalidSourceUrl() {
        receiptInvalidSourceUrlRunner((sourceUrl) -> {
            assertThrows(HttpResponseException.class, () -> client.beginExtractReceiptsFromUrl(sourceUrl).getSyncPoller());
        });
    }

    /**
     * Test for listing all models information.
     */
    @Test
    void listModels() {
        StepVerifier.create(client.listModels())
            .thenConsumeWhile(customFormModelInfo ->
                customFormModelInfo.getModelId() != null && customFormModelInfo.getCreatedOn() != null
                && customFormModelInfo.getLastUpdatedOn() != null && customFormModelInfo.getStatus() != null)
            .verifyComplete();
    }

    /**
     * Test for listing all models information with {@link Context}.
     */
    @Test
    void listModelsWithContext() {
        StepVerifier.create(client.listModels(Context.NONE))
            .thenConsumeWhile(modelInfo ->
                modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                    && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null)
            .verifyComplete();
    }
}
