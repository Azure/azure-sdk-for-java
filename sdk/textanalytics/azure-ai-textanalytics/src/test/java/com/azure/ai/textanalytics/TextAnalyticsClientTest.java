// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AbstractiveSummaryOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.ClassifyDocumentOperationDetail;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityConditionality;
import com.azure.ai.textanalytics.models.ExtractiveSummaryOperationDetail;
import com.azure.ai.textanalytics.models.ExtractiveSummarySentencesOrder;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.MultiLabelClassifyOptions;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiEntityDomain;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SingleLabelClassifyOptions;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AbstractiveSummaryPagedIterable;
import com.azure.ai.textanalytics.util.AbstractiveSummaryResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedIterable;
import com.azure.ai.textanalytics.util.ClassifyDocumentPagedIterable;
import com.azure.ai.textanalytics.util.ExtractiveSummaryPagedIterable;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedIterable;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import static com.azure.ai.textanalytics.models.TextAnalyticsErrorCode.INVALID_PARAMETER_VALUE;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {
    private TextAnalyticsClient client;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        TimeUnit.SECONDS.sleep(180);
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    private TextAnalyticsClient getTextAnalyticsClient(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion, boolean isStaticResource) {
        return getTextAnalyticsClientBuilder(
            buildSyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion,
            isStaticResource)
            .buildClient();
    }
    // Detect language

    /**
     * Verify that we can get statistics on the collection result when given a batch of documents with options.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        detectLanguagesBatchListCountryHintWithOptionsRunner((inputs, options) -> validateDetectLanguageResultCollection(true,
            getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs, null, options)));
    }

    /**
     * Test detect batch languages for a list of string input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguagesBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        detectLanguageStringInputRunner((inputs) -> validateDetectLanguageResultCollection(
            false, getExpectedBatchDetectedLanguages(), client.detectLanguageBatch(inputs, null, null)));
    }

    /**
     * Verifies that a single DetectLanguageResult is returned for a document to detect language.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        detectSingleTextLanguageRunner(input ->
            validatePrimaryLanguage(getDetectedLanguageEnglish(), client.detectLanguage(input)));
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.detectLanguage(input));
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    /**
     * Verifies that a bad request exception is returned for input documents with same IDs.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        detectLanguageEmptyCountryHintRunner((input, countryHint) ->
            validatePrimaryLanguage(getDetectedLanguageSpanish(), client.detectLanguage(input, countryHint)));
    }

    /**
     * Verify that with countryHint with "none" will not throw exception.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        detectLanguageNoneCountryHintRunner((input, countryHint) ->
            validatePrimaryLanguage(getDetectedLanguageSpanish(), client.detectLanguage(input, countryHint)));
    }

    // Recognize Entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeCategorizedEntitiesForSingleTextInputRunner(input -> {
            final List<CategorizedEntity> entities = client.recognizeEntities(input).stream().collect(Collectors.toList());
            validateCategorizedEntities(entities);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.recognizeEntities(input).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs -> {
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchCategorizedEntityRunner((inputs) ->
            validateCategorizedEntitiesResultCollectionWithResponse(false, getExpectedBatchCategorizedEntities(), 200,
                client.recognizeEntitiesBatchWithResponse(inputs, null, Context.NONE))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            validateCategorizedEntitiesResultCollectionWithResponse(true, getExpectedBatchCategorizedEntities(), 200,
                client.recognizeEntitiesBatchWithResponse(inputs, options, Context.NONE))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeCategorizedEntityStringInputRunner((inputs) ->
            validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatch(inputs, null, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeCategorizedEntitiesLanguageHintRunner((inputs, language) ->
            validateCategorizedEntitiesResultCollection(false, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatch(inputs, language, null))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesForListWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeStringBatchCategorizedEntitiesShowStatsRunner((inputs, options) ->
            validateCategorizedEntitiesResultCollection(true, getExpectedBatchCategorizedEntities(),
                client.recognizeEntitiesBatch(inputs, null, options))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void recognizeEntitiesBatchWithResponseEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
//        batchEmojiRunner(
//            documents -> client.recognizeEntitiesBatchWithResponse(documents,
//                new RecognizeEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT), Context.NONE)
//                             .getValue().forEach(result -> result.getEntities().forEach(
//                                 categorizedEntity -> {
//                                     assertEquals(9, categorizedEntity.getLength());
//                                     assertEquals(12, categorizedEntity.getOffset());
//                                 })),
//            CATEGORIZED_ENTITY_INPUTS.get(1)
//        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizePiiSingleDocumentRunner(document -> {
            final PiiEntityCollection entities = client.recognizePiiEntities(document);
            validatePiiEntities(getPiiEntitiesList1(), entities.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyTextRunner(document -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class, () ->
                client.recognizePiiEntities(document).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs -> {
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitySingleErrorRunner((inputs) -> {
            Response<RecognizePiiEntitiesResultCollection> response = client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE);
            response.getValue().forEach(recognizePiiEntitiesResult -> {
                Exception exception = assertThrows(TextAnalyticsException.class, recognizePiiEntitiesResult::getEntities);
                assertEquals(String.format(BATCH_ERROR_EXCEPTION_MESSAGE, "RecognizePiiEntitiesResult"), exception.getMessage());
            });
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitiesRunner(inputs ->
            validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntities(), 200,
                client.recognizePiiEntitiesBatchWithResponse(inputs, null, Context.NONE)));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            validatePiiEntitiesResultCollectionWithResponse(true, getExpectedBatchPiiEntities(), 200,
                client.recognizePiiEntitiesBatchWithResponse(inputs, options, Context.NONE)));
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizePiiEntitiesLanguageHintRunner((inputs, language) ->
            validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesBatch(inputs, language, null))
        );
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeStringBatchPiiEntitiesShowStatsRunner((inputs, options) ->
            validatePiiEntitiesResultCollection(true, getExpectedBatchPiiEntities(),
                client.recognizePiiEntitiesBatch(inputs, null, options)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
    public void recognizePiiEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        zalgoTextRunner(document -> {
            final PiiEntityCollection result = client.recognizePiiEntities(document);
            result.forEach(piiEntity -> {
                assertEquals(11, piiEntity.getLength());
                assertEquals(121, piiEntity.getOffset());
            });
        }, PII_ENTITY_OFFSET_INPUT);
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizePiiDomainFilterRunner((document, options) -> {
            final PiiEntityCollection entities = client.recognizePiiEntities(document, "en", options);
            validatePiiEntities(getPiiEntitiesList1ForDomainFilter(), entities.stream().collect(Collectors.toList()));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputStringForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizePiiLanguageHintRunner((inputs, language) -> {
            final RecognizePiiEntitiesResultCollection response = client.recognizePiiEntitiesBatch(inputs, language,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION));
            validatePiiEntitiesResultCollection(false, getExpectedBatchPiiEntitiesForDomainFilter(), response);
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputForDomainFilter(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchPiiEntitiesRunner((inputs) -> {
            final Response<RecognizePiiEntitiesResultCollection> response = client.recognizePiiEntitiesBatchWithResponse(inputs,
                new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION), Context.NONE);
            validatePiiEntitiesResultCollectionWithResponse(false, getExpectedBatchPiiEntitiesForDomainFilter(), 200, response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntitiesForBatchInputForCategoriesFilter(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeStringBatchPiiEntitiesForCategoriesFilterRunner(
            (inputs, options) -> {
                final RecognizePiiEntitiesResultCollection resultCollection =
                    client.recognizePiiEntitiesBatch(inputs, "en", options);
                validatePiiEntitiesResultCollection(false,
                    getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection);
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizePiiEntityWithCategoriesFilterFromOtherResult(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeStringBatchPiiEntitiesForCategoriesFilterRunner(
            (inputs, options) -> {
                List<PiiEntityCategory> categories = new ArrayList<>();
                final RecognizePiiEntitiesResultCollection resultCollection = client.recognizePiiEntitiesBatch(inputs, "en", options);
                resultCollection.forEach(
                    result -> result.getEntities().forEach(
                        piiEntity -> {
                            final PiiEntityCategory category = piiEntity.getCategory();
                            if (PiiEntityCategory.ABA_ROUTING_NUMBER == category
                                    || PiiEntityCategory.US_SOCIAL_SECURITY_NUMBER == category) {
                                categories.add(category);
                            }
                        }));
                validatePiiEntitiesResultCollection(false,
                    getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection);

                // Override whatever the categoriesFiler has currently
                final PiiEntityCategory[] piiEntityCategories = categories.toArray(
                    new PiiEntityCategory[categories.size()]);
                options.setCategoriesFilter(piiEntityCategories);

                // Use the categories from another endpoint to call.
                final RecognizePiiEntitiesResultCollection resultCollection2 = client.recognizePiiEntitiesBatch(
                    inputs, "en", options);
                validatePiiEntitiesResultCollection(false,
                            getExpectedBatchPiiEntitiesForCategoriesFilter(), resultCollection2);
            });
    }

    // Recognize linked entity

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeLinkedEntitiesForSingleTextInputRunner(input -> {
            final List<LinkedEntity> linkedEntities = client.recognizeLinkedEntities(input)
                .stream().collect(Collectors.toList());
            validateLinkedEntity(getLinkedEntitiesList1().get(0), linkedEntities.get(0));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.recognizeLinkedEntities(input).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.recognizeLinkedEntitiesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs -> {
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchLinkedEntityRunner((inputs) ->
            validateLinkedEntitiesResultCollectionWithResponse(false, getExpectedBatchLinkedEntities(), 200,
                client.recognizeLinkedEntitiesBatchWithResponse(inputs, null, Context.NONE))
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchLinkedEntitiesShowStatsRunner((inputs, options) ->
            validateLinkedEntitiesResultCollectionWithResponse(true, getExpectedBatchLinkedEntities(), 200,
                client.recognizeLinkedEntitiesBatchWithResponse(inputs, options, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeLinkedStringInputRunner((inputs) ->
            validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatch(inputs, null, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeLinkedLanguageHintRunner((inputs, language) ->
            validateLinkedEntitiesResultCollection(false, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatch(inputs, language, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        recognizeBatchStringLinkedEntitiesShowStatsRunner((inputs, options) ->
            validateLinkedEntitiesResultCollection(true, getExpectedBatchLinkedEntities(), client.recognizeLinkedEntitiesBatch(inputs, null, options)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeLinkedEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
    public void recognizeLinkedEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractKeyPhrasesForSingleTextInputRunner(input -> {
            final KeyPhrasesCollection keyPhrasesCollection = client.extractKeyPhrases(input);
            validateKeyPhrases(asList("monde"), keyPhrasesCollection.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyTextRunner(input -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.extractKeyPhrases(input).iterator().hasNext());
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.extractKeyPhrasesBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs -> {
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractBatchKeyPhrasesRunner((inputs) ->
            validateExtractKeyPhrasesResultCollectionWithResponse(false, getExpectedBatchKeyPhrases(), 200,
                client.extractKeyPhrasesBatchWithResponse(inputs, null, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractBatchKeyPhrasesShowStatsRunner((inputs, options) ->
            validateExtractKeyPhrasesResultCollectionWithResponse(true, getExpectedBatchKeyPhrases(), 200,
                client.extractKeyPhrasesBatchWithResponse(inputs, options, Context.NONE)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractKeyPhrasesStringInputRunner((inputs) ->
            validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatch(inputs, null, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractKeyPhrasesLanguageHintRunner((inputs, language) ->
            validateExtractKeyPhrasesResultCollection(false, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatch(inputs, language, null)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractBatchStringKeyPhrasesShowStatsRunner((inputs, options) ->
            validateExtractKeyPhrasesResultCollection(true, getExpectedBatchKeyPhrases(), client.extractKeyPhrasesBatch(inputs, null, options)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void extractKeyPhrasesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeSentimentForSingleTextInputRunner(input -> {
            validateDocumentSentiment(false, getExpectedDocumentSentiment(), client.analyzeSentiment(input));
        });
    }

    /**
     * Test analyzing sentiment for a string input with default language hint.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithDefaultLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeSentimentForSingleTextInputRunner(input -> {
            final DocumentSentiment analyzeSentimentResult = client.analyzeSentiment(input, null);
            validateDocumentSentiment(false, getExpectedDocumentSentiment(), analyzeSentimentResult);
        });
    }

    /**
     * Test analyzing sentiment for a string input and verifying the result of opinion mining.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForTextInputWithOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeSentimentForTextInputWithOpinionMiningRunner((input, options) -> {
            final DocumentSentiment analyzeSentimentResult =
                client.analyzeSentiment(input, "en", options);
            validateDocumentSentiment(true, getExpectedDocumentSentiment(), analyzeSentimentResult);
        });
    }

    /**
     * Verifies that a TextAnalyticsException is thrown for an empty document.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyTextRunner(document -> {
            final TextAnalyticsException exception = assertThrows(TextAnalyticsException.class,
                () -> client.analyzeSentiment(document));
            assertEquals(INVALID_DOCUMENT, exception.getErrorCode());
        });
    }

    /**
     * Test analyzing sentiment for a duplicate ID list.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs -> {
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.analyzeSentimentBatchWithResponse(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * String documents with null TextAnalyticsRequestOptions and null language code which will use the default language
     * code, 'en'.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions is null and null language code which will use the default language code, 'en'.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeSentimentStringInputRunner(inputs ->
            validateAnalyzeSentimentResultCollection(false, false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, new TextAnalyticsRequestOptions())));
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * String documents with null TextAnalyticsRequestOptions and given a language code.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     * which TextAnalyticsRequestOptions is null and given a language code.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringWithLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeSentimentLanguageHintRunner((inputs, language) ->
            validateAnalyzeSentimentResultCollection(false, false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, language, new TextAnalyticsRequestOptions())));
    }

    /**
     * Verify that the collection result includes request statistics but not sentence options when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which to show the request statistics only and verify the analyzed sentiment result.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            validateAnalyzeSentimentResultCollection(true, false, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, options.setIncludeOpinionMining(false))));
    }

    /**
     * Verify that the collection result includes sentence options but not request statistics when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) -> {
            options.setIncludeStatistics(false);
            validateAnalyzeSentimentResultCollection(false, true, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, options));
        });
    }

    /**
     * Verify that the collection result includes sentence options and request statistics when given a batch of
     * String documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForListStringShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner((inputs, options) ->
            validateAnalyzeSentimentResultCollection(true, true, getExpectedBatchTextSentiment(),
                client.analyzeSentimentBatch(inputs, null, options)));
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * TextDocumentInput documents with null TextAnalyticsRequestOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     * which TextAnalyticsRequestOptions is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputWithNullRequestOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentRunner(inputs ->
            validateAnalyzeSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200,
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentShowStatsRunner((inputs, requestOptions) ->
            validateAnalyzeSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, requestOptions, Context.NONE)));
    }

    /**
     * Verify that the collection result excludes request statistics and sentence options when given a batch of
     * TextDocumentInput documents with null AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputWithNullAnalyzeSentimentOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            validateAnalyzeSentimentResultCollectionWithResponse(false, false, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, (AnalyzeSentimentOptions) null, Context.NONE)));
    }

    /**
     * Verify that the collection result includes request statistics but not sentence options when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions includes request statistics but not opinion mining.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            validateAnalyzeSentimentResultCollectionWithResponse(true, false, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, options.setIncludeOpinionMining(false), Context.NONE)));
    }

    /**
     * Verify that the collection result includes sentence options but not request statistics when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions includes opinion mining but not request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) -> {
            options.setIncludeStatistics(false);
            validateAnalyzeSentimentResultCollectionWithResponse(false, true, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, options, Context.NONE));
        });
    }

    /**
     * Verify that the collection result includes sentence options and request statistics when given a batch of
     * TextDocumentInput documents with AnalyzeSentimentOptions.
     *
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     * which AnalyzeSentimentOptions includes opinion mining and request statistics.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentForBatchInputShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchSentimentOpinionMining((inputs, options) ->
            validateAnalyzeSentimentResultCollectionWithResponse(true, true, getExpectedBatchTextSentiment(), 200,
                client.analyzeSentimentBatchWithResponse(inputs, options, Context.NONE)));
    }

    /**
     * Verifies that an InvalidDocumentBatch exception is returned for input documents with too many documents.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.analyzeSentimentBatch(inputs, null, null).stream().findFirst().get());
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT_BATCH, textAnalyticsError.getErrorCode());
        });
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(
                        sentenceSentiment -> {
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
                        }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiWithSkinToneModifierRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiFamilyRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentEmojiFamilyWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiFamilyWithSkinToneModifierRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        diacriticsNfcRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @Disabled("https://dev.azure.com/msazure/Cognitive%20Services/_workitems/edit/14262098")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        diacriticsNfdRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        koreanNfcRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        koreanNfdRunner(
            document ->
                client.analyzeSentiment(document, null, new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        zalgoTextRunner(
            document ->
                client.analyzeSentiment(document, null,
                    new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
                    .getSentences()
                    .forEach(sentenceSentiment -> {
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
                    }),
            SENTIMENT_OFFSET_INPUT
        );
    }

    // Healthcare LRO
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareStringInputWithoutOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        healthcareStringInputRunner((documents, dummyOptions) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                false,
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage(),
                analyzeHealthcareEntitiesPagedIterable.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareStringInputWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        healthcareStringInputRunner((documents, options) -> {
            boolean isValidApiVersionForDisplayName = serviceVersion != TextAnalyticsServiceVersion.V3_0
                && serviceVersion != TextAnalyticsServiceVersion.V3_1;
            if (isValidApiVersionForDisplayName) {
                options.setDisplayName("operationName");
            }
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, "en", options);
            syncPoller = setPollInterval(syncPoller);
            PollResponse<AnalyzeHealthcareEntitiesOperationDetail> pollResponse = syncPoller.waitForCompletion();
            if (isValidApiVersionForDisplayName) {
                assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            }
            AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage(),
                analyzeHealthcareEntitiesPagedIterable.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareMaxOverload(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        healthcareLroRunner((documents, options) -> {
            boolean isValidApiVersionForDisplayName = serviceVersion != TextAnalyticsServiceVersion.V3_0
                && serviceVersion != TextAnalyticsServiceVersion.V3_1;
            if (isValidApiVersionForDisplayName) {
                options.setDisplayName("operationName");
            }
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
            syncPoller = setPollInterval(syncPoller);
            PollResponse<AnalyzeHealthcareEntitiesOperationDetail> pollResponse = syncPoller.waitForCompletion();
            if (isValidApiVersionForDisplayName) {
                assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            }
            AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage(),
                analyzeHealthcareEntitiesPagedIterable.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareLroPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        healthcareLroPaginationRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
            validateAnalyzeHealthcareEntitiesResultCollectionList(
                options.isIncludeStatistics(),
                getExpectedAnalyzeHealthcareEntitiesResultCollectionListForMultiplePages(0, 10, 0),
                analyzeHealthcareEntitiesPagedIterable.stream().collect(Collectors.toList()));
        }, 10);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void healthcareLroEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyListRunner((documents, errorMessage) -> {
            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> client.beginAnalyzeHealthcareEntities(documents, null, Context.NONE).getFinalResult());
            assertEquals(errorMessage, exception.getMessage());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/22257")
    public void analyzeHealthcareEntitiesEmojiUnicodeCodePoint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
//        emojiRunner(
//            document -> {
//                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail,
//                              PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
//                    syncPoller = client.beginAnalyzeHealthcareEntities(
//                    Collections.singletonList(
//                        new TextDocumentInput("0", document)),
//                        new AnalyzeHealthcareEntitiesOptions().setStringIndexType(StringIndexType.UNICODE_CODE_POINT),
//                    Context.NONE);
//                syncPoller = setPollInterval(syncPoller);
//                syncPoller.waitForCompletion();
//                PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareEntitiesResultCollection
//                    = syncPoller.getFinalResult();
//                healthcareEntitiesResultCollection.forEach(
//                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
//                        entity -> {
//                            assertEquals(11, entity.getLength());
//                            assertEquals(19, entity.getOffset());
//                        })));
//            },
//            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiWithSkinToneModifierRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiFamilyRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emojiFamilyWithSkinToneModifierRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        diacriticsNfcRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        diacriticsNfdRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        koreanNfcRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        koreanNfdRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        zalgoTextRunner(
            document -> {
                SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                    syncPoller = client.beginAnalyzeHealthcareEntities(
                    Collections.singletonList(new TextDocumentInput("0", document)), null, Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
                analyzeHealthcareEntitiesPagedIterable.forEach(
                    result -> result.forEach(entitiesResult -> entitiesResult.getEntities().forEach(
                        entity -> {
                            assertEquals(11, entity.getLength());
                            assertEquals(133, entity.getOffset());
                        })));
            },
            HEALTHCARE_ENTITY_OFFSET_INPUT);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareEntitiesForAssertion(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeHealthcareEntitiesForAssertionRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, "en", options);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeHealthcareEntitiesPagedIterable analyzeHealthcareEntitiesPagedIterable = syncPoller.getFinalResult();
            // "All female participants that are premenopausal will be required to have a pregnancy test;
            // any participant who is pregnant or breastfeeding will not be included"
            final HealthcareEntityAssertion assertion =
                analyzeHealthcareEntitiesPagedIterable.stream().collect(Collectors.toList())
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        cancelHealthcareLroRunner((documents, options) -> {
            SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
                syncPoller = client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
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

    // Analyze actions
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeActionsStringInputRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeLinkedEntitiesResultCollectionForActions(), null))),
                    IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizePiiEntitiesResultCollection(), null))),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(false, null,
                        TIME_NOW, getExtractKeyPhrasesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(false, null,
                        TIME_NOW, getAnalyzeSentimentResultCollectionForActions(), null))),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchActionsRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks,
                    new AnalyzeActionsOptions().setIncludeStatistics(false), Context.NONE);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
                Arrays.asList(getExpectedAnalyzeBatchActionsResult(
                    IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeEntitiesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizeLinkedEntitiesResultCollectionForActions(), null))),
                    IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(false, null,
                        TIME_NOW, getRecognizePiiEntitiesResultCollection(), null))),
                    IterableStream.of(null),
                    IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(false, null,
                        TIME_NOW, getExtractKeyPhrasesResultCollection(), null))),
                    IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(false, null,
                        TIME_NOW, getAnalyzeSentimentResultCollectionForActions(), null))),
                    IterableStream.of(null),
                    IterableStream.of(null)
                )),
                result.stream().collect(Collectors.toList()));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsWithMultiSameKindActions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeActionsWithMultiSameKindActionsRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, null, null);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
            final List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeActionsWithActionNamesRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, null, Context.NONE);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
            final List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeBatchActionsPaginationRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable>
                syncPoller = client.beginAnalyzeActions(
                    documents, tasks, new AnalyzeActionsOptions().setIncludeStatistics(false), Context.NONE);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
            validateAnalyzeBatchActionsResultList(false, false,
                getExpectedAnalyzeActionsResultListForMultiplePages(0, 20, 2),
                result.stream().collect(Collectors.toList()));
        }, 22);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeActionsEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyListRunner((documents, errorMessage) -> {
            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> client.beginAnalyzeActions(documents,
                    new TextAnalyticsActions().setRecognizeEntitiesActions(new RecognizeEntitiesAction()),
                    null, Context.NONE)
                    .getFinalResult());
            assertEquals(errorMessage, exception.getMessage());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeEntitiesRecognitionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeEntitiesRecognitionRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                    client.beginAnalyzeActions(documents, tasks,
                        new AnalyzeActionsOptions().setIncludeStatistics(false), Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

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
                    result.stream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzePiiEntityRecognitionWithCategoriesFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzePiiEntityRecognitionWithCategoriesFiltersRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                    client.beginAnalyzeActions(documents, tasks,
                        new AnalyzeActionsOptions().setIncludeStatistics(false), Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

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
                    result.stream().collect(Collectors.toList()));
            }
        );
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/35642")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzePiiEntityRecognitionWithDomainFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzePiiEntityRecognitionWithDomainFiltersRunner(
            (documents, tasks) -> {
                SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                    client.beginAnalyzeActions(documents, tasks,
                        new AnalyzeActionsOptions().setIncludeStatistics(false), Context.NONE);
                syncPoller = setPollInterval(syncPoller);
                syncPoller.waitForCompletion();
                AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

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
                    result.stream().collect(Collectors.toList()));
            }
        );
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeLinkedEntityActions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeLinkedEntityRecognitionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false));
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            validateAnalyzeBatchActionsResultList(false, false,
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
                result.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeKeyPhrasesExtractionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractKeyPhrasesRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false));
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

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
                result.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeSentimentAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeSentimentRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en",
                    new AnalyzeActionsOptions().setIncludeStatistics(false));
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

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
                result.stream().collect(Collectors.toList()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeHealthcareAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        analyzeHealthcareEntitiesRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

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
                result.stream().collect(Collectors.toList()));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeCustomEntitiesAction(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        recognizeCustomEntitiesActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());

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
    public void singleLabelClassificationAction(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        classifyCustomSingleCategoryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());

            actionsResults.forEach(
                actionsResult -> actionsResult.getSingleLabelClassifyResults().forEach(
                    customSingleCategoryActionResult -> customSingleCategoryActionResult.getDocumentsResults().forEach(
                        documentResult -> validateLabelClassificationResult(documentResult))));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void multiCategoryClassifyAction(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        classifyCustomMultiCategoryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", null);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());

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
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        recognizeCustomEntitiesRunner((documents, parameters) -> {
            SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable> syncPoller =
                client.beginRecognizeCustomEntities(documents, parameters.get(0), parameters.get(1));
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            RecognizeCustomEntitiesPagedIterable pagedIterable = syncPoller.getFinalResult();
            pagedIterable.forEach(resultCollection ->
                resultCollection.forEach(documentResult ->
                    validateCategorizedEntities(documentResult.getEntities().stream().collect(Collectors.toList()))));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void recognizeCustomEntities(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        recognizeCustomEntitiesRunner((documents, parameters) -> {
            RecognizeCustomEntitiesOptions options = new RecognizeCustomEntitiesOptions()
                .setDisplayName("operationName");
            SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable> syncPoller =
                client.beginRecognizeCustomEntities(documents, parameters.get(0), parameters.get(1), "en", options);
            syncPoller = setPollInterval(syncPoller);
            PollResponse<RecognizeCustomEntitiesOperationDetail> pollResponse = syncPoller.waitForCompletion();
            assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            RecognizeCustomEntitiesPagedIterable pagedIterable = syncPoller.getFinalResult();
            pagedIterable.forEach(resultCollection ->
                resultCollection.forEach(documentResult ->
                    validateCategorizedEntities(documentResult.getEntities().stream().collect(Collectors.toList()))));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void singleLabelClassificationStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        classifyCustomSingleLabelRunner((documents, parameters) -> {
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable> syncPoller =
                client.beginSingleLabelClassify(documents, parameters.get(0), parameters.get(1));
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ClassifyDocumentPagedIterable pagedIterable = syncPoller.getFinalResult();
            pagedIterable.forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void singleLabelClassification(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        classifyCustomSingleLabelRunner((documents, parameters) -> {
            SingleLabelClassifyOptions options = new SingleLabelClassifyOptions().setDisplayName("operationName");
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable> syncPoller =
                client.beginSingleLabelClassify(documents, parameters.get(0), parameters.get(1), "en", options);
            syncPoller = setPollInterval(syncPoller);
            PollResponse<ClassifyDocumentOperationDetail> pollResponse = syncPoller.waitForCompletion();
            assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            ClassifyDocumentPagedIterable pagedIterable = syncPoller.getFinalResult();
            pagedIterable.forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void multiLabelClassificationStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        classifyCustomMultiLabelRunner((documents, parameters) -> {
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable> syncPoller =
                client.beginMultiLabelClassify(documents, parameters.get(0), parameters.get(1));
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ClassifyDocumentPagedIterable pagedIterable = syncPoller.getFinalResult();
            pagedIterable.forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void multiLabelClassification(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, true);
        classifyCustomMultiLabelRunner((documents, parameters) -> {
            MultiLabelClassifyOptions options = new MultiLabelClassifyOptions().setDisplayName("operationName");
            SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable> syncPoller =
                client.beginMultiLabelClassify(documents, parameters.get(0), parameters.get(1), "en", options);
            syncPoller = setPollInterval(syncPoller);
            PollResponse<ClassifyDocumentOperationDetail> pollResponse = syncPoller.waitForCompletion();
            assertEquals(options.getDisplayName(), pollResponse.getValue().getDisplayName());
            ClassifyDocumentPagedIterable pagedIterable = syncPoller.getFinalResult();
            pagedIterable.forEach(resultCollection ->
                resultCollection.forEach(documentResult -> validateLabelClassificationResult(documentResult)));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionWithDefaultParameterValues(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions());
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
            // We are expecting the top 3 highest rank score ann these score are sorted by offset by default
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
                result.stream().collect(Collectors.toList()));
        }, null, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeExtractSummaryActionSortedByOffset(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions());
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());
            actionsResults.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions());
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
            final List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());
            actionsResults.forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions());
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            result.stream().collect(Collectors.toList()).forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions());
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();

            final List<AnalyzeActionsResult> actionsResults = result.stream().collect(Collectors.toList());

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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        // The range of max sentences count is at between 1 and 20.
        int[] invalidMaxSentenceCounts = {0, 21};

        for (int invalidCount: invalidMaxSentenceCounts) {
            extractiveSummaryActionRunner(
                (documents, tasks) -> {
                    HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
                        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                            client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions());
                        syncPoller = setPollInterval(syncPoller);
                        syncPoller.waitForCompletion();
                        AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
                    });
                    assertEquals(
                        INVALID_PARAMETER_VALUE,
                        ((TextAnalyticsError) exception.getValue()).getErrorCode());
                }, invalidCount, null);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void analyzeAbstractiveSummaryActionWithDefaultParameterValues(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        abstractiveSummaryActionRunner((documents, tasks) -> {
            SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
                client.beginAnalyzeActions(documents, tasks, "en", new AnalyzeActionsOptions());
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
            // We are expecting the top 3 highest rank score ann these score are sorted by offset by default
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
                result.stream().collect(Collectors.toList()));
        }, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryDuplicateIdInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        duplicateIdRunner(inputs -> {
            final HttpResponseException response = assertThrows(HttpResponseException.class,
                () -> client.beginAbstractSummary(inputs, null, Context.NONE));
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponse().getStatusCode());
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        emptyDocumentIdRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAbstractSummary(inputs, null, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_DOCUMENT, textAnalyticsError.getErrorCode());
        });
    }

    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33555")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryTooManyDocuments(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        tooManyDocumentsRunner(inputs -> {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginAbstractSummary(inputs, null, null).getFinalResult());
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
            final TextAnalyticsError textAnalyticsError = (TextAnalyticsError) httpResponseException.getValue();
            assertEquals(INVALID_PARAMETER_VALUE, textAnalyticsError.getErrorCode());
        });

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        abstractiveSummaryRunner((documents, options) -> {
            SyncPoller<AbstractiveSummaryOperationDetail, AbstractiveSummaryPagedIterable> syncPoller =
                client.beginAbstractSummary(documents);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AbstractiveSummaryPagedIterable result = syncPoller.getFinalResult();
            result.stream().collect(Collectors.toList()).forEach(
                documentResult -> validateAbstractiveSummaryResultCollection(false,
                    new AbstractiveSummaryResultCollection(asList(getExpectedAbstractiveSummaryResult())),
                    documentResult));
        }, 4);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginAbstractSummaryMaxOverload(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        abstractiveSummaryMaxOverloadRunner((documents, options) -> {
            SyncPoller<AbstractiveSummaryOperationDetail, AbstractiveSummaryPagedIterable> syncPoller =
                client.beginAbstractSummary(documents, options, Context.NONE);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            AbstractiveSummaryPagedIterable result = syncPoller.getFinalResult();
            result.stream().collect(Collectors.toList()).forEach(
                documentResult -> validateAbstractiveSummaryResultCollection(false,
                    new AbstractiveSummaryResultCollection(asList(getExpectedAbstractiveSummaryResult())),
                    documentResult));
        }, 4);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummarySortedByOffset(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedIterable> syncPoller =
                client.beginExtractSummary(documents, "en", options);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ExtractiveSummaryPagedIterable result = syncPoller.getFinalResult();
            result.stream().collect(Collectors.toList()).forEach(
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
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedIterable> syncPoller =
                client.beginExtractSummary(documents, "en", options);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ExtractiveSummaryPagedIterable result = syncPoller.getFinalResult();
            result.stream().collect(Collectors.toList()).forEach(
                documentResultCollection -> documentResultCollection.forEach(
                    documentResult -> assertTrue(
                        isDescendingOrderByRankScore(
                            documentResult.getSentences().stream().collect(Collectors.toList())))
                ));
        }, 4, ExtractiveSummarySentencesOrder.RANK);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummarySentenceCountLessThanMaxCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedIterable> syncPoller =
                client.beginExtractSummary(documents, "en", options);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();

            ExtractiveSummaryPagedIterable result = syncPoller.getFinalResult();
            result.stream().collect(Collectors.toList()).forEach(
                documentResultCollection -> documentResultCollection.forEach(
                    documentResult -> assertTrue(
                        documentResult.getSentences().stream().collect(Collectors.toList()).size() < 20)));
        }, 20, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummaryNonDefaultSentenceCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        extractiveSummaryRunner((documents, options) -> {
            SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedIterable> syncPoller =
                client.beginExtractSummary(documents, "en", options);
            syncPoller = setPollInterval(syncPoller);
            syncPoller.waitForCompletion();
            ExtractiveSummaryPagedIterable result = syncPoller.getFinalResult();
            result.stream().collect(Collectors.toList()).forEach(
                documentResultCollection -> documentResultCollection.forEach(
                    documentResult -> assertEquals(
                        documentResult.getSentences().stream().collect(Collectors.toList()).size(), 5)));
        }, 5, null);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.textanalytics.TestUtils#getTestParameters")
    public void beginExtractSummaryMaxSentenceCountInvalidRangeException(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion) {
        client = getTextAnalyticsClient(httpClient, serviceVersion, false);
        // The range of max sentences count is at between 1 and 20.
        int[] invalidMaxSentenceCounts = {0, 21};

        for (int invalidCount: invalidMaxSentenceCounts) {
            extractiveSummaryRunner(
                (documents, options) -> {
                    HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
                        SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedIterable> syncPoller =
                            client.beginExtractSummary(documents, "en", options);
                        syncPoller = setPollInterval(syncPoller);
                        syncPoller.waitForCompletion();
                        ExtractiveSummaryPagedIterable result = syncPoller.getFinalResult();
                    });
                    assertEquals(
                        INVALID_PARAMETER_VALUE,
                        ((TextAnalyticsError) exception.getValue()).getErrorCode());
                }, invalidCount, null);
        }
    }
}
