// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.serializer.DefaultJsonSerializer;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PollingStrategyTests {
    private static final TypeReference<PollResult> POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(PollResult.class);
    private static final TypeReference<Resource> RESOURCE_TYPE_REFERENCE
        = TypeReference.createInstance(Resource.class);
    private static final TypeReference<ResourcePollResult> RESOURCE_POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(ResourcePollResult.class);

    @Test
    public void statusCheckPollingStrategySucceedsOnActivation() {
        int[] activationCallCount = new int[1];
        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.GET, "http://localhost"), 200, new HttpHeaders(),
                new PollResult("ActivationDone"));
        });

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new StatusCheckPollingStrategy<>(), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithResourceLocation() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set("Operation-Location", mockPollUrl), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("Succeeded", finalResultUrl)));
            } else if (finalResultUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("final-state", finalResultUrl)));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithPostLocationHeader() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set("Operation-Location", mockPollUrl).set("Location", finalResultUrl),
                new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("Succeeded")));
            } else if (finalResultUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last().
                flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingWithPostNoLocationHeaderInPollResponse() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                    new HttpHeaders().set("Operation-Location", mockPollUrl),
                    new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("Succeeded")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
                activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
                POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
                .expectSubscription()
                .expectNextMatches(asyncPollResponse ->
                        asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                        .last().
                        flatMap(AsyncPollResponse::getFinalResult))
                .expectNextMatches(pollResult -> "Succeeded".equals(pollResult.getStatus()))
                .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithPut() {
        int[] activationCallCount = new int[1];
        String putUrl = "http://localhost";
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.PUT, putUrl), 200,
                new HttpHeaders().set("Operation-Location", mockPollUrl), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("Succeeded")));
            } else if (putUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithPutPolling() {
        // https://github.com/microsoft/api-guidelines/blob/vNext/azure/ConsiderationsForServiceDesign.md#put-with-additional-long-running-processing
        // The difference from case "operationLocationPollingStrategySucceedsOnPollWithPut" is that the response of PUT is the Resource object, not PullResult object

        int[] activationCallCount = new int[1];
        String putUrl = "http://localhost";
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<Resource>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.PUT, putUrl), 200,
                new HttpHeaders().set("Operation-Location", mockPollUrl), new Resource("resource"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new ResourcePollResult("Succeeded")));
            } else if (putUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new Resource("resource")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<ResourcePollResult, Resource> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
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

    @Test
    public void operationLocationPollingStrategyFailsOnPoll() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set("Operation-Location", mockPollUrl), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(), new PollResult("Failed")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectErrorMessage("Long running operation failed.")
            .verify();

        assertEquals(1, activationCallCount[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Operation-Location", "resourceLocation"})
    public void operationResourcePollingStrategyRelativePath(String headerName) {
        int[] activationCallCount = new int[1];
        String endpointUrl = "http://localhost";
        String mockPollRelativePath = "/poll";
        String finalResultAbsolutePath = endpointUrl + "/final";
        String mockPollAbsolutePath = endpointUrl + mockPollRelativePath;

        // Mocking
        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;

            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost/post"), 200,
                new HttpHeaders().set(headerName, mockPollRelativePath), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollAbsolutePath);
        HttpClient httpClient = request -> {
            if (mockPollAbsolutePath.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("Succeeded", finalResultAbsolutePath)));
            } else if (finalResultAbsolutePath.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("final-state", finalResultAbsolutePath)));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        // Create OperationResourcePollingStrategy
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(createPipeline(httpClient), endpointUrl,
                new DefaultJsonSerializer(), headerName, Context.NONE),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        // Verify
        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void locationPollingStrategySucceedsOnPollWithPostLocationHeader() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set("Location", mockPollUrl), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200,
                    new HttpHeaders().set("Location", finalResultUrl),
                    new PollResult("Succeeded")));
            } else if (finalResultUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new LocationPollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void locationPollingWithPostNoLocationHeaderInPollResponse() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                    new HttpHeaders().set("Location", mockPollUrl), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200,
                        new HttpHeaders(),
                        new PollResult("Succeeded")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
                activationOperation::get, new LocationPollingStrategy<>(createPipeline(httpClient)),
                POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
                .expectSubscription()
                .expectNextMatches(asyncPollResponse ->
                        asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                        .last()
                        .flatMap(AsyncPollResponse::getFinalResult))
                .expectNextMatches(pollResult -> "Succeeded".equals(pollResult.getStatus()))
                .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void locationPollingStrategyRelativePath() {
        int[] activationCallCount = new int[1];
        String endpointUrl = "http://localhost";
        String mockPollRelativePath = "/poll";
        String finalResultAbsolutePath = endpointUrl + "/final";
        String mockPollAbsolutePath = endpointUrl + mockPollRelativePath;

        // Mocking
        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost/post"), 200,
                new HttpHeaders().set("Location", mockPollRelativePath), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollAbsolutePath);

        HttpClient httpClient = request -> {
            if (mockPollAbsolutePath.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200,
                    new HttpHeaders().set("Location", finalResultAbsolutePath),
                    new PollResult("Succeeded")));
            } else if (finalResultAbsolutePath.equals(request.getUrl().toString())) {
                return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                    new PollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        // Create LocationPollingStrategy
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new LocationPollingStrategy<>(createPipeline(httpClient), endpointUrl,
                new DefaultJsonSerializer(), Context.NONE), POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void pollingStrategyPassContextToHttpClient() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set("Location", mockPollUrl), new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        AtomicReference<Context> lastContext = new AtomicReference<>();
        HttpClient httpClient = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return send(request, Context.NONE);
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request, Context context) {
                lastContext.set(context);
                if (mockPollUrl.equals(request.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200,
                        new HttpHeaders().set("Location", finalResultUrl),
                        new PollResult("Succeeded")));
                } else if (finalResultUrl.equals(request.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
                }
            }
        };

        // PollingStrategy with context = Context.NONE
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new DefaultPollingStrategy<>(createPipeline(httpClient), null, null),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux.map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals(Context.NONE, lastContext.get());

        // PollingStrategy with context
        final Context context = new Context("key", "value");
        pollerFlux = PollerFlux.create(Duration.ofSeconds(1), activationOperation::get,
            new DefaultPollingStrategy<>(createPipeline(httpClient), null, context), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux.map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals("value", lastContext.get().getData("key").orElse(null));

        pollerFlux = PollerFlux.create(Duration.ofSeconds(1), activationOperation::get,
            new DefaultPollingStrategy<>(createPipeline(httpClient), null, Context.NONE), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux.contextWrite(reactor.util.context.Context.of("key2", "value2")).map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals("value2", lastContext.get().getData("key2").orElse(null));

        assertEquals(3, activationCallCount[0]);
    }

    @ParameterizedTest
    @MethodSource("statusCodeProvider")
    public void retryPollingOperationWithPostActivationOperation(int[] args) {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Mono<Response<PollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set("Operation-Location", mockPollUrl).set("Location", finalResultUrl),
                new PollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (mockPollUrl.equals(request.getUrl().toString()) && count == 0) {
                    return Mono.just(new MockHttpResponse(pollRequest, args[0],
                        new HttpHeaders().set("Location", finalResultUrl),
                        new PollResult("Retry")));
                } else if (mockPollUrl.equals(request.getUrl().toString()) && count == 1) {
                    return Mono.just(new MockHttpResponse(pollRequest, args[1],
                        new HttpHeaders().set("Location", finalResultUrl),
                        new PollResult("Succeeded")));
                } else if (finalResultUrl.equals(request.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, args[2], new HttpHeaders(),
                        new PollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
                }
            })
            .build();
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(Duration.ofSeconds(1),
            activationOperation::get, new OperationResourcePollingStrategy<>(pipeline), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();
        assertEquals(args[3], attemptCount.get());
        assertEquals(1, activationCallCount[0]);
    }

    static Stream<int[]> statusCodeProvider() {
        return Stream.of(
            new int[]{500, 200, 200, 3},
            new int[]{200, 500, 200, 2});
    }

    public static class PollResult {
        private String status;
        private String resourceLocation;

        public PollResult() {
        }

        public PollResult(String status) {
            this.status = status;
            this.resourceLocation = null;
        }

        public PollResult(String status, String resourceLocation) {
            this.status = status;
            this.resourceLocation = resourceLocation;
        }

        public String getStatus() {
            return status;
        }

        public PollResult setStatus(String status) {
            this.status = status;
            return this;
        }

        public String getResourceLocation() {
            return resourceLocation;
        }

        public PollResult setResourceLocation(String resourceLocation) {
            this.resourceLocation = resourceLocation;
            return this;
        }

        @Override
        public String toString() {
            return "Status: " + status;
        }
    }

    public static class ResourcePollResult {
        private final String status;

        @JsonCreator
        public ResourcePollResult(@JsonProperty(value = "status", required = true) String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class Resource {
        private final String name;

        @JsonCreator
        public Resource(@JsonProperty(value = "name", required = true) String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Resource: " + name;
        }
    }

    private static HttpPipeline createPipeline(HttpClient httpClient) {
        return new HttpPipelineBuilder().httpClient(httpClient).build();
    }
}
