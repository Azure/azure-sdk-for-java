// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf;

import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.perf.core.ServiceTest;
import com.azure.core.util.polling.SyncPoller;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReceiptRecognitionUrlTest extends ServiceTest<PerfStressOptions> {
    private final String receiptFileUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/"
        + "master/sdk/formrecognizer/azure-ai-formrecognizer/src/test/resources/sample_files/Test/contoso-receipt.png";

    public ReceiptRecognitionUrlTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptFileUrl).waitForCompletion();
    }

    @Override
    public Mono<Void> runAsync() {
        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
            formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptFileUrl).getSyncPoller();
        syncPoller.waitForCompletion();
        return Mono.empty();
    }
}
