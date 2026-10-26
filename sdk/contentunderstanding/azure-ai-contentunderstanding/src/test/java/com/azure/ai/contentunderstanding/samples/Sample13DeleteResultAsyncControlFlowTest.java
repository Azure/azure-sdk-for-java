// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample13DeleteResultAsyncControlFlowTest {
    @Test
    public void successfulResultIsReturned() {
        String result = Sample13_DeleteResultAsync
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.just("result"), "Test")
            .block();

        assertEquals("result", result);
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean subscribed = new AtomicBoolean();
        Mono<String> finalResult = Mono.just("result").doOnSubscribe(ignored -> subscribed.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample13_DeleteResultAsync
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
            () -> Sample13_DeleteResultAsync
                .requireSuccessfulResult(LongRunningOperationStatus.USER_CANCELLED, finalResult, "Test")
                .block());

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
        assertFalse(subscribed.get());
    }

    @Test
    public void emptyFinalResultIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample13_DeleteResultAsync
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.empty(), "Test")
                .block());

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void finalResultFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample13_DeleteResultAsync
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.error(expected),
                    "Test")
                .block());

        assertSame(expected, actual);
    }

    @Test
    public void resultIsReturnedAfterDeletionCompletes() {
        AtomicBoolean deletionCompleted = new AtomicBoolean();
        Mono<Void> deletion = Mono.fromRunnable(() -> deletionCompleted.set(true));

        String result = Sample13_DeleteResultAsync.completeAfterDeletion(deletion, "result").block();

        assertTrue(deletionCompleted.get());
        assertEquals("result", result);
    }

    @Test
    public void deletionFailurePropagates() {
        RuntimeException expected = new RuntimeException("delete failed");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample13_DeleteResultAsync.completeAfterDeletion(Mono.error(expected), "result").block());

        assertSame(expected, actual);
    }
}
