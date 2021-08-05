// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Collections;

import static com.azure.ai.textanalytics.TestUtils.VALID_HTTPS_LOCALHOST;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.INVALID_DOCUMENT_BATCH_NPE_MESSAGE;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.INVALID_DOCUMENT_NPE_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Text Analytics asynchronous client's documents input
 */
public class DocumentInputAsyncTest {

    static TextAnalyticsAsyncClient client;

    @BeforeAll
    protected static void beforeTest() {
        client = new TextAnalyticsClientBuilder()
            .endpoint(VALID_HTTPS_LOCALHOST)
            .credential(new AzureKeyCredential("fakeKey"))
            .buildAsyncClient();
    }

    @AfterAll
    protected static void afterTest() {
        client = null;
    }

    // Detect language
    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#detectLanguage(String)}
     */
    @Test
    public void detectLanguageNullInput() {
        StepVerifier.create(client.detectLanguage(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#detectLanguage(String, String)}
     */
    @Test
    public void detectLanguageNullInputWithCountryHint() {
        StepVerifier.create(client.detectLanguage(null, "US"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInput() {
        StepVerifier.create(client.detectLanguageBatch(null, null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputList() {
        StepVerifier.create(client.detectLanguageBatch(Collections.emptyList(), null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithCountryHint() {
        StepVerifier.create(client.detectLanguageBatch(null, "US", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithCountryHint() {
        StepVerifier.create(client.detectLanguageBatch(Collections.emptyList(), "US", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithCountryHintAndRequestOptions() {
        StepVerifier.create(client.detectLanguageBatch(null, "US",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        StepVerifier.create(client.detectLanguageBatch(Collections.emptyList(), "US",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.detectLanguageBatchWithResponse(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.detectLanguageBatchWithResponse(Collections.emptyList(),
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    // Recognize Entity

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntities(String)}
     */
    @Test
    public void recognizeEntitiesNullInput() {
        StepVerifier.create(client.recognizeEntities(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntities(String, String)}
     */
    @Test
    public void recognizeEntitiesNullInputWithLanguageHint() {
        StepVerifier.create(client.recognizeEntities(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInput() {
        StepVerifier.create(client.recognizeEntitiesBatch(null, null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputList() {
        StepVerifier.create(client.recognizeEntitiesBatch(Collections.emptyList(), null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithLanguageHint() {
        StepVerifier.create(client.recognizeEntitiesBatch(null, "en", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithLanguageHint() {
        StepVerifier.create(client.recognizeEntitiesBatch(Collections.emptyList(), "en", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.recognizeEntitiesBatch(null, "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.recognizeEntitiesBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.recognizeEntitiesBatchWithResponse(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#recognizeEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.recognizeEntitiesBatchWithResponse(Collections.emptyList(),
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    // Recognize linked entity

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(String)}
     */
    @Test
    public void recognizeLinkedEntitiesNullInput() {
        StepVerifier.create(client.recognizeLinkedEntities(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(String, String)}
     */
    @Test
    public void recognizeLinkedEntitiesNullInputWithLanguageHint() {
        StepVerifier.create(client.recognizeLinkedEntities(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInput() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(null, null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputList() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(Collections.emptyList(), null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithLanguageHint() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(null, "en", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithLanguageHint() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(null, "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(Collections.emptyList(),
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    // Extract key phrase

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrases(String)}
     */
    @Test
    public void extractKeyPhrasesNullInput() {
        StepVerifier.create(client.extractKeyPhrases(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrases(String, String)}
     */
    @Test
    public void extractKeyPhrasesNullInputWithLanguageHint() {
        StepVerifier.create(client.extractKeyPhrases(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInput() {
        StepVerifier.create(client.extractKeyPhrasesBatch(null, null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputList() {
        StepVerifier.create(client.extractKeyPhrasesBatch(Collections.emptyList(), null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithLanguageHint() {
        StepVerifier.create(client.extractKeyPhrasesBatch(null, "en", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithLanguageHint() {
        StepVerifier.create(client.extractKeyPhrasesBatch(Collections.emptyList(), "en", null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.extractKeyPhrasesBatch(null, "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.extractKeyPhrasesBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(Collections.emptyList(),
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    // Sentiment

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentiment(String)}
     */
    @Test
    public void analyzeSentimentNullInput() {
        StepVerifier.create(client.analyzeSentiment(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null document is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentiment(String, String)}
     */
    @Test
    public void analyzeSentimentNullInputWithLanguageHint() {
        StepVerifier.create(client.analyzeSentiment(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentBatchNullInput() {
        StepVerifier.create(client.analyzeSentimentBatch(null, null, new TextAnalyticsRequestOptions()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentBatchEmptyInputList() {
        StepVerifier.create(client.analyzeSentimentBatch(Collections.emptyList(), null, new TextAnalyticsRequestOptions()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentBatchNullInputWithLanguageHint() {
        StepVerifier.create(client.analyzeSentimentBatch(null, "en", new TextAnalyticsRequestOptions()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentBatchEmptyInputListWithLanguageHint() {
        StepVerifier.create(client.analyzeSentimentBatch(Collections.emptyList(), "en", new TextAnalyticsRequestOptions()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentBatchNullInputWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.analyzeSentimentBatch(null, "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentBatchEmptyInputListWithLanguageHintAndRequestOptions() {
        StepVerifier.create(client.analyzeSentimentBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.analyzeSentimentBatchWithResponse(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyzeSentimentEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.analyzeSentimentBatchWithResponse(Collections.emptyList(),
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }
}
