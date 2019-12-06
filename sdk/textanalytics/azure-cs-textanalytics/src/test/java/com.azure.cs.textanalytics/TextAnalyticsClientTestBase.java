// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

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
import com.azure.core.util.logging.ClientLogger;
import com.azure.cs.textanalytics.implementation.models.DocumentLanguage;
import com.azure.cs.textanalytics.implementation.models.LanguageResult;
import com.azure.cs.textanalytics.models.DetectLanguageInput;
import com.azure.cs.textanalytics.models.DetectLanguageResult;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DocumentError;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.cs.textanalytics.models.TextBatchStatistics;
import com.azure.cs.textanalytics.models.TextDocumentStatistics;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class TextAnalyticsClientTestBase extends TestBase {
    private static final String TEXT_ANALYTICS_PROPERTIES = "azure-textanalytics.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";

    final Map<String, String> properties = CoreUtils.getProperties(TEXT_ANALYTICS_PROPERTIES);

    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientTestBase.class);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    private boolean showStatistics = false;

    void beforeTestSetup() {
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        final String endpoint = getEndPoint();

        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        HttpClient httpClient;
        // Closest to API goes first, closest to wire goes last.
        Configuration buildConfiguration = Configuration.getGlobalConfiguration().clone();
        TextAnalyticsServiceVersion serviceVersion = TextAnalyticsServiceVersion.getLatest();

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(null, clientName, clientVersion, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, String.format("%s/.default", endpoint)));
        }
        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));


        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
            policies.add(interceptorManager.getRecordPolicy());
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    @Test
    public abstract void detectLanguage();

    @Test
    public abstract void detectLanguagesBatchInput();

    void detectLanguageShowStatisticsRunner(BiConsumer<List<DetectLanguageInput>, TextAnalyticsRequestOptions> testRunner) {
        final List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("0", "This is written in English", "US"),
            new DetectLanguageInput("1", "Este es un document escrito en Español."),
            new DetectLanguageInput("2", "~@!~:)", "US")
            // add error document => empty text
        );

        testRunner.accept(detectLanguageInputs, setTextAnalyticsRequestOptions());
    }

    static void detectLanguagesCountryHintRunner(BiConsumer<List<String>, String> testRunner) {
        final List<String> inputs = new ArrayList<>(Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.", "~@!~:)"));

        testRunner.accept(inputs, "US");
    }

    static void detectLanguageStringInputRunner(Consumer<List<String>> testRunner) {
        final List<String> inputs = new ArrayList<>(Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.", "~@!~:)"));

        testRunner.accept(inputs);
    }

    private TextAnalyticsRequestOptions setTextAnalyticsRequestOptions() {
        this.showStatistics = true;
        return new TextAnalyticsRequestOptions().setShowStatistics(true);
    }

    void detectLanguageRunner(Consumer<List<DetectLanguageInput>> testRunner) {
        final List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("0", "This is written in English", "US"),
            new DetectLanguageInput("1", "Este es un document escrito en Español."),
            new DetectLanguageInput("2", "~@!~:)", "US")
        );

        testRunner.accept(detectLanguageInputs);
    }

    String getEndPoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }

    /**
     * Helper method to verify returned LanguageResult (batch result) matches with teh actual DocumentResultCollection
     * model.
     *
     * @param actualResult DocumentResultCollection<> returned by the API.
     * @param testApi the API to test.
     */
    <T> void validateBatchResult(DocumentResultCollection<T> actualResult, String testApi) {
        Iterable<T> iterable = actualResult::iterator;
        List<T> list = StreamSupport
            .stream(iterable.spliterator(), false)
            .collect(Collectors.toList());

        LanguageResult expectedResult = getExpectedBatchResult();
        // assert batch result
        assertEquals(expectedResult.getModelVersion(), actualResult.getModelVersion());
        if (this.showStatistics) {
            validateBatchStatistics(expectedResult.getStatistics(), actualResult.getStatistics());
        }
        validateDocuments(expectedResult.getDocuments(), list, testApi);

        // TODO error model
        // DocumentError error = new DocumentError().setId("4").setError("error");
        // List<DocumentError> errors = new ArrayList<>();
        // errors.add(error);
        // validateErrorDocuments(expected.getErrors(), detectLanguageResultList);
    }

    private static void validateBatchStatistics(TextBatchStatistics expectedStatistics,
                                                TextBatchStatistics actualStatistics) {
        assertEquals(expectedStatistics.getDocumentCount(), actualStatistics.getDocumentCount());
        assertEquals(expectedStatistics.getErroneousDocumentCount(), actualStatistics.getErroneousDocumentCount());
        assertEquals(expectedStatistics.getValidDocumentCount(), actualStatistics.getValidDocumentCount());
        assertEquals(expectedStatistics.getTransactionCount(), actualStatistics.getTransactionCount());
    }

    private static LanguageResult getExpectedBatchResult() {
        List<DetectedLanguage> detectedLanguageDoc1 = new ArrayList<>(Collections.singletonList(
            new DetectedLanguage().setName("English").setIso6391Name("en").setScore(1.0)));
        List<DetectedLanguage> detectedLanguageDoc2 = new ArrayList<>(Collections.singletonList(
            new DetectedLanguage().setName("Spanish").setIso6391Name("es").setScore(1.0)));
        List<DetectedLanguage> detectedLanguageDoc3 = new ArrayList<>(Collections.singletonList(
            new DetectedLanguage().setName("(Unknown)").setIso6391Name("(Unknown)").setScore(0.0)));

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics().setTransactionCount(1)
            .setCharacterCount(26);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics().setTransactionCount(1)
            .setCharacterCount(39);
        TextDocumentStatistics textDocumentStatistics3 = new TextDocumentStatistics().setTransactionCount(1)
            .setCharacterCount(6);

        List<DocumentLanguage> documents = new ArrayList<>();
        documents.add(new DocumentLanguage().setId("0").setDetectedLanguages(detectedLanguageDoc1)
            .setStatistics(textDocumentStatistics1));
        documents.add(new DocumentLanguage().setId("1").setDetectedLanguages(detectedLanguageDoc2)
            .setStatistics(textDocumentStatistics2));
        documents.add(new DocumentLanguage().setId("2").setDetectedLanguages(detectedLanguageDoc3)
            .setStatistics(textDocumentStatistics3));

        TextBatchStatistics batchStatistics = new TextBatchStatistics().setDocumentCount(3)
            .setErroneousDocumentCount(0).setValidDocumentCount(3).setTransactionCount(3);
        return new LanguageResult().setDocuments(documents).setModelVersion("2019-10-01").setStatistics(batchStatistics);
    }

    static void validateErrorDocuments(List<DocumentError> errors, List<DetectLanguageResult> detectLanguageResultList) {
        for (DocumentError expectedErrorDocument : errors) {
            DetectLanguageResult actualErrorDocument = detectLanguageResultList.stream().
                filter(document -> document.getId().equals(expectedErrorDocument.getId())).findFirst().get();
            assertNotNull(actualErrorDocument);
            assertEquals(expectedErrorDocument.getId(), actualErrorDocument.getId());
            // TODO: Need to fix the error model
            assertEquals(expectedErrorDocument.getError().toString(), actualErrorDocument.getError().toString());
        }

    }

    /**
     * Helper method to verify documents returned in a batch request.
     *
     * @param expected List DocumentLanguages contained in the RestResponse body.
     * @param actual List DetectLanguageResult returned by the API.
     * @param testApi the API to test.
     */
    // TODO: need to make this function generic
    <T> void validateDocuments(List<DocumentLanguage> expected, List<T> actual, String testApi) {
        switch (testApi) {
            case "Language":
                List<DetectLanguageResult> actualLanguageList = (List<DetectLanguageResult>) actual;
                for (DocumentLanguage expectedDocumentLanguage : expected) {
                    DetectLanguageResult actualResult = actualLanguageList.stream().filter(actualDoc ->
                        expectedDocumentLanguage.getId().equals(actualDoc.getId())).findFirst().get();
                    assertNotNull(actualResult);
                    if (actualResult.getError() == null && this.showStatistics) {
                        validateDocumentStatistics(expectedDocumentLanguage.getStatistics(), actualResult.getStatistics());
                        validateDetectedLanguages(expectedDocumentLanguage.getDetectedLanguages(),
                            actualResult.getDetectedLanguages());
                    }
                }
        }
    }

    /**
     * Helper method to verify TextDocumentStatistics match with expected.
     *
     * @param expected the expected value for TextDocumentStatistics.
     * @param actual the value returned by API.
     */
    private static void validateDocumentStatistics(TextDocumentStatistics expected, TextDocumentStatistics actual) {
        assertEquals(expected.getCharacterCount(), actual.getCharacterCount());
        assertEquals(expected.getTransactionCount(), actual.getTransactionCount());
    }

    /**
     * Helper method to validate the detected languages returned with the expected language list.
     *
     * @param expectedLanguageList detectedLanguages returned by the service.
     * @param actualLanguageList detectedLanguages returned by the API.
     */
    static void validateDetectedLanguages(List<DetectedLanguage> expectedLanguageList,
                                          List<DetectedLanguage> actualLanguageList) {
        for (int i = 0; i < expectedLanguageList.size(); i++) {
            DetectedLanguage expectedDetectedLanguage = expectedLanguageList.get(i);
            DetectedLanguage actualDetectedLanguage = actualLanguageList.get(i);
            assertEquals(expectedDetectedLanguage.getIso6391Name(), actualDetectedLanguage.getIso6391Name());
            assertEquals(expectedDetectedLanguage.getName(), actualDetectedLanguage.getName());
            assertEquals(expectedDetectedLanguage.getScore(), actualDetectedLanguage.getScore());
        }
    }
}
