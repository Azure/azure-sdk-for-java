// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.TextSentimentClass;
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
import java.util.List;

import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            StepVerifier.create(client.detectBatchLanguageWithResponse(inputs, options))
                .assertNext(response -> validateDetectLanguage(true, getExpectedBatchDetectedLanguages(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Test Detect batch input languages.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) ->
            StepVerifier.create(client.detectBatchLanguage(inputs))
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test Detect batch languages for List of String input with country Hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) ->
            StepVerifier.create(client.detectLanguageWithResponse(inputs, countryHint))
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Test Detect batch languages for List of String input.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) ->
            StepVerifier.create(client.detectLanguage(inputs))
                .assertNext(response -> validateDetectLanguage(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a text input to detectLanguages.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0);
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(Collections.singletonList(primaryLanguage), response.getDetectedLanguages()))
            .verifyComplete();
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a text input with invalid country hint.
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        StepVerifier.create(client.detectLanguageWithResponse("Este es un document escrito en Español.", "en"))
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
            .assertNext(response -> validateDetectedLanguages(Collections.singletonList(primaryLanguage), response.getDetectedLanguages()))
            .verifyComplete();
    }

    /**
     * Verifies that a Bad request exception is returned for input documents with same ids.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) ->
            StepVerifier.create(client.detectBatchLanguageWithResponse(inputs, options))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    // Entities
    @Test
    public void recognizeEntitiesForTextInput() {
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("Seattle", "Location", null, 26, 7, 0.0);
        CategorizedEntity categorizedEntity2 = new CategorizedEntity("last week", "DateTime", "DateRange", 34, 9, 0.0);
        RecognizeEntitiesResult recognizeEntitiesResultList = new RecognizeEntitiesResult("0", null, null, Arrays.asList(categorizedEntity1, categorizedEntity2));
        StepVerifier.create(client.recognizeEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateCategorizedEntities(recognizeEntitiesResultList.getEntities(), response.getEntities()))
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
            .assertNext(response -> assertEquals(response.getEntities().size(), 0))
            .verifyComplete();
    }

    @Test
    public void recognizeEntitiesBatchInputSingleError() {
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizeBatchEntities(inputs))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && throwable.getMessage().equals(BATCH_ERROR_EXCEPTION_MESSAGE)));
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchCategorizedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeBatchEntities(inputs))
                .assertNext(response -> validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeBatchEntitiesWithResponse(inputs, options))
                .assertNext(response -> validateCategorizedEntity(true, getExpectedBatchCategorizedEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeEntities(inputs))
                .assertNext(response -> validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeCatgeorizedEntitiesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeEntitiesWithResponse(inputs, language))
                .assertNext(response -> validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), response.getValue()))
                .verifyComplete());
    }

    // Linked Entities
    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        LinkedEntity linkedEntity = new LinkedEntity("Seattle", Collections.singletonList(linkedEntityMatch), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");
        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResultList = new RecognizeLinkedEntitiesResult("0", null, null, Collections.singletonList(linkedEntity));

        StepVerifier.create(client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateLinkedEntities(recognizeLinkedEntitiesResultList.getLinkedEntities(), response.getLinkedEntities()))
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
            .assertNext(response -> assertEquals(response.getLinkedEntities().size(), 0))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInput() {
        recognizeBatchLinkedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeBatchLinkedEntities(inputs))
                .assertNext(response -> validateLinkedEntity(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeBatchLinkedEntitiesWithResponse(inputs, options))
                .assertNext(response -> validateLinkedEntity(true, getExpectedBatchLinkedEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntities(inputs))
                .assertNext(response -> validateLinkedEntity(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeLinkedEntitiesWithResponse(inputs, language))
                .assertNext(response -> validateLinkedEntity(false, getExpectedBatchLinkedEntities(), response.getValue()))
                .verifyComplete());
    }

    // Pii Entities
    @Test
    public void recognizePiiEntitiesForTextInput() {
        PiiEntity piiEntity = new PiiEntity("859-98-0987", "U.S. Social Security Number (SSN)", "", 28, 11, 0.0);
        RecognizePiiEntitiesResult recognizePiiEntitiesResultList = new RecognizePiiEntitiesResult("0", null, null, Collections.singletonList(piiEntity));

        StepVerifier.create(client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's."))
            .assertNext(response -> validatePiiEntities(recognizePiiEntitiesResultList.getEntities(), response.getEntities()))
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
            .assertNext(response -> assertEquals(response.getEntities().size(), 0))
            .verifyComplete();
    }

    @Test
    public void recognizePiiEntitiesForBatchInput() {
        recognizeBatchPiiRunner((inputs) ->
            StepVerifier.create(client.recognizeBatchPiiEntities(inputs))
                .assertNext(response -> validatePiiEntity(false, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForBatchInputShowStatistics() {
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeBatchPiiEntitiesWithResponse(inputs, options))
                .assertNext(response -> validatePiiEntity(true, getExpectedBatchPiiEntities(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForBatchStringInput() {
        recognizePiiStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntities(inputs))
                .assertNext(response -> validatePiiEntity(false, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @Test
    public void recognizePiiEntitiesForListLanguageHint() {
        recognizePiiLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizePiiEntitiesWithResponse(inputs, language))
                .assertNext(response -> validatePiiEntity(false, getExpectedBatchPiiEntities(), response.getValue()))
                .verifyComplete());
    }

    // Key Phrases
    @Test
    public void extractKeyPhrasesForTextInput() {
        StepVerifier.create(client.extractKeyPhrases("Bonjour tout le monde."))
            .assertNext(response -> validateKeyPhrases(Collections.singletonList("monde"), response.getKeyPhrases()))
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
            .assertNext(response -> assertEquals(response.getKeyPhrases().size(), 0))
            .verifyComplete();
    }

    @Test
    public void extractKeyPhrasesForBatchInput() {
        extractBatchKeyPhrasesRunner((inputs) ->
            StepVerifier.create(client.extractBatchKeyPhrases(inputs))
                .assertNext(response -> validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());

    }

    @Test
    public void extractKeyPhrasesForBatchInputShowStatistics() {
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractBatchKeyPhrasesWithResponse(inputs, options))
                .assertNext(response -> validateExtractKeyPhrase(true, getExpectedBatchKeyPhrases(), response.getValue()))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesForBatchStringInput() {
        extractKeyPhrasesStringInputRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrases(inputs))
                .assertNext(response -> validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @Test
    public void extractKeyPhrasesForListLanguageHint() {
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.extractKeyPhrasesWithResponse(inputs, language))
                .assertNext(response -> validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), response.getValue()))
                .verifyComplete());
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @Test
    public void analyseSentimentForTextInput() {
        final TextSentiment expectedDocumentSentiment = new TextSentiment(TextSentimentClass.MIXED, 0.0, 0.0, 0.0, 66, 0);
        final List<TextSentiment> expectedSentenceSentiments = Arrays.asList(
            new TextSentiment(TextSentimentClass.NEGATIVE, 0.0, 0.0, 0.0, 31, 0),
            new TextSentiment(TextSentimentClass.POSITIVE, 0.0, 0.0, 0.0, 35, 32));

        StepVerifier
            .create(client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi."))
            .assertNext(response -> {
                validateAnalysedSentiment(expectedDocumentSentiment, response.getDocumentSentiment());
                validateAnalysedSentenceSentiment(expectedSentenceSentiments, response.getSentenceSentiments());
            }).verifyComplete();
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
        final TextSentiment expectedDocumentSentiment = new TextSentiment(TextSentimentClass.NEUTRAL, 0.0, 0.0, 0.0, 5, 0);
        final List<TextSentiment> expectedSentenceSentiments = Arrays.asList(
            new TextSentiment(TextSentimentClass.NEUTRAL, 0.0, 0.0, 0.0, 1, 0),
            new TextSentiment(TextSentimentClass.NEUTRAL, 0.0, 0.0, 0.0, 4, 1));

        StepVerifier
            .create(client.analyzeSentiment("!@#%%"))
            .assertNext(response -> {
                validateAnalysedSentiment(expectedDocumentSentiment, response.getDocumentSentiment());
                validateAnalysedSentenceSentiment(expectedSentenceSentiments, response.getSentenceSentiments());
            }).verifyComplete();
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentiment(inputs))
                .assertNext(response -> validateSentiment(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with language hint.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentWithResponse(inputs, language))
                .assertNext(response -> validateSentiment(false, getExpectedBatchTextSentiment(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for batch input.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeBatchSentiment(inputs))
                .assertNext(response -> validateSentiment(false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeBatchSentimentWithResponse(inputs, options))
                .assertNext(response -> validateSentiment(true, getExpectedBatchTextSentiment(), response.getValue()))
                .verifyComplete());
    }

    /**
     * Test client builder with valid subscription key
     */
    @Test
    public void validKey() {
        // Arrange
        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(getSubscriptionKey())).buildAsyncClient();

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
            .verifyComplete();
    }

    /**
     * Test client builder with invalid subscription key
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
     * Test client with valid subscription key but update to invalid key and make call to server.
     */
    @Test
    public void updateToInvalidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential(getSubscriptionKey());

        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to invalid key
        credential.updateCredential(INVALID_KEY);

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .verifyError(HttpResponseException.class);
    }

    /**
     * Test client with invalid subscription key but update to valid key and make call to server.
     */
    @Test
    public void updateToValidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential(INVALID_KEY);

        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to valid key
        credential.updateCredential(getSubscriptionKey());

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
            .verifyComplete();
    }

    /**
     * Test for missing endpoint
     */
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.buildAsyncClient();
        });
    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @Test
    public void nullServiceVersion() {
        // Arrange
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .subscriptionKey(new TextAnalyticsApiKeyCredential(getSubscriptionKey()))
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
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
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
            .subscriptionKey(new TextAnalyticsApiKeyCredential(getSubscriptionKey()))
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
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
            .verifyComplete();
    }
}
