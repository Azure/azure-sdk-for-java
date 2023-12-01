// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepeatingInputStreamTests {
    @ParameterizedTest
    @ValueSource(ints = {100, 2 * 1024 * 1024})
    public void generateRandomStream(int length) throws IOException {
        RepeatingInputStream stream = new RepeatingInputStream(length);
        assertEquals(length, stream.available());
        assertTrue(stream.markSupported());

        byte[] allBytes = readAllBytes(stream, length, length);

        assertEquals(0, stream.available());
        stream.reset();
        assertEquals(length, stream.available());

        assertContentEquals(allBytes, stream, 11);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 2 * 1024 * 1024})
    public void generateStreamFromSource(int length) throws IOException {
        String bufferStr = "Sample content - we'll repeat this string.";
        String expected = String.join("", Collections.nCopies((length/bufferStr.length()) + 1, bufferStr)).substring(0, length);

        RepeatingInputStream stream = new RepeatingInputStream(BinaryData.fromString(bufferStr), length);
        assertEquals(length, stream.available());

        byte[] allBytes = readAllBytes(stream, length, length);
        assertEquals(expected, new String(allBytes, StandardCharsets.UTF_8));
        stream.reset();

        assertContentEquals(allBytes, stream, 37);
    }

    private byte[] readAllBytes(InputStream actual, int step, int length) throws IOException {
        byte[] buffer = new byte[length];
        int pos = 0;
        int read;
        while ((read = actual.read(buffer, pos, step)) > 0) {
            pos += read;
        }

        return buffer;
    }

    private void assertContentEquals(byte[] expected, InputStream actual, int step) throws IOException {
        byte[] buffer = readAllBytes(actual, step, expected.length);
        for (int i = 0; i < expected.length; i ++) {
            assertEquals(expected[i], buffer[i]);
        }
    }
}

