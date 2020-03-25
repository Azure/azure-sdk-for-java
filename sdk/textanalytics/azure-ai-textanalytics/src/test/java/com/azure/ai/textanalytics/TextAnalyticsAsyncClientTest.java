// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
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

import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static com.azure.ai.textanalytics.TestUtils.getExpectedCategorizedEntities;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0);
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
                new DetectedLanguage("(Unknown)", "(Unknown)", 0.0), response))
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
                new DetectedLanguage("Spanish", "es", 0.0), response))
            .verifyComplete();
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @Test
    public void detectLanguageNoneCountryHint() {
        StepVerifier.create(client.detectLanguage("Este es un documento  escrito en Español", "none"))
            .assertNext(response -> validatePrimaryLanguage(
                new DetectedLanguage("Spanish", "es", 0.0), response))
            .verifyComplete();
    }

    // Entities
    @Test
    public void recognizeEntitiesForTextInput() {
        StepVerifier.create(client.recognizeEntities("I had a wonderful trip to Seattle last week.").byPage())
            .assertNext(response -> validateCategorizedEntities(getExpectedCategorizedEntities(), response))
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
            .expectNextCount(0)
            .verifyComplete();
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

    // Linked Entities
    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        final LinkedEntity linkedEntity = new LinkedEntity("Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch)), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");

        StepVerifier.create(client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateLinkedEntity(linkedEntity, response))
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
            .expectNextCount(0)
            .verifyComplete();
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

    // Personally Identifiable Information Entities
    @Test
    public void recognizePiiEntitiesForTextInput() {
        PiiEntity piiEntity0 = new PiiEntity("Microsoft", EntityCategory.ORGANIZATION, null, 0, 9, 1.0);
        PiiEntity piiEntity = new PiiEntity("859-98-0987", EntityCategory.fromString("U.S. Social Security Number (SSN)"), null, 28, 11, 0.65);
        StepVerifier.create(client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's."))
            .assertNext(response -> validatePiiEntity(piiEntity0, response))
            .assertNext(response -> validatePiiEntity(piiEntity, response))
            .verifyComplete();
    }

    @Test
    public void recognizePiiEntitiesForEmptyText() {
        StepVerifier.create(client.recognizePiiEntities(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizePiiEntitiesForFaultyText() {
        StepVerifier.create(client.recognizePiiEntities("!@#%%"))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    public void recognizePiiEntitiesForBatchInput() {
        recognizeBatchPiiRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, null).byPage())
                .assertNext(response -> validatePiiEntityWithPagedResponse(false, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForBatchInputShowStatistics() {
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, options).byPage())
                .assertNext(response -> validatePiiEntityWithPagedResponse(true, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForBatchStringInput() {
        recognizePiiStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs).byPage())
                .assertNext(response -> validatePiiEntityWithPagedResponse(false, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForListLanguageHint() {
        recognizePiiLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, language, null).byPage())
                .assertNext(response -> validatePiiEntityWithPagedResponse(false, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForListStringWithOptions() {
        recognizeStringBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, null, options).byPage())
                .assertNext(response -> validatePiiEntityWithPagedResponse(true, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    // Key Phrases
    @Test
    public void extractKeyPhrasesForTextInput() {
        StepVerifier.create(client.extractKeyPhrases("Bonjour tout le monde."))
            .assertNext(response -> assertEquals("monde", response))
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
            .expectNextCount(0)
            .verifyComplete();
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

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @Test
    public void analyseSentimentForTextInput() {
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment(TextSentiment.NEGATIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 31, 0),
                new SentenceSentiment(TextSentiment.POSITIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 35, 32)
            )));

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
                new SentenceSentiment(TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0), 1, 0),
                new SentenceSentiment(TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0), 4, 1)
            )));

        StepVerifier.create(client.analyzeSentiment("!@#%%"))
            .assertNext(response -> validateAnalyzedSentiment(expectedDocumentSentiment, response)).verifyComplete();
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
