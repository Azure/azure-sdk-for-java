// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PollerTests {
    @Mock
    private Supplier<Mono<Response>> activationOperation;

    @Mock
    private BiFunction<PollResponse<Response>, PollResponse<Response>, Mono<PollResponse<Response>>> pollOperation;

    @Mock
    private BiFunction<PollResponse<Response>, PollResponse<Response>, Mono<CertificateOutput>> fetchResultOperation;

    @Mock
    private BiFunction<PollResponse<Response>, PollResponse<Response>, Mono<Response>> cancelOperation;

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void afterTest() {
        Mockito.framework().clearInlineMocks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPollIntervalZero() {
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ZERO,
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPollIntervalNegative() {
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(-1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);
    }

    @Test(expected = NullPointerException.class)
    public void constructorPollIntervalNull() {
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            null,
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);
    }

    @Test(expected = NullPointerException.class)
    public void constructorActivationOperationNull() {
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            null,
            pollOperation,
            cancelOperation,
            fetchResultOperation);
    }

    @Test(expected = NullPointerException.class)
    public void constructorPollOperationNull() {
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            null,
            cancelOperation,
            fetchResultOperation);
    }

    @Test(expected = NullPointerException.class)
    public void constructorCancelOperationNull() {
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            null,
            fetchResultOperation);
    }

    @Test(expected = NullPointerException.class)
    public void constructorFetchResultOperationNull() {
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            null);
    }

    @Test
    public void subscribeToSpecificOtherOperationStatusTest() {
        // Arrange
        final Duration retryAfter = Duration.ofMillis(100);
        //
        PollResponse<Response> response0 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("0"), retryAfter);

        PollResponse<Response> response1 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("1"), retryAfter);

        PollResponse<Response> response2 = new PollResponse<>(
            LongRunningOperationStatus.fromString("OTHER_1",false),
            new Response("2"), retryAfter);

        PollResponse<Response> response3 = new PollResponse<>(
            LongRunningOperationStatus.fromString("OTHER_2", false),
            new Response("3"), retryAfter);

        PollResponse<Response> response4 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("4"), retryAfter);

        when(activationOperation.get()).thenReturn(Mono.empty());

        when(pollOperation.apply(any(), any())).thenReturn(
            Mono.just(response0),
            Mono.just(response1),
            Mono.just(response2),
            Mono.just(response3),
            Mono.just(response4));

        // Act
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);

        // Assert
        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response3.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response4.getStatus())
            .verifyComplete();
    }

    @Test
    public void subscribeToActivationOnlyOnceTest() {
        // Arrange
        final Duration retryAfter = Duration.ofMillis(100);

        PollResponse<Response> response0 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("0"), retryAfter);

        PollResponse<Response> response1 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("1"), retryAfter);

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), retryAfter);

        int [] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        when(activationOperation.get()).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            return Mono.just(new Response("ActivationDone"));
        }));

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);

        when(pollOperation.apply(any(), any())).thenReturn(
            Mono.just(response0),
            Mono.just(response1),
            Mono.just(response2));

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .verifyComplete();

        when(pollOperation.apply(any(), any())).thenReturn(
            Mono.just(response0),
            Mono.just(response1),
            Mono.just(response2));

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .verifyComplete();

        Assert.assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void cancellationCanBeCalledFromOperatorChainTest() {
        final Duration retryAfter = Duration.ofMillis(100);

        PollResponse<Response> response0 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("0"), retryAfter);

        PollResponse<Response> response1 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("1"), retryAfter);

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), retryAfter);

        final Response activationResponse = new Response("Foo");
        when(activationOperation.get()).thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

        final List<Object> cancelParameters = new ArrayList<>();
        when(cancelOperation.apply(any(), any())).thenAnswer((Answer) invocation -> {
            for (Object argument : invocation.getArguments()) {
                cancelParameters.add(argument);
            }
            return Mono.just(new Response("OperationCancelled"));
        });

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);

        when(pollOperation.apply(any(), any())).thenReturn(
            Mono.just(response0),
            Mono.just(response1),
            Mono.just(response2));

        @SuppressWarnings({"rawtypes"})
        final AsyncPollResponse<Response, CertificateOutput>[] secondAsyncResponse = new AsyncPollResponse[1];
        secondAsyncResponse[0] = null;
        //
        Response cancelResponse = pollerFlux
            .take(2)
            .last()
            .flatMap((Function<AsyncPollResponse<Response, CertificateOutput>, Mono<Response>>) asyncPollResponse -> {
                secondAsyncResponse[0] = asyncPollResponse;
                return asyncPollResponse.cancelOperation();
            }).block();

        Assert.assertNotNull(cancelResponse);
        Assert.assertTrue(cancelResponse.getResponse().equalsIgnoreCase("OperationCancelled"));
        Assert.assertNotNull(secondAsyncResponse[0]);
        Assert.assertTrue(secondAsyncResponse[0].getValue().getResponse().equalsIgnoreCase("1"));
        Assert.assertEquals(2, cancelParameters.size());
        cancelParameters.get(0).equals(activationResponse);
        cancelParameters.get(1).equals(response1);
    }

    @Test
    public void getResultCanBeCalledFromOperatorChainTest() {
        final Duration retryAfter = Duration.ofMillis(100);

        PollResponse<Response> response0 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("0"), retryAfter);

        PollResponse<Response> response1 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
            new Response("1"), retryAfter);

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), retryAfter);

        final Response activationResponse = new Response("Foo");
        when(activationOperation.get()).thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

        final List<Object> fetchResultParameters = new ArrayList<>();
        when(fetchResultOperation.apply(any(), any())).thenAnswer((Answer) invocation -> {
            for (Object argument : invocation.getArguments()) {
                fetchResultParameters.add(argument);
            }
            return Mono.just(new CertificateOutput("LROFinalResult"));
        });

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);

        when(pollOperation.apply(any(), any())).thenReturn(
            Mono.just(response0),
            Mono.just(response1),
            Mono.just(response2));

        @SuppressWarnings({"rawtypes"})
        final AsyncPollResponse<Response, CertificateOutput>[] terminalAsyncResponse = new AsyncPollResponse[1];

        terminalAsyncResponse[0] = null;
        //
        CertificateOutput lroResult = pollerFlux
            .takeUntil(apr -> apr.getStatus().isComplete())
            .last()
            .flatMap((Function<AsyncPollResponse<Response, CertificateOutput>, Mono<CertificateOutput>>)
                asyncPollResponse -> {
                    terminalAsyncResponse[0] = asyncPollResponse;
                    return asyncPollResponse.getFinalResult();
                }).block();

        Assert.assertNotNull(lroResult);
        Assert.assertTrue(lroResult.getName().equalsIgnoreCase("LROFinalResult"));
        Assert.assertNotNull(terminalAsyncResponse[0]);
        Assert.assertTrue(terminalAsyncResponse[0].getValue().getResponse().equalsIgnoreCase("2"));
        Assert.assertEquals(2, fetchResultParameters.size());
        fetchResultParameters.get(0).equals(activationResponse);
        fetchResultParameters.get(1).equals(response2);
    }

    public static class Response {
        private final String response;

        public Response(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        @Override
        public String toString() {
            return "Response: " + response;
        }
    }

    public class CertificateOutput {
        String name;

        public CertificateOutput(String certName) {
            name = certName;
        }

        public String getName() {
            return name;
        }
    }
}
