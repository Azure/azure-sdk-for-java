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

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.polling.LongRunningOperationStatus.*;
import static com.azure.core.util.polling.PollerFlux.error;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class SimpleSyncPollerTests {
    @Mock
    private Function<PollingContext<Response>, Response> activationOperation;

    @Mock
    private Function<PollingContext<Response>, PollResponse<Response>> activationOperationWithResponse;

    @Mock
    private Function<PollingContext<Response>, PollResponse<Response>> pollOperation;

    @Mock
    private Function<PollingContext<Response>, CertificateOutput> fetchResultOperation;

    @Mock
    private BiFunction<PollingContext<Response>, PollResponse<Response>, Response> cancelOperation;

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
    public void noPollingForSynchronouslyCompletedActivationInSyncPollerTest() {
        int[] activationCallCount = new int[1];
        when(activationOperationWithResponse.apply(any()))
            .thenAnswer((Answer<PollResponse<Response>>) invocationOnMock -> {
                activationCallCount[0]++;
                return new PollResponse<>(SUCCESSFULLY_COMPLETED,
                    new Response("ActivationDone"));
            });

        SyncPoller<Response, CertificateOutput> syncPoller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            activationOperationWithResponse,
            pollOperation,
            cancelOperation,
            fetchResultOperation);

        when(pollOperation.apply(any())).thenThrow(
            new RuntimeException("Polling shouldn't happen for synchronously completed activation."));

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
        assertThrows(IllegalArgumentException.class, () -> new SimpleSyncPoller<>(
            Duration.ZERO,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorPollIntervalNegative() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleSyncPoller<>(
            Duration.ofSeconds(-1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorPollIntervalNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(
            null,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncConstructorActivationOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(
            Duration.ofSeconds(1),
            null,
            pollOperation,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorPollOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(
            Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
            null,
            cancelOperation,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorCancelOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(
            Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
            pollOperation,
            null,
            fetchResultOperation));
    }

    @Test
    public void syncPollerConstructorFetchResultOperationNull() {
        assertThrows(NullPointerException.class, () -> new SimpleSyncPoller<>(
            Duration.ofSeconds(1),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
            pollOperation,
            cancelOperation,
            null));
    }

    @Test
    public void syncPollerShouldCallActivationFromConstructor() {
        Boolean[] activationCalled = new Boolean[1];
        activationCalled[0] = false;
        when(activationOperation.apply(any())).thenAnswer((Answer<Response>) invocationOnMock -> {
            activationCalled[0] = true;
            return new Response("ActivationDone");
        });

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        Assertions.assertTrue(activationCalled[0]);
    }

    @Test
    public void eachPollShouldReceiveLastPollResponse() {
        when(activationOperation.apply(any())).thenReturn(new Response("A"));
        when(pollOperation.apply(any())).thenAnswer((Answer<?>) invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Assertions.assertTrue(invocation.getArguments()[0] instanceof PollingContext);
            PollingContext<Response> pollingContext = (PollingContext<Response>) invocation.getArguments()[0];
            Assertions.assertNotNull(pollingContext.getActivationResponse());
            Assertions.assertNotNull(pollingContext.getLatestResponse());
            PollResponse<Response> latestResponse = pollingContext.getLatestResponse();
            Assertions.assertNotNull(latestResponse);
            PollResponse<Response> nextResponse = new PollResponse<>(IN_PROGRESS,
                    new Response(latestResponse.getValue().toString() + "A"), Duration.ofMillis(10));
            return nextResponse;
        });

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(
                Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
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
        PollResponse<Response> response0 = new PollResponse<>(IN_PROGRESS,
                new Response("0"), Duration.ofMillis(10));

        PollResponse<Response> response1 = new PollResponse<>(IN_PROGRESS,
                new Response("1"), Duration.ofMillis(10));

        PollResponse<Response> response2 = new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                new Response("2"), Duration.ofMillis(10));

        final Response activationResponse = new Response("Activated");
        when(activationOperation.apply(any())).thenReturn(activationResponse);

        when(pollOperation.apply(any())).thenReturn(
                response0,
                response1,
                response2);

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(
                Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
    }

    @Test
    public void getResultShouldPollUntilCompletionAndFetchResult() {
        final Response activationResponse = new Response("Activated");
        when(activationOperation.apply(any())).thenReturn(activationResponse);

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        when(pollOperation.apply(any())).thenAnswer((Answer<PollResponse<Response>>) invocationOnMock -> {
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
        });

        when(fetchResultOperation.apply(any())).thenReturn(new CertificateOutput("cert1"));

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(
                Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
                pollOperation,
                cancelOperation,
                fetchResultOperation);

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
        when(activationOperation.apply(any())).thenReturn(activationResponse);

        when(fetchResultOperation.apply(any())).thenReturn(new CertificateOutput("cert1"));

        when(pollOperation.apply(any())).thenReturn(
                response0,
                response1,
                response2);

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(
                Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitForCompletion();
        Assertions.assertNotNull(pollResponse.getValue());
        assertEquals(response2.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
        //
        when(pollOperation.apply(any())).thenAnswer((Answer<PollResponse<Response>>) invocationOnMock -> {
            Assertions.assertTrue(true, "A Poll after completion should be called");
            return null;
        });
        CertificateOutput certificateOutput = poller.getFinalResult();
        Assertions.assertNotNull(certificateOutput);
        assertEquals("cert1", certificateOutput.getName());
    }

    @Test
    public void waitUntilShouldPollAfterMatchingStatus() {
        final Response activationResponse = new Response("Activated");
        when(activationOperation.apply(any())).thenReturn(activationResponse);

        LongRunningOperationStatus matchStatus
                = LongRunningOperationStatus.fromString("OTHER_1", false);

        int[] invocationCount = new int[1];
        invocationCount[0] = -1;
        //
        when(pollOperation.apply(any())).thenAnswer((Answer<PollResponse<Response>>) invocationOnMock -> {
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
                            new Response("1"), Duration.ofMillis(10));
                default:
                    throw new RuntimeException("Poll should not be called after matching response");
            }
        });

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(
                Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
                pollOperation,
                cancelOperation,
                fetchResultOperation);

        PollResponse<Response> pollResponse = poller.waitUntil(matchStatus);
        assertEquals(matchStatus, pollResponse.getStatus());
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void verifyExceptionPropagationFromPollingOperationSyncPoller() {
        final Response activationResponse = new Response("Foo");
        when(activationOperation.apply(any())).thenReturn(activationResponse);

        final AtomicReference<Integer> cnt = new AtomicReference<>(0);
        pollOperation = (pollingContext) -> {
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

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(
            Duration.ofMillis(10),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.apply(cxt)),
            pollOperation,
            cancelOperation,
            fetchResultOperation);

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
