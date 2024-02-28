// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.core.util.polling.LongRunningOperationStatus.IN_PROGRESS;
import static com.azure.core.util.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
import static com.azure.core.util.polling.PollerFlux.error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SimpleSyncPollerTests {
    private static final Duration TEN_MILLIS = Duration.ofMillis(10);

    @Test
    public void noPollingForSynchronouslyCompletedActivationInSyncPollerTest() {
        int[] activationCallCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> activationOperationWithResponse
            = ignored -> {
                activationCallCount[0]++;
                return new PollResponse<>(SUCCESSFULLY_COMPLETED, new TestResponse("ActivationDone"));
            };

        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            throw new RuntimeException("Polling shouldn't happen for synchronously completed activation.");
        };

        SyncPoller<TestResponse, CertificateOutput> syncPoller = new SimpleSyncPoller<>(TEN_MILLIS,
            activationOperationWithResponse, pollOperation, (ignored1, ignore2) -> null, ignored -> null);

        try {
            PollResponse<TestResponse> response = syncPoller.waitForCompletion(Duration.ofSeconds(1));
            assertEquals(SUCCESSFULLY_COMPLETED, response.getStatus());
            assertEquals(1, activationCallCount[0]);
        } catch (Exception e) {
            fail("SyncPoller did not complete on activation", e);
        }
    }

    @Test
    public void syncPollerConstructorPollIntervalZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new SimpleSyncPoller<>(Duration.ZERO,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> new SimpleSyncPoller<>(Duration.ofSeconds(-1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class,
            () -> new SimpleSyncPoller<>(null, cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null),
                ignored -> null, (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(Duration.ofSeconds(1), null,
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SimpleSyncPoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SimpleSyncPoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null, null,
                ignored -> null));
    }

    @Test
    public void syncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SimpleSyncPoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, null));
    }

    @Test
    public void syncPollerShouldCallActivationFromConstructor() {
        boolean[] activationCalled = new boolean[1];
        Function<PollingContext<TestResponse>, TestResponse> activationOperation = ignored -> {
            activationCalled[0] = true;
            return new TestResponse("ActivationDone");
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null);

        Assertions.assertTrue(activationCalled[0]);
    }

    @Test
    public void eachPollShouldReceiveLastPollResponse() {
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = pollingContext -> {
            Assertions.assertNotNull(pollingContext.getActivationResponse());
            Assertions.assertNotNull(pollingContext.getLatestResponse());
            PollResponse<TestResponse> latestResponse = pollingContext.getLatestResponse();
            Assertions.assertNotNull(latestResponse);
            return new PollResponse<>(IN_PROGRESS, new TestResponse(latestResponse.getValue().toString() + "A"),
                TEN_MILLIS);
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
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
        PollResponse<TestResponse> response2
            = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("2"), TEN_MILLIS);

        final TestResponse activationResponse = new TestResponse("Activated");

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), TEN_MILLIS);

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), TEN_MILLIS);

                case 2:
                    return response2;

                default:
                    throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(response2.getValue().getResponse(), poller.waitForCompletion().getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
    }

    @Test
    public void getResultShouldPollUntilCompletionAndFetchResult() {
        final TestResponse activationResponse = new TestResponse("Activated");

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), TEN_MILLIS);

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), TEN_MILLIS);

                case 2:
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("2"),
                        TEN_MILLIS);

                default:
                    throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        Function<PollingContext<TestResponse>, CertificateOutput> fetchResultOperation
            = ignored -> new CertificateOutput("cert1");

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, fetchResultOperation);

        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        assertEquals("cert1", certificateOutput.getName());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void getResultShouldNotPollOnCompletedPoller() {
        PollResponse<TestResponse> response2
            = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new TestResponse("2"), TEN_MILLIS);

        final TestResponse activationResponse = new TestResponse("Activated");

        Function<PollingContext<TestResponse>, CertificateOutput> fetchResultOperation
            = ignored -> new CertificateOutput("cert1");

        int[] invocationCount = new int[] { -1 };
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), TEN_MILLIS);

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), TEN_MILLIS);

                case 2:
                    return response2;

                default:
                    throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, fetchResultOperation);

        PollResponse<TestResponse> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
        //
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
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), TEN_MILLIS);

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), TEN_MILLIS);

                case 2:
                    return new PollResponse<>(matchStatus, new TestResponse("1"), TEN_MILLIS);

                default:
                    throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperationSyncPoller() {
        final TestResponse activationResponse = new TestResponse("Foo");

        final AtomicReference<Integer> cnt = new AtomicReference<>(0);
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            cnt.getAndSet(cnt.get() + 1);
            if (cnt.get() <= 2) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("1"));
            } else if (cnt.get() == 3) {
                throw new RuntimeException("Polling operation failed!");
            } else if (cnt.get() == 4) {
                return new PollResponse<>(IN_PROGRESS, new TestResponse("2"));
            } else {
                return new PollResponse<>(SUCCESSFULLY_COMPLETED, new TestResponse("3"));
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class, poller::getFinalResult);
        assertTrue(exception.getMessage().contains("Polling operation failed!"));
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
    public void waitUntilShouldPollToCompletion() {
        final TestResponse activationResponse = new TestResponse("Activated");
        LongRunningOperationStatus matchStatus = SUCCESSFULLY_COMPLETED;

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("0"), TEN_MILLIS);

                case 1:
                    return new PollResponse<>(IN_PROGRESS, new TestResponse("1"), TEN_MILLIS);

                case 2:
                    return new PollResponse<>(SUCCESSFULLY_COMPLETED, new TestResponse("2"), TEN_MILLIS);

                default:
                    throw new RuntimeException("Poll should not be called after matching response");
            }
        };

        SyncPoller<TestResponse, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(matchStatus, poller.waitUntil(matchStatus).getStatus());
        assertEquals(2, invocationCount[0]);
    }
}
