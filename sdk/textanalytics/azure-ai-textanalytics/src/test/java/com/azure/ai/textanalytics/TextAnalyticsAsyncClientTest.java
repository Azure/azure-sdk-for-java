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
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

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

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildAsyncClient());
    }

    // Detected Languages

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @Test
    public void detectLanguagesBatchInputShowStatistics() {
        detectLanguageShowStatisticsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, options).byPage())
                .assertNext(response -> validateDetectLanguage(true, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each {@code DetectLanguageResult} input of a batch.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null).byPage())
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch with given country hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, countryHint).byPage())
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch with request options.
     */
    @Test
    public void detectLanguagesBatchListCountryHintWithOptions() {
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null, options).byPage())
                .assertNext(response -> validateDetectLanguage(true, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatch(inputs).byPage())
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a single DetectedLanguage is returned for a document to detectLanguage.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0, null);
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validatePrimaryLanguage(primaryLanguage, response))
            .verifyComplete();
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        StepVerifier.create(client.detectLanguage("Este es un documento  escrito en Español.", "en"))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that TextAnalyticsException is thrown for an empty document.
     */
    @Test
    public void detectLanguageEmptyText() {
        StepVerifier.create(client.detectLanguage(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @Test
    public void detectLanguageFaultyText() {
        StepVerifier.create(client.detectLanguage("!@#%%"))
            .assertNext(response -> validatePrimaryLanguage(
                new DetectedLanguage("(Unknown)", "(Unknown)", 0.0, null), response))
            .verifyComplete();
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same ids.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, options))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @Test
    public void detectLanguageEmptyCountryHint() {
        StepVerifier.create(client.detectLanguage("Este es un documento  escrito en Español", ""))
            .assertNext(response -> validatePrimaryLanguage(
                new DetectedLanguage("Spanish", "es", 0.0, null), response))
            .verifyComplete();
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @Test
    public void detectLanguageNoneCountryHint() {
        StepVerifier.create(client.detectLanguage("Este es un documento  escrito en Español", "none"))
            .assertNext(response -> validatePrimaryLanguage(
                new DetectedLanguage("Spanish", "es", 0.0, null), response))
            .verifyComplete();
    }

    // Entities
    @Test
    public void recognizeEntitiesForTextInput() {
        StepVerifier.create(client.recognizeEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateCategorizedEntities(getCategorizedEntitiesList1(),
                response.stream().collect(Collectors.toList())))
            .verifyComplete();
    }

    @Test
    public void recognizeEntitiesForEmptyText() {
        StepVerifier.create(client.recognizeEntities(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizeEntitiesForFaultyText() {
        StepVerifier.create(client.recognizeEntities("!@#%%"))
            .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
            .verifyComplete();
    }

    @Test
    public void recognizeEntitiesDuplicateIdInput() {
        recognizeCategorizedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @Test
    public void recognizeEntitiesBatchInputSingleError() {
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(BATCH_ERROR_EXCEPTION_MESSAGE)));
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchCategorizedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, options).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(true, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, language).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForListWithOptions() {
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, options).byPage())
                .assertNext(response -> validateCategorizedEntitiesWithPagedResponse(true, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesTooManyDocuments() {
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
    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0);
        final LinkedEntity linkedEntity = new LinkedEntity("Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch)), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");

        StepVerifier.create(client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateLinkedEntity(linkedEntity, response.iterator().next()))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesForEmptyText() {
        StepVerifier.create(client.recognizeLinkedEntities(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizeLinkedEntitiesForFaultyText() {
        StepVerifier.create(client.recognizeLinkedEntities("!@#%%"))
            .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesDuplicateIdInput() {
        recognizeBatchLinkedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInput() {
        recognizeBatchLinkedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, options).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, language).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForListStringWithOptions() {
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, options).byPage())
                .assertNext(response -> validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesTooManyDocuments() {
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
    @Test
    public void extractKeyPhrasesForTextInput() {
        StepVerifier.create(client.extractKeyPhrases("Bonjour tout le monde."))
            .assertNext(response -> assertEquals("monde", response.iterator().next()))
            .verifyComplete();
    }

    @Test
    public void extractKeyPhrasesForEmptyText() {
        StepVerifier.create(client.extractKeyPhrases(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void extractKeyPhrasesForFaultyText() {
        StepVerifier.create(client.extractKeyPhrases("!@#%%"))
            .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
            .verifyComplete();
    }

    @Test
    public void extractKeyPhrasesDuplicateIdInput() {
        extractBatchKeyPhrasesDuplicateIdRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @Test
    public void extractKeyPhrasesForBatchInput() {
        extractBatchKeyPhrasesRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());

    }

    @Test
    public void extractKeyPhrasesForBatchInputShowStatistics() {
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, options).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesForBatchStringInput() {
        extractKeyPhrasesStringInputRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesForListLanguageHint() {
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, language).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesForListStringWithOptions() {
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, options).byPage())
                .assertNext(response -> validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesWarning() {
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

    @Test
    public void extractKeyPhrasesBatchWarning() {
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
    @Test
    public void analyseSentimentForTextInput() {
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
    @Test
    public void analyseSentimentForEmptyText() {
        StepVerifier.create(client.analyzeSentiment(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Test analyzing sentiment for a faulty document.
     */
    @Test
    public void analyseSentimentForFaultyText() {
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
    @Test
    public void analyseSentimentDuplicateIdInput() {
        analyseBatchSentimentDuplicateIdRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with language code.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, language).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with request options.
     */
    @Test
    public void analyseSentimentForListStringWithOptions() {
        analyseBatchStringSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a batch of documents.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, options).byPage())
                .assertNext(response -> validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }
}
