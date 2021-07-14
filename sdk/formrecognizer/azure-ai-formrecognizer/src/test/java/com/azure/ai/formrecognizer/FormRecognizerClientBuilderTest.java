// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.HTTPS_EXCEPTION_MESSAGE;
import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.INVALID_ENDPOINT;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_API_KEY;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_ENDPOINT;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.URL_TEST_FILE_FORMAT;
import static com.azure.ai.formrecognizer.TestUtils.VALID_HTTP_LOCALHOST;
import static com.azure.ai.formrecognizer.TestUtils.setSyncPollerPollInterval;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Form Recognizer client builder
 */
public class FormRecognizerClientBuilderTest extends TestBase {
    /**
     * Test client builder with invalid API key
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void clientBuilderWithInvalidApiKeyCredential(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithInvalidApiKeyCredentialRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().beginRecognizeContentFromUrl(input)));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void clientBuilderWithRotateToInvalidKey(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithRotateToInvalidKeyRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().beginRecognizeContentFromUrl(input)));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void clientBuilderWithRotateToValidKey(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithRotateToValidKeyRunner(httpClient, serviceVersion, clientBuilder -> (input) ->
            assertNotNull(setSyncPollerPollInterval(clientBuilder.buildClient()
                .beginRecognizeContentFromUrl(input), interceptorManager).getFinalResult()));
    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void clientBuilderWithNullServiceVersion(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithNullServiceVersionRunner(httpClient, serviceVersion, clientBuilder -> (input) ->
            assertNotNull(setSyncPollerPollInterval(clientBuilder.buildClient()
                .beginRecognizeContentFromUrl(input), interceptorManager).getFinalResult()));
    }

    /**
     * Test for default pipeline in client builder
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void clientBuilderWithDefaultPipeline(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithDefaultPipelineRunner(httpClient, serviceVersion, clientBuilder -> (input) ->
            assertNotNull(setSyncPollerPollInterval(clientBuilder.buildClient()
                .beginRecognizeContentFromUrl(input), interceptorManager).getFinalResult()));
    }

    /**
     * Test for invalid endpoint, which throws connection refused exception message.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void clientBuilderWithInvalidEndpoint(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithDefaultPipelineRunner(httpClient, serviceVersion, clientBuilder -> (input) -> {
            assertThrows(RuntimeException.class,
                () -> clientBuilder.endpoint(INVALID_ENDPOINT)
                    .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                    .buildClient()
                    .beginRecognizeContentFromUrl(input).getFinalResult());
        });
    }

    /**
     * Test for an valid http endpoint, which throws HTTPS requirement exception message.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void clientBuilderWithHttpEndpoint(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithDefaultPipelineRunner(httpClient, serviceVersion, clientBuilder -> (input) -> {
            Exception exception = assertThrows(RuntimeException.class,
                () -> clientBuilder.endpoint(VALID_HTTP_LOCALHOST)
                    .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                    .buildClient()
                    .beginRecognizeContentFromUrl(input).getFinalResult());
            assertEquals(HTTPS_EXCEPTION_MESSAGE, exception.getMessage());
        });
    }

    @Test
    @DoNotRecord
    public void applicationIdFallsBackToLogOptions() {
        FormRecognizerClient formRecognizerClient =
            new FormRecognizerClientBuilder()
                .endpoint(getEndpoint())
                .credential(new AzureKeyCredential(getApiKey()))
                .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
                .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(httpRequest -> {
                    assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                    return Mono.just(new MockHttpResponse(httpRequest, 400));
                })
                .buildClient();
        assertThrows(HttpResponseException.class,
            () -> formRecognizerClient.beginRecognizeContentFromUrl(URL_TEST_FILE_FORMAT + CONTENT_FORM_JPG));
    }

    @Test
    @DoNotRecord
    public void clientOptionsIsPreferredOverLogOptions() {
        FormRecognizerClient formRecognizerClient =
            new FormRecognizerClientBuilder()
                .endpoint(getEndpoint())
                .credential(new AzureKeyCredential(getApiKey()))
                .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
                .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
                .httpClient(httpRequest -> {
                    assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                    return Mono.just(new MockHttpResponse(httpRequest, 400));
                })
                .buildClient();
        assertThrows(HttpResponseException.class,
            () -> formRecognizerClient.beginRecognizeContentFromUrl(URL_TEST_FILE_FORMAT + CONTENT_FORM_JPG));
    }

    @Test
    @DoNotRecord
    public void clientOptionHeadersAreAddedLast() {
        FormRecognizerClient formRecognizerClient =
            new FormRecognizerClientBuilder()
                .endpoint(getEndpoint())
                .credential(new AzureKeyCredential(getApiKey()))
                .clientOptions(new ClientOptions()
                                   .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
                .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(httpRequest -> {
                    assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                    return Mono.just(new MockHttpResponse(httpRequest, 400));
                })
                .buildClient();
        assertThrows(HttpResponseException.class,
            () -> formRecognizerClient.beginRecognizeContentFromUrl(URL_TEST_FILE_FORMAT + CONTENT_FORM_JPG));
    }

    // Client builder runner
    void clientBuilderWithInvalidApiKeyCredentialRunner(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion,
        Function<FormRecognizerClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final FormRecognizerClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion, getEndpoint(),
            new AzureKeyCredential(INVALID_KEY));
        testRunner.apply(clientBuilder).accept(CONTENT_FORM_JPG, new HttpResponseException("", null));
    }

    void clientBuilderWithRotateToInvalidKeyRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormRecognizerClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(getApiKey());
        final FormRecognizerClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(), credential);
        // Update to invalid key
        credential.update(INVALID_KEY);
        testRunner.apply(clientBuilder).accept(CONTENT_FORM_JPG, new HttpResponseException("", null));
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
    }

    void clientBuilderWithRotateToValidKeyRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormRecognizerClientBuilder, Consumer<String>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(INVALID_KEY);
        final FormRecognizerClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(), credential);
        // Update to valid key
        credential.update(getApiKey());
        testRunner.apply(clientBuilder).accept(URL_TEST_FILE_FORMAT + CONTENT_FORM_JPG);
    }

    void clientBuilderWithNullServiceVersionRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormRecognizerClientBuilder, Consumer<String>> testRunner) {
        final FormRecognizerClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), new AzureKeyCredential(getApiKey()))
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.apply(clientBuilder).accept(URL_TEST_FILE_FORMAT + CONTENT_FORM_JPG);
    }

    void clientBuilderWithDefaultPipelineRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormRecognizerClientBuilder, Consumer<String>> testRunner) {
        final FormRecognizerClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), new AzureKeyCredential(getApiKey()))
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.apply(clientBuilder).accept(URL_TEST_FILE_FORMAT + CONTENT_FORM_JPG);
    }

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link AzureKeyCredential} credential
     *
     * @return {@link FormRecognizerClientBuilder}
     */
    FormRecognizerClientBuilder createClientBuilder(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        String endpoint, AzureKeyCredential credential) {
        final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .credential(credential)
            .endpoint(endpoint)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .serviceVersion(serviceVersion);

        if (!interceptorManager.isPlaybackMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
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
