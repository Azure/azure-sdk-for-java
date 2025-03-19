// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.v2.core.http.polling.LongRunningOperationStatus.IN_PROGRESS;
import static com.azure.v2.core.http.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SimplePollerTests {
    private static final Duration TEN_MILLIS = Duration.ofMillis(10);

    @Test
    public void noPollingForSynchronouslyCompletedActivationInPollerTest() {
        int[] activationCallCount = new int[1];
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> activationOperationWithResponse
            = ignored -> {
                activationCallCount[0]++;
                return new PollResponse<>(SUCCESSFULLY_COMPLETED, new TestResponse("ActivationDone"));
            };

        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation = ignored -> {
            throw new RuntimeException("Polling shouldn't happen for synchronously completed activation.");
        };

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS, activationOperationWithResponse,
            pollOperation, (ignored1, ignore2) -> null, ignored -> null);

        try {
            PollResponse<TestResponse> response = poller.waitForCompletion(Duration.ofSeconds(1));
            assertEquals(SUCCESSFULLY_COMPLETED, response.getStatus());
            assertEquals(1, activationCallCount[0]);
        } catch (Exception e) {
            fail("Poller did not complete on activation", e);
        }
    }

    @Test
    public void pollerConstructorPollIntervalZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new SimplePoller<>(Duration.ZERO,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void pollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> new SimplePoller<>(Duration.ofSeconds(-1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void pollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class,
            () -> new SimplePoller<>(null, cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null),
                ignored -> null, (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimplePoller<>(Duration.ofSeconds(1), null, ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void pollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SimplePoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), null,
                (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void pollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SimplePoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null, null,
                ignored -> null));
    }

    @Test
    public void pollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class,
            () -> new SimplePoller<>(Duration.ofSeconds(1),
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
                (ignored1, ignored2) -> null, null));
    }

    @Test
    public void pollerShouldCallActivationFromConstructor() {
        boolean[] activationCalled = new boolean[1];
        Function<PollingContext<TestResponse>, TestResponse> activationOperation = ignored -> {
            activationCalled[0] = true;
            return new TestResponse("ActivationDone");
        };

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
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

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
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

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
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

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
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

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
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

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperationPoller() {
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

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class, poller::getFinalResult);
        assertTrue(exception.getMessage().contains("Polling operation failed!"));
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

        Poller<TestResponse, CertificateOutput> poller = new SimplePoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<TestResponse> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(matchStatus, poller.waitUntil(matchStatus).getStatus());
        assertEquals(2, invocationCount[0]);
    }
}
