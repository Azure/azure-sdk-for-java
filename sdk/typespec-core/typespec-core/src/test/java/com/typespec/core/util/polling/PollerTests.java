// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.typespec.core.util.polling.LongRunningOperationStatus.FAILED;
import static com.typespec.core.util.polling.LongRunningOperationStatus.IN_PROGRESS;
import static com.typespec.core.util.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
import static com.typespec.core.util.polling.PollerFlux.create;
import static com.typespec.core.util.polling.PollerFlux.error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class PollerTests {
    private static final Duration STEPVERIFIER_TIMEOUT = Duration.ofSeconds(30);

    @Test
    public void asyncPollerConstructorPollIntervalZero() {
        assertThrows(IllegalArgumentException.class, () -> new PollerFlux<>(Duration.ZERO, ignored -> null,
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void asyncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class, () -> new PollerFlux<>(Duration.ofSeconds(-1), ignored -> null,
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void asyncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(null, ignored -> null, ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void asyncPollerConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(Duration.ofSeconds(1), null, ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void asyncPollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(Duration.ofSeconds(1), ignored -> null, null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void asyncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(Duration.ofSeconds(1), ignored -> null,
            ignored -> null, null, ignored -> null));
    }

    @Test
    public void asyncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(Duration.ofSeconds(1), ignored -> null,
            ignored -> null, (ignored1, ignored2) -> null,
            null));
    }

    @Test
    public void subscribeToSpecificOtherOperationStatusTest() {
        // Arrange
        final Duration retryAfter = Duration.ofMillis(10);
        //
        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS, new Response("0"), retryAfter);
        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS, new Response("1"), retryAfter);
        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.fromString("OTHER_1", false),
            new Response("2"), retryAfter);
        PollResponse<Response> response3 = new PollResponse<>(LongRunningOperationStatus.fromString("OTHER_2", false),
            new Response("3"), retryAfter);
        PollResponse<Response> response4 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("4"), retryAfter);

        Function<PollingContext<Response>, Mono<Response>> activationOperation = ignored -> Mono.empty();

        int[] callCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0: return Mono.just(response0);
                case 1: return Mono.just(response1);
                case 2: return Mono.just(response2);
                case 3: return Mono.just(response3);
                case 4: return Mono.just(response4);
                default: return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        // Act
        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        // Assert
        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response3.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response4.getStatus())
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);
    }

    @Test
    public void noPollingForSynchronouslyCompletedActivationTest() {
        int[] activationCallCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> activationOperationWithResponse
            = ignored -> Mono.fromCallable(() -> {
                activationCallCount[0]++;
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    new Response("ActivationDone"));
            });

        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored ->
            Mono.error(new RuntimeException("Polling shouldn't happen for synchronously completed activation."));

        PollerFlux<Response, CertificateOutput> pollerFlux = create(Duration.ofMillis(10),
            activationOperationWithResponse, pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus()
                == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void noPollingForSynchronouslyCompletedActivationInSyncPollerTest() {
        int[] activationCallCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> activationOperationWithResponse
            = ignored -> Mono.fromCallable(() -> {
                activationCallCount[0]++;
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    new Response("ActivationDone"));
            });

        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored ->
            Mono.error(new RuntimeException("Polling shouldn't happen for synchronously completed activation."));

        SyncPoller<Response, CertificateOutput> syncPoller = create(Duration.ofMillis(10),
            activationOperationWithResponse, pollOperation, (ignored1, ignored2) -> null,
            ignored -> (Mono<CertificateOutput>) null)
            .getSyncPoller();

        try {
            PollResponse<Response> response = syncPoller.waitForCompletion(Duration.ofSeconds(1));
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
            assertEquals(1, activationCallCount[0]);
        } catch (Exception e) {
            fail("SyncPoller did not complete on activation", e);
        }
    }

    @Test
    public void ensurePollingForInProgressActivationResponseTest() {
        final Duration retryAfter = Duration.ofMillis(10);

        int[] activationCallCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> activationOperationWithResponse
            = ignored -> Mono.fromCallable(() -> {
                activationCallCount[0]++;
                return new PollResponse<>(IN_PROGRESS, new Response("ActivationDone"));
            });

        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS, new Response("0"), retryAfter);
        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS, new Response("1"), retryAfter);
        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.fromString("OTHER_1", false),
            new Response("2"), retryAfter);
        PollResponse<Response> response3 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("3"), retryAfter);

        int[] callCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0: return Mono.just(response0);
                case 1: return Mono.just(response1);
                case 2: return Mono.just(response2);
                case 3: return Mono.just(response3);
                default: return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        PollerFlux<Response, CertificateOutput> pollerFlux = create(Duration.ofMillis(10),
            activationOperationWithResponse, pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .assertNext(asyncPollResponse -> assertEquals(response0.getStatus(), asyncPollResponse.getStatus()))
            .assertNext(asyncPollResponse -> assertEquals(response1.getStatus(), asyncPollResponse.getStatus()))
            .assertNext(asyncPollResponse -> assertEquals(response2.getStatus(), asyncPollResponse.getStatus()))
            .assertNext(asyncPollResponse -> assertEquals(response3.getStatus(), asyncPollResponse.getStatus()))
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void subscribeToActivationOnlyOnceTest() {
        // Arrange
        final Duration retryAfter = Duration.ofMillis(10);

        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS, new Response("0"), retryAfter);
        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS, new Response("1"), retryAfter);
        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), retryAfter);

        int[] activationCallCount = new int[1];
        Function<PollingContext<Response>, Mono<Response>> activationOperation = ignored -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new Response("ActivationDone");
        });

        int[] pollCallCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            switch (pollCallCount[0]++) {
                case 0: return Mono.just(response0);
                case 1: return Mono.just(response1);
                case 2: return Mono.just(response2);
                default: return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);

        pollCallCount[0] = 0;

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response0.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response1.getStatus())
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == response2.getStatus())
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);

        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void cancellationCanBeCalledFromOperatorChainTest() {
        final Duration retryAfter = Duration.ofMillis(10);

        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS, new Response("0"), retryAfter);
        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS, new Response("1"), retryAfter);
        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), retryAfter);

        final Response activationResponse = new Response("Foo");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        int[] callCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0: return Mono.just(response0);
                case 1: return Mono.just(response1);
                case 2: return Mono.just(response2);
                default: return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        final List<Object> cancelParameters = new ArrayList<>();
        BiFunction<PollingContext<Response>, PollResponse<Response>, Mono<Response>> cancelOperation
            = (pollingContext, pollResponse) -> {
                Collections.addAll(cancelParameters, pollingContext, pollResponse);
                return Mono.just(new Response("OperationCancelled"));
            };

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, cancelOperation, ignored -> null);

        AtomicReference<AsyncPollResponse<Response, CertificateOutput>> secondAsyncResponse = new AtomicReference<>();
        //
        Response cancelResponse = pollerFlux
            .take(2)
            .last()
            .flatMap((Function<AsyncPollResponse<Response, CertificateOutput>, Mono<Response>>) asyncPollResponse -> {
                secondAsyncResponse.set(asyncPollResponse);
                return asyncPollResponse.cancelOperation();
            }).block();

        Assertions.assertNotNull(cancelResponse);
        Assertions.assertTrue(cancelResponse.getResponse().equalsIgnoreCase("OperationCancelled"));
        Assertions.assertNotNull(secondAsyncResponse.get());
        Assertions.assertEquals("1", secondAsyncResponse.get().getValue().getResponse());
        assertEquals(2, cancelParameters.size());
        assertEquals(activationResponse, ((PollingContext<?>) cancelParameters.get(0)).getActivationResponse()
            .getValue());
        assertEquals(activationResponse, ((PollResponse<?>) cancelParameters.get(1)).getValue());
    }

    @Test
    public void getResultCanBeCalledFromOperatorChainTest() {
        final Duration retryAfter = Duration.ofMillis(10);

        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS, new Response("0"), retryAfter);
        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS, new Response("1"), retryAfter);
        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), retryAfter);
        final Response activationResponse = new Response("Foo");

        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        int[] callCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0: return Mono.just(response0);
                case 1: return Mono.just(response1);
                case 2: return Mono.just(response2);
                default: return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        final List<PollingContext<Response>> fetchResultParameters = new ArrayList<>();
        Function<PollingContext<Response>, Mono<CertificateOutput>> fetchResultOperation = pollingContext -> {
            fetchResultParameters.add(pollingContext);
            return Mono.just(new CertificateOutput("LROFinalResult"));
        };

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, (ignored1, ignored2) -> null, fetchResultOperation);

        AtomicReference<AsyncPollResponse<Response, CertificateOutput>> terminalAsyncResponse = new AtomicReference<>();
        //
        CertificateOutput lroResult = pollerFlux
            .takeUntil(apr -> apr.getStatus().isComplete())
            .last()
            .flatMap((Function<AsyncPollResponse<Response, CertificateOutput>, Mono<CertificateOutput>>)
                asyncPollResponse -> {
                    terminalAsyncResponse.set(asyncPollResponse);
                    return asyncPollResponse.getFinalResult();
                }).block();

        Assertions.assertNotNull(lroResult);
        Assertions.assertTrue(lroResult.getName().equalsIgnoreCase("LROFinalResult"));
        Assertions.assertNotNull(terminalAsyncResponse.get());
        Assertions.assertTrue(terminalAsyncResponse.get().getValue().getResponse().equalsIgnoreCase("2"));
        assertEquals(1, fetchResultParameters.size());
        PollingContext<Response> pollingContext = fetchResultParameters.get(0);
        assertEquals(activationResponse, pollingContext.getActivationResponse().getValue());
        assertEquals(response2, pollingContext.getLatestResponse());
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperation() {
        final Response activationResponse = new Response("Foo");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        final AtomicReference<Integer> cnt = new AtomicReference<>(0);
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = (pollingContext) -> {
            cnt.getAndSet(cnt.get() + 1);
            if (cnt.get() <= 2) {
                return Mono.just(new PollResponse<>(IN_PROGRESS, new Response("1")));
            } else if (cnt.get() == 3) {
                throw new RuntimeException("Polling operation failed!");
            } else if (cnt.get() == 4) {
                return Mono.just(new PollResponse<>(IN_PROGRESS, new Response("2")));
            } else {
                return Mono.just(new PollResponse<>(SUCCESSFULLY_COMPLETED, new Response("3")));
            }
        };

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == IN_PROGRESS)
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == IN_PROGRESS)
            .expectErrorMessage("Polling operation failed!")
            .verify(STEPVERIFIER_TIMEOUT);
    }

    @Test
    public void verifyErrorFromPollingOperation() {
        final Response activationResponse = new Response("Foo");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        final AtomicReference<Integer> cnt = new AtomicReference<>(0);
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = (pollingContext) -> {
            cnt.getAndSet(cnt.get() + 1);
            if (cnt.get() <= 2) {
                return Mono.just(new PollResponse<>(IN_PROGRESS, new Response("1")));
            } else if (cnt.get() == 3) {
                return Mono.just(new PollResponse<>(FAILED, new Response("2")));
            } else if (cnt.get() == 4) {
                return Mono.just(new PollResponse<>(IN_PROGRESS, new Response("3")));
            } else {
                return Mono.just(new PollResponse<>(SUCCESSFULLY_COMPLETED, new Response("4")));
            }
        };

        PollerFlux<Response, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == IN_PROGRESS)
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == IN_PROGRESS)
            .expectNextMatches(asyncPollResponse -> asyncPollResponse.getStatus() == FAILED)
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);
    }

    @Test
    public void syncPollerConstructorPollIntervalZero() {
        assertThrows(IllegalArgumentException.class, () -> new SyncOverAsyncPoller<>(Duration.ZERO,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class, () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(-1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class, () -> new SyncOverAsyncPoller<>(null,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(1), null,
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class, () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class, () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null, null,
            ignored -> null));
    }

    @Test
    public void syncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class, () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, null));
    }

    @Test
    public void syncPollerShouldCallActivationFromConstructor() {
        Boolean[] activationCalled = new Boolean[1];
        activationCalled[0] = false;
        Function<PollingContext<Response>, Mono<Response>> activationOperation = ignored -> Mono.fromCallable(() -> {
            activationCalled[0] = true;
            return new Response("ActivationDone");
        });

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null);

        Assertions.assertTrue(activationCalled[0]);
    }

    @Test
    public void eachPollShouldReceiveLastPollResponse() {
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(new Response("A"));

        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = pollingContext -> {
            Assertions.assertNotNull(pollingContext.getActivationResponse());
            Assertions.assertNotNull(pollingContext.getLatestResponse());
            PollResponse<Response> latestResponse = pollingContext.getLatestResponse();
            Assertions.assertNotNull(latestResponse);
            return Mono.just(new PollResponse<>(IN_PROGRESS,
                new Response(latestResponse.getValue().toString() + "A"), Duration.ofMillis(10)));
        };

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

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
        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS, new Response("0"), Duration.ofMillis(10));
        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS, new Response("1"), Duration.ofMillis(10));
        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), Duration.ofMillis(10));

        final Response activationResponse = new Response("Activated");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        int[] pollCallCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            switch (pollCallCount[0]++) {
                case 0: return Mono.just(response0);
                case 1: return Mono.just(response1);
                case 2: return Mono.just(response2);
                default: return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
    }

    @Test
    public void getResultShouldPollUntilCompletionAndFetchResult() {
        final Response activationResponse = new Response("Activated");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return Mono.just(new PollResponse<>(IN_PROGRESS,
                        new Response("0"), Duration.ofMillis(10)));
                case 1:
                    return Mono.just(new PollResponse<>(IN_PROGRESS,
                        new Response("1"), Duration.ofMillis(10)));
                case 2:
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        new Response("2"), Duration.ofMillis(10)));
                default:
                    throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        Function<PollingContext<Response>, Mono<CertificateOutput>> fetchResultOperation
            = ignored -> Mono.just(new CertificateOutput("cert1"));

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            pollOperation, (ignored1, ignored2) -> null, fetchResultOperation);

        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        assertEquals("cert1", certificateOutput.getName());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void getResultShouldNotPollOnCompletedPoller() {
        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS, new Response("0"), Duration.ofMillis(10));
        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS, new Response("1"), Duration.ofMillis(10));
        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), Duration.ofMillis(10));

        final Response activationResponse = new Response("Activated");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        Function<PollingContext<Response>, Mono<CertificateOutput>> fetchResultOperation
            = ignored -> Mono.just(new CertificateOutput("cert1"));

        int[] pollCallCount = new int[1];
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            switch (pollCallCount[0]++) {
                case 0: return Mono.just(response0);
                case 1: return Mono.just(response1);
                case 2: return Mono.just(response2);
                default: return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            pollOperation, (ignored1, ignored2) -> null, fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());

        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        assertEquals("cert1", certificateOutput.getName());
    }

    @Test
    public void waitUntilShouldPollAfterMatchingStatus() {
        final Response activationResponse = new Response("Activated");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        LongRunningOperationStatus matchStatus
            = LongRunningOperationStatus.fromString("OTHER_1", false);

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return Mono.just(new PollResponse<>(IN_PROGRESS,
                        new Response("0"), Duration.ofMillis(10)));
                case 1:
                    return Mono.just(new PollResponse<>(IN_PROGRESS,
                        new Response("1"), Duration.ofMillis(10)));
                case 2:
                    return Mono.just(new PollResponse<>(matchStatus,
                        new Response("1"), Duration.ofMillis(10)));
                default:
                    throw new RuntimeException("Poll should not be called after matching response");
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperationSyncPoller() {
        final Response activationResponse = new Response("Foo");
        Function<PollingContext<Response>, Mono<Response>> activationOperation
            = ignored -> Mono.just(activationResponse);

        final AtomicReference<Integer> cnt = new AtomicReference<>(0);
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = (pollingContext) -> {
            cnt.getAndSet(cnt.get() + 1);
            if (cnt.get() <= 2) {
                return Mono.just(new PollResponse<>(IN_PROGRESS, new Response("1")));
            } else if (cnt.get() == 3) {
                throw new RuntimeException("Polling operation failed!");
            } else if (cnt.get() == 4) {
                return Mono.just(new PollResponse<>(IN_PROGRESS, new Response("2")));
            } else {
                return Mono.just(new PollResponse<>(SUCCESSFULLY_COMPLETED, new Response("3")));
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class, poller::getFinalResult);
        assertEquals(exception.getMessage(), "Polling operation failed!");
    }

    @Test
    public void testPollerFluxError() throws InterruptedException {
        IllegalArgumentException expectedException = new IllegalArgumentException();
        PollerFlux<String, String> pollerFlux = error(expectedException);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        pollerFlux.subscribe(
            response -> Assertions.fail("Did not expect a response"),
            ex -> {
                countDownLatch.countDown();
                Assertions.assertSame(expectedException, ex);
            },
            () -> Assertions.fail("Did not expect the flux to complete")
        );
        boolean completed = countDownLatch.await(1, TimeUnit.SECONDS);
        Assertions.assertTrue(completed);
    }

    @Test
    public void testSyncPollerError() {
        PollerFlux<String, String> pollerFlux = error(new IllegalArgumentException());
        // should getSyncPoller() be lazy?
        Assertions.assertThrows(IllegalArgumentException.class, pollerFlux::getSyncPoller);
    }

    @Test
    public void testUpdatePollingIntervalWithoutVirtualTimer() {
        PollerFlux<String, String> pollerFlux = PollerFlux.create(Duration.ofMillis(10),
            context -> Mono.just(new PollResponse<>(IN_PROGRESS, "Activation")),
            context -> Mono.just(new PollResponse<>(IN_PROGRESS, "PollOperation")),
            (context, response) -> Mono.just("Cancel"),
            context -> Mono.just("FinalResult"));

        pollerFlux.setPollInterval(Duration.ofMillis(200));
        StepVerifier.create(pollerFlux.take(5))
            .thenAwait(Duration.ofSeconds(1))
            .expectNextCount(5)
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);
    }

    @Test
    public void testUpdatePollingInterval() {
        PollerFlux<String, String> pollerFlux = PollerFlux.create(Duration.ofMillis(10),
            context -> Mono.just(new PollResponse<>(IN_PROGRESS, "Activation")),
            context -> Mono.just(new PollResponse<>(IN_PROGRESS, "PollOperation")),
            (context, response) -> Mono.just("Cancel"),
            context -> Mono.just("FinalResult"));

        StepVerifier.create(pollerFlux.take(5))
            .thenAwait(Duration.ofMillis(55))
            .expectNextCount(5)
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);

        pollerFlux.setPollInterval(Duration.ofMillis(50));
        StepVerifier.create(pollerFlux.take(5))
            .thenAwait(Duration.ofMillis(255))
            .expectNextCount(5)
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);

        pollerFlux.setPollInterval(Duration.ofMillis(195));
        StepVerifier.create(pollerFlux.take(5))
            .thenAwait(Duration.ofSeconds(1))
            .expectNextCount(5)
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);
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

    public static class CertificateOutput {
        String name;

        public CertificateOutput(String certName) {
            name = certName;
        }

        public String getName() {
            return name;
        }
    }
}
