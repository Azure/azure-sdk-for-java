// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.Error;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {

    private TextAnalyticsClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new TextAnalyticsClientBuilder()
            .endpoint(getEndPoint())
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
            getExpectedBatchDetectedLanguages(), "Language"));
    }

    /**
     * Test Detect batch input languages.
     */
    @Test
    public void detectLanguagesBatchInput() {
        detectLanguageRunner((inputs) -> validateBatchResult(client.detectBatchLanguages(inputs),
            getExpectedBatchDetectedLanguages(), "Language"));
    }

    /**
     * Test Detect batch languages for List of String input with country Hint.
     */
    @Test
    public void detectLanguagesBatchListCountryHint() {
        detectLanguagesCountryHintRunner((inputs, countryHint) -> validateBatchResult(
            client.detectLanguagesWithResponse(inputs, countryHint, Context.NONE).getValue(),
            getExpectedBatchDetectedLanguages(), "Language"));
    }

    /**
     * Test Detect batch languages for List of String input.
     */
    @Test
    public void detectLanguagesBatchStringInput() {
        detectLanguageStringInputRunner((inputs) -> validateBatchResult(client.detectLanguages(inputs),
            getExpectedBatchDetectedLanguages(), "Language"));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a text input to detectLanguages.
     */
    @Test
    public void detectSingleTextLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage().setName("English").setIso6391Name("en").setScore(1.0);
        List<DetectedLanguage> expectedLanguageList = new ArrayList<>(Arrays.asList(primaryLanguage));
        validateDetectedLanguages(
            client.detectLanguage("This is a test English Text").getDetectedLanguages(), expectedLanguageList);
    }

    /**
     * Verifies that an exception is thrown when null text is passed.
     */
    @Test
    public void detectLanguagesNullInput() {
        assertRunnableThrowsException(() -> client.detectBatchLanguagesWithResponse(null, null,
            Context.NONE).getValue(), HttpResponseException.class);
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
}
