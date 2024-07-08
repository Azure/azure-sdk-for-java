// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import io.clientcore.core.http.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.rest.Response;
import io.clientcore.core.http.rest.SimpleResponse;
import io.clientcore.core.util.Context;
import com.azure.core.v2.util.serializer.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DefaultPollingStrategy}.
 */
public class DefaultPollingStrategyTests {
    private static final TypeReference<TestPollResult> POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(TestPollResult.class);

    @Test
    public void pollingStrategyPassContextToHttpClient() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(HttpHeaderName.LOCATION, mockPollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        AtomicReference<Context> lastContext = new AtomicReference<>();
        HttpClient httpClient = getHttpClient(mockPollUrl, finalResultUrl, pollRequest, lastContext);

        // PollingStrategy with context = Context.none()
        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new DefaultPollingStrategy<>(createPipeline(httpClient), null, null),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux.map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals(Context.none(), lastContext.get());

        // PollingStrategy with context
        final Context context = new Context("key", "value");
        pollerFlux = PollerFlux.create(Duration.ofMillis(1), activationOperation::get,
            new DefaultPollingStrategy<>(createPipeline(httpClient), null, context), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux.map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals("value", lastContext.get().getData("key").orElse(null));

        pollerFlux = PollerFlux.create(Duration.ofMillis(1), activationOperation::get,
            new DefaultPollingStrategy<>(createPipeline(httpClient), null, Context.none()), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier
            .create(pollerFlux.contextWrite(reactor.util.context.Context.of("key2", "value2"))
                .map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals("value2", lastContext.get().getData("key2").orElse(null));

        assertEquals(3, activationCallCount[0]);
    }

    private static HttpClient getHttpClient(String mockPollUrl, String finalResultUrl, HttpRequest pollRequest,
        AtomicReference<Context> lastContext) {
        return new HttpClient() {
            @Override
            public Response<?>> send(HttpRequest request) {
                return send(request);
            }

            @Override
            public Response<?>> send(HttpRequest request, Context context) {
                lastContext.set(context);
                if (mockPollUrl.equals(request.getUrl().toString())) {
                    return new MockHttpResponse(pollRequest, 200,
                        new HttpHeaders().set(HttpHeaderName.LOCATION, finalResultUrl),
                        new TestPollResult("Succeeded")));
                } else if (finalResultUrl.equals(request.getUrl().toString())) {
                    return
                        new MockHttpResponse(pollRequest, 200, new HttpHeaders(), new TestPollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
                }
            }
        };
    }

    static HttpPipeline createPipeline(HttpClient httpClient) {
        return new HttpPipelineBuilder().httpClient(httpClient).build();
    }
}
