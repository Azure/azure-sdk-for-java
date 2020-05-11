// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.DETECTED_LANGUAGE_ENGLISH;
import static com.azure.ai.textanalytics.TestUtils.DETECT_ENGLISH_LANGUAGE_RESULTS;
import static com.azure.ai.textanalytics.TestUtils.DETECT_LANGUAGE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.DETECT_SPANISH_LANGUAGE_RESULTS;
import static com.azure.ai.textanalytics.TestUtils.HTTP_RESPONSE_EXCEPTION_CLASS;
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASE_FRENCH_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SPANISH_SAME_AS_ENGLISH_INPUTS;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validateKeyPhrases;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validatePrimaryLanguage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Text Analytics client builder
 */
public class TextAnalyticsClientBuilderTest extends TestBase {
    private static final String AZURE_TEXT_ANALYTICS_API_KEY = "AZURE_TEXT_ANALYTICS_API_KEY";
    private static final String INVALID_KEY = "invalid key";

    /**
     * Test client builder with valid API key
     */
    @Test
    public void clientBuilderWithValidApiKeyCredential() {
        clientBuilderWithValidApiKeyCredentialRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client builder with invalid API key
     */
    @Test
    public void clientBuilderWithInvalidApiKeyCredential() {
        clientBuilderWithInvalidApiKeyCredentialRunner(clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @Test
    public void clientBuilderWithRotateToInvalidKey() {
        clientBuilderWithRotateToInvalidKeyRunner(clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @Test
    public void clientBuilderWithRotateToValidKey() {
        clientBuilderWithRotateToValidKeyRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @Test
    public void clientBuilderWithNullServiceVersion() {
        clientBuilderWithNullServiceVersionRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default pipeline in client builder
     */
    @Test
    public void clientBuilderWithDefaultPipeline() {
        clientBuilderWithDefaultPipelineRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default country hint in client builder for a single document
     */
    @Test
    public void clientBuilderWithDefaultCountryHint() {
        clientBuilderWithDefaultCountryHintRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default country hint in client builder for a single document
     */
    @Test
    public void clientBuilderWithNewCountryHint() {
        clientBuilderWithDefaultCountryHintRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input, "MX")));
    }

    /**
     * Test for default country hint in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithDefaultCountryHintForBatchOperation() {
        clientBuilderWithDefaultCountryHintForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<DetectLanguageResult> result =
                clientBuilder.buildClient().detectLanguageBatch(input).stream().collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validatePrimaryLanguage(output.get(i), result.get(i).getPrimaryLanguage());
            }
        });
    }

    /**
     * Test for default country hint in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithNewCountryHintForBatchOperation() {
        clientBuilderWithNewCountryHintForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<DetectLanguageResult> result =
                clientBuilder.buildClient().detectLanguageBatch(input, "US").stream().collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validatePrimaryLanguage(output.get(i), result.get(i).getPrimaryLanguage());
            }
        });
    }

    /**
     * Test for default language in client builder for single document
     */
    @Test
    public void clientBuilderWithDefaultLanguage() {
        clientBuilderWithDefaultLanguageRunner(clientBuilder -> (input, output) ->
            assertEquals(output, clientBuilder.buildClient().extractKeyPhrases(input).iterator().next()));
    }

    /**
     * Test for default language in client builder for single document
     */
    @Test
    public void clientBuilderWithNewLanguage() {
        clientBuilderWithNewLanguageRunner(clientBuilder -> (input, output) ->
            assertEquals(output, clientBuilder.buildClient().extractKeyPhrases(input, "EN").iterator().next()));
    }

    /**
     * Test for default language in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithDefaultLanguageForBatchOperation() {
        clientBuilderWithDefaultLanguageForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<ExtractKeyPhraseResult> result =
                clientBuilder.buildClient().extractKeyPhrasesBatch(input).stream().collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validateKeyPhrases(output.get(i), result.get(i).getKeyPhrases().stream().collect(Collectors.toList()));
            }
        });
    }

    /**
     * Test for default language in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithNewLanguageForBatchOperation() {
        clientBuilderWithNewLanguageForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<ExtractKeyPhraseResult> result =
                clientBuilder.buildClient().extractKeyPhrasesBatch(input, "EN").stream()
                    .collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validateKeyPhrases(output.get(i), result.get(i).getKeyPhrases().stream().collect(Collectors.toList()));
            }
        });
    }

    // Client builder runner
    void clientBuilderWithValidApiKeyCredentialRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(),
            new AzureKeyCredential(getApiKey()));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithInvalidApiKeyCredentialRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(),
            new AzureKeyCredential(INVALID_KEY));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), HTTP_RESPONSE_EXCEPTION_CLASS);
    }

    void clientBuilderWithRotateToInvalidKeyRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(getApiKey());
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(), credential);
        // Update to invalid key
        credential.update(INVALID_KEY);
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), HTTP_RESPONSE_EXCEPTION_CLASS);
    }

    void clientBuilderWithRotateToValidKeyRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(INVALID_KEY);
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(), credential);
        // Update to valid key
        credential.update(getApiKey());
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithNullServiceVersionRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder =
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey()))
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithDefaultPipelineRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder =
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey()))
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithDefaultCountryHintRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey())).defaultCountryHint("MX"))
            .accept(SPANISH_SAME_AS_ENGLISH_INPUTS.get(0), DETECT_SPANISH_LANGUAGE_RESULTS.get(0));
    }

    void clientBuilderWithDefaultCountryHintForBatchOperationRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<List<String>, List<DetectedLanguage>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey())).defaultCountryHint("MX"))
            .accept(SPANISH_SAME_AS_ENGLISH_INPUTS, DETECT_SPANISH_LANGUAGE_RESULTS);
    }

    void clientBuilderWithNewCountryHintForBatchOperationRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<List<String>, List<DetectedLanguage>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey())).defaultCountryHint("MX"))
            .accept(SPANISH_SAME_AS_ENGLISH_INPUTS, DETECT_ENGLISH_LANGUAGE_RESULTS);
    }

    void clientBuilderWithDefaultLanguageRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, String>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey())).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS.get(1), "Mondly");
    }

    void clientBuilderWithNewLanguageRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, String>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey())).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS.get(1), "Je m'appelle Mondly");
    }

    void clientBuilderWithDefaultLanguageForBatchOperationRunner(Function<TextAnalyticsClientBuilder,
        BiConsumer<List<String>, List<List<String>>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey())).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS,
                Arrays.asList(Collections.singletonList("monde"), Collections.singletonList("Mondly")));
    }

    void clientBuilderWithNewLanguageForBatchOperationRunner(Function<TextAnalyticsClientBuilder,
        BiConsumer<List<String>, List<List<String>>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new AzureKeyCredential(getApiKey())).defaultLanguage("EN"))
            .accept(KEY_PHRASE_FRENCH_INPUTS,
                Arrays.asList(Collections.singletonList("monde"), Collections.singletonList("Je m'appelle Mondly")));
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link AzureKeyCredential} credential
     * @return {@link TextAnalyticsClientBuilder}
     */
    TextAnalyticsClientBuilder createClientBuilder(String endpoint, AzureKeyCredential credential) {
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .credential(credential)
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
            : Configuration.getGlobalConfiguration().get(AZURE_TEXT_ANALYTICS_API_KEY);
    }
}
