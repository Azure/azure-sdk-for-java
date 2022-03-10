// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.junit.extensions;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SyncAsyncExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SyncAsyncTest {

    private static final AtomicInteger SYNC_INVOCATIONS = new AtomicInteger();
    private static final AtomicInteger ASYNC_INVOCATIONS = new AtomicInteger();

    @Order(1)
    @com.azure.core.test.junit.extensions.annotation.SyncAsyncTest
    public void sampleSyncAsyncTest() throws Exception {
        Integer value = SyncAsyncExtension.execute(
            SYNC_INVOCATIONS::incrementAndGet,
            ASYNC_INVOCATIONS::incrementAndGet
        );

        assertEquals(1, value);
    }

    @Order(2)
    @Test
    public void actualAssertion() {
        assertEquals(1, SYNC_INVOCATIONS.get());
        assertEquals(1, ASYNC_INVOCATIONS.get());
    }
}
