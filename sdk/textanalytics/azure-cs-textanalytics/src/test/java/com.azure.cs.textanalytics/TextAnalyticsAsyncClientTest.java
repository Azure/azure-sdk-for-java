// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.RetryPolicy;
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
     * Verify that we can get statistics on the collection result when given a batch input with options.
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
            StepVerifier.create(client.detectLanguages(inputs, countryHint))
                .assertNext(response -> validateBatchResult(response, getExpectedBatchDetectedLanguages(), "Language"))
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
     *
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
     * Verifies that an error DetectLanguageResult is returned for a text input with invalid country hint.
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
    //         .assertNext(response -> assertEquals(error, ((Error)response.getError()).getCode()))
    //         .verifyComplete();
    // }

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
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @Test
    public void detectLanguageFaultyText() {
        StepVerifier.create(client.detectLanguage("!@#%%"))
            .assertNext(response ->
                assertEquals(response.getPrimaryLanguage().getIso6391Name(), "(Unknown)"))
            .verifyComplete();
    }

    // TODO: add with response tests
}
