// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.textanalytics.TestUtils.getCategorizedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageEnglish;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageSpanish;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static com.azure.ai.textanalytics.TestUtils.getLinkedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getUnknownDetectedLanguage;
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
        return getTextAnalyticsAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
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
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .assertNext(response ->
                    validateDetectLanguageResultCollectionWithResponse(true, getExpectedBatchDetectedLanguages(),
                        200, response)
                )
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
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, null))
                .assertNext(response ->
                    validateDetectLanguageResultCollectionWithResponse(false, getExpectedBatchDetectedLanguages(),
                        200, response))
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
            StepVerifier.create(client.detectLanguageBatch(inputs, countryHint, null))
                .assertNext(actualResults ->
                    validateDetectLanguageResultCollection(false, getExpectedBatchDetectedLanguages(), actualResults))
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
            StepVerifier.create(client.detectLanguageBatch(inputs, null, options))
                .assertNext(response -> validateDetectLanguageResultCollection(true, getExpectedBatchDetectedLanguages(), response))
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
            StepVerifier.create(client.detectLanguageBatch(inputs, null, null))
                .assertNext(response -> validateDetectLanguageResultCollection(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a single DetectedLanguage is returned for a document to detectLanguage.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectSingleTextLanguageRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageEnglish(), response))
                .verifyComplete());
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageInvalidCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageInvalidCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE))
                .verify());
    }

    /**
     * Verifies that TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE))
                .verify());
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .assertNext(response -> validatePrimaryLanguage(getUnknownDetectedLanguage(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same ids.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageDuplicateIdRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageEmptyCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageSpanish(), response))
                .verifyComplete());
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageNoneCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageSpanish(), response))
                .verifyComplete());
    }

    // Entities
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesForSingleTextInputRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .assertNext(response -> validateCategorizedEntities(getCategorizedEntitiesList1(),
                    response.stream().collect(Collectors.toList())))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE))
                .verify()
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
                .verifyComplete()
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .assertNext(resultCollection -> {
                    resultCollection.getValue().forEach(result -> {
                        assertTrue(result.isError());
                        final TextAnalyticsError error = result.getError();
                        TextAnalyticsErrorCode errorCode = error.getErrorCode();
                        assertTrue(TextAnalyticsErrorCode.fromString("invalidDocument").equals(errorCode));
                        assertTrue("Document text is empty.".equals(error.getMessage()));
                    });
                }).verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollectionWithResponse(false, getExpectedBatchCategorizedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateCategorizedEntitiesResultCollectionWithResponse(true, getExpectedBatchCategorizedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, options))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(true, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeEntitiesTooManyDocumentsRunner(inputs -> {
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, null))
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
        recognizeLinkedEntitiesForSingleTextInputRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .assertNext(response -> validateLinkedEntity(getLinkedEntitiesList1().get(0), response.iterator().next()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE))
                .verify());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateLinkedEntitiesResultCollectionWithResponse(false, getExpectedBatchLinkedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateLinkedEntitiesResultCollectionWithResponse(true, getExpectedBatchLinkedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, null))
                .assertNext(response -> validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, options))
                .assertNext(response -> validateLinkedEntitiesResultCollection(true, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedEntitiesTooManyDocumentsRunner(inputs -> {
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, null))
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
        extractKeyPhrasesForSingleTextInputRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .assertNext(response -> assertEquals("monde", response.iterator().next()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE))
                .verify());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesDuplicateIdRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollectionWithResponse(false, getExpectedBatchKeyPhrases(), 200, response))
                .verifyComplete());

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, options))
                .assertNext(response -> validateExtractKeyPhrasesResultCollectionWithResponse(true, getExpectedBatchKeyPhrases(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesStringInputRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, language, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, options))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(true, getExpectedBatchKeyPhrases(), response))
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
            inputs -> StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .assertNext(response -> response.getValue().forEach(keyPhrasesResult ->
                    keyPhrasesResult.getKeyPhrases().getWarnings().forEach(warning -> {
                        assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                        assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
                    })
                ))
                .verifyComplete()
        );
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
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
    public void analyzeSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.analyzeSentiment(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE))
            .verify();
    }

    /**
     * Test analyzing sentiment for a faulty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
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
    public void analyzeSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentDuplicateIdRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, null))
                .assertNext(response -> validateSentimentResultCollection(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with language code.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, language, null))
                .assertNext(response -> validateSentimentResultCollection(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchStringSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options))
                .assertNext(response -> validateSentimentResultCollection(true, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a batch of documents.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, null))
                .assertNext(response -> validateSentimentResultCollectionWithResponse(false, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete());
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options))
                .assertNext(response -> validateSentimentResultCollectionWithResponse(true, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete());
    }
}
