// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.validateLayoutDataResults;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_URL;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedFormPages;
import static com.azure.ai.formrecognizer.TestUtils.getPageResults;
import static com.azure.ai.formrecognizer.TestUtils.getReadResults;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Form Recognizer client builder
 */
public class FormRecognizerClientBuilderTest extends TestBase {
    private static final String AZURE_FORM_RECOGNIZER_API_KEY = "AZURE_FORM_RECOGNIZER_API_KEY";
    private static final String INVALID_KEY = "invalid key";

    /**
     * Test client builder with invalid API key
     */
    @Test
    public void clientBuilderWithInvalidApiKeyCredential() {
        clientBuilderWithInvalidApiKeyCredentialRunner(clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().beginRecognizeContentFromUrl(input)));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @Test
    public void clientBuilderWithRotateToValidKey() {
        clientBuilderWithRotateToValidKeyRunner(clientBuilder -> (input, output) ->
            validateLayoutDataResults(clientBuilder.buildClient().beginRecognizeContentFromUrl(input).getFinalResult(),
                getReadResults(), getPageResults(), false));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @Test
    public void clientBuilderWithRotateToInvalidKey() {
        clientBuilderWithRotateToInvalidKeyRunner(clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().beginRecognizeContentFromUrl(input)));
    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @Test
    public void clientBuilderWithNullServiceVersion() {
        clientBuilderWithNullServiceVersionRunner(clientBuilder -> (input, output) ->
            validateLayoutDataResults(clientBuilder.buildClient().beginRecognizeContentFromUrl(input).getFinalResult(),
                getReadResults(), getPageResults(), false));
    }

    /**
     * Test for default pipeline in client builder
     */
    @Test
    public void clientBuilderWithDefaultPipeline() {
        clientBuilderWithDefaultPipelineRunner(clientBuilder -> (input, output) ->
            validateLayoutDataResults(clientBuilder.buildClient().beginRecognizeContentFromUrl(input).getFinalResult(),
                getReadResults(), getPageResults(), false));
    }

    // Client builder runner
    void clientBuilderWithInvalidApiKeyCredentialRunner(
        Function<FormRecognizerClientBuilder, BiConsumer<String, ErrorResponseException>> testRunner) {
        final FormRecognizerClientBuilder clientBuilder = createClientBuilder(getEndpoint(),
            new AzureKeyCredential(INVALID_KEY));
        testRunner.apply(clientBuilder).accept(LAYOUT_URL, new ErrorResponseException("", null));
    }

    void clientBuilderWithRotateToInvalidKeyRunner(
        Function<FormRecognizerClientBuilder, BiConsumer<String, ErrorResponseException>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(getApiKey());
        final FormRecognizerClientBuilder clientBuilder = createClientBuilder(getEndpoint(), credential);
        // Update to invalid key
        credential.update(INVALID_KEY);
        testRunner.apply(clientBuilder).accept(LAYOUT_URL, new ErrorResponseException("", null));
    }

    void clientBuilderWithRotateToValidKeyRunner(
        Function<FormRecognizerClientBuilder, BiConsumer<String, IterableStream<FormPage>>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(INVALID_KEY);
        final FormRecognizerClientBuilder clientBuilder = createClientBuilder(getEndpoint(), credential);
        // Update to valid key
        credential.update(getApiKey());
        testRunner.apply(clientBuilder).accept(LAYOUT_URL, getExpectedFormPages());
    }

    void clientBuilderWithNullServiceVersionRunner(
        Function<FormRecognizerClientBuilder, BiConsumer<String, IterableStream<FormPage>>> testRunner) {
        final FormRecognizerClientBuilder clientBuilder =
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey()))
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.apply(clientBuilder).accept(LAYOUT_URL, getExpectedFormPages());
    }

    void clientBuilderWithDefaultPipelineRunner(
        Function<FormRecognizerClientBuilder, BiConsumer<String, IterableStream<FormPage>>> testRunner) {
        final FormRecognizerClientBuilder clientBuilder =
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey()))
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.apply(clientBuilder).accept(LAYOUT_URL, getExpectedFormPages());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_ENDPOINT");
    }

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link AzureKeyCredential} credential
     *
     * @return {@link FormRecognizerClientBuilder}
     */
    FormRecognizerClientBuilder createClientBuilder(String endpoint, AzureKeyCredential credential) {
        final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .apiKey(credential)
            .endpoint(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        return clientBuilder;
    }

    /**
     * Get the string of API key value based on what running mode is on.
     *
     * @return the API key string
     */
    String getApiKey() {
        return interceptorManager.isPlaybackMode() ? "apiKeyInPlayback"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_API_KEY);
    }
}
