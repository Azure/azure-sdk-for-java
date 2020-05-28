// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.azure.ai.textanalytics.TestUtils.VALID_HTTPS_LOCALHOST;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.INVALID_DOCUMENT_BATCH_NPE_MESSAGE;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.INVALID_DOCUMENT_NPE_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Text Analytics client's documents input
 */
public class DocumentInputTest {
    static TextAnalyticsClient client;

    @BeforeAll
    protected static void beforeTest() {
        client = new TextAnalyticsClientBuilder()
            .endpoint(VALID_HTTPS_LOCALHOST)
            .credential(new AzureKeyCredential("fakeKey"))
            .buildClient();
    }

    @AfterAll
    protected static void afterTest() {
        client = null;
    }

    // Detect language
    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#detectLanguage(String)}
     */
    @Test
    public void detectLanguageNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguage(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#detectLanguage(String, String)}
     */
    @Test
    public void detectLanguageNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguage(null, "US"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatch(null, null, null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatch(Collections.emptyList(), null, null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatch(null, "US", null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatch(Collections.emptyList(), "US", null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatch(null, "US",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatch(Collections.emptyList(), "US",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatchWithResponse(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true),
                Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of {@link DetectLanguageInput} is
     * given for {@link TextAnalyticsClient#detectLanguageBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatchWithResponse(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Recognize Entity

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeEntities(String)}
     */
    @Test
    public void recognizeEntitiesNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntities(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeEntities(String, String)}
     */
    @Test
    public void recognizeEntitiesNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntities(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatch(null, null, null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatch(Collections.emptyList(), null, null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatch(null, "en", null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatch(Collections.emptyList(), "en", null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatch(null, "en",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatch(Collections.emptyList(), "en",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatchWithResponse(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true),
                Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsClient#recognizeEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatchWithResponse(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Recognize linked entity

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntities(String)}
     */
    @Test
    public void recognizeLinkedEntitiesNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntities(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntities(String, String)}
     */
    @Test
    public void recognizeLinkedEntitiesNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntities(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatch(null, null, null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatch(Collections.emptyList(), null, null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatch(null, "en", null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en", null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatch(null, "en",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatchWithResponse(null,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatchWithResponse(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Extract key phrase

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#extractKeyPhrases(String)}
     */
    @Test
    public void extractKeyPhrasesNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrases(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#extractKeyPhrases(String, String)}
     */
    @Test
    public void extractKeyPhrasesNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrases(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatch(null, null, null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatch(Collections.emptyList(), null, null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatch(null, "en", null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatch(Collections.emptyList(), "en", null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatch(null, "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatch(Collections.emptyList(), "en",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatchWithResponse(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true),
                Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatchWithResponse(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Sentiment

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#analyzeSentiment(String)}
     */
    @Test
    public void analyseSentimentNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentiment(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsClient#analyzeSentiment(String, String)}
     */
    @Test
    public void analyseSentimentNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentiment(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatch(null, null, null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatch(Collections.emptyList(), null, null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatch(null, "en", null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatch(Collections.emptyList(), "en", null));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatch(null, "en",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatch(Collections.emptyList(), "en",
                new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatchWithResponse(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void analyseSentimentEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatchWithResponse(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }
}
