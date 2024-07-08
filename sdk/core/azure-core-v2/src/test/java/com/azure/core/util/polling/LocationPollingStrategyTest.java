// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import io.clientcore.core.http.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.rest.Response;
import io.clientcore.core.http.rest.SimpleResponse;
import com.azure.core.v2.implementation.serializer.DefaultJsonSerializer;
import io.clientcore.core.util.Context;
import com.azure.core.v2.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Supplier;

import static com.azure.core.util.polling.DefaultPollingStrategyTests.createPipeline;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocationPollingStrategyTest {
    private static final TypeReference<TestPollResult> POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(TestPollResult.class);

    @Test
    public void locationPollingStrategyRelativePath() {
        int[] activationCallCount = new int[1];
        String endpointUrl = "http://localhost";
        String mockPollRelativePath = "/poll";
        String finalResultAbsolutePath = endpointUrl + "/final";
        String mockPollAbsolutePath = endpointUrl + mockPollRelativePath;

        // Mocking
        Supplier<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost/post"), 200,
                new HttpHeaders().set(HttpHeaderName.LOCATION, mockPollRelativePath), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollAbsolutePath);

        HttpClient httpClient = request -> {
            if (mockPollAbsolutePath.equals(request.getUrl().toString())) {
                return new MockHttpResponse(pollRequest, 200,
                    new HttpHeaders().set(HttpHeaderName.LOCATION, finalResultAbsolutePath),
                    new TestPollResult("Succeeded")));
            } else if (finalResultAbsolutePath.equals(request.getUrl().toString())) {
                return Mono
                    .just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(), new TestPollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        // Create LocationPollingStrategy
        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(
            Duration.ofMillis(1), activationOperation::get, new LocationPollingStrategy<>(createPipeline(httpClient),
                endpointUrl, new DefaultJsonSerializer(), Context.none()),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(
                asyncPollResponse -> asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(
            pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void locationPollingWithPostNoLocationHeaderInPollResponse() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";

        Supplier<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(HttpHeaderName.LOCATION, mockPollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return Mono
                    .just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(), new TestPollResult("Succeeded")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new LocationPollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(
                asyncPollResponse -> asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(
            pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "Succeeded".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void locationPollingStrategySucceedsOnPollWithPostLocationHeader() {
        int[] activationCallCount = new int[1];
        String mockPollUrl = "http://localhost/poll";
        String finalResultUrl = "http://localhost/final";

        Supplier<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(HttpHeaderName.LOCATION, mockPollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, mockPollUrl);

        HttpClient httpClient = request -> {
            if (mockPollUrl.equals(request.getUrl().toString())) {
                return new MockHttpResponse(pollRequest, 200,
                    new HttpHeaders().set(HttpHeaderName.LOCATION, finalResultUrl), new TestPollResult("Succeeded")));
            } else if (finalResultUrl.equals(request.getUrl().toString())) {
                return Mono
                    .just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(), new TestPollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new LocationPollingStrategy<>(createPipeline(httpClient)),
            POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(
                asyncPollResponse -> asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(
            pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

    @ParameterizedTest
    @CsvSource({
        "http://localhost/poll?api-version=2023-03-22, http://localhost/poll, http://localhost/final?api-version=2023-03-22, http://localhost/final",
        "http://localhost/poll?api-version=2023-03-22, http://localhost/poll?api-version=2021-03-22, http://localhost/final?api-version=2023-03-22, http://localhost/final?api-version=2021-03-22" })
    public void locationPollingStrategyServiceVersionTest(String requestPollUrl, String responsePollUrl,
        String requestFinalResultUrl, String responseFinalResultUrl) {
        int[] activationCallCount = new int[1];

        Supplier<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "http://localhost"), 200,
                new HttpHeaders().set(HttpHeaderName.LOCATION, responsePollUrl), new TestPollResult("InProgress"));
        });

        HttpRequest pollRequest = new HttpRequest(HttpMethod.GET, responsePollUrl);

        HttpClient httpClient = request -> {
            if (requestPollUrl.equals(request.getUrl().toString())) {
                return new MockHttpResponse(pollRequest, 200,
                    new HttpHeaders().set(HttpHeaderName.LOCATION, responseFinalResultUrl),
                    new TestPollResult("Succeeded")));
            } else if (requestFinalResultUrl.equals(request.getUrl().toString())) {
                return Mono
                    .just(new MockHttpResponse(pollRequest, 200, new HttpHeaders(), new TestPollResult("final-state")));
            } else {
                return Mono.error(new IllegalArgumentException("Unknown request URL " + request.getUrl()));
            }
        };

        PollingStrategyOptions pollingStrategyOptions
            = new PollingStrategyOptions(createPipeline(httpClient)).setServiceVersion("2023-03-22");
        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new LocationPollingStrategy<>(pollingStrategyOptions), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(
                asyncPollResponse -> asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        StepVerifier.create(
            pollerFlux.takeUntil(apr -> apr.getStatus().isComplete()).last().flatMap(AsyncPollResponse::getFinalResult))
            .expectNextMatches(testPollResult -> "final-state".equals(testPollResult.getStatus()))
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }
}
