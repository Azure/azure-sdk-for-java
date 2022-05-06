// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AadGraphClientTest {

    @Test
    public void testThreadInterrupted() {
        Thread.currentThread().interrupt();
        assertTrue(Thread.currentThread().isInterrupted());
    }
}
