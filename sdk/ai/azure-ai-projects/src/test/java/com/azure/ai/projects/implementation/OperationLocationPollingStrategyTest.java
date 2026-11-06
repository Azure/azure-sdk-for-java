// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.TypeReference;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationLocationPollingStrategyTest {
    private static final String ENDPOINT = "https://example.test";
    private static final Duration RETRY_AFTER = Duration.ofSeconds(5);

    static Stream<Arguments> pollingStatusCases() {
        return Stream.of(false, true)
            .flatMap(async -> Stream.of(Arguments.of(async, "cancelled", LongRunningOperationStatus.USER_CANCELLED),
                Arguments.of(async, "CANCELLED", LongRunningOperationStatus.USER_CANCELLED),
                Arguments.of(async, "succeeded", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
                Arguments.of(async, "failed", LongRunningOperationStatus.FAILED),
                Arguments.of(async, "queued", LongRunningOperationStatus.fromString("queued", false))));
    }

    @ParameterizedTest
    @MethodSource("pollingStatusCases")
    void pollingNormalizesCancellationAndPreservesResponse(boolean async, String status,
        LongRunningOperationStatus expected) {
        String body = "{\"id\":\"job\",\"status\":\"" + status + "\"}";
        HttpClient httpClient = request -> {
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertEquals("/jobs/job", request.getUrl().getPath());
            return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "5"),
                body.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public BinaryData getBodyAsBinaryData() {
                    return BinaryData.fromString(body);
                }
            });
        };
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient).build();
        PollingStrategyOptions options = new PollingStrategyOptions(pipeline).setEndpoint(ENDPOINT);
        Response<BinaryData> initial = new SimpleResponse<>(new HttpRequest(HttpMethod.POST, ENDPOINT + "/jobs"), 201,
            new HttpHeaders().set("Operation-Location", ENDPOINT + "/jobs/job"),
            BinaryData.fromString("{\"id\":\"job\",\"status\":\"queued\"}"));
        TypeReference<BinaryData> responseType = TypeReference.createInstance(BinaryData.class);

        if (async) {
            OperationLocationPollingStrategy<BinaryData, BinaryData> strategy
                = new OperationLocationPollingStrategy<>(options, "result");
            StepVerifier.create(
                PollerFlux.create(Duration.ofMillis(1), () -> Mono.just(initial), strategy, responseType, responseType)
                    .next())
                .assertNext(response -> {
                    assertEquals(expected, response.getStatus());
                    assertEquals(expected.isComplete(), response.getStatus().isComplete());
                    assertEquals(body, response.getValue().toString());
                })
                .verifyComplete();
        } else {
            SyncOperationLocationPollingStrategy<BinaryData, BinaryData> strategy
                = new SyncOperationLocationPollingStrategy<>(options, "result");
            assertPollResponse(
                SyncPoller.createPoller(Duration.ofMillis(1), () -> initial, strategy, responseType, responseType)
                    .poll(),
                expected, body);
        }
    }

    private static void assertPollResponse(PollResponse<BinaryData> response, LongRunningOperationStatus expected,
        String body) {
        assertEquals(expected, response.getStatus());
        assertEquals(expected.isComplete(), response.getStatus().isComplete());
        assertEquals(body, response.getValue().toString());
        assertEquals(RETRY_AFTER, response.getRetryAfter());
    }
}
