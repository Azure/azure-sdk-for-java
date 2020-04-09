// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildClient());
    }

    // Detect language

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with options.
     */
    @Test
    public void detectLanguagesBatchInputShowStatistics() {
        detectLanguageShowStatisticsRunner((inputs, options) -> validateDetectLanguage(true,
            getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, options, Context.NONE).streamByPage().findFirst().get()));
    }

    /**
     * Test Detect batch of documents languages.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) -> validateDetectLanguage(false,
            getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs, null, Context.NONE).streamByPage().findFirst().get()));
    }

    /**
     * Test detect batch languages for a list of string input with country hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, countryHint).streamByPage().findFirst().get()));
    }

    /**
     * Test detect batch languages for a list of string input with request options
     */
    @Test
    public void detectLanguagesBatchListCountryHintWithOptions() {
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) -> validateDetectLanguage(true,
            getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, null, options).streamByPage().findFirst().get()));
    }

    /**
     * Test detect batch languages for a list of string input.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs).streamByPage().findFirst().get()));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a document to detect language.
     */
    @Test
    public void detectSingleTextLanguage() {
        validatePrimaryLanguage(new DetectedLanguage("English", "en", 0.0),
            client.detectLanguage("This is a test English Text"));
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @Test
    public void detectLanguageEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.detectLanguage(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @Test
    public void detectLanguageFaultyText() {
        DetectedLanguage primaryLanguage = new DetectedLanguage("(Unknown)", "(Unknown)", 0.0);
        validatePrimaryLanguage(client.detectLanguage("!@#%%"), primaryLanguage);
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        Exception exception = assertThrows(TextAnalyticsException.class, () ->
            client.detectLanguage("Este es un documento  escrito en Español.", "en"));
        assertTrue(exception.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @Test
    public void detectLanguageEmptyCountryHint() {
        validatePrimaryLanguage(new DetectedLanguage("Spanish", "es", 0.0),
            client.detectLanguage("Este es un documento  escrito en Español", ""));
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @Test
    public void detectLanguageNoneCountryHint() {
        validatePrimaryLanguage(new DetectedLanguage("Spanish", "es", 0.0),
            client.detectLanguage("Este es un documento  escrito en Español", "none"));
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same IDs.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.detectLanguageBatch(inputs, options, Context.NONE).stream().findFirst().get());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    // Recognize Entity

    @Test
    public void recognizeEntitiesForTextInput() {
        final CategorizedEntity categorizedEntity1 = new CategorizedEntity("Seattle", EntityCategory.LOCATION, "GPE", 26, 7, 0.0);
        final CategorizedEntity categorizedEntity2 = new CategorizedEntity("last week", EntityCategory.DATE_TIME, "DateRange", 34, 9, 0.0);

        final List<CategorizedEntity> entities = client.recognizeEntities("I had a wonderful trip to Seattle last week.").stream().collect(Collectors.toList());
        validateCategorizedEntity(categorizedEntity1, entities.get(0));
        validateCategorizedEntity(categorizedEntity2, entities.get(1));
    }

    @Test
    public void recognizeEntitiesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizeEntities("").iterator().hasNext());
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizeEntitiesForFaultyText() {
        assertFalse(client.recognizeEntities("!@#%%").iterator().hasNext());
    }

    @Test
    public void recognizeEntitiesBatchInputSingleError() {
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) -> {
            TextAnalyticsPagedIterable<RecognizeCategorizedEntitiesResult> response = client.recognizeEntitiesBatch(inputs, null, Context.NONE);
            response.forEach(recognizeEntitiesResult -> {
                Exception exception = assertThrows(TextAnalyticsException.class, () -> recognizeEntitiesResult.getEntities());
                assertTrue(exception.getMessage().equals(BATCH_ERROR_EXCEPTION_MESSAGE));
            });
        });
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchCategorizedEntityRunner((inputs) ->
            client.recognizeEntitiesBatch(inputs, null, Context.NONE).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeEntitiesBatch(inputs, options, Context.NONE).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeCategorizedEntityStringInputRunner((inputs) -> client.recognizeEntitiesBatch(inputs).iterableByPage()
            .forEach(pagedResponse ->
                validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            client.recognizeEntitiesBatch(inputs, language).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeEntitiesForListWithOptions() {
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeEntitiesBatch(inputs, null, options).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    // Recognize linked entity

    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        final LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        final LinkedEntity linkedEntity1 = new LinkedEntity("Seattle",
            new IterableStream<>(Collections.singletonList(linkedEntityMatch1)),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");
        final List<LinkedEntity> linkedEntities = client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week.")
            .stream().collect(Collectors.toList());
        validateLinkedEntity(linkedEntity1, linkedEntities.get(0));
    }

    @Test
    public void recognizeLinkedEntitiesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizeLinkedEntities("").iterator().hasNext());
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizeLinkedEntitiesForFaultyText() {
        assertFalse(client.recognizeLinkedEntities("!@#%%").iterator().hasNext());
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInput() {
        recognizeBatchLinkedEntityRunner((inputs) ->
            client.recognizeLinkedEntitiesBatch(inputs, null, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeLinkedEntitiesBatch(inputs, options, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) ->
            client.recognizeLinkedEntitiesBatch(inputs).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            client.recognizeLinkedEntitiesBatch(inputs, language).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @Test
    public void recognizeLinkedEntitiesForListStringWithOptions() {
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeLinkedEntitiesBatch(inputs, null, options).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    // Extract key phrase

    @Test
    public void extractKeyPhrasesForTextInput() {
        assertEquals("monde", client.extractKeyPhrases("Bonjour tout le monde.").iterator().next());
    }

    @Test
    public void extractKeyPhrasesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.extractKeyPhrases("").iterator().hasNext());
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void extractKeyPhrasesForFaultyText() {
        assertFalse(client.extractKeyPhrases("!@#%%").iterator().hasNext());
    }

    @Test
    public void extractKeyPhrasesForBatchInput() {
        extractBatchKeyPhrasesRunner((inputs) ->
            client.extractKeyPhrasesBatch(inputs, null, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @Test
    public void extractKeyPhrasesForBatchInputShowStatistics() {
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            client.extractKeyPhrasesBatch(inputs, options, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @Test
    public void extractKeyPhrasesForBatchStringInput() {
        extractKeyPhrasesStringInputRunner((inputs) ->
            client.extractKeyPhrasesBatch(inputs).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @Test
    public void extractKeyPhrasesForListLanguageHint() {
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            client.extractKeyPhrasesBatch(inputs, language).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @Test
    public void extractKeyPhrasesForListStringWithOptions() {
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            client.extractKeyPhrasesBatch(inputs, null, options).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @Test
    public void analyseSentimentForTextInput() {
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(
            TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment(TextSentiment.NEGATIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 31, 0),
                new SentenceSentiment(TextSentiment.POSITIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 35, 32)
            )));
        DocumentSentiment analyzeSentimentResult =
            client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi.");

        validateAnalyzedSentiment(expectedDocumentSentiment, analyzeSentimentResult);
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @Test
    public void analyseSentimentForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.analyzeSentiment(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Test analyzing sentiment for a faulty document.
     */
    @Test
    public void analyseSentimentForFaultyText() {
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(TextSentiment.NEUTRAL,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment(TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0), 1, 0),
                new SentenceSentiment(TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0), 4, 1)
            )));

        DocumentSentiment analyzeSentimentResult = client.analyzeSentiment("!@#%%");

        validateAnalyzedSentiment(expectedDocumentSentiment, analyzeSentimentResult);
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            client.analyzeSentimentBatch(inputs).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Test analyzing sentiment for a list of string input with language code.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            client.analyzeSentimentBatch(inputs, language).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @Test
    public void analyseSentimentForListStringWithOptions() {
        analyseBatchStringSentimentShowStatsRunner((inputs, options) ->
            client.analyzeSentimentBatch(inputs, null, options).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Test analyzing sentiment for batch of documents.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs ->
            client.analyzeSentimentBatch(inputs, null, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            client.analyzeSentimentBatch(inputs, options, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), pagedResponse)));
    }
}
