// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.MinedOpinion;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.LINKED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.PII_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SENTIMENT_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.TOO_LONG_INPUT;
import static com.azure.ai.textanalytics.TestUtils.getDuplicateTextDocumentInputs;
import static com.azure.ai.textanalytics.TestUtils.getWarningsTextDocumentInputs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class TextAnalyticsClientTestBase extends TestBase {
    static final String BATCH_ERROR_EXCEPTION_MESSAGE = "Error in accessing the property on document id: 2, when %s returned with an error: Document text is empty. ErrorCodeValue: {invalidDocument}";
    static final String INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE = "Country hint is not valid. Please specify an ISO 3166-1 alpha-2 two letter country code. ErrorCodeValue: {invalidCountryHint}";
    static final String INVALID_DOCUMENT_BATCH_NPE_MESSAGE = "'documents' cannot be null.";
    static final String INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE = "'documents' cannot be empty.";
    static final String INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE = "Document text is empty. ErrorCodeValue: {invalidDocument}";
    static final String INVALID_DOCUMENT_NPE_MESSAGE = "'document' cannot be null.";
    static final String WARNING_TOO_LONG_DOCUMENT_INPUT_MESSAGE = "The document contains very long words (longer than 64 characters). These words will be truncated and may result in unreliable model predictions.";

    // Detect Language
    @Test
    abstract void detectSingleTextLanguage(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguageEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguagesBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguagesBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void detectLanguagesBatchListCountryHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

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
    abstract void recognizeEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Personally Identifiable Information Entities
    @Test
    abstract void recognizePiiEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesBatchInputSingleError(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizePiiEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Linked Entities
    @Test
    abstract void recognizeLinkedEntitiesForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForBatchInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void recognizeLinkedEntitiesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

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
    abstract void extractKeyPhrasesForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesForListLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesBatchWarning(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void extractKeyPhrasesBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    // Sentiment
    @Test
    abstract void analyzeSentimentForTextInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForTextInputWithDefaultLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForTextInputWithOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForEmptyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForFaultyText(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentDuplicateIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentEmptyIdInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchStringInput(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringWithLanguageHint(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForListStringShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputWithNullRequestOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputShowStatistics(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputWithNullAnalyzeSentimentOptions(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputShowStatisticsExcludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputNotShowStatisticsButIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentForBatchInputShowStatisticsAndIncludeOpinionMining(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

    @Test
    abstract void analyzeSentimentBatchTooManyDocuments(HttpClient httpClient, TextAnalyticsServiceVersion serviceVersion);

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
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = TestUtils.getTextDocumentInputs(PII_ENTITY_INPUTS);
        TextAnalyticsRequestOptions options = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        testRunner.accept(textDocumentInputs, options);
    }

    void recognizeStringBatchPiiEntitiesShowStatsRunner(
        BiConsumer<List<String>, TextAnalyticsRequestOptions> testRunner) {
        testRunner.accept(PII_ENTITY_INPUTS, new TextAnalyticsRequestOptions().setIncludeStatistics(true));
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
    };

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

    void faultyTextRunner(Consumer<String> testRunner) {
        testRunner.accept("!@#%%");
    }

    void detectLanguageInputEmptyIdRunner(Consumer<List<DetectLanguageInput>> testRunner) {
        testRunner.accept(Arrays.asList(new DetectLanguageInput("", DETECT_LANGUAGE_INPUTS.get(0))));
    }

    void textAnalyticsInputEmptyIdRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(Arrays.asList(new TextDocumentInput("", CATEGORIZED_ENTITY_INPUTS.get(0))));
    }

    void tooManyDocumentsRunner(
        Consumer<List<String>> testRunner) {
        final String documentInput = CATEGORIZED_ENTITY_INPUTS.get(0);
        // max num of document size is 10
        testRunner.accept(Arrays.asList(
            documentInput, documentInput, documentInput, documentInput, documentInput, documentInput,
            documentInput, documentInput, documentInput, documentInput, documentInput, documentInput));
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
            builder.credential(new DefaultAzureCredentialBuilder().build());
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
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validatePiiEntities(
                expectedItem.getEntities().stream().collect(Collectors.toList()),
                actualItem.getEntities().stream().collect(Collectors.toList())));
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

    static void validateSentimentResultCollectionWithResponse(boolean showStatistics, boolean includeOpinionMining,
        AnalyzeSentimentResultCollection expected,
        int expectedStatusCode, Response<AnalyzeSentimentResultCollection> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSentimentResultCollection(showStatistics, includeOpinionMining, expected, response.getValue());
    }

    static void validateSentimentResultCollection(boolean showStatistics, boolean includeOpinionMining,
        AnalyzeSentimentResultCollection expected, AnalyzeSentimentResultCollection actual) {
        validateTextAnalyticsResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateAnalyzedSentiment(includeOpinionMining, expectedItem.getDocumentSentiment(), actualItem.getDocumentSentiment()));
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
        assertEquals(expectedCategorizedEntity.getLength(), actualCategorizedEntity.getLength());
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
        assertEquals(expectedPiiEntity.getLength() > 0, actualPiiEntity.getLength() > 0);
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
        assertEquals(expectedLinkedEntity.getUrl(), actualLinkedEntity.getUrl());
        assertEquals(expectedLinkedEntity.getDataSourceEntityId(), actualLinkedEntity.getDataSourceEntityId());
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
    static void validateAnalyzedSentenceSentiment(boolean includeOpinionMining, List<SentenceSentiment> expectedSentimentList,
        List<SentenceSentiment> actualSentimentList) {

        assertEquals(expectedSentimentList.size(), actualSentimentList.size());
        for (int i = 0; i < expectedSentimentList.size(); i++) {
            validateSentenceSentiment(includeOpinionMining, expectedSentimentList.get(i), actualSentimentList.get(i));
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
            validateSentenceMinedOpinions(expectedSentiment.getMinedOpinions().stream().collect(Collectors.toList()),
                actualSentiment.getMinedOpinions().stream().collect(Collectors.toList()));
        } else {
            assertNull(actualSentiment.getMinedOpinions());
        }
    }

    /**
     * Helper method to validate sentence's mined opinions.
     *
     * @param expectedMinedOpinions a list of mined opinions returned by the service.
     * @param actualMinedOpinions a list of mined opinions returned by the API.
     */
    static void validateSentenceMinedOpinions(List<MinedOpinion> expectedMinedOpinions,
        List<MinedOpinion> actualMinedOpinions) {
        assertEquals(expectedMinedOpinions.size(), actualMinedOpinions.size());
        for (int i = 0; i < actualMinedOpinions.size(); i++) {
            final MinedOpinion expectedMinedOpinion = expectedMinedOpinions.get(i);
            final MinedOpinion actualMinedOpinion = actualMinedOpinions.get(i);
            validateAspectSentiment(expectedMinedOpinion.getAspect(), actualMinedOpinion.getAspect());
            validateAspectOpinionList(expectedMinedOpinion.getOpinions().stream().collect(Collectors.toList()),
                actualMinedOpinion.getOpinions().stream().collect(Collectors.toList()));
        }
    }

    /**
     * Helper method to validate aspect sentiment.
     *
     * @param expectedAspectSentiment An expected aspect sentiment.
     * @param actualAspectSentiment An actual aspect sentiment.
     */
    static void validateAspectSentiment(AspectSentiment expectedAspectSentiment, AspectSentiment actualAspectSentiment) {
        assertEquals(expectedAspectSentiment.getSentiment(), actualAspectSentiment.getSentiment());
        assertEquals(expectedAspectSentiment.getText(), actualAspectSentiment.getText());
        assertEquals(expectedAspectSentiment.getLength(), actualAspectSentiment.getLength());
        assertEquals(expectedAspectSentiment.getOffset(), actualAspectSentiment.getOffset());
    }

    /**
     * Helper method to validate a list of {@link OpinionSentiment}.
     *
     * @param expectedOpinionSentiments A list of expected opinion sentiments.
     * @param actualOpinionSentiments A list of actual opinion sentiments.
     */
    static void validateAspectOpinionList(List<OpinionSentiment> expectedOpinionSentiments, List<OpinionSentiment> actualOpinionSentiments) {
        assertEquals(expectedOpinionSentiments.size(), actualOpinionSentiments.size());
        for (int i = 0; i < expectedOpinionSentiments.size(); i++) {
            validateAspectOpinion(expectedOpinionSentiments.get(i), actualOpinionSentiments.get(i));
        }
    }

    /**
     * Helper method to validate opinion sentiment.
     *
     * @param expectedAspectOpinion An expected opinion sentiment.
     * @param actualAspectOpinion An actual opinion sentiment.
     */
    static void validateAspectOpinion(OpinionSentiment expectedAspectOpinion, OpinionSentiment actualAspectOpinion) {
        assertEquals(expectedAspectOpinion.getSentiment(), actualAspectOpinion.getSentiment());
        assertEquals(expectedAspectOpinion.getText(), actualAspectOpinion.getText());
        assertEquals(expectedAspectOpinion.isNegated(), actualAspectOpinion.isNegated());
        assertEquals(expectedAspectOpinion.getLength(), actualAspectOpinion.getLength());
        assertEquals(expectedAspectOpinion.getOffset(), actualAspectOpinion.getOffset());
    }

    /**
     * Helper method to validate one pair of analyzed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentiment analyzed document sentiment returned by the service.
     * @param actualSentiment analyzed document sentiment returned by the API.
     */
    static void validateAnalyzedSentiment(boolean includeOpinionMining, DocumentSentiment expectedSentiment,
        DocumentSentiment actualSentiment) {
        assertEquals(expectedSentiment.getSentiment(), actualSentiment.getSentiment());
        validateAnalyzedSentenceSentiment(includeOpinionMining,
            expectedSentiment.getSentences().stream().collect(Collectors.toList()),
            actualSentiment.getSentences().stream().collect(Collectors.toList()));
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
            assertEquals(expectedLinkedEntity.getLength(), actualLinkedEntity.getLength());
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
        assertEquals(expectedError.getMessage(), actualError.getMessage());
        assertEquals(expectedError.getTarget(), actualError.getTarget());
    }
}
