// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.graph;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AadGraphClientTest {

    @Test
    public void testThreadInterrupted() {
        assertFalse(Thread.currentThread().isInterrupted());
        Thread.currentThread().interrupt();
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    public void testThreadInterruptedInMethod() {
        boolean isInterrupted = false;
        //Initially set the thread interrupted flag to true
        Thread.currentThread().interrupt();
        try {
            //mock the call which can throw InterruptedException
            TimeUnit.SECONDS.sleep(2L);
        } catch (InterruptedException e) {
            assertFalse(Thread.currentThread().isInterrupted());
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            isInterrupted = true;
        }

        assertTrue(Thread.currentThread().isInterrupted());
        assertTrue(isInterrupted);
    }
}
