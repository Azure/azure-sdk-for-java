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

public class Sample17CreateAnalyzerWorkflowAsyncControlFlowTest {
    @Test
    public void successfulResultIsReturned() {
        assertEquals("result",
            Sample17_CreateAnalyzerWorkflowAsync
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.just("result"), "Test")
                .block());
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean subscribed = new AtomicBoolean();
        Mono<String> finalResult = Mono.just("result").doOnSubscribe(ignored -> subscribed.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample17_CreateAnalyzerWorkflowAsync
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
            () -> Sample17_CreateAnalyzerWorkflowAsync
                .requireSuccessfulResult(LongRunningOperationStatus.USER_CANCELLED, finalResult, "Test")
                .block());

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
        assertFalse(subscribed.get());
    }

    @Test
    public void emptyFinalResultIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample17_CreateAnalyzerWorkflowAsync
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.empty(), "Test")
                .block());

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void finalResultFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample17_CreateAnalyzerWorkflowAsync
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.error(expected),
                    "Test")
                .block());

        assertSame(expected, actual);
    }

    @Test
    public void successfulWorkflowWaitsForCleanup() {
        AtomicInteger cleanupCalls = new AtomicInteger();

        String result = Sample17_CreateAnalyzerWorkflowAsync
            .runWithCleanup(Mono.just("result"), () -> Mono.fromRunnable(cleanupCalls::incrementAndGet))
            .block();

        assertEquals("result", result);
        assertEquals(1, cleanupCalls.get());
    }

    @Test
    public void failedWorkflowWaitsForCleanupAndPropagatesFailure() {
        AtomicInteger cleanupCalls = new AtomicInteger();
        RuntimeException expected = new RuntimeException("workflow failed");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample17_CreateAnalyzerWorkflowAsync
                .runWithCleanup(Mono.error(expected), () -> Mono.fromRunnable(cleanupCalls::incrementAndGet))
                .block());

        assertSame(expected, actual);
        assertEquals(1, cleanupCalls.get());
    }

    @Test
    public void emptyWorkflowRunsCleanupAndFails() {
        AtomicInteger cleanupCalls = new AtomicInteger();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample17_CreateAnalyzerWorkflowAsync
                .runWithCleanup(Mono.empty(), () -> Mono.fromRunnable(cleanupCalls::incrementAndGet))
                .block());

        assertTrue(exception.getMessage().contains("without a result"));
        assertEquals(1, cleanupCalls.get());
    }
}
