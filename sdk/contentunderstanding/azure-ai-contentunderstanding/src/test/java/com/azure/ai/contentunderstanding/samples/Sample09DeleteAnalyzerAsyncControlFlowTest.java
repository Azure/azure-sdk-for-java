// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample09DeleteAnalyzerAsyncControlFlowTest {
    @Test
    public void successfulResultIsReturned() {
        String result = Sample09_DeleteAnalyzerAsync
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.just("result"), "Test")
            .block();

        assertEquals("result", result);
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean subscribed = new AtomicBoolean();
        Mono<String> finalResult = Mono.just("result").doOnSubscribe(ignored -> subscribed.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample09_DeleteAnalyzerAsync
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
            () -> Sample09_DeleteAnalyzerAsync
                .requireSuccessfulResult(LongRunningOperationStatus.USER_CANCELLED, finalResult, "Test")
                .block());

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
        assertFalse(subscribed.get());
    }

    @Test
    public void unexpectedGetSuccessIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample09_DeleteAnalyzerAsync.verifyDeleted(Mono.just(new ContentAnalyzer()), "analyzer").block());

        assertTrue(exception.getMessage().contains("still retrievable"));
    }

    @Test
    public void unexpectedGetFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample09_DeleteAnalyzerAsync.verifyDeleted(Mono.error(expected), "analyzer").block());

        assertSame(expected, actual);
    }

    @Test
    public void deletedAnalyzerMustNotAppearInList() {
        ContentAnalyzer analyzer = analyzerWithId("deleted");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample09_DeleteAnalyzerAsync.verifyNotListed(Flux.just(analyzer), "deleted").block());

        assertTrue(exception.getMessage().contains("still appears"));
    }

    @Test
    public void absentAnalyzerPassesListVerification() {
        Sample09_DeleteAnalyzerAsync.verifyNotListed(Flux.just(analyzerWithId("other")), "deleted").block();
    }

    @Test
    public void failureCleanupRunsAndOriginalFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");
        AtomicInteger cleanupCalls = new AtomicInteger();

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample09_DeleteAnalyzerAsync
                .runWithFailureCleanup(Mono.just("resource"), ignored -> Mono.error(expected),
                    ignored -> Mono.fromRunnable(cleanupCalls::incrementAndGet))
                .block());

        assertSame(expected, actual);
        assertEquals(1, cleanupCalls.get());
    }

    @Test
    public void successfulWorkflowDoesNotRunFailureCleanup() {
        AtomicInteger cleanupCalls = new AtomicInteger();

        Sample09_DeleteAnalyzerAsync
            .runWithFailureCleanup(Mono.just("resource"), ignored -> Mono.empty(),
                ignored -> Mono.fromRunnable(cleanupCalls::incrementAndGet))
            .block();

        assertEquals(0, cleanupCalls.get());
    }

    private static ContentAnalyzer analyzerWithId(String analyzerId) {
        return BinaryData.fromString("{\"analyzerId\":\"" + analyzerId + "\"}").toObject(ContentAnalyzer.class);
    }
}
