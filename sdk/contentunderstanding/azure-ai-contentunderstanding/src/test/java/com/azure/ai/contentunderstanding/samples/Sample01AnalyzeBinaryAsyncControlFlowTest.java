// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample01AnalyzeBinaryAsyncControlFlowTest {
    @Test
    public void waitsForTerminalCompletion() {
        AtomicBoolean executed = new AtomicBoolean();
        Mono<Void> operation = Mono.fromRunnable(() -> executed.set(true));

        Sample01_AnalyzeBinaryAsync.waitForCompletion(operation, Duration.ofSeconds(1), "timeout");

        assertTrue(executed.get());
    }

    @Test
    public void emptyOperationCompletesNormally() {
        assertDoesNotThrow(
            () -> Sample01_AnalyzeBinaryAsync.waitForCompletion(Mono.empty(), Duration.ofSeconds(1), "timeout"));
    }

    @Test
    public void operationFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> Sample01_AnalyzeBinaryAsync
            .waitForCompletion(Mono.error(expected), Duration.ofSeconds(1), "timeout"));

        assertSame(expected, actual);
    }

    @Test
    public void timeoutCancelsOperation() {
        AtomicBoolean cancelled = new AtomicBoolean();
        Mono<Void> operation = Mono.<Void>never().doOnCancel(() -> cancelled.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> Sample01_AnalyzeBinaryAsync
            .waitForCompletion(operation, Duration.ofMillis(100), "Sample01 timed out."));

        assertTrue(exception.getMessage().contains("Sample01 timed out"));
        assertTrue(exception.getCause() instanceof TimeoutException);
        assertTrue(cancelled.get());
    }
}
