// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.http.HttpClientHelper;
import com.azure.ai.agents.implementation.models.AgentDefinitionOptInKeys;
import com.azure.ai.agents.implementation.models.FoundryFeaturesOptInKeys;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.HttpResponseException;
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
import com.openai.client.OpenAIClientAsync;
import com.openai.core.ClientOptions;
import com.openai.credential.BearerTokenCredential;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FoundryFeaturesHeaderVerificationTest {
    @Test
    public void asyncAuthenticationPreservesLazyCredentialsAndRetryCount() {
        RecordingHttpClient transport = new RecordingHttpClient(request -> new MockHttpResponse(request, 500,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
            "{}".getBytes(StandardCharsets.UTF_8)));
        com.openai.core.http.HttpClient custom
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(transport).build());
        AgentsClientBuilder builder = createBuilder(transport);
        OpenAIClientAsync client = builder.buildOpenAIAsyncClient(options -> options.httpClient(custom).maxRetries(1));
        assertThrows(CompletionException.class, () -> client.models().list().join());
        assertEquals(2, transport.requests.size());
        AtomicInteger calls = new AtomicInteger();
        OpenAIClientAsync overridden = builder.buildOpenAIAsyncClient(
            options -> options.httpClient(custom).maxRetries(0).credential(BearerTokenCredential.create(() -> {
                calls.incrementAndGet();
                return "custom-token";
            })));
        assertEquals(0, calls.get());
        assertThrows(CompletionException.class, () -> overridden.models().list().join());
        assertTrue(calls.get() > 0);
        assertEquals("Bearer custom-token",
            transport.getLastRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void asyncOpenAIAuthenticationNeverRequestsSynchronousTokens() {
        RecordingHttpClient transport = newOpenAIRecordingHttpClient();
        AtomicInteger requests = new AtomicInteger();
        TokenCredential credential = new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext context) {
                assertEquals(Collections.singletonList("https://ai.azure.com/.default"), context.getScopes());
                return Mono.defer(() -> {
                    requests.incrementAndGet();
                    return Mono.just(new AccessToken("async-token", OffsetDateTime.now().plusHours(1)));
                });
            }

            @Override
            public AccessToken getTokenSync(TokenRequestContext context) {
                throw new AssertionError("Async authentication must not call getTokenSync");
            }
        };
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost/projects/test")
            .credential(credential)
            .httpClient(transport);
        builder.buildOpenAIAsyncClient().models().list().join();
        builder.buildAgentScopedOpenAIAsyncClient("agent").models().list().join();
        com.openai.core.http.HttpClient custom
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(transport).build());
        builder.buildOpenAIAsyncClient(options -> options.httpClient(custom)).models().list().join();
        builder.buildAgentScopedOpenAIAsyncClient("agent", options -> options.httpClient(custom))
            .models()
            .list()
            .join();
        builder.buildResponsesAsyncClient()
            .createResponseWithResponse(BinaryData.fromString("{\"model\":\"gpt-4o\",\"input\":\"hi\"}"), null)
            .block(Duration.ofSeconds(5));
        assertEquals(5, requests.get());
        assertEquals("Bearer async-token",
            transport.getLastRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        builder.buildOpenAIAsyncClient(options -> options.apiKey("override").httpClient(custom)).models().list().join();
        assertEquals(5, requests.get());
        assertEquals("Bearer override", transport.getLastRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void asyncAuthenticationWaitsWithoutBlockingAndDoesNotSendOnFailure() {
        Sinks.One<AccessToken> pending = Sinks.one();
        RecordingHttpClient transport = newOpenAIRecordingHttpClient();
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost/projects/test")
            .httpClient(transport)
            .credential(context -> pending.asMono());
        OpenAIClientAsync client = builder.buildOpenAIAsyncClient();
        CompletableFuture<?> result = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> client.models().list());
        assertFalse(result.isDone());
        assertTrue(transport.requests.isEmpty());
        pending.tryEmitValue(new AccessToken("delayed", OffsetDateTime.now().plusHours(1)));
        result.join();
        assertEquals("Bearer delayed", transport.getLastRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        int sent = transport.requests.size();
        for (Mono<AccessToken> failure : Arrays
            .asList(Mono.<AccessToken>error(new IllegalStateException("token failed")), Mono.<AccessToken>empty())) {
            OpenAIClientAsync failingClient = builder.credential(context -> failure).buildOpenAIAsyncClient();
            assertThrows(CompletionException.class, () -> failingClient.models().list().join());
            assertEquals(sent, transport.requests.size());
        }
    }

    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");
    private static final HttpHeaderName CUSTOM_PIPELINE_HEADER = HttpHeaderName.fromString("X-Custom-Pipeline");
    private static final String CUSTOM_PIPELINE_VALUE = "custom-pipeline";
    private static final String AGENT_PREVIEW_FEATURES
        = Stream
            .concat(Arrays.stream(AgentDefinitionOptInKeys.values()).map(AgentDefinitionOptInKeys::toString),
                Stream.of(FoundryFeaturesOptInKeys.AGENTS_OPTIMIZATION_V2_PREVIEW.toString(),
                    FoundryFeaturesOptInKeys.MODEL_ROUTER_CONTROLS_V1_PREVIEW.toString()))
            .collect(Collectors.joining(","));

    @Test
    public void allowPreviewAddsAreaSpecificHeaders() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(httpClient).allowPreview(true);

        builder.beta().buildBetaAgentsClient().getOptimizationJobWithResponse("job", new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));

        builder.beta().buildBetaAgentsClient().getOptimizationJobWithResponse("job", new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));

        builder.beta().buildBetaMemoryStoresClient().getMemoryStoreWithResponse("store", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.MEMORY_STORES_V1_PREVIEW.toString(), foundryFeatures(httpClient));

        builder.buildAgentsClient()
            .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));
    }

    @Test
    public void betaClientsAddAreaSpecificHeadersByDefault() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(httpClient);

        builder.beta().buildBetaAgentsClient().getOptimizationJobWithResponse("job", new RequestOptions());
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));

        builder.beta().buildBetaMemoryStoresClient().getMemoryStoreWithResponse("store", new RequestOptions());
        assertEquals(FoundryFeaturesOptInKeys.MEMORY_STORES_V1_PREVIEW.toString(), foundryFeatures(httpClient));
    }

    @Test
    public void betaHeaderDoesNotLeakToGaClientBuiltFromSameBuilder() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(httpClient);

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
        String explicitHeader = FoundryFeaturesOptInKeys.AGENTS_OPTIMIZATION_V2_PREVIEW.toString();
        RequestOptions requestOptions = new RequestOptions().setHeader(FOUNDRY_FEATURES, explicitHeader);

        createBuilder(httpClient).allowPreview(true)
            .beta()
            .buildBetaAgentsClient()
            .getOptimizationJobWithResponse("job", requestOptions);

        assertEquals(explicitHeader, foundryFeatures(httpClient));
    }

    @Test
    public void allowPreviewPreservesExplicitEmptyHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        RequestOptions options = new RequestOptions().setHeader(HttpHeaderName.fromString("foundry-features"), "");

        createBuilder(httpClient).allowPreview(true)
            .buildAgentsClient()
            .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), options);

        assertEquals("", foundryFeatures(httpClient));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void previewRequiredErrorIncludesGuidanceAndPreservesResponse(boolean async) {
        String body = "{\"error\":{\"code\":\"preview_feature_required\",\"message\":\"Voice preview required\","
            + "\"details\":[{\"code\":\"detail\",\"message\":\"Service detail\"}]}}";
        RecordingHttpClient httpClient = errorClient(403, body);
        AgentsClientBuilder builder = createBuilder(createCustomPipeline(httpClient));

        HttpResponseException exception = createVersionError(builder, async);

        assertTrue(exception.getMessage().contains("AgentsClientBuilder.allowPreview(true)"));
        assertTrue(exception.getMessage().contains("Voice preview required"));
        assertEquals(403, exception.getResponse().getStatusCode());
        assertSame(httpClient.getLastRequest(), exception.getResponse().getRequest());
        assertEquals("request-id",
            exception.getResponse().getHeaderValue(HttpHeaderName.fromString("x-ms-request-id")));
        assertEquals(body, exception.getResponse().getBodyAsString().block());
        Map<?, ?> error = (Map<?, ?>) ((Map<?, ?>) exception.getValue()).get("error");
        assertEquals("preview_feature_required", error.get("code"));
        assertEquals(1, ((List<?>) error.get("details")).size());
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void previewEnabledDoesNotAddErrorGuidance(boolean async) {
        RecordingHttpClient httpClient
            = errorClient(403, "{\"error\":{\"code\":\"preview_feature_required\",\"message\":\"Preview required\"}}");

        HttpResponseException exception = createVersionError(createBuilder(httpClient).allowPreview(true), async);

        assertFalse(exception.getMessage().contains("AgentsClientBuilder.allowPreview(true)"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void unrelatedErrorsDoNotAddPreviewGuidance(boolean async) {
        for (String body : new String[] {
            "{\"error\":{\"code\":\"forbidden\",\"message\":\"Access denied\"}}",
            "not json",
            "",
            "null",
            "[]" }) {
            HttpResponseException exception = createVersionError(createBuilder(errorClient(403, body)), async);
            assertFalse(exception.getMessage().contains("AgentsClientBuilder.allowPreview(true)"));
            assertEquals(403, exception.getResponse().getStatusCode());
            assertEquals(body, exception.getResponse().getBodyAsString().block());
        }
        HttpResponseException exception = createVersionError(
            createBuilder(
                errorClient(400, "{\"error\":{\"code\":\"preview_feature_required\",\"message\":\"Bad request\"}}")),
            async);
        assertFalse(exception.getMessage().contains("AgentsClientBuilder.allowPreview(true)"));
        assertEquals(400, exception.getResponse().getStatusCode());
    }

    private static RecordingHttpClient errorClient(int status, String body) {
        return new RecordingHttpClient(request -> new MockHttpResponse(request, status,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json")
                .set(HttpHeaderName.fromString("x-ms-request-id"), "request-id"),
            body.getBytes(StandardCharsets.UTF_8)));
    }

    private static HttpResponseException createVersionError(AgentsClientBuilder builder, boolean async) {
        return assertThrows(HttpResponseException.class, () -> {
            if (async) {
                builder.buildAgentsAsyncClient()
                    .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions())
                    .block();
            } else {
                builder.buildAgentsClient()
                    .createAgentVersionWithResponse("agent", BinaryData.fromString("{}"), new RequestOptions());
            }
        });
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
            .getOptimizationJobWithResponse("job", new RequestOptions());

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
        String explicitHeader = FoundryFeaturesOptInKeys.AGENTS_OPTIMIZATION_V2_PREVIEW.toString();
        RequestOptions requestOptions = new RequestOptions().setHeader(FOUNDRY_FEATURES, explicitHeader);

        createBuilder(createCustomPipeline(httpClient)).beta()
            .buildBetaAgentsClient()
            .getOptimizationJobWithResponse("job", requestOptions);

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
    public void agentScopedOpenAIClientUsesCustomPipelineAndPreviewHeaderByDefault() {
        RecordingHttpClient httpClient = newOpenAIRecordingHttpClient();
        HttpPipeline customPipeline = createCustomPipeline(httpClient);

        createBuilder(customPipeline).buildAgentScopedOpenAIClient("agent").models().list();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));
        assertEquals("/api/projects/project/agents/agent/endpoint/protocols/openai/models",
            httpClient.getLastRequest().getUrl().getPath());
        assertEquals("api-version=v1", httpClient.getLastRequest().getUrl().getQuery());

        createBuilder(customPipeline).allowPreview(true).buildAgentScopedOpenAIClient("agent").models().list();
        assertEquals(CUSTOM_PIPELINE_VALUE, customPipelineHeader(httpClient));
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(httpClient));
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
            AgentsClientBuilder builder
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

    @Test
    public void realtimeHandshakeOverridesPreserveSecurityAndDefaults() {
        com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration configuration
            = new com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration(
                java.net.URI.create("https://localhost/api/projects/project"), new MockTokenCredential(), "v1",
                "test-sdk", new HttpHeaders().set("X-Custom", "builder"), null);
        com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions options
            = new com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions()
                .setConnectionUrl(java.net.URI.create("wss://localhost/custom?sig=a%2Bb"))
                .setAgentSessionId("session id")
                .setApiVersion("preview")
                .setStructuredInputs("{\"language\":\"en\"}")
                .setCredentialScopes(Collections.singletonList("custom-scope"))
                .setExtraQuery(Collections.singletonMap("api-version", "override"));
        Map<String, String> headers = new java.util.LinkedHashMap<>();
        headers.put("foundry-features", "");
        headers.put("user-agent", "custom-agent");
        headers.put("Authorization", "must-not-be-used");
        options.setExtraHeaders(headers);
        java.net.URI uri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent", options);
        assertEquals("/custom", uri.getPath());
        assertTrue(uri.getRawQuery().contains("sig=a%2Bb"));
        assertTrue(uri.getRawQuery().contains("api-version=override"));
        assertTrue(uri.getRawQuery().contains("agent_session_id=session%20id"));
        HttpHeaders actual = VoiceAgentWebSocketUtils.buildHeaders(configuration, options, "test-token");
        assertEquals("", actual.getValue(FOUNDRY_FEATURES));
        assertEquals("custom-agent", actual.getValue(HttpHeaderName.USER_AGENT));
        assertEquals("Bearer test-token", actual.getValue(HttpHeaderName.AUTHORIZATION));
        assertEquals("builder", actual.getValue("X-Custom"));
        assertEquals(options.getStructuredInputs(), actual.getValue("x-ms-voice-structured-inputs"));
        assertEquals(Collections.singletonList("custom-scope"),
            VoiceAgentWebSocketUtils.createTokenRequestContext(options).getScopes());
        for (String unsafe : new String[] {
            "wss://other.example/custom",
            "ws://localhost/custom",
            "wss://localhost:444/custom",
            "wss://user@localhost/custom",
            "wss://localhost/custom#fragment" }) {
            options.setConnectionUrl(java.net.URI.create(unsafe));
            assertThrows(IllegalArgumentException.class,
                () -> VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent", options));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void openAIOverridesPreserveCredentialsHeadersAndQuery(boolean async) {
        RecordingHttpClient httpClient = newOpenAIRecordingHttpClient();
        AgentsClientBuilder builder = createBuilder(httpClient);
        java.util.function.Consumer<ClientOptions.Builder> configure
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

    private static AgentsClientBuilder createBuilder(RecordingHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint("https://localhost:8080/api/projects/project")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void customOpenAITransportRetainsAuthenticationAndAgentDefaults(boolean async) {
        RecordingHttpClient customTransport = newOpenAIRecordingHttpClient();
        AtomicInteger tokenRequests = new AtomicInteger();
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost/api/projects/project")
            .clientOptions(new com.azure.core.util.ClientOptions().setApplicationId("review-app"))
            .httpClient(request -> Mono.error(new AssertionError("Default transport must not be used")))
            .credential(context -> {
                assertEquals(Collections.singletonList("https://ai.azure.com/.default"), context.getScopes());
                tokenRequests.incrementAndGet();
                return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
            });
        com.openai.core.http.HttpClient transport
            = HttpClientHelper.mapToOpenAIHttpClient(new HttpPipelineBuilder().httpClient(customTransport).build());
        if (async) {
            builder.buildAgentScopedOpenAIAsyncClient("agent", options -> options.httpClient(transport))
                .models()
                .list()
                .join();
        } else {
            builder.buildAgentScopedOpenAIClient("agent", options -> options.httpClient(transport)).models().list();
        }
        assertEquals(AGENT_PREVIEW_FEATURES, foundryFeatures(customTransport));
        assertTrue(customTransport.getLastRequest()
            .getHeaders()
            .getValue(HttpHeaderName.USER_AGENT)
            .startsWith("review-app azsdk-java-azure-ai-agents/"));
        assertEquals("api-version=v1", customTransport.getLastRequest().getUrl().getQuery());
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
