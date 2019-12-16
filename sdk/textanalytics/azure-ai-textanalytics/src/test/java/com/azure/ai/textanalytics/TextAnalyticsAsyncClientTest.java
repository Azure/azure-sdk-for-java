// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

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
        DetectedLanguage primaryLanguage = new DetectedLanguage().setName("English").setIso6391Name("en").setScore(1.0);
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
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid Country Hint.");
        StepVerifier.create(client.detectLanguageWithResponse("Este es un document escrito en Español.", "en"))
            .assertNext(response -> validateErrorDocument(expectedError, response.getValue().getError()))
            .verifyComplete();
    }

    /**
     * Verifies that an error document is returned for a empty text input.
     */
    @Test
    public void detectLanguageEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
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
        NamedEntity namedEntity1 = new NamedEntity().setText("Seattle").setType("Location").setOffset(26).setLength(7).setScore(0.80624294281005859);
        NamedEntity namedEntity2 = new NamedEntity().setText("last week").setType("DateTime").setSubtype("DateRange").setOffset(34).setLength(9).setScore(0.8);
        NamedEntityResult namedEntityResultList = new NamedEntityResult("0", null, null, Arrays.asList(namedEntity1, namedEntity2));
        StepVerifier.create(client.recognizeEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateNamedEntities(namedEntityResultList.getNamedEntities(), response.getNamedEntities()))
            .verifyComplete();
    }

    @Test
    public void recognizeEntitiesForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        StepVerifier.create(client.recognizeEntities(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    @Override
    public void recognizeEntitiesForFaultyText() {
        // TODO: (savaity) confirm with service team this returns no error-ed document, no exception but empty documents and error list.
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

    @Test
    public void recognizeLinkedEntitiesForTextInput() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch().setText("Seattle").setLength(7).setOffset(26).setScore(0.11472424095537814);
        LinkedEntity linkedEntity1 = new LinkedEntity().setName("Seattle").setUrl("https://en.wikipedia.org/wiki/Seattle").setDataSource("Wikipedia").setLinkedEntityMatches(Collections.singletonList(linkedEntityMatch1)).setLanguage("en").setId("Seattle");
        LinkedEntityResult linkedEntityResultList = new LinkedEntityResult("0", null, null, Collections.singletonList(linkedEntity1));

        StepVerifier.create(client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week."))
            .assertNext(response -> validateLinkedEntities(linkedEntityResultList.getLinkedEntities(), response.getLinkedEntities()))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        StepVerifier.create(client.recognizeLinkedEntities(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    @Test
    public void recognizeLinkedEntitiesForFaultyText() {

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

    @Override
    public void recognizeKeyPhrasesForTextInput() {

    }

    @Override
    public void recognizeKeyPhrasesForEmptyText() {

    }

    @Override
    public void recognizeKeyPhrasesForFaultyText() {

    }

    @Override
    public void recognizeKeyPhrasesForBatchInput() {

    }

    @Override
    public void recognizeKeyPhrasesForBatchInputShowStatistics() {

    }

    @Override
    public void recognizeKeyPhrasesForBatchStringInput() {

    }

    @Override
    public void recognizeKeyPhrasesForListLanguageHint() {

    }

    // Pii Entities
    @Test
    public void recognizePiiEntitiesForTextInput() {
        NamedEntity namedEntity1 = new NamedEntity().setText("859-98-0987").setType("U.S. Social Security Number (SSN)").setSubtype("").setOffset(28).setLength(11).setScore(0.65);
        NamedEntityResult namedEntityResultList = new NamedEntityResult("0", null, null, Collections.singletonList(namedEntity1));

        StepVerifier.create(client.recognizePiiEntities("Microsoft employee with ssn 859-98-0987 is using our awesome API's."))
            .assertNext(response -> validateNamedEntities(namedEntityResultList.getNamedEntities(), response.getNamedEntities()))
            .verifyComplete();
    }

    @Test
    public void recognizePiiEntitiesForEmptyText() {
        Error expectedError = new Error().setCode("InvalidArgument").setMessage("Invalid document in request.");
        StepVerifier.create(client.recognizePiiEntities(""))
            .assertNext(response -> validateErrorDocument(expectedError, response.getError()))
            .verifyComplete();
    }

    @Override
    public void recognizePiiEntitiesForFaultyText() {
        // TODO: (savaity) confirm with service team this returns no error-ed document, no exception but empty documents and error list.
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
}
