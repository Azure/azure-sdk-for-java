// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.polling;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.azure.core.util.polling.LongRunningOperationStatus.IN_PROGRESS;
import static com.azure.core.util.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Tests require making calls on new threads, running in parallel can cause flakiness issues.")
public class PollingWithTimeoutTests {
    private static final Duration TEN_MILLIS = Duration.ofMillis(10);

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitForCompletionSinglePollTimesOut() {
        final Response activationResponse = new Response("Activated");

        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            sleep(10000);
            return new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS);
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(TEN_MILLIS));
        assertInstanceOf(TimeoutException.class, exception.getCause());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitForCompletionOperationTimesOut() {
        final Response activationResponse = new Response("Activated");

        AtomicBoolean hasBeenRan = new AtomicBoolean();
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            if (hasBeenRan.compareAndSet(false, true)) {
                return new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS);
            } else {
                sleep(10000);
                return new PollResponse<>(IN_PROGRESS, new Response("1"), TEN_MILLIS);
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(TEN_MILLIS));
        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitUntilSinglePollTimesOut() {
        final Response activationResponse = new Response("Activated");

        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            sleep(10000);
            return new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS);
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = poller.waitUntil(TEN_MILLIS, SUCCESSFULLY_COMPLETED);
        assertEquals(activationResponse.getResponse(), pollResponse.getValue().getResponse());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't reach the {@code statusToWaitFor} within the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncWaitUntilOperationTimesOut() {
        AtomicBoolean hasBeenRan = new AtomicBoolean();
        PollResponse<Response> expected = new PollResponse<>(IN_PROGRESS, new Response("0"), null);
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            if (hasBeenRan.compareAndSet(false, true)) {
                return expected;
            } else {
                sleep(10000);
                return new PollResponse<>(IN_PROGRESS, new Response("1"), TEN_MILLIS);
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new Response("Activated")), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = assertDoesNotThrow(() -> poller.waitUntil(TEN_MILLIS,
            SUCCESSFULLY_COMPLETED));
        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertEquals(expected.getValue().getResponse(), pollResponse.getValue().getResponse());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncGetFinalResultSinglePollTimesOut() {
        final Response activationResponse = new Response("Activated");

        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            sleep(10000);
            return new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS);
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.getFinalResult(TEN_MILLIS));
        assertInstanceOf(TimeoutException.class, exception.getCause());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void simpleSyncGetFinalResultOperationTimesOut() {
        final Response activationResponse = new Response("Activated");

        AtomicBoolean hasBeenRan = new AtomicBoolean();
        Function<PollingContext<Response>, PollResponse<Response>> pollOperation = ignored -> {
            if (hasBeenRan.compareAndSet(false, true)) {
                return new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS);
            } else {
                sleep(10000);
                return new PollResponse<>(IN_PROGRESS, new Response("1"), TEN_MILLIS);
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SimpleSyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.getFinalResult(TEN_MILLIS));
        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause());
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitForCompletionSinglePollTimesOut() {
        final Response activationResponse = new Response("Activated");

        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored ->
            Mono.delay(Duration.ofSeconds(10))
                .map(ignored2 -> new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS));

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(TEN_MILLIS));
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitForCompletionOperationTimesOut() {
        final Response activationResponse = new Response("Activated");

        AtomicBoolean hasBeenRan = new AtomicBoolean();
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored ->
            (hasBeenRan.compareAndSet(false, true))
                ? Mono.just(new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS))
                : Mono.delay(Duration.ofSeconds(10)).map(ignored2 -> new PollResponse<>(IN_PROGRESS, new Response("1"),
                    TEN_MILLIS));

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.waitForCompletion(TEN_MILLIS));
        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitUntilSinglePollTimesOut() {
        final Response activationResponse = new Response("Activated");

        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored ->
            Mono.delay(Duration.ofSeconds(10))
                .map(ignored2 -> new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS));

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = poller.waitUntil(Duration.ofMillis(1), SUCCESSFULLY_COMPLETED);
        assertEquals(activationResponse.getResponse(), pollResponse.getValue().getResponse());
    }

    /**
     * Tests that the last received PollResponse is used when waitUtil times out.
     */
    @RepeatedTest(100)
    public void syncOverAsyncWaitUntilOperationTimesOut() {
        final Response activationResponse = new Response("Activated");

        AtomicBoolean hasBeenRan = new AtomicBoolean();
        PollResponse<Response> expected = new PollResponse<>(IN_PROGRESS, new Response("0"), null);
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            if (hasBeenRan.compareAndSet(false, true)) {
                return Mono.just(expected);
            } else {
                return Mono.delay(Duration.ofSeconds(10))
                    .map(ignored2 -> new PollResponse<>(IN_PROGRESS, new Response("1"), TEN_MILLIS));
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        PollResponse<Response> pollResponse = assertDoesNotThrow(() -> poller.waitUntil(TEN_MILLIS,
            SUCCESSFULLY_COMPLETED));
        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertEquals(expected.getValue().getResponse(), pollResponse.getValue().getResponse());
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if a single poll takes longer
     * than the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncGetFinalResultSinglePollTimesOut() {
        final Response activationResponse = new Response("Activated");

        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored ->
            Mono.delay(Duration.ofSeconds(10))
                .map(ignored2 -> new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS));

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.getFinalResult(TEN_MILLIS));
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
    }

    /**
     * Tests that a {@link RuntimeException} wrapping a {@link TimeoutException} is thrown if the polling operation
     * doesn't complete within the timeout period.
     */
    @RepeatedTest(100)
    public void syncOverAsyncGetFinalResultOperationTimesOut() {
        final Response activationResponse = new Response("Activated");

        AtomicBoolean hasBeenRan = new AtomicBoolean();
        Function<PollingContext<Response>, Mono<PollResponse<Response>>> pollOperation = ignored -> {
            if (hasBeenRan.compareAndSet(false, true)) {
                return Mono.just(new PollResponse<>(IN_PROGRESS, new Response("0"), TEN_MILLIS));
            } else {
                return Mono.delay(Duration.ofSeconds(10))
                    .map(ignored2 -> new PollResponse<>(IN_PROGRESS, new Response("1"), TEN_MILLIS));
            }
        };

        SyncPoller<Response, CertificateOutput> poller = new SyncOverAsyncPoller<>(TEN_MILLIS,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResponse), pollOperation,
            (ignored1, ignored2) -> null, ignored -> null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> poller.getFinalResult(TEN_MILLIS));
        assertTrue(hasBeenRan.get(), "Expected poll operation to have been ran at least once.");
        assertInstanceOf(TimeoutException.class, exception.getCause(), () -> printException(exception));
    }

    private static String printException(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
