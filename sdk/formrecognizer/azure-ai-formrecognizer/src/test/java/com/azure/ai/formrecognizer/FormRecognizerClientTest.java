// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static com.azure.ai.formrecognizer.TestUtils.FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.getExtractedReceipts;

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
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceipt(sourceUrl);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceipt(sourceUrl, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(includeTextDetails, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptData() throws FileNotFoundException {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceipt(data, FILE_LENGTH, FormContentType.IMAGE_PNG, false, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptDataTextDetails() throws FileNotFoundException {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceipt(data, FILE_LENGTH, FormContentType.IMAGE_PNG, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }
}
