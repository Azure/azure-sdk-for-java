// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.serializer.DefaultJsonReader;
import com.azure.core.util.serializer.DefaultJsonWriter;
import com.azure.core.util.serializer.JsonWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link JsonPatchOperation}.
 */
public class JsonPatchOperationTests {
    @ParameterizedTest
    @MethodSource("toJsonSupplier")
    public void toJson(JsonPatchOperation operation, String expectedJson) throws IOException {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        try (JsonWriter writer = DefaultJsonWriter.toStream(outputStream)) {
            operation.toJson(writer);
        }

        assertEquals(expectedJson, outputStream.toString(StandardCharsets.UTF_8));
    }

    private static Stream<Arguments> toJsonSupplier() {
        return Stream.of(
            Arguments.of(new JsonPatchOperation(JsonPatchOperationKind.REMOVE, null, "/a", Option.uninitialized()),
                "{\"op\":\"remove\",\"path\":\"/a\"}"),
            Arguments.of(new JsonPatchOperation(JsonPatchOperationKind.TEST, null, "/a", Option.of("\"simple\"")),
                "{\"op\":\"test\",\"path\":\"/a\",\"value\":\"simple\"}"),
            Arguments.of(new JsonPatchOperation(JsonPatchOperationKind.MOVE, "/a", "/b", Option.uninitialized()),
                "{\"op\":\"move\",\"from\":\"/a\",\"path\":\"/b\"}"),
            Arguments.of(new JsonPatchOperation(JsonPatchOperationKind.ADD, null, "/a",
                Option.of("{\"array\":[\"string\",42,true,null]}")),
                "{\"op\":\"add\",\"path\":\"/a\",\"value\":{\"array\":[\"string\",42,true,null]}}")
        );
    }

    @ParameterizedTest
    @MethodSource("fromJsonSupplier")
    public void fromJson(String json, JsonPatchOperation expectedOperation) {
        JsonPatchOperation actualOperation = JsonPatchOperation.fromJson(DefaultJsonReader.fromString(json));

        assertEquals(expectedOperation.getOp(), actualOperation.getOp());
        assertEquals(expectedOperation.getFrom(), actualOperation.getFrom());
        assertEquals(expectedOperation.getPath(), actualOperation.getPath());

        assertEquals(expectedOperation.getValue().isInitialized(), actualOperation.getValue().isInitialized());
        if (expectedOperation.getValue().isInitialized()) {
            assertEquals(expectedOperation.getValue().getValue(), actualOperation.getValue().getValue());
        }
    }

    private static Stream<Arguments> fromJsonSupplier() {
        return Stream.of(
//            Arguments.of("{\"op\":\"remove\",\"path\":\"/a\"}",
//                new JsonPatchOperation(JsonPatchOperationKind.REMOVE, null, "/a", Option.uninitialized())),
//            Arguments.of("{\"op\":\"test\",\"path\":\"/a\",\"value\":\"sample\"}",
//                new JsonPatchOperation(JsonPatchOperationKind.TEST, null, "/a", Option.of("\"simple\""))),
//            Arguments.of("{\"op\":\"move\",\"from\":\"/a\",\"path\":\"/b\"}",
//                new JsonPatchOperation(JsonPatchOperationKind.MOVE, "/a", "/b", Option.uninitialized())),
            Arguments.of("{\"op\":\"add\",\"path\":\"/a\",\"value\":{\"array\":[\"string\",42,true,null]}}",
                new JsonPatchOperation(JsonPatchOperationKind.ADD, null, "/a",
                    Option.of("{\"array\":[\"string\",42,true,null]}")))
        );
    }
}
