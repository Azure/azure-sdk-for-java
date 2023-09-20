// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
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
import static com.azure.ai.textanalytics.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.textanalytics.TestUtils.HTTP_RESPONSE_EXCEPTION_CLASS;
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASE_FRENCH_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SPANISH_SAME_AS_ENGLISH_INPUTS;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.AZURE_TEXT_ANALYTICS_API_KEY;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_API_KEY;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_ENDPOINT;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.AZURE_TEXT_ANALYTICS_ENDPOINT;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validateKeyPhrases;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validatePrimaryLanguage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Text Analytics client builder
 */
public class TextAnalyticsClientBuilderTest extends TestProxyTestBase {
    private static final String INVALID_KEY = "invalid key";
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";

    /**
     * Test client builder with valid API key
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithValidApiKeyCredential(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithValidApiKeyCredentialRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client builder with invalid API key
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithInvalidApiKeyCredential(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithInvalidApiKeyCredentialRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithRotateToInvalidKey(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithRotateToInvalidKeyRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithRotateToValidKey(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithRotateToValidKeyRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithNullServiceVersion(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithNullServiceVersionRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default pipeline in client builder
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithDefaultPipeline(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultPipelineRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default country hint in client builder for a single document
     */
    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/16415133")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithDefaultCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultCountryHintRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default country hint in client builder for a single document
     */
    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/16415133")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithNewCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultCountryHintRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input, "MX")));
    }

    /**
     * Test for default country hint in client builder for a batch of documents
     */
    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/16415133")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithDefaultCountryHintForBatchOperation(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultCountryHintForBatchOperationRunner(httpClient, serviceVersion,
            clientBuilder -> (input, output) -> {
                final List<DetectLanguageResult> result =
                    clientBuilder.buildClient().detectLanguageBatch(input, "MX", null).stream().collect(Collectors.toList());
                for (int i = 0; i < result.size(); i++) {
                    validatePrimaryLanguage(output.get(i), result.get(i).getPrimaryLanguage());
                }
            });
    }

    /**
     * Test for default country hint in client builder for a batch of documents
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithNewCountryHintForBatchOperation(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithNewCountryHintForBatchOperationRunner(httpClient, serviceVersion,
            clientBuilder -> (input, output) -> {
                final List<DetectLanguageResult> result =
                    clientBuilder.buildClient().detectLanguageBatch(input, "US", null).stream().collect(Collectors.toList());
                for (int i = 0; i < result.size(); i++) {
                    validatePrimaryLanguage(output.get(i), result.get(i).getPrimaryLanguage());
                }
            });
    }

    /**
     * Test for default language in client builder for single document
     */
    @Disabled("Regression output, https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/15811649")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithDefaultLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultLanguageRunner(httpClient, serviceVersion, clientBuilder -> (input, output) -> {
            validateKeyPhrases(output,
                clientBuilder.buildClient().extractKeyPhrases(input).stream().collect(Collectors.toList()));
        });
    }

    /**
     * Test for default language in client builder for single document
     */
    @Disabled("Regression output, https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/15811649")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithNewLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultLanguageRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            assertEquals(output,
                clientBuilder.buildClient().extractKeyPhrases(input, "EN").stream().collect(Collectors.toList())));
    }

    /**
     * Test for default language in client builder for a batch of documents
     */
    @Disabled("Regression output, https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/15811649")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithDefaultLanguageForBatchOperation(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultLanguageForBatchOperationRunner(httpClient, serviceVersion,
            clientBuilder -> (input, output) -> {
                final List<ExtractKeyPhraseResult> result =
                    clientBuilder.buildClient().extractKeyPhrasesBatch(input, "FR", null)
                        .stream().collect(Collectors.toList());
                for (int i = 0; i < result.size(); i++) {
                    validateKeyPhrases(output.get(i),
                        result.get(i).getKeyPhrases().stream().collect(Collectors.toList()));
                }
            });
    }

    /**
     * Test for default language in client builder for a batch of documents
     */
    @Disabled("Regression output, https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/15811649")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithNewLanguageForBatchOperation(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithDefaultLanguageForBatchOperationRunner(httpClient, serviceVersion, clientBuilder -> (input,
            output) -> {
            final List<ExtractKeyPhraseResult> result =
                clientBuilder.buildClient().extractKeyPhrasesBatch(input, "EN", null).stream()
                    .collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validateKeyPhrases(output.get(i), result.get(i).getKeyPhrases().stream().collect(Collectors.toList()));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void clientBuilderWithAAD(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        clientBuilderWithAadRunner(httpClient, serviceVersion, clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    @Test
    @DoNotRecord
    public void applicationIdFallsBackToLogOptions() {
        TextAnalyticsClient textAnalyticsClient =
            new TextAnalyticsClientBuilder()
                .endpoint(getEndpoint(false))
                .credential(new AzureKeyCredential(getApiKey(false)))
                .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
                .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(httpRequest -> {
                    assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                    return Mono.just(new MockHttpResponse(httpRequest, 400));
                })
                .buildClient();
        assertThrows(HttpResponseException.class, () -> textAnalyticsClient.detectLanguage("hello world"));
    }

    @Test
    @DoNotRecord
    public void clientOptionsIsPreferredOverLogOptions() {
        TextAnalyticsClient textAnalyticsClient =
            new TextAnalyticsClientBuilder()
                .endpoint(getEndpoint(false))
                .credential(new AzureKeyCredential(getApiKey(false)))
                .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
                .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
                .httpClient(httpRequest -> {
                    assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                    return Mono.just(new MockHttpResponse(httpRequest, 400));
                })
                .buildClient();
        assertThrows(HttpResponseException.class, () -> textAnalyticsClient.detectLanguage("hello world"));
    }

    @Test
    @DoNotRecord
    public void clientOptionHeadersAreAddedLast() {
        TextAnalyticsClient textAnalyticsClient =
            new TextAnalyticsClientBuilder()
                .endpoint(getEndpoint(false))
                .credential(new AzureKeyCredential(getApiKey(false)))
                .clientOptions(new ClientOptions()
                    .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
                .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(httpRequest -> {
                    assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                    return Mono.just(new MockHttpResponse(httpRequest, 400));
                })
                .buildClient();
        assertThrows(HttpResponseException.class, () -> textAnalyticsClient.detectLanguage("hello world"));
    }

    // Client builder runner
    void clientBuilderWithValidApiKeyCredentialRunner(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(false), new AzureKeyCredential(getApiKey(false)));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithInvalidApiKeyCredentialRunner(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(false), new AzureKeyCredential(INVALID_KEY));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), HTTP_RESPONSE_EXCEPTION_CLASS);
    }

    void clientBuilderWithRotateToInvalidKeyRunner(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, HttpResponseException>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(getApiKey(false));
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(false), credential);
        // Update to invalid key
        credential.update(INVALID_KEY);
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), HTTP_RESPONSE_EXCEPTION_CLASS);
    }

    void clientBuilderWithRotateToValidKeyRunner(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final AzureKeyCredential credential = new AzureKeyCredential(INVALID_KEY);
        final TextAnalyticsClientBuilder clientBuilder = createClientBuilder(httpClient, serviceVersion,
            getEndpoint(false), credential);
        // Update to valid key
        credential.update(getApiKey(false));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithNullServiceVersionRunner(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(false),
                new AzureKeyCredential(getApiKey(false)))
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithDefaultPipelineRunner(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        final TextAnalyticsClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(false),
                new AzureKeyCredential(getApiKey(false)))
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    void clientBuilderWithDefaultCountryHintRunner(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {
        testRunner.apply(
            createClientBuilder(httpClient, serviceVersion, getEndpoint(false),
                new AzureKeyCredential(getApiKey(false))).defaultCountryHint("MX"))
            .accept(SPANISH_SAME_AS_ENGLISH_INPUTS.get(0), DETECT_SPANISH_LANGUAGE_RESULTS.get(0));
    }

    void clientBuilderWithDefaultCountryHintForBatchOperationRunner(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<List<String>, List<DetectedLanguage>>> testRunner) {
        testRunner.apply(
            createClientBuilder(httpClient, serviceVersion,
                getEndpoint(false), new AzureKeyCredential(getApiKey(false))).defaultCountryHint("MX"))
            .accept(SPANISH_SAME_AS_ENGLISH_INPUTS, DETECT_SPANISH_LANGUAGE_RESULTS);
    }

    void clientBuilderWithNewCountryHintForBatchOperationRunner(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<List<String>, List<DetectedLanguage>>> testRunner) {
        testRunner.apply(
            createClientBuilder(httpClient, serviceVersion,
                getEndpoint(false), new AzureKeyCredential(getApiKey(false))).defaultCountryHint("MX"))
            .accept(SPANISH_SAME_AS_ENGLISH_INPUTS, DETECT_ENGLISH_LANGUAGE_RESULTS);
    }

    void clientBuilderWithDefaultLanguageRunner(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, List<String>>> testRunner) {
        testRunner.apply(
            createClientBuilder(httpClient, serviceVersion,
                getEndpoint(false), new AzureKeyCredential(getApiKey(false))).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS.get(0), Arrays.asList("Bonjour", "monde"));
    }

    void clientBuilderWithDefaultLanguageForBatchOperationRunner(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder,
        BiConsumer<List<String>, List<List<String>>>> testRunner) {
        testRunner.apply(
            createClientBuilder(httpClient, serviceVersion,
                getEndpoint(false), new AzureKeyCredential(getApiKey(false))).defaultLanguage("FR"))
            .accept(KEY_PHRASE_FRENCH_INPUTS,
                Arrays.asList(Arrays.asList("Bonjour", "monde"), Collections.singletonList("Mondly")));
    }

    void clientBuilderWithAadRunner(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        Function<TextAnalyticsClientBuilder, BiConsumer<String, DetectedLanguage>> testRunner) {

        final TextAnalyticsClientBuilder clientBuilder =
            new TextAnalyticsClientBuilder()
                .endpoint(getEndpoint(false))
                .pipeline(getHttpPipeline(httpClient))
                .serviceVersion(serviceVersion);

        if (interceptorManager.isRecordMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (interceptorManager.isPlaybackMode()) {
            // since running in playback mode won't have the token credential, so skipping matching it.
            interceptorManager.addMatchers(Arrays.asList(
                new CustomMatcher().setExcludedHeaders(Arrays.asList("Authorization"))));

            clientBuilder.credential(new MockTokenCredential());
        }
        if (!interceptorManager.isPlaybackMode()) {
            clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        testRunner.apply(clientBuilder).accept(DETECT_LANGUAGE_INPUTS.get(0), DETECTED_LANGUAGE_ENGLISH);
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new AddHeadersPolicy(new HttpHeaders()));
        policies.add(new AddHeadersFromContextPolicy());
        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION,  Configuration.getGlobalConfiguration().clone()));
        policies.add(new RequestIdPolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy());
        policies.add(new AddDatePolicy());

        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, DEFAULT_SCOPE));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
                                    .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                    .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
                                    .build();

        return pipeline;
    }

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link AzureKeyCredential} credential
     * @return {@link TextAnalyticsClientBuilder}
     */
    TextAnalyticsClientBuilder createClientBuilder(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion,
        String endpoint, AzureKeyCredential credential) {
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .credential(credential)
            .endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .serviceVersion(serviceVersion);

        if (interceptorManager.isRecordMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return clientBuilder;
    }

    String getEndpoint(boolean isStaticResource) {
        return interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : isStaticResource ? AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_ENDPOINT : AZURE_TEXT_ANALYTICS_ENDPOINT;
    }

    String getApiKey(boolean isStaticSource) {
        return interceptorManager.isPlaybackMode() ? "apiKeyInPlayback"
            : isStaticSource ? AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_API_KEY : AZURE_TEXT_ANALYTICS_API_KEY;
    }
}
