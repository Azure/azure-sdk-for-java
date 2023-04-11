// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util.polling;

import com.azure.core.experimental.http.MockHttpResponse;
import com.azure.core.experimental.models.PollResult;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.OperationResourcePollingStrategy;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperationLocationPollingStrategyTest {

    private static final TypeReference<TestResource> RESOURCE_TYPE_REFERENCE
        = TypeReference.createInstance(TestResource.class);
    private static final TypeReference<PollResult> RESOURCE_POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(PollResult.class);

    @Test
    public void operationLocationPollingStrategyPutSucceedsOnPoll() {
        // https://github.com/microsoft/api-guidelines/blob/vNext/azure/ConsiderationsForServiceDesign.md#put-with-additional-long-running-processing

        int[] activationCallCount = new int[1];
        String putUrl = "http://localhost";
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<TestResource>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.PUT, putUrl), 200,
                new HttpHeaders().set("Operation-Location", mockPollUrl), new TestResource("resource"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                Map<String, String> pollResponse = new HashMap<>();
                pollResponse.put("id", "operation1");
                pollResponse.put("status", "Succeeded");
                return Mono.just(new MockHttpResponse(pollRequest, 200,
                    BinaryData.fromObject(pollResponse).toBytes(), SerializerEncoding.JSON));
            } else if (putUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200,
                    BinaryData.fromObject(new TestResource("resource")).toBytes(), SerializerEncoding.JSON));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, TestResource> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            RESOURCE_POLL_RESULT_TYPE_REFERENCE, RESOURCE_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(resource -> "resource".equals(resource.getName()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    private static HttpPipeline createPipeline(HttpClient httpClient) {
        return new HttpPipelineBuilder().httpClient(httpClient).build();
    }
}
