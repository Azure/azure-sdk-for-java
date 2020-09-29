// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JsonPatchDocument}.
 */
public class JsonPatchDocumentTests {
    private static final ObjectMapper MAPPER = ((JacksonAdapter) JacksonAdapter.createDefaultSerializerAdapter())
        .serializer()
        .registerModule(JsonPatchDocumentSerializer.getModule())
        .registerModule(JsonPatchOperationSerializer.getModule());

    @ParameterizedTest
    @MethodSource("formattingSupplier")
    public void toStringTest(JsonPatchDocument document, String expected) {
        assertEquals(expected, document.toString());
    }

    @ParameterizedTest
    @MethodSource("formattingSupplier")
    public void jsonifyDocument(JsonPatchDocument document, String expected) throws IOException {
        assertEquals(expected, MAPPER.writeValueAsString(document).replace(" ", ""));
    }

    @ParameterizedTest
    @MethodSource("formattingSupplier")
    public void jsonifyOperationList(JsonPatchDocument document, String expected) throws IOException {
        assertEquals(expected, MAPPER.writeValueAsString(document.getJsonPatchOperations()).replace(" ", ""));
    }

    private static Stream<Arguments> formattingSupplier() {
        JsonPatchDocument complexDocument = new JsonPatchDocument()
            .appendTest("/a/b/c", "foo")
            .appendRemove("/a/b/c")
            .appendAdd("/a/b/c", new String[] {"foo", "bar"})
            .appendReplace("/a/b/c", 42)
            .appendMove("/a/b/c", "/a/b/d")
            .appendCopy("/a/b/d", "/a/b/e");

        String complexExpected = "["
            + "{\"op\":\"test\",\"path\":\"/a/b/c\",\"value\":\"foo\"},"
            + "{\"op\":\"remove\",\"path\":\"/a/b/c\"},"
            + "{\"op\":\"add\",\"path\":\"/a/b/c\",\"value\":[\"foo\",\"bar\"]},"
            + "{\"op\":\"replace\",\"path\":\"/a/b/c\",\"value\":42},"
            + "{\"op\":\"move\",\"from\":\"/a/b/c\",\"path\":\"/a/b/d\"},"
            + "{\"op\":\"copy\",\"from\":\"/a/b/d\",\"path\":\"/a/b/e\"}"
            + "]";

        return Stream.of(
            Arguments.of(new JsonPatchDocument().appendAdd("/baz", "qux"),
                constructExpectedOperation("add", null, "/baz", "qux")),

            Arguments.of(new JsonPatchDocument().appendAdd("/foo/1", "qux"),
                constructExpectedOperation("add", null, "/foo/1", "qux")),

            Arguments.of(new JsonPatchDocument().appendAdd("/child",
                Collections.singletonMap("grandchild", Collections.emptyMap())),
                constructExpectedOperation("add", null, "/child", "{\"grandchild\":{}}", false)),

            Arguments.of(new JsonPatchDocument().appendAdd("/foo/-", new String[] {"abc", "def"}),
                constructExpectedOperation("add", null, "/foo/-", "[\"abc\",\"def\"]", false)),

            Arguments.of(new JsonPatchDocument().appendReplace("/bar", "foo"),
                constructExpectedOperation("replace", null, "/bar", "foo")),

            Arguments.of(new JsonPatchDocument().appendReplace("/foo", new String[] {"fizz", "buzz", "fizzbuzz"}),
                constructExpectedOperation("replace", null, "/foo", "[\"fizz\",\"buzz\",\"fizzbuzz\"]", false)),

            Arguments.of(new JsonPatchDocument().appendReplace("/baz", "foo"),
                constructExpectedOperation("replace", null, "/baz", "foo")),

            Arguments.of(new JsonPatchDocument().appendCopy("/foo", "/copy"),
                constructExpectedOperation("copy", "/foo", "/copy", null)),

            Arguments.of(new JsonPatchDocument().appendCopy("/foo/bar", "/bar"),
                constructExpectedOperation("copy", "/foo/bar", "/bar", null)),

            Arguments.of(new JsonPatchDocument().appendCopy("/baz", "/fizz"),
                constructExpectedOperation("copy", "/baz", "/fizz", null)),

            Arguments.of(new JsonPatchDocument().appendMove("/foo", "/bar"),
                constructExpectedOperation("move", "/foo", "/bar", null)),

            Arguments.of(new JsonPatchDocument().appendMove("/foo/bar", "/foo"),
                constructExpectedOperation("move", "/foo/bar", "/foo", null)),

            Arguments.of(new JsonPatchDocument().appendMove("/foo", "/foo/bar"),
                constructExpectedOperation("move", "/foo", "/foo/bar", null)),

            Arguments.of(new JsonPatchDocument().appendMove("/baz", "/fizz"),
                constructExpectedOperation("move", "/baz", "/fizz", null)),

            Arguments.of(new JsonPatchDocument().appendRemove("/bar"),
                constructExpectedOperation("remove", null, "/bar", null)),

            Arguments.of(new JsonPatchDocument().appendRemove("/foo/bar"),
                constructExpectedOperation("remove", null, "/foo/bar", null)),

            Arguments.of(new JsonPatchDocument().appendRemove("/baz"),
                constructExpectedOperation("remove", null, "/baz", null)),

            Arguments.of(new JsonPatchDocument().appendTest("/foo", "bar"),
                constructExpectedOperation("test", null, "/foo", "bar")),

            Arguments.of(new JsonPatchDocument().appendTest("/foo", 42),
                constructExpectedOperation("test", null, "/foo", "42", false)),

            Arguments.of(new JsonPatchDocument().appendTest("/baz", "bar"),
                constructExpectedOperation("test", null, "/baz", "bar")),

            Arguments.of(complexDocument, complexExpected)
        );
    }

    private static String constructExpectedOperation(String op, String from, String path, String value) {
        return constructExpectedOperation(op, from, path, value, true);
    }

    private static String constructExpectedOperation(String op, String from, String path, String value,
        boolean quoteValue) {
        StringBuilder builder = new StringBuilder("[{\"op\":\"").append(op).append("\"");

        if (from != null) {
            builder.append(",\"from\":\"").append(from).append("\"");
        }

        builder.append(",\"path\":\"").append(path).append("\"");

        if (value != null) {
            builder.append(",\"value\":")
                .append(quoteValue ? "\"" : "")
                .append(value)
                .append(quoteValue ? "\"" : "");
        }

        return builder.append("}]").toString();
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentSupplier")
    public void invalidArgument(Runnable runnable) {
        assertThrows(NullPointerException.class, runnable::run);
    }

    private static Stream<Arguments> invalidArgumentSupplier() {
        JsonPatchDocument document = new JsonPatchDocument();
        return Stream.of(
            Arguments.of((Runnable) () -> document.appendAdd(null, "\"bar\"")),

            Arguments.of((Runnable) () -> document.appendReplace(null, "\"bar\"")),

            Arguments.of((Runnable) () -> document.appendCopy(null, "\"bar\"")),
            Arguments.of((Runnable) () -> document.appendCopy("/foo", null)),

            Arguments.of((Runnable) () -> document.appendMove(null, "\"bar\"")),
            Arguments.of((Runnable) () -> document.appendMove("/foo", null)),

            Arguments.of((Runnable) () -> document.appendRemove(null)),

            Arguments.of((Runnable) () -> document.appendTest(null, "\"bar\""))
        );
    }
}
