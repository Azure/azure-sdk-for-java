// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample06GetAnalyzerAsyncControlFlowTest {
    @Test
    public void operationsRunInOrder() {
        List<String> operations = new ArrayList<>();

        Sample06_GetAnalyzerAsync
            .runOperations(operation("prebuilt", operations), operation("invoice", operations),
                operation("custom", operations))
            .block();

        assertEquals(Arrays.asList("prebuilt", "invoice", "custom"), operations);
    }

    @Test
    public void firstFailurePropagatesAndStopsLaterOperations() {
        RuntimeException expected = new RuntimeException("expected failure");
        AtomicBoolean invoiceSubscribed = new AtomicBoolean();
        AtomicBoolean customSubscribed = new AtomicBoolean();

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample06_GetAnalyzerAsync
                .runOperations(Mono.error(expected), subscribedMono(invoiceSubscribed),
                    subscribedMono(customSubscribed))
                .block());

        assertSame(expected, actual);
        assertFalse(invoiceSubscribed.get());
        assertFalse(customSubscribed.get());
    }

    @Test
    public void secondFailurePropagatesAndStopsCustomOperation() {
        RuntimeException expected = new RuntimeException("expected failure");
        AtomicBoolean customSubscribed = new AtomicBoolean();

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample06_GetAnalyzerAsync
                .runOperations(Mono.empty(), Mono.error(expected), subscribedMono(customSubscribed))
                .block());

        assertSame(expected, actual);
        assertFalse(customSubscribed.get());
    }

    @Test
    public void unsuccessfulLroStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean subscribed = new AtomicBoolean();
        Mono<String> finalResult = Mono.just("result").doOnSubscribe(ignored -> subscribed.set(true));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample06_GetAnalyzerAsync
                .requireSuccessfulResult(LongRunningOperationStatus.FAILED, finalResult, "Test")
                .block());

        assertTrue(exception.getMessage().contains("FAILED"));
        assertFalse(subscribed.get());
    }

    private static Mono<Void> operation(String name, List<String> operations) {
        return Mono.defer(() -> {
            operations.add(name);
            return Mono.empty();
        });
    }

    private static Mono<Void> subscribedMono(AtomicBoolean subscribed) {
        return Mono.defer(() -> {
            subscribed.set(true);
            return Mono.empty();
        });
    }
}
