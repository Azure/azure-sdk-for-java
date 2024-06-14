// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AbstractiveSummary;
import com.azure.ai.textanalytics.models.AbstractiveSummaryAction;
import com.azure.ai.textanalytics.models.AbstractiveSummaryActionResult;
import com.azure.ai.textanalytics.models.AbstractiveSummaryOptions;
import com.azure.ai.textanalytics.models.AbstractiveSummaryResult;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesAction;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesActionResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentAction;
import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.ClassifyDocumentResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesAction;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.ExtractiveSummaryAction;
import com.azure.ai.textanalytics.models.ExtractiveSummaryActionResult;
import com.azure.ai.textanalytics.models.ExtractiveSummaryOptions;
import com.azure.ai.textanalytics.models.ExtractiveSummaryResult;
import com.azure.ai.textanalytics.models.ExtractiveSummarySentence;
import com.azure.ai.textanalytics.models.ExtractiveSummarySentencesOrder;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.MultiLabelClassifyAction;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiEntityDomain;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SentenceOpinion;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SingleLabelClassifyAction;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.util.AbstractiveSummaryResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.ExtractiveSummaryResultCollection;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestProxyTestBase;
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
import static com.azure.ai.textanalytics.TestUtils.CUSTOM_ACTION_NAME;
import static com.azure.ai.textanalytics.TestUtils.CUSTOM_ENTITIES_INPUT;
import static com.azure.ai.textanalytics.TestUtils.CUSTOM_MULTI_CLASSIFICATION;
import static com.azure.ai.textanalytics.TestUtils.CUSTOM_SINGLE_CLASSIFICATION;
import static com.azure.ai.textanalytics.TestUtils.DETECT_LANGUAGE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.FAKE_API_KEY;
import static com.azure.ai.textanalytics.TestUtils.HEALTHCARE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.LINKED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.PII_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SENTIMENT_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SUMMARY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.getDuplicateTextDocumentInputs;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TextAnalyticsClientTestBase extends TestProxyTestBase {
    static final String BATCH_ERROR_EXCEPTION_MESSAGE = "Error in accessing the property on document id: 2, when %s returned with an error: Document text is empty. ErrorCodeValue: {InvalidDocument}";
    static final String INVALID_DOCUMENT_BATCH_NPE_MESSAGE = "'documents' cannot be null.";
    static final String INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE = "'documents' cannot be empty.";
    static final String INVALID_DOCUMENT_NPE_MESSAGE = "'document' cannot be null.";
    static final String REDACTED = "REDACTED";

    static final String AZURE_TEXT_ANALYTICS_ENDPOINT =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_ENDPOINT =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_ENDPOINT");
    static final String AZURE_TEXT_ANALYTICS_API_KEY =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_API_KEY =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_API_KEY");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_PROJECT_NAME =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_PROJECT_NAME");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_DEPLOYMENT_NAME =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_DEPLOYMENT_NAME");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_PROJECT_NAME =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_PROJECT_NAME");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_DEPLOYMENT_NAME =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_DEPLOYMENT_NAME");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_PROJECT_NAME =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_PROJECT_NAME");
    static final String AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_DEPLOYMENT_NAME =
        Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_DEPLOYMENT_NAME");

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
    abstract void healthcareStringInputWithoutOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void healthcareStringInputWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void healthcareMaxOverload(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

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
    abstract void analyzeHealthcareEntitiesForAssertion(HttpClient httpClient,
                                                                                                                TextAnalyticsServiceVersion serviceVersion);

    // Healthcare LRO - Cancellation

    @Test
    abstract void cancelHealthcareLro(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Analyze multiple actions
    @Test
    abstract void analyzeActionsStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeActionsWithOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeActionsWithMultiSameKindActions(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeActionsWithActionNames(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

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

    @Test
    abstract void analyzeHealthcareAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomEntitiesAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void singleLabelClassificationAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void multiCategoryClassifyAction(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomEntitiesStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomEntities(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void singleLabelClassificationStringInput(HttpClient httpClient,
                                                       TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void singleLabelClassification(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void multiLabelClassificationStringInput(HttpClient httpClient,
                                                      TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void multiLabelClassification(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Abstractive Summarization
    @Test
    abstract void analyzeAbstractiveSummaryActionWithDefaultParameterValues(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    // Extractive Summarization
    @Test
    abstract void analyzeExtractSummaryActionWithDefaultParameterValues(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionSortedByOffset(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionSortedByRankScore(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionWithSentenceCountLessThanMaxCount(
        HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionWithNonDefaultSentenceCount(
        HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeExtractSummaryActionMaxSentenceCountInvalidRangeException(
        HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void beginAbstractSummaryStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void beginAbstractSummaryMaxOverload(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

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
        testRunner.accept(DETECT_LANGUAGE_INPUTS, options);
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

    void duplicateIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(getDuplicateTextDocumentInputs());
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

    void detectLanguageInputEmptyIdRunner(Consumer<List<DetectLanguageInput>> testRunner) {
        testRunner.accept(asList(new DetectLanguageInput("", DETECT_LANGUAGE_INPUTS.get(0))));
    }

    void emptyDocumentIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
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
    void healthcareStringInputRunner(BiConsumer<List<String>, AnalyzeHealthcareEntitiesOptions> testRunner) {
        testRunner.accept(HEALTHCARE_INPUTS, new AnalyzeHealthcareEntitiesOptions().setIncludeStatistics(true));
    }

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
    void analyzeActionsStringInputRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(
                CATEGORIZED_ENTITY_INPUTS.get(0),
                PII_ENTITY_INPUTS.get(0)),
            new TextAnalyticsActions()
                .setDisplayName("Test1")
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction())
                .setRecognizePiiEntitiesActions(new RecognizePiiEntitiesAction())
                .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction())
                .setAnalyzeSentimentActions(new AnalyzeSentimentAction()));
    }

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
                .setAnalyzeSentimentActions(new AnalyzeSentimentAction())
        );
    }

    void analyzeActionsWithMultiSameKindActionsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(
                new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
                new TextDocumentInput("1", PII_ENTITY_INPUTS.get(0))),
            new TextAnalyticsActions()
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction().setActionName(CUSTOM_ACTION_NAME),
                    new RecognizeEntitiesAction())
                .setRecognizePiiEntitiesActions(new RecognizePiiEntitiesAction().setActionName(CUSTOM_ACTION_NAME),
                    new RecognizePiiEntitiesAction())
                .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction().setActionName(CUSTOM_ACTION_NAME),
                    new ExtractKeyPhrasesAction())
                .setRecognizeLinkedEntitiesActions(
                    new RecognizeLinkedEntitiesAction().setActionName(CUSTOM_ACTION_NAME),
                    new RecognizeLinkedEntitiesAction())
                .setAnalyzeSentimentActions(new AnalyzeSentimentAction().setActionName(CUSTOM_ACTION_NAME),
                    new AnalyzeSentimentAction())
        );
    }

    void analyzeActionsWithActionNamesRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            asList(new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0))),
            new TextAnalyticsActions()
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction().setActionName(CUSTOM_ACTION_NAME))
                .setRecognizePiiEntitiesActions(new RecognizePiiEntitiesAction().setActionName(CUSTOM_ACTION_NAME))
                .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction().setActionName(CUSTOM_ACTION_NAME))
                .setAnalyzeSentimentActions(new AnalyzeSentimentAction().setActionName(CUSTOM_ACTION_NAME))
            // TODO: https://github.com/Azure/azure-sdk-for-java/issues/24908

//                .setRecognizeCustomEntitiesActions(
//                    new RecognizeCustomEntitiesAction(AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_PROJECT_NAME,
//                        AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_DEPLOYMENT_NAME).setActionName(CUSTOM_ACTION_NAME))
//                .setSingleCategoryClassifyActions(
//                    new SingleCategoryClassifyAction(AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_PROJECT_NAME,
//                        AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_DEPLOYMENT_NAME)
//                        .setActionName(CUSTOM_ACTION_NAME))
//                .setMultiCategoryClassifyActions(
//                    new MultiCategoryClassifyAction(AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_PROJECT_NAME,
//                        AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_DEPLOYMENT_NAME)
//                        .setActionName(CUSTOM_ACTION_NAME))
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

    void analyzeHealthcareEntitiesRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(
            HEALTHCARE_INPUTS,
            new TextAnalyticsActions()
                .setAnalyzeHealthcareEntitiesActions(
                    new AnalyzeHealthcareEntitiesAction()));
    }

    void recognizeCustomEntitiesActionRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(CUSTOM_ENTITIES_INPUT,
            new TextAnalyticsActions()
                .setRecognizeCustomEntitiesActions(
                    new RecognizeCustomEntitiesAction(AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_PROJECT_NAME,
                        AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_DEPLOYMENT_NAME)));
    }

    void classifyCustomSingleCategoryActionRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(CUSTOM_SINGLE_CLASSIFICATION,
            new TextAnalyticsActions()
                .setSingleLabelClassifyActions(
                    new SingleLabelClassifyAction(AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_PROJECT_NAME,
                        AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_DEPLOYMENT_NAME)));
    }

    void classifyCustomMultiCategoryActionRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner) {
        testRunner.accept(CUSTOM_MULTI_CLASSIFICATION,
            new TextAnalyticsActions()
                .setMultiLabelClassifyActions(
                    new MultiLabelClassifyAction(AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_PROJECT_NAME,
                        AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_DEPLOYMENT_NAME)));
    }

    void recognizeCustomEntitiesRunner(BiConsumer<List<String>, List<String>> testRunner) {
        testRunner.accept(CUSTOM_ENTITIES_INPUT,
            asList(AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_PROJECT_NAME,
                AZURE_TEXT_ANALYTICS_CUSTOM_ENTITIES_DEPLOYMENT_NAME));
    }

    void classifyCustomSingleLabelRunner(BiConsumer<List<String>, List<String>> testRunner) {
        testRunner.accept(CUSTOM_SINGLE_CLASSIFICATION,
            asList(AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_PROJECT_NAME,
                AZURE_TEXT_ANALYTICS_CUSTOM_SINGLE_CLASSIFICATION_DEPLOYMENT_NAME));
    }

    void classifyCustomMultiLabelRunner(BiConsumer<List<String>, List<String>> testRunner) {
        testRunner.accept(CUSTOM_MULTI_CLASSIFICATION,
            asList(AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_PROJECT_NAME,
                AZURE_TEXT_ANALYTICS_CUSTOM_MULTI_CLASSIFICATION_DEPLOYMENT_NAME));
    }

    void extractiveSummaryActionRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner,
        Integer maxSentenceCount, ExtractiveSummarySentencesOrder extractiveSummarySentencesOrder) {
        testRunner.accept(SUMMARY_INPUTS,
            new TextAnalyticsActions()
                .setExtractiveSummaryActions(
                    new ExtractiveSummaryAction()
                        .setMaxSentenceCount(maxSentenceCount)
                        .setOrderBy(extractiveSummarySentencesOrder)));
    }

    void extractiveSummaryRunner(BiConsumer<List<String>, ExtractiveSummaryOptions> testRunner,
        Integer maxSentenceCount, ExtractiveSummarySentencesOrder extractiveSummarySentencesOrder) {
        testRunner.accept(SUMMARY_INPUTS,
            new ExtractiveSummaryOptions()
                .setMaxSentenceCount(maxSentenceCount)
                .setOrderBy(extractiveSummarySentencesOrder));
    }

    void extractiveSummaryMaxOverloadRunner(BiConsumer<List<TextDocumentInput>, ExtractiveSummaryOptions> testRunner,
        Integer maxSentenceCount, ExtractiveSummarySentencesOrder extractiveSummarySentencesOrder) {
        testRunner.accept(TestUtils.getTextDocumentInputs(SUMMARY_INPUTS),
            new ExtractiveSummaryOptions()
                .setMaxSentenceCount(maxSentenceCount)
                .setOrderBy(extractiveSummarySentencesOrder));
    }


    void abstractiveSummaryActionRunner(BiConsumer<List<String>, TextAnalyticsActions> testRunner,
                                        Integer sentenceCount) {
        testRunner.accept(SUMMARY_INPUTS,
            new TextAnalyticsActions()
                .setAbstractiveSummaryActions(
                    new AbstractiveSummaryAction().setSentenceCount(sentenceCount)));
    }

    void abstractiveSummaryRunner(BiConsumer<List<String>, AbstractiveSummaryOptions> testRunner,
                                  Integer sentenceCount) {
        testRunner.accept(SUMMARY_INPUTS, new AbstractiveSummaryOptions().setSentenceCount(sentenceCount));
    }

    void abstractiveSummaryMaxOverloadRunner(BiConsumer<List<TextDocumentInput>, AbstractiveSummaryOptions> testRunner,
                                             Integer sentenceCount) {
        testRunner.accept(TestUtils.getTextDocumentInputs(SUMMARY_INPUTS),
            new AbstractiveSummaryOptions().setSentenceCount(sentenceCount));
    }

    String getEndpoint(boolean isStaticResource) {
        return interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : isStaticResource ? AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_ENDPOINT : AZURE_TEXT_ANALYTICS_ENDPOINT;
    }

    String getApiKey(boolean isStaticSource) {
        return interceptorManager.isPlaybackMode() ? FAKE_API_KEY
            : isStaticSource ? AZURE_TEXT_ANALYTICS_CUSTOM_TEXT_API_KEY : AZURE_TEXT_ANALYTICS_API_KEY;
    }

    TextAnalyticsClientBuilder getTextAnalyticsClientBuilder(HttpClient httpClient,
        TextAnalyticsServiceVersion serviceVersion, boolean isStaticResource) {
        TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint(isStaticResource))
            .credential(new AzureKeyCredential(getApiKey(isStaticResource)))
            .httpClient(httpClient)
            .serviceVersion(serviceVersion);
        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (!interceptorManager.isLiveMode()) {
            // Remove `operation-location, `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493", "AZSDK2030");
        }
        return builder;
    }

    static void validateDetectLanguageResultCollectionWithResponse(boolean showStatistics,
        Response<DetectLanguageResultCollection> response) {
        assertNotNull(response);
        validateDetectLanguageResultCollection(showStatistics, response.getValue());
    }

    static void validateDetectLanguageResultCollection(boolean showStatistics, DetectLanguageResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual, (actualItem) ->
            validatePrimaryLanguage(actualItem.getPrimaryLanguage()));
    }

    static void validateCategorizedEntitiesResultCollectionWithResponse(boolean showStatistics,
        Response<RecognizeEntitiesResultCollection> response) {
        assertNotNull(response);
        validateCategorizedEntitiesResultCollection(showStatistics, response.getValue());
    }

    static void validateCategorizedEntitiesResultCollection(boolean showStatistics, RecognizeEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual, (actualItem) ->
            validateCategorizedEntities(actualItem.getEntities().stream().collect(Collectors.toList())));
    }

    static void validatePiiEntitiesResultCollectionWithResponse(boolean showStatistics,
                                                                Response<RecognizePiiEntitiesResultCollection> response) {
        assertNotNull(response);
        validatePiiEntitiesResultCollection(showStatistics, response.getValue());
    }

    static void validatePiiEntitiesResultCollection(boolean showStatistics, RecognizePiiEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual, (actualItem) -> {
            final PiiEntityCollection actualPiiEntityCollection = actualItem.getEntities();
            assertNotNull(actualPiiEntityCollection.getRedactedText());
            validatePiiEntities(actualPiiEntityCollection.stream().collect(Collectors.toList()));
        });
    }

    static void validateLinkedEntitiesResultCollectionWithResponse(boolean showStatistics,
        Response<RecognizeLinkedEntitiesResultCollection> response) {
        assertNotNull(response);
        validateLinkedEntitiesResultCollection(showStatistics, response.getValue());
    }

    static void validateLinkedEntitiesResultCollection(boolean showStatistics,
                                                       RecognizeLinkedEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual, (actualItem) ->
            validateLinkedEntities(actualItem.getEntities().stream().collect(Collectors.toList())));
    }

    static void validateExtractKeyPhrasesResultCollectionWithResponse(boolean showStatistics,
        Response<ExtractKeyPhrasesResultCollection> response) {
        assertNotNull(response);
        validateExtractKeyPhrasesResultCollection(showStatistics, response.getValue());
    }

    static void validateClassifyDocumentResult(ClassifyDocumentResult actual) {
        assertNotNull(actual.getId());

        if (actual.isError()) {
            assertNull(actual.getError());
            List<ClassificationCategory> actualClassifications =
                actual.getClassifications().stream().collect(Collectors.toList());
            for (ClassificationCategory actualClassification : actualClassifications) {
                assertNotNull(actualClassification.getCategory());
            }
        }
    }

    static void validateExtractKeyPhrasesResultCollection(boolean showStatistics,
                                                          ExtractKeyPhrasesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual, (actualItem) ->
            validateKeyPhrases(actualItem.getKeyPhrases().stream().collect(Collectors.toList())));
    }

    static void validateAnalyzeSentimentResultCollectionWithResponse(boolean showStatistics,
        boolean includeOpinionMining, Response<AnalyzeSentimentResultCollection> response) {
        assertNotNull(response);
        validateAnalyzeSentimentResultCollection(showStatistics, includeOpinionMining, response.getValue());
    }

    static void validateAnalyzeSentimentResultCollection(boolean showStatistics, boolean includeOpinionMining,
                                                         AnalyzeSentimentResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual, (actualItem) ->
            validateDocumentSentiment(includeOpinionMining, actualItem.getDocumentSentiment()));
    }

    static void validateAnalyzeHealthcareEntitiesResultCollection(boolean showStatistics,
                                                                  AnalyzeHealthcareEntitiesResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual,
            (actualItem) -> validateHealthcareEntityDocumentResult(actualItem));
    }

    /**
     * Helper method to validate a single detected language.
     *
     * @param actualLanguage detectedLanguage returned by the API.
     */
    static void validatePrimaryLanguage(DetectedLanguage actualLanguage) {
        assertNotNull(actualLanguage.getName());
        assertNotNull(actualLanguage.getIso6391Name());
    }

    /**
     * Helper method to validate a single linked entity.
     *
     * @param actualLinkedEntity LinkedEntity returned by the API.
     */
    static void validateLinkedEntity(LinkedEntity actualLinkedEntity) {
        assertNotNull(actualLinkedEntity.getName());
        assertNotNull(actualLinkedEntity.getDataSource());
        assertNotNull(actualLinkedEntity.getLanguage());
        assertNotNull(actualLinkedEntity.getUrl());
        assertNotNull(actualLinkedEntity.getDataSourceEntityId());
        // TODO: Bing ID is missing. https://github.com/Azure/azure-sdk-for-java/issues/22208
        // assertEquals(expectedLinkedEntity.getBingEntitySearchApiId(), actualLinkedEntity.getBingEntitySearchApiId());
        validateLinkedEntityMatches(actualLinkedEntity.getMatches().stream().collect(Collectors.toList()));
    }

    /**
     * Helper method to validate a single key phrase.
     *
     * @param actualKeyPhrases key phrases returned by the API.
     */
    static void validateKeyPhrases(List<String> actualKeyPhrases) {
        for (int i = 0; i < actualKeyPhrases.size(); i++) {
            assertNotNull(actualKeyPhrases.get(i));
        }
    }

    /**
     * Helper method to validate the list of categorized entities.
     *
     * @param actualCategorizedEntityList categorizedEntities returned by the API.
     */
    static void validateCategorizedEntities(List<CategorizedEntity> actualCategorizedEntityList) {
        for (CategorizedEntity categorizedEntity : actualCategorizedEntityList) {
            assertNotNull(categorizedEntity.getText());
            assertNotNull(categorizedEntity.getCategory());
        }
    }

    /**
     * Helper method to validate the list of Personally Identifiable Information entities.
     *
     * @param actualPiiEntityList piiEntities returned by the API.
     */
    static void validatePiiEntities(List<PiiEntity> actualPiiEntityList) {
        for (PiiEntity actualPiiEntity : actualPiiEntityList) {
            assertNotNull(actualPiiEntity.getText());
            assertNotNull(actualPiiEntity.getCategory());
        }
    }

    /**
     * Helper method to validate the list of linked entities.
     *
     * @param actualLinkedEntityList linkedEntities returned by the API.
     */
    static void validateLinkedEntities(List<LinkedEntity> actualLinkedEntityList) {
        for (LinkedEntity actualLinkedEntity : actualLinkedEntityList) {
            validateLinkedEntity(actualLinkedEntity);
        }
    }

    /**
     * Helper method to validate the list of sentence sentiment. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param actualSentimentList a list of analyzed sentence sentiment returned by the API.
     */
    static void validateSentenceSentimentList(boolean includeOpinionMining, List<SentenceSentiment> actualSentimentList) {
        for (SentenceSentiment sentenceSentiment : actualSentimentList) {
            validateSentenceSentiment(includeOpinionMining, sentenceSentiment);
        }
    }

    /**
     * Helper method to validate one pair of analyzed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param actualSentiment analyzed sentence sentiment returned by the API.
     */
    static void validateSentenceSentiment(boolean includeOpinionMining, SentenceSentiment actualSentiment) {
        assertNotNull(actualSentiment.getSentiment());
        assertNotNull(actualSentiment.getText());

        if (includeOpinionMining) {
            validateSentenceOpinions(actualSentiment.getOpinions().stream().collect(Collectors.toList()));
        } else {
            assertNull(actualSentiment.getOpinions());
        }
    }

    /**
     * Helper method to validate sentence's opinions.
     *
     * @param actualSentenceOpinions a list of sentence opinions returned by the API.
     */
    static void validateSentenceOpinions(List<SentenceOpinion> actualSentenceOpinions) {
        for (int i = 0; i < actualSentenceOpinions.size(); i++) {
            final SentenceOpinion actualSentenceOpinion = actualSentenceOpinions.get(i);
            validateTargetSentiment(actualSentenceOpinion.getTarget());
            validateAssessmentList(actualSentenceOpinion.getAssessments().stream().collect(Collectors.toList()));
        }
    }

    /**
     * Helper method to validate target sentiment.
     *
     * @param actual An actual target sentiment.
     */
    static void validateTargetSentiment(TargetSentiment actual) {
        assertNotNull(actual.getSentiment());
        assertNotNull(actual.getText());
    }

    /**
     * Helper method to validate a list of {@link AssessmentSentiment}.
     *
     * @param actual A list of actual assessment sentiments.
     */
    static void validateAssessmentList(List<AssessmentSentiment> actual) {
        for (AssessmentSentiment assessmentSentiment : actual) {
            validateAssessmentSentiment(assessmentSentiment);
        }
    }

    /**
     * Helper method to validate assessment sentiment.
     *
     * @param actual An actual assessment sentiment.
     */
    static void validateAssessmentSentiment(AssessmentSentiment actual) {
        assertNotNull(actual.getSentiment());
        assertNotNull(actual.getText());
    }

    /**
     * Helper method to validate one pair of analyzed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param actualSentiment analyzed document sentiment returned by the API.
     */
    static void validateDocumentSentiment(boolean includeOpinionMining, DocumentSentiment actualSentiment) {
        assertNotNull(actualSentiment.getSentiment());
        validateSentenceSentimentList(includeOpinionMining,
            actualSentiment.getSentences().stream().collect(Collectors.toList()));
    }

    static void validateLabelClassificationResult(ClassifyDocumentResult documentResult) {
        assertNotNull(documentResult.getId());
        if (documentResult.isError()) {
            assertNotNull(documentResult.getError());
        } else {
            assertNull(documentResult.getError());
            for (ClassificationCategory classification : documentResult.getClassifications()) {
                assertNotNull(classification.getCategory());
            }
        }
    }

    // Healthcare task
    static void validateHealthcareEntity(HealthcareEntity actual) {
        assertNotNull(actual.getCategory());
        // It is petty easy to change the text of the entity, so we don't validate it. The service is responsible for
        // the text extraction. We just validate the text exist.
        assertNotNull(actual.getText());
    }


    static void validateHealthcareEntityDocumentResult(AnalyzeHealthcareEntitiesResult actual) {
        validateHealthcareEntityRelations(actual.getEntityRelations().stream().collect(Collectors.toList()));
        validateHealthcareEntities(actual.getEntities().stream().collect(Collectors.toList()));
    }

    static void validateHealthcareEntityRelations(List<HealthcareEntityRelation> actual) {
        for (int i = 0; i < actual.size(); i++) {
            validateHealthcareEntityRelation(actual.get(i));
        }
    }

    static void validateHealthcareEntityRelation(HealthcareEntityRelation actual) {
        final List<HealthcareEntityRelationRole> actualRoles = actual.getRoles().stream().collect(Collectors.toList());
        assertNotNull(actual.getRelationType());
        for (HealthcareEntityRelationRole actualRole : actualRoles) {
            validateHealthcareEntityRelationRole(actualRole);
        }
    }

    static void validateHealthcareEntityRelationRole(HealthcareEntityRelationRole actual) {
        assertNotNull(actual.getName());
        validateHealthcareEntity(actual.getEntity());
    }

    static void validateHealthcareEntities(List<HealthcareEntity> actual) {
        actual.sort(Comparator.comparing(HealthcareEntity::getText));
        for (HealthcareEntity healthcareEntity : actual) {
            validateHealthcareEntity(healthcareEntity);
        }
    }

    static void validateAnalyzeHealthcareEntitiesResultCollectionList(boolean showStatistics,
        List<AnalyzeHealthcareEntitiesResultCollection> actual) {
        for (AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResults : actual) {
            validateAnalyzeHealthcareEntitiesResultCollection(showStatistics, analyzeHealthcareEntitiesResults);
        }
    }

    // Analyze tasks
    static void validateAnalyzeBatchActionsResultList(boolean showStatistics, boolean includeOpinionMining,
                                                      List<AnalyzeActionsResult> actual) {
        for (AnalyzeActionsResult analyzeActionsResult : actual) {
            validateAnalyzeActionsResult(showStatistics, includeOpinionMining, analyzeActionsResult);
        }
    }

    static void validateAnalyzeActionsResult(boolean showStatistics, boolean includeOpinionMining,
                                             AnalyzeActionsResult actual) {
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
            actual.getRecognizeEntitiesResults().stream().collect(Collectors.toList()));
        validateRecognizeLinkedEntitiesActionResults(showStatistics,
            actual.getRecognizeLinkedEntitiesResults().stream().collect(Collectors.toList()));
        validateRecognizePiiEntitiesActionResults(showStatistics,
            actual.getRecognizePiiEntitiesResults().stream().collect(Collectors.toList()));
        validateAnalyzeHealthcareEntitiesActionResults(showStatistics,
            actual.getAnalyzeHealthcareEntitiesResults().stream().collect(Collectors.toList()));
        validateExtractKeyPhrasesActionResults(showStatistics,
            actual.getExtractKeyPhrasesResults().stream().collect(Collectors.toList()));
        validateAnalyzeSentimentActionResults(showStatistics, includeOpinionMining,
            actual.getAnalyzeSentimentResults().stream().collect(Collectors.toList()));
        validateExtractiveSummaryActionResults(showStatistics,
            actual.getExtractiveSummaryResults().stream().collect(Collectors.toList()));
        validateAbstractiveSummaryActionResults(showStatistics,
            actual.getAbstractiveSummaryResults().stream().collect(Collectors.toList()));
    }

    // Action results validation
    static void validateRecognizeEntitiesActionResults(boolean showStatistics, List<RecognizeEntitiesActionResult> actual) {
        for (RecognizeEntitiesActionResult recognizeEntitiesActionResult : actual) {
            validateRecognizeEntitiesActionResult(showStatistics, recognizeEntitiesActionResult);
        }
    }

    static void validateRecognizeLinkedEntitiesActionResults(boolean showStatistics,
                                                             List<RecognizeLinkedEntitiesActionResult> actual) {
        for (RecognizeLinkedEntitiesActionResult recognizeLinkedEntitiesActionResult : actual) {
            validateRecognizeLinkedEntitiesActionResult(showStatistics, recognizeLinkedEntitiesActionResult);
        }
    }

    static void validateRecognizePiiEntitiesActionResults(boolean showStatistics,
                                                          List<RecognizePiiEntitiesActionResult> actual) {
        for (RecognizePiiEntitiesActionResult recognizePiiEntitiesActionResult : actual) {
            validateRecognizePiiEntitiesActionResult(showStatistics, recognizePiiEntitiesActionResult);
        }
    }

    static void validateAnalyzeHealthcareEntitiesActionResults(boolean showStatistics,
                                                               List<AnalyzeHealthcareEntitiesActionResult> actual) {
        for (AnalyzeHealthcareEntitiesActionResult analyzeHealthcareEntitiesActionResult : actual) {
            validateAnalyzeHealthcareEntitiesActionResult(showStatistics, analyzeHealthcareEntitiesActionResult);
        }
    }

    static void validateExtractKeyPhrasesActionResults(boolean showStatistics,
                                                       List<ExtractKeyPhrasesActionResult> actual) {
        for (ExtractKeyPhrasesActionResult extractKeyPhrasesActionResult : actual) {
            validateExtractKeyPhrasesActionResult(showStatistics, extractKeyPhrasesActionResult);
        }
    }

    static void validateAnalyzeSentimentActionResults(boolean showStatistics, boolean includeOpinionMining,
                                                      List<AnalyzeSentimentActionResult> actual) {
        for (AnalyzeSentimentActionResult analyzeSentimentActionResult : actual) {
            validateAnalyzeSentimentActionResult(showStatistics, includeOpinionMining, analyzeSentimentActionResult);
        }
    }

    // Action result validation
    static void validateRecognizeEntitiesActionResult(boolean showStatistics, RecognizeEntitiesActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validateCategorizedEntitiesResultCollection(showStatistics, actual.getDocumentsResults());
        }
    }

    static void validateRecognizeLinkedEntitiesActionResult(boolean showStatistics,
                                                            RecognizeLinkedEntitiesActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validateLinkedEntitiesResultCollection(showStatistics, actual.getDocumentsResults());
        }
    }

    static void validateRecognizePiiEntitiesActionResult(boolean showStatistics,
                                                         RecognizePiiEntitiesActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validatePiiEntitiesResultCollection(showStatistics, actual.getDocumentsResults());
        }
    }

    static void validateAnalyzeHealthcareEntitiesActionResult(boolean showStatistics,
                                                              AnalyzeHealthcareEntitiesActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validateAnalyzeHealthcareEntitiesResultCollection(showStatistics, actual.getDocumentsResults());
        }
    }

    static void validateExtractKeyPhrasesActionResult(boolean showStatistics, ExtractKeyPhrasesActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validateExtractKeyPhrasesResultCollection(showStatistics, actual.getDocumentsResults());
        }
    }

    static void validateAnalyzeSentimentActionResult(boolean showStatistics, boolean includeOpinionMining,
                                                     AnalyzeSentimentActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validateAnalyzeSentimentResultCollection(showStatistics, includeOpinionMining, actual.getDocumentsResults());
        }
    }

    /**
     * Helper method to verify {@link TextAnalyticsResult documents} returned in a batch request.
     */
    static <T extends TextAnalyticsResult, H extends IterableStream<T>> void validateTextAnalyticsResult(
        boolean showStatistics, H actualResults, Consumer<T> additionalAssertions) {

        final Map<String, T> actual = actualResults.stream().collect(
            Collectors.toMap(TextAnalyticsResult::getId, r -> r));


        if (showStatistics) {
            if (actualResults instanceof AnalyzeHealthcareEntitiesResultCollection) {
                validateBatchStatistics(((AnalyzeHealthcareEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof AnalyzeSentimentResultCollection) {
                validateBatchStatistics(((AnalyzeSentimentResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof ClassifyDocumentResultCollection) {
                validateBatchStatistics(((ClassifyDocumentResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof DetectLanguageResultCollection) {
                validateBatchStatistics(((DetectLanguageResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof ExtractKeyPhrasesResultCollection) {
                validateBatchStatistics(((ExtractKeyPhrasesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof ExtractiveSummaryResultCollection) {
                validateBatchStatistics(((ExtractiveSummaryResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizeCustomEntitiesResultCollection) {
                validateBatchStatistics(((RecognizeCustomEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizeEntitiesResultCollection) {
                validateBatchStatistics(((RecognizeEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizeLinkedEntitiesResultCollection) {
                validateBatchStatistics(((RecognizeLinkedEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizePiiEntitiesResultCollection) {
                validateBatchStatistics(((RecognizePiiEntitiesResultCollection) actualResults).getStatistics());
            }
        } else {
            if (actualResults instanceof AnalyzeHealthcareEntitiesResultCollection) {
                assertNull(((AnalyzeHealthcareEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof AnalyzeSentimentResultCollection) {
                assertNull(((AnalyzeSentimentResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof ClassifyDocumentResultCollection) {
                assertNull(((ClassifyDocumentResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof DetectLanguageResultCollection) {
                assertNull(((DetectLanguageResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof ExtractKeyPhrasesResultCollection) {
                assertNull(((ExtractKeyPhrasesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof ExtractiveSummaryResultCollection) {
                assertNull(((ExtractiveSummaryResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizeCustomEntitiesResultCollection) {
                assertNull(((RecognizeCustomEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizeEntitiesResultCollection) {
                assertNull(((RecognizeEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizeLinkedEntitiesResultCollection) {
                assertNull(((RecognizeLinkedEntitiesResultCollection) actualResults).getStatistics());
            } else if (actualResults instanceof RecognizePiiEntitiesResultCollection) {
                assertNull(((RecognizePiiEntitiesResultCollection) actualResults).getStatistics());
            }
        }

        actual.forEach((key, actualValue) -> {
            assertNotNull(actualValue);

            if (showStatistics) {
                validateDocumentStatistics(actualValue.getStatistics());
            }

            if (actualValue.getError() == null) {
                assertNull(actualValue.getError());
            } else {
                assertNotNull(actualValue.getError());
                assertNotNull(actualValue.getError().getErrorCode());

                validateErrorDocument(actualValue.getError());
            }

            additionalAssertions.accept(actualValue);
        });
    }

    /**
     * Helper method to verify TextBatchStatistics.
     *
     * @param actualStatistics the value returned by API.
     */
    private static void validateBatchStatistics(TextDocumentBatchStatistics actualStatistics) {
        int documentCount = actualStatistics.getDocumentCount();
        int invalidDocumentCount = actualStatistics.getInvalidDocumentCount();
        int validDocumentCount = actualStatistics.getValidDocumentCount();
        if (documentCount > 0) {
            assertTrue(documentCount >= invalidDocumentCount);
            assertTrue(documentCount >= validDocumentCount);
        }
        assertTrue(actualStatistics.getTransactionCount() > 0);
    }

    /**
     * Helper method to verify TextDocumentStatistics.
     *
     * @param actual the value returned by API.
     */
    private static void validateDocumentStatistics(TextDocumentStatistics actual) {
        assertTrue(actual.getCharacterCount() > 0);
        assertTrue(actual.getTransactionCount() > 0);
    }

    /**
     * Helper method to verify LinkedEntityMatches.
     *
     * @param actualLinkedEntityMatches the value returned by API.
     */
    private static void validateLinkedEntityMatches(List<LinkedEntityMatch> actualLinkedEntityMatches) {
        for (LinkedEntityMatch actualLinkedEntity : actualLinkedEntityMatches) {
            assertNotNull(actualLinkedEntity.getText());
        }
    }

    /**
     * Helper method to verify the error document.
     *
     * @param actualError the Error returned from the API.
     */
    static void validateErrorDocument(TextAnalyticsError actualError) {
        assertNotNull(actualError.getErrorCode());
        assertNotNull(actualError.getMessage());
    }

    static void validateExtractiveSummaryResultCollection(boolean showStatistics,
                                                          ExtractiveSummaryResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual,
            (actualItem) -> validateDocumentExtractiveSummaryResult(actualItem));
    }

    static void validateDocumentExtractiveSummaryResult(ExtractiveSummaryResult actual) {
        validateExtractiveSummarySentenceList(actual.getSentences().stream().collect(Collectors.toList()));
    }

    static void validateExtractiveSummaryActionResults(boolean showStatistics,
                                                       List<ExtractiveSummaryActionResult> actual) {
        for (ExtractiveSummaryActionResult extractiveSummaryActionResult : actual) {
            validateExtractiveSummaryActionResult(showStatistics, extractiveSummaryActionResult);
        }
    }

    static void validateExtractiveSummaryActionResult(boolean showStatistics,
                                                      ExtractiveSummaryActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validateExtractiveSummaryResultCollection(showStatistics, actual.getDocumentsResults());
        }
    }

    static void validateExtractiveSummarySentenceList(List<ExtractiveSummarySentence> actual) {
        for (ExtractiveSummarySentence extractiveSummarySentence : actual) {
            assertNotNull(extractiveSummarySentence.getText());
        }
    }

    static boolean isAscendingOrderByOffSet(List<ExtractiveSummarySentence> extractiveSummarySentences) {
        int currMin = Integer.MIN_VALUE;
        for (ExtractiveSummarySentence extractiveSummarySentence : extractiveSummarySentences) {
            if (extractiveSummarySentence.getOffset() <= currMin) {
                return false;
            } else {
                currMin = extractiveSummarySentence.getOffset();
            }
        }
        return true;
    }

    static boolean isDescendingOrderByRankScore(List<ExtractiveSummarySentence> extractiveSummarySentences) {
        double currentMax = Double.MAX_VALUE;
        for (ExtractiveSummarySentence extractiveSummarySentence : extractiveSummarySentences) {
            if (extractiveSummarySentence.getRankScore() > currentMax) {
                return false;
            } else {
                currentMax = extractiveSummarySentence.getRankScore();
            }
        }
        return true;
    }

    static void validateAbstractiveSummaryActionResults(boolean showStatistics, List<AbstractiveSummaryActionResult> actual) {
        for (AbstractiveSummaryActionResult abstractiveSummaryActionResult : actual) {
            validateAbstractiveSummaryActionResult(showStatistics, abstractiveSummaryActionResult);
        }
    }

    static void validateAbstractiveSummaryActionResult(boolean showStatistics, AbstractiveSummaryActionResult actual) {
        if (actual.isError()) {
            assertNotNull(actual.getError());
            validateErrorDocument(actual.getError());
        } else {
            validateAbstractiveSummaryResultCollection(showStatistics, actual.getDocumentsResults());
        }
    }

    static void validateAbstractiveSummaryResultCollection(boolean showStatistics,
                                                           AbstractiveSummaryResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, actual,
                (actualItem) -> validateDocumentAbstractiveSummaryResult(actualItem));
    }

    static void validateDocumentAbstractiveSummaryResult(AbstractiveSummaryResult actual) {
        validateAbstractiveSummaries(actual.getSummaries().stream().collect(Collectors.toList()));
    }

    static void validateAbstractiveSummaries(List<AbstractiveSummary> actual) {
        for (final AbstractiveSummary abstractiveSummary : actual) {
            assertNotNull(abstractiveSummary.getText());
        }
    }
}
