// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SharedExecutorService}
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Modifies shared global state in SharedExecutorService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SharedExecutorServiceTests {
    @BeforeAll
    public static void resetBeforeTesting() {
        SharedExecutorService.reset();
    }

    @AfterAll
    public static void resetAfterTesting() {
        SharedExecutorService.reset();
    }

    @Test
    @Order(1)
    public void startsUninitialized() {
        assertNull(SharedExecutorService.getInstance().executor.get());
    }

    @Test
    @Order(2)
    public void uninitializedUsesDefaultExecutor() throws ExecutionException, InterruptedException {
        SharedExecutorService.getInstance().submit(() -> {
        }).get();

        // ExecutorService should be set when there isn't one available.
        assertNotNull(SharedExecutorService.getInstance().executor.get());

        // And since we defined it within the SDK we add a shutdown thread.
        assertNotNull(SharedExecutorService.getInstance().executor.get().shutdownThread);
    }

    @Test
    @Order(3)
    public void settingCustomExecutorShutsDownDefault() throws ExecutionException, InterruptedException {
        ExecutorService defaultService = SharedExecutorService.getInstance().executor.get().executorService;
        AtomicInteger callCount = new AtomicInteger();
        ExecutorService executorService = Executors.newCachedThreadPool(r -> {
            callCount.getAndIncrement();
            return new Thread(r);
        });
        SharedExecutorService.setExecutorService(executorService);
        SharedExecutorService.getInstance().submit(() -> {
        }).get();

        assertEquals(1, callCount.get());

        // Default ExecutorService should get shut down after a custom one is set.
        assertTrue(defaultService.isShutdown());

        // Custom ExecutorService doesn't have a shutdown thread.
        assertNull(SharedExecutorService.getInstance().executor.get().shutdownThread);

        // Shut down custom ExecutorService for next test validation.
        executorService.shutdown();
    }

    @Test
    @Order(4)
    public void shuttingDownCustomerServiceSetsDefault() throws ExecutionException, InterruptedException {
        SharedExecutorService.getInstance().submit(() -> {
        }).get();

        // Custom ExecutorService was shut down, need to set one again.
        assertNotNull(SharedExecutorService.getInstance().executor.get());

        // And since we defined it within the SDK we add a shutdown thread.
        assertNotNull(SharedExecutorService.getInstance().executor.get().shutdownThread);
    }
}
