// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.DocumentSentimentLabel;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentenceSentimentLabel;
import com.azure.ai.textanalytics.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
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
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void detectLanguagesBatchInputShowStatistics() {
        detectLanguageShowStatisticsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .assertNext(response -> validateDetectLanguage(true, getExpectedBatchDetectedLanguages(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Test to detect language for each {@code DetectLanguageResult} input of a batch.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, null))
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch, with given country hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, countryHint, null))
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatch(inputs))
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a single DetectedLanguage is returned for a text input to detectLanguage.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0);
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validatePrimaryLanguage(primaryLanguage, response))
            .verifyComplete();
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a text input with invalid country hint.
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        StepVerifier.create(client.detectLanguage("Este es un document escrito en Español.", "en"))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that TextAnalyticsException is thrown for a empty text input.
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
        DetectedLanguage primaryLanguage = new DetectedLanguage("(Unknown)", "(Unknown)", 0.0);
        StepVerifier.create(client.detectLanguage("!@#%%"))
            .assertNext(response -> validatePrimaryLanguage(primaryLanguage, response))
            .verifyComplete();
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same ids.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    // Entities
    @Test
    public void recognizeEntitiesForTextInput() {
        final CategorizedEntity categorizedEntity1 = new CategorizedEntity("Seattle", "Location", null, 26, 7, 0.0);
        final CategorizedEntity categorizedEntity2 = new CategorizedEntity("last week", "DateTime", "DateRange", 34, 9, 0.0);
        StepVerifier.create(client.recognizeEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateCategorizedEntity(categorizedEntity1, response))
            .assertNext(response -> validateCategorizedEntity(categorizedEntity2, response))
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
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(BATCH_ERROR_EXCEPTION_MESSAGE)));
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchCategorizedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateCategorizedEntity(true, getExpectedBatchCategorizedEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs))
                .assertNext(response -> validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeCatgeorizedEntitiesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    // Linked Entities
    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        final LinkedEntity linkedEntity = new LinkedEntity("Seattle", Collections.singletonList(linkedEntityMatch), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");

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
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateLinkedEntity(false, getExpectedBatchLinkedEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateLinkedEntity(true, getExpectedBatchLinkedEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs))
                .assertNext(response -> validateLinkedEntity(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }
    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateLinkedEntity(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    // Personally Identifiable Information Entities
    @Test
    public void recognizePiiEntitiesForTextInput() {
        PiiEntity piiEntity = new PiiEntity("859-98-0987", "U.S. Social Security Number (SSN)", "", 28, 11, 0.0);
        StepVerifier.create(client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's."))
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
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validatePiiEntity(false, getExpectedBatchPiiEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForBatchInputShowStatistics() {
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validatePiiEntity(true, getExpectedBatchPiiEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForBatchStringInput() {
        recognizePiiStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs))
                .assertNext(response -> validatePiiEntity(false, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForListLanguageHint() {
        recognizePiiLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, language, null))
                .assertNext(response -> validatePiiEntity(false, getExpectedBatchPiiEntities(), response))
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
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .assertNext(response -> validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), response.getValue()))
                .verifyComplete());

    }

    @Test
    public void extractKeyPhrasesForBatchInputShowStatistics() {
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, options))
                .assertNext(response -> validateExtractKeyPhrase(true, getExpectedBatchKeyPhrases(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesForBatchStringInput() {
        extractKeyPhrasesStringInputRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs))
                .assertNext(response -> validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesForListLanguageHint() {
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, language, null))
                .assertNext(response -> validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @Test
    public void analyseSentimentForTextInput() {
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(DocumentSentimentLabel.MIXED,
            new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0),
            Arrays.asList(
                new SentenceSentiment(SentenceSentimentLabel.NEGATIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 31, 0),
                new SentenceSentiment(SentenceSentimentLabel.POSITIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 35, 32)
            ));

        StepVerifier
            .create(client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi."))
            .assertNext(response -> validateAnalyzedSentiment(expectedDocumentSentiment, response)).verifyComplete();
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a empty text input.
     */
    @Test
    public void analyseSentimentForEmptyText() {
        StepVerifier.create(client.analyzeSentiment(""))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && throwable.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Test analyzing sentiment for a faulty input text.
     */
    @Test
    public void analyseSentimentForFaultyText() {
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(
            DocumentSentimentLabel.NEUTRAL,
            new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0),
            Arrays.asList(
                new SentenceSentiment(SentenceSentimentLabel.NEUTRAL, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 1, 0),
                new SentenceSentiment(SentenceSentimentLabel.NEUTRAL, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 4, 1)
            ));

        StepVerifier.create(client.analyzeSentiment("!@#%%"))
            .assertNext(response -> validateAnalyzedSentiment(expectedDocumentSentiment, response)).verifyComplete();
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs))
                .assertNext(response -> validateSentiment(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with language hint.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, language, null))
                .assertNext(response -> validateSentiment(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for batch input.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, null))
                .assertNext(response -> validateSentiment(false, getExpectedBatchTextSentiment(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options))
                .assertNext(response -> validateSentiment(true, getExpectedBatchTextSentiment(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Test client builder with valid API key
     */
    @Test
    public void validKey() {
        // Arrange
        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(getApiKey())).buildAsyncClient();

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response ->
                validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0), response))
            .verifyComplete();
    }

    /**
     * Test client builder with invalid API key
     */
    @Test
    public void invalidKey() {
        // Arrange
        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(INVALID_KEY)).buildAsyncClient();

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .verifyError(HttpResponseException.class);
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @Test
    public void updateToInvalidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential(getApiKey());
        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to invalid key
        credential.updateCredential(INVALID_KEY);

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .verifyError(HttpResponseException.class);
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @Test
    public void updateToValidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential(INVALID_KEY);

        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to valid key
        credential.updateCredential(getApiKey());

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response ->
                validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0), response))
            .verifyComplete();
    }

    /**
     * Test for null service version, which would take the default service version by default
     */
    @Test
    public void nullServiceVersion() {
        // Arrange
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .apiKey(new TextAnalyticsApiKeyCredential(getApiKey()))
            .retryPolicy(new RetryPolicy())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(null);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        // Action and Assert
        StepVerifier.create(clientBuilder.buildAsyncClient().detectLanguage("This is a test English Text"))
            .assertNext(response ->
                validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0), response))
            .verifyComplete();
    }

    /**
     * Test for default pipeline in client builder
     */
    @Test
    public void defaultPipeline() {
        // Arrange
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .apiKey(new TextAnalyticsApiKeyCredential(getApiKey()))
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        // Action and Assert
        StepVerifier.create(clientBuilder.buildAsyncClient().detectLanguage("This is a test English Text"))
            .assertNext(response ->
                validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0), response))
            .verifyComplete();
    }
}
