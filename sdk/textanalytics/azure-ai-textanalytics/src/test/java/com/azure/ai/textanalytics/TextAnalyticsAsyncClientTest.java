// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AbstractiveSummaryOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.ClassifyDocumentOperationDetail;
import com.azure.ai.textanalytics.models.EntityConditionality;
import com.azure.ai.textanalytics.models.ExtractiveSummaryOperationDetail;
import com.azure.ai.textanalytics.models.ExtractiveSummarySentencesOrder;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.MultiLabelClassifyOptions;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityDomain;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SingleLabelClassifyOptions;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AbstractiveSummaryPagedFlux;
import com.azure.ai.textanalytics.util.AbstractiveSummaryResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedFlux;
import com.azure.ai.textanalytics.util.ClassifyDocumentPagedFlux;
import com.azure.ai.textanalytics.util.ExtractiveSummaryPagedFlux;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedFlux;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
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
import static com.azure.ai.textanalytics.TestUtils.CUSTOM_ACTION_NAME;
import static com.azure.ai.textanalytics.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.textanalytics.TestUtils.HEALTHCARE_ENTITY_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.LINKED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.PII_ENTITY_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.SENTIMENT_OFFSET_INPUT;
import static com.azure.ai.textanalytics.TestUtils.TIME_NOW;
import static com.azure.ai.textanalytics.TestUtils.getAbstractiveSummaryActionResult;
import static com.azure.ai.textanalytics.TestUtils.getAnalyzeSentimentResultCollectionForActions;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageEnglish;
import static com.azure.ai.textanalytics.TestUtils.getDetectedLanguageSpanish;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAbstractiveSummaryResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeActionsResultListForMultiplePages;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeBatchActionsResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeHealthcareEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedAnalyzeHealthcareEntitiesResultCollection;
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
import static com.azure.ai.textanalytics.TestUtils.getExpectedExtractiveSummaryResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getExpectedExtractiveSummaryResultSortByOffset;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizeEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizeLinkedEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExpectedRecognizePiiEntitiesActionResult;
import static com.azure.ai.textanalytics.TestUtils.getExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getExtractiveSummaryActionResult;
import static com.azure.ai.textanalytics.TestUtils.getLinkedEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getPiiEntitiesList1;
import static com.azure.ai.textanalytics.TestUtils.getPiiEntitiesList1ForDomainFilter;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeEntitiesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeHealthcareEntitiesResult1;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeHealthcareEntitiesResult2;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeLinkedEntitiesResultCollection;
import static com.azure.ai.textanalytics.TestUtils.getRecognizeLinkedEntitiesResultCollectionForActions;
import static com.azure.ai.textanalytics.TestUtils.getRecognizePiiEntitiesResultCollection;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_COUNTRY_HINT;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_DOCUMENT;
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_DOCUMENT_BATCH;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsAsyncClientTest extends TextAnalyticsClientTestBase {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private TextAnalyticsAsyncClient client;

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }

    private TextAnalyticsAsyncClient getTextAnalyticsAsyncClient(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion, boolean isStaticResource) {
        return getTextAnalyticsClientBuilder(
            buildAsyncAssertingClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion,
            isStaticResource)
            .buildAsyncClient();
    }

    // Detected Languages

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageShowStatisticsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .assertNext(response ->
                    validateDetectLanguageResultCollectionWithResponse(true, getExpectedBatchDetectedLanguages(),
                        200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Test to detect language for each {@code DetectLanguageResult} input of a batch.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, null))
                .assertNext(response ->
                    validateDetectLanguageResultCollectionWithResponse(false, getExpectedBatchDetectedLanguages(),
                        200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Test to detect language for each string input of batch with given country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguagesCountryHintRunner((inputs, countryHint) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, countryHint, null))
                .assertNext(actualResults ->
                    validateDetectLanguageResultCollection(false, getExpectedBatchDetectedLanguages(), actualResults))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Test to detect language for each string input of batch with request options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchListCountryHintWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null, options))
                .assertNext(response -> validateDetectLanguageResultCollection(true, getExpectedBatchDetectedLanguages(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Test to detect language for each string input of batch.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageStringInputRunner((inputs) ->
            StepVerifier.create(client.detectLanguageBatch(inputs, null, null))
                .assertNext(response -> validateDetectLanguageResultCollection(false, getExpectedBatchDetectedLanguages(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that a single DetectedLanguage is returned for a document to detectLanguage.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectSingleTextLanguageRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageEnglish(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for a document with invalid country hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageInvalidCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageInvalidCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_COUNTRY_HINT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyTextRunner(input ->
            StepVerifier.create(client.detectLanguage(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same ids.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageDuplicateIdRunner((inputs, options) ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, options))
                .expectErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass()))
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that an invalid document exception is returned for input documents with an empty ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageInputEmptyIdRunner(inputs ->
            StepVerifier.create(client.detectLanguageBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verify that with countryHint with empty string will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageEmptyCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageSpanish(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        detectLanguageNoneCountryHintRunner((input, countryHint) ->
            StepVerifier.create(client.detectLanguage(input, countryHint))
                .assertNext(response -> validatePrimaryLanguage(getDetectedLanguageSpanish(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    // Entities
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeCategorizedEntitiesForSingleTextInputRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .assertNext(response -> validateCategorizedEntities(response.stream().collect(Collectors.toList())))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyTextRunner(input ->
            StepVerifier.create(client.recognizeEntities(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify(DEFAULT_TIMEOUT)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass()))
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchCategorizedEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .assertNext(resultCollection -> resultCollection.getValue().forEach(recognizeEntitiesResult -> {
                    Exception exception = assertThrows(TextAnalyticsException.class, recognizeEntitiesResult::getEntities);
                    assertEquals(String.format(BATCH_ERROR_EXCEPTION_MESSAGE, "RecognizeEntitiesResult"), exception.getMessage());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchCategorizedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollectionWithResponse(false, getExpectedBatchCategorizedEntities(), 200, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateCategorizedEntitiesResultCollectionWithResponse(true, getExpectedBatchCategorizedEntities(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, options))
                .assertNext(response -> validateCategorizedEntitiesResultCollection(true, getExpectedBatchCategorizedEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.recognizeEntitiesBatch(inputs, null, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void recognizeEntitiesBatchWithResponseEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(15, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiFamilyWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(30, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        diacriticsNfcRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(14, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        diacriticsNfdRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(15, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        koreanNfcRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        koreanNfdRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(13, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        zalgoTextRunner(document ->
            StepVerifier.create(client.recognizeEntities(document))
                .assertNext(result -> result.forEach(categorizedEntity -> {
                    assertEquals(9, categorizedEntity.getLength());
                    assertEquals(126, categorizedEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), CATEGORIZED_ENTITY_INPUTS.get(1)
        );
    }

    // Recognize Personally Identifiable Information entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizePiiSingleDocumentRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(response -> validatePiiEntities(getPiiEntitiesList1(), response.stream().collect(Collectors.toList())))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyTextRunner(document -> StepVerifier.create(client.recognizePiiEntities(document))
            .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
            .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass()))
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitySingleErrorRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .assertNext(resultCollection -> resultCollection.getValue().forEach(recognizePiiEntitiesResult -> {
                    Exception exception = assertThrows(TextAnalyticsException.class, recognizePiiEntitiesResult::getEntities);
                    assertEquals(String.format(BATCH_ERROR_EXCEPTION_MESSAGE, "RecognizePiiEntitiesResult"), exception.getMessage());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitiesRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntities(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validatePiiEntitiesResultCollectionWithResponse(true, getExpectedBatchPiiEntities(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizePiiLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, language, null))
                .assertNext(response -> validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeStringBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, null, options))
                .assertNext(response -> validatePiiEntitiesResultCollection(true, getExpectedBatchPiiEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, null, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(8, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(10, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiFamilyRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(17, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiFamilyWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(25, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        diacriticsNfcRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(9, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        diacriticsNfdRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(10, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        koreanNfcRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(8, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        koreanNfdRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(8, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        zalgoTextRunner(document ->
            StepVerifier.create(client.recognizePiiEntities(document))
                .assertNext(result -> result.forEach(piiEntity -> {
                    assertEquals(11, piiEntity.getLength());
                    assertEquals(121, piiEntity.getOffset());
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), PII_ENTITY_OFFSET_INPUT
        );
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizePiiDomainFilterRunner((document, options) ->
            StepVerifier.create(client.recognizePiiEntities(document, "en", options))
                .assertNext(response -> validatePiiEntities(getPiiEntitiesList1ForDomainFilter(),
                    response.stream().collect(Collectors.toList())))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputStringForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizePiiLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, language,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION)))
                .assertNext(response -> validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntitiesForDomainFilter(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitiesRunner((inputs) ->
            StepVerifier.create(client.recognizePiiEntitiesBatchWithResponse(inputs,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION)))
                .assertNext(response -> validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntitiesForDomainFilter(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputForCategoriesFilter(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeStringBatchPiiEntitiesForCategoriesFilterRunner(
            (inputs, options) ->
                StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, "en", options))
                    .assertNext(
                        resultCollection -> validatePiiEntitiesResultCollection(false,
                            getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection))
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntityWithCategoriesFilterFromOtherResult(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);

                // Override whatever the categoriesFiler has currently
                final PiiEntityCategory[] piiEntityCategories = categories.toArray(new PiiEntityCategory[categories.size()]);
                options.setCategoriesFilter(piiEntityCategories);

                // Use the categories from another endpoint to call.
                StepVerifier.create(client.recognizePiiEntitiesBatch(inputs, "en", options))
                    .assertNext(
                        resultCollection -> validatePiiEntitiesResultCollection(false,
                            getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection))
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            });
    }

    // Linked Entities
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeLinkedEntitiesForSingleTextInputRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .assertNext(response -> validateLinkedEntity(getLinkedEntitiesList1().get(0), response.iterator().next()))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyTextRunner(input ->
            StepVerifier.create(client.recognizeLinkedEntities(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass()))
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchLinkedEntityRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, null))
                .assertNext(response -> validateLinkedEntitiesResultCollectionWithResponse(false,
                    getExpectedBatchLinkedEntities(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatchWithResponse(inputs, options))
                .assertNext(response -> validateLinkedEntitiesResultCollectionWithResponse(true, getExpectedBatchLinkedEntities(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeLinkedStringInputRunner((inputs) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, null))
                .assertNext(response -> validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, language, null))
                .assertNext(response -> validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, options))
                .assertNext(response -> validateLinkedEntitiesResultCollection(true, getExpectedBatchLinkedEntities(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.recognizeLinkedEntitiesBatch(inputs, null, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(15, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiFamilyRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(22, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emojiFamilyWithSkinToneModifierRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(30, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        diacriticsNfcRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(14, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        diacriticsNfdRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(15, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        koreanNfcRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        koreanNfdRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(13, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        zalgoTextRunner(document ->
            StepVerifier.create(client.recognizeLinkedEntities(document))
                .assertNext(result -> result.forEach(linkedEntity -> {
                    linkedEntity.getMatches().forEach(linkedEntityMatch -> {
                        assertEquals(9, linkedEntityMatch.getLength());
                        assertEquals(126, linkedEntityMatch.getOffset());
                    });
                }))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT), LINKED_ENTITY_INPUTS.get(1)
        );
    }

    // Key Phrases
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractKeyPhrasesForSingleTextInputRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .assertNext(keyPhrasesCollection -> validateKeyPhrases(asList("monde"),
                    keyPhrasesCollection.stream().collect(Collectors.toList())))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyTextRunner(input ->
            StepVerifier.create(client.extractKeyPhrases(input))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass()))
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractBatchKeyPhrasesRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollectionWithResponse(false, getExpectedBatchKeyPhrases(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatchWithResponse(inputs, options))
                .assertNext(response -> validateExtractKeyPhrasesResultCollectionWithResponse(true, getExpectedBatchKeyPhrases(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractKeyPhrasesStringInputRunner((inputs) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, language, null))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, options))
                .assertNext(response -> validateExtractKeyPhrasesResultCollection(true, getExpectedBatchKeyPhrases(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.extractKeyPhrasesBatch(inputs, null, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    // Sentiment

    /**
     * Test analyzing sentiment for a string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeSentimentForSingleTextInputRunner(input ->
            StepVerifier.create(client.analyzeSentiment(input))
                .assertNext(response -> validateDocumentSentiment(false, getExpectedDocumentSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT)
        );
    }

    /**
     * Test analyzing sentiment for a string input with default language hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithDefaultLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeSentimentForSingleTextInputRunner(input ->
            StepVerifier.create(client.analyzeSentiment(input, null))
                .assertNext(response -> validateDocumentSentiment(false, getExpectedDocumentSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT)
        );
    }

    /**
     * Test analyzing sentiment for a string input and verifying the result of opinion mining.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeSentimentForTextInputWithOpinionMiningRunner((input, options) ->
            StepVerifier.create(client.analyzeSentiment(input, "en", options))
                .assertNext(response -> validateDocumentSentiment(true, getExpectedDocumentSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that an TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyTextRunner(document ->
            StepVerifier.create(client.analyzeSentiment(document))
                .expectErrorMatches(throwable -> throwable instanceof TextAnalyticsException
                    && INVALID_DOCUMENT.equals(((TextAnalyticsException) throwable).getErrorCode()))
                .verify(DEFAULT_TIMEOUT)
        );
    }

    /**
     * Test analyzing sentiment for a duplicate ID list.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, new TextAnalyticsRequestOptions()))
                .expectErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass()))
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that an invalid document exception is returned for input documents with an empty ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeSentimentStringInputRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, new TextAnalyticsRequestOptions()))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(false, false,
                    getExpectedBatchTextSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeSentimentLanguageHintRunner((inputs, language) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, language, new TextAnalyticsRequestOptions()))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(false, false, getExpectedBatchTextSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options.setIncludeOpinionMining(false)))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(true, false, getExpectedBatchTextSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) -> {
            options.setIncludeStatistics(false);
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(false, true, getExpectedBatchTextSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, options))
                .assertNext(response -> validateAnalyzeSentimentResultCollection(true, true, getExpectedBatchTextSentiment(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, (TextAnalyticsRequestOptions) null))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentShowStatsRunner((inputs, requestOptions) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, requestOptions))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, (AnalyzeSentimentOptions) null))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options.setIncludeOpinionMining(false)))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) -> {
            options.setIncludeStatistics(false);
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options))
                .assertNext(response ->
                    validateAnalyzeSentimentResultCollectionWithResponse(false, true, getExpectedBatchTextSentiment(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            StepVerifier.create(client.analyzeSentimentBatchWithResponse(inputs, options))
                .assertNext(response -> validateAnalyzeSentimentResultCollectionWithResponse(true, true, getExpectedBatchTextSentiment(), 200, response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
    }

    /**
     * Verifies that an InvalidDocumentBatch exception is returned for input documents with too many documents.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs ->
            StepVerifier.create(client.analyzeSentimentBatch(inputs, null, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT));
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiWithSkinToneModifier(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamilyWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT),
            SENTIMENT_OFFSET_INPUT
        );
    }

    // Healthcare LRO
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareStringInputWithoutOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        healthcareStringInputRunner((documents, dummyOptions) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                false,
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage(),
                analyzeHealthcareEntitiesPagedFlux.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareStringInputWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        healthcareStringInputRunner((documents, options) -> {
            boolean isValidApiVersionForDisplayName = serviceVersion != TextAnalyticsServiceVersion.V3_0
                && serviceVersion != TextAnalyticsServiceVersion.V3_1;
            if (isValidApiVersionForDisplayName) {
                options.setDisplayName("operationName");
            }
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, "en", options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            PollResponse<AnalyzeHealthcareEntitiesOperationDetail> pollResponse = syncPoller.waitForCompletion();
            if (isValidApiVersionForDisplayName) {
                assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            }
            AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();

            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage(),
                analyzeHealthcareEntitiesPagedFlux.toStream().collect(Collectors.toList()));
        });
    }
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareMaxOverload(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        healthcareLroRunner((documents, options) -> {
            boolean isValidApiVersionForDisplayName = serviceVersion != TextAnalyticsServiceVersion.V3_0
                && serviceVersion != TextAnalyticsServiceVersion.V3_1;
            if (isValidApiVersionForDisplayName) {
                options.setDisplayName("operationName");
            }
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            PollResponse<AnalyzeHealthcareEntitiesOperationDetail> pollResponse = syncPoller.waitForCompletion();
            if (isValidApiVersionForDisplayName) {
                assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            }
            AnalyzeHealthcareEntitiesPagedFlux analyzeHealthcareEntitiesPagedFlux = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage(),
                analyzeHealthcareEntitiesPagedFlux.toStream().collect(Collectors.toList()));
        });
    }
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareLroPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
    public void healthcareLroEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyListRunner((documents, errorMessage) -> {
            StepVerifier.create(client.beginAnalyzeHealthcareEntities(documents, null))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                    && errorMessage.equals(throwable.getMessage()))
                .verify(DEFAULT_TIMEOUT);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void analyzeHealthcareEntitiesEmojiUnicodeCodePoint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
    public void analyzeHealthcareEntitiesDiacriticsNfc(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
    public void analyzeHealthcareEntitiesDiacriticsNfd(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
    @Disabled("Temporary disable it for green test")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void cancelHealthcareLro(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        cancelHealthcareLroRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.cancelOperation();
            LongRunningOperationStatus operationStatus = syncPoller.poll().getStatus();
            while (!LongRunningOperationStatus.USER_CANCELLED.equals(operationStatus)) {
                operationStatus = syncPoller.poll().getStatus();
            }
            syncPoller.waitForCompletion();
            Assertions.assertEquals(LongRunningOperationStatus.USER_CANCELLED, operationStatus);
        });
    }

    // Analyze Actions LRO
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeActionsStringInputRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeLinkedEntitiesResultCollectionForActions(), null))),
                    IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false, null,
                        TIME_NOW,
                        getRecognizePiiEntitiesResultCollection(), null))),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(false, null,
                        TIME_NOW, getExtractKeyPhrasesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(false, null,
                        TIME_NOW, getAnalyzeSentimentResultCollectionForActions(), null))),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeBatchActionsRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks,
                    new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeLinkedEntitiesResultCollectionForActions(), null))),
                    IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false, null,
                        TIME_NOW,
                        getRecognizePiiEntitiesResultCollection(), null))),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(false, null,
                        TIME_NOW, getExtractKeyPhrasesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(false, null,
                        TIME_NOW, getAnalyzeSentimentResultCollectionForActions(), null))),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsWithMultiSameKindActions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeActionsWithMultiSameKindActionsRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());
            actionsResults.forEach(actionsResult -> {
                assertEquals(2, actionsResult.getRecognizeEntitiesResults().stream().count());
                assertEquals(2, actionsResult.getRecognizePiiEntitiesResults().stream().count());
                assertEquals(2, actionsResult.getRecognizeLinkedEntitiesResults().stream().count());
                assertEquals(2, actionsResult.getAnalyzeSentimentResults().stream().count());
                assertEquals(2, actionsResult.getExtractKeyPhrasesResults().stream().count());
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsWithActionNames(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeActionsWithActionNamesRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());
            actionsResults.forEach(actionsResult -> {
                assertEquals(CUSTOM_ACTION_NAME, actionsResult.getRecognizeEntitiesResults().stream()
                                                     .collect(Collectors.toList()).get(0).getActionName());
                assertEquals(CUSTOM_ACTION_NAME, actionsResult.getRecognizePiiEntitiesResults().stream()
                                                     .collect(Collectors.toList()).get(0).getActionName());
                assertEquals(CUSTOM_ACTION_NAME, actionsResult.getRecognizeLinkedEntitiesResults().stream()
                                                     .collect(Collectors.toList()).get(0).getActionName());
                assertEquals(CUSTOM_ACTION_NAME, actionsResult.getAnalyzeSentimentResults().stream()
                                                     .collect(Collectors.toList()).get(0).getActionName());
                assertEquals(CUSTOM_ACTION_NAME, actionsResult.getExtractKeyPhrasesResults().stream()
                                                     .collect(Collectors.toList()).get(0).getActionName());
            });
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/32009")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyListRunner((documents, errorMessage) ->
            StepVerifier.create(client.beginAnalyzeActions(documents,
                new TextAnalyticsActions()
                    .setRecognizeEntitiesActions(new RecognizeEntitiesAction()), null))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                    && errorMessage.equals(throwable.getMessage()))
                .verify(DEFAULT_TIMEOUT));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeEntitiesRecognitionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                        IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(false, null,
                            TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null)
                    )),
                    result.toStream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzePiiEntityRecognitionWithCategoriesFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false, null,
                            TIME_NOW, getExpectedBatchPiiEntitiesForCategoriesFilter(), null))),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null)
                    )),
                    result.toStream().collect(Collectors.toList()));
            }
        );
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzePiiEntityRecognitionWithDomainFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false, null,
                            TIME_NOW, getExpectedBatchPiiEntitiesForDomainFilter(), null))),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null),
                        IterableStream.of(null)
                    )),
                    result.toStream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeLinkedEntityActions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
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
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeLinkedEntitiesResultCollection(), null))),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeKeyPhrasesExtractionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractKeyPhrasesRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(false, null,
                        TIME_NOW, getExtractKeyPhrasesResultCollection(), null))),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeSentimentRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(false, null,
                        TIME_NOW, getExpectedBatchTextSentiment(), null))),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        analyzeHealthcareEntitiesRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedAnalyzeHealthcareEntitiesActionResult(false, null, TIME_NOW,
                        getExpectedAnalyzeHealthcareEntitiesResultCollection(2,
                            asList(
                                getRecognizeHealthcareEntitiesResult1("0"),
                                getRecognizeHealthcareEntitiesResult2())),
                            null))),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.toStream().collect(Collectors.toList()));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeCustomEntitiesAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
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

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void singleLabelClassificationAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        classifyCustomSingleCategoryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getSingleLabelClassifyResults().forEach(
                    customSingleCategoryActionResult -> customSingleCategoryActionResult.getDocumentsResults().forEach(
                        documentResult -> validateLabelClassificationResult(documentResult))));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void multiCategoryClassifyAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        classifyCustomMultiCategoryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getMultiLabelClassifyResults().forEach(
                    customMultiCategoryActionResult -> customMultiCategoryActionResult.getDocumentsResults().forEach(
                        documentResult -> validateLabelClassificationResult(documentResult))));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeCustomEntitiesStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        recognizeCustomEntitiesRunner((documents, parameters) -> {
            SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedFlux> syncPoller =
                client.beginRecognizeCustomEntities(documents, parameters.get(0), parameters.get(1)).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            RecognizeCustomEntitiesPagedFlux pagedFlux = syncPoller.getFinalResult();
            pagedFlux.toStream().collect(Collectors.toList()).forEach(resultCollection ->
                resultCollection.forEach(documentResult ->
                    validateCategorizedEntities(documentResult.getEntities().stream().collect(Collectors.toList()))));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeCustomEntities(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        recognizeCustomEntitiesRunner((documents, parameters) -> {
            RecognizeCustomEntitiesOptions options = new RecognizeCustomEntitiesOptions()
                .setDisplayName("operationName");
            SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedFlux> syncPoller =
                client.beginRecognizeCustomEntities(documents, parameters.get(0), parameters.get(1), "en", options)
                    .getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            PollResponse<RecognizeCustomEntitiesOperationDetail> pollResponse = syncPoller.waitForCompletion();
            assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            RecognizeCustomEntitiesPagedFlux pagedFlux = syncPoller.getFinalResult();
            pagedFlux.toStream().collect(Collectors.toList()).forEach(resultCollection ->
                resultCollection.forEach(documentResult ->
                    validateCategorizedEntities(documentResult.getEntities().stream().collect(Collectors.toList()))));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void singleLabelClassificationStringInput(HttpClient httpClient,
                                                     TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        classifyCustomSingleLabelRunner((documents, parameters) -> {
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> syncPoller =
                client.beginSingleLabelClassify(documents, parameters.get(0), parameters.get(1))
                    .getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ClassifyDocumentPagedFlux pagedFlux = syncPoller.getFinalResult();
            pagedFlux.toStream().collect(Collectors.toList()).forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void singleLabelClassification(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        classifyCustomSingleLabelRunner((documents, parameters) -> {
            SingleLabelClassifyOptions options = new SingleLabelClassifyOptions().setDisplayName("operationName");
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> syncPoller =
                client.beginSingleLabelClassify(documents, parameters.get(0), parameters.get(1), "en", options)
                    .getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            PollResponse<ClassifyDocumentOperationDetail> pollResponse = syncPoller.waitForCompletion();
            assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            ClassifyDocumentPagedFlux pagedFlux = syncPoller.getFinalResult();
            pagedFlux.toStream().collect(Collectors.toList()).forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void multiLabelClassificationStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        classifyCustomMultiLabelRunner((documents, parameters) -> {
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> syncPoller =
                client.beginMultiLabelClassify(documents, parameters.get(0), parameters.get(1))
                    .getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ClassifyDocumentPagedFlux pagedFlux = syncPoller.getFinalResult();
            pagedFlux.toStream().collect(Collectors.toList()).forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void multiLabelClassification(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, true);
        classifyCustomMultiLabelRunner((documents, parameters) -> {
            MultiLabelClassifyOptions options = new MultiLabelClassifyOptions().setDisplayName("operationName");
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> syncPoller =
                client.beginMultiLabelClassify(documents, parameters.get(0), parameters.get(1), "en", options)
                    .getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            PollResponse<ClassifyDocumentOperationDetail> pollResponse = syncPoller.waitForCompletion();
            assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            ClassifyDocumentPagedFlux pagedFlux = syncPoller.getFinalResult();
            pagedFlux.toStream().collect(Collectors.toList()).forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionWithDefaultParameterValues(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            // We are expecting the top 3 highest rank score and these scores are sorted by offset by default
            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExtractiveSummaryActionResult(false, null,
                        TIME_NOW,
                        getExpectedExtractiveSummaryResultCollection(getExpectedExtractiveSummaryResultSortByOffset()),
                        null))),
                    IterableStream.of(null)
                )),
                result.toStream().collect(Collectors.toList()));
        }, null, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionSortedByOffset(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            result.toStream().collect(Collectors.toList()).forEach(
                actionsResult -> actionsResult.getExtractiveSummaryResults().forEach(
                    extractiveSummaryActionResult -> extractiveSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertTrue(isAscendingOrderByOffSet(
                            documentResult.getSentences().stream().collect(Collectors.toList()))))));
        }, 4, ExtractiveSummarySentencesOrder.OFFSET);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionSortedByRankScore(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                actionsResult -> actionsResult.getExtractiveSummaryResults().forEach(
                    extractiveSummaryActionResult -> extractiveSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertTrue(isDescendingOrderByRankScore(
                            documentResult.getSentences().stream().collect(Collectors.toList()))))));
        }, 4, ExtractiveSummarySentencesOrder.RANK);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionWithSentenceCountLessThanMaxCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            result.toStream().collect(Collectors.toList()).forEach(
                actionsResult -> actionsResult.getExtractiveSummaryResults().forEach(
                    extractiveSummaryActionResult -> extractiveSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertTrue(
                            documentResult.getSentences().stream().collect(Collectors.toList()).size() < 20))));
        }, 20, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionWithNonDefaultSentenceCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.toStream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getExtractiveSummaryResults().forEach(
                    extractiveSummaryActionResult -> extractiveSummaryActionResult.getDocumentsResults().forEach(
                        documentResult -> assertEquals(
                            documentResult.getSentences().stream().collect(Collectors.toList()).size(), 5))));
        }, 5, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionMaxSentenceCountInvalidRangeException(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        // The range of max sentences count is at between 1 and 20.
        int[] invalidMaxSentenceCounts = {0, 21};

        for (int invalidCount: invalidMaxSentenceCounts) {
            extractiveSummaryActionRunner(
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
    public void analyzeAbstractiveSummaryActionWithDefaultParameterValues(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        abstractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions()).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedFlux result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(null),
                    IterableStream.of(asList(getAbstractiveSummaryActionResult(false, null,
                        TIME_NOW,
                        new AbstractiveSummaryResultCollection(asList(getExpectedAbstractiveSummaryResult())),
                        null
                    )))
                )),
                result.toStream().collect(Collectors.toList()));
        }, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryDuplicateIdInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs -> {
            StepVerifier.create(client.beginAbstractSummary(inputs, null))
                .expectErrorSatisfies(ex -> assertEquals(HttpResponseException.class, ex.getClass()))
                .verify(DEFAULT_TIMEOUT);
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs -> {
            StepVerifier.create(client.beginAbstractSummary(inputs, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT);
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryTooManyDocuments(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs -> {
            StepVerifier.create(client.beginAbstractSummary(inputs, null, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
                    assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
                })
                .verify(DEFAULT_TIMEOUT);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        abstractiveSummaryRunner((documents, options) -> {
            SyncPoller<AbstractiveSummaryOperationDetail, AbstractiveSummaryPagedFlux> syncPoller =
                client.beginAbstractSummary(documents)
                    .getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AbstractiveSummaryPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                documentResult -> validateAbstractiveSummaryResultCollection(false,
                    new AbstractiveSummaryResultCollection(asList(getExpectedAbstractiveSummaryResult())),
                    documentResult));
        }, 4);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryMaxOverload(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        abstractiveSummaryMaxOverloadRunner((documents, options) -> {
            SyncPoller<AbstractiveSummaryOperationDetail, AbstractiveSummaryPagedFlux> syncPoller =
                client.beginAbstractSummary(documents, options)
                    .getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AbstractiveSummaryPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                documentResult -> validateAbstractiveSummaryResultCollection(false,
                    new AbstractiveSummaryResultCollection(asList(getExpectedAbstractiveSummaryResult())),
                    documentResult));
        }, 4);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummarySortedByOffset(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedFlux> syncPoller =
                client.beginExtractSummary(documents, "en", options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ExtractiveSummaryPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                documentResultCollection -> documentResultCollection.forEach(
                    documentResult -> assertTrue(
                        isAscendingOrderByOffSet(documentResult.getSentences().stream().collect(Collectors.toList())))
                ));
        }, 4, ExtractiveSummarySentencesOrder.OFFSET);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummarySortedByRankScore(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedFlux> syncPoller =
                client.beginExtractSummary(documents, "en", options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ExtractiveSummaryPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                documentResultCollection -> documentResultCollection.forEach(
                    documentResult -> assertTrue(
                        isDescendingOrderByRankScore(documentResult.getSentences().stream().collect(Collectors.toList())))
                ));
        }, 4, ExtractiveSummarySentencesOrder.RANK);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummarySentenceCountLessThanMaxCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedFlux> syncPoller =
                client.beginExtractSummary(documents, "en", options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();

            ExtractiveSummaryPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                documentResultCollection -> documentResultCollection.forEach(
                    documentResult -> assertTrue(
                        documentResult.getSentences().stream().collect(Collectors.toList()).size() < 20)));
        }, 20, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummaryNonDefaultSentenceCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedFlux> syncPoller =
                client.beginExtractSummary(documents, "en", options).getSyncPoller();
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ExtractiveSummaryPagedFlux result = syncPoller.getFinalResult();
            result.toStream().collect(Collectors.toList()).forEach(
                documentResultCollection -> documentResultCollection.forEach(
                    documentResult -> assertEquals(
                        documentResult.getSentences().stream().collect(Collectors.toList()).size(), 5)));
        }, 5, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummaryMaxSentenceCountInvalidRangeException(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsAsyncClient(httpClient, serviceVersion, false);
        // The range of max sentences count is at between 1 and 20.
        int[] invalidMaxSentenceCounts = {0, 21};

        for (int invalidCount: invalidMaxSentenceCounts) {
            extractiveSummaryRunner(
                (documents, options) -> {
                    HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
                        SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedFlux> syncPoller =
                            client.beginExtractSummary(documents, "en", options)
                                .getSyncPoller();
                        syncPoller = setPollInterval(syncPoller);
                        syncPoller.waitForCompletion();
                        ExtractiveSummaryPagedFlux result = syncPoller.getFinalResult();
                    });
                    assertEquals(
                        TextAnalyticsErrorCode.INVALID_PARAMETER_VALUE,
                        ((TextAnalyticsError) exception.getValue()).getErrorCode());
                }, invalidCount, null);
        }
    }
}
