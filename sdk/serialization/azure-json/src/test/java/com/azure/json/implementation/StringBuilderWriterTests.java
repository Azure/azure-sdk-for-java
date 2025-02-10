// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link StringBuilderWriter}.
 */
public class StringBuilderWriterTests {
    @Test
    public void nullStringBuilderThrows() {
        assertThrows(NullPointerException.class, () -> new StringBuilderWriter(null));
    }

    @ParameterizedTest
    @MethodSource("canWriteToNewInstanceSupplier")
    public void canWriteToNewInstance(Callable<StringBuilderWriter> writeOperation, String expected) throws Exception {
        StringBuilderWriter writer = writeOperation.call();
        assertEquals(expected, writer.toString());
    }

    private static Stream<Arguments> canWriteToNewInstanceSupplier() {
        return Stream.of(Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.write('a');
            return writer;
        }, "a"), Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.write(new char[] { 'a', 'b', 'c' });
            return writer;
        }, "abc"), Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.write("abc");
            return writer;
        }, "abc"), Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.write("abc", 1, 2);
            return writer;
        }, "b"), Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.append("abc");
            return writer;
        }, "abc"), Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.append("abc", 1, 2);
            return writer;
        }, "b"), Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.append('a');
            return writer;
        }, "a"), Arguments.of((Callable<StringBuilderWriter>) () -> {
            StringBuilderWriter writer = new StringBuilderWriter();
            writer.write(new char[] { 'a', 'b', 'c' }, 1, 1);
            return writer;
        }, "b"));
    }

    @ParameterizedTest
    @MethodSource("writingAfterCloseThrowsSupplier")
    public void writingAfterCloseThrows(Callable<StringBuilderWriter> writeOperation) {
        assertThrows(IOException.class, writeOperation::call);
    }

    private static Stream<Callable<StringBuilderWriter>> writingAfterCloseThrowsSupplier() {
        StringBuilderWriter writer = new StringBuilderWriter();
        writer.close();

        return Stream.of(() -> {
            writer.write('a');
            return writer;
        }, () -> {
            writer.write(new char[] { 'a', 'b', 'c' });
            return writer;
        }, () -> {
            writer.write("abc");
            return writer;
        }, () -> {
            writer.write("abc", 1, 2);
            return writer;
        }, () -> {
            writer.append("abc");
            return writer;
        }, () -> {
            writer.append("abc", 1, 2);
            return writer;
        }, () -> {
            writer.append('a');
            return writer;
        }, () -> {
            writer.write(new char[] { 'a', 'b', 'c' }, 1, 1);
            return writer;
        }, () -> {
            writer.flush();
            return writer;
        });
    }
}
