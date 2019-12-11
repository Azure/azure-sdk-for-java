// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.Error;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextAnalyticsAsyncClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsAsyncClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new TextAnalyticsClientBuilder()
            .endpoint(getEndPoint())
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
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchDetectedLanguages(), "Language"))
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
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), "Language"))
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
                .assertNext(response -> validateBatchResult(response.getValue(), getExpectedBatchDetectedLanguages(), "Language"))
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
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), "Language"))
                .verifyComplete();
        });
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a text input to detectLanguages.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage().setName("English").setIso6391Name("en").setScore(1.0);
        List<DetectedLanguage> expectedLanguageList = new ArrayList<>(Arrays.asList(primaryLanguage));
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
}
