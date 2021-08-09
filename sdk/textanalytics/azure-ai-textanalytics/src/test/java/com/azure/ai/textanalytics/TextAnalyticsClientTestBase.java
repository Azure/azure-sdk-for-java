// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentAction;
import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesAction;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.ExtractSummaryAction;
import com.azure.ai.textanalytics.models.ExtractSummaryActionResult;
import com.azure.ai.textanalytics.models.ExtractSummaryResult;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiEntityDomain;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SentenceOpinion;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SummarySentence;
import com.azure.ai.textanalytics.models.SummarySentencesOrder;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.ExtractSummaryResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.CATEGORIZED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.DETECT_LANGUAGE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.FAKE_API_KEY;
import static com.azure.ai.textanalytics.TestUtils.HEALTHCARE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.LINKED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.PII_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SENTIMENT_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SUMMARY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.TOO_LONG_INPUT;
import static com.azure.ai.textanalytics.TestUtils.getDuplicateTextDocumentInputs;
import static com.azure.ai.textanalytics.TestUtils.getWarningsTextDocumentInputs;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TextAnalyticsClientTestBase extends TestBase {
    static final String BATCH_ERROR_EXCEPTION_MESSAGE = "Error in accessing the property on document id: 2, when %s returned with an error: Document text is empty. ErrorCodeValue: {InvalidDocument}";
    static final String INVALID_DOCUMENT_BATCH_NPE_MESSAGE = "'documents' cannot be null.";
    static final String INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE = "'documents' cannot be empty.";
    static final String INVALID_DOCUMENT_NPE_MESSAGE = "'document' cannot be null.";
    static final String WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE = "The document contains very long words (longer than 64 characters). These words will be truncated and may result in unreliable model predictions.";
    static final String REDACTED = "REDACTED";
    static InterceptorManager interceptorManagerTestBase;
    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = DEFAULT_POLL_INTERVAL;
        }
        interceptorManagerTestBase = interceptorManager;
    }

    protected <T, U> SyncPoller<T, U> setPollInterval(SyncPoller<T, U> syncPoller) {
        return syncPoller.setPollInterval(durationTestMode);
    }

    // Detect Language
    @Test
    abstract void detectLanguagesBatchInputShowStatistics(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguagesBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguagesBatchListCountryHint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguagesBatchListCountryHintWithOptions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguagesBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageInvalidCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageEmptyCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageNoneCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Categorized Entities
    @Test
    abstract void recognizeEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesBatchInputSingleError(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForBatchStringInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForListLanguageHint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForListWithOptions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesBatchTooManyDocuments(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesBatchWithResponseEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Personally Identifiable Information Entities
    @Test
    abstract void recognizePiiEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesDuplicateIdInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesBatchInputSingleError(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForBatchInputShowStatistics(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForListLanguageHint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForListStringWithOptions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesBatchTooManyDocuments(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesBatchWithResponseEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForDomainFilter(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForBatchInputStringForDomainFilter(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForBatchInputForDomainFilter(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForBatchInputForCategoriesFilter(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntityWithCategoriesFilterFromOtherResult(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    // Linked Entities
    @Test
    abstract void recognizeLinkedEntitiesForTextInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForFaultyText(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesEmptyIdInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForBatchStringInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForListStringWithOptions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesBatchTooManyDocuments(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesBatchWithResponseEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesEmojiFamilyWIthSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesDiacriticsNfc(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesDiacriticsNfd(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Key Phrases
    @Test
    abstract void extractKeyPhrasesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForBatchStringInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForListLanguageHint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForListStringWithOptions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesBatchWarning(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesBatchTooManyDocuments(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    // Sentiment
    @Test
    abstract void analyzeSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForTextInputWithDefaultLanguageHint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForTextInputWithOpinionMining(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchStringInput(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringWithLanguageHint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringShowStatisticsExcludeOpinionMining(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringShowStatisticsAndIncludeOpinionMining(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputWithNullRequestOptions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputShowStatistics(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputWithNullAnalyzeSentimentOptions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputShowStatisticsExcludeOpinionMining(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputShowStatisticsAndIncludeOpinionMining(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentBatchTooManyDocuments(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentEmoji(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentBatchWithResponseEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentEmojiFamily(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentEmojiFamilyWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentDiacriticsNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentDiacriticsNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Healthcare LRO
    @Test
    abstract void healthcareLroWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void healthcareLroPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void healthcareLroEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesEmojiUnicodeCodePoint(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesEmoji(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesEmojiWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesEmojiFamily(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesEmojiFamilyWithSkinToneModifier(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesDiacriticsNfc(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesDiacriticsNfd(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesKoreanNfc(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesKoreanNfd(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesZalgoText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeHealthcareEntitiesForAssertion(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Healthcare LRO - Cancellation

    @Test
    abstract void cancelHealthcareLro(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Analyze multiple actions
    @Test
    abstract void analyzeActionsWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeActionsPagination(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeActionsEmptyInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeEntitiesRecognitionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzePiiEntityRecognitionWithCategoriesFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzePiiEntityRecognitionWithDomainFilters(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeLinkedEntityActions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeKeyPhrasesExtractionAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Extractive Summarization
//    @Test
//    abstract void analyzeExtractSummaryActionWithDefaultParameterValues(HttpClient httpClient,
//        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionSortedByOffset(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionSortedByRankScore(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionWithSentenceCountLessThanMaxCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionWithNonDefaultSentenceCount(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionMaxSentenceCountInvalidRangeException(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    // Detect Language runner
    void detectLanguageShowStatisticsRunner(BiConsumer<List<DetectLanguageInput>,
        TextAnalyticsRequestOptions> testRunner) {
        final List<DetectLanguageInput> detectLanguageInputs = TestUtils.getDetectLanguageInputs();

        TextAnalyticsRequestOptions options = new TextAnalyticsRequestOptions().setIncludeStatistics(true);
        testRunner.accept(detectLanguageInputs, options);
    }

    void detectLanguageDuplicateIdRunner(BiConsumer<List<DetectLanguageInput>,
        TextAnalyticsRequestOptions> testRunner) {
        testRunner.accept(TestUtils.getDuplicateIdDetectLanguageInputs(), null);
    }

    void detectLanguagesCountryHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(DETECT_LANGUAGE_INPUTS, "US");
    }

    void detectLanguagesBatchListCountryHintWithOptionsRunner(BiConsumer<List<String>,
        TextAnalyticsRequestOptions> testRunner) {
        TextAnalyticsRequestOptions options = new TextAnalyticsRequestOptions().setIncludeStatistics(true);
        testRunner.accept(TestUtils.DETECT_LANGUAGE_INPUTS, options);
    }

    void detectLanguageStringInputRunner(Consumer<List<String>> testRunner) {
        testRunner.accept(DETECT_LANGUAGE_INPUTS);
    }

    void detectLanguageRunner(Consumer<List<DetectLanguageInput>> testRunner) {
        testRunner.accept(TestUtils.getDetectLanguageInputs());
    }

    void detectSingleTextLanguageRunner(Consumer<String> testRunner) {
        testRunner.accept(DETECT_LANGUAGE_INPUTS.get(0));
    }

    void detectLanguageInvalidCountryHintRunner(BiConsumer<String, String> testRunner) {
        testRunner.accept(DETECT_LANGUAGE_INPUTS.get(1), "en");
    }

    void detectLanguageEmptyCountryHintRunner(BiConsumer<String, String> testRunner) {
        testRunner.accept(DETECT_LANGUAGE_INPUTS.get(1), "");
    }

    void detectLanguageNoneCountryHintRunner(BiConsumer<String, String> testRunner) {
        testRunner.accept(DETECT_LANGUAGE_INPUTS.get(1), "none");
    }

    // Categorized Entity runner
    void recognizeCategorizedEntitiesForSingleTextInputRunner(Consumer<String> testRunner) {
        testRunner.accept(CATEGORIZED_ENTITY_INPUTS.get(0));
    }

    void recognizeCategorizedEntityStringInputRunner(Consumer<List<String>> testRunner) {
        testRunner.accept(CATEGORIZED_ENTITY_INPUTS);
    }

    void recognizeCategorizedEntityDuplicateIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(getDuplicateTextDocumentInputs());
    }

    void recognizeCategorizedEntitiesLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(CATEGORIZED_ENTITY_INPUTS, "en");
    }

    void recognizeBatchCategorizedEntitySingleErrorRunner(Consumer<List<TextDocumentInput>> testRunner) {
        List<TextDocumentInput> inputs = Collections.singletonList(new TextDocumentInput("2", " "));
        testRunner.accept(inputs);
    }

    void recognizeBatchCategorizedEntityRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(TestUtils.getTextDocumentInputs(CATEGORIZED_ENTITY_INPUTS));
    }

    void recognizeBatchCategorizedEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = TestUtils.getTextDocumentInputs(CATEGORIZED_ENTITY_INPUTS);
        TextAnalyticsRequestOptions options = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        testRunner.accept(textDocumentInputs, options);
    }

    void recognizeStringBatchCategorizedEntitiesShowStatsRunner(
        BiConsumer<List<String>, TextAnalyticsRequestOptions> testRunner) {
        testRunner.accept(CATEGORIZED_ENTITY_INPUTS, new TextAnalyticsRequestOptions().setIncludeStatistics(true));
    }

    // Personally Identifiable Information Entity runner
    void recognizePiiSingleDocumentRunner(Consumer<String> testRunner) {
        testRunner.accept(PII_ENTITY_INPUTS.get(0));
    }

    void recognizePiiDomainFilterRunner(BiConsumer<String, RecognizePiiEntitiesOptions> testRunner) {
        testRunner.accept(PII_ENTITY_INPUTS.get(0),
            new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION));
    }

    void recognizePiiLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(PII_ENTITY_INPUTS, "en");
    }

    void recognizeBatchPiiEntityDuplicateIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(getDuplicateTextDocumentInputs());
    }

    void recognizePiiEntitiesLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(PII_ENTITY_INPUTS, "en");
    }

    void recognizeBatchPiiEntitySingleErrorRunner(Consumer<List<TextDocumentInput>> testRunner) {
        List<TextDocumentInput> inputs = Collections.singletonList(new TextDocumentInput("2", " "));
        testRunner.accept(inputs);
    }

    void recognizeBatchPiiEntitiesRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(TestUtils.getTextDocumentInputs(PII_ENTITY_INPUTS));
    }

    void recognizeBatchPiiEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, RecognizePiiEntitiesOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = TestUtils.getTextDocumentInputs(PII_ENTITY_INPUTS);
        RecognizePiiEntitiesOptions options = new RecognizePiiEntitiesOptions().setIncludeStatistics(true);

        testRunner.accept(textDocumentInputs, options);
    }

    void recognizeStringBatchPiiEntitiesShowStatsRunner(
        BiConsumer<List<String>, RecognizePiiEntitiesOptions> testRunner) {
        testRunner.accept(PII_ENTITY_INPUTS, new RecognizePiiEntitiesOptions().setIncludeStatistics(true));
    }

    void recognizeStringBatchPiiEntitiesForCategoriesFilterRunner(
        BiConsumer<List<String>, RecognizePiiEntitiesOptions> testRunner) {
        testRunner.accept(PII_ENTITY_INPUTS,
            new RecognizePiiEntitiesOptions().setCategoriesFilter(
                PiiEntityCategory.US_SOCIAL_SECURITY_NUMBER, PiiEntityCategory.ABA_ROUTING_NUMBER));
    }

    // Linked Entity runner
    void recognizeLinkedEntitiesForSingleTextInputRunner(Consumer<String> testRunner) {
        testRunner.accept(LINKED_ENTITY_INPUTS.get(0));
    }

    void recognizeBatchStringLinkedEntitiesShowStatsRunner(
        BiConsumer<List<String>, TextAnalyticsRequestOptions> testRunner) {
        testRunner.accept(LINKED_ENTITY_INPUTS, new TextAnalyticsRequestOptions().setIncludeStatistics(true));
    }

    void recognizeBatchLinkedEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        TextAnalyticsRequestOptions options = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        testRunner.accept(TestUtils.getTextDocumentInputs(LINKED_ENTITY_INPUTS), options);
    }

    void recognizeLinkedLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(LINKED_ENTITY_INPUTS, "en");
    }

    void recognizeLinkedStringInputRunner(Consumer<List<String>> testRunner) {
        testRunner.accept(LINKED_ENTITY_INPUTS);
    }

    void recognizeBatchLinkedEntityRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(TestUtils.getTextDocumentInputs(LINKED_ENTITY_INPUTS));
    }

    void recognizeBatchLinkedEntityDuplicateIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(getDuplicateTextDocumentInputs());
    }

    // Key Phrases runner
    void extractKeyPhrasesForSingleTextInputRunner(Consumer<String> testRunner) {
        testRunner.accept(KEY_PHRASE_INPUTS.get(1));
    }

    void extractBatchStringKeyPhrasesShowStatsRunner(BiConsumer<List<String>, TextAnalyticsRequestOptions> testRunner) {
        testRunner.accept(KEY_PHRASE_INPUTS, new TextAnalyticsRequestOptions().setIncludeStatistics(true));
    }

    void extractBatchKeyPhrasesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = TestUtils.getTextDocumentInputs(KEY_PHRASE_INPUTS);
        TextAnalyticsRequestOptions options = new TextAnalyticsRequestOptions().setIncludeStatistics(true);
        testRunner.accept(textDocumentInputs, options);
    }

    void extractKeyPhrasesLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(KEY_PHRASE_INPUTS, "en");
    }

    void extractKeyPhrasesStringInputRunner(Consumer<List<String>> testRunner) {
        testRunner.accept(KEY_PHRASE_INPUTS);
    }

    void extractBatchKeyPhrasesRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(TestUtils.getTextDocumentInputs(KEY_PHRASE_INPUTS));
    }

    void extractBatchKeyPhrasesDuplicateIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(getDuplicateTextDocumentInputs());
    }

    void extractKeyPhrasesWarningRunner(Consumer<String> testRunner) {
        testRunner.accept(TOO_LONG_INPUT);
    }

    void extractKeyPhrasesBatchWarningRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(getWarningsTextDocumentInputs());
    }

    // Sentiment Runner
    void analyzeSentimentForSingleTextInputRunner(Consumer<String> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS.get(0));
    }

    void analyzeSentimentForTextInputWithOpinionMiningRunner(BiConsumer<String, AnalyzeSentimentOptions> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS.get(0), new AnalyzeSentimentOptions().setIncludeOpinionMining(true));
    }

    void analyzeSentimentLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS, "en");
    }

    void analyzeSentimentStringInputRunner(Consumer<List<String>> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS);
    }

    void analyzeBatchSentimentRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(TestUtils.getTextDocumentInputs(SENTIMENT_INPUTS));
    }

    void analyzeBatchSentimentDuplicateIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(getDuplicateTextDocumentInputs());
    }

    void analyzeBatchStringSentimentShowStatsAndIncludeOpinionMiningRunner(BiConsumer<List<String>, AnalyzeSentimentOptions> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS,
            new AnalyzeSentimentOptions().setIncludeStatistics(true).setIncludeOpinionMining(true));
    }

    void analyzeBatchSentimentShowStatsRunner(BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = TestUtils.getTextDocumentInputs(SENTIMENT_INPUTS);
        testRunner.accept(textDocumentInputs, new TextAnalyticsRequestOptions().setIncludeStatistics(true));
    }

    void analyzeBatchSentimentOpinionMining(BiConsumer<List<TextDocumentInput>, AnalyzeSentimentOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = TestUtils.getTextDocumentInputs(SENTIMENT_INPUTS);
        testRunner.accept(textDocumentInputs, new AnalyzeSentimentOptions().setIncludeOpinionMining(true)
            .setIncludeStatistics(true));
    }

    // other Runners
    void emptyTextRunner(Consumer<String> testRunner) {
        testRunner.accept("");
    }

    void emptyListRunner(BiConsumer<List<TextDocumentInput>, String> testRunner) {
        testRunner.accept(new ArrayList<>(), "'documents' cannot be empty.");
    }

    void faultyTextRunner(Consumer<String> testRunner) {
        testRunner.accept("!@#%%");
    }

    void detectLanguageInputEmptyIdRunner(Consumer<List<DetectLanguageInput>> testRunner) {
        testRunner.accept(asList(new DetectLanguageInput("", DETECT_LANGUAGE_INPUTS.get(0))));
    }

    void textAnalyticsInputEmptyIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(asList(new TextDocumentInput("", CATEGORIZED_ENTITY_INPUTS.get(0))));
    }

    void tooManyDocumentsRunner(Consumer<List<String>> testRunner) {
        final String documentInput = CATEGORIZED_ENTITY_INPUTS.get(0);
        // max num of document size is 10
        testRunner.accept(asList(
            documentInput, documentInput, documentInput, documentInput, documentInput, documentInput,
            documentInput, documentInput, documentInput, documentInput, documentInput, documentInput));
    }

    // offset runners
    void emojiRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("👩 " + text); // count as 3 units
    }

    void batchEmojiRunner(Consumer<List<TextDocumentInput>> testRunner, String text) {
        testRunner.accept(Collections.singletonList(new TextDocumentInput("0", "👩 " + text))); // count as 3 units
    }

    void emojiWithSkinToneModifierRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("👩🏻 " + text); // count as 5 units
    }

    void emojiFamilyRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("👩‍👩‍👧‍👧 " + text); // count as 12 units
    }

    void emojiFamilyWithSkinToneModifierRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("👩🏻‍👩🏽‍👧🏾‍👦🏿 " + text); // count as 20 units
    }

    void diacriticsNfcRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("año " + text); // count as 4 units
    }

    void diacriticsNfdRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("año " + text); // count as 5 units
    }

    void koreanNfcRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("아가 " + text); // count as 3 units
    }

    void koreanNfdRunner(Consumer<String> testRunner, String text) {
        testRunner.accept("아가 " + text); // count as 3 units
    }

    void zalgoTextRunner(Consumer<String> testRunner, String text) {
        // count as 116 units
        testRunner.accept("ơ̵̧̧̢̳̘̘͕͔͕̭̟̙͎͈̞͔̈̇̒̃͋̇̅͛̋͛̎́͑̄̐̂̎͗͝m̵͍͉̗̄̏͌̂̑̽̕͝͠g̵̢̡̢̡̨̡̧̛͉̞̯̠̤̣͕̟̫̫̼̰͓̦͖̣̣͎̋͒̈́̓̒̈̍̌̓̅͑̒̓̅̅͒̿̏́͗̀̇͛̏̀̈́̀̊̾̀̔͜͠͝ͅ " + text);
    }

    // Healthcare LRO runner
    void healthcareLroRunner(BiConsumer<List<TextDocumentInput>, AnalyzeHealthcareEntitiesOptions> testRunner) {
        testRunner.accept(
            asList(
                new TextDocumentInput("0", HEALTHCARE_INPUTS.get(0)),
                new TextDocumentInput("1", HEALTHCARE_INPUTS.get(1))),
            new AnalyzeHealthcareEntitiesOptions().setIncludeStatistics(true));
    }

    void healthcareLroPaginationRunner(
        BiConsumer<List<TextDocumentInput>, AnalyzeHealthcareEntitiesOptions> testRunner, int totalDocuments) {
        List<TextDocumentInput> documents = new ArrayList<>();
        // Service has 10 as the default size per page. So there will be 2 remaining page in the next page link
        for (int i = 0; i < totalDocuments; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i), HEALTHCARE_INPUTS.get(0)));
        }
        testRunner.accept(documents, new AnalyzeHealthcareEntitiesOptions().setIncludeStatistics(true));
    }

    void analyzeHealthcareEntitiesForAssertionRunner(
        BiConsumer<List<String>, AnalyzeHealthcareEntitiesOptions> testRunner) {
        testRunner.accept(asList(
            "All female participants that are premenopausal will be required to have a pregnancy test; "
                + "any participant who is pregnant or breastfeeding will not be included"),
            new AnalyzeHealthcareEntitiesOptions().setIncludeStatistics(false));
    }

    // Healthcare LRO runner- Cancellation
    void cancelHealthcareLroRunner(BiConsumer<List<TextDocumentInput>, AnalyzeHealthcareEntitiesOptions> testRunner) {
        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i), HEALTHCARE_INPUTS.get(0)));
        }
        testRunner.accept(documents, new AnalyzeHealthcareEntitiesOptions());
    }

    // Analyze batch actions
    void analyzeBatchActionsRunner(BiConsumer<List<TextDocumentInput>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(
                new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
                new TextDocumentInput("1", PII_ENTITY_INPUTS.get(0))),
            new TextAnalyticsActions()
                .setDisplayName("Test1")
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction())
                .setRecognizePiiEntitiesActions(new RecognizePiiEntitiesAction())
                .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction())
                .setRecognizeLinkedEntitiesActions(new RecognizeLinkedEntitiesAction())
                .setAnalyzeSentimentActions(new AnalyzeSentimentAction())
        );
    }

    void analyzeBatchActionsPaginationRunner(BiConsumer<List<TextDocumentInput>, TextAnalyticsActions> testRunner,
        int documentsInTotal) {
        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < documentsInTotal; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i), PII_ENTITY_INPUTS.get(0)));
        }
        testRunner.accept(documents,
            new TextAnalyticsActions().setDisplayName("Test1")
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction())
                .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction())
                .setRecognizePiiEntitiesActions(new RecognizePiiEntitiesAction())
                .setRecognizeLinkedEntitiesActions(new RecognizeLinkedEntitiesAction())
                .setAnalyzeSentimentActions(new AnalyzeSentimentAction()));
    }

    void analyzeEntitiesRecognitionRunner(BiConsumer<List<TextDocumentInput>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(
                new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
                new TextDocumentInput("1", PII_ENTITY_INPUTS.get(0))),
            new TextAnalyticsActions()
                .setDisplayName("Test1")
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction()));
    }

    void analyzePiiEntityRecognitionWithCategoriesFiltersRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(
                new TextDocumentInput("0", PII_ENTITY_INPUTS.get(0)),
                new TextDocumentInput("1", PII_ENTITY_INPUTS.get(1))),
            new TextAnalyticsActions()
                .setDisplayName("Test1")
                .setRecognizePiiEntitiesActions(
                    new RecognizePiiEntitiesAction()
                        .setCategoriesFilter(PiiEntityCategory.US_SOCIAL_SECURITY_NUMBER,
                            PiiEntityCategory.ABA_ROUTING_NUMBER)
                ));
    }

    void analyzePiiEntityRecognitionWithDomainFiltersRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(
                new TextDocumentInput("0", PII_ENTITY_INPUTS.get(0)),
                new TextDocumentInput("1", PII_ENTITY_INPUTS.get(1))),
            new TextAnalyticsActions()
                .setDisplayName("Test1")
                .setRecognizePiiEntitiesActions(
                    new RecognizePiiEntitiesAction()
                        .setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION)
                ));
    }

    void analyzeLinkedEntityRecognitionRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            LINKED_ENTITY_INPUTS,
            new TextAnalyticsActions()
                .setDisplayName("Test1")
                .setRecognizeLinkedEntitiesActions(
                    new RecognizeLinkedEntitiesAction()));
    }

    void extractKeyPhrasesRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(CATEGORIZED_ENTITY_INPUTS.get(0), PII_ENTITY_INPUTS.get(0)),
            new TextAnalyticsActions()
                .setDisplayName("Test1")
                .setExtractKeyPhrasesActions(
                    new ExtractKeyPhrasesAction()));
    }

    void analyzeSentimentRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            SENTIMENT_INPUTS,
            new TextAnalyticsActions()
                .setAnalyzeSentimentActions(new AnalyzeSentimentAction()));
    }

    void analyzeExtractSummaryRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner,
        Integer maxSentenceCount, SummarySentencesOrder summarySentencesOrder) {
        testRunner.accept(SUMMARY_INPUTS,
            new TextAnalyticsActions()
                .setExtractSummaryActions(
                    new ExtractSummaryAction()
                        .setMaxSentenceCount(maxSentenceCount)
                        .setSentencesOrderBy(summarySentencesOrder)));
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }

    TextAnalyticsClientBuilder getTextAnalyticsAsyncClientBuilder(HttpClient httpClient,
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
            builder.credential((new AzureKeyCredential(
                Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY"))));
        }
        return builder;
    }

    static void validateDetectLanguageResultCollectionWithResponse(boolean showStatistics,
        DetectLanguageResultCollection expected, int expectedStatusCode,
        Response<DetectLanguageResultCollection> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateDetectLanguageResultCollection(showStatistics, expected, response.getValue());
    }

    static void validateDetectLanguageResultCollection(boolean showStatistics,
        DetectLanguageResultCollection expected, DetectLanguageResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validatePrimaryLanguage(expectedItem.getPrimaryLanguage(), actualItem.getPrimaryLanguage()));
    }

    static void validateCategorizedEntitiesResultCollectionWithResponse(boolean showStatistics,
        RecognizeEntitiesResultCollection expected, int expectedStatusCode,
        Response<RecognizeEntitiesResultCollection> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateCategorizedEntitiesResultCollection(showStatistics, expected, response.getValue());
    }

    static void validateCategorizedEntitiesResultCollection(boolean showStatistics,
        RecognizeEntitiesResultCollection expected, RecognizeEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateCategorizedEntities(
                expectedItem.getEntities().stream().collect(Collectors.toList()),
                actualItem.getEntities().stream().collect(Collectors.toList())));
    }

    static void validatePiiEntitiesResultCollectionWithResponse(boolean showStatistics,
        RecognizePiiEntitiesResultCollection expected, int expectedStatusCode,
        Response<RecognizePiiEntitiesResultCollection> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validatePiiEntitiesResultCollection(showStatistics, expected, response.getValue());
    }

    static void validatePiiEntitiesResultCollection(boolean showStatistics,
        RecognizePiiEntitiesResultCollection expected, RecognizePiiEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) -> {
            final PiiEntityCollection expectedPiiEntityCollection = expectedItem.getEntities();
            final PiiEntityCollection actualPiiEntityCollection = actualItem.getEntities();
            assertEquals(expectedPiiEntityCollection.getRedactedText(), actualPiiEntityCollection.getRedactedText());
            validatePiiEntities(
                expectedPiiEntityCollection.stream().collect(Collectors.toList()),
                actualPiiEntityCollection.stream().collect(Collectors.toList()));
        });
    }

    static void validateLinkedEntitiesResultCollectionWithResponse(boolean showStatistics,
        RecognizeLinkedEntitiesResultCollection expected, int expectedStatusCode,
        Response<RecognizeLinkedEntitiesResultCollection> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateLinkedEntitiesResultCollection(showStatistics, expected, response.getValue());
    }

    static void validateLinkedEntitiesResultCollection(boolean showStatistics,
        RecognizeLinkedEntitiesResultCollection expected, RecognizeLinkedEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateLinkedEntities(
                expectedItem.getEntities().stream().collect(Collectors.toList()),
                actualItem.getEntities().stream().collect(Collectors.toList())));
    }

    static void validateExtractKeyPhrasesResultCollectionWithResponse(boolean showStatistics,
        ExtractKeyPhrasesResultCollection expected, int expectedStatusCode,
        Response<ExtractKeyPhrasesResultCollection> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateExtractKeyPhrasesResultCollection(showStatistics, expected, response.getValue());
    }

    static void validateExtractKeyPhrasesResultCollection(boolean showStatistics,
        ExtractKeyPhrasesResultCollection expected, ExtractKeyPhrasesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateKeyPhrases(
                expectedItem.getKeyPhrases().stream().collect(Collectors.toList()),
                actualItem.getKeyPhrases().stream().collect(Collectors.toList())));
    }

    static void validateAnalyzeSentimentResultCollectionWithResponse(boolean showStatistics,
        boolean includeOpinionMining, AnalyzeSentimentResultCollection expected,
        int expectedStatusCode, Response<AnalyzeSentimentResultCollection> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateAnalyzeSentimentResultCollection(showStatistics, includeOpinionMining, expected, response.getValue());
    }

    static void validateAnalyzeSentimentResultCollection(boolean showStatistics, boolean includeOpinionMining,
        AnalyzeSentimentResultCollection expected, AnalyzeSentimentResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateDocumentSentiment(includeOpinionMining, expectedItem.getDocumentSentiment(),
                actualItem.getDocumentSentiment()));
    }

    static void validateExtractSummaryResultCollection(boolean showStatistics,
        ExtractSummaryResultCollection expected, ExtractSummaryResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual,
            (expectedItem, actualItem) -> validateDocumentExtractSummaryResult(expectedItem, actualItem));
    }

    static void validateHealthcareEntitiesResult(boolean showStatistics,
        AnalyzeHealthcareEntitiesResultCollection expected, AnalyzeHealthcareEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual,
            (expectedItem, actualItem) -> validateHealthcareEntityDocumentResult(expectedItem, actualItem));
    }

    /**
     * Helper method to validate a single detected language.
     *
     * @param expectedLanguage detectedLanguage returned by the service.
     * @param actualLanguage detectedLanguage returned by the API.
     */
    static void validatePrimaryLanguage(DetectedLanguage expectedLanguage, DetectedLanguage actualLanguage) {
        // TODO: issue https://github.com/Azure/azure-sdk-for-java/issues/13841
        assertNotNull(actualLanguage.getIso6391Name());
        assertNotNull(actualLanguage.getName());
        assertNotNull(actualLanguage.getConfidenceScore());
    }

    /**
     * Helper method to validate a single categorized entity.
     *
     * @param expectedCategorizedEntity CategorizedEntity returned by the service.
     * @param actualCategorizedEntity CategorizedEntity returned by the API.
     */
    static void validateCategorizedEntity(
        CategorizedEntity expectedCategorizedEntity, CategorizedEntity actualCategorizedEntity) {
        assertEquals(expectedCategorizedEntity.getSubcategory(), actualCategorizedEntity.getSubcategory());
        assertEquals(expectedCategorizedEntity.getText(), actualCategorizedEntity.getText());
        assertEquals(expectedCategorizedEntity.getOffset(), actualCategorizedEntity.getOffset());
        assertEquals(expectedCategorizedEntity.getCategory(), actualCategorizedEntity.getCategory());
        assertNotNull(actualCategorizedEntity.getConfidenceScore());
    }

    /**
     * Helper method to validate a single Personally Identifiable Information entity.
     *
     * @param expectedPiiEntity PiiEntity returned by the service.
     * @param actualPiiEntity PiiEntity returned by the API.
     */
    static void validatePiiEntity(PiiEntity expectedPiiEntity, PiiEntity actualPiiEntity) {
        assertEquals(expectedPiiEntity.getOffset(), actualPiiEntity.getOffset());
        assertEquals(expectedPiiEntity.getSubcategory(), actualPiiEntity.getSubcategory());
        assertEquals(expectedPiiEntity.getText(), actualPiiEntity.getText());
        assertEquals(expectedPiiEntity.getCategory(), actualPiiEntity.getCategory());
        assertNotNull(actualPiiEntity.getConfidenceScore());
    }

    /**
     * Helper method to validate a single linked entity.
     *
     * @param expectedLinkedEntity LinkedEntity returned by the service.
     * @param actualLinkedEntity LinkedEntity returned by the API.
     */
    static void validateLinkedEntity(LinkedEntity expectedLinkedEntity, LinkedEntity actualLinkedEntity) {
        assertEquals(expectedLinkedEntity.getName(), actualLinkedEntity.getName());
        assertEquals(expectedLinkedEntity.getDataSource(), actualLinkedEntity.getDataSource());
        assertEquals(expectedLinkedEntity.getLanguage(), actualLinkedEntity.getLanguage());
        if (interceptorManagerTestBase.isPlaybackMode()) {
            assertEquals(REDACTED, actualLinkedEntity.getUrl());
        } else {
            assertEquals(expectedLinkedEntity.getUrl(), actualLinkedEntity.getUrl());
        }
        assertEquals(expectedLinkedEntity.getDataSourceEntityId(), actualLinkedEntity.getDataSourceEntityId());
        // TODO: Bing ID is missing. https://github.com/Azure/azure-sdk-for-java/issues/22208
        // assertEquals(expectedLinkedEntity.getBingEntitySearchApiId(), actualLinkedEntity.getBingEntitySearchApiId());
        validateLinkedEntityMatches(expectedLinkedEntity.getMatches().stream().collect(Collectors.toList()),
            actualLinkedEntity.getMatches().stream().collect(Collectors.toList()));
    }

    /**
     * Helper method to validate a single key phrase.
     *
     * @param expectedKeyPhrases key phrases returned by the service.
     * @param actualKeyPhrases key phrases returned by the API.
     */
    static void validateKeyPhrases(List<String> expectedKeyPhrases, List<String> actualKeyPhrases) {
        assertEquals(expectedKeyPhrases.size(), actualKeyPhrases.size());
        Collections.sort(expectedKeyPhrases);
        Collections.sort(actualKeyPhrases);
        for (int i = 0; i < expectedKeyPhrases.size(); i++) {
            assertEquals(expectedKeyPhrases.get(i), actualKeyPhrases.get(i));
        }
    }

    /**
     * Helper method to validate the list of categorized entities.
     *
     *  @param expectedCategorizedEntityList categorizedEntities returned by the service.
     * @param actualCategorizedEntityList categorizedEntities returned by the API.
     */
    static void validateCategorizedEntities(List<CategorizedEntity> expectedCategorizedEntityList,
        List<CategorizedEntity> actualCategorizedEntityList) {
        assertEquals(expectedCategorizedEntityList.size(), actualCategorizedEntityList.size());
        expectedCategorizedEntityList.sort(Comparator.comparing(CategorizedEntity::getText));
        actualCategorizedEntityList.sort(Comparator.comparing(CategorizedEntity::getText));

        for (int i = 0; i < expectedCategorizedEntityList.size(); i++) {
            CategorizedEntity expectedCategorizedEntity = expectedCategorizedEntityList.get(i);
            CategorizedEntity actualCategorizedEntity = actualCategorizedEntityList.get(i);
            validateCategorizedEntity(expectedCategorizedEntity, actualCategorizedEntity);
        }
    }

    /**
     * Helper method to validate the list of Personally Identifiable Information entities.
     *
     * @param expectedPiiEntityList piiEntities returned by the service.
     * @param actualPiiEntityList piiEntities returned by the API.
     */
    static void validatePiiEntities(List<PiiEntity> expectedPiiEntityList, List<PiiEntity> actualPiiEntityList) {
        assertEquals(expectedPiiEntityList.size(), actualPiiEntityList.size());
        expectedPiiEntityList.sort(Comparator.comparing(PiiEntity::getText));
        actualPiiEntityList.sort(Comparator.comparing(PiiEntity::getText));

        for (int i = 0; i < expectedPiiEntityList.size(); i++) {
            PiiEntity expectedPiiEntity = expectedPiiEntityList.get(i);
            PiiEntity actualPiiEntity = actualPiiEntityList.get(i);
            validatePiiEntity(expectedPiiEntity, actualPiiEntity);
        }
    }

    /**
     * Helper method to validate the list of linked entities.
     *
     * @param expectedLinkedEntityList linkedEntities returned by the service.
     * @param actualLinkedEntityList linkedEntities returned by the API.
     */
    static void validateLinkedEntities(List<LinkedEntity> expectedLinkedEntityList,
        List<LinkedEntity> actualLinkedEntityList) {
        assertEquals(expectedLinkedEntityList.size(), actualLinkedEntityList.size());
        expectedLinkedEntityList.sort(Comparator.comparing(LinkedEntity::getName));
        actualLinkedEntityList.sort(Comparator.comparing(LinkedEntity::getName));

        for (int i = 0; i < expectedLinkedEntityList.size(); i++) {
            LinkedEntity expectedLinkedEntity = expectedLinkedEntityList.get(i);
            LinkedEntity actualLinkedEntity = actualLinkedEntityList.get(i);
            validateLinkedEntity(expectedLinkedEntity, actualLinkedEntity);
        }
    }

    /**
     * Helper method to validate the list of sentence sentiment. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentimentList a list of analyzed sentence sentiment returned by the service.
     * @param actualSentimentList a list of analyzed sentence sentiment returned by the API.
     */
    static void validateSentenceSentimentList(boolean includeOpinionMining, List<SentenceSentiment> expectedSentimentList,
        List<SentenceSentiment> actualSentimentList) {

        assertEquals(expectedSentimentList.size(), actualSentimentList.size());
        for (int i = 0; i < expectedSentimentList.size(); i++) {
            validateSentenceSentiment(includeOpinionMining, expectedSentimentList.get(i), actualSentimentList.get(i));
        }
    }

    static void validateSummarySentenceList(List<SummarySentence> expect, List<SummarySentence> actual) {
        assertEquals(expect.size(), actual.size());
        for (int i = 0; i < expect.size(); i++) {
            validateSummarySentence(expect.get(i), actual.get(i));
        }
    }

    /**
     * Helper method to validate one pair of analyzed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentiment analyzed sentence sentiment returned by the service.
     * @param actualSentiment analyzed sentence sentiment returned by the API.
     */
    static void validateSentenceSentiment(boolean includeOpinionMining, SentenceSentiment expectedSentiment, SentenceSentiment actualSentiment) {
        assertEquals(expectedSentiment.getSentiment(), actualSentiment.getSentiment());
        assertEquals(expectedSentiment.getText(), actualSentiment.getText());
        assertEquals(expectedSentiment.getOffset(), actualSentiment.getOffset());
        assertEquals(expectedSentiment.getLength(), actualSentiment.getLength());

        if (includeOpinionMining) {
            validateSentenceOpinions(expectedSentiment.getOpinions().stream().collect(Collectors.toList()),
                actualSentiment.getOpinions().stream().collect(Collectors.toList()));
        } else {
            assertNull(actualSentiment.getOpinions());
        }
    }

    static void validateSummarySentence(SummarySentence expect, SummarySentence actual) {
        assertEquals(expect.getText(), actual.getText());
        assertEquals(expect.getOffset(), actual.getOffset());
        assertEquals(expect.getLength(), actual.getLength());
        assertNotNull(actual.getRankScore());
    }

    /**
     * Helper method to validate sentence's opinions.
     *
     * @param expectedSentenceOpinions a list of sentence opinions returned by the service.
     * @param actualSentenceOpinions a list of sentence opinions returned by the API.
     */
    static void validateSentenceOpinions(List<SentenceOpinion> expectedSentenceOpinions,
        List<SentenceOpinion> actualSentenceOpinions) {
        assertEquals(expectedSentenceOpinions.size(), actualSentenceOpinions.size());
        for (int i = 0; i < actualSentenceOpinions.size(); i++) {
            final SentenceOpinion expectedSentenceOpinion = expectedSentenceOpinions.get(i);
            final SentenceOpinion actualSentenceOpinion = actualSentenceOpinions.get(i);
            validateTargetSentiment(expectedSentenceOpinion.getTarget(), actualSentenceOpinion.getTarget());
            validateAssessmentList(expectedSentenceOpinion.getAssessments().stream().collect(Collectors.toList()),
                actualSentenceOpinion.getAssessments().stream().collect(Collectors.toList()));
        }
    }

    /**
     * Helper method to validate target sentiment.
     *
     * @param expected An expected target sentiment.
     * @param actual An actual target sentiment.
     */
    static void validateTargetSentiment(TargetSentiment expected, TargetSentiment actual) {
        assertEquals(expected.getSentiment(), actual.getSentiment());
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getOffset(), actual.getOffset());
    }

    /**
     * Helper method to validate a list of {@link AssessmentSentiment}.
     *
     * @param expected A list of expected assessment sentiments.
     * @param actual A list of actual assessment sentiments.
     */
    static void validateAssessmentList(List<AssessmentSentiment> expected, List<AssessmentSentiment> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            validateAssessmentSentiment(expected.get(i), actual.get(i));
        }
    }

    /**
     * Helper method to validate assessment sentiment.
     *
     * @param expect An expected assessment sentiment.
     * @param actual An actual assessment sentiment.
     */
    static void validateAssessmentSentiment(AssessmentSentiment expect, AssessmentSentiment actual) {
        assertEquals(expect.getSentiment(), actual.getSentiment());
        assertEquals(expect.getText(), actual.getText());
        assertEquals(expect.isNegated(), actual.isNegated());
        assertEquals(expect.getOffset(), actual.getOffset());
    }

    /**
     * Helper method to validate one pair of analyzed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentiment analyzed document sentiment returned by the service.
     * @param actualSentiment analyzed document sentiment returned by the API.
     */
    static void validateDocumentSentiment(boolean includeOpinionMining, DocumentSentiment expectedSentiment,
        DocumentSentiment actualSentiment) {
        assertEquals(expectedSentiment.getSentiment(), actualSentiment.getSentiment());
        validateSentenceSentimentList(includeOpinionMining,
            expectedSentiment.getSentences().stream().collect(Collectors.toList()),
            actualSentiment.getSentences().stream().collect(Collectors.toList()));
    }

    static void validateDocumentExtractSummaryResult(ExtractSummaryResult expect,
        ExtractSummaryResult actual) {

        validateSummarySentenceList(
            expect.getSentences().stream().collect(Collectors.toList()),
            actual.getSentences().stream().collect(Collectors.toList())
        );

    }

    // Healthcare task
    static void validateHealthcareEntity(HealthcareEntity expected, HealthcareEntity actual) {
        assertEquals(expected.getCategory(), actual.getCategory());
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getOffset(), actual.getOffset());
        assertEquals(expected.getLength(), actual.getLength());
        assertEquals(expected.getNormalizedText(), actual.getNormalizedText());
        assertEquals(expected.getSubcategory(), actual.getSubcategory());
        validateEntityAssertion(expected.getAssertion(), actual.getAssertion());
        validateEntityDataSourceList(expected.getDataSources(), actual.getDataSources());
    }

    static void validateEntityAssertion(HealthcareEntityAssertion expected, HealthcareEntityAssertion actual) {
        if (actual == expected) {
            return;
        }
        assertEquals(expected.getConditionality(), actual.getConditionality());
        assertEquals(expected.getAssociation(), actual.getAssociation());
        assertEquals(expected.getCertainty(), actual.getCertainty());
    }

    static void validateEntityDataSourceList(IterableStream<EntityDataSource> expected,
        IterableStream<EntityDataSource> actual) {
        if (expected == actual) {
            return;
        } else if (expected == null || actual == null) {
            assertTrue(false);
        }
    }

    static void validateHealthcareEntityDocumentResult(AnalyzeHealthcareEntitiesResult expected,
        AnalyzeHealthcareEntitiesResult actual) {
        validateHealthcareEntityRelations(expected.getEntityRelations().stream().collect(Collectors.toList()),
            actual.getEntityRelations().stream().collect(Collectors.toList()));
        validateHealthcareEntities(expected.getEntities().stream().collect(Collectors.toList()),
            actual.getEntities().stream().collect(Collectors.toList()));
    }

    static void validateHealthcareEntityRelations(List<HealthcareEntityRelation> expected,
        List<HealthcareEntityRelation> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            validateHealthcareEntityRelation(expected.get(i), actual.get(i));
        }
    }

    static void validateHealthcareEntityRelation(HealthcareEntityRelation expected, HealthcareEntityRelation actual) {
        final List<HealthcareEntityRelationRole> expectedRoles = expected.getRoles().stream().collect(Collectors.toList());
        final List<HealthcareEntityRelationRole> actualRoles = actual.getRoles().stream().collect(Collectors.toList());
        assertEquals(expected.getRelationType(), actual.getRelationType());
        for (int i = 0; i < expectedRoles.size(); i++) {
            validateHealthcareEntityRelationRole(expectedRoles.get(i), actualRoles.get(i));
        }
    }

    static void validateHealthcareEntityRelationRole(HealthcareEntityRelationRole expected,
        HealthcareEntityRelationRole actual) {
        assertEquals(expected.getName(), actual.getName());
        validateHealthcareEntity(expected.getEntity(), actual.getEntity());
    }

    static void validateHealthcareEntities(List<HealthcareEntity> expected, List<HealthcareEntity> actual) {
        assertEquals(expected.size(), actual.size());
        expected.sort(Comparator.comparing(HealthcareEntity::getText));
        actual.sort(Comparator.comparing(HealthcareEntity::getText));
        for (int i = 0; i < expected.size(); i++) {
            validateHealthcareEntity(expected.get(i), actual.get(i));
        }
    }

    static void validateAnalyzeHealthcareEntitiesResultCollectionList(boolean showStatistics,
        List<AnalyzeHealthcareEntitiesResultCollection> expected,
        List<AnalyzeHealthcareEntitiesResultCollection> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateHealthcareEntitiesResult(showStatistics, expected.get(i), actual.get(i));
        }
    }

    // Analyze tasks
    static void validateAnalyzeBatchActionsResultList(boolean showStatistics,  boolean includeOpinionMining,
        List<AnalyzeActionsResult> expected, List<AnalyzeActionsResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateAnalyzeActionsResult(showStatistics, includeOpinionMining, expected.get(i), actual.get(i));
        }
    }

    static void validateAnalyzeActionsResult(boolean showStatistics, boolean includeOpinionMining,
        AnalyzeActionsResult expected, AnalyzeActionsResult actual) {
        // TODO: batch actions has return non statistics.
        // Issue: https://github.com/Azure/azure-sdk-for-java/issues/19672
//        final TextDocumentBatchStatistics expectedOperationStatistics = expected.getStatistics();
//        final TextDocumentBatchStatistics actualOperationStatistics = actual.getStatistics();
//        if (showStatistics) {
//            assertEquals(expectedOperationStatistics.getDocumentCount(), actualOperationStatistics.getDocumentCount());
//            assertEquals(expectedOperationStatistics.getInvalidDocumentCount(),
//                actualOperationStatistics.getDocumentCount());
//            assertEquals(expectedOperationStatistics.getValidDocumentCount(),
//                actualOperationStatistics.getValidDocumentCount());
//            assertEquals(expectedOperationStatistics.getTransactionCount(),
//                actualOperationStatistics.getTransactionCount());
//        }

        validateRecognizeEntitiesActionResults(showStatistics,
            expected.getRecognizeEntitiesResults().stream().collect(Collectors.toList()),
            actual.getRecognizeEntitiesResults().stream().collect(Collectors.toList()));
        validateRecognizeLinkedEntitiesActionResults(showStatistics,
            expected.getRecognizeLinkedEntitiesResults().stream().collect(Collectors.toList()),
            actual.getRecognizeLinkedEntitiesResults().stream().collect(Collectors.toList()));
        validateRecognizePiiEntitiesActionResults(showStatistics,
            expected.getRecognizePiiEntitiesResults().stream().collect(Collectors.toList()),
            actual.getRecognizePiiEntitiesResults().stream().collect(Collectors.toList()));
        validateExtractKeyPhrasesActionResults(showStatistics,
            expected.getExtractKeyPhrasesResults().stream().collect(Collectors.toList()),
            actual.getExtractKeyPhrasesResults().stream().collect(Collectors.toList()));
        validateAnalyzeSentimentActionResults(showStatistics, includeOpinionMining,
            expected.getAnalyzeSentimentResults().stream().collect(Collectors.toList()),
            actual.getAnalyzeSentimentResults().stream().collect(Collectors.toList()));
        validateExtractSummaryActionResults(showStatistics,
            expected.getExtractSummaryResults().stream().collect(Collectors.toList()),
            actual.getExtractSummaryResults().stream().collect(Collectors.toList()));
    }

    // Action results validation
    static void validateRecognizeEntitiesActionResults(boolean showStatistics,
        List<RecognizeEntitiesActionResult> expected, List<RecognizeEntitiesActionResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateRecognizeEntitiesActionResult(showStatistics, expected.get(i), actual.get(i));
        }
    }

    static void validateRecognizeLinkedEntitiesActionResults(boolean showStatistics,
        List<RecognizeLinkedEntitiesActionResult> expected, List<RecognizeLinkedEntitiesActionResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateRecognizeLinkedEntitiesActionResult(showStatistics, expected.get(i), actual.get(i));
        }
    }

    static void validateRecognizePiiEntitiesActionResults(boolean showStatistics,
        List<RecognizePiiEntitiesActionResult> expected, List<RecognizePiiEntitiesActionResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateRecognizePiiEntitiesActionResult(showStatistics, expected.get(i), actual.get(i));
        }
    }

    static void validateExtractKeyPhrasesActionResults(boolean showStatistics,
        List<ExtractKeyPhrasesActionResult> expected, List<ExtractKeyPhrasesActionResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateExtractKeyPhrasesActionResult(showStatistics, expected.get(i), actual.get(i));
        }
    }

    static void validateAnalyzeSentimentActionResults(boolean showStatistics, boolean includeOpinionMining,
        List<AnalyzeSentimentActionResult> expected, List<AnalyzeSentimentActionResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateAnalyzeSentimentActionResult(showStatistics, includeOpinionMining, expected.get(i), actual.get(i));
        }
    }

    static void validateExtractSummaryActionResults(boolean showStatistics,
        List<ExtractSummaryActionResult> expected, List<ExtractSummaryActionResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            validateExtractSummaryActionResult(showStatistics, expected.get(i), actual.get(i));
        }
    }


    // Action result validation
    static void validateRecognizeEntitiesActionResult(boolean showStatistics,
        RecognizeEntitiesActionResult expected, RecognizeEntitiesActionResult actual) {
        assertEquals(expected.isError(), actual.isError());
        if (actual.isError()) {
            if (expected.getError() == null) {
                assertNull(actual.getError());
            } else {
                assertNotNull(actual.getError());
                validateErrorDocument(expected.getError(), actual.getError());
            }
        } else {
            validateCategorizedEntitiesResultCollection(showStatistics, expected.getDocumentsResults(), actual.getDocumentsResults());
        }
    }

    static void validateRecognizeLinkedEntitiesActionResult(boolean showStatistics,
        RecognizeLinkedEntitiesActionResult expected, RecognizeLinkedEntitiesActionResult actual) {
        assertEquals(expected.isError(), actual.isError());
        if (actual.isError()) {
            if (expected.getError() == null) {
                assertNull(actual.getError());
            } else {
                assertNotNull(actual.getError());
                validateErrorDocument(expected.getError(), actual.getError());
            }
        } else {
            validateLinkedEntitiesResultCollection(showStatistics, expected.getDocumentsResults(), actual.getDocumentsResults());
        }
    }

    static void validateRecognizePiiEntitiesActionResult(boolean showStatistics,
        RecognizePiiEntitiesActionResult expected, RecognizePiiEntitiesActionResult actual) {
        assertEquals(expected.isError(), actual.isError());
        if (actual.isError()) {
            if (expected.getError() == null) {
                assertNull(actual.getError());
            } else {
                assertNotNull(actual.getError());
                validateErrorDocument(expected.getError(), actual.getError());
            }
        } else {
            validatePiiEntitiesResultCollection(showStatistics, expected.getDocumentsResults(), actual.getDocumentsResults());
        }
    }

    static void validateExtractKeyPhrasesActionResult(boolean showStatistics,
        ExtractKeyPhrasesActionResult expected, ExtractKeyPhrasesActionResult actual) {
        assertEquals(expected.isError(), actual.isError());
        if (actual.isError()) {
            if (expected.getError() == null) {
                assertNull(actual.getError());
            } else {
                assertNotNull(actual.getError());
                validateErrorDocument(expected.getError(), actual.getError());
            }
        } else {
            validateExtractKeyPhrasesResultCollection(showStatistics, expected.getDocumentsResults(), actual.getDocumentsResults());
        }
    }

    static void validateAnalyzeSentimentActionResult(boolean showStatistics, boolean includeOpinionMining,
        AnalyzeSentimentActionResult expected, AnalyzeSentimentActionResult actual) {
        assertEquals(expected.isError(), actual.isError());
        if (actual.isError()) {
            if (expected.getError() == null) {
                assertNull(actual.getError());
            } else {
                assertNotNull(actual.getError());
                validateErrorDocument(expected.getError(), actual.getError());
            }
        } else {
            validateAnalyzeSentimentResultCollection(showStatistics, includeOpinionMining,
                expected.getDocumentsResults(), actual.getDocumentsResults());
        }
    }

    static void validateExtractSummaryActionResult(boolean showStatistics,
        ExtractSummaryActionResult expected, ExtractSummaryActionResult actual) {
        assertEquals(expected.isError(), actual.isError());
        if (actual.isError()) {
            if (expected.getError() == null) {
                assertNull(actual.getError());
            } else {
                assertNotNull(actual.getError());
                validateErrorDocument(expected.getError(), actual.getError());
            }
        } else {
            validateExtractSummaryResultCollection(showStatistics,
                expected.getDocumentsResults(), actual.getDocumentsResults());
        }
    }

    /**
     * Helper method to verify {@link TextAnalyticsResult documents} returned in a batch request.
     */
    static <T extends TextAnalyticsResult, H extends IterableStream<T>> void validateTextAnalyticsResult(
        boolean showStatistics, H expectedResults, H actualResults, BiConsumer<T, T> additionalAssertions) {

        final Map<String, T> expected = expectedResults.stream().collect(
            Collectors.toMap(TextAnalyticsResult::getId, r -> r));
        final Map<String, T> actual = actualResults.stream().collect(
            Collectors.toMap(TextAnalyticsResult::getId, r -> r));

        assertEquals(expected.size(), actual.size());

        if (showStatistics) {
            if (expectedResults instanceof AnalyzeSentimentResultCollection) {
                validateBatchStatistics(((AnalyzeSentimentResultCollection) expectedResults).getStatistics(),
                    ((AnalyzeSentimentResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof DetectLanguageResultCollection) {
                validateBatchStatistics(((DetectLanguageResultCollection) expectedResults).getStatistics(),
                    ((DetectLanguageResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof ExtractKeyPhrasesResultCollection) {
                validateBatchStatistics(((ExtractKeyPhrasesResultCollection) expectedResults).getStatistics(),
                    ((ExtractKeyPhrasesResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof RecognizeEntitiesResultCollection) {
                validateBatchStatistics(((RecognizeEntitiesResultCollection) expectedResults).getStatistics(),
                    ((RecognizeEntitiesResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof RecognizeLinkedEntitiesResultCollection) {
                validateBatchStatistics(((RecognizeLinkedEntitiesResultCollection) expectedResults).getStatistics(),
                    ((RecognizeLinkedEntitiesResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof AnalyzeHealthcareEntitiesResultCollection) {
                validateBatchStatistics(((AnalyzeHealthcareEntitiesResultCollection) expectedResults).getStatistics(),
                    ((AnalyzeHealthcareEntitiesResultCollection) actualResults).getStatistics());
            }
        } else {
            if (expectedResults instanceof AnalyzeSentimentResultCollection) {
                assertNull(((AnalyzeSentimentResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof DetectLanguageResultCollection) {
                assertNull(((DetectLanguageResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof ExtractKeyPhrasesResultCollection) {
                assertNull(((ExtractKeyPhrasesResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof RecognizeEntitiesResultCollection) {
                assertNull(((RecognizeEntitiesResultCollection) actualResults).getStatistics());
            } else if (expectedResults instanceof RecognizeLinkedEntitiesResultCollection) {
                assertNull(((RecognizeLinkedEntitiesResultCollection) actualResults).getStatistics());
            }
        }

        expected.forEach((key, expectedValue) -> {
            T actualValue = actual.get(key);
            assertNotNull(actualValue);

            if (showStatistics) {
                validateDocumentStatistics(expectedValue.getStatistics(), actualValue.getStatistics());
            }

            if (expectedValue.getError() == null) {
                assertNull(actualValue.getError());
            } else {
                assertNotNull(actualValue.getError());
                assertEquals(expectedValue.getError().getErrorCode(), actualValue.getError().getErrorCode());

                validateErrorDocument(expectedValue.getError(), actualValue.getError());
            }

            additionalAssertions.accept(expectedValue, actualValue);
        });
    }

    /**
     * Helper method to verify TextBatchStatistics.
     *
     * @param expectedStatistics the expected value for TextBatchStatistics.
     * @param actualStatistics the value returned by API.
     */
    private static void validateBatchStatistics(TextDocumentBatchStatistics expectedStatistics,
         TextDocumentBatchStatistics actualStatistics) {
        assertEquals(expectedStatistics.getDocumentCount(), actualStatistics.getDocumentCount());
        assertEquals(expectedStatistics.getInvalidDocumentCount(), actualStatistics.getInvalidDocumentCount());
        assertEquals(expectedStatistics.getValidDocumentCount(), actualStatistics.getValidDocumentCount());
        assertEquals(expectedStatistics.getTransactionCount(), actualStatistics.getTransactionCount());
    }

    /**
     * Helper method to verify TextDocumentStatistics.
     *
     * @param expected the expected value for TextDocumentStatistics.
     * @param actual the value returned by API.
     */
    private static void validateDocumentStatistics(TextDocumentStatistics expected, TextDocumentStatistics actual) {
        assertEquals(expected.getCharacterCount(), actual.getCharacterCount());
        assertEquals(expected.getTransactionCount(), actual.getTransactionCount());
    }

    /**
     * Helper method to verify LinkedEntityMatches.
     *
     * @param expectedLinkedEntityMatches the expected value for LinkedEntityMatches.
     * @param actualLinkedEntityMatches the value returned by API.
     */
    private static void validateLinkedEntityMatches(List<LinkedEntityMatch> expectedLinkedEntityMatches,
        List<LinkedEntityMatch> actualLinkedEntityMatches) {
        assertEquals(expectedLinkedEntityMatches.size(), actualLinkedEntityMatches.size());
        expectedLinkedEntityMatches.sort(Comparator.comparing(LinkedEntityMatch::getText));
        actualLinkedEntityMatches.sort(Comparator.comparing(LinkedEntityMatch::getText));

        for (int i = 0; i < expectedLinkedEntityMatches.size(); i++) {
            LinkedEntityMatch expectedLinkedEntity = expectedLinkedEntityMatches.get(i);
            LinkedEntityMatch actualLinkedEntity = actualLinkedEntityMatches.get(i);
            assertEquals(expectedLinkedEntity.getText(), actualLinkedEntity.getText());
            assertEquals(expectedLinkedEntity.getOffset(), actualLinkedEntity.getOffset());
            assertNotNull(actualLinkedEntity.getConfidenceScore());
        }
    }

    /**
     * Helper method to verify the error document.
     *
     * @param expectedError the Error returned from the service.
     * @param actualError the Error returned from the API.
     */
    static void validateErrorDocument(TextAnalyticsError expectedError, TextAnalyticsError actualError) {
        assertEquals(expectedError.getErrorCode(), actualError.getErrorCode());
        assertNotNull(actualError.getMessage());
    }

    static boolean isAscendingOrderByOffSet(List<SummarySentence> summarySentences) {
        int currMin = Integer.MIN_VALUE;
        for (SummarySentence summarySentence : summarySentences) {
            if (summarySentence.getOffset() <= currMin) {
                return false;
            } else {
                currMin = summarySentence.getOffset();
            }
        }
        return true;
    }

    static boolean isDescendingOrderByRankScore(List<SummarySentence> summarySentences) {
        double currentMax = Double.MAX_VALUE;
        for (SummarySentence summarySentence : summarySentences) {
            if (summarySentence.getRankScore() > currentMax) {
                return false;
            } else {
                currentMax = summarySentence.getRankScore();
            }
        }
        return true;
    }
}
