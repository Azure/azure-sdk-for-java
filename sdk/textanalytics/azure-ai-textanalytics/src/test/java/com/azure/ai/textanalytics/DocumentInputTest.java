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
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#detectLanguage(String)}
     */
    @Test
    public void detectLanguagesNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguage(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#detectLanguage(String, String)}
     */
    @Test
    public void detectLanguagesNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguage(null, "US"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable)}
     */
    @Test
    public void detectLanguagesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatch(null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatch(null, "US"));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatch(null, "US", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.detectLanguageBatch(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an illegal argument exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable)}
     */
    @Test
    public void detectLanguageEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatch(Collections.emptyList()));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String)}
     */
    @Test
    public void detectLanguageEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatch(Collections.emptyList(), "US"));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguageEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatch(Collections.emptyList(), "US", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of {@link DetectLanguageInput} is given for
     * {@link TextAnalyticsClient#detectLanguageBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void detectLanguageEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.detectLanguageBatch(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Recognize Entity

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeEntities(String)}
     */
    @Test
    public void recognizeEntitiesNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntities(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeEntities(String, String)}
     */
    @Test
    public void recognizeEntitiesNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntities(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeEntitiesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatch(null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatch(null, "en"));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatch(null, "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeEntitiesBatch(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an illegal argument exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeEntitiesEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatch(Collections.emptyList()));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeEntitiesEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatch(Collections.emptyList(), "en"));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatch(Collections.emptyList(), "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of {@link TextDocumentInput} is given for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeEntitiesEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeEntitiesBatch(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Recognize linked entity

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntities(String)}
     */
    @Test
    public void recognizeLinkedEntitiesNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntities(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntities(String, String)}
     */
    @Test
    public void recognizeLinkedEntitiesNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntities(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatch(null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatch(null, "en"));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatch(null, "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.recognizeLinkedEntitiesBatch(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an illegal argument exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeLinkedEntitiesEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatch(Collections.emptyList()));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeLinkedEntitiesEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en"));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of {@link TextDocumentInput} is given for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void recognizeLinkedEntitiesEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.recognizeLinkedEntitiesBatch(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Extract key phrase

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#extractKeyPhrases(String)}
     */
    @Test
    public void extractKeyPhrasesNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrases(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#extractKeyPhrases(String, String)}
     */
    @Test
    public void extractKeyPhrasesNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrases(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatch(null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatch(null, "en"));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatch(null, "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.extractKeyPhrasesBatch(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an illegal argument exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable)}
     */
    @Test
    public void extractKeyPhrasesEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatch(Collections.emptyList()));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String)}
     */
    @Test
    public void extractKeyPhrasesEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatch(Collections.emptyList(), "en"));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatch(Collections.emptyList(), "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of {@link TextDocumentInput} is given for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void extractKeyPhrasesEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.extractKeyPhrasesBatch(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    // Sentiment

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#analyzeSentiment(String)}
     */
    @Test
    public void analyseSentimentNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentiment(null));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#analyzeSentiment(String, String)}
     */
    @Test
    public void analyseSentimentNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentiment(null, "en"));
        assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null document is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable)}
     */
    @Test
    public void analyseSentimentBatchNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatch(null));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithCountryHint() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatch(null, "en"));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatch(null, "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when null documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithMaxOverload() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.analyzeSentimentBatch(null, new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that an illegal argument exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable)}
     */
    @Test
    public void analyseSentimentEmptyInputList() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatch(Collections.emptyList()));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String)}
     */
    @Test
    public void analyseSentimentEmptyInputListWithCountryHint() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatch(Collections.emptyList(), "en"));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of documents is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentEmptyInputListWithCountryHintAndRequestOptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatch(Collections.emptyList(), "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true)));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }

    /**
     * Verifies that a null pointer exception is thrown when an empty list of {@link TextDocumentInput} is given for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    @Test
    public void analyseSentimentEmptyInputListWithMaxOverload() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.analyzeSentimentBatch(
                Collections.emptyList(), new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE));
        assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
    }
}
