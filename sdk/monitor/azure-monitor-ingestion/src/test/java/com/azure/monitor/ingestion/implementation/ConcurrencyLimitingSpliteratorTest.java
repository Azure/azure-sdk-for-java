// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurrencyLimitingSpliteratorTest {
    private static final int TEST_TIMEOUT_SEC = 30;
    private static final ExecutorService TEST_THREAD_POOL = Executors.newCachedThreadPool();

    @Test
    public void invalidParams() {
        assertThrows(NullPointerException.class, () -> new ConcurrencyLimitingSpliterator<Integer>(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new ConcurrencyLimitingSpliterator<>(Arrays.asList(1, 2, 3).iterator(), 0));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 5, 7, 11, 15})
    public void concurrentCalls(int concurrency) throws ExecutionException, InterruptedException {
        assumeTrue(Runtime.getRuntime().availableProcessors() > concurrency);

        List<Integer> list = IntStream.range(0, 11).boxed().collect(Collectors.toList());
        ConcurrencyLimitingSpliterator<Integer> spliterator = new ConcurrencyLimitingSpliterator<>(list.iterator(), concurrency);

        Stream<Integer> stream = StreamSupport.stream(spliterator, true);

        int effectiveConcurrency = Math.min(list.size(), concurrency);
        CountDownLatch latch = new CountDownLatch(effectiveConcurrency);
        List<Integer> processed = TEST_THREAD_POOL
            .submit(() ->
                stream.map(r -> {
                    latch.countDown();
                    try {
                        Thread.sleep(10);
                        assertTrue(latch.await(TEST_TIMEOUT_SEC, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        fail("countdown await interrupted");
                    }
                    return r;
                })
                .collect(Collectors.toList())
            ).get();

        assertArrayEquals(list.toArray(), processed.stream().sorted().toArray());
    }

    @Test
    public void concurrencyHigherThanItemsCount() throws ExecutionException, InterruptedException {
        int concurrency = 100;
        List<Integer> list = IntStream.range(0, 7).boxed().collect(Collectors.toList());
        ConcurrencyLimitingSpliterator<Integer> spliterator = new ConcurrencyLimitingSpliterator<>(list.iterator(), concurrency);

        Stream<Integer> stream = StreamSupport.stream(spliterator, true);

        AtomicInteger parallel = new AtomicInteger(0);
        AtomicInteger maxParallel = new AtomicInteger(0);
        List<Integer> processed = TEST_THREAD_POOL
            .submit(() ->
                stream.map(r -> {
                    int cur = parallel.incrementAndGet();
                    int curMax = maxParallel.get();
                    while (cur > curMax && !maxParallel.compareAndSet(curMax, cur)) {
                        curMax = maxParallel.get();
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        fail("timeout");
                    }

                    parallel.decrementAndGet();
                    return r;
                })
                .collect(Collectors.toList())
            ).get();

        assertTrue(maxParallel.get() <= list.size());
        assertArrayEquals(list.toArray(), processed.stream().sorted().toArray());
    }
}
