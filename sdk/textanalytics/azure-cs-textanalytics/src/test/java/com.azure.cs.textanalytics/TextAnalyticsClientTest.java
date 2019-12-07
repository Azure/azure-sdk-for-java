// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.util.logging.ClientLogger;
import com.azure.cs.textanalytics.models.DetectLanguageResult;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {

    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientTest.class);

    private TextAnalyticsClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
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
        detectLanguageShowStatisticsRunner((inputs, options) ->
            validateBatchResult(client.detectBatchLanguages(inputs, options), getExpectedBatchDetectedLanguages(),
                "Language"));
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
            client.detectLanguages(inputs, countryHint), getExpectedBatchDetectedLanguages(), "Language"));
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
     *
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
        assertRunnableThrowsException(() -> client.detectBatchLanguages(null, null), NullPointerException.class);
    }

    /**
     * Verifies that the error result is returned when empty text is passed.
     */
    @Test
    public void detectLanguageEmptyText() {
        DetectLanguageResult result = client.detectLanguage("");
        assertNotNull(result.getError());
    }

    /**
     * Verifies that it returns an exception is thrown when null text is passed.
     */
    @Test
    public void detectLanguageNullText() {
        assertRunnableThrowsException(() -> client.detectLanguage(null), NullPointerException.class);
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @Test
    public void detectLanguageFaultyText() {
        DetectLanguageResult result = client.detectLanguage("!@#%%");
        assertEquals(result.getPrimaryLanguage().getIso6391Name(), "(Unknown)");
    }
}
