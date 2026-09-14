// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FoundryFeaturesHeaderVerificationTest {
    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");
    private static final HttpHeaderName CUSTOM_PIPELINE_HEADER = HttpHeaderName.fromString("X-Custom-Pipeline");
    private static final String CUSTOM_PIPELINE_VALUE = "custom-pipeline";

    @Test
    public void allowPreviewAddsAreaSpecificHeaders() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AIProjectClientBuilder builder = createBuilder(httpClient).allowPreview(true);

        builder.beta().buildBetaModelsClient().getModelVersionWithResponse("model", "1", new RequestOptions());
        assertEquals("Models=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaRedTeamsClient().getRedTeamWithResponse("red-team", new RequestOptions());
        assertEquals("RedTeams=V1Preview", foundryFeatures(httpClient));

        builder.beta()
            .buildBetaEvaluationTaxonomiesClient()
            .getEvaluationTaxonomyWithResponse("taxonomy", new RequestOptions());
        assertEquals("Evaluations=V1Preview", foundryFeatures(httpClient));

        builder.beta()
            .buildBetaEvaluatorsClient()
            .getEvaluatorVersionWithResponse("evaluator", "1", new RequestOptions());
        assertEquals("Evaluations=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaInsightsClient().getInsightWithResponse("insight", new RequestOptions());
        assertEquals("Insights=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaSchedulesClient().getScheduleWithResponse("schedule", new RequestOptions());
        assertEquals("Schedules=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaRoutinesClient().getRoutineWithResponse("routine", new RequestOptions());
        assertEquals("Routines=V2Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaSkillsClient().getSkillWithResponse("skill", new RequestOptions());
        assertEquals("Skills=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaDatasetsClient().getGenerationJobWithResponse("job", new RequestOptions());
        assertEquals("DataGenerationJobs=V1Preview", foundryFeatures(httpClient));

        builder.buildEvaluationRulesClient()
            .createOrUpdateEvaluationRuleWithResponse("rule", BinaryData.fromString("{}"), new RequestOptions());
        assertEquals("Evaluations=V1Preview", foundryFeatures(httpClient));
    }

    @Test
    public void betaClientsAddAreaSpecificHeadersByDefault() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AIProjectClientBuilder builder = createBuilder(httpClient);

        builder.beta().buildBetaModelsClient().getModelVersionWithResponse("model", "1", new RequestOptions());
        assertEquals("Models=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaRedTeamsClient().getRedTeamWithResponse("red-team", new RequestOptions());
        assertEquals("RedTeams=V1Preview", foundryFeatures(httpClient));

        builder.beta()
            .buildBetaEvaluationTaxonomiesClient()
            .getEvaluationTaxonomyWithResponse("taxonomy", new RequestOptions());
        assertEquals("Evaluations=V1Preview", foundryFeatures(httpClient));

        builder.beta()
            .buildBetaEvaluatorsClient()
            .getEvaluatorVersionWithResponse("evaluator", "1", new RequestOptions());
        assertEquals("Evaluations=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaInsightsClient().getInsightWithResponse("insight", new RequestOptions());
        assertEquals("Insights=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaSchedulesClient().getScheduleWithResponse("schedule", new RequestOptions());
        assertEquals("Schedules=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaRoutinesClient().getRoutineWithResponse("routine", new RequestOptions());
        assertEquals("Routines=V2Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaSkillsClient().getSkillWithResponse("skill", new RequestOptions());
        assertEquals("Skills=V1Preview", foundryFeatures(httpClient));

        builder.beta().buildBetaDatasetsClient().getGenerationJobWithResponse("job", new RequestOptions());
        assertEquals("DataGenerationJobs=V1Preview", foundryFeatures(httpClient));

        builder.beta()
            .buildBetaAgentInsightMonitorsClient()
            .getAgentInsightMonitorWithResponse("monitor", new RequestOptions());
        assertEquals("AgentInsights=V1Preview", foundryFeatures(httpClient));
    }

    @Test
    public void betaHeaderDoesNotLeakToGaClientBuiltFromSameBuilder() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AIProjectClientBuilder builder = createBuilder(httpClient);

        builder.beta().buildBetaDatasetsClient().getGenerationJobWithResponse("job", new RequestOptions());
        assertEquals("DataGenerationJobs=V1Preview", foundryFeatures(httpClient));

        // Beta clients temporarily add their required Foundry-Features policy while their pipeline is being built.
        // The policy must not remain on the reusable builder, otherwise a later non-beta client built from the same
        // builder would silently inherit a beta opt-in header despite allowPreview defaulting to false for GA clients.
        builder.buildEvaluationRulesClient()
            .createOrUpdateEvaluationRuleWithResponse("rule", BinaryData.fromString("{}"), new RequestOptions());
        assertNull(foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewDoesNotOverrideExplicitHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        String explicitHeader = "Insights=V1Preview";
        RequestOptions requestOptions = new RequestOptions().setHeader(FOUNDRY_FEATURES, explicitHeader);

        createBuilder(httpClient).allowPreview(true)
            .beta()
            .buildBetaDatasetsClient()
            .getGenerationJobWithResponse("job", requestOptions);

        assertEquals(explicitHeader, foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewFalseDoesNotAddGaHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();

        createBuilder(httpClient).allowPreview(false)
            .buildEvaluationRulesClient()
            .createOrUpdateEvaluationRuleWithResponse("rule", BinaryData.fromString("{}"), new RequestOptions());

        assertNull(foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewUsesBuiltClientFeatureHeaderWithoutPathMatching() {
        RecordingHttpClient httpClient = new RecordingHttpClient();

        createBuilder(httpClient).endpoint("https://localhost:8080/api/projects/project/evaluations/evaluation")
            .beta()
            .buildBetaDatasetsClient()
            .getGenerationJobWithResponse("job", new RequestOptions());

        assertEquals("DataGenerationJobs=V1Preview", foundryFeatures(httpClient));
    }

    @Test
    public void betaClientWithCustomPipelineAddsFoundryHeaderAndPreservesPipeline() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        HttpPipeline customPipeline = createCustomPipeline(httpClient);
        int originalPolicyCount = customPipeline.getPolicyCount();

        createBuilder(customPipeline).beta()
            .buildBetaModelsClient()
            .getModelVersionWithResponse("model", "1", new RequestOptions());

        assertEquals("Models=V1Preview", foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertEquals(originalPolicyCount, customPipeline.getPolicyCount());
    }

    @Test
    public void customPipelineDoesNotAddEvaluationRuleHeaderUnlessAllowPreview() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        HttpPipeline customPipeline = createCustomPipeline(httpClient);

        createBuilder(customPipeline).buildEvaluationRulesClient()
            .createOrUpdateEvaluationRuleWithResponse("rule", BinaryData.fromString("{}"), new RequestOptions());

        assertNull(foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));

        createBuilder(customPipeline).allowPreview(true)
            .buildEvaluationRulesClient()
            .createOrUpdateEvaluationRuleWithResponse("rule", BinaryData.fromString("{}"), new RequestOptions());

        assertEquals("Evaluations=V1Preview", foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
    }

    @Test
    public void customPipelineDoesNotOverrideExplicitFoundryHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        String explicitHeader = "Insights=V1Preview";
        RequestOptions requestOptions = new RequestOptions().setHeader(FOUNDRY_FEATURES, explicitHeader);

        createBuilder(createCustomPipeline(httpClient)).beta()
            .buildBetaDatasetsClient()
            .getGenerationJobWithResponse("job", requestOptions);

        assertEquals(explicitHeader, foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
    }

    @Test
    public void openAIClientsUseCustomPipeline() {
        RecordingHttpClient httpClient = newOpenAIRecordingHttpClient();
        AIProjectClientBuilder builder = createBuilder(createCustomPipeline(httpClient));

        builder.buildOpenAIClient().models().list();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertNull(foundryFeatures(httpClient));

        builder.buildAgentScopedOpenAIClient("agent").models().list();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertEquals(
            "WorkflowAgents=V1Preview,ExternalAgents=V1Preview,VoiceAgents=V1Preview,"
                + "DraftAgents=V1Preview,AgentsOptimization=V2Preview,ModelRouterControls=V1Preview",
            foundryFeatures(httpClient));
        assertEquals("/api/projects/project/agents/agent/endpoint/protocols/openai/models",
            httpClient.getLastRequest().getUrl().getPath());
        assertEquals("api-version=v1", httpClient.getLastRequest().getUrl().getQuery());

        builder.buildAgentScopedOpenAIAsyncClient("agent").models().list().join();
        assertEquals("api-version=v1", httpClient.getLastRequest().getUrl().getQuery());
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
    }

    private static RecordingHttpClient newOpenAIRecordingHttpClient() {
        return new RecordingHttpClient(FoundryFeaturesHeaderVerificationTest::openAIResponse);
    }

    @Test
    public void explicitLogOptionsOverrideConsoleLoggingDefault() throws java.io.IOException {
        for (boolean enabled : new boolean[] { false, true }) {
            RecordingHttpClient httpClient = new RecordingHttpClient(request -> new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream; charset=utf-8"),
                "data: test\n\n".getBytes(StandardCharsets.UTF_8)));
            AIProjectClientBuilder builder
                = createBuilder(httpClient).configuration(com.azure.core.util.Configuration.getGlobalConfiguration()
                    .clone()
                    .put("AZURE_AI_PROJECTS_CONSOLE_LOGGING", "true"));
            if (!enabled) {
                builder.httpLogOptions(new com.azure.core.http.policy.HttpLogOptions()
                    .setLogLevel(com.azure.core.http.policy.HttpLogDetailLevel.NONE));
            }
            java.util.concurrent.atomic.AtomicReference<com.openai.core.http.HttpClient> transport
                = new java.util.concurrent.atomic.AtomicReference<>();
            builder.buildOpenAIClient(options -> transport.set(options.build().httpClient()));
            com.openai.core.http.HttpRequest request = com.openai.core.http.HttpRequest.builder()
                .method(com.openai.core.http.HttpMethod.GET)
                .baseUrl("https://localhost/stream")
                .build();
            try (com.openai.core.http.HttpResponse response = transport.get().execute(request);
                java.io.InputStream body = response.body()) {
                assertEquals(enabled, body instanceof java.io.FilterInputStream);
                assertEquals('d', body.read());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void openAIOverridesPreserveCredentialsHeadersAndQuery(boolean async) {
        RecordingHttpClient httpClient = newOpenAIRecordingHttpClient();
        AIProjectClientBuilder builder = createBuilder(httpClient);
        java.util.function.Consumer<com.openai.core.ClientOptions.Builder> configure
            = options -> options.baseUrl("https://localhost:8080/custom/openai")
                .apiKey("test-api-key")
                .replaceHeaders("User-Agent", "review-client/1.0")
                .replaceHeaders("foundry-features", "")
                .replaceQueryParams("api-version", "test-version");
        if (async) {
            builder.buildAgentScopedOpenAIAsyncClient("agent", configure).models().list().join();
        } else {
            builder.buildAgentScopedOpenAIClient("agent", configure).models().list();
        }
        assertEquals("/custom/openai/models", httpClient.getLastRequest().getUrl().getPath());
        assertEquals("api-version=test-version", httpClient.getLastRequest().getUrl().getQuery());
        assertEquals("Bearer test-api-key",
            httpClient.getLastRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        assertEquals("", foundryFeatures(httpClient));
        assertEquals("review-client/1.0", httpClient.getLastRequest().getHeaders().getValue(HttpHeaderName.USER_AGENT));
    }

    private static AIProjectClientBuilder createBuilder(RecordingHttpClient httpClient) {
        return new AIProjectClientBuilder().endpoint("https://localhost:8080/api/projects/project")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AIProjectsServiceVersion.V1);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void customOpenAITransportRetainsAuthenticationAndAgentDefaults(boolean async) {
        RecordingHttpClient customTransport = newOpenAIRecordingHttpClient();
        java.util.concurrent.atomic.AtomicInteger tokenRequests = new java.util.concurrent.atomic.AtomicInteger();
        AIProjectClientBuilder builder = new AIProjectClientBuilder().endpoint("https://localhost/api/projects/project")
            .clientOptions(new com.azure.core.util.ClientOptions().setApplicationId("review-app"))
            .httpClient(request -> Mono.error(new AssertionError("Default transport must not be used")))
            .credential(context -> {
                assertEquals(java.util.Collections.singletonList("https://ai.azure.com/.default"), context.getScopes());
                tokenRequests.incrementAndGet();
                return Mono.just(new com.azure.core.credential.AccessToken("test-token",
                    java.time.OffsetDateTime.now().plusHours(1)));
            });
        com.openai.core.http.HttpClient transport = com.azure.ai.projects.implementation.http.HttpClientHelper
            .mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(customTransport).build());
        if (async) {
            builder.buildAgentScopedOpenAIAsyncClient("agent", options -> options.httpClient(transport))
                .models()
                .list()
                .join();
        } else {
            builder.buildAgentScopedOpenAIClient("agent", options -> options.httpClient(transport)).models().list();
        }
        assertEquals(
            "WorkflowAgents=V1Preview,ExternalAgents=V1Preview,VoiceAgents=V1Preview,"
                + "DraftAgents=V1Preview,AgentsOptimization=V2Preview,ModelRouterControls=V1Preview",
            foundryFeatures(customTransport));
        assertEquals("api-version=v1", customTransport.getLastRequest().getUrl().getQuery());
        assertTrue(customTransport.getLastRequest()
            .getHeaders()
            .getValue(HttpHeaderName.USER_AGENT)
            .startsWith("review-app azsdk-java-azure-ai-projects/"));
        assertEquals("Bearer test-token",
            customTransport.getLastRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        int initialTokenRequests = tokenRequests.get();
        assertTrue(initialTokenRequests > 0);
        if (async) {
            builder.buildOpenAIAsyncClient(options -> options.httpClient(transport)).models().list().join();
        } else {
            builder.buildOpenAIClient(options -> options.httpClient(transport)).models().list();
        }
        assertNull(foundryFeatures(customTransport));
        assertNull(customTransport.getLastRequest().getUrl().getQuery());
        assertEquals("/api/projects/project/openai/v1/models", customTransport.getLastRequest().getUrl().getPath());
        assertTrue(tokenRequests.get() > initialTokenRequests);
    }

    private static AIProjectClientBuilder createBuilder(HttpPipeline pipeline) {
        return new AIProjectClientBuilder().endpoint("https://localhost:8080/api/projects/project")
            .credential(new MockTokenCredential())
            .pipeline(pipeline)
            .serviceVersion(AIProjectsServiceVersion.V1);
    }

    private static HttpPipeline createCustomPipeline(RecordingHttpClient httpClient) {
        return new HttpPipelineBuilder().httpClient(httpClient).policies(new CustomPipelinePolicy()).build();
    }

    private static String foundryFeatures(RecordingHttpClient httpClient) {
        return httpClient.getLastRequest().getHeaders().getValue(FOUNDRY_FEATURES);
    }

    private static String customPipelineHeader(RecordingHttpClient httpClient) {
        return httpClient.getLastRequest().getHeaders().getValue(CUSTOM_PIPELINE_HEADER);
    }

    private static HttpResponse openAIResponse(HttpRequest request) {
        String path = request.getUrl().getPath();
        String responseBody = path.endsWith("/models") ? "{\"data\":[],\"object\":\"list\"}" : "{}";
        return jsonResponse(request, responseBody);
    }

    private static HttpResponse jsonResponse(HttpRequest request, String responseBody) {
        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.fromString("Content-Type"), "application/json");
        return new MockHttpResponse(request, 200, responseHeaders, responseBody.getBytes(StandardCharsets.UTF_8));
    }

    private static final class CustomPipelinePolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().getHeaders().set(CUSTOM_PIPELINE_HEADER, CUSTOM_PIPELINE_VALUE);
            return next.process();
        }
    }

    private static final class RecordingHttpClient implements HttpClient {
        private final List<HttpRequest> requests = new ArrayList<>();
        private final Function<HttpRequest, HttpResponse> responseFactory;

        private RecordingHttpClient() {
            this(request -> jsonResponse(request, "{}"));
        }

        private RecordingHttpClient(Function<HttpRequest, HttpResponse> responseFactory) {
            this.responseFactory = responseFactory;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.requests.add(request);
            return Mono.just(responseFactory.apply(request));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return send(request);
        }

        HttpRequest getLastRequest() {
            return this.requests.get(this.requests.size() - 1);
        }
    }
}
