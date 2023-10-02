// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

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
 * Tests {@link JsonProviders}.
 */
public class JsonProvidersTests {
    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    @ParameterizedTest
    @MethodSource("nullJsonSupplier")
    public void nullJsonThrowsNullPointerException(Method creator, boolean useOptions) {
        if (useOptions) {
            assertThrows(NullPointerException.class, () -> PUBLIC_LOOKUP.unreflect(creator)
                .invokeWithArguments(null, new JsonOptions()));
        } else {
            assertThrows(NullPointerException.class, () -> PUBLIC_LOOKUP.unreflect(creator)
                .invokeWithArguments((Object) null));
        }
    }

    private static Stream<Arguments> nullJsonSupplier() throws NoSuchMethodException {
        return Stream.of(
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", byte[].class), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", byte[].class, JsonOptions.class), true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", String.class), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", String.class, JsonOptions.class), true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", InputStream.class), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", InputStream.class, JsonOptions.class),
                true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", Reader.class), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", Reader.class, JsonOptions.class), true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", OutputStream.class), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", OutputStream.class, JsonOptions.class),
                true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", Writer.class), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", Writer.class, JsonOptions.class), true)
        );
    }

    @ParameterizedTest
    @MethodSource("nonNullJsonSupplier")
    public <T> void nullJsonOptionsThrowsNullPointerException(Method creator, T json, boolean useOptions) {
        if (useOptions) {
            assertThrows(NullPointerException.class, () -> PUBLIC_LOOKUP.unreflect(creator)
                .invokeWithArguments(json, null));
        } else {
            assertDoesNotThrow(() -> PUBLIC_LOOKUP.unreflect(creator).invokeWithArguments(json));
        }
    }

    @ParameterizedTest
    @MethodSource("nonNullJsonSupplier")
    public <T> void canCreate(Method creator, T json, boolean useOptions) {
        if (useOptions) {
            assertDoesNotThrow(() -> PUBLIC_LOOKUP.unreflect(creator).invokeWithArguments(json, new JsonOptions()));
        } else {
            assertDoesNotThrow(() -> PUBLIC_LOOKUP.unreflect(creator).invokeWithArguments(json));
        }
    }

    private static Stream<Arguments> nonNullJsonSupplier() throws NoSuchMethodException {
        return Stream.of(
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", byte[].class), new byte[0], false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", byte[].class, JsonOptions.class),
                new byte[0], true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", String.class), "", false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", String.class, JsonOptions.class), "",
                true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", InputStream.class),
                new ByteArrayInputStream(new byte[0]), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", InputStream.class, JsonOptions.class),
                new ByteArrayInputStream(new byte[0]), true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", Reader.class), new StringReader(""),
                false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createReader", Reader.class, JsonOptions.class),
                new StringReader(""), true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", OutputStream.class),
                new ByteArrayOutputStream(), false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", OutputStream.class, JsonOptions.class),
                new ByteArrayOutputStream(), true),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", Writer.class), new StringWriter(),
                false),
            Arguments.of(JsonProviders.class.getDeclaredMethod("createWriter", Writer.class, JsonOptions.class),
                new StringWriter(), true)
        );
    }
}
