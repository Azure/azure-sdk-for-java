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

public class Sample00UpdateDefaultsAsyncControlFlowTest {
    @Test
    public void waitsForTerminalCompletion() {
        AtomicBoolean executed = new AtomicBoolean();
        Mono<Void> operation = Mono.fromRunnable(() -> executed.set(true));

        Sample00_UpdateDefaultsAsync.waitForCompletion(operation, Duration.ofSeconds(1));

        assertTrue(executed.get());
    }

    @Test
    public void emptyOperationCompletesNormally() {
        assertDoesNotThrow(() -> Sample00_UpdateDefaultsAsync.waitForCompletion(Mono.empty(), Duration.ofSeconds(1)));
    }

    @Test
    public void operationFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample00_UpdateDefaultsAsync.waitForCompletion(Mono.error(expected), Duration.ofSeconds(1)));

        assertSame(expected, actual);
    }

    @Test
    public void timeoutCancelsOperation() {
        AtomicBoolean cancelled = new AtomicBoolean();
        Mono<Void> operation = Mono.<Void>never().doOnCancel(() -> cancelled.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample00_UpdateDefaultsAsync.waitForCompletion(operation, Duration.ofMillis(100)));

        assertTrue(exception.getMessage().contains("Timed out waiting for async operations to complete"));
        assertTrue(exception.getCause() instanceof TimeoutException);
        assertTrue(cancelled.get());
    }
}
