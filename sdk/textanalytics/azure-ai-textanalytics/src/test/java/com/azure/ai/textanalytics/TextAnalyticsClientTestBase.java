// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.Error;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.LinkedEntityResult;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.NamedEntityResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class TextAnalyticsClientTestBase extends TestBase {
    private static final String TEXT_ANALYTICS_PROPERTIES = "azure-ai-textanalytics.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";

    final Map<String, String> properties = CoreUtils.getProperties(TEXT_ANALYTICS_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    private boolean showStatistics = false;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();

    enum TestEndpoint {
        LANGUAGE, NAMED_ENTITY, LINKED_ENTITY
    }

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
    public abstract void detectSingleTextLanguage();

    @Test
    public abstract void detectLanguageEmptyText();

    @Test
    public abstract void detectLanguageFaultyText();

    @Test
    public abstract void detectLanguagesBatchInput();

    @Test
    public abstract void detectLanguagesBatchInputShowStatistics();

    @Test
    public abstract void detectLanguagesBatchStringInput();

    @Test
    public abstract void detectLanguagesBatchListCountryHint();

    void detectLanguageShowStatisticsRunner(BiConsumer<List<DetectLanguageInput>,
        TextAnalyticsRequestOptions> testRunner) {
        final List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("0", "This is written in English", "US"),
            new DetectLanguageInput("1", "Este es un document escrito en Español."),
            new DetectLanguageInput("2", "~@!~:)", "US")
            // add error document => empty text
        );

        testRunner.accept(detectLanguageInputs, setTextAnalyticsRequestOptions());
    }

    void detectLanguageDuplicateIdRunner(BiConsumer<List<DetectLanguageInput>,
        TextAnalyticsRequestOptions> testRunner) {
        final List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("0", "This is written in English", "US"),
            new DetectLanguageInput("0", "Este es un document escrito en Español.")
        );

        testRunner.accept(detectLanguageInputs, setTextAnalyticsRequestOptions());
    }

    static void detectLanguagesCountryHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.", "~@!~:)");

        testRunner.accept(inputs, "en");
    }

    static void recognizeNamedEntitiesLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        testRunner.accept(inputs, "en");
    }
    static void detectLanguageStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.", "~@!~:)");

        testRunner.accept(inputs);
    }

    static void recognizeNamedEntityStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        testRunner.accept(inputs);
    }

    static void detectLanguageRunner(Consumer<List<DetectLanguageInput>> testRunner) {
        final List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("0", "This is written in English", "US"),
            new DetectLanguageInput("1", "Este es un document escrito en Español."),
            new DetectLanguageInput("2", "~@!~:)", "US")
        );

        testRunner.accept(detectLanguageInputs);
    }

    static void recognizeBatchNamedEntityRunner(Consumer<List<TextDocumentInput>> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));
        testRunner.accept(textDocumentInputs);
    }

    static void recognizePiiLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
            "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

        testRunner.accept(inputs, "en");
    }
    static void recognizePiiStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
            "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

        testRunner.accept(inputs);
    }

    static void recognizeBatchPiiRunner(Consumer<List<TextDocumentInput>> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Microsoft employee with ssn 859-98-0987 is using our awesome API's."),
            new TextDocumentInput("1", "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check."));
        testRunner.accept(textDocumentInputs);
    }

    void recognizeBatchPiiEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Microsoft employee with ssn 859-98-0987 is using our awesome API's."),
            new TextDocumentInput("1", "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check."));
        testRunner.accept(textDocumentInputs, setTextAnalyticsRequestOptions());
    }

    void recognizeBatchNamedEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));
        testRunner.accept(textDocumentInputs, setTextAnalyticsRequestOptions());
    }

    void recognizeBatchLinkedEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));
        testRunner.accept(textDocumentInputs, setTextAnalyticsRequestOptions());
    }

    static void recognizeLinkedLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
            "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

        testRunner.accept(inputs, "en");
    }
    static void recognizeLinkedStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
            "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

        testRunner.accept(inputs);
    }

    static void recognizeBatchLinkedEntityRunner(Consumer<List<TextDocumentInput>> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Microsoft employee with ssn 859-98-0987 is using our awesome API's."),
            new TextDocumentInput("1", "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check."));
        testRunner.accept(textDocumentInputs);
    }

    static DocumentResultCollection<DetectLanguageResult> getExpectedBatchDetectedLanguages() {
        DetectedLanguage detectedLanguage1 = new DetectedLanguage().setName("English").setIso6391Name("en")
            .setScore(1.0);
        DetectedLanguage detectedLanguage2 = new DetectedLanguage().setName("Spanish").setIso6391Name("es")
            .setScore(1.0);
        DetectedLanguage detectedLanguage3 = new DetectedLanguage().setName("(Unknown)").setIso6391Name("(Unknown)")
            .setScore(0.0);
        List<DetectedLanguage> detectedLanguageList1 = Collections.singletonList(detectedLanguage1);
        List<DetectedLanguage> detectedLanguageList2 = Collections.singletonList(detectedLanguage2);
        List<DetectedLanguage> detectedLanguageList3 = Collections.singletonList(detectedLanguage3);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setCharacterCount(26).setTransactionCount(1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setCharacterCount(39).setTransactionCount(1);
        TextDocumentStatistics textDocumentStatistics3 = new TextDocumentStatistics().setCharacterCount(6).setTransactionCount(1);

        DetectLanguageResult detectLanguageResult1 = new DetectLanguageResult("0", textDocumentStatistics1, null, detectedLanguage1, detectedLanguageList1);
        DetectLanguageResult detectLanguageResult2 = new DetectLanguageResult("1", textDocumentStatistics2, null, detectedLanguage2, detectedLanguageList2);
        DetectLanguageResult detectLanguageResult3 = new DetectLanguageResult("2", textDocumentStatistics3, null, detectedLanguage3, detectedLanguageList3);

        TextBatchStatistics textBatchStatistics = new TextBatchStatistics().setDocumentCount(3).setErroneousDocumentCount(0).setTransactionCount(3).setValidDocumentCount(3);
        List<DetectLanguageResult> detectLanguageResultList = Arrays.asList(detectLanguageResult1, detectLanguageResult2, detectLanguageResult3);

        return new DocumentResultCollection<>(detectLanguageResultList, "2019-10-01", textBatchStatistics);
    }

    static DocumentResultCollection<NamedEntityResult> getExpectedBatchNamedEntities() {
        NamedEntity namedEntity1 = new NamedEntity().setText("Seattle").setType("Location").setOffset(26).setLength(7).setScore(0.80624294281005859);
        NamedEntity namedEntity2 = new NamedEntity().setText("last week").setType("DateTime").setSubtype("DateRange").setOffset(34).setLength(9).setScore(0.8);
        NamedEntity namedEntity3 = new NamedEntity().setText("Microsoft").setType("Organization").setOffset(10).setLength(9).setScore(0.99983596801757812);

        List<NamedEntity> namedEntityList1 = Arrays.asList(namedEntity1, namedEntity2);
        List<NamedEntity> namedEntityList2 = Collections.singletonList(namedEntity3);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setCharacterCount(44).setTransactionCount(1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setCharacterCount(20).setTransactionCount(1);

        NamedEntityResult namedEntityResult1 = new NamedEntityResult("0", textDocumentStatistics1, null, namedEntityList1);
        NamedEntityResult namedEntityResult2 = new NamedEntityResult("1", textDocumentStatistics2, null, namedEntityList2);

        TextBatchStatistics textBatchStatistics = new TextBatchStatistics().setDocumentCount(2).setErroneousDocumentCount(0).setTransactionCount(2).setValidDocumentCount(2);
        List<NamedEntityResult> namedEntityResultList = Arrays.asList(namedEntityResult1, namedEntityResult2);

        return new DocumentResultCollection<>(namedEntityResultList, "2019-10-01", textBatchStatistics);
    }

    static DocumentResultCollection<NamedEntityResult> getExpectedBatchPiiEntities() {
        NamedEntity namedEntity1 = new NamedEntity().setText("859-98-0987").setType("U.S. Social Security Number (SSN)").setSubtype("").setOffset(28).setLength(11).setScore(0.65);
        NamedEntity namedEntity2 = new NamedEntity().setText("111000025").setType("ABA Routing Number").setSubtype("").setOffset(18).setLength(9).setScore(0.75);

        List<NamedEntity> namedEntityList1 = Collections.singletonList(namedEntity1);
        List<NamedEntity> namedEntityList2 = Collections.singletonList(namedEntity2);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setCharacterCount(67).setTransactionCount(1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setCharacterCount(105).setTransactionCount(1);

        NamedEntityResult namedEntityResult1 = new NamedEntityResult("0", textDocumentStatistics1, null, namedEntityList1);
        NamedEntityResult namedEntityResult2 = new NamedEntityResult("1", textDocumentStatistics2, null, namedEntityList2);

        TextBatchStatistics textBatchStatistics = new TextBatchStatistics().setDocumentCount(2).setErroneousDocumentCount(0).setTransactionCount(2).setValidDocumentCount(2);
        List<NamedEntityResult> namedEntityResultList = Arrays.asList(namedEntityResult1, namedEntityResult2);

        return new DocumentResultCollection<>(namedEntityResultList, "2019-10-01", textBatchStatistics);
    }

    static DocumentResultCollection<LinkedEntityResult> getExpectedBatchLinkedEntities() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch().setText("Seattle").setLength(7).setOffset(26).setScore(0.11472424095537814);
        LinkedEntityMatch linkedEntityMatch2 = new LinkedEntityMatch().setText("Microsoft").setLength(9).setOffset(10).setScore(0.18693659716732069);

        LinkedEntity linkedEntity1 = new LinkedEntity().setName("Seattle").setUrl("https://en.wikipedia.org/wiki/Seattle").setDataSource("Wikipedia").setLinkedEntityMatches(Collections.singletonList(linkedEntityMatch1));
        LinkedEntity linkedEntity2 = new LinkedEntity().setName("Microsoft").setUrl("https://en.wikipedia.org/wiki/Microsoft").setDataSource("Wikipedia").setLinkedEntityMatches(Collections.singletonList(linkedEntityMatch2));

        List<LinkedEntity> linkedEntityList1 = Collections.singletonList(linkedEntity1);
        List<LinkedEntity> linkedEntityList2 = Collections.singletonList(linkedEntity2);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setCharacterCount(67).setTransactionCount(1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setCharacterCount(105).setTransactionCount(1);

        LinkedEntityResult linkedEntityResult1 = new LinkedEntityResult("0", textDocumentStatistics1, null, linkedEntityList1);
        LinkedEntityResult linkedEntityResult2 = new LinkedEntityResult("1", textDocumentStatistics2, null, linkedEntityList2);

        TextBatchStatistics textBatchStatistics = new TextBatchStatistics().setDocumentCount(2).setErroneousDocumentCount(0).setTransactionCount(2).setValidDocumentCount(2);
        List<LinkedEntityResult> linkedEntityResultList = Arrays.asList(linkedEntityResult1, linkedEntityResult2);

        return new DocumentResultCollection<>(linkedEntityResultList, "2019-10-01", textBatchStatistics);
    }

    // Named Entities
    @Test
    public abstract void recognizeEntitiesForTextInput();

    @Test
    public abstract void recognizeEntitiesForEmptyText();

    @Test
    public abstract void recognizeEntitiesForFaultyText();

    @Test
    public abstract void recognizeEntitiesForBatchInput();

    @Test
    public abstract void recognizeEntitiesForBatchInputShowStatistics();

    @Test
    public abstract void recognizePiiEntitiesForTextInput();

    @Test
    public abstract void recognizePiiEntitiesForEmptyText();

    @Test
    public abstract void recognizePiiEntitiesForFaultyText();

    @Test
    public abstract void recognizePiiEntitiesForBatchInput();

    @Test
    public abstract void recognizePiiEntitiesForBatchInputShowStatistics();

    @Test
    public abstract void recognizePiiEntitiesForBatchStringInput();

    @Test
    public abstract void recognizePiiEntitiesForListLanguageHint();

    @Test
    public abstract void recognizeEntitiesForBatchStringInput();

    @Test
    public abstract void recognizeEntitiesForListLanguageHint();

    @Test
    public abstract void recognizeLinkedEntitiesForTextInput();

    @Test
    public abstract void recognizeLinkedEntitiesForEmptyText();

    @Test
    public abstract void recognizeLinkedEntitiesForFaultyText();

    @Test
    public abstract void recognizeLinkedEntitiesForBatchInput();

    @Test
    public abstract void recognizeLinkedEntitiesForBatchInputShowStatistics();

    @Test
    public abstract void recognizeLinkedEntitiesForBatchStringInput();

    @Test
    public abstract void recognizeLinkedEntitiesForListLanguageHint();



    @Test
    public abstract void recognizeKeyPhrasesForTextInput();

    @Test
    public abstract void recognizeKeyPhrasesForEmptyText();

    @Test
    public abstract void recognizeKeyPhrasesForFaultyText();

    @Test
    public abstract void recognizeKeyPhrasesForBatchInput();

    @Test
    public abstract void recognizeKeyPhrasesForBatchInputShowStatistics();

    @Test
    public abstract void recognizeKeyPhrasesForBatchStringInput();

    @Test
    public abstract void recognizeKeyPhrasesForListLanguageHint();



    // Sentiment
    @Test
    public abstract void analyseSentimentForTextInput();

    @Test
    public abstract void analyseSentimentForEmptyText();

    @Test
    public abstract void analyseSentimentForFaultyText();

    @Test
    public abstract void analyseSentimentForBatchInput();

    @Test
    public abstract void analyseSentimentForBatchInputShowStatistics();

    @Test
    public abstract void analyseSentimentForBatchStringInput();

    @Test
    public abstract void analyseSentimentForListLanguageHint();

    static void analyseSentimentLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "The hotel was dark and unclean."
        );

        testRunner.accept(inputs, "en");
    }

    static void analyseSentimentStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "The hotel was dark and unclean.",
            "The restaurant had amazing gnocchi."
        );

        testRunner.accept(inputs);
    }

    static void analyseBatchSentimentRunner(Consumer<List<TextDocumentInput>> testRunner) {

    }

    private TextAnalyticsRequestOptions setTextAnalyticsRequestOptions() {
        this.showStatistics = true;
        return new TextAnalyticsRequestOptions().setShowStatistics(true);
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }

    /**
     * Helper method to verify batch result.
     *
     * @param actualResult DocumentResultCollection<> returned by the API.
     * @param testApi the API to test.
     */
    <T> void validateBatchResult(DocumentResultCollection<T> actualResult,
        DocumentResultCollection<T> expectedResult, TestEndpoint testApi) {
        // assert batch result
        assertEquals(expectedResult.getModelVersion(), actualResult.getModelVersion());
        if (this.showStatistics) {
            validateBatchStatistics(expectedResult.getStatistics(), actualResult.getStatistics());
        }
        validateDocuments(expectedResult, actualResult, testApi);

        // TODO (savaity): refactor error model in azure-sdk-for-java#6559
        // DocumentError error = new DocumentError().setId("4").setError("error");
        // List<DocumentError> errors = new ArrayList<>();
        // errors.add(error);
        // validateErrorDocuments(expected.getErrors(), detectLanguageResultList);
    }

    /**
     * Helper method to verify documents returned in a batch request.
     *
     * @param expectedResult the expected result collection..
     * @param actualResult the actual result collection returned by the API.
     * @param testApi the API to test.
     */
    private <T> void validateDocuments(DocumentResultCollection<T> expectedResult,
        DocumentResultCollection<T> actualResult, TestEndpoint testApi) {
        switch (testApi) {
            case LANGUAGE:
                final List<DetectLanguageResult> detectLanguageResults = expectedResult.stream()
                    .filter(element -> element instanceof DetectLanguageResult)
                    .map(element -> (DetectLanguageResult) element)
                    .collect(Collectors.toList());

                final List<DetectLanguageResult> actualDetectLanguageResults = actualResult.stream()
                    .filter(element -> element instanceof DetectLanguageResult)
                    .map(element -> (DetectLanguageResult) element)
                    .collect(Collectors.toList());
                assertEquals(detectLanguageResults.size(), actualDetectLanguageResults.size());

                actualDetectLanguageResults.forEach(actualItem -> {
                    Optional<DetectLanguageResult> optionalExpectedItem = detectLanguageResults.stream().filter(
                        expectedEachItem -> actualItem.getId().equals(expectedEachItem.getId())).findFirst();
                    assertTrue(optionalExpectedItem.isPresent());
                    DetectLanguageResult expectedItem = optionalExpectedItem.get();
                    if (actualItem.getError() == null && this.showStatistics) {
                        validatePrimaryLanguage(expectedItem.getPrimaryLanguage(), actualItem.getPrimaryLanguage());
                        validateDocumentStatistics(expectedItem.getStatistics(), actualItem.getStatistics());
                        validateDetectedLanguages(expectedItem.getDetectedLanguages(), actualItem.getDetectedLanguages());
                    }
                });
                break;
            case NAMED_ENTITY:
                final List<NamedEntityResult> namedEntityResults = expectedResult.stream()
                    .filter(element -> element instanceof NamedEntityResult)
                    .map(element -> (NamedEntityResult) element)
                    .collect(Collectors.toList());

                final List<NamedEntityResult> actualNamedEntityResults = actualResult.stream()
                    .filter(element -> element instanceof NamedEntityResult)
                    .map(element -> (NamedEntityResult) element)
                    .collect(Collectors.toList());
                assertEquals(namedEntityResults.size(), actualNamedEntityResults.size());

                actualNamedEntityResults.forEach(actualItem -> {
                    Optional<NamedEntityResult> optionalExpectedItem = namedEntityResults.stream().filter(
                        expectedEachItem -> actualItem.getId().equals(expectedEachItem.getId())).findFirst();
                    assertTrue(optionalExpectedItem.isPresent());
                    NamedEntityResult expectedItem = optionalExpectedItem.get();
                    if (actualItem.getError() == null && this.showStatistics) {
                        validateDocumentStatistics(expectedItem.getStatistics(), actualItem.getStatistics());
                        validateNamedEntities(expectedItem.getNamedEntities(), actualItem.getNamedEntities());
                    }
                });
                break;
            case LINKED_ENTITY:
                final List<NamedEntityResult> linkedEntityResults = expectedResult.stream()
                    .filter(element -> element instanceof NamedEntityResult)
                    .map(element -> (NamedEntityResult) element)
                    .collect(Collectors.toList());

                final List<NamedEntityResult> actualLinkedEntityResults = actualResult.stream()
                    .filter(element -> element instanceof NamedEntityResult)
                    .map(element -> (NamedEntityResult) element)
                    .collect(Collectors.toList());
                assertEquals(linkedEntityResults.size(), actualLinkedEntityResults.size());

                actualLinkedEntityResults.forEach(actualItem -> {
                    Optional<NamedEntityResult> optionalExpectedItem = linkedEntityResults.stream().filter(
                        expectedEachItem -> actualItem.getId().equals(expectedEachItem.getId())).findFirst();
                    assertTrue(optionalExpectedItem.isPresent());
                    NamedEntityResult expectedItem = optionalExpectedItem.get();
                    if (actualItem.getError() == null && this.showStatistics) {
                        validateDocumentStatistics(expectedItem.getStatistics(), actualItem.getStatistics());
                        validateNamedEntities(expectedItem.getNamedEntities(), actualItem.getNamedEntities());
                    }
                });
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported testApi : '%s'.", testApi));
        }
    }

    /**
     * Helper method to verify TextBatchStatistics.
     *
     * @param expectedStatistics
     * @param actualStatistics
     */
    private static void validateBatchStatistics(TextBatchStatistics expectedStatistics,
        TextBatchStatistics actualStatistics) {
        assertEquals(expectedStatistics.getDocumentCount(), actualStatistics.getDocumentCount());
        assertEquals(expectedStatistics.getErroneousDocumentCount(), actualStatistics.getErroneousDocumentCount());
        assertEquals(expectedStatistics.getValidDocumentCount(), actualStatistics.getValidDocumentCount());
        assertEquals(expectedStatistics.getTransactionCount(), actualStatistics.getTransactionCount());
    }

    /**
     * Helper method to verify the error document.
     *
     * @param expectedError the Error returned from the service.
     * @param actualError the Error returned from the API.
     */
    static void validateErrorDocument(Error expectedError, Error actualError) {
        assertEquals(expectedError.getCode(), actualError.getCode());
        assertEquals(expectedError.getMessage(), actualError.getMessage());
        assertEquals(expectedError.getTarget(), actualError.getTarget());
        assertEquals(expectedError.getInnererror(), actualError.getInnererror());
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
     * Helper method to validate a single detected language.
     *
     * @param expectedLanguage detectedLanguage returned by the service.
     * @param actualLanguage detectedLanguage returned by the API.
     */
    static void validatePrimaryLanguage(DetectedLanguage expectedLanguage, DetectedLanguage actualLanguage) {
        assertEquals(expectedLanguage.getIso6391Name(), actualLanguage.getIso6391Name());
        assertEquals(expectedLanguage.getName(), actualLanguage.getName());
        assertEquals(expectedLanguage.getScore(), actualLanguage.getScore());
    }

    /**
     * Helper method to validate the list of detected languages.
     *
     * @param expectedLanguageList detectedLanguages returned by the service.
     * @param actualLanguageList detectedLanguages returned by the API.
     */
    static void validateDetectedLanguages(List<DetectedLanguage> expectedLanguageList,
        List<DetectedLanguage> actualLanguageList) {
        assertEquals(expectedLanguageList.size(), actualLanguageList.size());
        expectedLanguageList.sort(Comparator.comparing(DetectedLanguage::getName));
        actualLanguageList.sort(Comparator.comparing(DetectedLanguage::getName));

        for (int i = 0; i < expectedLanguageList.size(); i++) {
            DetectedLanguage expectedDetectedLanguage = expectedLanguageList.get(i);
            DetectedLanguage actualDetectedLanguage = actualLanguageList.get(i);
            validatePrimaryLanguage(expectedDetectedLanguage, actualDetectedLanguage);
        }
    }

    /**
     * Helper method to validate a single named entity.
     *
     * @param expectedNamedEntity namedEntity returned by the service.
     * @param actualNamedEntity namedEntity returned by the API.
     */
    static void validateNamedEntity(NamedEntity expectedNamedEntity, NamedEntity actualNamedEntity) {

        assertEquals(expectedNamedEntity.getLength(), actualNamedEntity.getLength());
        assertEquals(expectedNamedEntity.getOffset(), actualNamedEntity.getOffset());
        assertEquals(expectedNamedEntity.getScore(), actualNamedEntity.getScore());
        assertEquals(expectedNamedEntity.getSubtype(), actualNamedEntity.getSubtype());
        assertEquals(expectedNamedEntity.getText(), actualNamedEntity.getText());
        assertEquals(expectedNamedEntity.getType(), actualNamedEntity.getType());

    }

    /**
     * Helper method to validate a single named entity.
     *
     * @param expectedLinkedEntity namedEntity returned by the service.
     * @param actualLinkedEntity namedEntity returned by the API.
     */
    static void validateLinkedEntity(LinkedEntity expectedLinkedEntity, LinkedEntity actualLinkedEntity) {

        assertEquals(expectedLinkedEntity.getName(), actualLinkedEntity.getName());
        assertEquals(expectedLinkedEntity.getDataSource(), actualLinkedEntity.getDataSource());
        assertEquals(expectedLinkedEntity.getLanguage(), actualLinkedEntity.getLanguage());
        assertEquals(expectedLinkedEntity.getUri(), actualLinkedEntity.getUri());
        assertEquals(expectedLinkedEntity.getId(), actualLinkedEntity.getId());
        validateLinkedEntityMatches(expectedLinkedEntity.getLinkedEntityMatches(), actualLinkedEntity.getLinkedEntityMatches());
    }

    private static void validateLinkedEntityMatches(List<LinkedEntityMatch> expectedLinkedEntityMatches, List<LinkedEntityMatch> actualLinkedEntityMatches1) {
        assertEquals(expectedLinkedEntityMatches.size(), actualLinkedEntityMatches1.size());
        expectedLinkedEntityMatches.sort(Comparator.comparing(LinkedEntityMatch::getText));
        actualLinkedEntityMatches1.sort(Comparator.comparing(LinkedEntityMatch::getText));

        for (int i = 0; i < expectedLinkedEntityMatches.size(); i++) {
            LinkedEntityMatch expectedLinkedEntity = expectedLinkedEntityMatches.get(i);
            LinkedEntityMatch actualLinkedEntity = actualLinkedEntityMatches1.get(i);
            assertEquals(expectedLinkedEntity.getLength(), actualLinkedEntity.getLength());
            assertEquals(expectedLinkedEntity.getOffset(), actualLinkedEntity.getOffset());
            assertEquals(expectedLinkedEntity.getScore(), actualLinkedEntity.getScore());
            assertEquals(expectedLinkedEntity.getText(), actualLinkedEntity.getText());
        }
    }

    /**
     * Helper method to validate the list of named entities.
     *
     * @param expectedNamedEntityList namedEntities returned by the service.
     * @param actualNamedEntityList namedEntities returned by the API.
     */
    static void validateNamedEntities(List<NamedEntity> expectedNamedEntityList,
        List<NamedEntity> actualNamedEntityList) {
        assertEquals(expectedNamedEntityList.size(), actualNamedEntityList.size());
        expectedNamedEntityList.sort(Comparator.comparing(NamedEntity::getText));
        actualNamedEntityList.sort(Comparator.comparing(NamedEntity::getText));

        for (int i = 0; i < expectedNamedEntityList.size(); i++) {
            NamedEntity expectedNamedEntity = expectedNamedEntityList.get(i);
            NamedEntity actualNamedEntity = actualNamedEntityList.get(i);
            validateNamedEntity(expectedNamedEntity, actualNamedEntity);
        }
    }

    /**
     * Helper method to validate the list of named entities.
     *
     * @param expectedLinkedEntityList namedEntities returned by the service.
     * @param actualLinkedEntityList namedEntities returned by the API.
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
     * Helper method to validate the list of named entities.
     *
     * @param expectedSentimentList analyzed sentiment returned by the service.
     * @param actualSentimentList analyzed sentiment returned by the API.
     */
    static void validAnalyzedSentiment(List<TextSentiment> expectedSentimentList,
                                       List<TextSentiment> actualSentimentList) {

        assertEquals(expectedSentimentList.size(), actualSentimentList.size());
        expectedSentimentList.sort(Comparator.comparing(TextSentiment::getTextSentimentClass));
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType,
        int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    static void assertRestException(Runnable exceptionThrower,
        Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }
}
