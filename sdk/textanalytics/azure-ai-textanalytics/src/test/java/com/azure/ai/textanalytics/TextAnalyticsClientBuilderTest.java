// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.FAKE_API_KEY;
import static com.azure.ai.textanalytics.TestUtils.VALID_HTTPS_LOCALHOST;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validateKeyPhrases;
import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validatePrimaryLanguage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Text Analytics client builder
 */
public class TextAnalyticsClientBuilderTest extends TextAnalyticsClientBuilderTestBase {

    /**
     * Test client builder with valid API key
     */
    @Test
    public void clientBuilderWithValidApiKeyCredential() {
        clientBuilderWithValidApiKeyCredentialRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client builder with invalid API key
     */
    @Test
    public void clientBuilderWithInvalidApiKeyCredential() {
        clientBuilderWithInvalidApiKeyCredentialRunner(clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client with valid API key but update to invalid key and make call to server.
     */
    @Test
    public void clientBuilderWithRotateToInvalidKey() {
        clientBuilderWithRotateToInvalidKeyRunner(clientBuilder -> (input, output) ->
            assertThrows(output.getClass(), () -> clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test client with invalid API key but update to valid key and make call to server.
     */
    @Test
    public void clientBuilderWithRotateToValidKey() {
        clientBuilderWithRotateToValidKeyRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @Test
    public void clientBuilderWithNullServiceVersion() {
        clientBuilderWithNullServiceVersionRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default pipeline in client builder
     */
    @Test
    public void clientBuilderWithDefaultPipeline() {
        clientBuilderWithDefaultPipelineRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default country hint in client builder for a single document
     */
    @Test
    public void clientBuilderWithDefaultCountryHint() {
        clientBuilderWithDefaultCountryHintRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input)));
    }

    /**
     * Test for default country hint in client builder for a single document
     */
    @Test
    public void clientBuilderWithNewCountryHint() {
        clientBuilderWithDefaultCountryHintRunner(clientBuilder -> (input, output) ->
            validatePrimaryLanguage(output, clientBuilder.buildClient().detectLanguage(input, "MX")));
    }

    /**
     * Test for default country hint in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithDefaultCountryHintForBatchOperation() {
        clientBuilderWithDefaultCountryHintForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<DetectLanguageResult> result =
                clientBuilder.buildClient().detectLanguageBatch(input).stream().collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validatePrimaryLanguage(output.get(i), result.get(i).getPrimaryLanguage());
            }
        });
    }

    /**
     * Test for default country hint in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithNewCountryHintForBatchOperation() {
        clientBuilderWithNewCountryHintForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<DetectLanguageResult> result =
                clientBuilder.buildClient().detectLanguageBatch(input, "US").stream().collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validatePrimaryLanguage(output.get(i), result.get(i).getPrimaryLanguage());
            }
        });
    }

    /**
     * Test for default language in client builder for single document
     */
    @Test
    public void clientBuilderWithDefaultLanguage() {
        clientBuilderWithDefaultLanguageRunner(clientBuilder -> (input, output) ->
            assertEquals(output, clientBuilder.buildClient().extractKeyPhrases(input).iterator().next()));
    }

    /**
     * Test for default language in client builder for single document
     */
    @Test
    public void clientBuilderWithNewLanguage() {
        clientBuilderWithNewLanguageRunner(clientBuilder -> (input, output) ->
            assertEquals(output, clientBuilder.buildClient().extractKeyPhrases(input, "EN").iterator().next()));
    }

    /**
     * Test for default language in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithDefaultLanguageForBatchOperation() {
        clientBuilderWithDefaultLanguageForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<ExtractKeyPhraseResult> result =
                clientBuilder.buildClient().extractKeyPhrasesBatch(input).stream().collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validateKeyPhrases(output.get(i), result.get(i).getKeyPhrases().stream().collect(Collectors.toList()));
            }
        });
    }

    /**
     * Test for default language in client builder for a batch of documents
     */
    @Test
    public void clientBuilderWithNewLanguageForBatchOperation() {
        clientBuilderWithNewLanguageForBatchOperationRunner(clientBuilder -> (input, output) -> {
            final List<ExtractKeyPhraseResult> result =
                clientBuilder.buildClient().extractKeyPhrasesBatch(input, "EN").stream()
                    .collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                validateKeyPhrases(output.get(i), result.get(i).getKeyPhrases().stream().collect(Collectors.toList()));
            }
        });
    }
}
