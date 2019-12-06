// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.cs.textanalytics.models.DetectedLanguage;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TextAnalyticsAsyncClientTest extends TextAnalyticsClientTestBase{
    private TextAnalyticsAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
        client = clientSetup(httpPipeline -> new TextAnalyticsClientBuilder()
            .endpoint(getEndPoint())
            .pipeline(httpPipeline)
            .buildAsyncClient());
    }

    /**
     * Test Detect batch input langugaes with show statistics.
     */
    @Test
    public void detectLanguagesBatchInputShowStatistics() {
        detectLanguageShowStatisticsRunner((inputs, options) -> {
            StepVerifier.create(client.detectBatchLanguages(inputs, options))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), "Language"))
                .verifyComplete();
        });
    }

    /**
     * Test Detect batch input langugaes with show statistics.
     */
    @Test
    public void detectLanguagesBatchInputShowStatisticsNew() {
        detectLanguageShowStatisticsRunner((inputs, options) -> {
            StepVerifier.create(client.detectBatchLanguages(inputs, options))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), "Language"))
                .verifyComplete();
        });
    }

    /**
     * Verifies that a batch collection is returned on batch input for detectLanguages.
     */
    @Test
    public void detectLanguagesBatchStringList() {
        detectLanguagesCountryHintRunner((inputs, countryHint) -> {
            StepVerifier.create(client.detectLanguages(inputs, countryHint))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), "Language"))
                .verifyComplete();
        });
    }

    /**
     * Verifies that a batch collection is returned on batch input for detectLanguages.
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
     * Verifies that a batch collection is returned on batch input for detectLanguages.
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
     * Verifies that a Null pointer exception is thrown when null text is passed.
     */
    @Test
    public void detectLanguagesNullInput() {
        detectLanguageRunner((inputs) -> {
            StepVerifier.create(client.detectBatchLanguagesWithResponse(null, null))
                .verifyError(NullPointerException.class);
        });
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a single text input to detectLanguages.
     *
     */
    @Test
    public void detectLanguage() {
        DetectedLanguage primaryLanguage = new DetectedLanguage().setName("English").setIso6391Name("en").setScore(1.0);
        List<DetectedLanguage> expectedLanguageList = new ArrayList<>(Arrays.asList(primaryLanguage));
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(expectedLanguageList, response.getDetectedLanguages()))
            .verifyComplete();
    }

    /**
     * Verifies that a single error DetectLanguageResult is returned for a single text input with invalid country hint.
     *
     * TODO: update error Model. #6559
     */
    // @Test
    // public void detectLanguageInvalidCountryHint() {
    //     DetectedLanguage primaryLanguage = new DetectedLanguage().setName("English").setIso6391Name("en").setScore(1.0);
    //     Error error = new Error().setCode("Invalid Hint");
    //     StepVerifier.create(client.detectLanguage("Este es un document escrito en Español.", "en"))
    //         .assertNext(response -> assertEquals(error.getCode(), ((Error)response.getError()).getCode()))
    //         .verifyComplete();
    // }

    /**
     * Verifies that a single DetectLanguageResult is returned for a single text input with country hint.
     *
     * TODO: update error Model. #6559
     */
    // @Test
    // public void detectLanguageCountryHint() {
    //     DetectedLanguage primaryLanguage = new DetectedLanguage().setName("Spanish").setIso6391Name("es").setScore(1.0);
    //     Error error = new Error().setCode("Invalid Hint");
    //     StepVerifier.create(client.detectLanguage("Este es un document escrito en Español.", "es"))
    //         .assertNext(response -> assertEquals(error.getCode(), ((Error)response.getError()).getCode()))
    //         .verifyComplete();
    // }

    /**
     * Verifies that the error result is returned when empty text is passed.
     */
    @Test
    public void detectLanguageEmptyText() {
        StepVerifier.create(client.detectLanguage(""))
            .assertNext(response -> assertNotNull(response.getError()))
            .verifyComplete();
    }

    /**
     * Verifies that it returns an exception is thrown when null text is passed.
     */
    @Test
    public void detectLanguageNullText() {
        StepVerifier.create(client.detectLanguage(null)).verifyError(NullPointerException.class);
    }

    /**
     * Verifies that an document returns with an error when error text is passed.
     */
    @Test
    public void detectLanguageFaultyText() {
        StepVerifier.create(client.detectLanguage("!@#%%"))
            .assertNext(response ->
                assertEquals(response.getPrimaryLanguage().getIso6391Name(), "(Unknown)"))
            .verifyComplete();
    }
}
