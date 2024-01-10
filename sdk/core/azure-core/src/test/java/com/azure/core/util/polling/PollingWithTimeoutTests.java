// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.polling;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.core.util.polling.LongRunningOperationStatus.IN_PROGRESS;
import static com.azure.core.util.polling.LongRunningOperationStatus.NOT_STARTED;
import static com.azure.core.util.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Tests require making calls on new threads, running in parallel can cause flakiness issues.")
public class PollingWithTimeoutTests {
    private static final Duration TEN_MILLIS = Duration.ofMillis(10);
    private static final Duration HUNDRED_MILLIS = Duration.ofMillis(100);

    private static final PollResponse<Response> ACTIVATION_RESPONSE
        = new PollResponse<>(NOT_STARTED, new Response("Activated"));
    private static final PollResponse<Response> RESPONSE_ZERO = new PollResponse<>(IN_PROGRESS, new Response("0"));
    private static final PollResponse<Response> RESPONSE_ONE = new PollResponse<>(IN_PROGRESS, new Response("1"));

    private static final Function<PollingContext<Response>, PollResponse<Response>> SYNC_NEVER_COMPLETES = ignored -> {
        sleep();
        return RESPONSE_ZERO;
    };

    private static final Function<PollingContext<Response>, Mono<PollResponse<Response>>> ASYNC_NEVER_COMPLETES
        = ignored -> Mono.delay(Duration.ofSeconds(10)).map(ignored2 -> RESPONSE_ZERO);

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitForCompletionSinglePollTimesOut() {
        SyncPoller<Response, CertificateOutput> poller = createSimplePoller(SYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::waitForCompletion);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitForCompletionOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<Response, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::waitForCompletion, hasBeenRan);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitUntilSinglePollTimesOut() {
        SyncPoller<Response, CertificateOutput> poller = createSimplePoller(SYNC_NEVER_COMPLETES);

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), ACTIVATION_RESPONSE.getValue());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't reach the {@code statusToWaitFor} within the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitUntilOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<Response, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), hasBeenRan,
            RESPONSE_ZERO.getValue());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncGetFinalResultSinglePollTimesOut() {
        SyncPoller<Response, CertificateOutput> poller = createSimplePoller(SYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::getFinalResult);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncGetFinalResultOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<Response, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::getFinalResult, hasBeenRan);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitForCompletionSinglePollTimesOut() {
        SyncPoller<Response, CertificateOutput> poller = createSyncOverAsyncPoller(ASYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::waitForCompletion);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitForCompletionOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<Response, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::waitForCompletion, hasBeenRan);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitUntilSinglePollTimesOut() {
        SyncPoller<Response, CertificateOutput> poller = createSyncOverAsyncPoller(ASYNC_NEVER_COMPLETES);

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), ACTIVATION_RESPONSE.getValue());
    }

    /**
     * Tests that the last received PollResponse is used when waitUtil times out.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitUntilOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<Response, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), hasBeenRan,
            RESPONSE_ZERO.getValue());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncGetFinalResultSinglePollTimesOut() {
        SyncPoller<Response, CertificateOutput> poller = createSyncOverAsyncPoller(ASYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::getFinalResult);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncGetFinalResultOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<Response, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::getFinalResult, hasBeenRan);
    }

    private static SyncPoller<Response, CertificateOutput> createSimplePoller(
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation) {
        return new SimpleSyncPoller<>(TEN_MILLIS, cxt -> ACTIVATION_RESPONSE, pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);
    }

    private static SyncPoller<Response, CertificateOutput> createSyncOverAsyncPoller(
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation) {
        return new SyncOverAsyncPoller<>(TEN_MILLIS, cxt -> ACTIVATION_RESPONSE, pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);
    }

    private static void assertTimeoutException(Consumer<Duration> polling) {
        assertTimeoutException(polling, null);
    }

    private static void assertTimeoutException(Consumer<Duration> polling, AtomicBoolean hasBeenRan) {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> polling.accept(HUNDRED_MILLIS));

        if (hasBeenRan != null) {
            assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        }

        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
    }

    private static void assertReturns(Function<Duration, PollResponse<Response>> polling, Response expected) {
        assertReturns(polling, null, expected);
    }

    private static void assertReturns(Function<Duration, PollResponse<Response>> polling, AtomicBoolean hasBeenRan,
        Response expected) {
        PollResponse<Response> pollResponse = assertDoesNotThrow(() -> polling.apply(HUNDRED_MILLIS));

        if (hasBeenRan != null) {
            assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        }

        assertEquals(expected.getResponse(), pollResponse.getValue().getResponse());
    }

    private static Function<PollingContext<Response>, PollResponse<Response>> syncRunsOnce(AtomicBoolean hasBeenRan) {
        return ignored -> {
            if (hasBeenRan.compareAndSet(false, true)) {
                return RESPONSE_ZERO;
            } else {
                sleep();
                return RESPONSE_ONE;
            }
        };
    }

    private static Function<PollingContext<Response>, Mono<PollResponse<Response>>> asyncRunsOnce(
        AtomicBoolean hasBeenRan) {
        return ignored -> (hasBeenRan.compareAndSet(false, true))
            ? Mono.just(RESPONSE_ZERO) : Mono.delay(Duration.ofSeconds(10)).map(ignored2 -> RESPONSE_ONE);
    }

    private static String printException(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private static void sleep() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
