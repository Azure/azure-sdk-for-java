// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PollerTests {
    @Mock
    private Function<PollingContext<Response>, Mono<Response>> activationOperation;

    @Mock
    private Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation;

    @Mock
    private Function<PollingContext<Response>, Mono<CertificateOutput>> fetchResultOperation;

    @Mock
    private BiFunction<PollingContext<Response>, PollResponse<Response>, Mono<Response>> cancelOperation;

    @BeforeEach
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void afterTest() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void asyncPollerConstructorPollIntervalZero() {
        assertThrows(IllegalArgumentException.class, () -> new PollerFlux<>(
            Duration.ZERO,
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void asyncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class, () -> new PollerFlux<>(
            Duration.ofSeconds(-1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void asyncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(
            null,
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void asyncPollerConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(
            Duration.ofSeconds(1),
            null,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void asyncPollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            null,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void asyncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            null,
            fetchResultOperation));
    }

    @Test
    public void asyncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            null));
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
            LongRunningOperationStatus.fromString("OTHER_1", false),
            new Response("2"), retryAfter);

        PollResponse<Response> response3 = new PollResponse<>(
            LongRunningOperationStatus.fromString("OTHER_2", false),
            new Response("3"), retryAfter);

        PollResponse<Response> response4 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("4"), retryAfter);

        when(activationOperation.apply(any())).thenReturn(Mono.empty());

        when(pollOperation.apply(any())).thenReturn(
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

        int[] activationCallCount = new int[1];
        activationCallCount[0] = 0;
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> {
            activationCallCount[0]++;
            return Mono.just(new Response("ActivationDone"));
        }));

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation);

        when(pollOperation.apply(any())).thenReturn(
            Mono.just(response0),
            Mono.just(response1),
            Mono.just(response2));

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .verifyComplete();

        when(pollOperation.apply(any())).thenReturn(
            Mono.just(response0),
            Mono.just(response1),
            Mono.just(response2));

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .verifyComplete();

        Assertions.assertEquals(1, activationCallCount[0]);
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
        when(activationOperation.apply(any()))
                .thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

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

        when(pollOperation.apply(any())).thenReturn(
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

        Assertions.assertNotNull(cancelResponse);
        Assertions.assertTrue(cancelResponse.getResponse().equalsIgnoreCase("OperationCancelled"));
        Assertions.assertNotNull(secondAsyncResponse[0]);
        Assertions.assertTrue(secondAsyncResponse[0].getValue().getResponse().equalsIgnoreCase("1"));
        Assertions.assertEquals(2, cancelParameters.size());
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
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

        final List<Object> fetchResultParameters = new ArrayList<>();
        when(fetchResultOperation.apply(any())).thenAnswer((Answer) invocation -> {
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

        when(pollOperation.apply(any())).thenReturn(
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

        Assertions.assertNotNull(lroResult);
        Assertions.assertTrue(lroResult.getName().equalsIgnoreCase("LROFinalResult"));
        Assertions.assertNotNull(terminalAsyncResponse[0]);
        Assertions.assertTrue(terminalAsyncResponse[0].getValue().getResponse().equalsIgnoreCase("2"));
        Assertions.assertEquals(1, fetchResultParameters.size());
        Assertions.assertTrue(fetchResultParameters.get(0) instanceof PollingContext);
        PollingContext<Response>  pollingContext = (PollingContext<Response>) fetchResultParameters.get(0);
        pollingContext.getActivationResponse().equals(activationResponse);
        pollingContext.getLatestResponse().equals(response2);
    }


    @Test
    public void syncPollerConstructorPollIntervalZero() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultSyncPoller<>(
            Duration.ZERO,
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultSyncPoller<>(
            Duration.ofSeconds(-1),
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class, () -> new DefaultSyncPoller<>(
            null,
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new DefaultSyncPoller<>(
            Duration.ofSeconds(1),
            null,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class, () -> new DefaultSyncPoller<>(
            Duration.ofSeconds(1),
            activationOperation,
            null,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class, () -> new DefaultSyncPoller<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            null,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class, () -> new DefaultSyncPoller<>(
            Duration.ofSeconds(1),
            activationOperation,
            pollOperation,
            cancelOperation,
            null));
    }

    @Test
    public void syncPollerShouldCallActivationFromConstructor() {
        Boolean[] activationCalled = new Boolean[1];
        activationCalled[0] = false;
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> {
            activationCalled[0] = true;
            return Mono.just(new Response("ActivationDone"));
        }));

        SyncPoller<Response, CertificateOutput> poller = new DefaultSyncPoller<>(
                Duration.ofSeconds(1),
                activationOperation,
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        Assertions.assertTrue(activationCalled[0]);
    }

    @Test
    public void eachPollShouldReceiveLastPollResponse() {
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> Mono.just(new Response("A"))));
        when(pollOperation.apply(any())).thenAnswer((Answer) invocation -> {
            Assertions.assertEquals(1, invocation.getArguments().length);
            Assertions.assertTrue(invocation.getArguments()[0] instanceof PollingContext);
            PollingContext<Response> pollingContext = (PollingContext<Response>) invocation.getArguments()[0];
            Assertions.assertTrue(pollingContext.getActivationResponse() instanceof PollResponse);
            Assertions.assertTrue(pollingContext.getLatestResponse() instanceof PollResponse);
            PollResponse<Response> latestResponse = pollingContext.getLatestResponse();
            Assertions.assertNotNull(latestResponse);
            PollResponse<Response> nextResponse = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                    new Response(latestResponse.getValue().toString() + "A"), Duration.ofMillis(100));
            return Mono.just(nextResponse);
        });

        SyncPoller<Response, CertificateOutput> poller = new DefaultSyncPoller<>(
                Duration.ofSeconds(1),
                activationOperation,
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        PollResponse<Response> pollResponse = poller.poll();
        Assertions.assertNotNull(pollResponse);
        Assertions.assertNotNull(pollResponse.getValue().getResponse());
        Assertions.assertTrue(pollResponse.getValue()
                .getResponse()
                .equalsIgnoreCase("Response: AA"));
        //
        pollResponse = poller.poll();
        Assertions.assertNotNull(pollResponse);
        Assertions.assertNotNull(pollResponse.getValue().getResponse());
        Assertions.assertTrue(pollResponse.getValue()
                .getResponse()
                .equalsIgnoreCase("Response: Response: AAA"));
        //
        pollResponse = poller.poll();
        Assertions.assertNotNull(pollResponse);
        Assertions.assertNotNull(pollResponse.getValue().getResponse());
        Assertions.assertTrue(pollResponse.getValue()
                .getResponse()
                .equalsIgnoreCase("Response: Response: Response: AAAA"));
    }

    @Test
    public void waitForCompletionShouldReturnTerminalPollResponse() {
        PollResponse<Response> response0 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                new Response("0"), Duration.ofMillis(100));

        PollResponse<Response> response1 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                new Response("1"), Duration.ofMillis(100));

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                new Response("2"), Duration.ofMillis(100));

        final Response activationResponse = new Response("Activated");
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

        when(pollOperation.apply(any())).thenReturn(
                Mono.just(response0),
                Mono.just(response1),
                Mono.just(response2));

        SyncPoller<Response, CertificateOutput> poller = new DefaultSyncPoller<>(
                Duration.ofSeconds(1),
                activationOperation,
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        Assertions.assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
    }

    @Test
    public void getResultShouldPollUntilCompletionAndFetchResult() {
        final Response activationResponse = new Response("Activated");
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        when(pollOperation.apply(any())).thenAnswer((Answer<Mono<PollResponse<Response>>>) invocationOnMock -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            new Response("0"), Duration.ofMillis(100)));
                case 1:
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            new Response("1"), Duration.ofMillis(100)));
                case 2:
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            new Response("2"), Duration.ofMillis(100)));
                default:
                    throw new RuntimeException("Poll should not be called after terminal response");
            }
        });

        when(fetchResultOperation.apply(any())).thenReturn(Mono.defer(() -> {
            return Mono.just(new CertificateOutput("cert1"));
        }));

        SyncPoller<Response, CertificateOutput> poller = new DefaultSyncPoller<>(
                Duration.ofSeconds(1),
                activationOperation,
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        Assertions.assertEquals("cert1", certificateOutput.getName());
        Assertions.assertEquals(2, invocationCount[0]);
    }

    @Test
    public void getResultShouldNotPollOnCompletedPoller() {
        PollResponse<Response> response0 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                new Response("0"), Duration.ofMillis(100));

        PollResponse<Response> response1 = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                new Response("1"), Duration.ofMillis(100));

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                new Response("2"), Duration.ofMillis(100));

        final Response activationResponse = new Response("Activated");
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

        when(fetchResultOperation.apply(any())).thenReturn(Mono.defer(() -> {
            return Mono.just(new CertificateOutput("cert1"));
        }));

        when(pollOperation.apply(any())).thenReturn(
                Mono.just(response0),
                Mono.just(response1),
                Mono.just(response2));

        SyncPoller<Response, CertificateOutput> poller = new DefaultSyncPoller<>(
                Duration.ofSeconds(1),
                activationOperation,
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        Assertions.assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
        //
        when(pollOperation.apply(any())).thenAnswer((Answer<Mono<PollResponse<Response>>>) invocationOnMock -> {
            Assertions.assertTrue(true, "A Poll after completion should be called");
            return Mono.empty();
        });
        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        Assertions.assertEquals("cert1", certificateOutput.getName());
    }

    @Test
    public void waitUntilShouldPollAfterMatchingStatus() {
        final Response activationResponse = new Response("Activated");
        when(activationOperation.apply(any())).thenReturn(Mono.defer(() -> Mono.just(activationResponse)));

        LongRunningOperationStatus matchStatus
                = LongRunningOperationStatus.fromString("OTHER_1", false);

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        when(pollOperation.apply(any())).thenAnswer((Answer<Mono<PollResponse<Response>>>) invocationOnMock -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            new Response("0"), Duration.ofMillis(100)));
                case 1:
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            new Response("1"), Duration.ofMillis(100)));
                case 2:
                    return Mono.just(new PollResponse<>(matchStatus,
                            new Response("1"), Duration.ofMillis(100)));
                default:
                    throw new RuntimeException("Poll should not be called after matching response");
            }
        });

        SyncPoller<Response, CertificateOutput> poller = new DefaultSyncPoller<>(
                Duration.ofSeconds(1),
                activationOperation,
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitUntil(matchStatus);
        Assertions.assertEquals(matchStatus, pollResponse.getStatus());
        Assertions.assertEquals(2, invocationCount[0]);
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
