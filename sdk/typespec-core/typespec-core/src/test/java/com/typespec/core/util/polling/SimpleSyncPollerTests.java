// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.typespec.core.util.polling.LongRunningOperationStatus.IN_PROGRESS;
import static com.typespec.core.util.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
import static com.typespec.core.util.polling.PollerFlux.error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SimpleSyncPollerTests {

    @Test
    public void noPollingForSynchronouslyCompletedActivationInSyncPollerTest() {
        int[] activationCallCount = new int[1];
        Function<PollingContext<Response>, PollResponse<Response>> activationOperationWithResponse = ignored -> {
            activationCallCount[0]++;
            return new PollResponse<>(SUCCESSFULLY_COMPLETED, new Response("ActivationDone"));
        };

        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            throw new RuntimeException("Polling shouldn't happen for synchronously completed activation.");
        };

        SyncPoller<Response, CertificateOutput> syncPoller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            activationOperationWithResponse, pollOperation, (ignored1, ignore2) -> null, ignored -> null);

        try {
            PollResponse<Response> response = syncPoller.waitForCompletion(Duration.ofSeconds(1));
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
            assertEquals(1, activationCallCount[0]);
        } catch (Exception e) {
            fail("SyncPoller did not complete on activation", e);
        }
    }

    @Test
    public void syncPollerConstructorPollIntervalZero() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleSyncPoller<>(Duration.ZERO,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleSyncPoller<>(Duration.ofSeconds(-1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(null,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(Duration.ofSeconds(1), null,
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null));
    }

    @Test
    public void syncPollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), null, (ignored1, ignored2) -> null,
            ignored -> null));
    }

    @Test
    public void syncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null, null,
            ignored -> null));
    }

    @Test
    public void syncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null), ignored -> null,
            (ignored1, ignored2) -> null, null));
    }

    @Test
    public void syncPollerShouldCallActivationFromConstructor() {
        boolean[] activationCalled = new boolean[1];
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response>
            activationOperation
            = ignored -> {
                activationCalled[0] = true;
                return new Response("ActivationDone");
            };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            ignored -> null, (ignored1, ignored2) -> null, ignored -> null);

        Assertions.assertTrue(activationCalled[0]);
    }

    @Test
    public void eachPollShouldReceiveLastPollResponse() {
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response> activationOperation
            = ignored -> new Response("A");

        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = pollingContext -> {
            Assertions.assertNotNull(pollingContext.getActivationResponse());
            Assertions.assertNotNull(pollingContext.getLatestResponse());
            PollResponse<Response> latestResponse = pollingContext.getLatestResponse();
            Assertions.assertNotNull(latestResponse);
            return new PollResponse<>(IN_PROGRESS, new Response(latestResponse.getValue().toString() + "A"),
                Duration.ofMillis(10));
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
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
        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS,
            new Response("0"), Duration.ofMillis(10));

        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS,
            new Response("1"), Duration.ofMillis(10));

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), Duration.ofMillis(10));

        final Response activationResponse = new Response("Activated");
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response> activationOperation
            = ignored -> activationResponse;

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0: return response0;
                case 1: return response1;
                case 2: return response2;
                default: throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(response2.getValue().getResponse(), poller.waitForCompletion().getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
    }

    @Test
    public void getResultShouldPollUntilCompletionAndFetchResult() {
        final Response activationResponse = new Response("Activated");
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response> activationOperation
            = ignored -> activationResponse;

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS,
                        new Response("0"), Duration.ofMillis(10));
                case 1:
                    return new PollResponse<>(IN_PROGRESS,
                        new Response("1"), Duration.ofMillis(10));
                case 2:
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        new Response("2"), Duration.ofMillis(10));
                default:
                    throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        Function<PollingContext<Response>, CertificateOutput> fetchResultOperation
            = ignored -> new CertificateOutput("cert1");

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            pollOperation, (ignored1, ignored2) -> null, fetchResultOperation);

        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        assertEquals("cert1", certificateOutput.getName());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void getResultShouldNotPollOnCompletedPoller() {
        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS,
            new Response("0"), Duration.ofMillis(10));

        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS,
            new Response("1"), Duration.ofMillis(10));

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new Response("2"), Duration.ofMillis(10));

        final Response activationResponse = new Response("Activated");
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response> activationOperation
            = ignored -> activationResponse;

        Function<PollingContext<Response>, CertificateOutput> fetchResultOperation
            = ignored -> new CertificateOutput("cert1");

        int[] invocationCount = new int[] { -1 };
        //
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0: return response0;
                case 1: return response1;
                case 2: return response2;
                default: throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            pollOperation, (ignored1, ignored2) -> null, fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
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
        final Response activationResponse = new Response("Activated");
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response> activationOperation
            = ignored -> activationResponse;

        LongRunningOperationStatus matchStatus
            = LongRunningOperationStatus.fromString("OTHER_1", false);

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0: return new PollResponse<>(IN_PROGRESS, new Response("0"), Duration.ofMillis(10));
                case 1: return new PollResponse<>(IN_PROGRESS, new Response("1"), Duration.ofMillis(10));
                case 2: return new PollResponse<>(matchStatus, new Response("1"), Duration.ofMillis(10));
                default: throw new RuntimeException("Poll should not be called after terminal response");
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperationSyncPoller() {
        final Response activationResponse = new Response("Foo");
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response> activationOperation
            = ignored -> activationResponse;

        final AtomicReference<Integer> cnt = new AtomicReference<>(0);
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            cnt.getAndSet(cnt.get() + 1);
            if (cnt.get() <= 2) {
                return new PollResponse<>(IN_PROGRESS, new Response("1"));
            } else if (cnt.get() == 3) {
                throw new RuntimeException("Polling operation failed!");
            } else if (cnt.get() == 4) {
                return new PollResponse<>(IN_PROGRESS, new Response("2"));
            } else {
                return new PollResponse<>(SUCCESSFULLY_COMPLETED, new Response("3"));
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class, poller::getFinalResult);
        assertTrue(exception.getMessage().contains("Polling operation failed!"));
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
    public void waitUntilShouldPollToCompletion() {
        final Response activationResponse = new Response("Activated");
        Function<PollingContext<SimpleSyncPollerTests.Response>, SimpleSyncPollerTests.Response> activationOperation
            = ignored -> activationResponse;

        LongRunningOperationStatus matchStatus = SUCCESSFULLY_COMPLETED;

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            invocationCount[0]++;
            switch (invocationCount[0]) {
                case 0:
                    return new PollResponse<>(IN_PROGRESS,
                        new Response("0"), Duration.ofMillis(10));
                case 1:
                    return new PollResponse<>(IN_PROGRESS,
                        new Response("1"), Duration.ofMillis(10));
                case 2:
                    return new PollResponse<>(matchStatus,
                        new Response("2"), Duration.ofMillis(10));
                default:
                    throw new RuntimeException("Poll should not be called after matching response");
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt)),
            pollOperation, (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(matchStatus, poller.waitUntil(matchStatus).getStatus());
        assertEquals(2, invocationCount[0]);
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
