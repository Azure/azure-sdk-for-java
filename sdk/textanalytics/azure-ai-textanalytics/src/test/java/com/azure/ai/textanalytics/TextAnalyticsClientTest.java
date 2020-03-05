// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.DocumentSentimentLabel;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
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
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
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
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntities;
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

    /**
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void detectLanguagesBatchInputShowStatistics() {
        detectLanguageShowStatisticsRunner((inputs, options) -> validateDetectLanguage(true,
            getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatchWithResponse(inputs, options, Context.NONE).getValue()));
    }

    /**
     * Test Detect batch input languages.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) -> validateDetectLanguage(false,
            getExpectedBatchDetectedLanguages(), client.detectLanguageBatchWithResponse(inputs, null, Context.NONE).getValue()));
    }

    /**
     * Test detect batch languages for a list of string input with country hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, countryHint, null)));
    }

    /**
     * Test detect batch languages for a list of string input.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs)));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a text input to detectLanguages.
     */
    @Test
    public void detectSingleTextLanguage() {
        validatePrimaryLanguage(new DetectedLanguage("English", "en", 0.0),
            client.detectLanguage("This is a test English Text"));
    }

    /**
     * Verifies that an exception is thrown when null text is passed.
     */
    @Test
    public void detectLanguagesNullInput() {
        assertThrows(NullPointerException.class, () -> client.detectLanguageBatchWithResponse(null, null,
            Context.NONE).getValue());
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty text input.
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
     * Verifies that a TextAnalyticsException is thrown for a text input with invalid country hint.
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        Exception exception = assertThrows(TextAnalyticsException.class, () ->
            client.detectLanguage("Este es un document escrito en Español.", "en"));
        assertTrue(exception.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same IDs.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.detectLanguageBatchWithResponse(inputs, options, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @Test
    public void recognizeEntitiesForTextInput() {
        final CategorizedEntity categorizedEntity1 = new CategorizedEntity("Seattle", "Location", null, 26, 7, 0.0);
        final CategorizedEntity categorizedEntity2 = new CategorizedEntity("last week", "DateTime", "DateRange", 34, 9, 0.0);

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
            Response<DocumentResultCollection<RecognizeEntitiesResult>> response = client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE);
            for (RecognizeEntitiesResult recognizeEntitiesResult : response.getValue()) {
                Exception exception = assertThrows(TextAnalyticsException.class, () -> recognizeEntitiesResult.getEntities());
                assertTrue(exception.getMessage().equals(BATCH_ERROR_EXCEPTION_MESSAGE));
            }
        });
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchCategorizedEntityRunner((inputs) -> validateCategorizedEntity(false,
            getExpectedBatchCategorizedEntities(), client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE).getValue()));
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            validateCategorizedEntity(true, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatchWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), client.recognizeEntitiesBatch(inputs)));
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeCatgeorizedEntitiesLanguageHintRunner((inputs, language) ->
            validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatch(inputs, language, null)));
    }

    @Test
    public void recognizePiiEntitiesForTextInput() {
        final PiiEntity piiEntity = new PiiEntity("859-98-0987", "U.S. Social Security Number (SSN)", "", 28, 11, 0.0);
        final PagedIterable<PiiEntity> entities = client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's.");
        validatePiiEntity(piiEntity, entities.iterator().next());
    }

    @Test
    public void recognizePiiEntitiesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizePiiEntities("").iterator().hasNext());
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizePiiEntitiesForFaultyText() {
        assertFalse(client.recognizePiiEntities("!@#%%").iterator().hasNext());
    }

    @Test
    public void recognizePiiEntitiesForBatchInput() {
        recognizeBatchPiiRunner((inputs) ->
            validatePiiEntity(false, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE).getValue()));
    }

    @Test
    public void recognizePiiEntitiesForBatchInputShowStatistics() {
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            validatePiiEntity(true, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesBatchWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void recognizePiiEntitiesForBatchStringInput() {
        recognizePiiStringInputRunner((inputs) ->
            validatePiiEntity(false, getExpectedBatchPiiEntities(), client.recognizePiiEntitiesBatch(inputs)));
    }

    @Test
    public void recognizePiiEntitiesForListLanguageHint() {
        recognizePiiLanguageHintRunner((inputs, language) ->
            validatePiiEntity(false, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesBatch(inputs, language, null)));
    }

    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        final LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        final LinkedEntity linkedEntity1 = new LinkedEntity("Seattle", Collections.singletonList(linkedEntityMatch1), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");
        final List<LinkedEntity> linkedEntities = client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week.").stream().collect(Collectors.toList());
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
            validateLinkedEntity(false, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatchWithResponse(inputs, null, Context.NONE).getValue()));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            validateLinkedEntity(true, getExpectedBatchLinkedEntities(),
                client.recognizeLinkedEntitiesBatchWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) ->
            validateLinkedEntity(false, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatch(inputs)));
    }

    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            validateLinkedEntity(false, getExpectedBatchLinkedEntities(),
                client.recognizeLinkedEntitiesBatch(inputs, language, null)));
    }

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
            validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatchWithResponse(inputs, null, Context.NONE).getValue()));
    }

    @Test
    public void extractKeyPhrasesForBatchInputShowStatistics() {
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            validateExtractKeyPhrase(true, getExpectedBatchKeyPhrases(),
                client.extractKeyPhrasesBatchWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void extractKeyPhrasesForBatchStringInput() {
        extractKeyPhrasesStringInputRunner((inputs) ->
            validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatch(inputs)));
    }

    @Test
    public void extractKeyPhrasesForListLanguageHint() {
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(),
                client.extractKeyPhrasesBatch(inputs, language, null)));
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @Test
    public void analyseSentimentForTextInput() {
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(
            DocumentSentimentLabel.MIXED,
            new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0),
            Arrays.asList(
                new SentenceSentiment(SentenceSentimentLabel.NEGATIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 31, 0),
                new SentenceSentiment(SentenceSentimentLabel.POSITIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 35, 32)
            ));
        DocumentSentiment analyzeSentimentResult =
            client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi.");

        validateAnalyzedSentiment(expectedDocumentSentiment, analyzeSentimentResult);
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty text input.
     */
    @Test
    public void analyseSentimentForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.analyzeSentiment(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Test analyzing sentiment for a faulty input text.
     */
    @Test
    public void analyseSentimentForFaultyText() {
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(DocumentSentimentLabel.NEUTRAL,
            new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0),
            Arrays.asList(
                new SentenceSentiment(SentenceSentimentLabel.NEUTRAL, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 1, 0),
                new SentenceSentiment(SentenceSentimentLabel.NEUTRAL, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 4, 1)
            ));

        DocumentSentiment analyzeSentimentResult = client.analyzeSentiment("!@#%%");

        validateAnalyzedSentiment(expectedDocumentSentiment, analyzeSentimentResult);
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            validateSentiment(false, getExpectedBatchTextSentiment(), client.analyzeSentimentBatch(inputs)));
    }

    /**
     * Test analyzing sentiment for a list of string input with language hint.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            validateSentiment(false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, language, null)));
    }

    /**
     * Test analyzing sentiment for batch input.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs -> validateSentiment(false, getExpectedBatchTextSentiment(),
            client.analyzeSentimentBatchWithResponse(inputs, null, Context.NONE).getValue()));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            validateSentiment(true, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatchWithResponse(inputs, options, Context.NONE).getValue()));
    }

    /**
     * Test client builder with valid API key
     */
    @Test
    public void validKey() {
        // Arrange
        final TextAnalyticsClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(getApiKey())).buildClient();

        // Action and Assert
        validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0),
            client.detectLanguage("This is a test English Text"));
    }

    /**
     * Test client builder with invalid API key
     */
    @Test
    public void invalidKey() {
        // Arrange
        final TextAnalyticsClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(INVALID_KEY)).buildClient();

        // Action and Assert
        assertThrows(HttpResponseException.class, () -> client.detectLanguage("This is a test English Text"));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @Test
    public void updateToInvalidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential(getApiKey());

        final TextAnalyticsClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to invalid key
        credential.updateCredential(INVALID_KEY);

        // Action and Assert
        assertThrows(HttpResponseException.class, () -> client.detectLanguage("This is a test English Text"));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @Test
    public void updateToValidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential(INVALID_KEY);

        final TextAnalyticsClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to valid key
        credential.updateCredential(getApiKey());

        // Action and Assert
        validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0),
            client.detectLanguage("This is a test English Text"));
    }

    /**
     * Test for null service version, which would take take the default service version by default
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
        validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0),
            clientBuilder.buildClient().detectLanguage("This is a test English Text"));
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
        validatePrimaryLanguage(new DetectedLanguage("English", "en", 1.0),
            clientBuilder.buildClient().detectLanguage("This is a test English Text"));
    }
}
