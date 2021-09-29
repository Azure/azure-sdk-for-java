// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.SentenceSentimentPropertiesHelper;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityConditionality;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityDomain;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.SummarySentencesOrder;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedFlux;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.CATEGORIZED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.textanalytics.TestUtils.HEALTHCARE_ENTITY_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.LINKED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.PII_ENTITY_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.SENTIMENT_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.TIME_NOW;
import static com.azure.ai.textanalytics.TestUtils.getAnalyzeSentimentResultCollectionForActions;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageEnglish;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageSpanish;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeActionsResultListForMultiplePages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeBatchActionsResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeHealthcareEntitiesResultCollectionListForMultiplePages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeSentimentActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchCategorizedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchDetectedLanguages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchKeyPhrases;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchLinkedEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntities;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntitiesForCategoriesFilter;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchPiiEntitiesForDomainFilter;
import static com.azure.ai.textanalytics.TestUtils.getExpectedBatchTextSentiment;
import static com.azure.ai.textanalytics.TestUtils.getExpectedDocumentSentiment;
import static com.azure.ai.textanalytics.TestUtils.getExpectedExtractKeyPhrasesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedExtractSummaryResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getExpectedExtractSummaryResultSortByOffset;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizeEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizeLinkedEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizePiiEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getExtractSummaryActionResult;
import static com.azure.ai.textanalytics.TestUtils.getLinkedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getPiiEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getPiiEntitiesList1ForDomainFilter;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeEntitiesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeLinkedEntitiesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeLinkedEntitiesResultCollectionForActions;
import static com.azure.ai.textanalytics.TestUtils.getRecognizePiiEntitiesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getUnknownDetectedLanguage;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_COUNTRY_HINT;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_DOCUMENT;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_DOCUMENT_BATCH;
import static com.azure.ai.textanalytics.models.WarningCode.LONG_WORDS_IN_DOCUMENT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsAsyncClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private TextAnalyticsAsyncClient getTextAnalyticsAsyncClient(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        return getTextAnalyticsAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Detected Languages

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageShowStatisticsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .assertNext(response ->
                    validateDetectLanguageResultCollectionWithResponse(true, getExpectedBatchDetectedLanguages(),
                        200, response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each {@code DetectLanguageResult} input of a batch.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, null))
                .assertNext(response ->
                    validateDetectLanguageResultCollectionWithResponse(false, getExpectedBatchDetectedLanguages(),
                        200, response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch with given country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguagesCountryHintRunner((inputs, countryHint) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, countryHint, null))
                .assertNext(actualResults ->
                    validateDetectLanguageResultCollection(false, getExpectedBatchDetectedLanguages(), actualResults))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHintWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null, options))
                .assertNext(response -> validateDetectLanguageResultCollection(true, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Test to detect language for each string input of batch.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageStringInputRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null, null))
                .assertNext(response -> validateDetectLanguageResultCollection(false, getExpectedBatchDetectedLanguages(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a single DetectedLanguage is returned for a document to detectLanguage.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectSingleTextLanguageRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageEnglish(), response))
                .verifyComplete());
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageInvalidCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageInvalidCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_COUNTRY_HINT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify());
    }

    /**
     * Verifies that TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify());
    }

    /**
     * Verifies that detectLanguage returns an "UNKNOWN" result when faulty text is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .assertNext(response -> validatePrimaryLanguage(getUnknownDetectedLanguage(), response))
                .verifyComplete());
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same ids.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageDuplicateIdRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Verifies that an invalid document exception is returned for input documents with an empty ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageInputEmptyIdRunner(inputs ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                }));
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageEmptyCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageSpanish(), response))
                .verifyComplete());
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        detectLanguageNoneCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageSpanish(), response))
                .verifyComplete());
    }

    // Entities
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesForSingleTextInputRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .assertNext(response -> validateCategorizedEntities(response.stream().collect(Collectors.toList())))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify()
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
                .verifyComplete()
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .assertNext(resultCollection -> resultCollection.getValue().forEach(recognizeEntitiesResult -> {
                    Exception exception = assertThrows(TextAnalyticsException.class, recognizeEntitiesResult::getEntities);
                    assertEquals(String.format(BATCH_ERROR_EXCEPTION_MESSAGE, "RecognizeEntitiesResult"), exception.getMessage());
                })).verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollectionWithResponse(false, getExpectedBatchCategorizedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateCategorizedEntitiesResultCollectionWithResponse(true, getExpectedBatchCategorizedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, options))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(true, getExpectedBatchCategorizedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void recognizeEntitiesBatchWithResponseEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
//        batchEmojiRunner(
//            documents -> StepVerifier.create(client.recognizeEntitiesBatchWithResponse(documents,
//                new RecognizeEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT)))
//                             .assertNext(response -> response.getValue().stream().forEach(
//                                 recognizeEntitiesResult -> recognizeEntitiesResult.getEntities().forEach(
//                                     categorizedEntity -> {
//                                         assertEquals(9, categorizedEntity.getLength());
//                                         assertEquals(12, categorizedEntity.getOffset());
//                                     })))
//                    .verifyComplete(),
//            CATEGORIZED_ENTITY_INPUTS.get(1)
//        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(15, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(22, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(30, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfcRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(14, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfdRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(15, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfcRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfdRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        zalgoTextRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(126, categorizedEntity.getOffset());
                })).verifyComplete(), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    // Recognize Personally Identifiable Information entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizePiiSingleDocumentRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(response -> validatePiiEntities(getPiiEntitiesList1(), response.stream().collect(Collectors.toList())))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(document -> StepVerifier.create(client.recognizePiiEntities(document))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
            .verify());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchPiiEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .assertNext(resultCollection -> resultCollection.getValue().forEach(recognizePiiEntitiesResult -> {
                    Exception exception = assertThrows(TextAnalyticsException.class, recognizePiiEntitiesResult::getEntities);
                    assertEquals(String.format(BATCH_ERROR_EXCEPTION_MESSAGE, "RecognizePiiEntitiesResult"), exception.getMessage());
                })).verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitiesRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validatePiiEntitiesResultCollectionWithResponse(true, getExpectedBatchPiiEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizePiiLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, language, null))
                .assertNext(response -> validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeStringBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, null, options))
                .assertNext(response -> validatePiiEntitiesResultCollection(true, getExpectedBatchPiiEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, null, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(8, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void recognizePiiEntitiesBatchWithResponseEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
//        batchEmojiRunner(
//            documents -> StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(documents,
//                new RecognizePiiEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT)))
//                             .assertNext(response -> response.getValue().stream().forEach(
//                                 result -> result.getEntities().forEach(
//                                     piiEntity -> {
//                                         assertEquals(11, piiEntity.getLength());
//                                         assertEquals(7, piiEntity.getOffset());
//                                     })))
//                             .verifyComplete(),
//            PII_ENTITY_OFFSET_INPUT
//        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(10, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(17, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(25, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfcRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(9, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfdRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(10, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfcRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(8, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfdRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(8, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        zalgoTextRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(121, piiEntity.getOffset());
                })).verifyComplete(), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizePiiDomainFilterRunner((document, options) ->
            StepVerifier.create(client.recognizePiiEntities(document, "en", options))
                .assertNext(response -> validatePiiEntities(getPiiEntitiesList1ForDomainFilter(),
                    response.stream().collect(Collectors.toList())))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputStringForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizePiiLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, language,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION)))
                .assertNext(response -> validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntitiesForDomainFilter(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchPiiEntitiesRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION)))
                .assertNext(response -> validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntitiesForDomainFilter(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputForCategoriesFilter(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeStringBatchPiiEntitiesForCategoriesFilterRunner(
            (inputs, options) ->
                StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, "en", options))
                    .assertNext(
                        resultCollection -> validatePiiEntitiesResultCollection(false,
                            getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection))
                    .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntityWithCategoriesFilterFromOtherResult(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeStringBatchPiiEntitiesForCategoriesFilterRunner(
            (inputs, options) -> {
                List<PiiEntityCategory> categories = new ArrayList<>();
                StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, "en", options))
                    .assertNext(
                        resultCollection -> {
                            resultCollection.forEach(result -> result.getEntities().forEach(piiEntity -> {
                                final PiiEntityCategory category = piiEntity.getCategory();
                                if (PiiEntityCategory.ABA_ROUTING_NUMBER == category
                                        || PiiEntityCategory.US_SOCIAL_SECURITY_NUMBER == category) {
                                    categories.add(category);
                                }
                            }));
                            validatePiiEntitiesResultCollection(false,
                                getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection);
                        })
                    .verifyComplete();

                // Override whatever the categoriesFiler has currently
                final PiiEntityCategory[] piiEntityCategories = categories.toArray(new PiiEntityCategory[categories.size()]);
                options.setCategoriesFilter(piiEntityCategories);

                // Use the categories from another endpoint to call.
                StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, "en", options))
                    .assertNext(
                        resultCollection -> validatePiiEntitiesResultCollection(false,
                            getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection))
                    .verifyComplete();
            });
    }

    // Linked Entities
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedEntitiesForSingleTextInputRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .assertNext(response -> validateLinkedEntity(getLinkedEntitiesList1().get(0), response.iterator().next()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityDuplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateLinkedEntitiesResultCollectionWithResponse(false,
                    getExpectedBatchLinkedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateLinkedEntitiesResultCollectionWithResponse(true, getExpectedBatchLinkedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, null))
                .assertNext(response -> validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, options))
                .assertNext(response -> validateLinkedEntitiesResultCollection(true, getExpectedBatchLinkedEntities(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void recognizeLinkedEntitiesBatchWithResponseEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
//        batchEmojiRunner(
//            documents -> StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(documents,
//                new RecognizeLinkedEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT)))
//                             .assertNext(response -> response.getValue().stream().forEach(
//                                 result -> result.getEntities().forEach(
//                                     linkedEntity ->
//                                         linkedEntity.getMatches().forEach(linkedEntityMatch -> {
//                                             assertEquals(9, linkedEntityMatch.getLength());
//                                             assertEquals(12, linkedEntityMatch.getOffset());
//                                         }))))
//                             .verifyComplete(),
//            LINKED_ENTITY_INPUTS.get(1)
//        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(15, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(22, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(30, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfcRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(14, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfdRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(15, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfcRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfdRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        zalgoTextRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(126, linkedEntityMatch.getOffset());
                    });
                })).verifyComplete(), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    // Key Phrases
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesForSingleTextInputRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .assertNext(keyPhrasesCollection -> validateKeyPhrases(asList("Bonjour", "monde"),
                    keyPhrasesCollection.stream().collect(Collectors.toList())))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .assertNext(result -> assertFalse(result.getWarnings().iterator().hasNext()))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesDuplicateIdRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollectionWithResponse(false, getExpectedBatchKeyPhrases(), 200, response))
                .verifyComplete());

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, options))
                .assertNext(response -> validateExtractKeyPhrasesResultCollectionWithResponse(true, getExpectedBatchKeyPhrases(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesStringInputRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, language, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, options))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(true, getExpectedBatchKeyPhrases(), response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesWarningRunner(
            input -> StepVerifier.create(client.extractKeyPhrases(input))
                .assertNext(keyPhrasesResult -> {
                    keyPhrasesResult.getWarnings().forEach(warning -> {
                        assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                        assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
                    });
                })
                .verifyComplete()
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesBatchWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesBatchWarningRunner(
            inputs -> StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .assertNext(response -> response.getValue().forEach(keyPhrasesResult ->
                    keyPhrasesResult.getKeyPhrases().getWarnings().forEach(warning -> {
                        assertTrue(WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE.equals(warning.getMessage()));
                        assertTrue(LONG_WORDS_IN_DOCUMENT.equals(warning.getWarningCode()));
                    })
                ))
                .verifyComplete()
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                }));
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentForSingleTextInputRunner(input ->
            StepVerifier.create(client.analyzeSentiment(input))
                .assertNext(response -> validateDocumentSentiment(false, getExpectedDocumentSentiment(), response))
                .verifyComplete()
        );
    }

    /**
     * Test analyzing sentiment for a string input with default language hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithDefaultLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentForSingleTextInputRunner(input ->
            StepVerifier.create(client.analyzeSentiment(input, null))
                .assertNext(response -> validateDocumentSentiment(false, getExpectedDocumentSentiment(), response))
                .verifyComplete()
        );
    }

    /**
     * Test analyzing sentiment for a string input and verifying the result of opinion mining.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentForTextInputWithOpinionMiningRunner((input, options) ->
            StepVerifier.create(client.analyzeSentiment(input, "en", options))
                .assertNext(response -> validateDocumentSentiment(true, getExpectedDocumentSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyTextRunner(document ->
            StepVerifier.create(client.analyzeSentiment(document))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify()
        );
    }

    /**
     * Test analyzing sentiment for a faulty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        faultyTextRunner(input -> {
            final SentenceSentiment sentenceSentiment1 = new SentenceSentiment("!", TextSentiment.NEUTRAL,
                new SentimentConfidenceScores(0.0, 0.0, 0.0));
            SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment1, 0);
            SentenceSentimentPropertiesHelper.setLength(sentenceSentiment1, 1);
            final SentenceSentiment sentenceSentiment2 = new SentenceSentiment("@#%%", TextSentiment.NEUTRAL,
                new SentimentConfidenceScores(0.0, 0.0, 0.0));
            SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment2, 1);
            SentenceSentimentPropertiesHelper.setLength(sentenceSentiment2, 4);

            final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(
                TextSentiment.NEUTRAL,
                new SentimentConfidenceScores(0.0, 0.0, 0.0),
                new IterableStream<>(asList(sentenceSentiment1, sentenceSentiment2)),
                null);
            StepVerifier.create(client.analyzeSentiment(input))
                .assertNext(response -> validateDocumentSentiment(false, expectedDocumentSentiment, response))
                .verifyComplete();
        });
    }

    /**
     * Test analyzing sentiment for a duplicate ID list.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentDuplicateIdRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, new TextAnalyticsRequestOptions()))
                .verifyErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass())));
    }

    /**
     * Verifies that an invalid document exception is returned for input documents with an empty ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        textAnalyticsInputEmptyIdRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                }));
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * String documents with null TextAnalyticsRequestOptions and null language code which will use the default language
     * code, 'en'.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions is null and null language code which will use the default language code, 'en'.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, new TextAnalyticsRequestOptions()))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(false, false,
                    getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * String documents with null TextAnalyticsRequestOptions and given a language code.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions is null and given a language code.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringWithLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, language, new TextAnalyticsRequestOptions()))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(false, false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verify that the collection result includes request statistics but not sentence options when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which to show the request statistics only and verify the analyzed sentiment result.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options.setIncludeOpinionMining(false)))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(true, false, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verify that the collection result includes sentence options but not request statistics when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) -> {
            options.setIncludeStatistics(false);
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(false, true, getExpectedBatchTextSentiment(), response))
                .verifyComplete();
        });
    }

    /**
     * Verify that the collection result includes sentence options and request statistics when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(true, true, getExpectedBatchTextSentiment(), response))
                .verifyComplete());
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * TextDocumentInput documents with null TextAnalyticsRequestOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputWithNullRequestOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, (TextAnalyticsRequestOptions) null))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete());
    }

    /**
     * Verify that we can get statistics on the collection result when given a batch of
     * TextDocumentInput documents with TextAnalyticsRequestOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions includes request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentShowStatsRunner((inputs, requestOptions) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, requestOptions))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete());
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * TextDocumentInput documents with null AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputWithNullAnalyzeSentimentOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, (AnalyzeSentimentOptions) null))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete());
    }

    /**
     * Verify that the collection result includes request statistics but not sentence options when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes request statistics but not opinion mining.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options.setIncludeOpinionMining(false)))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete());
    }

    /**
     * Verify that the collection result includes sentence options but not request statistics when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining but not request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) -> {
            options.setIncludeStatistics(false);
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options))
                .assertNext(response ->
                    validateAnalyzeSentimentResultCollectionWithResponse(false, true, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete();
        });
    }

    /**
     * Verify that the collection result includes sentence options and request statistics when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(true, true, getExpectedBatchTextSentiment(), 200, response))
                .verifyComplete());
    }

    /**
     * Verifies that an InvalidDocumentBatch exception is returned for input documents with too many documents.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(result -> result.getSentences().forEach(sentenceSentiment -> {
                        assertEquals(25, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            opinion.getAssessments().forEach(opinionSentiment -> {
                                assertEquals(7, opinionSentiment.getLength());
                                assertEquals(17, opinionSentiment.getOffset());
                            });
                            final TargetSentiment targetSentiment = opinion.getTarget();
                            assertEquals(5, targetSentiment.getLength());
                            assertEquals(7, targetSentiment.getOffset());
                        });
                    }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void analyzeSentimentBatchWithResponseEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
//        batchEmojiRunner(
//            documents ->
//                StepVerifier.create(client.analyzeSentimentBatchWithResponse(documents,
//                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)
//                        .setStringIndexType(StringIndexType.UNICODE_CODE_POINT)))
//                    .assertNext(response -> response.getValue().stream().forEach(
//                        result -> result.getDocumentSentiment().getSentences().forEach(
//                            sentenceSentiment -> {
//                                assertEquals(24, sentenceSentiment.getLength());
//                                assertEquals(0, sentenceSentiment.getOffset());
//                                sentenceSentiment.getOpinions().forEach(opinion -> {
//                                    opinion.getAssessments().forEach(assessmentSentiment -> {
//                                        assertEquals(7, assessmentSentiment.getLength());
//                                        assertEquals(16, assessmentSentiment.getOffset());
//                                    });
//                                    final TargetSentiment targetSentiment = opinion.getTarget();
//                                    assertEquals(5, targetSentiment.getLength());
//                                    assertEquals(6, targetSentiment.getOffset());
//                                });
//                            })))
//                    .verifyComplete(),
//            SENTIMENT_OFFSET_INPUT
//        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiWithSkinToneModifier(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(result -> result.getSentences().forEach(sentenceSentiment -> {
                        assertEquals(27, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            opinion.getAssessments().forEach(assessmentSentiment -> {
                                assertEquals(7, assessmentSentiment.getLength());
                                assertEquals(19, assessmentSentiment.getOffset());
                            });
                            final TargetSentiment targetSentiment = opinion.getTarget();
                            assertEquals(5, targetSentiment.getLength());
                            assertEquals(9, targetSentiment.getOffset());
                        });
                    }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(
                        result -> result.getSentences().forEach(
                            sentenceSentiment -> {
                                assertEquals(34, sentenceSentiment.getLength());
                                assertEquals(0, sentenceSentiment.getOffset());
                                sentenceSentiment.getOpinions().forEach(opinion -> {
                                    opinion.getAssessments().forEach(assessmentSentiment -> {
                                        assertEquals(7, assessmentSentiment.getLength());
                                        assertEquals(26, assessmentSentiment.getOffset());
                                    });
                                    final TargetSentiment targetSentiment = opinion.getTarget();
                                    assertEquals(5, targetSentiment.getLength());
                                    assertEquals(16, targetSentiment.getOffset());
                                });
                            })
                    )
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamilyWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(
                        result -> result.getSentences().forEach(
                            sentenceSentiment -> {
                                assertEquals(42, sentenceSentiment.getLength());
                                assertEquals(0, sentenceSentiment.getOffset());
                                sentenceSentiment.getOpinions().forEach(opinion -> {
                                    opinion.getAssessments().forEach(assessmentSentiment -> {
                                        assertEquals(7, assessmentSentiment.getLength());
                                        assertEquals(34, assessmentSentiment.getOffset());
                                    });
                                    final TargetSentiment targetSentiment = opinion.getTarget();
                                    assertEquals(5, targetSentiment.getLength());
                                    assertEquals(24, targetSentiment.getOffset());
                                });
                            }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfcRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(result -> result.getSentences().forEach(sentenceSentiment -> {
                        assertEquals(26, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            opinion.getAssessments().forEach(assessmentSentiment -> {
                                assertEquals(7, assessmentSentiment.getLength());
                                assertEquals(18, assessmentSentiment.getOffset());
                            });
                            final TargetSentiment targetSentiment = opinion.getTarget();
                            assertEquals(5, targetSentiment.getLength());
                            assertEquals(8, targetSentiment.getOffset());
                        });
                    }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfdRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(result -> result.getSentences().forEach(
                        sentenceSentiment -> {
                            assertEquals(27, sentenceSentiment.getLength());
                            assertEquals(0, sentenceSentiment.getOffset());
                            sentenceSentiment.getOpinions().forEach(opinion -> {
                                opinion.getAssessments().forEach(assessmentSentiment -> {
                                    assertEquals(7, assessmentSentiment.getLength());
                                    assertEquals(19, assessmentSentiment.getOffset());
                                });
                                final TargetSentiment targetSentiment = opinion.getTarget();
                                assertEquals(5, targetSentiment.getLength());
                                assertEquals(9, targetSentiment.getOffset());
                            });
                        }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfcRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(result -> result.getSentences().forEach(sentenceSentiment -> {
                        assertEquals(25, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            opinion.getAssessments().forEach(assessmentSentiment -> {
                                assertEquals(7, assessmentSentiment.getLength());
                                assertEquals(17, assessmentSentiment.getOffset());
                            });
                            final TargetSentiment targetSentiment = opinion.getTarget();
                            assertEquals(5, targetSentiment.getLength());
                            assertEquals(7, targetSentiment.getOffset());
                        });
                    }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfdRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(result -> result.getSentences().forEach(sentenceSentiment -> {
                        assertEquals(25, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            opinion.getAssessments().forEach(assessmentSentiment -> {
                                assertEquals(7, assessmentSentiment.getLength());
                                assertEquals(17, assessmentSentiment.getOffset());
                            });
                            final TargetSentiment targetSentiment = opinion.getTarget();
                            assertEquals(5, targetSentiment.getLength());
                            assertEquals(7, targetSentiment.getOffset());
                        });
                    }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        zalgoTextRunner(
            document ->
                StepVerifier.create(client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true)))
                    .assertNext(result -> result.getSentences().forEach(sentenceSentiment -> {
                        assertEquals(138, sentenceSentiment.getLength());
                        assertEquals(0, sentenceSentiment.getOffset());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            opinion.getAssessments().forEach(assessmentSentiment -> {
                                assertEquals(7, assessmentSentiment.getLength());
                                assertEquals(130, assessmentSentiment.getOffset());
                            });
                            final TargetSentiment targetSentiment = opinion.getTarget();
                            assertEquals(5, targetSentiment.getLength());
                            assertEquals(120, targetSentiment.getOffset());
                        });
                    }))
                    .verifyComplete(),
            SENTIMENT_OFFSET_INPUT
        );
    }


    // Healthcare LRO
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22208")
    public void healthcareLroPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        healthcareLroPaginationRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForMultiplePages(0, 10, 0),
                analyzeHealthcareEntitiesPagedFlux.toStream().collect(Collectors.toList()));
        }, 10);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22208")
    public void healthcareLroWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        healthcareLroRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage(),
                analyzeHealthcareEntitiesPagedFlux.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareLroEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyListRunner((documents, errorMessage) -> {
            StepVerifier.create(client.beginAnalyzeHealthcareEntities(documents, null))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                    && errorMessage.equals(throwable.getMessage()))
                .verify();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void analyzeHealthcareEntitiesEmojiUnicodeCodePoint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
//        emojiRunner(
//            document -> {
//                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, PagedFlux<AnalyzeHealthcareEntitiesResultCollection>>
//                    syncPoller = client.beginAnalyzeHealthcareEntities(
//                        Collections.singletonList(new TextDocumentInput("0", document)),
//                        new AnalyzeHealthcareEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT))
//                                     .getSyncPoller();
//                syncPoller = setPollInterval(syncPoller);
//                syncPoller.waitForCompletion();
//                PagedFlux<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollectionPagedFlux
//                    = syncPoller.getFinalResult();
//                healthcareEntitiesResultCollectionPagedFlux.toStream().forEach(result -> {
//                    result.forEach(entitiesResult ->
//                        entitiesResult.getEntities().forEach(entity -> {
//                            assertEquals(11, entity.getLength());
//                            assertEquals(19, entity.getOffset());
//                        }));
//                });
//            },
//            HEALTHCARE_ENTITY_OFFSET_INPUT
//        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiWithSkinToneModifierRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(22, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emojiFamilyWithSkinToneModifierRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(37, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22208")
    public void analyzeHealthcareEntitiesDiacriticsNfc(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfcRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(21, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22208")
    public void analyzeHealthcareEntitiesDiacriticsNfd(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        diacriticsNfdRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfcRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        koreanNfdRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        zalgoTextRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedFlux.toStream().forEach(result -> result.forEach(
                    entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(133, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesForAssertion(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeHealthcareEntitiesForAssertionRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, "en", options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
            // "All female participants that are premenopausal will be required to have a pregnancy test;
            // any participant who is pregnant or breastfeeding will not be included"
            final HealthcareEntityAssertion assertion =
                analyzeHealthcareEntitiesPagedFlux.toStream().collect(Collectors.toList())
                    .get(0).stream().collect(Collectors.toList()) // List of document result
                    .get(0).getEntities().stream().collect(Collectors.toList()) // List of entities
                    .get(1) // "premenopausal" is the second entity recognized.
                    .getAssertion();
            assertEquals(EntityConditionality.HYPOTHETICAL, assertion.getConditionality());
            assertNull(assertion.getAssociation());
            assertNull(assertion.getCertainty());
        });
    }

    // Healthcare LRO - Cancellation

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void cancelHealthcareLro(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        cancelHealthcareLroRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.cancelOperation();
            syncPoller.waitForCompletion();
            Assertions.assertEquals(LongRunningOperationStatus.USER_CANCELLED, syncPoller.poll().getStatus());
        });
    }

    // Analyze Actions LRO

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchActionsRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks,
                    new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(false,
                        TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(false,
                        TIME_NOW, getRecognizeLinkedEntitiesResultCollectionForActions(), null))),
                    IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false,
                        TIME_NOW,
                        getRecognizePiiEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(false,
                        TIME_NOW, getExtractKeyPhrasesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(false,
                        TIME_NOW, getAnalyzeSentimentResultCollectionForActions(), null))),
                    IterableStream.of(Collections.emptyList())
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeBatchActionsPaginationRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux>
                syncPoller = client.beginAnalyzeActions(
                    documents, tasks, new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            validateAnalyzeBatchActionsResultList(false, false,
                getExpectedAnalyzeActionsResultListForMultiplePages(0, 20, 2),
                result.toStream().collect(Collectors.toList()));
        }, 22);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        emptyListRunner((documents, errorMessage) ->
            StepVerifier.create(client.beginAnalyzeActions(documents,
                new TextAnalyticsActions()
                    .setRecognizeEntitiesActions(new RecognizeEntitiesAction()), null))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                    && errorMessage.equals(throwable.getMessage()))
                .verify());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeEntitiesRecognitionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeEntitiesRecognitionRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                    client.beginAnalyzeActions(documents, tasks,
                        new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

                validateAnalyzeBatchActionsResultList(false, false,
                    Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                        IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(false,
                            TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()))),
                    result.toStream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzePiiEntityRecognitionWithCategoriesFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzePiiEntityRecognitionWithCategoriesFiltersRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                    client.beginAnalyzeActions(documents, tasks,
                        new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

                validateAnalyzeBatchActionsResultList(false, false,
                    Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false,
                            TIME_NOW, getExpectedBatchPiiEntitiesForCategoriesFilter(), null))),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()))),
                    result.toStream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://msazure.visualstudio.com/Cognitive%20Services/_workitems/edit/9956158")
    public void analyzePiiEntityRecognitionWithDomainFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzePiiEntityRecognitionWithDomainFiltersRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                    client.beginAnalyzeActions(documents, tasks,
                        new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

                validateAnalyzeBatchActionsResultList(false, false,
                    Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false,
                            TIME_NOW, getExpectedBatchPiiEntitiesForDomainFilter(), null))),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()),
                        IterableStream.of(Collections.emptyList()))),
                    result.toStream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("Linked entity recognition action doesn't contains bingId property. https://github.com/Azure/azure-sdk-for-java/issues/22208")
    public void analyzeLinkedEntityActions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeLinkedEntityRecognitionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(
                false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(false,
                        TIME_NOW, getRecognizeLinkedEntitiesResultCollection(), null))),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList())
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeKeyPhrasesExtractionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        extractKeyPhrasesRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(false,
                        TIME_NOW, getExtractKeyPhrasesResultCollection(), null))),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()))),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeSentimentRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(false,
                        TIME_NOW, getExpectedBatchTextSentiment(), null))),
                    IterableStream.of(Collections.emptyList()))),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @Disabled("Service returned response has unstable value. https://github.com/Azure/azure-sdk-for-java/issues/23428")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionWithDefaultParameterValues(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeExtractSummaryRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            // We are expecting the top 3 highest rank score and these scores are sorted by offset by default
            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(Collections.emptyList()),
                    IterableStream.of(asList(getExtractSummaryActionResult(false,
                        TIME_NOW,
                        getExpectedExtractSummaryResultCollection(getExpectedExtractSummaryResultSortByOffset()),
                        null))))),
                result.toStream().collect(Collectors.toList()));
        }, null, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionSortedByOffset(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeExtractSummaryRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            result.toStream().collect(Collectors.toList()).forEach(
                actionsResult -> actionsResult.getExtractSummaryResults().forEach(
                    extractSummaryActionResult -> extractSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertTrue(isAscendingOrderByOffSet(
                            documentResult.getSentences().stream().collect(Collectors.toList()))))));
        }, 4, SummarySentencesOrder.OFFSET);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionSortedByRankScore(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeExtractSummaryRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                actionsResult -> actionsResult.getExtractSummaryResults().forEach(
                    extractSummaryActionResult -> extractSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertTrue(isDescendingOrderByRankScore(
                            documentResult.getSentences().stream().collect(Collectors.toList()))))));
        }, 4, SummarySentencesOrder.RANK);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionWithSentenceCountLessThanMaxCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeExtractSummaryRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            result.toStream().collect(Collectors.toList()).forEach(
                actionsResult -> actionsResult.getExtractSummaryResults().forEach(
                    extractSummaryActionResult -> extractSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertTrue(
                            documentResult.getSentences().stream().collect(Collectors.toList()).size() < 20))));
        }, 20, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionWithNonDefaultSentenceCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        analyzeExtractSummaryRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getExtractSummaryResults().forEach(
                    extractSummaryActionResult -> extractSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertEquals(
                            documentResult.getSentences().stream().collect(Collectors.toList()).size(), 5))));
        }, 5, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionMaxSentenceCountInvalidRangeException(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        // The range of max sentences count is at between 1 and 20.
        int[] invalidMaxSentenceCounts = {0, 21};

        for (int invalidCount: invalidMaxSentenceCounts) {
            analyzeExtractSummaryRunner(
                (documents, tasks) -> {
                    HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
                        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                            client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions())
                                .getSyncPoller();
                        syncPoller = setPollInterval(syncPoller);
                        syncPoller.waitForCompletion();
                        AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
                    });
                    assertEquals(
                        TextAnalyticsErrorCode.INVALID_PARAMETER_VALUE,
                        ((TextAnalyticsError) exception.getValue()).getErrorCode());
                }, invalidCount, null);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeCustomEntitiesAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        recognizeCustomEntitiesActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getRecognizeCustomEntitiesResults().forEach(
                    customEntitiesActionResult -> customEntitiesActionResult.getDocumentsResults().forEach(
                        documentResult -> validateCategorizedEntities(
                            documentResult.getEntities().stream().collect(Collectors.toList())))));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void singleCategoryClassifyAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        classifyCustomSingleCategoryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getSingleCategoryClassifyResults().forEach(
                    customSingleCategoryActionResult -> customSingleCategoryActionResult.getDocumentsResults().forEach(
                        documentResult -> validateCustomSingleCategory(documentResult))));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void multiCategoryClassifyAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion);
        classifyCustomMultiCategoriesActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getMultiCategoryClassifyResults().forEach(
                    customMultiCategoryActionResult -> customMultiCategoryActionResult.getDocumentsResults().forEach(
                        documentResult -> validateCustomMultiCategories(documentResult))));
        });
    }
}
