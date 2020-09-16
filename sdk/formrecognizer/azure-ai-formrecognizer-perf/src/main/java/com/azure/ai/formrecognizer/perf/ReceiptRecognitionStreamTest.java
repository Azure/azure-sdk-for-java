// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf;

import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.perf.core.ServiceTest;
import com.azure.ai.formrecognizer.perf.core.Utility;
import com.azure.core.util.polling.SyncPoller;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ReceiptRecognitionStreamTest extends ServiceTest<PerfStressOptions> {
    private FileInputStream fileInputStream;
    private String receiptFilePath = "src/main/resources/samplesFiles/contoso-receipt.png";
    private long fileLength;
    public ReceiptRecognitionStreamTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(createReceiptInputStream());
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return closeReceiptInputStream().then(super.globalCleanupAsync());
    }

    @Override
    public void run() {
        formRecognizerClient.beginRecognizeReceipts(fileInputStream, fileLength).waitForCompletion();
    }

    @Override
    public Mono<Void> runAsync() {
        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
        formRecognizerAsyncClient.beginRecognizeReceipts(Utility.toFluxByteBuffer(fileInputStream), fileLength)
            .getSyncPoller();
        syncPoller.waitForCompletion();
        return Mono.empty();
    }

    private Mono<Void> createReceiptInputStream() {

        try {
            fileInputStream =  new FileInputStream(receiptFilePath);
            fileLength = new File(receiptFilePath).length();
            return Mono.empty();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local file not found.", e);
        }
    }

    private Mono<Void> closeReceiptInputStream() {
        try {
            fileInputStream.close();
            fileLength = 0;
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException("Can't close file input stream.", e);
        }
    }
}
