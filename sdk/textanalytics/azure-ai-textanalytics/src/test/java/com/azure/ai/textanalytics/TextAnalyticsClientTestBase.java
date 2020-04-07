// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResult;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TestUtils.CATEGORIZED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.DETECT_LANGUAGE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.KEY_PHRASE_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.LINKED_ENTITY_INPUTS;
import static com.azure.ai.textanalytics.TestUtils.SENTIMENT_INPUTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TextAnalyticsClientTestBase extends TestBase {
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final String NAME = "name";
    private static final String TEXT_ANALYTICS_PROPERTIES = "azure-ai-textanalytics.properties";
    private static final String VERSION = "version";

    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(TEXT_ANALYTICS_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    static final String BATCH_ERROR_EXCEPTION_MESSAGE = "Error in accessing the property on document id: 2, when RecognizeCategorizedEntitiesResult returned with an error: Document text is empty. ErrorCodeValue: {invalidDocument}";
    static final String INVALID_COUNTRY_HINT_EXPECTED_EXCEPTION_MESSAGE = "Country hint is not valid. Please specify an ISO 3166-1 alpha-2 two letter country code. ErrorCodeValue: {invalidCountryHint}";
    static final String INVALID_DOCUMENT_BATCH_NPE_MESSAGE = "'documents' cannot be null.";
    static final String INVALID_DOCUMENT_EMPTY_LIST_EXCEPTION_MESSAGE = "'documents' cannot be empty.";
    static final String INVALID_DOCUMENT_EXPECTED_EXCEPTION_MESSAGE = "Document text is empty. ErrorCodeValue: {invalidDocument}";
    static final String INVALID_DOCUMENT_NPE_MESSAGE = "'document' cannot be null.";

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        HttpClient httpClient;
        Configuration buildConfiguration = Configuration.getGlobalConfiguration().clone();

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, DEFAULT_SCOPE));
        }
        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
        }
        policies.add(interceptorManager.getRecordPolicy());

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    // Detect Language
    @Test
    abstract void detectSingleTextLanguage();

    @Test
    abstract void detectLanguageEmptyText();

    @Test
    abstract void detectLanguageFaultyText();

    @Test
    abstract void detectLanguagesBatchInput();

    @Test
    abstract void detectLanguagesBatchInputShowStatistics();

    @Test
    abstract void detectLanguagesBatchListCountryHint();

    // Categorized Entities
    @Test
    abstract void recognizeEntitiesForTextInput();

    @Test
    abstract void recognizeEntitiesForEmptyText();

    @Test
    abstract void recognizeEntitiesForFaultyText();

    @Test
    abstract void recognizeEntitiesBatchInputSingleError();

    @Test
    abstract void recognizeEntitiesForBatchInput();

    @Test
    abstract void recognizeEntitiesForBatchInputShowStatistics();

    @Test
    abstract void recognizeEntitiesForListLanguageHint();

    // Linked Entities
    @Test
    abstract void recognizeLinkedEntitiesForTextInput();

    @Test
    abstract void recognizeLinkedEntitiesForEmptyText();

    @Test
    abstract void recognizeLinkedEntitiesForFaultyText();

    @Test
    abstract void recognizeLinkedEntitiesForBatchInput();

    @Test
    abstract void recognizeLinkedEntitiesForBatchInputShowStatistics();

    @Test
    abstract void recognizeLinkedEntitiesForListLanguageHint();

    // Key Phrases
    @Test
    abstract void extractKeyPhrasesForTextInput();

    @Test
    abstract void extractKeyPhrasesForEmptyText();

    @Test
    abstract void extractKeyPhrasesForFaultyText();

    @Test
    abstract void extractKeyPhrasesForBatchInput();

    @Test
    abstract void extractKeyPhrasesForBatchInputShowStatistics();

    @Test
    abstract void extractKeyPhrasesForListLanguageHint();

    // Sentiment
    @Test
    abstract void analyseSentimentForTextInput();

    @Test
    abstract void analyseSentimentForEmptyText();

    @Test
    abstract void analyseSentimentForFaultyText();

    @Test
    abstract void analyseSentimentForBatchInput();

    @Test
    abstract void analyseSentimentForBatchInputShowStatistics();

    @Test
    abstract void analyseSentimentForBatchStringInput();

    @Test
    abstract void analyseSentimentForListLanguageHint();

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

    // Categorized Entity runner
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

    // Linked Entity runner
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

    // Sentiment Runner
    void analyseSentimentLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS, "en");
    }

    void analyseSentimentStringInputRunner(Consumer<List<String>> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS);
    }

    void analyseBatchSentimentRunner(Consumer<List<TextDocumentInput>> testRunner) {
        testRunner.accept(TestUtils.getTextDocumentInputs(SENTIMENT_INPUTS));
    }

    void analyseBatchStringSentimentShowStatsRunner(BiConsumer<List<String>, TextAnalyticsRequestOptions> testRunner) {
        testRunner.accept(SENTIMENT_INPUTS, new TextAnalyticsRequestOptions().setIncludeStatistics(true));
    }

    void analyseBatchSentimentShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = TestUtils.getTextDocumentInputs(SENTIMENT_INPUTS);

        TextAnalyticsRequestOptions options = new TextAnalyticsRequestOptions().setIncludeStatistics(true);
        testRunner.accept(textDocumentInputs, options);
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }

    static void validateDetectLanguage(boolean showStatistics, TextAnalyticsPagedResponse<DetectLanguageResult> expected,
        TextAnalyticsPagedResponse<DetectLanguageResult> actual) {
        validateDocumentResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validatePrimaryLanguage(expectedItem.getPrimaryLanguage(), actualItem.getPrimaryLanguage()));
    }

    static void validateCategorizedEntitiesWithPagedResponse(boolean showStatistics,
        TextAnalyticsPagedResponse<RecognizeCategorizedEntitiesResult> expected,
        TextAnalyticsPagedResponse<RecognizeCategorizedEntitiesResult> actual) {

        validateDocumentResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateCategorizedEntities(
                expectedItem.getEntities().stream().collect(Collectors.toList()),
                actualItem.getEntities().stream().collect(Collectors.toList())));
    }

    static void validateCategorizedEntities(
        TextAnalyticsPagedResponse<CategorizedEntity> expected, TextAnalyticsPagedResponse<CategorizedEntity> actual) {
        validateCategorizedEntities(expected.getValue(), actual.getValue());
    }

    static void validateLinkedEntitiesWithPagedResponse(boolean showStatistics,
        TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult> expected,
        TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult> actual) {
        validateDocumentResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateLinkedEntities(
                expectedItem.getEntities().stream().collect(Collectors.toList()),
                actualItem.getEntities().stream().collect(Collectors.toList())));
    }

    static void validateExtractKeyPhraseWithPagedResponse(boolean showStatistics,
        TextAnalyticsPagedResponse<ExtractKeyPhraseResult> expected,
        TextAnalyticsPagedResponse<ExtractKeyPhraseResult> actual) {
        validateDocumentResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateKeyPhrases(
                expectedItem.getKeyPhrases().stream().collect(Collectors.toList()),
                actualItem.getKeyPhrases().stream().collect(Collectors.toList())));
    }

    static void validateSentimentWithPagedResponse(boolean showStatistics,
        TextAnalyticsPagedResponse<AnalyzeSentimentResult> expected,
        TextAnalyticsPagedResponse<AnalyzeSentimentResult> actual) {
        validateDocumentResult(showStatistics, expected, actual, (expectedItem, actualItem) ->
            validateAnalyzedSentiment(expectedItem.getDocumentSentiment(), actualItem.getDocumentSentiment()));
    }

    /**
     * Helper method to validate a single detected language.
     *
     * @param expectedLanguage detectedLanguage returned by the service.
     * @param actualLanguage detectedLanguage returned by the API.
     */
    static void validatePrimaryLanguage(DetectedLanguage expectedLanguage, DetectedLanguage actualLanguage) {
        assertEquals(expectedLanguage.getIso6391Name(), actualLanguage.getIso6391Name());
        assertEquals(expectedLanguage.getName(), actualLanguage.getName());
        assertNotNull(actualLanguage.getScore());
    }

    /**
     * Helper method to validate a single categorized entity.
     *
     * @param expectedCategorizedEntity CategorizedEntity returned by the service.
     * @param actualCategorizedEntity CategorizedEntity returned by the API.
     */
    static void validateCategorizedEntity(
        CategorizedEntity expectedCategorizedEntity, CategorizedEntity actualCategorizedEntity) {
        assertEquals(expectedCategorizedEntity.getGraphemeLength() > 0, actualCategorizedEntity.getGraphemeLength() > 0);
        assertEquals(expectedCategorizedEntity.getGraphemeOffset(), actualCategorizedEntity.getGraphemeOffset());
        assertEquals(expectedCategorizedEntity.getSubCategory(), actualCategorizedEntity.getSubCategory());
        assertEquals(expectedCategorizedEntity.getText(), actualCategorizedEntity.getText());
        assertEquals(expectedCategorizedEntity.getCategory(), actualCategorizedEntity.getCategory());
        assertNotNull(actualCategorizedEntity.getConfidenceScore());
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
        validateLinkedEntityMatches(expectedLinkedEntity.getLinkedEntityMatches().stream().collect(Collectors.toList()),
            actualLinkedEntity.getLinkedEntityMatches().stream().collect(Collectors.toList()));
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
    static void validateAnalyzedSentenceSentiment(List<SentenceSentiment> expectedSentimentList,
        List<SentenceSentiment> actualSentimentList) {

        assertEquals(expectedSentimentList.size(), actualSentimentList.size());
        for (int i = 0; i < expectedSentimentList.size(); i++) {
            validateSentenceSentiment(expectedSentimentList.get(i), actualSentimentList.get(i));
        }
    }

    /**
     * Helper method to validate one pair of analyzed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentiment analyzed sentence sentiment returned by the service.
     * @param actualSentiment analyzed sentence sentiment returned by the API.
     */
    static void validateSentenceSentiment(SentenceSentiment expectedSentiment, SentenceSentiment actualSentiment) {
        assertEquals(expectedSentiment.getSentiment(), actualSentiment.getSentiment());
        // score per label doesn't need to verify since service can return it with different values
        assertEquals(expectedSentiment.getGraphemeOffset(), actualSentiment.getGraphemeOffset());
        assertTrue(actualSentiment.getGraphemeLength() > 0);
    }

    /**
     * Helper method to validate one pair of analyzed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentiment analyzed document sentiment returned by the service.
     * @param actualSentiment analyzed document sentiment returned by the API.
     */
    static void validateAnalyzedSentiment(DocumentSentiment expectedSentiment, DocumentSentiment actualSentiment) {
        assertEquals(expectedSentiment.getSentiment(), actualSentiment.getSentiment());
        validateAnalyzedSentenceSentiment(expectedSentiment.getSentences().stream().collect(Collectors.toList()),
            expectedSentiment.getSentences().stream().collect(Collectors.toList()));
    }

    /**
     * Helper method to verify {@link DocumentResult documents} returned in a batch request.
     */
    private static <T extends DocumentResult> void validateDocumentResult(boolean showStatistics,
        TextAnalyticsPagedResponse<T> expectedResults, TextAnalyticsPagedResponse<T> actualResults,
        BiConsumer<T, T> additionalAssertions) {

        final Map<String, T> expected = expectedResults.getElements().stream().collect(
            Collectors.toMap(DocumentResult::getId, r -> r));
        final Map<String, T> actual = actualResults.getElements().stream().collect(
            Collectors.toMap(DocumentResult::getId, r -> r));

        assertEquals(expected.size(), actual.size());

        if (showStatistics) {
            validateBatchStatistics(expectedResults.getStatistics(), actualResults.getStatistics());
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
                assertEquals(expectedValue.getError().getCode(), actualValue.getError().getCode());

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
        assertEquals(expected.getGraphemeCount(), actual.getGraphemeCount());
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
            assertEquals(expectedLinkedEntity.getGraphemeLength() > 0, actualLinkedEntity.getGraphemeLength() > 0);
            assertEquals(expectedLinkedEntity.getGraphemeOffset(), actualLinkedEntity.getGraphemeOffset());
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
        assertEquals(expectedError.getCode(), actualError.getCode());
        assertEquals(expectedError.getMessage(), actualError.getMessage());
        assertEquals(expectedError.getTarget(), actualError.getTarget());
    }
}
