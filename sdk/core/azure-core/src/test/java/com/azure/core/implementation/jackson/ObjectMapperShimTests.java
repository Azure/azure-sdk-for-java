// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.TypeReference;
import com.azure.core.util.serializer.nojackson.Simple;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Tests the shared JavaType cache's capacity policy")
public class ObjectMapperShimTests {
    private static final String NESTED_JSON = "{\"items\":[{\"id\":\"1\",\"name\":\"value\"}]}";

    @ParameterizedTest
    @ValueSource(strings = { "string", "bytes", "stream" })
    public void cachedNestedGenericSkipsArgumentResolution(String input) throws IOException {
        ObjectMapperShim shim = new ObjectMapperShim(new ObjectMapper());
        CountingParameterizedType type
            = new CountingParameterizedType((ParameterizedType) new TypeReference<Map<String, List<Simple>>>() {
            }.getJavaType());
        Map<String, List<Simple>> expected
            = Collections.singletonMap("items", Collections.singletonList(new Simple("1", "value")));

        assertEquals(expected, readNestedValue(shim, type, input));
        int resolutionsAfterFirstRead = type.argumentResolutions;
        assertEquals(expected, readNestedValue(shim, type, input));
        assertEquals(resolutionsAfterFirstRead, type.argumentResolutions);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cachedGenericStillClearsCacheAtCapacity() throws ReflectiveOperationException, IOException {
        Field cacheField = ObjectMapperShim.class.getDeclaredField("TYPE_TO_JAVA_TYPE_CACHE");
        cacheField.setAccessible(true);
        Map<Type, JavaType> cache = (Map<Type, JavaType>) cacheField.get(null);
        Map<Type, JavaType> savedCache = new HashMap<>(cache);
        try {
            cache.clear();
            ObjectMapper mapper = new ObjectMapper();
            ObjectMapperShim shim = new ObjectMapperShim(mapper);
            CountingParameterizedType type
                = new CountingParameterizedType((ParameterizedType) new TypeReference<Map<String, List<Simple>>>() {
                }.getJavaType());
            readNestedValue(shim, type, "string");
            int resolutionsBeforeCapacityRead = type.argumentResolutions;
            JavaType filler = mapper.constructType(String.class);
            while (cache.size() < 10000) {
                cache.put(new Type() {
                }, filler);
            }

            readNestedValue(shim, type, "string");

            assertTrue(type.argumentResolutions > resolutionsBeforeCapacityRead);
            assertTrue(cache.size() < 10000);
            assertNotNull(cache.get(type));
        } finally {
            cache.clear();
            cache.putAll(savedCache);
        }
    }

    private static Map<String, List<Simple>> readNestedValue(ObjectMapperShim shim, Type type, String input)
        throws IOException {
        switch (input) {
            case "string":
                return shim.readValue(NESTED_JSON, type);

            case "bytes":
                return shim.readValue(NESTED_JSON.getBytes(StandardCharsets.UTF_8), type);

            case "stream":
                return shim.readValue(new ByteArrayInputStream(NESTED_JSON.getBytes(StandardCharsets.UTF_8)), type);

            default:
                throw new IllegalArgumentException("Unexpected input: " + input);
        }
    }

    private static final class CountingParameterizedType implements ParameterizedType {
        private final ParameterizedType type;
        private int argumentResolutions;

        private CountingParameterizedType(ParameterizedType type) {
            this.type = type;
        }

        @Override
        public Type[] getActualTypeArguments() {
            argumentResolutions++;
            return type.getActualTypeArguments();
        }

        @Override
        public Type getRawType() {
            return type.getRawType();
        }

        @Override
        public Type getOwnerType() {
            return type.getOwnerType();
        }

        @Override
        public boolean equals(Object other) {
            return type.equals(other);
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }
    }

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
