// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {
    @Test
    public void shutdownHookTerminatesPool() throws InterruptedException, ExecutionException {
        int timeoutSec = 2;
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        Thread hook = Utils.registerShutdownHook(threadPool, timeoutSec);

        Stream<Integer> stream = IntStream.of(100, 4000)
            .boxed()
            .parallel()
            .map(this::task);

        Future<List<Integer>> tasks = threadPool.submit(() -> stream.collect(Collectors.toList()));

        hook.run();

        assertTrue(threadPool.isShutdown());
        assertTrue(threadPool.isTerminated());
        assertArrayEquals(new Integer[] {100, -1}, tasks.get().toArray());

        assertThrows(RejectedExecutionException.class, () -> threadPool.submit(() -> stream.collect(Collectors.toList())));
    }

    @Test
    public void shutdownHookRegistered() {
        int timeoutSec = 2;
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        Thread hook = Utils.registerShutdownHook(threadPool, timeoutSec);
        assertTrue(Runtime.getRuntime().removeShutdownHook(hook));
    }

    private int task(int sleepMs) {
        try {
            Thread.sleep(sleepMs);
            return sleepMs;
        } catch (InterruptedException e) {
            return -1;
        }
    }
}
