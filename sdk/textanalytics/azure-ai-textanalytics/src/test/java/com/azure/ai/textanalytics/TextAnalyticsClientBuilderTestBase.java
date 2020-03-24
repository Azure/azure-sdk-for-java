// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
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

import static com.azure.ai.textanalytics.TestUtils.DETECTED_LANGUAGE_ENGLISH;
import static com.azure.ai.textanalytics.TestUtils.DETECT_ENGLISH_LANGUAGE_RESULTS;
import static com.azure.ai.textanalytics.TestUtils.DETECT_FRENCH_LANGUAGE_RESULTS;
import static com.azure.ai.textanalytics.TestUtils.DETECT_LANGUAGE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.FRENCH_SAME_AS_ENGLISH_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.HTTP_RESPONSE_EXCEPTION_CLASS;
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASE_FRENCH_INPUTS;

public abstract class TextAnalyticsClientBuilderTestBase  extends TestBase {
    private static final String AZURE_TEXT_ANALYTICS_API_KEY = "AZURE_TEXT_ANALYTICS_API_KEY";
    private static final String INVALID_KEY = "invalid key";

    // Client builder
    @Test
    abstract void clientBuilderWithValidApiKeyCredential();

    @Test
    abstract void clientBuilderWithInvalidApiKeyCredential();

    @Test
    abstract void clientBuilderWithRotateToInvalidKey();

    @Test
    abstract void clientBuilderWithRotateToValidKey();

    @Test
    abstract void clientBuilderWithNullServiceVersion();

    @Test
    abstract void clientBuilderWithDefaultPipeline();

    @Test
    abstract void clientBuilderWithDefaultCountryHint();

    @Test
    abstract void clientBuilderWithDefaultCountryHintForBatchOperation();

    @Test
    abstract void clientBuilderWithDefaultLanguage();

    @Test
    abstract void clientBuilderWithDefaultLanguageForBatchOperation();

    // Client builder runner
    void clientBuilderWithValidApiKeyCredentialRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(getApiKey()));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithInvalidApiKeyCredentialRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(INVALID_KEY));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), HTTP_RESPONSE_EXCEPTION_CLASS);
    }

    void clientBuilderWithRotateToInvalidKeyRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential(getApiKey());
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(), credential);
        // Update to invalid key
        credential.updateCredential(INVALID_KEY);
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), HTTP_RESPONSE_EXCEPTION_CLASS);
    }

    void clientBuilderWithRotateToValidKeyRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential(INVALID_KEY);
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(getEndpoint(), credential);
        // Update to valid key
        credential.updateCredential(getApiKey());
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithNullServiceVersionRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder =
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey()))
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithDefaultPipelineRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder =
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey()))
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithDefaultCountryHintRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey())).defaultCountryHint("FR"))
            .accept(FRENCH_SAME_AS_ENGLISH_INPUTS.get(0), DETECT_FRENCH_LANGUAGE_RESULTS.get(0));
    }

    void clientBuilderWithDefaultCountryHintForBatchOperationRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<List<String>, List<DetectedLanguage>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey())).defaultCountryHint("FR"))
            .accept(FRENCH_SAME_AS_ENGLISH_INPUTS, DETECT_FRENCH_LANGUAGE_RESULTS);
    }

    void clientBuilderWithNewCountryHintForBatchOperationRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<List<String>, List<DetectedLanguage>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey())).defaultCountryHint("FR"))
            .accept(FRENCH_SAME_AS_ENGLISH_INPUTS, DETECT_ENGLISH_LANGUAGE_RESULTS);
    }

    void clientBuilderWithDefaultLanguageRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, String>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey())).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS.get(1), "Mondly");
    }

    void clientBuilderWithNewLanguageRunner(
        Function<TextAnalyticsClientBuilder, BiConsumer<String, String>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey())).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS.get(1), "Je m'appelle Mondly");
    }

    void clientBuilderWithDefaultLanguageForBatchOperationRunner(Function<TextAnalyticsClientBuilder,
        BiConsumer<List<String>, List<List<String>>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey())).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS,
                Arrays.asList(Collections.singletonList("monde"), Collections.singletonList("Mondly")));
    }

    void clientBuilderWithNewLanguageForBatchOperationRunner(Function<TextAnalyticsClientBuilder,
        BiConsumer<List<String>, List<List<String>>>> testRunner) {
        testRunner.apply(
            createClientBuilder(getEndpoint(), new TextAnalyticsApiKeyCredential(getApiKey())).defaultLanguage("EN"))
            .accept(KEY_PHRASE_FRENCH_INPUTS,
                Arrays.asList(Collections.singletonList("monde"), Collections.singletonList("Je m'appelle Mondly")));
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link TextAnalyticsApiKeyCredential} credential
     * @return {@link TextAnalyticsClientBuilder}
     */
    TextAnalyticsClientBuilder createClientBuilder(String endpoint, TextAnalyticsApiKeyCredential credential) {
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
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
            : Configuration.getGlobalConfiguration().get(AZURE_TEXT_ANALYTICS_API_KEY);
    }
}
