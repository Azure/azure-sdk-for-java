// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.serializer.DefaultJsonSerializer;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PollingStrategyTests {
    @Mock
    private Supplier<Mono<Response<PollResult>>> activationOperation;

    @Mock
    private HttpClient httpClient;

    private AutoCloseable openMocks;

    @BeforeEach
    public void beforeTest() {
        this.openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterTest() throws Exception {
        openMocks.close();
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    public void statusCheckPollingStrategySucceedsOnActivation() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.GET, "http://localhost"),
                200,
                new HttpHeaders(),
                new PollResult("ActivationDone"));
            return Mono.just(response);
        }));
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new StatusCheckPollingStrategy<>(),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithResourceLocation() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.POST, "http://localhost"),
                200,
                new HttpHeaders().set("Operation-Location", mockPollUrl),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));
        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        when(httpClient.send(any(), any()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("Succeeded", finalResultUrl)));
                } else if (finalResultUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state", finalResultUrl)));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new OperationResourcePollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build()),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithPostLocationHeader() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.POST, "http://localhost"),
                200,
                new HttpHeaders().set("Operation-Location", mockPollUrl).set("Location", finalResultUrl),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));
        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        when(httpClient.send(any(), any()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("Succeeded")));
                } else if (finalResultUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new OperationResourcePollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build()),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategySucceedsOnPollWithPut() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String putUrl = "http://localhost";
        String mockPollUrl = "http://localhost/poll";
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.PUT, putUrl),
                200,
                new HttpHeaders().set("Operation-Location", mockPollUrl),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));
        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        when(httpClient.send(any(), any()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("Succeeded")));
                } else if (putUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new OperationResourcePollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build()),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void operationLocationPollingStrategyFailsOnPoll() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String mockPollUrl = "http://localhost/poll";
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.POST, "http://localhost"),
                200,
                new HttpHeaders().set("Operation-Location", mockPollUrl),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));
        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        when(httpClient.send(any(), any()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("Failed")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new OperationResourcePollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build()),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectErrorMessage("Long running operation failed.")
            .verify();
        assertEquals(1, activationCallCount[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Operation-Location", "resourceLocation"})
    public void operationResourcePollingStrategyRelativePath(String headerName) {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String endpointUrl = "http://localhost";
        String mockPollRelativePath = "/poll";
        String finalResultAbsolutePath = endpointUrl + "/final";
        String mockPollAbsolutePath = endpointUrl + mockPollRelativePath;
        // Mocking
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.POST, "http://localhost/post"),
                200,
                new HttpHeaders().set(headerName, mockPollRelativePath),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollAbsolutePath);
        when(httpClient.send(any(), any()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollAbsolutePath.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("Succeeded", finalResultAbsolutePath)));
                } else if (finalResultAbsolutePath.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state", finalResultAbsolutePath)));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });

        // Create OperationResourcePollingStrategy
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new OperationResourcePollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build(),
                endpointUrl, new DefaultJsonSerializer(), headerName, Context.NONE),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        // Verify
        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                                                        == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void locationPollingStrategySucceedsOnPollWithPostLocationHeader() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.POST, "http://localhost"),
                200,
                new HttpHeaders().set("Location", mockPollUrl),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));
        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        when(httpClient.send(any(), any()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200,
                        new HttpHeaders().set("Location", finalResultUrl),
                        new PollResult("Succeeded")));
                } else if (finalResultUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new LocationPollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build()),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void locationPollingStrategyRelativePath() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String endpointUrl = "http://localhost";
        String mockPollRelativePath = "/poll";
        String finalResultAbsolutePath = endpointUrl + "/final";
        String mockPollAbsolutePath = endpointUrl + mockPollRelativePath;
        // Mocking
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.POST, "http://localhost/post"),
                200,
                new HttpHeaders().set("Location", mockPollRelativePath),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollAbsolutePath);
        when(httpClient.send(any(), any()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollAbsolutePath.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200,
                        new HttpHeaders().set("Location", finalResultAbsolutePath),
                        new PollResult("Succeeded")));
                } else if (finalResultAbsolutePath.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });

        // Create LocationPollingStrategy
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new LocationPollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build(),
                endpointUrl, new DefaultJsonSerializer(), Context.NONE),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                                                        == LongRunningOperationStatus.IN_PROGRESS);

        StepVerifier.create(pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(pollResult -> "final-state".equals(pollResult.getStatus()))
            .verifyComplete();
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void pollingStrategyPassContextToHttpClient() {
        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            SimpleResponse<PollResult> response = new SimpleResponse<>(
                new HttpRequest(HttpMethod.POST, "http://localhost"),
                200,
                new HttpHeaders().set("Location", mockPollUrl),
                new PollResult("InProgress"));
            return Mono.just(response);
        }));
        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);
        ArgumentCaptor<Context> contextArgument = ArgumentCaptor.forClass(Context.class);
        when(httpClient.send(any(), contextArgument.capture()))
            .thenAnswer(iom -> {
                HttpRequest req = iom.getArgument(0);
                if (mockPollUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200,
                        new HttpHeaders().set("Location", finalResultUrl),
                        new PollResult("Succeeded")));
                } else if (finalResultUrl.equals(req.getUrl().toString())) {
                    return Mono.just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(),
                        new PollResult("final-state")));
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown request URL " + req.getUrl()));
                }
            });

        // PollingStrategy with context = Context.NONE
        PollerFlux<PollResult, PollResult> pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new DefaultPollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build(), null, null),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux.map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals(Context.NONE, contextArgument.getValue());

        // PollingStrategy with context
        final Context context = new Context("key", "value");
        pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new DefaultPollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build(), null, context),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux.map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals("value", contextArgument.getValue().getData("key").orElse(null));

        pollerFlux = PollerFlux.create(
            Duration.ofSeconds(1),
            () -> activationOperation.get(),
            new DefaultPollingStrategy<>(new HttpPipelineBuilder().httpClient(httpClient).build(), null, Context.NONE),
            new TypeReference<PollResult>() { }, new TypeReference<PollResult>() { });

        StepVerifier.create(pollerFlux.contextWrite(reactor.util.context.Context.of("key2", "value2")).map(AsyncPollResponse::getStatus))
            .expectSubscription()
            .expectNext(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();
        Assertions.assertEquals("value2", contextArgument.getValue().getData("key2").orElse(null));

        assertEquals(3, activationCallCount[0]);
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
}
