// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import static com.azure.ai.formrecognizer.TestUtils.FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_URL;
import static com.azure.ai.formrecognizer.TestUtils.getExtractedReceipts;
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
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceiptsFromUrl(sourceUrl);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceiptsFromUrl(sourceUrl, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(includeTextDetails, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptData() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceipts(data, FILE_LENGTH, FormContentType.IMAGE_JPEG, false, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptDataTextDetails() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
                client.beginExtractReceipts(data, FILE_LENGTH, FormContentType.IMAGE_PNG, includeTextDetails, null);
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    /**
     * Test client builder with valid API key
     */
    @Test
    public void validKey() {
        // Arrange
        final FormRecognizerClient client = createClientBuilder(getEndpoint(),
            new AzureKeyCredential(getApiKey())).buildClient();

        // Action and Assert
        validateReceiptResult(false, getExtractedReceipts(),
            client.beginExtractReceiptsFromUrl(RECEIPT_URL).getFinalResult());
    }

    /**
     * Test client builder with invalid API key
     */
    @Test
    public void invalidKey() {
        // Arrange
        final FormRecognizerClient client = createClientBuilder(getEndpoint(),
            new AzureKeyCredential(INVALID_KEY)).buildClient();

        // Action and Assert
        assertThrows(HttpResponseException.class, () -> client.beginExtractReceiptsFromUrl(RECEIPT_URL));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @Test
    public void updateToInvalidKey() {
        // Arrange
        final AzureKeyCredential credential = new AzureKeyCredential(getApiKey());
        final FormRecognizerClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to invalid key
        credential.update(INVALID_KEY);

        // Action and Assert
        assertThrows(HttpResponseException.class, () -> client.beginExtractReceiptsFromUrl(RECEIPT_URL));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @Test
    public void updateToValidKey() {
        // Arrange
        final AzureKeyCredential credential =
            new AzureKeyCredential(INVALID_KEY);

        final FormRecognizerClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to valid key
        credential.update(getApiKey());

        // Action and Assert
        validateReceiptResult(false, getExtractedReceipts(),
            client.beginExtractReceiptsFromUrl(RECEIPT_URL).getFinalResult());
    }

    /**
     * Test for null service version, which would take the default service version by default
     */
    @Test
    public void nullServiceVersion() {
        // Arrange
        final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .apiKey(new AzureKeyCredential(getApiKey()))
            .retryPolicy(new RetryPolicy())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(null);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        // Action and Assert
        validateReceiptResult(false, getExtractedReceipts(),
            client.beginExtractReceiptsFromUrl(RECEIPT_URL).getFinalResult());
    }

    /**
     * Test for default pipeline in client builder
     */
    @Test
    public void defaultPipeline() {
        // Arrange
        final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .apiKey(new AzureKeyCredential(getApiKey()))
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        // Action and Assert
        validateReceiptResult(false, getExtractedReceipts(),
            client.beginExtractReceiptsFromUrl(RECEIPT_URL).getFinalResult());
    }
}
