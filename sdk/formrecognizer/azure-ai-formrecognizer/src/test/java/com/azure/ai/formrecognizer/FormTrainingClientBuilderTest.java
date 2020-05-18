// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_API_KEY;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_ENDPOINT;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.VALID_URL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Form Training client builder
 */
public class FormTrainingClientBuilderTest extends TestBase {
    private static final String FORM_JPG = "Form_1.jpg";

    /**
     * Test client builder with invalid API key
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void trainingClientBuilderInvalidKeyCredential(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithInvalidApiKeyCredentialRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().getAccountProperties()));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void trainingClientBuilderRotateToValidKey(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithRotateToValidKeyRunner(httpClient, serviceVersion, clientBuilder -> (input) ->
            assertNotNull(clientBuilder.buildClient().getFormRecognizerClient().beginRecognizeContentFromUrl(input).getFinalResult()));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void trainingClientBuilderRotateToInvalidKey(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithRotateToInvalidKeyRunner(httpClient, serviceVersion, clientBuilder -> (output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().getAccountProperties()));
    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void trainingClientBuilderNullServiceVersion(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithNullServiceVersionRunner(httpClient, serviceVersion, clientBuilder -> (input) ->
            assertNotNull(clientBuilder.buildClient().getFormRecognizerClient().beginRecognizeContentFromUrl(input).getFinalResult()));
    }

    /**
     * Test for default pipeline in client builder
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void trainingClientBuilderDefaultPipeline(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        clientBuilderWithDefaultPipelineRunner(httpClient, serviceVersion, clientBuilder -> (input) ->
            assertNotNull(clientBuilder.buildClient().getFormRecognizerClient().beginRecognizeContentFromUrl(input).getFinalResult()));
    }

    // Client builder runner
    void clientBuilderWithInvalidApiKeyCredentialRunner(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion,
        Function<FormTrainingClientBuilder, BiConsumer<String, ErrorResponseException>> testRunner) {
        final FormTrainingClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion, getEndpoint(),
            new AzureKeyCredential(INVALID_KEY));
        testRunner.apply(clientBuilder).accept(VALID_URL, new ErrorResponseException("", null));
    }

    void clientBuilderWithRotateToInvalidKeyRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormTrainingClientBuilder, Consumer<ErrorResponseException>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(getApiKey());
        final FormTrainingClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(), credential);
        // Update to invalid key
        credential.update(INVALID_KEY);
        testRunner.apply(clientBuilder).accept(new ErrorResponseException("", null));
    }

    void clientBuilderWithRotateToValidKeyRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormTrainingClientBuilder, Consumer<String>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(INVALID_KEY);
        final FormTrainingClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(), credential);
        // Update to valid key
        credential.update(getApiKey());
        testRunner.apply(clientBuilder).accept(getTestingSasUri(FORM_JPG));
    }

    void clientBuilderWithNullServiceVersionRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormTrainingClientBuilder, Consumer<String>> testRunner) {
        final FormTrainingClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), new AzureKeyCredential(getApiKey()))
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.apply(clientBuilder).accept(getTestingSasUri(FORM_JPG));
    }

    void clientBuilderWithDefaultPipelineRunner(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        Function<FormTrainingClientBuilder, Consumer<String>> testRunner) {
        final FormTrainingClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), new AzureKeyCredential(getApiKey()))
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.apply(clientBuilder).accept(getTestingSasUri(FORM_JPG));
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
    }

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link AzureKeyCredential} credential
     *
     * @return {@link FormTrainingClientBuilder}
     */
    FormTrainingClientBuilder createClientBuilder(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion,
        String endpoint, AzureKeyCredential credential) {
        final FormTrainingClientBuilder clientBuilder = new FormTrainingClientBuilder()
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

    private String getTestingSasUri(String fileName) {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        }

        final String testingFileUrl =
            Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL);
        final String[] urlParts = testingFileUrl.split("\\?");
        return urlParts[0] + "/" + fileName + "?" + urlParts[1];
    }
}
