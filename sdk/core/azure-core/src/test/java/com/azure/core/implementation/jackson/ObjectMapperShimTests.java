// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectMapperShimTests {
    @Test
    public void testConfigure() {
        final ObjectMapper innerMapper = new ObjectMapper();
        final ObjectMapperShim innerShim = new ObjectMapperShim(innerMapper);

        final AtomicReference<Boolean> configureIsCalled = new AtomicReference<>(false);
        ObjectMapperShim.createJsonMapper(innerShim, (outer, inner) -> {
            assertNotNull(outer);
            assertSame(innerMapper, inner);
            configureIsCalled.set(true);
        });

        assertTrue(configureIsCalled.get());
    }

    @Test
    public void testConfigureJacksonAdapter() {
        final AtomicReference<Boolean> configureIsCalled = new AtomicReference<>(false);

        new JacksonAdapter((outer, inner) -> configureIsCalled.set(true));

        assertFalse(configureIsCalled.get());
    }
}
