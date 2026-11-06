// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.models.TelephonyCampaign;
import com.azure.ai.agents.models.TelephonyCampaignOperation;
import com.azure.ai.agents.models.TelephonyCampaignStatus;
import com.azure.core.exception.AzureException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.TypeReference;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CampaignOperationLocationPollingStrategyTest {
    private static final String ENDPOINT = "https://example.test";
    private static final String CAMPAIGN_PATH = "/agents/agent/telephony_campaigns/campaign";
    private static final String MONITOR_PATH = "/agents/agent/telephony_campaign_operations/original";
    private static final HttpHeaderName OPERATION_ID = HttpHeaderName.fromString("Operation-Id");
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String CAMPAIGN = "{\"name\":\"campaign\",\"connection_name\":\"connection\","
        + "\"source\":{\"type\":\"phone_number\",\"value\":\"+14255550123\"},"
        + "\"status\":\"cancelled\",\"operation_id\":\"later-action\"," + "\"created_at\":1,\"updated_at\":1}";

    static Stream<Arguments> campaignCases() {
        return Stream.of(false, true)
            .flatMap(async -> Stream.of(false, true)
                .flatMap(
                    raw -> Stream.of(HttpMethod.PUT, HttpMethod.POST).map(method -> Arguments.of(async, raw, method))));
    }

    @ParameterizedTest
    @MethodSource("campaignCases")
    void finalGetUsesOriginalOperationId(boolean async, boolean raw, HttpMethod method) {
        Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();
        requests.add(request -> monitorResponse(request, "succeeded"));
        requests.add(request -> {
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertEquals(CAMPAIGN_PATH, request.getUrl().getPath());
            assertTrue(request.getUrl().getQuery().contains("operation_id=original%2Boperation"));
            assertFalse(request.getUrl().getQuery().contains("later-action"));
            assertNull(request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
            return jsonResponse(request, 200, CAMPAIGN);
        });

        Object result = raw
            ? finish(async, initialResponse(method, 202, true, true), requests, BinaryData.class, BinaryData.class)
            : finish(async, initialResponse(method, 202, true, true), requests, TelephonyCampaignOperation.class,
                TelephonyCampaign.class);

        TelephonyCampaign campaign
            = raw ? ((BinaryData) result).toObject(TelephonyCampaign.class) : (TelephonyCampaign) result;
        assertEquals("campaign", campaign.getName());
        assertEquals("later-action", campaign.getOperationId());
        assertEquals(TelephonyCampaignStatus.CANCELLED, campaign.getStatus());
        assertTrue(requests.isEmpty());
    }

    static Stream<Arguments> deletionCases() {
        return Stream.of(false, true)
            .flatMap(async -> Stream.of(false, true)
                .flatMap(raw -> Stream.of(202, 204).map(status -> Arguments.of(async, raw, status))));
    }

    @ParameterizedTest
    @MethodSource("deletionCases")
    void deletionCompletesWithoutFinalGet(boolean async, boolean raw, int status) {
        Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();
        if (status == 202) {
            requests.add(request -> monitorResponse(request, "succeeded"));
        }
        Object result = raw
            ? finish(async, initialResponse(HttpMethod.DELETE, status, status == 202, false), requests,
                BinaryData.class, Void.class)
            : finish(async, initialResponse(HttpMethod.DELETE, status, status == 202, false), requests,
                TelephonyCampaignOperation.class, Void.class);
        assertNull(result);
        assertTrue(requests.isEmpty());
    }

    static Stream<Arguments> terminalErrorCases() {
        return Stream.of(false, true)
            .flatMap(async -> Stream.of(HttpMethod.POST, HttpMethod.DELETE)
                .flatMap(
                    method -> Stream.of("failed", "cancelled").map(status -> Arguments.of(async, method, status))));
    }

    @ParameterizedTest
    @MethodSource("terminalErrorCases")
    void unsuccessfulOperationsDoNotFetchOrReturnSuccess(boolean async, HttpMethod method, String status) {
        Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();
        requests.add(request -> monitorResponse(request, status));
        AzureException error = assertThrows(AzureException.class, () -> finish(async,
            initialResponse(method, 202, true, true), requests, BinaryData.class, BinaryData.class));
        assertTrue(error.getMessage().contains(status));
        assertTrue(requests.isEmpty());
    }

    static Stream<Arguments> finalGetErrorCases() {
        return Stream.of(false, true).flatMap(async -> Stream.of(404, 409).map(status -> Arguments.of(async, status)));
    }

    @ParameterizedTest
    @MethodSource("finalGetErrorCases")
    void guardedGetErrorsArePreserved(boolean async, int status) {
        Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();
        requests.add(request -> monitorResponse(request, "succeeded"));
        requests.add(request -> jsonResponse(request, status,
            "{\"error\":{\"code\":\"campaign_operation_mismatch\",\"message\":\"Original campaign unavailable\"}}"));
        HttpResponseException error = assertThrows(HttpResponseException.class, () -> finish(async,
            initialResponse(HttpMethod.PUT, 201, true, true), requests, BinaryData.class, TelephonyCampaign.class));
        assertEquals(status, error.getResponse().getStatusCode());
        assertTrue(requests.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void missingOperationIdFailsBeforePolling(boolean async) {
        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> finish(async, initialResponse(HttpMethod.POST, 202, true, false), new ArrayDeque<>(),
                BinaryData.class, BinaryData.class));
        assertTrue(error.getMessage().contains("Operation-Id"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void missingMonitorFailsBeforePolling(boolean async) {
        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> finish(async, initialResponse(HttpMethod.POST, 202, false, true), new ArrayDeque<>(),
                BinaryData.class, BinaryData.class));
        assertTrue(error.getMessage().contains("Operation-Location"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void bodylessFinalGetIsAnError(boolean async) {
        Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();
        requests.add(request -> monitorResponse(request, "succeeded"));
        requests.add(request -> jsonResponse(request, 200, null));
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> finish(async,
            initialResponse(HttpMethod.PUT, 201, true, true), requests, BinaryData.class, TelephonyCampaign.class));
        assertTrue(error.getMessage().contains("response body"));
        assertTrue(requests.isEmpty());
    }

    private static Response<BinaryData> initialResponse(HttpMethod method, int status, boolean monitor,
        boolean operationId) {
        HttpHeaders headers = new HttpHeaders();
        if (monitor) {
            headers.set(PollingUtils.OPERATION_LOCATION_HEADER, ENDPOINT + MONITOR_PATH);
        }
        if (operationId) {
            headers.set(OPERATION_ID, "original+operation");
        }
        String path = CAMPAIGN_PATH + (method == HttpMethod.POST ? ":cancel" : "");
        BinaryData body = method == HttpMethod.DELETE
            ? null
            : BinaryData
                .fromString(method == HttpMethod.PUT ? CAMPAIGN : "{\"id\":\"original\",\"status\":\"queued\"}");
        HttpRequest request = new HttpRequest(method, ENDPOINT + path);
        request.getHeaders().set(HttpHeaderName.IF_MATCH, "original-control-token");
        return new SimpleResponse<>(request, status, headers, body);
    }

    private static HttpResponse monitorResponse(HttpRequest request, String status) {
        assertEquals(HttpMethod.GET, request.getHttpMethod());
        assertEquals(MONITOR_PATH, request.getUrl().getPath());
        return jsonResponse(request, 200, "{\"id\":\"original\",\"status\":\"" + status + "\",\"created_at\":1}");
    }

    private static HttpResponse jsonResponse(HttpRequest request, int status, String body) {
        return new MockHttpResponse(request, status,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
            body == null ? new byte[0] : BinaryData.fromString(body).toBytes());
    }

    private static <T, U> U finish(boolean async, Response<BinaryData> initial,
        Deque<Function<HttpRequest, HttpResponse>> requests, Class<T> monitorType, Class<U> resultType) {
        HttpClient transport = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertTrue(async, "Sync polling must use synchronous HTTP.");
                return Mono.fromSupplier(() -> respond(request));
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                assertFalse(async, "Async polling must use asynchronous HTTP.");
                return respond(request);
            }

            private HttpResponse respond(HttpRequest request) {
                assertFalse(requests.isEmpty(), "Unexpected request: " + request.getUrl());
                return requests.removeFirst().apply(request);
            }
        };
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(transport).build();
        Context context = new Context("campaign-test", "preserved");
        PollingStrategyOptions options = new PollingStrategyOptions(pipeline).setEndpoint(ENDPOINT)
            .setServiceVersion(AgentsServiceVersion.getLatest().getVersion())
            .setContext(context);
        BetaVoiceAgentsCampaignsImpl client
            = new AgentsClientImpl(pipeline, ENDPOINT, AgentsServiceVersion.getLatest()).getBetaVoiceAgentsCampaigns();
        if (async) {
            return PollerFlux.create(Duration.ofMillis(1), () -> Mono.just(initial),
                new CampaignOperationLocationPollingStrategy<T, U>(options, requestOptions -> {
                    assertEquals(context, requestOptions.getContext());
                    return client.getCampaignWithResponseAsync("agent", "campaign", requestOptions);
                }), TypeReference.createInstance(monitorType), TypeReference.createInstance(resultType))
                .last()
                .flatMap(response -> response.getFinalResult())
                .block(TIMEOUT);
        }
        return SyncPoller.createPoller(Duration.ofMillis(1), () -> initial,
            new SyncCampaignOperationLocationPollingStrategy<T, U>(options, requestOptions -> {
                assertEquals(context, requestOptions.getContext());
                return client.getCampaignWithResponse("agent", "campaign", requestOptions);
            }), TypeReference.createInstance(monitorType), TypeReference.createInstance(resultType))
            .getFinalResult(TIMEOUT);
    }
}
