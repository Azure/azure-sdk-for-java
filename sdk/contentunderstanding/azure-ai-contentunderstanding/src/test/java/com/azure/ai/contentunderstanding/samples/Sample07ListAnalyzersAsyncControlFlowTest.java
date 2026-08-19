// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Sample07ListAnalyzersAsyncControlFlowTest {
    @Test
    public void analyzersAreCollectedOnceAndInOrder() {
        ContentAnalyzer first = new ContentAnalyzer();
        ContentAnalyzer second = new ContentAnalyzer();
        AtomicInteger subscriptions = new AtomicInteger();

        List<ContentAnalyzer> result = Sample07_ListAnalyzersAsync
            .collectAnalyzers(Flux.just(first, second).doOnSubscribe(ignored -> subscriptions.incrementAndGet()))
            .block();

        assertEquals(Arrays.asList(first, second), result);
        assertEquals(1, subscriptions.get());
    }

    @Test
    public void emptyResultProducesAnEmptyList() {
        List<ContentAnalyzer> result = Sample07_ListAnalyzersAsync.collectAnalyzers(Flux.empty()).block();

        assertEquals(0, result.size());
    }

    @Test
    public void listFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> Sample07_ListAnalyzersAsync.collectAnalyzers(Flux.error(expected)).block());

        assertSame(expected, actual);
    }
}
