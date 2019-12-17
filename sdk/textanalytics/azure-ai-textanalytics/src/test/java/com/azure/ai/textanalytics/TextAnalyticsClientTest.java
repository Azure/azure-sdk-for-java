// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.Error;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.LinkedEntityResult;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.NamedEntityResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {

    private TextAnalyticsClient client;

    @Test
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
        detectLanguageShowStatisticsRunner((inputs, options) -> validateBatchResult(
            client.detectBatchLanguagesWithResponse(inputs, options, Context.NONE).getValue(),
            getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE));
    }

    /**
     * Test Detect batch input languages.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) -> validateBatchResult(client.detectBatchLanguages(inputs),
            getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE));
    }

    /**
     * Test Detect batch languages for List of String input with country Hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) -> validateBatchResult(
            client.detectLanguagesWithResponse(inputs, countryHint, Context.NONE).getValue(),
            getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE));
    }

    /**
     * Test Detect batch languages for List of String input.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) -> validateBatchResult(client.detectLanguages(inputs),
            getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a text input to detectLanguages.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage().setName("English").setIso6391Name("en").setScore(1.0);
        List<DetectedLanguage> expectedLanguageList = Arrays.asList(primaryLanguage);
        validateDetectedLanguages(
            client.detectLanguage("This is a test English Text").getDetectedLanguages(), expectedLanguageList);
    }

    /**
     * Verifies that an exception is thrown when null text is passed.
     */
    @Test
    public void detectLanguagesNullInput() {
        assertThrows(HttpResponseException.class, () -> client.detectBatchLanguagesWithResponse(null, null,
            Context.NONE).getValue());
    }

    /**
     * Verifies that the error result is returned when empty text is passed.
     */
    @Test
    public void detectLanguageEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        DetectLanguageResult result = client.detectLanguage("");
        assertNotNull(result.getError());
        validateErrorDocument(expectedError, result.getError());
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @Test
    public void detectLanguageFaultyText() {
        DetectLanguageResult result = client.detectLanguage("!@#%%");
        assertEquals(result.getPrimaryLanguage().getIso6391Name(), "(Unknown)");
    }

    /**
     * Verifies that an error document is returned for a text input with invalid country hint.
     * <p>
     * TODO: update error Model. #6559
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid Country Hint.");
        validateErrorDocument(client.detectLanguage("Este es un document escrito en Español.", "en")
                                  .getError(), expectedError);
    }

    /**
     * Verifies that a Bad request exception is returned for input documents with same ids.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) -> {
            assertRestException(() -> client.detectBatchLanguagesWithResponse(inputs, options, Context.NONE),
                HttpResponseException.class, 400);
        });
    }

    @Test
    public void recognizeEntitiesForTextInput() {
        NamedEntity namedEntity1 = new NamedEntity().setText("Seattle").setType("Location").setOffset(26).setLength(7).setScore(0.80624294281005859);
        NamedEntity namedEntity2 = new NamedEntity().setText("last week").setType("DateTime").setSubtype("DateRange").setOffset(34).setLength(9).setScore(0.8);
        NamedEntityResult namedEntityResultList = new NamedEntityResult("0", null, null, Arrays.asList(namedEntity1, namedEntity2));
        validateNamedEntities(namedEntityResultList.getNamedEntities(),
            client.recognizeEntities("I had a wonderful trip to Seattle last week.").getNamedEntities());
    }

    @Test
    public void recognizeEntitiesForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        validateErrorDocument(expectedError, client.recognizeEntities("").getError());
    }

    @Test
    public void recognizeEntitiesForFaultyText() {
        // TODO: (savaity) confirm with service team.
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchNamedEntityRunner((inputs) -> validateBatchResult(client.recognizeBatchEntities(inputs),
            getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchNamedEntitiesShowStatsRunner((inputs, options) ->
            validateBatchResult(client.recognizeBatchEntitiesWithResponse(inputs, options, Context.NONE).getValue(),
                getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizePiiEntitiesForTextInput() {
        NamedEntity namedEntity1 = new NamedEntity().setText("859-98-0987").setType("U.S. Social Security Number (SSN)").setSubtype("").setOffset(28).setLength(11).setScore(0.65);
        NamedEntityResult namedEntityResultList = new NamedEntityResult("0", null, null, Collections.singletonList(namedEntity1));
        validateNamedEntities(namedEntityResultList.getNamedEntities(),
            client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's.").getNamedEntities());
    }

    @Test
    public void recognizePiiEntitiesForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        validateErrorDocument(expectedError, client.recognizePiiEntities("").getError());
    }

    @Test
    public void recognizePiiEntitiesForFaultyText() {
        // TODO: (savaity) confirm with service team.
    }

    @Test
    public void recognizePiiEntitiesForBatchInput() {
        recognizeBatchPiiRunner((inputs) ->
            validateBatchResult(client.recognizeBatchPiiEntities(inputs),
                getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizePiiEntitiesForBatchInputShowStatistics() {
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            validateBatchResult(client.recognizeBatchPiiEntitiesWithResponse(inputs, options, Context.NONE).getValue(),
                getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizePiiEntitiesForBatchStringInput() {
        recognizePiiStringInputRunner((inputs) ->
            validateBatchResult(client.recognizePiiEntities(inputs),
                getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizePiiEntitiesForListLanguageHint() {
        recognizePiiLanguageHintRunner((inputs, language) ->
            validateBatchResult(client.recognizePiiEntitiesWithResponse(inputs, language, Context.NONE).getValue(),
                getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeNamedEntityStringInputRunner((inputs) ->
            validateBatchResult(client.recognizeEntities(inputs),
                getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeNamedEntitiesLanguageHintRunner((inputs, language) ->
            validateBatchResult(client.recognizeEntitiesWithResponse(inputs, language, Context.NONE).getValue(),
                getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY));
    }

    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch().setText("Seattle").setLength(7).setOffset(26).setScore(0.11472424095537814);
        LinkedEntity linkedEntity1 = new LinkedEntity().setName("Seattle").setUrl("https://en.wikipedia.org/wiki/Seattle").setDataSource("Wikipedia").setLinkedEntityMatches(Collections.singletonList(linkedEntityMatch1)).setLanguage("en").setId("Seattle");
        LinkedEntityResult linkedEntityResultList = new LinkedEntityResult("0", null, null, Collections.singletonList(linkedEntity1));

        validateLinkedEntities(linkedEntityResultList.getLinkedEntities(), client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week.").getLinkedEntities());
    }

    @Test
    public void recognizeLinkedEntitiesForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        validateErrorDocument(expectedError, client.recognizeLinkedEntities("").getError());
    }

    @Test
    public void recognizeLinkedEntitiesForFaultyText() {

    }

    @Test
    public void recognizeLinkedEntitiesForBatchInput() {
        recognizeBatchLinkedEntityRunner((inputs) ->
            validateBatchResult(client.recognizeBatchLinkedEntities(inputs),
                getExpectedBatchLinkedEntities(), TestEndpoint.LINKED_ENTITY));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            validateBatchResult(client.recognizeBatchLinkedEntitiesWithResponse(inputs, options, Context.NONE).getValue(),
                getExpectedBatchLinkedEntities(), TestEndpoint.LINKED_ENTITY));
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) ->
            validateBatchResult(client.recognizeLinkedEntities(inputs),
                getExpectedBatchLinkedEntities(), TestEndpoint.LINKED_ENTITY));
    }

    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            validateBatchResult(client.recognizeLinkedEntitiesWithResponse(inputs, language, Context.NONE).getValue(),
                getExpectedBatchLinkedEntities(), TestEndpoint.LINKED_ENTITY));
    }

    @Test
    public void recognizeKeyPhrasesForTextInput() {
        List<String> keyPhrasesList1 = Arrays.asList("monde");
        validateKeyPhrases(keyPhrasesList1,
            client.extractKeyPhrasesWithResponse("Bonjour tout le monde.", "fr", Context.NONE)
                .getValue().getKeyPhrases());
    }

    @Test
    public void recognizeKeyPhrasesForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        validateErrorDocument(expectedError, client.extractKeyPhrases("").getError());
    }

    @Test
    public void recognizeKeyPhrasesForFaultyText() {

    }

    @Test
    public void recognizeKeyPhrasesForBatchInput() {
        recognizeBatchLinkedEntityRunner((inputs) ->
            validateBatchResult(client.extractBatchKeyPhrases(inputs),
                getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES));
    }

    @Test
    public void recognizeKeyPhrasesForBatchInputShowStatistics() {
        recognizeBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            validateBatchResult(client.extractBatchKeyPhrasesWithResponse(inputs, options, Context.NONE).getValue(),
                getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES));
    }

    @Test
    public void recognizeKeyPhrasesForBatchStringInput() {
        recognizeKeyPhrasesStringInputRunner((inputs) ->
            validateBatchResult(client.extractKeyPhrases(inputs),
                getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES));
    }

    @Test
    public void recognizeKeyPhrasesForListLanguageHint() {
        recognizeKeyPhrasesLanguageHintRunner((inputs, language) ->
            validateBatchResult(client.extractKeyPhrasesWithResponse(inputs, language, Context.NONE).getValue(),
                getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES));
    }

    // Sentiment
    /**
     * Test analyzing sentiment for a string input.
     */
    @Test
    public void analyseSentimentForTextInput() {
        analyseBatchSentimentRunner(inputs -> validateBatchResult(client.analyzeBatchSentiment(inputs),
            getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT));
    }

    /**
     * Verifies that an error document is returned for a empty text input.
     */
    @Test
    public void analyseSentimentForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        validateErrorDocument(expectedError, client.analyzeSentiment("").getError());
    }

    @Test
    public void analyseSentimentForFaultyText() {
        // TODO (shawn): add this case later
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            validateBatchResult(client.analyzeSentiment(inputs), getExpectedBatchTextSentiment(),
                TestEndpoint.SENTIMENT));
    }

    /**
     * Test analyzing sentiment for a list of string input with language hint.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            validateBatchResult(client.analyzeSentimentWithResponse(inputs, language, Context.NONE).getValue(),
                getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT));
    }

    /**
     * Test analyzing sentiment for batch input.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs -> validateBatchResult(client.analyzeBatchSentiment(inputs),
            getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            validateBatchResult(client.analyzeBatchSentimentWithResponse(inputs, options, Context.NONE).getValue(),
                getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT));
    }
}
