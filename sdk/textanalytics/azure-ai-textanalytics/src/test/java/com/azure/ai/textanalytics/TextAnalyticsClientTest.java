// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.getCategorizedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.textanalytics.TestUtils.FAKE_API_KEY;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static com.azure.ai.textanalytics.models.WarningCode.LONG_WORDS_IN_DOCUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsClient client;

    private TextAnalyticsClient getTextAnalyticsClient(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(FAKE_API_KEY));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder.buildClient();
    }
    // Detect language

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageShowStatisticsRunner((inputs, options) -> validateDetectLanguage(true,
            getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, options, Context.NONE).streamByPage().findFirst().get()));
    }

    /**
     * Test Detect batch of documents languages.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageRunner((inputs) -> validateDetectLanguage(false,
            getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs, null, Context.NONE).streamByPage().findFirst().get()));
    }

    /**
     * Test detect batch languages for a list of string input with country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguagesCountryHintRunner((inputs, countryHint) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, countryHint).streamByPage().findFirst().get()));
    }

    /**
     * Test detect batch languages for a list of string input with request options
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHintWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) -> validateDetectLanguage(true,
            getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, null, options).streamByPage().findFirst().get()));
    }

    /**
     * Test detect batch languages for a list of string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageStringInputRunner((inputs) -> validateDetectLanguage(
            false, getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs).streamByPage().findFirst().get()));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a document to detect language.
     */
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        validatePrimaryLanguage(new DetectedLanguage("English", "en", 0.0, null),
            client.detectLanguage("This is a test English Text"));
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.detectLanguage(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        DetectedLanguage primaryLanguage = new DetectedLanguage("(Unknown)", "(Unknown)", 0.0, null);
        validatePrimaryLanguage(client.detectLanguage("!@#%%"), primaryLanguage);
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageInvalidCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        Exception exception = assertThrows(TextAnalyticsException.class, () ->
            client.detectLanguage("Este es un documento  escrito en Español.", "en"));
        assertTrue(exception.getMessage().equals(INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        validatePrimaryLanguage(new DetectedLanguage("Spanish", "es", 0.0, null),
            client.detectLanguage("Este es un documento  escrito en Español", ""));
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        validatePrimaryLanguage(new DetectedLanguage("Spanish", "es", 0.0, null),
            client.detectLanguage("Este es un documento  escrito en Español", "none"));
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same IDs.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageDuplicateIdRunner((inputs, options) -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.detectLanguageBatch(inputs, options, Context.NONE).stream().findFirst().get());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    // Recognize Entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        final List<CategorizedEntity> entities = client.recognizeEntities("I had a wonderful trip to Seattle last week.").stream().collect(Collectors.toList());
        validateCategorizedEntities(getCategorizedEntitiesList1(), entities);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizeEntities("").iterator().hasNext());
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        assertFalse(client.recognizeEntities("!@#%%").iterator().hasNext());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeCategorizedEntityDuplicateIdRunner(inputs -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizeEntitiesBatch(inputs, null, Context.NONE).stream().findFirst().get());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) -> {
            TextAnalyticsPagedIterable<RecognizeEntitiesResult> response = client.recognizeEntitiesBatch(inputs, null, Context.NONE);
            response.forEach(recognizeEntitiesResult -> {
                Exception exception = assertThrows(TextAnalyticsException.class, recognizeEntitiesResult::getEntities);
                assertEquals(exception.getMessage(), BATCH_ERROR_EXCEPTION_MESSAGE);
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntityRunner((inputs) ->
            client.recognizeEntitiesBatch(inputs, null, Context.NONE).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeEntitiesBatch(inputs, options, Context.NONE).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeCategorizedEntityStringInputRunner((inputs) -> client.recognizeEntitiesBatch(inputs).iterableByPage()
            .forEach(pagedResponse ->
                validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            client.recognizeEntitiesBatch(inputs, language).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeEntitiesBatch(inputs, null, options).iterableByPage().forEach(
                pagedResponse ->
                    validateCategorizedEntitiesWithPagedResponse(false, getExpectedBatchCategorizedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeEntitiesTooManyDocumentsRunner(inputs -> {
            HttpResponseException exception = assertThrows(HttpResponseException.class,
                () -> client.recognizeEntitiesBatch(inputs).stream().findFirst().get());
            assertEquals(EXCEEDED_ALLOWED_DOCUMENTS_LIMITS_MESSAGE, exception.getMessage());
            assertEquals(INVALID_DOCUMENT_BATCH, exception.getValue().toString());
        });
    }

    // Recognize linked entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        final LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("Seattle", 0.0);
        final LinkedEntity linkedEntity1 = new LinkedEntity("Seattle",
            new IterableStream<>(Collections.singletonList(linkedEntityMatch1)),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle", "Wikipedia");
        final List<LinkedEntity> linkedEntities = client.recognizeLinkedEntities("I had a wonderful trip to Seattle last week.")
            .stream().collect(Collectors.toList());
        validateLinkedEntity(linkedEntity1, linkedEntities.get(0));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.recognizeLinkedEntities("").iterator().hasNext());
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        assertFalse(client.recognizeLinkedEntities("!@#%%").iterator().hasNext());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityDuplicateIdRunner(inputs -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizeLinkedEntitiesBatch(inputs, null, Context.NONE).stream().findFirst().get());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityRunner((inputs) ->
            client.recognizeLinkedEntitiesBatch(inputs, null, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeLinkedEntitiesBatch(inputs, options, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeLinkedStringInputRunner((inputs) ->
            client.recognizeLinkedEntitiesBatch(inputs).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            client.recognizeLinkedEntitiesBatch(inputs, language).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(false, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            client.recognizeLinkedEntitiesBatch(inputs, null, options).iterableByPage().forEach(pagedResponse ->
                validateLinkedEntitiesWithPagedResponse(true, getExpectedBatchLinkedEntities(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeLinkedEntitiesTooManyDocumentsRunner(inputs -> {
            HttpResponseException exception = assertThrows(HttpResponseException.class,
                () -> client.recognizeLinkedEntitiesBatch(inputs).stream().findFirst().get());
            assertEquals(EXCEEDED_ALLOWED_DOCUMENTS_LIMITS_MESSAGE, exception.getMessage());
            assertEquals(INVALID_DOCUMENT_BATCH, exception.getValue().toString());
        });
    }

    // Extract key phrase

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        assertEquals("monde", client.extractKeyPhrases("Bonjour tout le monde.").iterator().next());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.extractKeyPhrases("").iterator().hasNext());
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        assertFalse(client.extractKeyPhrases("!@#%%").iterator().hasNext());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesDuplicateIdRunner(inputs -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.extractKeyPhrasesBatch(inputs, null, Context.NONE).stream().findFirst().get());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesRunner((inputs) ->
            client.extractKeyPhrasesBatch(inputs, null, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            client.extractKeyPhrasesBatch(inputs, options, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractKeyPhrasesStringInputRunner((inputs) ->
            client.extractKeyPhrasesBatch(inputs).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            client.extractKeyPhrasesBatch(inputs, language).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(false, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            client.extractKeyPhrasesBatch(inputs, null, options).iterableByPage().forEach(pagedResponse ->
                validateExtractKeyPhraseWithPagedResponse(true, getExpectedBatchKeyPhrases(), pagedResponse)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractKeyPhrasesWarningRunner(input ->
            client.extractKeyPhrases(input).getWarnings().forEach(warning -> {
                assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
            }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesBatchWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractKeyPhrasesBatchWarningRunner(inputs ->
            client.extractKeyPhrasesBatch(inputs, null, Context.NONE).forEach(keyPhrasesResult ->
                keyPhrasesResult.getKeyPhrases().getWarnings().forEach(warning -> {
                    assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                    assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
                })
            ));
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(
            TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment("", TextSentiment.NEGATIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                new SentenceSentiment("", TextSentiment.POSITIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0))
            )), null);
        DocumentSentiment analyzeSentimentResult =
            client.analyzeSentiment("The hotel was dark and unclean. The restaurant had amazing gnocchi.");

        validateAnalyzedSentiment(expectedDocumentSentiment, analyzeSentimentResult);
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        Exception exception = assertThrows(TextAnalyticsException.class, () -> client.analyzeSentiment(""));
        assertTrue(exception.getMessage().equals(INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE));
    }

    /**
     * Test analyzing sentiment for a faulty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(TextSentiment.NEUTRAL,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment("", TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                new SentenceSentiment("", TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0))
            )), null);

        DocumentSentiment analyzeSentimentResult = client.analyzeSentiment("!@#%%");

        validateAnalyzedSentiment(expectedDocumentSentiment, analyzeSentimentResult);
    }

    /**
     * Test analyzing sentiment for a duplicate ID list.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyseBatchSentimentDuplicateIdRunner(inputs -> {
            HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.analyzeSentimentBatch(inputs, null, Context.NONE).stream().findFirst().get());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    /**
     * Test analyzing sentiment for a list of string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyseSentimentStringInputRunner(inputs ->
            client.analyzeSentimentBatch(inputs).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Test analyzing sentiment for a list of string input with language code.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyseSentimentLanguageHintRunner((inputs, language) ->
            client.analyzeSentimentBatch(inputs, language).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyseBatchStringSentimentShowStatsRunner((inputs, options) ->
            client.analyzeSentimentBatch(inputs, null, options).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Test analyzing sentiment for batch of documents.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyseBatchSentimentRunner(inputs ->
            client.analyzeSentimentBatch(inputs, null, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(false, getExpectedBatchTextSentiment(), pagedResponse)));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyseSentimentForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyseBatchSentimentShowStatsRunner((inputs, options) ->
            client.analyzeSentimentBatch(inputs, options, Context.NONE).iterableByPage().forEach(pagedResponse ->
                validateSentimentWithPagedResponse(true, getExpectedBatchTextSentiment(), pagedResponse)));
    }
}
