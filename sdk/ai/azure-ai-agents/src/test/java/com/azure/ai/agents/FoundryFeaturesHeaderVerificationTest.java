// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.models.AgentDefinitionOptInKeys;
import com.azure.ai.agents.implementation.models.FoundryFeaturesOptInKeys;
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
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FoundryFeaturesHeaderVerificationTest {
    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");
    private static final HttpHeaderName CUSTOM_PIPELINE_HEADER = HttpHeaderName.fromString("X-Custom-Pipeline");
    private static final String CUSTOM_PIPELINE_VALUE = "custom-pipeline";
    private static final String AGENT_PREVIEW_FEATURES = Stream
        .concat(Arrays.stream(AgentDefinitionOptInKeys.values()).map(AgentDefinitionOptInKeys::toString),
            Stream.of(FoundryFeaturesOptInKeys.AGENTS_OPTIMIZATION_V1_PREVIEW.toString()))
        .collect(Collectors.joining(","));

    @Test
    public void allowPreviewAddsAreaSpecificHeaders() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(httpClient).allowPreview(true);

        builder.beta().buildBetaAgentsClient().getSessionWithResponse("agent", "session", new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));

        builder.beta().buildBetaAgentsClient().getOptimizationJobWithResponse("job", new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));

        builder.beta().buildBetaMemoryStoresClient().getMemoryStoreWithResponse("store", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.MEMORY_STORES_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaToolboxesClient().getToolboxWithResponse("toolbox", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.TOOLBOXES_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.buildAgentsClient()
            .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));
    }

    @Test
    public void betaClientsAddAreaSpecificHeadersByDefault() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(httpClient);

        builder.beta().buildBetaAgentsClient().getSessionWithResponse("agent", "session", new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));

        builder.beta().buildBetaMemoryStoresClient().getMemoryStoreWithResponse("store", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.MEMORY_STORES_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.beta().buildBetaToolboxesClient().getToolboxWithResponse("toolbox", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.TOOLBOXES_V1_PREVIEW.toString(), foundryFeatures(httpClient));
    }

    @Test
    public void betaHeaderDoesNotLeakToGaClientBuiltFromSameBuilder() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(httpClient);

        builder.beta().buildBetaMemoryStoresClient().getMemoryStoreWithResponse("store", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.MEMORY_STORES_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        // Beta clients temporarily add their required Foundry-Features policy while their pipeline is being built.
        // The policy must not remain on the reusable builder, otherwise a later GA client built from the same builder
        // would silently inherit a beta opt-in header despite allowPreview defaulting to false for GA clients.
        builder.buildAgentsClient()
            .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions());
        assertNull(foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewDoesNotOverrideExplicitHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        String explicitHeader = FoundryFeaturesOptInKeys.AGENTS_OPTIMIZATION_V1_PREVIEW.toString();
        RequestOptions requestOptions = new RequestOptions().setHeader(FOUNDRY_FEATURES, explicitHeader);

        createBuilder(httpClient).allowPreview(true)
            .beta()
            .buildBetaAgentsClient()
            .getSessionWithResponse("agent", "session", requestOptions);

        assertEquals(explicitHeader, foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewFalseDoesNotAddGaAgentHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();

        createBuilder(httpClient).allowPreview(false)
            .buildAgentsClient()
            .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions());

        assertNull(foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewUsesBuiltClientFeatureHeaderWithoutPathMatching() {
        RecordingHttpClient httpClient = new RecordingHttpClient();

        createBuilder(httpClient).endpoint("https://localhost:8080/api/projects/project/evaluations/evaluation")
            .allowPreview(true)
            .beta()
            .buildBetaAgentsClient()
            .getSessionWithResponse("agent", "session", new RequestOptions());

        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));
    }

    @Test
    public void betaClientWithCustomPipelineAddsFoundryHeaderAndPreservesPipeline() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        HttpPipeline customPipeline = createCustomPipeline(httpClient);
        int originalPolicyCount = customPipeline.getPolicyCount();

        createBuilder(customPipeline).beta()
            .buildBetaMemoryStoresClient()
            .getMemoryStoreWithResponse("store", new RequestOptions());

        assertEquals(FoundryFeaturesOptInKeys.MEMORY_STORES_V1_PREVIEW.toString(), foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertEquals(originalPolicyCount, customPipeline.getPolicyCount());
    }

    @Test
    public void customPipelineDoesNotAddGaHeaderUnlessAllowPreview() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        HttpPipeline customPipeline = createCustomPipeline(httpClient);

        createBuilder(customPipeline).buildAgentsClient()
            .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions());

        assertNull(foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));

        createBuilder(customPipeline).allowPreview(true)
            .buildAgentsClient()
            .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions());

        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
    }

    @Test
    public void customPipelineDoesNotOverrideExplicitFoundryHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        String explicitHeader = FoundryFeaturesOptInKeys.AGENTS_OPTIMIZATION_V1_PREVIEW.toString();
        RequestOptions requestOptions = new RequestOptions().setHeader(FOUNDRY_FEATURES, explicitHeader);

        createBuilder(createCustomPipeline(httpClient)).beta()
            .buildBetaAgentsClient()
            .getSessionWithResponse("agent", "session", requestOptions);

        assertEquals(explicitHeader, foundryFeatures(httpClient));
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
    }

    @Test
    public void openAIAndResponsesClientsUseCustomPipeline() {
        RecordingHttpClient httpClient = newOpenAIRecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(createCustomPipeline(httpClient));

        builder.buildOpenAIClient().models().list();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertNull(foundryFeatures(httpClient));

        builder.buildResponsesClient()
            .createResponseWithResponse(BinaryData.fromString("{\"model\":\"gpt-4o\",\"input\":\"hi\"}"), null)
            .close();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertNull(foundryFeatures(httpClient));
    }

    @Test
    public void agentScopedOpenAIClientUsesCustomPipelineAndAllowPreviewHeader() {
        RecordingHttpClient httpClient = newOpenAIRecordingHttpClient();
        HttpPipeline customPipeline = createCustomPipeline(httpClient);

        createBuilder(customPipeline).buildAgentScopedOpenAIClient("agent").models().list();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertNull(foundryFeatures(httpClient));

        createBuilder(customPipeline).allowPreview(true).buildAgentScopedOpenAIClient("agent").models().list();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));
    }

    private static RecordingHttpClient newOpenAIRecordingHttpClient() {
        return new RecordingHttpClient(FoundryFeaturesHeaderVerificationTest::openAIResponse);
    }

    private static AgentsClientBuilder createBuilder(RecordingHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint("https://localhost:8080/api/projects/project")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static AgentsClientBuilder createBuilder(HttpPipeline pipeline) {
        return new AgentsClientBuilder().endpoint("https://localhost:8080/api/projects/project")
            .credential(new MockTokenCredential())
            .pipeline(pipeline)
            .serviceVersion(AgentsServiceVersion.V1);
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
