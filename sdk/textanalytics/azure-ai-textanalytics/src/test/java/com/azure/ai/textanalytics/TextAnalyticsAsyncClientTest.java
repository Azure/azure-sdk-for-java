// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.textanalytics.TestUtils.FAKE_API_KEY;
import static com.azure.ai.textanalytics.TestUtils.getCategorizedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static com.azure.ai.textanalytics.models.WarningCode.LONG_WORDS_IN_DOCUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsAsyncClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private TextAnalyticsAsyncClient getTextAnalyticsAsyncClient(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(FAKE_API_KEY));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder.buildAsyncClient();
    }

    // Detected Languages

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageShowStatisticsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, options).byPage())
                .assertNext(response -> validateDetectLanguage(true, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each {@code DetectLanguageResult} input of a batch.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null).byPage())
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch with given country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguagesCountryHintRunner((inputs, countryHint) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, countryHint).byPage())
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHintWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null, options).byPage())
                .assertNext(response -> validateDetectLanguage(true, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageStringInputRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatch(inputs).byPage())
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a single DetectedLanguage is returned for a document to detectLanguage.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0, null);
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validatePrimaryLanguage(primaryLanguage, response))
            .verifyComplete();
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageInvalidCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.detectLanguage("Este es un documento  escrito en Español.", "en"))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.detectLanguage(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.detectLanguage("!@#%%"))
            .assertNext(response -> validatePrimaryLanguage(
                new DetectedLanguage("(Unknown)", "(Unknown)", 0.0, null), response))
            .verifyComplete();
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same ids.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageDuplicateIdRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, options))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.detectLanguage("Este es un documento  escrito en Español", ""))
            .assertNext(response -> validatePrimaryLanguage(
                new DetectedLanguage("Spanish", "es", 0.0, null), response))
            .verifyComplete();
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.detectLanguage("Este es un documento  escrito en Español", "none"))
            .assertNext(response -> validatePrimaryLanguage(
                new DetectedLanguage("Spanish", "es", 0.0, null), response))
            .verifyComplete();
    }

    // Entities
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.recognizeEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateCategorizedEntities(getCategorizedEntitiesList1(),
                response.stream().collect(Collectors.toList())))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.recognizeEntities(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.recognizeEntities("!@#%%"))
            .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(BATCH_ERROR_EXCEPTION_MESSAGE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, options).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(true, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, language).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, options).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(true, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeEntitiesTooManyDocumentsRunner(inputs -> {
            StepVerifier.create(client.recognizeEntitiesBatch(inputs))
                .verifyErrorSatisfies(ex -> {
                    HttpResponseException exception = (HttpResponseException) ex;
                    assertEquals(HttpResponseException.class, exception.getClass());
                    assertEquals(EXCEEDED_ALLOWED_DOCUMENTS_LIMITS_MESSAGE, exception.getMessage());
                    assertEquals(INVALID_DOCUMENT_BATCH, exception.getValue().toString());
                });
        });
    }

    // Linked Entities
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0);
        final LinkedEntity linkedEntity = new LinkedEntity("Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch)), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");

        StepVerifier.create(client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateLinkedEntity(linkedEntity, response.iterator().next()))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.recognizeLinkedEntities(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.recognizeLinkedEntities("!@#%%"))
            .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, options).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, language).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, options).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedEntitiesTooManyDocumentsRunner(inputs -> {
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs))
                .verifyErrorSatisfies(ex -> {
                    HttpResponseException exception = (HttpResponseException) ex;
                    assertEquals(HttpResponseException.class, exception.getClass());
                    assertEquals(EXCEEDED_ALLOWED_DOCUMENTS_LIMITS_MESSAGE, exception.getMessage());
                    assertEquals(INVALID_DOCUMENT_BATCH, exception.getValue().toString());
                });
        });
    }

    // Key Phrases
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.extractKeyPhrases("Bonjour tout le monde."))
            .assertNext(response -> assertEquals("monde", response.iterator().next()))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.extractKeyPhrases(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.extractKeyPhrases("!@#%%"))
            .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesDuplicateIdRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, options).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesStringInputRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, language).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, options).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesWarningRunner(
            input -> StepVerifier.create(client.extractKeyPhrases(input))
                .assertNext(keyPhrasesResult -> {
                    keyPhrasesResult.getWarnings().forEach(warning -> {
                        assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                        assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
                    });
                })
                .verifyComplete()
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesBatchWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesBatchWarningRunner(
            inputs -> StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null))
                .assertNext(keyPhrasesResult -> {
                    keyPhrasesResult.getKeyPhrases().getWarnings().forEach(warning -> {
                        assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                        assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
                    });
                })
                .expectNextCount(1)
                .verifyComplete()
        );
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment("", TextSentiment.NEGATIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                new SentenceSentiment("", TextSentiment.POSITIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0))
            )), null);

        StepVerifier
            .create(client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi."))
            .assertNext(response -> validateAnalyzedSentiment(expectedDocumentSentiment, response)).verifyComplete();
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.analyzeSentiment(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Test analyzing sentiment for a faulty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(
            TextSentiment.NEUTRAL,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment("", TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                new SentenceSentiment("", TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0))
            )), null);

        StepVerifier.create(client.analyzeSentiment("!@#%%"))
            .assertNext(response -> validateAnalyzedSentiment(expectedDocumentSentiment, response)).verifyComplete();
    }

    /**
     * Test analyzing sentiment for a duplicate ID list.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyseBatchSentimentDuplicateIdRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyseSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with language code.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyseSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, language).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyseBatchStringSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a batch of documents.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyseBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, options).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }
}
