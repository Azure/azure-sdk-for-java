// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ReferenceManager}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Forces JVM GC")
public class ReferenceManagerTests {
    @Test
    public void cleanupActionIsPerformedWhenObjectIsPhantomReachable() throws InterruptedException {
        byte[] expectedInitialBytes = new byte[4096];
        Arrays.fill(expectedInitialBytes, (byte) 1);

        byte[] wrappedBytes = new byte[4096];
        Arrays.fill(wrappedBytes, (byte) 1);

        byte[] expectedFinalBytes = new byte[4096];
        Arrays.fill(expectedFinalBytes, (byte) 0);

        WrapperClass wrapperClass = new WrapperClass(wrappedBytes);

        AtomicInteger cleanupCalls = new AtomicInteger();
        Runnable cleanupAction = () -> {
            cleanupCalls.incrementAndGet();
            Arrays.fill(wrappedBytes, (byte) 0);
        };

        ReferenceManager.INSTANCE.register(wrapperClass, cleanupAction);

        assertArraysEqual(expectedInitialBytes, wrappedBytes);

        wrapperClass = null;

        System.gc();
        System.gc();

        // Give the cleanup action a moment to run.
        Thread.sleep(500);

        assertArraysEqual(expectedFinalBytes, wrappedBytes);

        assertEquals(1, cleanupCalls.get());
    }

    @Test
    public void exceptionsInCleanupActionAreSwallowed() throws InterruptedException {
        WrapperClass wrapperClass = new WrapperClass(new byte[0]);
        AtomicInteger cleanupCalls = new AtomicInteger();
        Runnable cleanupAction = () -> {
            cleanupCalls.incrementAndGet();
            throw new IllegalStateException();
        };

        ReferenceManager.INSTANCE.register(wrapperClass, cleanupAction);

        wrapperClass = null;

        System.gc();
        System.gc();

        // Give the cleanup action a moment to run.
        Thread.sleep(500);

        assertEquals(1, cleanupCalls.get());

        // Then run the valid case to make sure the ReferenceManager thread continues working.
        cleanupActionIsPerformedWhenObjectIsPhantomReachable();
    }

    private static final class WrapperClass {
        private final byte[] wrappedBytes;

        private WrapperClass(byte[] wrappedBytes) {
            this.wrappedBytes = wrappedBytes;
        }
    }
}
