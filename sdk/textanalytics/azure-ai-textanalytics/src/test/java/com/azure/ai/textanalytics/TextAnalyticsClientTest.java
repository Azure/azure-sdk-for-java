// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
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
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
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
            client.detectBatchLanguagesWithResponse(inputs, options, Context.NONE).getValue()));
    }

    /**
     * Test Detect batch input languages.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) -> validateDetectLanguage(false,
            getExpectedBatchDetectedLanguages(), client.detectBatchLanguages(inputs)));
    }

    /**
     * Test Detect batch languages for List of String input with country Hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(),
            client.detectLanguagesWithResponse(inputs, countryHint, Context.NONE).getValue()));
    }

    /**
     * Test Detect batch languages for List of String input.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(), client.detectLanguages(inputs)));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a text input to detectLanguages.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 0.0);
        List<DetectedLanguage> expectedLanguageList = Collections.singletonList(primaryLanguage);
        validateDetectedLanguages(
            client.detectLanguage("This is a test English Text").getDetectedLanguages(), expectedLanguageList);
    }

    /**
     * Verifies that an exception is thrown when null text is passed.
     */
    @Test
    public void detectLanguagesNullInput() {
        assertThrows(NullPointerException.class, () -> client.detectBatchLanguagesWithResponse(null, null,
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
        validateDetectedLanguages(client.detectLanguage("!@#%%").getDetectedLanguages(), Collections.singletonList(primaryLanguage));
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for a text input with invalid country hint.
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        Exception exception = assertThrows(TextAnalyticsException.class, () ->
            client.detectLanguageWithResponse("Este es un document escrito en Español.", "en", Context.NONE));
        assertTrue(exception.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same IDs.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.detectBatchLanguagesWithResponse(inputs, options, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @Test
    public void recognizeEntitiesForTextInput() {
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("Seattle", "Location", null, 26, 7, 0.0);
        CategorizedEntity categorizedEntity2 = new CategorizedEntity("last week", "DateTime", "DateRange", 34, 9, 0.0);
        RecognizeEntitiesResult recognizeEntitiesResultList = new RecognizeEntitiesResult("0", null, null, Arrays.asList(categorizedEntity1, categorizedEntity2));
        validateCategorizedEntities(recognizeEntitiesResultList.getEntities(),
            client.recognizeEntities("I had a wonderful trip to Seattle last week.").getEntities());
    }

    @Test
    public void recognizeEntitiesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizeEntities(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizeEntitiesForFaultyText() {
        assertEquals(client.recognizeEntities("!@#%%").getEntities().size(), 0);
    }

    @Test
    public void recognizeEntitiesBatchInputSingleError() {
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) -> {
            DocumentResultCollection<RecognizeEntitiesResult> l = client.recognizeBatchEntities(inputs);
            for (RecognizeEntitiesResult recognizeEntitiesResult : l) {
                Exception exception = assertThrows(TextAnalyticsException.class, () -> recognizeEntitiesResult.getEntities());
                assertTrue(exception.getMessage().equals(BATCH_ERROR_EXCEPTION_MESSAGE));
            }
        });
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchCategorizedEntityRunner((inputs) -> validateCategorizedEntity(false,
            getExpectedBatchCategorizedEntities(), client.recognizeBatchEntities(inputs)));
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            validateCategorizedEntity(true, getExpectedBatchCategorizedEntities(),
                client.recognizeBatchEntitiesWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(), client.recognizeEntities(inputs)));
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeCatgeorizedEntitiesLanguageHintRunner((inputs, language) ->
            validateCategorizedEntity(false, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesWithResponse(inputs, language, Context.NONE).getValue()));
    }

    @Test
    public void recognizePiiEntitiesForTextInput() {
        PiiEntity piiEntity = new PiiEntity("859-98-0987", "U.S. Social Security Number (SSN)", "", 28, 11, 0.0);
        RecognizePiiEntitiesResult recognizePiiEntitiesResult = new RecognizePiiEntitiesResult("0", null, null, Collections.singletonList(piiEntity));
        validatePiiEntities(recognizePiiEntitiesResult.getEntities(),
            client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's.").getEntities());
    }

    @Test
    public void recognizePiiEntitiesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizePiiEntities(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizePiiEntitiesForFaultyText() {
        assertEquals(client.recognizePiiEntities("!@#%%").getEntities().size(), 0);
    }

    @Test
    public void recognizePiiEntitiesForBatchInput() {
        recognizeBatchPiiRunner((inputs) ->
            validatePiiEntity(false, getExpectedBatchPiiEntities(),
                client.recognizeBatchPiiEntities(inputs)));
    }

    @Test
    public void recognizePiiEntitiesForBatchInputShowStatistics() {
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            validatePiiEntity(true, getExpectedBatchPiiEntities(),
                client.recognizeBatchPiiEntitiesWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void recognizePiiEntitiesForBatchStringInput() {
        recognizePiiStringInputRunner((inputs) ->
            validatePiiEntity(false, getExpectedBatchPiiEntities(), client.recognizePiiEntities(inputs)));
    }

    @Test
    public void recognizePiiEntitiesForListLanguageHint() {
        recognizePiiLanguageHintRunner((inputs, language) ->
            validatePiiEntity(false, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesWithResponse(inputs, language, Context.NONE).getValue()));
    }

    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        LinkedEntity linkedEntity1 = new LinkedEntity("Seattle", Collections.singletonList(linkedEntityMatch1), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");
        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResultList = new RecognizeLinkedEntitiesResult("0", null, null, Collections.singletonList(linkedEntity1));

        validateLinkedEntities(recognizeLinkedEntitiesResultList.getLinkedEntities(), client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week.").getLinkedEntities());
    }

    @Test
    public void recognizeLinkedEntitiesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizeLinkedEntities(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void recognizeLinkedEntitiesForFaultyText() {
        assertEquals(client.recognizeLinkedEntities("!@#%%").getLinkedEntities().size(), 0);
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInput() {
        recognizeBatchLinkedEntityRunner((inputs) ->
            validateLinkedEntity(false, getExpectedBatchLinkedEntities(), client.recognizeBatchLinkedEntities(inputs)));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            validateLinkedEntity(true, getExpectedBatchLinkedEntities(),
                client.recognizeBatchLinkedEntitiesWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) ->
            validateLinkedEntity(false, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntities(inputs)));
    }

    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            validateLinkedEntity(false, getExpectedBatchLinkedEntities(),
                client.recognizeLinkedEntitiesWithResponse(inputs, language, Context.NONE).getValue()));
    }

    @Test
    public void extractKeyPhrasesForTextInput() {
        validateKeyPhrases(Collections.singletonList("monde"),
            client.extractKeyPhrases("Bonjour tout le monde.").getKeyPhrases());
    }

    @Test
    public void extractKeyPhrasesForEmptyText() {
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.extractKeyPhrases(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void extractKeyPhrasesForFaultyText() {
        assertEquals(client.extractKeyPhrases("!@#%%").getKeyPhrases().size(), 0);
    }

    @Test
    public void extractKeyPhrasesForBatchInput() {
        extractBatchKeyPhrasesRunner((inputs) ->
            validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), client.extractBatchKeyPhrases(inputs)));
    }

    @Test
    public void extractKeyPhrasesForBatchInputShowStatistics() {
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            validateExtractKeyPhrase(true, getExpectedBatchKeyPhrases(),
                client.extractBatchKeyPhrasesWithResponse(inputs, options, Context.NONE).getValue()));
    }

    @Test
    public void extractKeyPhrasesForBatchStringInput() {
        extractKeyPhrasesStringInputRunner((inputs) ->
            validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(), client.extractKeyPhrases(inputs)));
    }

    @Test
    public void extractKeyPhrasesForListLanguageHint() {
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            validateExtractKeyPhrase(false, getExpectedBatchKeyPhrases(),
                client.extractKeyPhrasesWithResponse(inputs, language, Context.NONE).getValue()));
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

        AnalyzeSentimentResult analyzeSentimentResult =
            client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi.");

        validateAnalysedSentiment(expectedDocumentSentiment, analyzeSentimentResult.getDocumentSentiment());
        validateAnalysedSentenceSentiment(expectedSentenceSentiments, analyzeSentimentResult.getSentenceSentiments());
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
        final TextSentiment expectedDocumentSentiment = new TextSentiment(TextSentimentClass.NEUTRAL, 0.0, 0.0, 0.0, 5, 0);
        final List<TextSentiment> expectedSentenceSentiments = Arrays.asList(
            new TextSentiment(TextSentimentClass.NEUTRAL, 0.0, 0.0, 0.0, 1, 0),
            new TextSentiment(TextSentimentClass.NEUTRAL, 0.0, 0.0, 0.0, 4, 1));

        AnalyzeSentimentResult analyzeSentimentResult = client.analyzeSentiment("!@#%%");

        validateAnalysedSentiment(expectedDocumentSentiment, analyzeSentimentResult.getDocumentSentiment());
        validateAnalysedSentenceSentiment(expectedSentenceSentiments, analyzeSentimentResult.getSentenceSentiments());
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            validateSentiment(false, getExpectedBatchTextSentiment(), client.analyzeSentiment(inputs)));
    }

    /**
     * Test analyzing sentiment for a list of string input with language hint.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            validateSentiment(false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentWithResponse(inputs, language, Context.NONE).getValue()));
    }

    /**
     * Test analyzing sentiment for batch input.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs -> validateSentiment(false, getExpectedBatchTextSentiment(),
            client.analyzeBatchSentiment(inputs)));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            validateSentiment(true, getExpectedBatchTextSentiment(),
                client.analyzeBatchSentimentWithResponse(inputs, options, Context.NONE).getValue()));
    }

    /**
     * Test client builder with valid subscription key
     */
    @Test
    public void validKey() {
        // Arrange
        final TextAnalyticsClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsApiKeyCredential(getSubscriptionKey())).buildClient();

        // Action and Assert
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            client.detectLanguage("This is a test English Text").getDetectedLanguages());
    }

    /**
     * Test client builder with invalid subscription key
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
     * Test client with valid subscription key but update to invalid key and make call to server.
     */
    @Test
    public void updateToInvalidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential(getSubscriptionKey());

        final TextAnalyticsClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to invalid key
        credential.updateCredential(INVALID_KEY);

        // Action and Assert
        assertThrows(HttpResponseException.class, () -> client.detectLanguage("This is a test English Text"));
    }

    /**
     * Test client with invalid subscription key but update to valid key and make call to server.
     */
    @Test
    public void updateToValidKey() {
        // Arrange
        final TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential(INVALID_KEY);

        final TextAnalyticsClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to valid key
        credential.updateCredential(getSubscriptionKey());

        // Action and Assert
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            client.detectLanguage("This is a test English Text").getDetectedLanguages());
    }

    /**
     * Test for missing endpoint
     */
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.buildClient();
        });
    }

    /**
     * Test for null subscription key
     */
    @Test
    public void nullSubscriptionKey() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(getEndpoint()).subscriptionKey(null);
        });
    }

    /**
     * Test for null AAD credential
     */
    @Test
    public void nullAADCredential() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(getEndpoint()).credential(null);
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
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            clientBuilder.buildClient().detectLanguage("This is a test English Text").getDetectedLanguages());
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
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            clientBuilder.buildClient().detectLanguage("This is a test English Text").getDetectedLanguages());
    }
}
