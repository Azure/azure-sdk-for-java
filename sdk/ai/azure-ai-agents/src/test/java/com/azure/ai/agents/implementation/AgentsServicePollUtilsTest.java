// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationJobResult;
import com.azure.ai.agents.models.MemoryStoreUpdateCompletedResult;
import com.azure.ai.agents.models.MemoryStoreUpdateResponse;
import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.TypeReference;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentsServicePollUtilsTest {

    static Stream<Arguments> memoryResultCases() {
        return Stream.of(false, true)
            .flatMap(async -> Stream
                .of("", ",\"result\":null", ",\"result\":{\"memory_operations\":[],\"usage\":{\"total_tokens\":17}}")
                .flatMap(result -> Stream.of("completed", "superseded")
                    .flatMap(
                        status -> Stream.of(false, true).map(resume -> Arguments.of(async, result, status, resume)))));
    }

    @ParameterizedTest
    @MethodSource("memoryResultCases")
    void memoryPollerHandlesEmptyResult(boolean async, String resultJson, String status, boolean resume) {
        HttpClient httpClient = request -> {
            if (resume) {
                assertEquals(HttpMethod.GET, request.getHttpMethod());
                assertTrue(request.getUrl().getPath().endsWith("/updates/update-123"));
            }
            boolean initial = request.getHttpMethod() == HttpMethod.POST;
            String body = initial
                ? "{\"update_id\":\"update-123\",\"status\":\"queued\"}"
                : "{\"update_id\":\"update-123\",\"status\":\"" + status + "\"" + resultJson + "}";
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json")
                .set(HttpHeaderName.fromString("Operation-Location"),
                    "https://localhost/api/projects/project/memory_stores/store/updates/update-123")
                .set(HttpHeaderName.RETRY_AFTER, "0");
            return Mono.just(
                new MockHttpResponse(request, initial ? 202 : 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        };
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost/api/projects/project")
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build());
        MemoryStoreUpdateCompletedResult result;
        if (async) {
            com.azure.ai.agents.BetaMemoryStoresAsyncClient client = builder.beta().buildBetaMemoryStoresAsyncClient();
            AsyncPollResponse<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> response = (resume
                ? client.resumeUpdateMemories("store", "update-123")
                : client.beginUpdateMemories("store", "scope")).setPollInterval(Duration.ofMillis(1))
                    .blockFirst(Duration.ofSeconds(5));
            assertNotNull(response);
            result = response.getFinalResult().block(Duration.ofSeconds(5));
        } else {
            com.azure.ai.agents.BetaMemoryStoresClient client = builder.beta().buildBetaMemoryStoresClient();
            result = (resume
                ? client.resumeUpdateMemories("store", "update-123")
                : client.beginUpdateMemories("store", "scope")).setPollInterval(Duration.ofMillis(1))
                    .getFinalResult(Duration.ofSeconds(5));
        }
        assertNotNull(result);
        assertTrue(result.getMemoryOperations().isEmpty());
        assertNotNull(result.getUsage());
        assertEquals(resultJson.contains("17") ? 17 : 0, result.getUsage().getTotalTokens());
        if (!resultJson.contains("17")) {
            assertEquals(0, result.getUsage().getEmbeddingTokens());
            assertEquals(0, result.getUsage().getInputTokens());
            assertEquals(0, result.getUsage().getOutputTokens());
            assertEquals(0, result.getUsage().getInputTokensDetails().getCachedTokensCount());
            assertEquals(0, result.getUsage().getInputTokensDetails().getCacheWriteTokens());
            assertEquals(0, result.getUsage().getOutputTokensDetails().getReasoningTokens());
        }
    }

    @Test
    void missingNonMemoryResultStillFails() {
        assertThrows(AzureException.class, () -> AgentsServicePollUtils.getFinalResultBody(Collections.emptyMap(),
            "result", TypeReference.createInstance(AgentOptimizationJobResult.class)));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void optimizationPollerExposesJobIdAndFinalResult(boolean async) {
        List<HttpRequest> requests = new ArrayList<>();
        HttpClient httpClient = request -> {
            requests.add(request);
            boolean initial = request.getHttpMethod() == HttpMethod.POST;
            String body = initial
                ? "{\"id\":\"job-123\",\"status\":\"queued\"}"
                : "{\"id\":\"job-123\",\"status\":\"succeeded\","
                    + "\"result\":{\"baseline\":\"candidate-baseline\",\"best\":\"candidate-best\",\"candidates\":[]}}";
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json")
                .set(HttpHeaderName.fromString("Operation-Location"),
                    "https://localhost/api/projects/project/operations/job-123")
                .set(HttpHeaderName.RETRY_AFTER, "0");
            return Mono.just(
                new MockHttpResponse(request, initial ? 201 : 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        };
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost/api/projects/project")
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .serviceVersion(AgentsServiceVersion.V1);

        AgentOptimizationJobResult result;
        if (async) {
            AsyncPollResponse<AgentOptimizationJob, AgentOptimizationJobResult> response = builder.beta()
                .buildBetaAgentsAsyncClient()
                .beginCreateOptimizationJob(new AgentOptimizationJob())
                .setPollInterval(Duration.ofMillis(1))
                .blockFirst(Duration.ofSeconds(5));
            assertNotNull(response);
            assertEquals("job-123", response.getValue().getId());
            result = response.getFinalResult().block(Duration.ofSeconds(5));
        } else {
            SyncPoller<AgentOptimizationJob, AgentOptimizationJobResult> poller = builder.beta()
                .buildBetaAgentsClient()
                .beginCreateOptimizationJob(new AgentOptimizationJob())
                .setPollInterval(Duration.ofMillis(1));
            assertEquals("job-123", poller.poll().getValue().getId());
            result = poller.getFinalResult(Duration.ofSeconds(5));
        }

        assertNotNull(result);
        assertEquals("candidate-baseline", result.getBaseline());
        assertEquals("candidate-best", result.getBest());
        assertEquals(1L, requests.stream().filter(request -> request.getHttpMethod() == HttpMethod.POST).count());
        assertTrue(requests.stream().anyMatch(request -> request.getHttpMethod() == HttpMethod.GET));
        requests.stream().filter(request -> request.getHttpMethod() == HttpMethod.GET).forEach(request -> {
            assertEquals("/api/projects/project/operations/job-123", request.getUrl().getPath());
            assertEquals("api-version=" + AgentsServiceVersion.V1.getVersion(), request.getUrl().getQuery());
        });
    }

    static Stream<Arguments> remapStatusCases() {
        return Stream.of(
            // Custom statuses that need remapping
            Arguments.of("completed", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of("Completed", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of("COMPLETED", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of("superseded", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of("Superseded", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED));
    }

    @ParameterizedTest
    @MethodSource("remapStatusCases")
    void remapStatusMapsCustomStatuses(String statusName, LongRunningOperationStatus expected) {
        // The parent's PollResult.setStatus(String) calls fromString(name, false) for unknown statuses
        LongRunningOperationStatus customStatus = LongRunningOperationStatus.fromString(statusName, false);
        PollResponse<String> original = new PollResponse<>(customStatus, "value");

        PollResponse<String> remapped = AgentsServicePollUtils.remapStatus(original);

        assertEquals(expected, remapped.getStatus());
        assertEquals("value", remapped.getValue());
    }

    static Stream<Arguments> standardStatusCases() {
        return Stream.of(Arguments.of(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of(LongRunningOperationStatus.FAILED), Arguments.of(LongRunningOperationStatus.USER_CANCELLED),
            Arguments.of(LongRunningOperationStatus.IN_PROGRESS), Arguments.of(LongRunningOperationStatus.NOT_STARTED));
    }

    @ParameterizedTest
    @MethodSource("standardStatusCases")
    void remapStatusPassesThroughStandardStatuses(LongRunningOperationStatus status) {
        PollResponse<String> original = new PollResponse<>(status, "value");

        PollResponse<String> result = AgentsServicePollUtils.remapStatus(original);

        assertSame(original, result, "Standard status should return the same PollResponse instance");
    }

    @Test
    void remapStatusPreservesRetryAfter() {
        LongRunningOperationStatus completed = LongRunningOperationStatus.fromString("completed", false);
        java.time.Duration retryAfter = java.time.Duration.ofSeconds(5);
        PollResponse<String> original = new PollResponse<>(completed, "value", retryAfter);

        PollResponse<String> remapped = AgentsServicePollUtils.remapStatus(original);

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, remapped.getStatus());
        assertEquals(retryAfter, remapped.getRetryAfter());
    }
}
