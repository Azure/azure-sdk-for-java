// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledForJreRange(min = JRE.JAVA_9)
public class CleanerAccessTests {
    @Test
    public void retainsTheSuppliedThreadFactory() throws Exception {
        AtomicReference<Thread> thread = new AtomicReference<>();
        CleanerAccess access = CleanerAccess.create(runnable -> {
            Thread created = new Thread(runnable, "azure-sdk-cleaner-test");
            thread.set(created);
            return created;
        });

        assertNotNull(access);
        assertNotNull(thread.get());
        assertEquals("azure-sdk-cleaner-test", thread.get().getName());
        assertTrue(thread.get().isDaemon());
        access.register(new Object(), () -> {
        });
    }

    @Test
    public void failedCreationLeavesTheLegacyFallbackAvailable() {
        assertNull(CleanerAccess.create(runnable -> null));
    }
}
