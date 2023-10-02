// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.contract;

import com.typespec.json.JsonOptions;
import com.typespec.json.JsonProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the contract of {@link JsonProvider}.
 * <p>
 * All implementations of {@link JsonProvider} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 */
public abstract class JsonProviderContractTests {
    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    /**
     * Creates an instance {@link JsonProvider} that will be used by a test.
     *
     * @return The {@link JsonProvider} that a test will use.
     */
    protected abstract JsonProvider getJsonProvider();

    @ParameterizedTest
    @MethodSource("nullJsonSupplier")
    public void nullJsonThrowsNullPointerException(Method creator) {
        assertThrows(NullPointerException.class, () -> PUBLIC_LOOKUP.unreflect(creator)
            .invokeWithArguments(getJsonProvider(), null, new JsonOptions()));
    }

    private static Stream<Method> nullJsonSupplier() throws NoSuchMethodException {
        return Stream.of(
            JsonProvider.class.getDeclaredMethod("createReader", byte[].class, JsonOptions.class),
            JsonProvider.class.getDeclaredMethod("createReader", String.class, JsonOptions.class),
            JsonProvider.class.getDeclaredMethod("createReader", InputStream.class, JsonOptions.class),
            JsonProvider.class.getDeclaredMethod("createReader", Reader.class, JsonOptions.class),
            JsonProvider.class.getDeclaredMethod("createWriter", OutputStream.class, JsonOptions.class),
            JsonProvider.class.getDeclaredMethod("createWriter", Writer.class, JsonOptions.class)
        );
    }

    @ParameterizedTest
    @MethodSource("nonNullJsonSupplier")
    public <T> void nullJsonOptionsThrowsNullPointerException(Method creator, T json) {
        assertThrows(NullPointerException.class, () -> PUBLIC_LOOKUP.unreflect(creator)
            .invokeWithArguments(getJsonProvider(), json, null));
    }

    @ParameterizedTest
    @MethodSource("nonNullJsonSupplier")
    public <T> void canCreate(Method creator, T json) {
        assertDoesNotThrow(() -> PUBLIC_LOOKUP.unreflect(creator)
            .invokeWithArguments(getJsonProvider(), json, new JsonOptions()));
    }

    private static Stream<Arguments> nonNullJsonSupplier() throws NoSuchMethodException {
        return Stream.of(
            Arguments.of(JsonProvider.class.getDeclaredMethod("createReader", byte[].class, JsonOptions.class),
                new byte[0]),
            Arguments.of(JsonProvider.class.getDeclaredMethod("createReader", String.class, JsonOptions.class), ""),
            Arguments.of(JsonProvider.class.getDeclaredMethod("createReader", InputStream.class, JsonOptions.class),
                new ByteArrayInputStream(new byte[0])),
            Arguments.of(JsonProvider.class.getDeclaredMethod("createReader", Reader.class, JsonOptions.class),
                new StringReader("")),
            Arguments.of(JsonProvider.class.getDeclaredMethod("createWriter", OutputStream.class, JsonOptions.class),
                new ByteArrayOutputStream()),
            Arguments.of(JsonProvider.class.getDeclaredMethod("createWriter", Writer.class, JsonOptions.class),
                new StringWriter())
        );
    }
}
