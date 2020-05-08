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
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable)}
     */
    @Test
    public void detectLanguagesBatchNullInput() {
        StepVerifier.create(client.detectLanguageBatch(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputList() {
        StepVerifier.create(client.detectLanguageBatch(Collections.emptyList()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithCountryHint() {
        StepVerifier.create(client.detectLanguageBatch(null, "US"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithCountryHint() {
        StepVerifier.create(client.detectLanguageBatch(Collections.emptyList(), "US"))
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
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.detectLanguageBatch(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void detectLanguagesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.detectLanguageBatch(Collections.emptyList(),
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
    public void recognizeEntitiesNullInputWithCountryHint() {
        StepVerifier.create(client.recognizeEntities(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeEntitiesBatchNullInput() {
        StepVerifier.create(client.recognizeEntitiesBatch(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputList() {
        StepVerifier.create(client.recognizeEntitiesBatch(Collections.emptyList()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithCountryHint() {
        StepVerifier.create(client.recognizeEntitiesBatch(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithCountryHint() {
        StepVerifier.create(client.recognizeEntitiesBatch(Collections.emptyList(), "en"))
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
    public void recognizeEntitiesBatchNullInputWithCountryHintAndRequestOptions() {
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
    public void recognizeEntitiesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        StepVerifier.create(client.recognizeEntitiesBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.recognizeEntitiesBatch(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeEntitiesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.recognizeEntitiesBatch(Collections.emptyList(),
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
    public void recognizeLinkedEntitiesNullInputWithCountryHint() {
        StepVerifier.create(client.recognizeLinkedEntities(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInput() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputList() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(Collections.emptyList()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithCountryHint() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithCountryHint() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en"))
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
    public void recognizeLinkedEntitiesBatchNullInputWithCountryHintAndRequestOptions() {
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
    public void recognizeLinkedEntitiesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void recognizeLinkedEntitiesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.recognizeLinkedEntitiesBatch(Collections.emptyList(),
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
    public void extractKeyPhrasesNullInputWithCountryHint() {
        StepVerifier.create(client.extractKeyPhrases(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInput() {
        StepVerifier.create(client.extractKeyPhrasesBatch(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputList() {
        StepVerifier.create(client.extractKeyPhrasesBatch(Collections.emptyList()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithCountryHint() {
        StepVerifier.create(client.extractKeyPhrasesBatch(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithCountryHint() {
        StepVerifier.create(client.extractKeyPhrasesBatch(Collections.emptyList(), "en"))
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
    public void extractKeyPhrasesBatchNullInputWithCountryHintAndRequestOptions() {
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
    public void extractKeyPhrasesBatchEmptyInputListWithCountryHintAndRequestOptions() {
        StepVerifier.create(client.extractKeyPhrasesBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.extractKeyPhrasesBatch(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void extractKeyPhrasesBatchEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.extractKeyPhrasesBatch(Collections.emptyList(),
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
    public void analyseSentimentNullInput() {
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
    public void analyseSentimentNullInputWithCountryHint() {
        StepVerifier.create(client.analyzeSentiment(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable)}
     */
    @Test
    public void analyseSentimentBatchNullInput() {
        StepVerifier.create(client.analyzeSentimentBatch(null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable)}
     */
    @Test
    public void analyseSentimentBatchEmptyInputList() {
        StepVerifier.create(client.analyzeSentimentBatch(Collections.emptyList()))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithCountryHint() {
        StepVerifier.create(client.analyzeSentimentBatch(null, "en"))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String)}
     */
    @Test
    public void analyseSentimentBatchEmptyInputListWithCountryHint() {
        StepVerifier.create(client.analyzeSentimentBatch(Collections.emptyList(), "en"))
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
    public void analyseSentimentBatchNullInputWithCountryHintAndRequestOptions() {
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
    public void analyseSentimentBatchEmptyInputListWithCountryHintAndRequestOptions() {
        StepVerifier.create(client.analyzeSentimentBatch(Collections.emptyList(), "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when null documents is given for
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentBatchNullInputWithMaxOverload() {
        StepVerifier.create(client.analyzeSentimentBatch(null,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(NullPointerException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_BATCH_NPE_MESSAGE.equals(exception.getMessage()));
            });
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when an empty list of {@link TextDocumentInput} is
     * given for {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, TextAnalyticsRequestOptions)}
     */
    @Test
    public void analyseSentimentEmptyInputListWithMaxOverload() {
        StepVerifier.create(client.analyzeSentimentBatch(Collections.emptyList(),
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalArgumentException.class, exception.getClass());
                assertTrue(INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE.equals(exception.getMessage()));
            });
    }
}
