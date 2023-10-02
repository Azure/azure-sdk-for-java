// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.test.annotation.SyncAsyncTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// We're indirectly testing JUnit extension
// Therefore we use ordering to sequence state mutations.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Isolated
public class SyncAsyncExtensionTest {
    private static final AtomicInteger SYNC_INVOCATIONS = new AtomicInteger();
    private static final AtomicInteger ASYNC_INVOCATIONS = new AtomicInteger();

    @Order(1)
    @SyncAsyncTest
    public void sampleSyncAsyncWithReturnTest() {
        Integer value = SyncAsyncExtension.execute(
            SYNC_INVOCATIONS::incrementAndGet,
            () -> Mono.fromCallable(ASYNC_INVOCATIONS::incrementAndGet)
        );

        assertEquals(1, value);
    }

    @Order(2)
    @SyncAsyncTest
    public void sampleSyncAsyncWithoutReturnTest() {
        SyncAsyncExtension.execute(
            SYNC_INVOCATIONS::incrementAndGet,
            () -> Mono.fromCallable(ASYNC_INVOCATIONS::incrementAndGet).then()
        );
    }

    @Order(3)
    @Test
    public void actualAssertion() {
        assertEquals(2, SYNC_INVOCATIONS.get());
        assertEquals(2, ASYNC_INVOCATIONS.get());
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
