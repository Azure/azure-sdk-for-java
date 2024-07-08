// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.polling.LongRunningOperationStatus.FAILED;
import static com.azure.core.util.polling.LongRunningOperationStatus.IN_PROGRESS;
import static com.azure.core.util.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
import static com.azure.core.util.polling.PollerFlux.create;
import static com.azure.core.util.polling.PollerFlux.error;
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
        assertThrows(NullPointerException.class,
            () -> new PollerFlux<>(Duration.ofSeconds(1), ignored -> null, ignored -> null, null, ignored -> null));
    }

    @Test
    public void asyncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class, () -> new PollerFlux<>(Duration.ofSeconds(1), ignored -> null,
            ignored -> null, (ignored1, ignored2) -> null, null));
    }

    @Test
    public void subscribeToSpecificOtherOperationStatusTest() {
        // Arrange
        final Duration retryAfter = Duration.ofMillis(10);
        //
        PollResponse<TestResponse> response0 = new PollResponse<>(IN_PROGRESS, new TestResponse("0"), retryAfter);
        PollResponse<TestResponse> response1 = new PollResponse<>(IN_PROGRESS, new TestResponse("1"), retryAfter);
        PollResponse<TestResponse> response2 = new PollResponse<>(
            LongRunningOperationStatus.fromString("OTHER_1", false), new TestResponse("2"), retryAfter);
        PollResponse<TestResponse> response3 = new PollResponse<>(
            LongRunningOperationStatus.fromString("OTHER_2", false), new TestResponse("3"), retryAfter);
        PollResponse<TestResponse> response4
            = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("4"), retryAfter);

        Function<PollingContext<TestResponse>, TestResponse>> activationOperation = ignored -> null;

        int[] callCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0:
                    return response0);

                case 1:
                    return response1);

                case 2:
                    return response2);

                case 3:
                    return response3);

                case 4:
                    return response4);

                default:
                    return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        // Act
        PollerFlux<TestResponse, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
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
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> activationOperationWithResponse
            = ignored -> Mono.fromCallable(() -> {
                activationCallCount[0]++;
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    new TestResponse("ActivationDone"));
            });

        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> Mono
            .error(new RuntimeException("Polling shouldn't happen for synchronously completed activation."));

        PollerFlux<TestResponse, CertificateOutput> pollerFlux = create(Duration.ofMillis(10),
            activationOperationWithResponse, pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(
                asyncPollResponse -> asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .expectComplete()
            .verify(STEPVERIFIER_TIMEOUT);
        assertEquals(1, activationCallCount[0]);
    }

    @Test
    public void noPollingForSynchronouslyCompletedActivationInSyncPollerTest() {
        int[] activationCallCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> activationOperationWithResponse
            = ignored -> Mono.fromCallable(() -> {
                activationCallCount[0]++;
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    new TestResponse("ActivationDone"));
            });

        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> Mono
            .error(new RuntimeException("Polling shouldn't happen for synchronously completed activation."));

        SyncPoller<TestResponse, CertificateOutput> syncPoller
            = create(Duration.ofMillis(10), activationOperationWithResponse, pollOperation,
                (ignored1, ignored2) -> null, ignored -> (CertificateOutput>) null).getSyncPoller();

        try {
            PollResponse<TestResponse> response = syncPoller.waitForCompletion(Duration.ofSeconds(1));
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
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> activationOperationWithResponse
            = ignored -> Mono.fromCallable(() -> {
                activationCallCount[0]++;
                return new PollResponse<>(IN_PROGRESS, new TestResponse("ActivationDone"));
            });

        PollResponse<TestResponse> response0 = new PollResponse<>(IN_PROGRESS, new TestResponse("0"), retryAfter);
        PollResponse<TestResponse> response1 = new PollResponse<>(IN_PROGRESS, new TestResponse("1"), retryAfter);
        PollResponse<TestResponse> response2 = new PollResponse<>(
            LongRunningOperationStatus.fromString("OTHER_1", false), new TestResponse("2"), retryAfter);
        PollResponse<TestResponse> response3
            = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("3"), retryAfter);

        int[] callCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0:
                    return response0);

                case 1:
                    return response1);

                case 2:
                    return response2);

                case 3:
                    return response3);

                default:
                    return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        PollerFlux<TestResponse, CertificateOutput> pollerFlux = create(Duration.ofMillis(10),
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

        PollResponse<TestResponse> response0 = new PollResponse<>(IN_PROGRESS, new TestResponse("0"), retryAfter);
        PollResponse<TestResponse> response1 = new PollResponse<>(IN_PROGRESS, new TestResponse("1"), retryAfter);
        PollResponse<TestResponse> response2
            = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("2"), retryAfter);

        int[] activationCallCount = new int[1];
        Function<PollingContext<TestResponse>, TestResponse>> activationOperation
            = ignored -> Mono.fromCallable(() -> {
                activationCallCount[0]++;
                return new TestResponse("ActivationDone");
            });

        int[] pollCallCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            switch (pollCallCount[0]++) {
                case 0:
                    return response0);

                case 1:
                    return response1);

                case 2:
                    return response2);

                default:
                    return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        PollerFlux<TestResponse, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
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

        PollResponse<TestResponse> response0 = new PollResponse<>(IN_PROGRESS, new TestResponse("0"), retryAfter);
        PollResponse<TestResponse> response1 = new PollResponse<>(IN_PROGRESS, new TestResponse("1"), retryAfter);
        PollResponse<TestResponse> response2
            = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("2"), retryAfter);

        final TestResponse activationResponse = new TestResponse("Foo");
        Function<PollingContext<TestResponse>, TestResponse>> activationOperation
            = ignored -> activationResponse);

        int[] callCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0:
                    return response0);

                case 1:
                    return response1);

                case 2:
                    return response2);

                default:
                    return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        final List<Object> cancelParameters = new ArrayList<>();
        BiFunction<PollingContext<TestResponse>, PollResponse<TestResponse>, TestResponse>> cancelOperation
            = (pollingContext, pollResponse) -> {
                Collections.addAll(cancelParameters, pollingContext, pollResponse);
                return new TestResponse("OperationCancelled"));
            };

        PollerFlux<TestResponse, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, cancelOperation, ignored -> null);

        AtomicReference<AsyncPollResponse<TestResponse, CertificateOutput>> secondAsyncResponse
            = new AtomicReference<>();
        //
        TestResponse cancelResponse = pollerFlux.take(2).last().flatMap(asyncPollResponse -> {
            secondAsyncResponse.set(asyncPollResponse);
            return asyncPollResponse.cancelOperation();
        }).block();

        Assertions.assertNotNull(cancelResponse);
        Assertions.assertTrue(cancelResponse.getResponse().equalsIgnoreCase("OperationCancelled"));
        Assertions.assertNotNull(secondAsyncResponse.get());
        Assertions.assertEquals("1", secondAsyncResponse.get().getValue().getResponse());
        assertEquals(2, cancelParameters.size());
        assertEquals(activationResponse,
            ((PollingContext<?>) cancelParameters.get(0)).getActivationResponse().getValue());
        assertEquals(activationResponse, ((PollResponse<?>) cancelParameters.get(1)).getValue());
    }

    @Test
    public void getResultCanBeCalledFromOperatorChainTest() {
        final Duration retryAfter = Duration.ofMillis(10);

        PollResponse<TestResponse> response2
            = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("2"), retryAfter);
        final TestResponse activationResponse = new TestResponse("Foo");

        Function<PollingContext<TestResponse>, TestResponse>> activationOperation
            = ignored -> activationResponse);

        int[] callCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            switch (callCount[0]++) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), retryAfter));

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), retryAfter));

                case 2:
                    return response2);

                default:
                    return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        final List<PollingContext<TestResponse>> fetchResultParameters = new ArrayList<>();
        Function<PollingContext<TestResponse>, CertificateOutput>> fetchResultOperation = pollingContext -> {
            fetchResultParameters.add(pollingContext);
            return new CertificateOutput("LROFinalResult"));
        };

        PollerFlux<TestResponse, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
            activationOperation, pollOperation, (ignored1, ignored2) -> null, fetchResultOperation);

        AtomicReference<AsyncPollResponse<TestResponse, CertificateOutput>> terminalAsyncResponse
            = new AtomicReference<>();
        //
        CertificateOutput lroResult = pollerFlux.takeUntil(apr -> apr.getStatus().isComplete())
            .last()
            .flatMap(
                (Function<AsyncPollResponse<TestResponse, CertificateOutput>, CertificateOutput>>) asyncPollResponse -> {
                    terminalAsyncResponse.set(asyncPollResponse);
                    return asyncPollResponse.getFinalResult();
                })
            .block();

        Assertions.assertNotNull(lroResult);
        Assertions.assertTrue(lroResult.getName().equalsIgnoreCase("LROFinalResult"));
        Assertions.assertNotNull(terminalAsyncResponse.get());
        Assertions.assertTrue(terminalAsyncResponse.get().getValue().getResponse().equalsIgnoreCase("2"));
        assertEquals(1, fetchResultParameters.size());
        PollingContext<TestResponse> pollingContext = fetchResultParameters.get(0);
        assertEquals(activationResponse, pollingContext.getActivationResponse().getValue());
        assertEquals(response2, pollingContext.getLatestResponse());
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperation() {
        final TestResponse activationResponse = new TestResponse("Foo");
        Function<PollingContext<TestResponse>, TestResponse>> activationOperation
            = ignored -> activationResponse);

        final AtomicInteger cnt = new AtomicInteger();
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = (pollingContext) -> {
            int count = cnt.incrementAndGet();
            if (count <= 2) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("1")));
            } else if (count == 3) {
                return Mono.error(new RuntimeException("Polling operation failed!"));
            } else if (count == 4) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("2")));
            } else {
                return new PollResponse<>(SUCCESSFULLY_COMPLETED, new TestResponse("3")));
            }
        };

        PollerFlux<TestResponse, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
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
        final TestResponse activationResponse = new TestResponse("Foo");
        Function<PollingContext<TestResponse>, TestResponse>> activationOperation
            = ignored -> activationResponse);

        final AtomicInteger cnt = new AtomicInteger();
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = (pollingContext) -> {
            int count = cnt.incrementAndGet();
            if (count <= 2) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("1")));
            } else if (count == 3) {
                return new PollResponse<>(FAILED, new TestResponse("2")));
            } else if (count == 4) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("3")));
            } else {
                return new PollResponse<>(SUCCESSFULLY_COMPLETED, new TestResponse("4")));
            }
        };

        PollerFlux<TestResponse, CertificateOutput> pollerFlux = new PollerFlux<>(Duration.ofMillis(10),
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
        assertThrows(IllegalArgumentException.class,
            () -> new SyncOverAsyncPoller<>(Duration.ZERO,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(-1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class,
            () -> new SyncOverAsyncPoller<>(null,
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
        assertThrows(NullPointerException.class,
            () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null, null,
                ignored -> null));
    }

    @Test
    public void syncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SyncOverAsyncPoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, null));
    }

    @Test
    public void syncPollerShouldCallActivationFromConstructor() {
        Boolean[] activationCalled = new Boolean[1];
        activationCalled[0] = false;
        Function<PollingContext<TestResponse>, TestResponse>> activationOperation
            = ignored -> Mono.fromCallable(() -> {
                activationCalled[0] = true;
                return new TestResponse("ActivationDone");
            });

        SyncPoller<TestResponse, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block()),
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null);

        Assertions.assertTrue(activationCalled[0]);
    }

    @Test
    public void eachPollShouldReceiveLastPollResponse() {
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = pollingContext -> {
            Assertions.assertNotNull(pollingContext.getActivationResponse());
            Assertions.assertNotNull(pollingContext.getLatestResponse());
            PollResponse<TestResponse> latestResponse = pollingContext.getLatestResponse();
            Assertions.assertNotNull(latestResponse);
            return new PollResponse<>(IN_PROGRESS,
                new TestResponse(latestResponse.getValue().toString() + "A"), Duration.ofMillis(10)));
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new TestResponse("A")), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.poll();
        Assertions.assertNotNull(pollResponse);
        Assertions.assertNotNull(pollResponse.getValue().getResponse());
        Assertions.assertTrue(pollResponse.getValue().getResponse().equalsIgnoreCase("Response: AA"));
        //
        pollResponse = poller.poll();
        Assertions.assertNotNull(pollResponse);
        Assertions.assertNotNull(pollResponse.getValue().getResponse());
        Assertions.assertTrue(pollResponse.getValue().getResponse().equalsIgnoreCase("Response: Response: AAA"));
        //
        pollResponse = poller.poll();
        Assertions.assertNotNull(pollResponse);
        Assertions.assertNotNull(pollResponse.getValue().getResponse());
        Assertions
            .assertTrue(pollResponse.getValue().getResponse().equalsIgnoreCase("Response: Response: Response: AAAA"));
    }

    @Test
    public void waitForCompletionShouldReturnTerminalPollResponse() {
        PollResponse<TestResponse> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new TestResponse("2"), Duration.ofMillis(10));

        final TestResponse activationResponse = new TestResponse("Activated");

        int[] pollCallCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            switch (pollCallCount[0]++) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), Duration.ofMillis(10)));

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), Duration.ofMillis(10)));

                case 2:
                    return response2);

                default:
                    return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
    }

    @Test
    public void getResultShouldPollUntilCompletionAndFetchResult() {
        final TestResponse activationResponse = new TestResponse("Activated");

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), Duration.ofMillis(10)));

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), Duration.ofMillis(10)));

                case 2:
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        new TestResponse("2"), Duration.ofMillis(10)));

                default:
                    return Mono.error(new RuntimeException("Poll should not be called after terminal response"));
            }
        };

        Function<PollingContext<TestResponse>, CertificateOutput>> fetchResultOperation
            = ignored -> new CertificateOutput("cert1"));

        SyncPoller<TestResponse, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, fetchResultOperation);

        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        assertEquals("cert1", certificateOutput.getName());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void getResultShouldNotPollOnCompletedPoller() {
        PollResponse<TestResponse> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new TestResponse("2"), Duration.ofMillis(10));

        final TestResponse activationResponse = new TestResponse("Activated");

        Function<PollingContext<TestResponse>, CertificateOutput>> fetchResultOperation
            = ignored -> new CertificateOutput("cert1"));

        int[] pollCallCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            switch (pollCallCount[0]++) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), Duration.ofMillis(10)));

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), Duration.ofMillis(10)));

                case 2:
                    return response2);

                default:
                    return Mono.error(new IllegalStateException("Too many requests"));
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, fetchResultOperation);

        PollResponse<TestResponse> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());

        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        assertEquals("cert1", certificateOutput.getName());
    }

    @Test
    public void waitUntilShouldPollAfterMatchingStatus() {
        final TestResponse activationResponse = new TestResponse("Activated");

        LongRunningOperationStatus matchStatus = LongRunningOperationStatus.fromString("OTHER_1", false);

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), Duration.ofMillis(10)));

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), Duration.ofMillis(10)));

                case 2:
                    return new PollResponse<>(matchStatus, new TestResponse("1"), Duration.ofMillis(10)));

                default:
                    return Mono.error(new RuntimeException("Poll should not be called after matching response"));
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperationSyncPoller() {
        final TestResponse activationResponse = new TestResponse("Foo");

        final AtomicInteger cnt = new AtomicInteger();
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>>> pollOperation = (pollingContext) -> {
            int count = cnt.incrementAndGet();
            if (count <= 2) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("1")));
            } else if (count == 3) {
                return Mono.error(new RuntimeException("Polling operation failed!"));
            } else if (count == 4) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("2")));
            } else {
                return new PollResponse<>(SUCCESSFULLY_COMPLETED, new TestResponse("3")));
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SyncOverAsyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class, poller::getFinalResult);
        assertEquals(exception.getMessage(), "Polling operation failed!");
    }

    @Test
    public void testPollerFluxError() throws InterruptedException {
        IllegalArgumentException expectedException = new IllegalArgumentException();
        PollerFlux<String, String> pollerFlux = error(expectedException);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        pollerFlux.subscribe(response -> Assertions.fail("Did not expect a response"), ex -> {
            countDownLatch.countDown();
            Assertions.assertSame(expectedException, ex);
        }, () -> Assertions.fail("Did not expect the flux to complete"));
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
            context -> new PollResponse<>(IN_PROGRESS, "Activation")),
            context -> new PollResponse<>(IN_PROGRESS, "PollOperation")),
            (context, response) -> "Cancel"), context -> "FinalResult"));

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
            context -> new PollResponse<>(IN_PROGRESS, "Activation")),
            context -> new PollResponse<>(IN_PROGRESS, "PollOperation")),
            (context, response) -> "Cancel"), context -> "FinalResult"));

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
}
