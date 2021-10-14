// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for {@link ReferenceManager}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Forces JVM GC")
public class ReferenceManagerIT {
    @Test
    public void cleanupActionIsPerformedWhenObjectIsPhantomReachable() throws InterruptedException {
        byte[] expectedInitialBytes = new byte[4096];
        Arrays.fill(expectedInitialBytes, (byte) 1);

        byte[] wrappedBytes = new byte[4096];
        Arrays.fill(wrappedBytes, (byte) 1);

        byte[] expectedFinalBytes = new byte[4096];
        Arrays.fill(expectedFinalBytes, (byte) 0);

        WrapperClass wrapperClass = new WrapperClass(wrappedBytes);

        Runnable cleanupAction = mock(Runnable.class);
        doAnswer(invocation -> {
            Arrays.fill(wrappedBytes, (byte) 0);
            return null;
        }).when(cleanupAction).run();

        ReferenceManager.INSTANCE.register(wrapperClass, cleanupAction);

        assertArrayEquals(expectedInitialBytes, wrappedBytes);

        wrapperClass = null;

        System.gc();
        System.gc();

        // Give the cleanup action a moment to run.
        Thread.sleep(500);

        assertArrayEquals(expectedFinalBytes, wrappedBytes);

        verify(cleanupAction, times(1)).run();
    }

    @Test
    public void exceptionsInCleanupActionAreSwallowed() throws InterruptedException {
        WrapperClass wrapperClass = new WrapperClass(new byte[0]);
        Runnable cleanupAction = mock(Runnable.class);
        doThrow(IllegalStateException.class).when(cleanupAction).run();

        ReferenceManager.INSTANCE.register(wrapperClass, cleanupAction);

        wrapperClass = null;

        System.gc();
        System.gc();

        // Give the cleanup action a moment to run.
        Thread.sleep(500);

        verify(cleanupAction, times(1)).run();

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
