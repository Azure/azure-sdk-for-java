// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.concurrent;

import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureFutureTests {
    private static ExecutorService executorService;

    @BeforeAll
    public static void setupClass() {
        executorService = CoreUtils.createCachedExecutorService(2);
    }

    @AfterAll
    public static void teardownClass() {
        executorService.shutdownNow();
    }

    @Test
    public void futureCompletesBeforeTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        AtomicBoolean completed = new AtomicBoolean(false);
        Future<?> future = executorService.submit(() -> {
            Thread.sleep(10);
            completed.set(true);
            return null;
        });

        future.get(100, TimeUnit.MILLISECONDS);

        assertTrue(completed.get());
    }

    @Test
    public void futureTimesOutAndIsCancelled() throws InterruptedException {
        AtomicBoolean completed = new AtomicBoolean(false);
        Future<?> future = executorService.submit(() -> {
            Thread.sleep(1000);
            completed.set(true);
            return null;
        });

        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Ignore
        }

        // Give time for the future to complete if cancellation didn't work.
        Thread.sleep(1000);

        assertFalse(completed.get());
    }
}
