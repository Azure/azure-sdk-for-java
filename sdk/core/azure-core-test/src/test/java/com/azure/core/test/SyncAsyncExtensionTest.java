// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.annotation.SyncAsyncTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SyncAsyncExtensionTest {
    private static final AtomicInteger SYNC_INVOCATIONS = new AtomicInteger();
    private static final AtomicInteger ASYNC_INVOCATIONS = new AtomicInteger();

    @Order(1)
    @SyncAsyncTest
    public void sampleSyncAsyncTest() {
        Integer value = SyncAsyncExtension.execute(
            SYNC_INVOCATIONS::incrementAndGet,
            () -> Mono.fromCallable(ASYNC_INVOCATIONS::incrementAndGet)
        );

        assertEquals(1, value);
    }

    @Order(2)
    @Test
    public void actualAssertion() {
        assertEquals(1, SYNC_INVOCATIONS.get());
        assertEquals(1, ASYNC_INVOCATIONS.get());
    }

    @Test
    public void throwsIfExtensionUsedWithoutAnnotation() {
        assertThrows(IllegalStateException.class,
            () -> SyncAsyncExtension.execute(
                () -> 1L,
                () -> Mono.just(1L)
            ));
    }
}
