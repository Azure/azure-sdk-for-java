// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.polling;

import org.junit.jupiter.api.Test;
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
    private static final Duration FIVE_SECONDS = Duration.ofSeconds(5);

    private static final PollResponse<TestResponse> ACTIVATION_RESPONSE
        = new PollResponse<>(NOT_STARTED, new TestResponse("Activated"));
    private static final PollResponse<TestResponse> RESPONSE_ZERO
        = new PollResponse<>(IN_PROGRESS, new TestResponse("0"));
    private static final PollResponse<TestResponse> RESPONSE_ONE
        = new PollResponse<>(IN_PROGRESS, new TestResponse("1"));

    private static final Function<PollingContext<TestResponse>, PollResponse<TestResponse>> SYNC_NEVER_COMPLETES =
        ignored -> sleepThenReturn(10000, RESPONSE_ONE);

    private static final Function<PollingContext<TestResponse>, Mono<PollResponse<TestResponse>>> ASYNC_NEVER_COMPLETES
        = ignored -> Mono.delay(Duration.ofSeconds(10)).map(ignored2 -> RESPONSE_ZERO);

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @Test
    public void simpleSyncWaitForCompletionSinglePollTimesOut() {
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(SYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::waitForCompletion);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @Test
    public void simpleSyncWaitForCompletionOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::waitForCompletion, hasBeenRan);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the operation timeout period but is less than the wait timeout.
     */
    @Test
    public void simpleSyncWaitForCompletionOperationTimesOutIsLessThanWaitTimeout() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start < FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out before the wait timeout.");
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the wait timeout period and is larger than the operation timeout.
     */
    @Test
    public void simpleSyncWaitForCompletionWaitTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsMoreThanOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start >= FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out due to the wait timeout. Waited for " + (end - start) + "ms.");
    }

    /**
     * Tests that the last response is returned if a single poll takes longer than the timeout period.
     */
    @Test
    public void simpleSyncWaitUntilSinglePollTimesOut() {
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(SYNC_NEVER_COMPLETES);

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), ACTIVATION_RESPONSE.getValue());
    }

    /**
     * Tests that the last response is returned if the polling operation doesn't reach the {@code statusToWaitFor}
     * within the timeout period.
     */
    @Test
    public void simpleSyncWaitUntilOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), hasBeenRan,
            RESPONSE_ZERO.getValue());
    }

    /**
     * Tests that the last response is returned if the polling operation doesn't reach the {@code statusToWaitFor}
     * within the operation timeout period but is less than the wait timeout.
     */
    @Test
    public void simpleSyncWaitUntilOperationTimesOutIsLessThanWaitTimeout() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        PollResponse<TestResponse> pollResponse = assertDoesNotThrow(
            () -> poller.waitUntil(HUNDRED_MILLIS, FIVE_SECONDS, SUCCESSFULLY_COMPLETED));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertEquals(RESPONSE_ZERO.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertTrue(end - start < FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out before the wait timeout.");
    }

    /**
     * Tests that the last response is returned if the polling operation doesn't reach the {@code statusToWaitFor}
     * within the wait timeout period but is larger than the operation timeout.
     */
    @Test
    public void simpleSyncWaitUntilWaitTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsMoreThanOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        PollResponse<TestResponse> pollResponse = assertDoesNotThrow(
            () -> poller.waitUntil(HUNDRED_MILLIS, FIVE_SECONDS, SUCCESSFULLY_COMPLETED));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertEquals(RESPONSE_ONE.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertTrue(end - start >= FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out due to the wait timeout. Waited for " + (end - start) + "ms.");
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @Test
    public void simpleSyncGetFinalResultSinglePollTimesOut() {
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(SYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::getFinalResult);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the operation timeout period but is less than the wait timeout.
     */
    @Test
    public void simpleSyncGetFinalResultOperationTimesOutIsLessThanWaitTimeout() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.getFinalResult(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start < FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out before the wait timeout.");
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the wait timeout period and is larger than the operation timeout.
     */
    @Test
    public void simpleSyncGetFinalResultWaitTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsMoreThanOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start >= FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out due to the wait timeout. Waited for " + (end - start) + "ms.");
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @Test
    public void simpleSyncGetFinalResultOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSimplePoller(syncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::getFinalResult, hasBeenRan);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @Test
    public void syncOverAsyncWaitForCompletionSinglePollTimesOut() {
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(ASYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::waitForCompletion);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @Test
    public void syncOverAsyncWaitForCompletionOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::waitForCompletion, hasBeenRan);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the operation timeout period but is less than the wait timeout.
     */
    @Test
    public void syncOverAsyncWaitForCompletionOperationTimesOutIsLessThanWaitTimeout() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start < FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out before the wait timeout.");
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the wait timeout period and is larger than the operation timeout.
     */
    @Test
    public void syncOverAsyncWaitForCompletionWaitTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(
            asyncRunsMoreThanOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start >= FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out due to the wait timeout. Waited for " + (end - start) + "ms.");
    }

    /**
     * Tests that the last response is returned if a single poll takes longer than the timeout period.
     */
    @Test
    public void syncOverAsyncWaitUntilSinglePollTimesOut() {
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(ASYNC_NEVER_COMPLETES);

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), ACTIVATION_RESPONSE.getValue());
    }

    /**
     * Tests that the last response is returned if the polling operation doesn't reach the {@code statusToWaitFor}
     * within the timeout period.
     */
    @Test
    public void syncOverAsyncWaitUntilOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        assertReturns(timeout -> poller.waitUntil(timeout, SUCCESSFULLY_COMPLETED), hasBeenRan,
            RESPONSE_ZERO.getValue());
    }

    /**
     * Tests that the last response is returned if the polling operation doesn't reach the {@code statusToWaitFor}
     * within the timeout period but is less than the wait timeout.
     */
    @Test
    public void syncOverAsyncWaitUntilOperationTimesOutIsLessThanWaitTimeout() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        PollResponse<TestResponse> pollResponse = assertDoesNotThrow(
            () -> poller.waitUntil(HUNDRED_MILLIS, FIVE_SECONDS, SUCCESSFULLY_COMPLETED));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertEquals(RESPONSE_ZERO.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertTrue(end - start < FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out before the wait timeout.");
    }

    /**
     * Tests that the last response is returned if the polling operation doesn't reach the {@code statusToWaitFor}
     * within the wait timeout period but is larger than the operation timeout.
     */
    @Test
    public void syncOverAsyncWaitUntilWaitTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(
            asyncRunsMoreThanOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        PollResponse<TestResponse> pollResponse = assertDoesNotThrow(
            () -> poller.waitUntil(HUNDRED_MILLIS, FIVE_SECONDS, SUCCESSFULLY_COMPLETED));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertEquals(RESPONSE_ONE.getValue().getResponse(), pollResponse.getValue().getResponse());
        assertTrue(end - start >= FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out due to the wait timeout. Waited for " + (end - start) + "ms.");
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @Test
    public void syncOverAsyncGetFinalResultSinglePollTimesOut() {
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(ASYNC_NEVER_COMPLETES);

        assertTimeoutException(poller::getFinalResult);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @Test
    public void syncOverAsyncGetFinalResultOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        assertTimeoutException(poller::getFinalResult, hasBeenRan);
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the operation timeout period but is less than the wait timeout.
     */
    @Test
    public void syncOverAsyncGetFinalResultOperationTimesOutIsLessThanWaitTimeout() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(asyncRunsOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.getFinalResult(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start < FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out before the wait timeout.");
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the wait timeout period and is larger than the operation timeout.
     */
    @Test
    public void syncOverAsyncGetFinalResultWaitTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        SyncPoller<TestResponse, CertificateOutput> poller = createSyncOverAsyncPoller(
            asyncRunsMoreThanOnce(hasBeenRan));

        long start = System.currentTimeMillis();
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.getFinalResult(HUNDRED_MILLIS, FIVE_SECONDS));
        long end = System.currentTimeMillis();

        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
        assertTrue(end - start >= FIVE_SECONDS.toMillis(),
            "Expected the operation to have timed out due to the wait timeout. Waited for " + (end - start) + "ms.");
    }

    private static SyncPoller<TestResponse, CertificateOutput> createSimplePoller(
        Function<PollingContext<TestResponse>, PollResponse<TestResponse>> pollOperation) {
        return new SimpleSyncPoller<>(TEN_MILLIS, cxt -> ACTIVATION_RESPONSE, pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);
    }

    private static SyncPoller<TestResponse, CertificateOutput> createSyncOverAsyncPoller(
        Function<PollingContext<TestResponse>, Mono<PollResponse<TestResponse>>> pollOperation) {
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

    private static void assertReturns(Function<Duration, PollResponse<TestResponse>> polling, TestResponse expected) {
        assertReturns(polling, null, expected);
    }

    private static void assertReturns(Function<Duration, PollResponse<TestResponse>> polling, AtomicBoolean hasBeenRan,
        TestResponse expected) {
        PollResponse<TestResponse> pollResponse = assertDoesNotThrow(() -> polling.apply(HUNDRED_MILLIS));

        if (hasBeenRan != null) {
            assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        }

        assertEquals(expected.getResponse(), pollResponse.getValue().getResponse());
    }

    private static Function<PollingContext<TestResponse>, PollResponse<TestResponse>> syncRunsOnce(
        AtomicBoolean hasBeenRan) {
        return ignored -> hasBeenRan.compareAndSet(false, true) ? RESPONSE_ZERO : sleepThenReturn(10000, RESPONSE_ONE);
    }

    private static Function<PollingContext<TestResponse>, PollResponse<TestResponse>> syncRunsMoreThanOnce(
        AtomicBoolean hasBeenRan) {
        return ignored -> hasBeenRan.compareAndSet(false, true) ? RESPONSE_ZERO : sleepThenReturn(10, RESPONSE_ONE);
    }

    private static Function<PollingContext<TestResponse>, Mono<PollResponse<TestResponse>>> asyncRunsOnce(
        AtomicBoolean hasBeenRan) {
        return ignored -> hasBeenRan.compareAndSet(false, true) ? Mono.just(RESPONSE_ZERO)
            : Mono.delay(Duration.ofSeconds(10)).map(ignored2 -> RESPONSE_ONE);
    }

    private static Function<PollingContext<TestResponse>, Mono<PollResponse<TestResponse>>> asyncRunsMoreThanOnce(
        AtomicBoolean hasBeenRan) {
        return ignored -> hasBeenRan.compareAndSet(false, true) ? Mono.just(RESPONSE_ZERO)
            : Mono.delay(TEN_MILLIS).map(ignored2 -> RESPONSE_ONE);
    }

    private static String printException(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private static <T> T sleepThenReturn(long sleepMillis, T returnValue) {
        try {
            Thread.sleep(sleepMillis);
            return returnValue;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
