// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample08UpdateAnalyzerAsyncControlFlowTest {
    @Test
    public void successfulResultIsReturned() {
        String result = Sample08_UpdateAnalyzerAsync
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.just("result"), "Test")
            .block();

        assertEquals("result", result);
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean subscribed = new AtomicBoolean();
        Mono<String> finalResult = Mono.just("result").doOnSubscribe(ignored -> subscribed.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample08_UpdateAnalyzerAsync
                .requireSuccessfulResult(LongRunningOperationStatus.FAILED, finalResult, "Test")
                .block());

        assertTrue(exception.getMessage().contains("FAILED"));
        assertFalse(subscribed.get());
    }

    @Test
    public void cancelledStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean subscribed = new AtomicBoolean();
        Mono<String> finalResult = Mono.just("result").doOnSubscribe(ignored -> subscribed.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample08_UpdateAnalyzerAsync
                .requireSuccessfulResult(LongRunningOperationStatus.USER_CANCELLED, finalResult, "Test")
                .block());

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
        assertFalse(subscribed.get());
    }

    @Test
    public void emptyFinalResultIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample08_UpdateAnalyzerAsync
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.empty(), "Test")
                .block());

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void cleanupRunsAfterSuccessfulOperation() {
        AtomicInteger cleanupCalls = new AtomicInteger();

        Sample08_UpdateAnalyzerAsync
            .runWithCleanup(Mono.just("resource"), ignored -> Mono.empty(),
                ignored -> Mono.fromRunnable(cleanupCalls::incrementAndGet))
            .block();

        assertEquals(1, cleanupCalls.get());
    }

    @Test
    public void cleanupRunsAndOperationFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");
        AtomicInteger cleanupCalls = new AtomicInteger();

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample08_UpdateAnalyzerAsync
                .runWithCleanup(Mono.just("resource"), ignored -> Mono.error(expected),
                    ignored -> Mono.fromRunnable(cleanupCalls::incrementAndGet))
                .block());

        assertSame(expected, actual);
        assertEquals(1, cleanupCalls.get());
    }

    @Test
    public void cleanupFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected cleanup failure");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample08_UpdateAnalyzerAsync
                .runWithCleanup(Mono.just("resource"), ignored -> Mono.empty(), ignored -> Mono.error(expected))
                .block());

        assertTrue(actual.getMessage().contains("Async resource cleanup failed"));
        assertSame(expected, actual.getCause());
    }
}
