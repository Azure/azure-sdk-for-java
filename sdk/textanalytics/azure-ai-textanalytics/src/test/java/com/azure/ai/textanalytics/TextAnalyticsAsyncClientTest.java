// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.ErrorCodeValue;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.TextSentimentClass;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextAnalyticsAsyncClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsAsyncClient client;

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
        detectLanguageShowStatisticsRunner((inputs, options) -> {
            StepVerifier.create(client.detectBatchLanguagesWithResponse(inputs, options))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE))
                .verifyComplete();
        });
    }

    /**
     * Test Detect batch input languages.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) -> {
            StepVerifier.create(client.detectBatchLanguages(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE))
                .verifyComplete();
        });
    }

    /**
     * Test Detect batch languages for List of String input with country Hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) -> {
            StepVerifier.create(client.detectLanguagesWithResponse(inputs, countryHint))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE))
                .verifyComplete();
        });
    }

    /**
     * Test Detect batch languages for List of String input.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) -> {
            StepVerifier.create(client.detectLanguages(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), TestEndpoint.LANGUAGE))
                .verifyComplete();
        });
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a text input to detectLanguages.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0);
        List<DetectedLanguage> expectedLanguageList = Arrays.asList(primaryLanguage);
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(expectedLanguageList, response.getDetectedLanguages()))
            .verifyComplete();
    }

    /**
     * Verifies that an error document is returned for a text input with invalid country hint.
     * <p>
     * TODO: update error Model. #6559
     */
    @Test
    public void detectLanguageInvalidCountryHint() {
        TextAnalyticsError expectedError = new TextAnalyticsError(ErrorCodeValue.INVALID_ARGUMENT, "Invalid Country Hint.", null, null);
        StepVerifier.create(client.detectLanguageWithResponse("Este es un document escrito en Español.", "en"))
            .assertNext(response -> validateErrorDocument(expectedError, response.getValue().getError()))
            .verifyComplete();
    }

    /**
     * Verifies that an error document is returned for a empty text input.
     */
    @Test
    public void detectLanguageEmptyText() {
        TextAnalyticsError expectedError = new TextAnalyticsError(ErrorCodeValue.INVALID_ARGUMENT, "Invalid document in request.", null, null);
        StepVerifier.create(client.detectLanguage(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @Test
    public void detectLanguageFaultyText() {
        StepVerifier.create(client.detectLanguage("!@#%%"))
            .assertNext(response -> assertEquals(response.getPrimaryLanguage().getIso6391Name(), "(Unknown)"))
            .verifyComplete();
    }

    /**
     * Verifies that a Bad request exception is returned for input documents with same ids.
     */
    @Test
    public void detectLanguageDuplicateIdInput() {
        detectLanguageDuplicateIdRunner((inputs, options) -> {
            StepVerifier.create(client.detectBatchLanguagesWithResponse(inputs, options, Context.NONE))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, 400));
        });
    }

    // Entities
    @Test
    public void recognizeEntitiesForTextInput() {
        NamedEntity namedEntity1 = new NamedEntity("Seattle", "Location", null, 26, 7, 0.80624294281005859);
        NamedEntity namedEntity2 = new NamedEntity("last week", "DateTime", "DateRange", 34, 9, 0.8);
        RecognizeEntitiesResult recognizeEntitiesResultList = new RecognizeEntitiesResult("0", null, null, Arrays.asList(namedEntity1, namedEntity2));
        StepVerifier.create(client.recognizeEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateNamedEntities(recognizeEntitiesResultList.getNamedEntities(), response.getNamedEntities()))
            .verifyComplete();
    }

    @Test
    public void recognizeEntitiesForEmptyText() {
        TextAnalyticsError expectedError = new TextAnalyticsError(ErrorCodeValue.INVALID_ARGUMENT, "Invalid document in request.", null, null);
        StepVerifier.create(client.recognizeEntities(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    @Test
    public void recognizeEntitiesForFaultyText() {
        // TODO: (savaity) confirm with service team this returns no error-ed document, no exception but empty documents and error list.
        StepVerifier.create(client.recognizeEntities("!@#%%"))
            .assertNext(response -> assertEquals(response.getNamedEntities().size(), 0))
            .verifyComplete();
    }

    @Test
    public void recognizeEntitiesForBatchInput() {
        recognizeBatchNamedEntityRunner((inputs) -> {
            StepVerifier.create(client.recognizeBatchEntities(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizeEntitiesForBatchInputShowStatistics() {
        recognizeBatchNamedEntitiesShowStatsRunner((inputs, options) -> {
            StepVerifier.create(client.recognizeBatchEntitiesWithResponse(inputs, options))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizeEntitiesForBatchStringInput() {
        recognizeNamedEntityStringInputRunner((inputs) -> {
            StepVerifier.create(client.recognizeEntities(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizeEntitiesForListLanguageHint() {
        recognizeNamedEntitiesLanguageHintRunner((inputs, language) -> {
            StepVerifier.create(client.recognizeEntitiesWithResponse(inputs, language))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchNamedEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    // Linked Entities
    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("Seattle", 0.11472424095537814, 7, 26);
        LinkedEntity linkedEntity1 = new LinkedEntity("Seattle", Collections.singletonList(linkedEntityMatch1), "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");
        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResultList = new RecognizeLinkedEntitiesResult("0", null, null, Collections.singletonList(linkedEntity1));

        StepVerifier.create(client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateLinkedEntities(recognizeLinkedEntitiesResultList.getLinkedEntities(), response.getLinkedEntities()))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesForEmptyText() {
        TextAnalyticsError expectedError = new TextAnalyticsError(ErrorCodeValue.INVALID_ARGUMENT, "Invalid document in request.", null, null);
        StepVerifier.create(client.recognizeLinkedEntities(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesForFaultyText() {
        StepVerifier.create(client.recognizeLinkedEntities("!@#%%"))
            .assertNext(response -> assertEquals(response.getLinkedEntities().size(), 0))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInput() {
        recognizeBatchLinkedEntityRunner((inputs) -> {
            StepVerifier.create(client.recognizeBatchLinkedEntities(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchLinkedEntities(), TestEndpoint.LINKED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizeLinkedEntitiesForBatchInputShowStatistics() {
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) -> {
            StepVerifier.create(client.recognizeBatchLinkedEntitiesWithResponse(inputs, options))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchLinkedEntities(), TestEndpoint.LINKED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizeLinkedEntitiesForBatchStringInput() {
        recognizeLinkedStringInputRunner((inputs) -> {
            StepVerifier.create(client.recognizeLinkedEntities(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchLinkedEntities(), TestEndpoint.LINKED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizeLinkedEntitiesForListLanguageHint() {
        recognizeLinkedLanguageHintRunner((inputs, language) -> {
            StepVerifier.create(client.recognizeLinkedEntitiesWithResponse(inputs, language))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchLinkedEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    // Pii Entities
    @Test
    public void recognizePiiEntitiesForTextInput() {
        NamedEntity namedEntity1 = new NamedEntity("859-98-0987", "U.S. Social Security Number (SSN)", "", 28, 11, 0.65);
        RecognizeEntitiesResult recognizeEntitiesResultList = new RecognizeEntitiesResult("0", null, null, Collections.singletonList(namedEntity1));

        StepVerifier.create(client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's."))
            .assertNext(response -> validateNamedEntities(recognizeEntitiesResultList.getNamedEntities(), response.getNamedEntities()))
            .verifyComplete();
    }

    @Test
    public void recognizePiiEntitiesForEmptyText() {
        TextAnalyticsError expectedError = new TextAnalyticsError(ErrorCodeValue.INVALID_ARGUMENT, "Invalid document in request.", null, null);
        StepVerifier.create(client.recognizePiiEntities(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    @Test
    public void recognizePiiEntitiesForFaultyText() {
        // TODO: (savaity) confirm with service team this returns no error-ed document, no exception but empty documents and error list.
        StepVerifier.create(client.recognizePiiEntities("!@#%%"))
            .assertNext(response -> assertEquals(response.getNamedEntities().size(), 0))
            .verifyComplete();
    }

    @Test
    public void recognizePiiEntitiesForBatchInput() {
        recognizeBatchPiiRunner((inputs) -> {
            StepVerifier.create(client.recognizeBatchPiiEntities(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizePiiEntitiesForBatchInputShowStatistics() {
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) -> {
            StepVerifier.create(client.recognizeBatchPiiEntitiesWithResponse(inputs, options))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizePiiEntitiesForBatchStringInput() {
        recognizePiiStringInputRunner((inputs) -> {
            StepVerifier.create(client.recognizePiiEntities(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }

    @Test
    public void recognizePiiEntitiesForListLanguageHint() {
        recognizePiiLanguageHintRunner((inputs, language) -> {
            StepVerifier.create(client.recognizePiiEntitiesWithResponse(inputs, language))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchPiiEntities(), TestEndpoint.NAMED_ENTITY))
                .verifyComplete();
        });
    }


    // Key Phrases
    @Test
    public void extractKeyPhrasesForTextInput() {
        List<String> keyPhrasesList1 = Arrays.asList("monde");
        StepVerifier.create(client.extractKeyPhrasesWithResponse("Bonjour tout le monde.", "fr"))
            .assertNext(response -> validateKeyPhrases(keyPhrasesList1, response.getValue().getKeyPhrases()))
            .verifyComplete();
    }

    @Test
    public void extractKeyPhrasesForEmptyText() {
        TextAnalyticsError expectedError = new TextAnalyticsError(ErrorCodeValue.INVALID_ARGUMENT, "Invalid document in request.", null, null);
        StepVerifier.create(client.extractKeyPhrases(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    @Test
    public void extractKeyPhrasesForFaultyText() {
        StepVerifier.create(client.extractKeyPhrases("!@#%%"))
            .assertNext(response -> assertEquals(response.getKeyPhrases().size(), 0))
            .verifyComplete();
    }

    @Test
    public void extractKeyPhrasesForBatchInput() {
        extractBatchKeyPhrasesRunner((inputs) -> {
            StepVerifier.create(client.extractBatchKeyPhrases(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES))
                .verifyComplete();
        });

    }

    @Test
    public void extractKeyPhrasesForBatchInputShowStatistics() {
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) -> {
            StepVerifier.create(client.extractBatchKeyPhrasesWithResponse(inputs, options))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES))
                .verifyComplete();
        });
    }

    @Test
    public void extractKeyPhrasesForBatchStringInput() {
        extractKeyPhrasesStringInputRunner((inputs) -> {
            StepVerifier.create(client.extractKeyPhrases(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES))
                .verifyComplete();
        });
    }

    @Test
    public void extractKeyPhrasesForListLanguageHint() {
        extractKeyPhrasesLanguageHintRunner((inputs, language) -> {
            StepVerifier.create(client.extractKeyPhrasesWithResponse(inputs, language))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchKeyPhrases(), TestEndpoint.KEY_PHRASES))
                .verifyComplete();
        });
    }

    // Sentiment
    /**
     * Test analyzing sentiment for a string input.
     */
    @Test
    public void analyseSentimentForTextInput() {
        final TextSentiment expectedDocumentSentiment = new TextSentiment(TextSentimentClass.MIXED, 0.1, 0.5, 0.4, 66, 0);
        final List<TextSentiment> expectedSentenceSentiments = Arrays.asList(
            new TextSentiment(TextSentimentClass.NEGATIVE, 0.99, 0.005, 0.005, 31, 0),
            new TextSentiment(TextSentimentClass.POSITIVE, 0.005, 0.005, 0.99, 35, 32));

        StepVerifier
            .create(client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi."))
            .assertNext(response -> {
                validateAnalysedSentiment(expectedDocumentSentiment, response.getDocumentSentiment());
                validateAnalysedSentenceSentiment(expectedSentenceSentiments, response.getSentenceSentiments());
            }).verifyComplete();
    }

    /**
     * Verifies that an error document is returned for a empty text input.
     */
    @Test
    public void analyseSentimentForEmptyText() {
        TextAnalyticsError expectedError = new TextAnalyticsError(ErrorCodeValue.INVALID_ARGUMENT, "Invalid document in request.", null, null);
        StepVerifier.create(client.analyzeSentiment(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError())).verifyComplete();
    }

    public void analyseSentimentForFaultyText() {
        // TODO (shawn): add this case later
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @Test
    public void analyseSentimentForBatchStringInput() {
        analyseSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentiment(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for a list of string input with language hint.
     */
    @Test
    public void analyseSentimentForListLanguageHint() {
        analyseSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentWithResponse(inputs, language))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT))
                .verifyComplete());
    }

    /**
     * Test analyzing sentiment for batch input.
     */
    @Test
    public void analyseSentimentForBatchInput() {
        analyseBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeBatchSentiment(inputs))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT))
                .verifyComplete());
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch input with options.
     */
    @Test
    public void analyseSentimentForBatchInputShowStatistics() {
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.analyzeBatchSentimentWithResponse(inputs, options))
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchTextSentiment(), TestEndpoint.SENTIMENT))
                .verifyComplete());
    }
}
