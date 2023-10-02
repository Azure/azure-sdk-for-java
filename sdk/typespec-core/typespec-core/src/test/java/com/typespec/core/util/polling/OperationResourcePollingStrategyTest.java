// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.MockHttpResponse;
import com.typespec.core.http.policy.RetryPolicy;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.SimpleResponse;
import com.typespec.core.implementation.serializer.DefaultJsonSerializer;
import com.typespec.core.util.Context;
import com.typespec.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.typespec.core.util.polling.DefaultPollingStrategyTests.createPipeline;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperationResourcePollingStrategyTest {
    private static final TypeReference<TestPollResult> POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(TestPollResult.class);
    private static final HttpHeaderName OPERATION_LOCATION = HttpHeaderName.fromString("Operation-Location");

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithResourceLocation() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(OPERATION_LOCATION, mockPollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("Succeeded", finalResultUrl)));
            } else if (finalResultUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("final-state", finalResultUrl)));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithPostLocationHeader() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(OPERATION_LOCATION, mockPollUrl).set(HttpHeaderName.LOCATION, finalResultUrl),
                new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("Succeeded")));
            } else if (finalResultUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last().
                flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingWithPostNoLocationHeaderInPollResponse() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(OPERATION_LOCATION, mockPollUrl),
                new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("Succeeded")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last().
                flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "Succeeded".equals(testPollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithPut() {
        int[] activationCallCount = new int[1];
        String putUrl = "http://localhost";
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.PUT, putUrl), 200,
                new HttpHeaders().set(OPERATION_LOCATION, mockPollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("Succeeded")));
            } else if (putUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {"Operation-Location", "resourceLocation"})
    public void operationResourcePollingStrategyRelativePath(String headerName) {
        int[] activationCallCount = new int[1];
        String endpointUrl = "http://localhost";
        String mockPollRelativePath = "/poll";
        String finalResultAbsolutePath = endpointUrl + "/final";
        String mockPollAbsolutePath = endpointUrl + mockPollRelativePath;

        // Mocking
        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;

            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost/post"), 200,
                new HttpHeaders().set(headerName, mockPollRelativePath), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollAbsolutePath);
        HttpClient httpClient = request -> {
            if (mockPollAbsolutePath.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("Succeeded", finalResultAbsolutePath)));
            } else if (finalResultAbsolutePath.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("final-state", finalResultAbsolutePath)));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        // Create OperationResourcePollingStrategy
        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient), endpointUrl,
                new DefaultJsonSerializer(), headerName, Context.NONE),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        // Verify
        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategyFailsOnPoll() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(OPERATION_LOCATION, mockPollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(), new TestPollResult("Failed")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.FAILED)
            .verifyComplete();

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectErrorMessage("Long running operation failed.")
            .verify();

        assertEquals(1, activationCallCount[0]);
    }

    @ParameterizedTest
    @MethodSource("statusCodeProvider")
    public void retryPollingOperationWithPostActivationOperation(int[] args) {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(OPERATION_LOCATION, mockPollUrl).set(HttpHeaderName.LOCATION, finalResultUrl),
                new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        HttpRequest finalRequest = new HttpRequest(HttpMethod.GET, finalResultUrl);
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (mockPollUrl.equals(request.getUrl().toString()) && count == 0) {
                    return Mono.just(new MockHttpResponse(pollRequest, args[0],
                        new HttpHeaders().set(HttpHeaderName.LOCATION, finalResultUrl),
                        new TestPollResult("Succeeded")));
                } else if (mockPollUrl.equals(request.getUrl().toString()) && count == 1) {
                    return Mono.just(new MockHttpResponse(pollRequest, args[1],
                        new HttpHeaders().set(HttpHeaderName.LOCATION, finalResultUrl),
                        new TestPollResult("Succeeded")));
                } else if (finalResultUrl.equals(request.getUrl().toString()) && count == 1) {
                    return Mono.just(new MockHttpResponse(finalRequest, args[1], new HttpHeaders(),
                        new TestPollResult("final-state")));
                } else if (finalResultUrl.equals(request.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(finalRequest, args[2], new HttpHeaders(),
                        new TestPollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
                }
            })
            .build();
        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(pipeline), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();
        assertEquals(args[3], attemptCount.get());
        assertEquals(1, activationCallCount[0]);
    }

    @ParameterizedTest
    @CsvSource({ "http://localhost/poll?api-version=2023-03-22, http://localhost/poll, http://localhost/final?api-version=2023-03-22, http://localhost/final",
        "http://localhost/poll?api-version=2023-03-22, http://localhost/poll?api-version=2021-03-22, http://localhost/final?api-version=2023-03-22, http://localhost/final?api-version=2021-03-22"})
    public void operationLocationPollingStrategyServiceVersionTest(String requestPollUrl, String responsePollUrl,
                                                                   String requestFinalResultUrl, String responseFinalResultUrl) {
        int[] activationCallCount = new int[1];

        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(OPERATION_LOCATION, responsePollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, requestPollUrl);

        HttpClient httpClient = request -> {
            if (requestPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("Succeeded", responseFinalResultUrl)));
            } else if (requestFinalResultUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new TestPollResult("final-state", responseFinalResultUrl)));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollingStrategyOptions pollingStrategyOptions = new PollingStrategyOptions(createPipeline(httpClient))
            .setServiceVersion("2023-03-22");
        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(null, pollingStrategyOptions),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    static Stream<int[]> statusCodeProvider() {
        return Stream.of(
            // poll 500, poll 200 Succeeded, final 200
            new int[]{500, 200, 200, 3},
            // poll 200 Succeeded, final 500, final 200
            new int[]{200, 500, 200, 3});
    }

}
