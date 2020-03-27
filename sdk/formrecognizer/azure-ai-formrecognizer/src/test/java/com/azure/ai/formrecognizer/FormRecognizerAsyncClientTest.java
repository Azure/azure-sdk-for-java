// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FormRecognizerApiKeyCredential;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.getExtractedReceipts;

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
                client.beginExtractReceipt(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
        });
    }

    @Test
    void extractReceiptSourceUrlTextDetails() {

    }

    @Test
    void extractReceiptData() {

    }

    @Test
    void extractReceiptDataTextDetails() {

    }

    /**
     * Test client builder with valid API key
     */
    @Test
    public void validKey() {
        // Arrange
        final FormRecognizerAsyncClient client = createClientBuilder(getEndpoint(),
            new FormRecognizerApiKeyCredential(getApiKey())).buildAsyncClient();

        // Action and Assert

    }

    /**
     * Test client builder with invalid API key
     */
    @Test
    public void invalidKey() {
        // Arrange
        final FormRecognizerAsyncClient client = createClientBuilder(getEndpoint(),
            new FormRecognizerApiKeyCredential(INVALID_KEY)).buildAsyncClient();

        // Action and Assert

    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @Test
    public void updateToInvalidKey() {
        // Arrange
        final FormRecognizerApiKeyCredential credential = new FormRecognizerApiKeyCredential(getApiKey());
        final FormRecognizerAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to invalid key
        credential.updateCredential(INVALID_KEY);

        // Action and Assert
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @Test
    public void updateToValidKey() {
        // Arrange
        final FormRecognizerApiKeyCredential credential =
            new FormRecognizerApiKeyCredential(INVALID_KEY);

        final FormRecognizerAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to valid key
        credential.updateCredential(getApiKey());

        // Action and Assert

    }

    /**
     * Test for null service version, which would take the default service version by default
     */
    @Test
    public void nullServiceVersion() {
        // Arrange
        final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .apiKey(new FormRecognizerApiKeyCredential(getApiKey()))
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

    }

    /**
     * Test for default pipeline in client builder
     */
    @Test
    public void defaultPipeline() {
        // Arrange
        final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .apiKey(new FormRecognizerApiKeyCredential(getApiKey()))
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        // Action and Assert

    }
}
