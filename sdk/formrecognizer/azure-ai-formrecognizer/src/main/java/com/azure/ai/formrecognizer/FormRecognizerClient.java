// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are, to extract receipt data fields from receipt documents.
 *
 * @see FormRecognizerClientBuilder
 */
@ServiceClient(builder = FormRecognizerClientBuilder.class)
public final class FormRecognizerClient {
    private final FormRecognizerAsyncClient client;

    /**
     * Create a {@link FormRecognizerClient client} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormRecognizerClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link FormRecognizerClient} that the client routes its request through.
     */
    FormRecognizerClient(FormRecognizerAsyncClient client) {
        this.client = client;
    }

    /**
     * Detects and extracts data from receipts using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract receipt operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> beginExtractReceiptsFromUrl(String sourceUrl) {
        return beginExtractReceiptsFromUrl(sourceUrl, false, null);
    }

    /**
     * Detects and extracts data from receipts using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract receipt operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<ExtractedReceipt>>
        beginExtractReceiptsFromUrl(String sourceUrl, boolean includeTextDetails, Duration pollInterval) {
        return client.beginExtractReceiptsFromUrl(sourceUrl, includeTextDetails, pollInterval).getSyncPoller();
    }

    /**
     * Detects and extracts data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<ExtractedReceipt>>
        beginExtractReceipts(InputStream data, long length, FormContentType formContentType) {
        return beginExtractReceipts(data, length, formContentType, false, null);
    }

    /**
     * Detects and extracts data from the providedd document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<ExtractedReceipt>>
        beginExtractReceipts(InputStream data, long length, FormContentType formContentType, boolean includeTextDetails,
                         Duration pollInterval) {
        // TODO: #9248 should be able to infer form content type
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(data);
        return client.beginExtractReceipts(buffer, length, includeTextDetails, formContentType, pollInterval)
            .getSyncPoller();
    }
}
