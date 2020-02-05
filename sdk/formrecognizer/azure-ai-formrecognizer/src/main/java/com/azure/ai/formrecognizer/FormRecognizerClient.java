// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.ReceiptPageResult;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are, detect language, recognize entities, recognize PII entities,
 * recognize linked entities, and analyze sentiment for a text input or a list of text inputs.
 *
 * <p><strong>Instantiating a synchronous Form Recognizer Client</strong></p>
 * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.instantiation}
 *
 * <p>View {@link FormRecognizerClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FormRecognizerClientBuilder
 */
@ServiceClient(builder = FormRecognizerClientBuilder.class)
public final class FormRecognizerClient {
    private final FormRecognizerAsyncClient client;

    /**
     * Create a {@code FormRecognizerClient client} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormRecognizerClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link FormRecognizerClient} that the client routes its request through.
     */
    FormRecognizerClient(FormRecognizerAsyncClient client) {
        this.client = client;
    }

    public SyncPoller<OperationResult, IterableStream<ReceiptPageResult>> beginAnalyzeReceipt(String sourceUrl,
        boolean includeTextDetails) {
        return client.beginAnalyzeReceipt(sourceUrl, includeTextDetails).getSyncPoller();
    }

    public SyncPoller<OperationResult, IterableStream<ReceiptPageResult>> beginAnalyzeReceipt(InputStream data,
        long length, boolean includeTextDetails, FormContentType formContentType) throws IOException {
        // TODO: should be able to infer form content type
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(data);
        // TODO: update this
        return client.beginAnalyzeReceipt(buffer, length, includeTextDetails, formContentType).getSyncPoller();
    }
}
