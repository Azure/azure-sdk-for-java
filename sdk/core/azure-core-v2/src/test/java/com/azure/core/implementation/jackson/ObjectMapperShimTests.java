// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

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
    @SuppressWarnings("deprecation")
    public void testConfigureJacksonAdapter()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final AtomicReference<Boolean> configureIsCalled = new AtomicReference<>(false);
        final AtomicReference<ObjectMapper> outerMapper = new AtomicReference<>(null);
        final AtomicReference<ObjectMapper> innerMapper = new AtomicReference<>(null);

        final JacksonAdapter adapter = new JacksonAdapter((outer, inner) -> {
            outerMapper.set(outer);
            innerMapper.set(inner);
            assertNotNull(outer);
            assertNotNull(inner);
            configureIsCalled.set(true);
        });

        assertTrue(configureIsCalled.get());
        assertSame(outerMapper.get(), adapter.serializer());

        // check protected simpleMapper getter
        Method method = JacksonAdapter.class.getDeclaredMethod("simpleMapper");
        method.setAccessible(true);
        assertSame(innerMapper.get(), method.invoke(adapter));
    }
}
