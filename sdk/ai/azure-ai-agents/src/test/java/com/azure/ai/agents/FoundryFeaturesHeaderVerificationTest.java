// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDefinitionOptInKeys;
import com.azure.ai.agents.models.FoundryFeaturesOptInKeys;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FoundryFeaturesHeaderVerificationTest {
    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");
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
    public void allowPreviewFalseDoesNotAddAgentHeader() {
        RecordingHttpClient httpClient = new RecordingHttpClient();

        createBuilder(httpClient).allowPreview(false)
            .beta()
            .buildBetaAgentsClient()
            .getSessionWithResponse("agent", "session", new RequestOptions());

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

    private static AgentsClientBuilder createBuilder(RecordingHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint("https://localhost:8080/api/projects/project")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static String foundryFeatures(RecordingHttpClient httpClient) {
        return httpClient.getLastRequest().getHeaders().getValue(FOUNDRY_FEATURES);
    }

    private static final class RecordingHttpClient implements HttpClient {
        private final List<HttpRequest> requests = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.requests.add(request);
            HttpHeaders responseHeaders
                = new HttpHeaders().set(HttpHeaderName.fromString("Content-Type"), "application/json");
            return Mono
                .just(new MockHttpResponse(request, 200, responseHeaders, "{}".getBytes(StandardCharsets.UTF_8)));
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
