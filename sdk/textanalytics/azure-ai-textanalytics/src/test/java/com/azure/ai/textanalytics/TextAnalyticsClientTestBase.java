// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.Error;
import com.azure.ai.textanalytics.models.KeyPhraseResult;
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
import com.azure.ai.textanalytics.models.TextSentimentClass;
import com.azure.ai.textanalytics.models.TextSentimentResult;
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

    static final String MODEL_VERSION = "2019-10-01";

    enum TestEndpoint {
        LANGUAGE, NAMED_ENTITY, LINKED_ENTITY, KEY_PHRASES, SENTIMENT
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
    public abstract void recognizeEntitiesForBatchStringInput();

    @Test
    public abstract void recognizeEntitiesForListLanguageHint();

    // Pii Entities
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

    // Linked Entities
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

    // Key Phrases
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

    // Detect Language runner
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

    static void detectLanguageStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.", "~@!~:)");

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

    // Named Entity runner
    static void recognizeNamedEntityStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        testRunner.accept(inputs);
    }

    static void recognizeNamedEntitiesLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        testRunner.accept(inputs, "en");
    }

    static void recognizeBatchNamedEntityRunner(Consumer<List<TextDocumentInput>> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));
        testRunner.accept(textDocumentInputs);
    }

    void recognizeBatchNamedEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));
        testRunner.accept(textDocumentInputs, setTextAnalyticsRequestOptions());
    }

    // Pii Entity runner
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

    // Linked Entity runner
    void recognizeBatchLinkedEntitiesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));
        testRunner.accept(textDocumentInputs, setTextAnalyticsRequestOptions());
    }

    static void recognizeLinkedLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        testRunner.accept(inputs, "en");
    }

    static void recognizeLinkedStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        testRunner.accept(inputs);
    }

    static void recognizeBatchLinkedEntityRunner(Consumer<List<TextDocumentInput>> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));
        testRunner.accept(textDocumentInputs);
    }

    // Key Phrases runner
    void extractBatchKeyPhrasesShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Hello world. This is some input text that I love."),
            new TextDocumentInput("1", "Bonjour tout le monde", "fr"));
        testRunner.accept(textDocumentInputs, setTextAnalyticsRequestOptions());
    }

    static void extractKeyPhrasesLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        testRunner.accept(inputs, "en");
    }

    static void extractKeyPhrasesStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        testRunner.accept(inputs);
    }

    static void extractBatchKeyPhrasesRunner(Consumer<List<TextDocumentInput>> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Hello world. This is some input text that I love."),
            new TextDocumentInput("1", "Bonjour tout le monde"));
        testRunner.accept(textDocumentInputs);
    }

    // Sentiment Runner
    static void analyseSentimentLanguageHintRunner(BiConsumer<List<String>, String> testRunner) {
        testRunner.accept(getSentimentInput(), "en");
    }

    static void analyseSentimentStringInputRunner(Consumer<List<String>> testRunner) {
        testRunner.accept(getSentimentInput());
    }

    static void analyseBatchSentimentRunner(Consumer<List<TextDocumentInput>> testRunner) {
        final List<String> sentimentInputs = getSentimentInput();
        testRunner.accept(Arrays.asList(
            new TextDocumentInput("0", sentimentInputs.get(0)),
            new TextDocumentInput("1", sentimentInputs.get(1))
        ));
    }

    void analyseBatchSentimentShowStatsRunner(
        BiConsumer<List<TextDocumentInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean. The restaurant had amazing gnocchi."),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi. The hotel was dark and unclean.")
        );
        testRunner.accept(textDocumentInputs, setTextAnalyticsRequestOptions());
    }

    static List<String> getSentimentInput() {
        return Arrays.asList("The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean.");
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
                    if (actualItem.getError() == null) {
                        if (this.showStatistics) {
                            validateDocumentStatistics(expectedItem.getStatistics(), actualItem.getStatistics());
                        }
                        validatePrimaryLanguage(expectedItem.getPrimaryLanguage(), actualItem.getPrimaryLanguage());
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
                    if (actualItem.getError() == null) {
                        if (this.showStatistics) {
                            validateDocumentStatistics(expectedItem.getStatistics(), actualItem.getStatistics());
                        }
                        validateNamedEntities(expectedItem.getNamedEntities(), actualItem.getNamedEntities());
                    }
                });
                break;
            case LINKED_ENTITY:
                final List<LinkedEntityResult> linkedEntityResults = expectedResult.stream()
                    .filter(element -> element instanceof LinkedEntityResult)
                    .map(element -> (LinkedEntityResult) element)
                    .collect(Collectors.toList());

                final List<LinkedEntityResult> actualLinkedEntityResults = actualResult.stream()
                    .filter(element -> element instanceof LinkedEntityResult)
                    .map(element -> (LinkedEntityResult) element)
                    .collect(Collectors.toList());
                assertEquals(linkedEntityResults.size(), actualLinkedEntityResults.size());

                actualLinkedEntityResults.forEach(actualItem -> {
                    Optional<LinkedEntityResult> optionalExpectedItem = linkedEntityResults.stream().filter(
                        expectedEachItem -> actualItem.getId().equals(expectedEachItem.getId())).findFirst();
                    assertTrue(optionalExpectedItem.isPresent());
                    LinkedEntityResult expectedItem = optionalExpectedItem.get();
                    if (actualItem.getError() == null) {
                        if (this.showStatistics) {
                            validateDocumentStatistics(expectedItem.getStatistics(), actualItem.getStatistics());
                        }
                        validateLinkedEntities(expectedItem.getLinkedEntities(), actualItem.getLinkedEntities());
                    }
                });
                break;
            case KEY_PHRASES:
                final List<KeyPhraseResult> keyPhraseResults = expectedResult.stream()
                    .filter(element -> element instanceof KeyPhraseResult)
                    .map(element -> (KeyPhraseResult) element)
                    .collect(Collectors.toList());

                final List<KeyPhraseResult> actualKeyPhraseResults = actualResult.stream()
                    .filter(element -> element instanceof KeyPhraseResult)
                    .map(element -> (KeyPhraseResult) element)
                    .collect(Collectors.toList());
                assertEquals(keyPhraseResults.size(), actualKeyPhraseResults.size());

                actualKeyPhraseResults.forEach(actualItem -> {
                    Optional<KeyPhraseResult> optionalExpectedItem = keyPhraseResults.stream().filter(
                        expectedEachItem -> actualItem.getId().equals(expectedEachItem.getId())).findFirst();
                    assertTrue(optionalExpectedItem.isPresent());
                    KeyPhraseResult expectedItem = optionalExpectedItem.get();
                    if (actualItem.getError() == null) {
                        if (this.showStatistics) {
                            validateDocumentStatistics(expectedItem.getStatistics(), actualItem.getStatistics());
                        }
                        validateKeyPhrases(expectedItem.getKeyPhrases(), actualItem.getKeyPhrases());
                    }
                });
                break;
            case SENTIMENT:
                final List<TextSentimentResult> expectedSentimentResults = expectedResult.stream()
                    .filter(element -> element instanceof TextSentimentResult)
                    .map(element -> (TextSentimentResult) element)
                    .collect(Collectors.toList());

                final List<TextSentimentResult> actualSentimentResults = actualResult.stream()
                    .filter(element -> element instanceof TextSentimentResult)
                    .map(element -> (TextSentimentResult) element)
                    .collect(Collectors.toList());

                expectedSentimentResults.sort(Comparator.comparing(TextSentimentResult::getId));
                actualSentimentResults.sort(Comparator.comparing(TextSentimentResult::getId));
                final int actualSize = actualSentimentResults.size();
                final int expectedSize = expectedSentimentResults.size();
                assertEquals(expectedSize, actualSize);

                for (int i = 0; i < actualSize; i++) {
                    final TextSentimentResult actualSentimentResult = actualSentimentResults.get(i);
                    final TextSentimentResult expectedSentimentResult = expectedSentimentResults.get(i);

                    if (actualSentimentResult.getError() == null) {
                        if (this.showStatistics) {
                            validateDocumentStatistics(expectedSentimentResult.getStatistics(), actualSentimentResult.getStatistics());
                        }
                        validateAnalysedSentiment(expectedSentimentResult.getDocumentSentiment(), actualSentimentResult.getDocumentSentiment());
                        validateAnalysedSentenceSentiment(expectedSentimentResult.getSentenceSentiments(), actualSentimentResult.getSentenceSentiments());
                    } else {
                        validateErrorDocument(actualSentimentResult.getError(), actualSentimentResult.getError());
                    }
                }
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported testApi : '%s'.", testApi));
        }
    }

    /**
     * Helper method to verify TextBatchStatistics.
     *
     * @param expectedStatistics the expected value for TextBatchStatistics.
     * @param actualStatistics the value returned by API.
     */
    private static void validateBatchStatistics(TextBatchStatistics expectedStatistics,
        TextBatchStatistics actualStatistics) {
        assertEquals(expectedStatistics.getDocumentCount(), actualStatistics.getDocumentCount());
        assertEquals(expectedStatistics.getErroneousDocumentCount(), actualStatistics.getErroneousDocumentCount());
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
            assertEquals(expectedLinkedEntity.getLength(), actualLinkedEntity.getLength());
            assertEquals(expectedLinkedEntity.getOffset(), actualLinkedEntity.getOffset());
            assertEquals(expectedLinkedEntity.getScore(), actualLinkedEntity.getScore());
            assertEquals(expectedLinkedEntity.getText(), actualLinkedEntity.getText());
        }
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

    /**
     * Helper method to validate a single key phrase.
     *
     * @param expectedKeyPhrases key phrases returned by the service.
     * @param actualKeyPhrases key phrases returned by the API.
     */
    void validateKeyPhrases(List<String> expectedKeyPhrases, List<String> actualKeyPhrases) {
        assertEquals(expectedKeyPhrases.size(), actualKeyPhrases.size());
        Collections.sort(expectedKeyPhrases);
        Collections.sort(actualKeyPhrases);

        for (int i = 0; i < expectedKeyPhrases.size(); i++) {
            assertTrue(expectedKeyPhrases.get(i).equals(actualKeyPhrases.get(i)));
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
     * Helper method to validate the list of sentence sentiment. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentimentList a list of analyzed sentence sentiment returned by the service.
     * @param actualSentimentList a list of analyzed sentence sentiment returned by the API.
     */
    static void validateAnalysedSentenceSentiment(List<TextSentiment> expectedSentimentList,
        List<TextSentiment> actualSentimentList) {

        assertEquals(expectedSentimentList.size(), actualSentimentList.size());
        for (int i = 0; i < expectedSentimentList.size(); i++) {
            validateAnalysedSentiment(expectedSentimentList.get(i), actualSentimentList.get(i));
        }
    }

    /**
     * Helper method to validate one pair of analysed sentiments. Can't really validate score numbers because it
     * frequently changed by background model computation.
     *
     * @param expectedSentiment analyzed document sentiment returned by the service.
     * @param actualSentiment analyzed document sentiment returned by the API.
     */
    static void validateAnalysedSentiment(TextSentiment expectedSentiment, TextSentiment actualSentiment) {
        assertEquals(expectedSentiment.getLength(), actualSentiment.getLength());
        assertEquals(expectedSentiment.getOffset(), actualSentiment.getOffset());
        assertEquals(expectedSentiment.getTextSentimentClass(), actualSentiment.getTextSentimentClass());

        assertEquals(expectedSentiment.getNegativeScore() > 0, actualSentiment.getNegativeScore() > 0);
        assertEquals(expectedSentiment.getNeutralScore() > 0, actualSentiment.getNeutralScore() > 0);
        assertEquals(expectedSentiment.getPositiveScore() > 0, actualSentiment.getPositiveScore() > 0);
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

        return new DocumentResultCollection<>(detectLanguageResultList, MODEL_VERSION, textBatchStatistics);
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

        return new DocumentResultCollection<>(namedEntityResultList, MODEL_VERSION, textBatchStatistics);
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

        return new DocumentResultCollection<>(namedEntityResultList, MODEL_VERSION, textBatchStatistics);
    }

    static DocumentResultCollection<LinkedEntityResult> getExpectedBatchLinkedEntities() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch().setText("Seattle").setLength(7).setOffset(26).setScore(0.11472424095537814);
        LinkedEntityMatch linkedEntityMatch2 = new LinkedEntityMatch().setText("Microsoft").setLength(9).setOffset(10).setScore(0.18693659716732069);

        LinkedEntity linkedEntity1 = new LinkedEntity()
            .setId("Seattle")
            .setName("Seattle")
            .setLanguage("en")
            .setUrl("https://en.wikipedia.org/wiki/Seattle")
            .setDataSource("Wikipedia")
            .setLinkedEntityMatches(Collections.singletonList(linkedEntityMatch1));

        LinkedEntity linkedEntity2 = new LinkedEntity()
            .setId("Microsoft")
            .setName("Microsoft")
            .setLanguage("en")
            .setUrl("https://en.wikipedia.org/wiki/Microsoft")
            .setDataSource("Wikipedia")
            .setLinkedEntityMatches(Collections.singletonList(linkedEntityMatch2));

        List<LinkedEntity> linkedEntityList1 = Collections.singletonList(linkedEntity1);
        List<LinkedEntity> linkedEntityList2 = Collections.singletonList(linkedEntity2);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setCharacterCount(44).setTransactionCount(1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setCharacterCount(20).setTransactionCount(1);

        LinkedEntityResult linkedEntityResult1 = new LinkedEntityResult("0", textDocumentStatistics1, null, linkedEntityList1);
        LinkedEntityResult linkedEntityResult2 = new LinkedEntityResult("1", textDocumentStatistics2, null, linkedEntityList2);

        TextBatchStatistics textBatchStatistics = new TextBatchStatistics().setDocumentCount(2).setErroneousDocumentCount(0).setTransactionCount(2).setValidDocumentCount(2);
        List<LinkedEntityResult> linkedEntityResultList = Arrays.asList(linkedEntityResult1, linkedEntityResult2);

        return new DocumentResultCollection<>(linkedEntityResultList, MODEL_VERSION, textBatchStatistics);
    }

    static DocumentResultCollection<KeyPhraseResult> getExpectedBatchKeyPhrases() {
        List<String> keyPhrasesList1 = Arrays.asList("input text", "world");
        List<String> keyPhrasesList2 = Arrays.asList("monde");

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setCharacterCount(49).setTransactionCount(1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setCharacterCount(21).setTransactionCount(1);

        KeyPhraseResult keyPhraseResult1 = new KeyPhraseResult("0", textDocumentStatistics1, null, keyPhrasesList1);
        KeyPhraseResult keyPhraseResult2 = new KeyPhraseResult("1", textDocumentStatistics2, null, keyPhrasesList2);

        TextBatchStatistics textBatchStatistics = new TextBatchStatistics().setDocumentCount(2).setErroneousDocumentCount(0).setTransactionCount(2).setValidDocumentCount(2);
        List<KeyPhraseResult> keyPhraseResultList = Arrays.asList(keyPhraseResult1, keyPhraseResult2);

        return new DocumentResultCollection<>(keyPhraseResultList, MODEL_VERSION, textBatchStatistics);
    }

    static DocumentResultCollection<TextSentimentResult> getExpectedBatchTextSentiment() {
        final TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setCharacterCount(67)
            .setTransactionCount(1);
        final TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setCharacterCount(67)
            .setTransactionCount(1);

        final TextSentiment expectedDocumentSentiment = new TextSentiment(TextSentimentClass.MIXED,
            0.00019, 0.5, 0.4, 66, 0);

        final TextSentimentResult textSentimentResult1 = new TextSentimentResult("0", textDocumentStatistics1,
            null,
            expectedDocumentSentiment,
            Arrays.asList(
                new TextSentiment(TextSentimentClass.NEGATIVE, 0.99, 0.0, 0.0, 31, 0),
                new TextSentiment(TextSentimentClass.POSITIVE, 0.0, 0.0, 0.99, 35, 32)
            ));

        final TextSentimentResult textSentimentResult2 = new TextSentimentResult("1", textDocumentStatistics2,
            null,
            expectedDocumentSentiment,
            Arrays.asList(
                new TextSentiment(TextSentimentClass.POSITIVE, 0.0, 0.0, 0.99, 35, 0),
                new TextSentiment(TextSentimentClass.NEGATIVE, 0.99, 0.0, 0.0, 31, 36)
            ));

        return new DocumentResultCollection<>(Arrays.asList(textSentimentResult1, textSentimentResult2),
            MODEL_VERSION,
            new TextBatchStatistics().setDocumentCount(2).setErroneousDocumentCount(0)
                .setTransactionCount(2).setValidDocumentCount(2));
    }

    private TextAnalyticsRequestOptions setTextAnalyticsRequestOptions() {
        this.showStatistics = true;
        return new TextAnalyticsRequestOptions().setShowStatistics(true);
    }

}
