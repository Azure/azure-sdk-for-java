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
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class OperationLocationPollingStrategyTest {

    private static final TypeReference<TestResource> RESOURCE_TYPE_REFERENCE
        = TypeReference.createInstance(TestResource.class);
    private static final TypeReference<PollResult> RESOURCE_POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(PollResult.class);

    @Test
    public void operationLocationPollingStrategyPutSucceedsOnPoll() {
        // https://github.com/microsoft/api-guidelines/blob/vNext/azure/ConsiderationsForServiceDesign.md#put-with-additional-long-running-processing

        String putUrl = "http://localhost/resource";
        String pollUrl = "http://localhost/operation";
        int[] pollCount = new int[] {0};

        String resourceName = "resource1";
        String operationId = "operation1";

        Supplier<Mono<Response<TestResource>>> activationOperation = () -> Mono.fromCallable(() -> {
            return new SimpleResponse<>(new HttpRequest(HttpMethod.PUT, putUrl), 200,
                new HttpHeaders().set("operation-location", pollUrl), new TestResource(resourceName));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, pollUrl);

        HttpClient httpClient = request -> {
            if (pollUrl.equals(request.getUrl().toString())) {
                ++pollCount[0];
                Map<String, String> pollResponse = new HashMap<>();
                pollResponse.put("id", operationId);
                pollResponse.put("status", pollCount[0] > 1 ? "Succeeded" : "InProgress");
                return Mono.just(new MockHttpResponse(pollRequest, 200,
                    BinaryData.fromObject(pollResponse).toBytes(), SerializerEncoding.JSON));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, TestResource> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationLocationPollingStrategy<>(
                new PollingStrategyOptions(createPipeline(httpClient))),
            RESOURCE_POLL_RESULT_TYPE_REFERENCE, RESOURCE_TYPE_REFERENCE);

        // verify poll result
        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS
                    && operationId.equals(asyncPollResponse.getValue().getId()))
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    && operationId.equals(asyncPollResponse.getValue().getId()))
            .verifyComplete();

        pollCount[0] = 0;

        // verify final result
        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(resource -> resourceName.equals(resource.getName()))
            .verifyComplete();
    }

    private static HttpPipeline createPipeline(HttpClient httpClient) {
        return new HttpPipelineBuilder().httpClient(httpClient).build();
    }
}
