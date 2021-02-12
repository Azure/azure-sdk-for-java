// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeBatchActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeBatchActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeBatchActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiEntityDomainType;
import com.azure.ai.textanalytics.models.RecognizeEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.StringIndexType;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.CATEGORIZED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.textanalytics.TestUtils.ENTITY_TASK;
import static com.azure.ai.textanalytics.TestUtils.HEALTHCARE_ENTITY_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASES_TASK;
import static com.azure.ai.textanalytics.TestUtils.LINKED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.PII_ENTITY_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.PII_TASK;
import static com.azure.ai.textanalytics.TestUtils.SENTIMENT_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.TIME_NOW;
import static com.azure.ai.textanalytics.TestUtils.getActionError;
import static com.azure.ai.textanalytics.TestUtils.getCategorizedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageEnglish;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageSpanish;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeBatchActionsResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeTaskResultListForMultiplePages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntitiesForDomainFilter;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static com.azure.ai.textanalytics.TestUtils.getExpectedDocumentSentiment;
import static com.azure.ai.textanalytics.TestUtils.getExpectedExtractKeyPhrasesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedHealthcareTaskResultListForMultiplePages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedHealthcareTaskResultListForSinglePage;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizeEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizePiiEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getLinkedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getPiiEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeEntitiesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getRecognizePiiEntitiesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getUnknownDetectedLanguage;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_COUNTRY_HINT;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_DOCUMENT;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_DOCUMENT_BATCH;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_REQUEST;
import static com.azure.ai.textanalytics.models.WarningCode.LONG_WORDS_IN_DOCUMENT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsClient client;

    private TextAnalyticsClient getTextAnalyticsClient(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        return getTextAnalyticsAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }
    // Detect language

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageShowStatisticsRunner((inputs, options) -> validateDetectLanguageResultCollectionWithResponse(true,
            getExpectedBatchDetectedLanguages(), 200,
            client.detectLanguageBatchWithResponse(inputs, options, Context.NONE)));
    }

    /**
     * Test Detect batch of documents languages.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageRunner((inputs) -> validateDetectLanguageResultCollectionWithResponse(false,
            getExpectedBatchDetectedLanguages(), 200,
            client.detectLanguageBatchWithResponse(inputs, null, Context.NONE)));
    }

    /**
     * Test detect batch languages for a list of string input with country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguagesCountryHintRunner((inputs, countryHint) -> validateDetectLanguageResultCollection(
            false, getExpectedBatchDetectedLanguages(),
            client.detectLanguageBatch(inputs, countryHint, null)));
    }

    /**
     * Test detect batch languages for a list of string input with request options
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHintWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) -> validateDetectLanguageResultCollection(true,
            getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs, null, options)));
    }

    /**
     * Test detect batch languages for a list of string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageStringInputRunner((inputs) -> validateDetectLanguageResultCollection(
            false, getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs, null, null)));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a document to detect language.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectSingleTextLanguageRunner(input ->
            validatePrimaryLanguage(getDetectedLanguageEnglish(), client.detectLanguage(input)));
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.detectLanguage(input));
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        faultyTextRunner(input -> validatePrimaryLanguage(client.detectLanguage(input), getUnknownDetectedLanguage()));
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same IDs.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageDuplicateIdRunner((inputs, options) -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.detectLanguageBatchWithResponse(inputs, options, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    /**
     * Verifies that an invalid document exception is returned for input documents with an empty ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageInputEmptyIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.detectLanguageBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageInvalidCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageInvalidCountryHintRunner((input, countryHint) -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.detectLanguage(input, countryHint));
            assertEquals(INVALID_COUNTRY_HINT, exception.getErrorCode());
        });
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageEmptyCountryHintRunner((input, countryHint) ->
            validatePrimaryLanguage(getDetectedLanguageSpanish(), client.detectLanguage(input, countryHint)));
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        detectLanguageNoneCountryHintRunner((input, countryHint) ->
            validatePrimaryLanguage(getDetectedLanguageSpanish(), client.detectLanguage(input, countryHint)));
    }

    // Recognize Entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesForSingleTextInputRunner(input -> {
            final List<CategorizedEntity> entities = client.recognizeEntities(input).stream().collect(Collectors.toList());
            validateCategorizedEntities(getCategorizedEntitiesList1(), entities);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.recognizeEntities(input).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        faultyTextRunner(input -> assertFalse(client.recognizeEntities(input).iterator().hasNext()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeCategorizedEntityDuplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) -> {
            Response<RecognizeEntitiesResultCollection> response = client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE);
            response.getValue().forEach(recognizeEntitiesResult -> {
                Exception exception = assertThrows(TextAnalyticsException.class, recognizeEntitiesResult::getEntities);
                assertEquals(String.format(BATCH_ERROR_EXCEPTION_MESSAGE, "RecognizeEntitiesResult"), exception.getMessage());
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntityRunner((inputs) ->
            validateCategorizedEntitiesResultCollectionWithResponse(false, getExpectedBatchCategorizedEntities(), 200,
                client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            validateCategorizedEntitiesResultCollectionWithResponse(true, getExpectedBatchCategorizedEntities(), 200,
                client.recognizeEntitiesBatchWithResponse(inputs, options, Context.NONE))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatisticsWithRecognizeEntitiesOption(
        HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitiesShowStatsWithRecognizeEntitiesOptionRunner(
            (inputs, options) -> validateCategorizedEntitiesResultCollectionWithResponse(true,
                getExpectedBatchCategorizedEntities(), 200,
                client.recognizeEntitiesBatchWithResponse(inputs, options, Context.NONE))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatch(inputs, null, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatch(inputs, language, null))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            validateCategorizedEntitiesResultCollection(true, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatch(inputs, null, options))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.recognizeEntitiesBatch(inputs, null, null).stream().findFirst().get());
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchWithResponseEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        batchEmojiRunner(
            documents -> client.recognizeEntitiesBatchWithResponse(documents,
                new RecognizeEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT), Context.NONE)
                             .getValue().forEach(result -> result.getEntities().forEach(
                                 categorizedEntity -> {
                                     assertEquals(9, categorizedEntity.getLength());
                                     assertEquals(12, categorizedEntity.getOffset());
                                 })),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(15, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(22, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(30, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfcRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(14, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfdRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(15, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfcRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfdRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        zalgoTextRunner(document ->
            client.recognizeEntities(document).forEach(
                categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(126, categorizedEntity.getOffset());
                }),
            CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    // Recognize Personally Identifiable Information entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizePiiSingleDocumentRunner(document -> {
            final PiiEntityCollection entities = client.recognizePiiEntities(document);
            validatePiiEntities(getPiiEntitiesList1(), entities.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyTextRunner(document -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class, () ->
                client.recognizePiiEntities(document).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        faultyTextRunner(document -> assertFalse(client.recognizePiiEntities(document).iterator().hasNext()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchPiiEntityDuplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitySingleErrorRunner((inputs) -> {
            Response<RecognizePiiEntitiesResultCollection> response = client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE);
            response.getValue().forEach(recognizePiiEntitiesResult -> {
                Exception exception = assertThrows(TextAnalyticsException.class, recognizePiiEntitiesResult::getEntities);
                assertEquals(String.format(BATCH_ERROR_EXCEPTION_MESSAGE, "RecognizePiiEntitiesResult"), exception.getMessage());
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitiesRunner(inputs ->
            validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntities(), 200,
                client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            validatePiiEntitiesResultCollectionWithResponse(true, getExpectedBatchPiiEntities(), 200,
                client.recognizePiiEntitiesBatchWithResponse(inputs, options, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizePiiEntitiesLanguageHintRunner((inputs, language) ->
            validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesBatch(inputs, language, null))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeStringBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            validatePiiEntitiesResultCollection(true, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesBatch(inputs, null, options)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.recognizePiiEntitiesBatch(inputs, null, null).stream().findFirst().get());
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(8, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchWithResponseEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        batchEmojiRunner(
            documents -> client.recognizePiiEntitiesBatchWithResponse(documents,
                new RecognizePiiEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT), Context.NONE)
                             .getValue().forEach(result -> result.getEntities().forEach(
                                 piiEntity -> {
                                     assertEquals(11, piiEntity.getLength());
                                     assertEquals(7, piiEntity.getOffset());
                                 })),
            PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(10, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(17, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(25, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfcRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(9, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfdRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(10, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfcRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(8, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfdRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(8, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        zalgoTextRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(121, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizePiiDomainFilterRunner((document, options) -> {
            final PiiEntityCollection entities = client.recognizePiiEntities(document, "en", options);
            validatePiiEntities(Arrays.asList(getPiiEntitiesList1().get(1)), entities.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputStringForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizePiiLanguageHintRunner((inputs, language) -> {
            final RecognizePiiEntitiesResultCollection response = client.recognizePiiEntitiesBatch(inputs, language,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomainType.PROTECTED_HEALTH_INFORMATION));
            validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntitiesForDomainFilter(), response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitiesRunner((inputs) -> {
            final Response<RecognizePiiEntitiesResultCollection> response = client.recognizePiiEntitiesBatchWithResponse(inputs,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomainType.PROTECTED_HEALTH_INFORMATION), Context.NONE);
            validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntitiesForDomainFilter(), 200, response);
        });
    }

    // Recognize linked entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeLinkedEntitiesForSingleTextInputRunner(input -> {
            final List<LinkedEntity> linkedEntities = client.recognizeLinkedEntities(input)
                .stream().collect(Collectors.toList());
            validateLinkedEntity(getLinkedEntitiesList1().get(0), linkedEntities.get(0));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.recognizeLinkedEntities(input).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            assertFalse(client.recognizeLinkedEntities(input).iterator().hasNext()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityDuplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizeLinkedEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.recognizeLinkedEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityRunner((inputs) ->
            validateLinkedEntitiesResultCollectionWithResponse(false, getExpectedBatchLinkedEntities(), 200,
                client.recognizeLinkedEntitiesBatchWithResponse(inputs, null, Context.NONE))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            validateLinkedEntitiesResultCollectionWithResponse(true, getExpectedBatchLinkedEntities(), 200,
                client.recognizeLinkedEntitiesBatchWithResponse(inputs, options, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatisticsWithRecognizeLinkedEntitiesOption(
        HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntitiesShowStatsWithRecognizeLinkedEntitiesOptionsRunner(
            (inputs, options) ->
                validateLinkedEntitiesResultCollectionWithResponse(true, getExpectedBatchLinkedEntities(), 200,
                    client.recognizeLinkedEntitiesBatchWithResponse(inputs, options, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeLinkedStringInputRunner((inputs) ->
            validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatch(inputs, null, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatch(inputs, language, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            validateLinkedEntitiesResultCollection(true, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatch(inputs, null, options)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.recognizeLinkedEntitiesBatch(inputs, null, null).stream().findFirst().get());
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity -> linkedEntity.getMatches().forEach(
                    linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesBatchWithResponseEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        batchEmojiRunner(
            documents ->
                client.recognizeLinkedEntitiesBatchWithResponse(documents,
                    new RecognizeLinkedEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT),
                    Context.NONE)
                    .getValue()
                    .forEach(recognizeLinkedEntitiesResult -> recognizeLinkedEntitiesResult.getEntities().forEach(
                        linkedEntity -> linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                            assertEquals(9, linkedEntityMatch.getLength());
                            assertEquals(12, linkedEntityMatch.getOffset());
                        }))),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity -> linkedEntity.getMatches().forEach(
                    linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(15, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity -> linkedEntity.getMatches().forEach(
                    linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(22, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(document ->
            client.recognizeLinkedEntities(document).forEach(linkedEntity ->
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(30, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfcRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity -> linkedEntity.getMatches().forEach(
                    linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(14, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfdRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity -> linkedEntity.getMatches().forEach(
                    linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(15, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfcRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity -> linkedEntity.getMatches().forEach(
                    linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfdRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity -> linkedEntity.getMatches().forEach(
                    linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        zalgoTextRunner(document ->
            client.recognizeLinkedEntities(document).forEach(
                linkedEntity ->
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(126, linkedEntityMatch.getOffset());
                    })),
            LINKED_ENTITY_INPUTS.get(1)
        );
    }

    // Extract key phrase

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractKeyPhrasesForSingleTextInputRunner(input ->
            assertEquals("monde",
                client.extractKeyPhrases(input).iterator().next()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.extractKeyPhrases(input).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        faultyTextRunner(input -> assertFalse(client.extractKeyPhrases(input).iterator().hasNext()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesDuplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.extractKeyPhrasesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.extractKeyPhrasesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesRunner((inputs) ->
            validateExtractKeyPhrasesResultCollectionWithResponse(false, getExpectedBatchKeyPhrases(), 200,
                client.extractKeyPhrasesBatchWithResponse(inputs, null, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            validateExtractKeyPhrasesResultCollectionWithResponse(true, getExpectedBatchKeyPhrases(), 200,
                client.extractKeyPhrasesBatchWithResponse(inputs, options, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractKeyPhrasesStringInputRunner((inputs) ->
            validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatch(inputs, null, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatch(inputs, language, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            validateExtractKeyPhrasesResultCollection(true, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatch(inputs, null, options)));
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
            client.extractKeyPhrasesBatchWithResponse(inputs, null, Context.NONE).getValue().forEach(keyPhrasesResult ->
                keyPhrasesResult.getKeyPhrases().getWarnings().forEach(warning -> {
                    assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                    assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
                })
            ));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.extractKeyPhrasesBatch(inputs, null, null).stream().findFirst().get());
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
        });
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeSentimentForSingleTextInputRunner(input -> {
            validateAnalyzedSentiment(false, getExpectedDocumentSentiment(), client.analyzeSentiment(input));
        });
    }

    /**
     * Test analyzing sentiment for a string input with default language hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithDefaultLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeSentimentForSingleTextInputRunner(input -> {
            final DocumentSentiment analyzeSentimentResult = client.analyzeSentiment(input, null);
            validateAnalyzedSentiment(false, getExpectedDocumentSentiment(), analyzeSentimentResult);
        });
    }

    /**
     * Test analyzing sentiment for a string input and verifying the result of opinion mining.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeSentimentForTextInputWithOpinionMiningRunner((input, options) -> {
            final DocumentSentiment analyzeSentimentResult =
                client.analyzeSentiment(input, "en", options);
            validateAnalyzedSentiment(true, getExpectedDocumentSentiment(), analyzeSentimentResult);
        });
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyTextRunner(document -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.analyzeSentiment(document));
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    /**
     * Test analyzing sentiment for a faulty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        faultyTextRunner(input -> {
            final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(
                TextSentiment.NEUTRAL,
                new SentimentConfidenceScores(0.0, 0.0, 0.0),
                new IterableStream<>(Arrays.asList(
                    new SentenceSentiment("!", TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0), null, 0),
                    new SentenceSentiment("@#%%", TextSentiment.NEUTRAL, new SentimentConfidenceScores(0.0, 0.0, 0.0), null, 1)
                )), null);
            validateAnalyzedSentiment(false, expectedDocumentSentiment, client.analyzeSentiment(input));
        });
    }

    /**
     * Test analyzing sentiment for a duplicate ID list.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchSentimentDuplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.analyzeSentimentBatchWithResponse(inputs, new TextAnalyticsRequestOptions(), Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    /**
     * Verifies that an invalid document exception is returned for input documents with an empty ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.analyzeSentimentBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    /**
     * Verify that the collection result excludes request statistics and mined options when given a batch of
     * String documents with null TextAnalyticsRequestOptions and null language code which will use the default language
     * code, 'en'.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions is null and null language code which will use the default language code, 'en'.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeSentimentStringInputRunner(inputs ->
            validateSentimentResultCollection(false, false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, new TextAnalyticsRequestOptions())));
    }

    /**
     * Verify that the collection result excludes request statistics and mined options when given a batch of
     * String documents with null TextAnalyticsRequestOptions and given a language code.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions is null and given a language code.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringWithLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeSentimentLanguageHintRunner((inputs, language) ->
            validateSentimentResultCollection(false, false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, language, new TextAnalyticsRequestOptions())));
    }

    /**
     * Verify that the collection result includes request statistics but not mined options when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which to show the request statistics only and verify the analyzed sentiment result.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            validateSentimentResultCollection(true, false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, options.setIncludeOpinionMining(false))));
    }

    /**
     * Verify that the collection result includes mined options but not request statistics when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) -> {
            options.setIncludeStatistics(false);
            validateSentimentResultCollection(false, true, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, options));
        });
    }

    /**
     * Verify that the collection result includes mined options and request statistics when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            validateSentimentResultCollection(true, true, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, options)));
    }

    /**
     * Verify that the collection result excludes request statistics and mined options when given a batch of
     * TextDocumentInput documents with null TextAnalyticsRequestOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     * which TextAnalyticsRequestOptions is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputWithNullRequestOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchSentimentRunner(inputs ->
            validateSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, (TextAnalyticsRequestOptions) null, Context.NONE)));
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of
     * TextDocumentInput documents with TextAnalyticsRequestOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     * which TextAnalyticsRequestOptions includes request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchSentimentShowStatsRunner((inputs, requestOptions) ->
            validateSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, requestOptions, Context.NONE)));
    }

    /**
     * Verify that the collection result excludes request statistics and mined options when given a batch of
     * TextDocumentInput documents with null AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputWithNullAnalyzeSentimentOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            validateSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, (AnalyzeSentimentOptions) null, Context.NONE)));
    }

    /**
     * Verify that the collection result includes request statistics but not mined options when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions includes request statistics but not opinion mining.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            validateSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, options.setIncludeOpinionMining(false), Context.NONE)));
    }

    /**
     * Verify that the collection result includes mined options but not request statistics when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions includes opinion mining but not request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) -> {
            options.setIncludeStatistics(false);
            validateSentimentResultCollectionWithResponse(false, true, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, options, Context.NONE));
        });
    }

    /**
     * Verify that the collection result includes mined options and request statistics when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            validateSentimentResultCollectionWithResponse(true, true, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, options, Context.NONE)));
    }

    /**
     * Verifies that an InvalidDocumentBatch exception is returned for input documents with too many documents.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.analyzeSentimentBatch(inputs, null, null).stream().findFirst().get());
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(
                        sentenceSentiment -> {
                            assertEquals(25, sentenceSentiment.getLength());
                            assertEquals(0, sentenceSentiment.getOffset());
                            sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                                minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                    assertEquals(7, opinionSentiment.getLength());
                                    assertEquals(17, opinionSentiment.getOffset());
                                });
                                final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                                assertEquals(5, aspectSentiment.getLength());
                                assertEquals(7, aspectSentiment.getOffset());
                            });
                        }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentBatchWithResponseEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        batchEmojiRunner(
            documents ->
                client.analyzeSentimentBatchWithResponse(documents,
                    new AnalyzeSentimentOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT)
                        .setIncludeOpinionMining(true), Context.NONE)
                    .getValue()
                    .forEach(
                        analyzeSentimentResult -> analyzeSentimentResult.getDocumentSentiment().getSentences().forEach(
                            sentenceSentiment -> {
                                assertEquals(24, sentenceSentiment.getLength());
                                assertEquals(0, sentenceSentiment.getOffset());
                                sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                                    minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                        assertEquals(7, opinionSentiment.getLength());
                                        assertEquals(16, opinionSentiment.getOffset());
                                    });
                                    final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                                    assertEquals(5, aspectSentiment.getLength());
                                    assertEquals(6, aspectSentiment.getOffset());
                                });
                            })),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(27, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(19, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(9, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(34, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(26, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(16, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamilyWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(42, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(34, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(24, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfcRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(26, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(18, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(8, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfdRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(27, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(19, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(9, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfcRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(25, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(17, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(7, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfdRunner(
            document ->
                client.analyzeSentiment(document, null, new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(25, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(17, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(7, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        zalgoTextRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
                        assertEquals(138, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getMinedOpinions().forEach(minedOpinion -> {
                            minedOpinion.getOpinions().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(130, opinionSentiment.getOffset());
                            });
                            final AspectSentiment aspectSentiment = minedOpinion.getAspect();
                            assertEquals(5, aspectSentiment.getLength());
                            assertEquals(120, aspectSentiment.getOffset());
                        });
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    // Healthcare LRO

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareLroWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        healthcareLroRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
            syncPoller.waitForCompletion();
            PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareTaskResults = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedHealthcareTaskResultListForSinglePage(),
                healthcareTaskResults.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareLroPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        healthcareLroPaginationRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
            syncPoller.waitForCompletion();
            PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareTaskResults = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedHealthcareTaskResultListForMultiplePages(0, 10, 0),
                healthcareTaskResults.stream().collect(Collectors.toList()));
        }, 10);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareLroEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyListRunner((documents, errorMessage) -> {
            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> client.beginAnalyzeHealthcareEntities(documents, null, Context.NONE).getFinalResult());
            assertEquals(errorMessage, exception.getMessage());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmojiUnicodeCodePoint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(
                        new TextDocumentInput("0", document)),
                        new AnalyzeHealthcareEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT),
                    Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(19, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(20, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(22, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmojiFamily(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(29, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmojiFamilyWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(37, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesDiacriticsNfc(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfcRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(21, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesDiacriticsNfd(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        diacriticsNfdRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(22, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfcRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(20, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        koreanNfdRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(20, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        zalgoTextRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
                    = syncPoller.getFinalResult();
                healthcareEntitiesResultCollection.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(133, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    // Healthcare LRO - Cancellation
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void cancelHealthcareLro(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        cancelHealthcareLroRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
                          PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
            syncPoller.cancelOperation();
            syncPoller.waitForCompletion();
            Assertions.assertEquals(LongRunningOperationStatus.USER_CANCELLED, syncPoller.poll().getStatus());
        });
    }

    // Analyze batch actions

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeTasksWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchActionsRunner((documents, tasks) -> {
            SyncPoller<AnalyzeBatchActionsOperationDetail, PagedIterable<AnalyzeBatchActionsResult>> syncPoller =
                client.beginAnalyzeBatchActions(documents, tasks,
                    new AnalyzeBatchActionsOptions().setIncludeStatistics(false), Context.NONE);
            syncPoller.waitForCompletion();
            PagedIterable<AnalyzeBatchActionsResult> result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false,
                Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(
                        false, TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(
                        false, TIME_NOW, getRecognizePiiEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(
                        false, TIME_NOW, getExtractKeyPhrasesResultCollection(), null))))),
                result.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeTasksPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchActionsPaginationRunner((documents, tasks) -> {
            SyncPoller<AnalyzeBatchActionsOperationDetail, PagedIterable<AnalyzeBatchActionsResult>>
                syncPoller = client.beginAnalyzeBatchActions(
                    documents, tasks, new AnalyzeBatchActionsOptions().setIncludeStatistics(false),
                Context.NONE);
            syncPoller.waitForCompletion();
            PagedIterable<AnalyzeBatchActionsResult> result = syncPoller.getFinalResult();
            validateAnalyzeBatchActionsResultList(false,
                getExpectedAnalyzeTaskResultListForMultiplePages(0, 20, 2),
                result.stream().collect(Collectors.toList()));
        }, 22);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeTasksEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        emptyListRunner((documents, errorMessage) -> {
            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> client.beginAnalyzeBatchActions(documents,
                    new TextAnalyticsActions().setRecognizeEntitiesOptions(new RecognizeEntitiesOptions()),
                    null, Context.NONE)
                    .getFinalResult());
            assertEquals(errorMessage, exception.getMessage());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeBatchActionsPartialCompleted(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchActionsPartialCompletedRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeBatchActionsOperationDetail, PagedIterable<AnalyzeBatchActionsResult>> syncPoller =
                    client.beginAnalyzeBatchActions(documents, tasks,
                        new AnalyzeBatchActionsOptions().setIncludeStatistics(false), Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeBatchActionsResult> result = syncPoller.getFinalResult();
                validateAnalyzeBatchActionsResultList(false,
                    Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(asList(
                            getExpectedRecognizePiiEntitiesActionResult(true, TIME_NOW, null,
                                getActionError(INVALID_REQUEST, PII_TASK, "0")),
                            getExpectedRecognizePiiEntitiesActionResult(
                                false, TIME_NOW, getRecognizePiiEntitiesResultCollection(), null))),
                        IterableStream.of(asList(
                            getExpectedExtractKeyPhrasesActionResult(
                                false, TIME_NOW, getExtractKeyPhrasesResultCollection(), null))))),
                    result.stream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeBatchActionsAllFailed(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion);
        analyzeBatchActionsAllFailedRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeBatchActionsOperationDetail, PagedIterable<AnalyzeBatchActionsResult>> syncPoller =
                    client.beginAnalyzeBatchActions(documents, tasks,
                        new AnalyzeBatchActionsOptions().setIncludeStatistics(false), Context.NONE);
                syncPoller.waitForCompletion();
                PagedIterable<AnalyzeBatchActionsResult> result = syncPoller.getFinalResult();

                validateAnalyzeBatchActionsResultList(false,
                    Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                        IterableStream.of(asList(
                            getExpectedRecognizeEntitiesActionResult(true, TIME_NOW, null,
                                getActionError(INVALID_REQUEST, ENTITY_TASK, "0")))),
                        IterableStream.of(asList(
                            getExpectedRecognizePiiEntitiesActionResult(true, TIME_NOW, null,
                                getActionError(INVALID_REQUEST, PII_TASK, "0")),
                            getExpectedRecognizePiiEntitiesActionResult(true, TIME_NOW, null,
                                getActionError(INVALID_REQUEST, PII_TASK, "1")))),
                        IterableStream.of(asList(
                            getExpectedExtractKeyPhrasesActionResult(true, TIME_NOW, null,
                                getActionError(INVALID_REQUEST, KEY_PHRASES_TASK, "0")))))),
                    result.stream().collect(Collectors.toList()));
            }
        );
    }
}
