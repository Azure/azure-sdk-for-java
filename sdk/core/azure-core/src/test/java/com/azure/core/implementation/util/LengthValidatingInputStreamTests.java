// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.exception.UnexpectedLengthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link LengthValidatingInputStream}.
 */
public class LengthValidatingInputStreamTests {
    @Test
    public void nullInnerInputStreamThrows() {
        assertThrows(NullPointerException.class, () -> new LengthValidatingInputStream(null, 0));
    }

    @Test
    public void negativeExpectedReadSizeThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new LengthValidatingInputStream(new ByteArrayInputStream(new byte[0]), -1));
    }

    @Test
    public void innerStreamIsTooSmall() {
        InputStream inner = new ByteArrayInputStream(new byte[4095]);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        assertThrows(UnexpectedLengthException.class, () -> readStream(validatorStream));
    }

    @Test
    public void innerStreamIsTooLarge() {
        InputStream inner = new ByteArrayInputStream(new byte[4097]);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        assertThrows(UnexpectedLengthException.class, () -> readStream(validatorStream));
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 4096, 4097 })
    public void canReadStream(int bufferSize) throws Exception {
        byte[] bytes = new byte[4096];
        new Random().nextBytes(bytes);
        InputStream inner = new ByteArrayInputStream(bytes);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        byte[] afterRead = readStream(validatorStream, 4096);

        assertArrayEquals(bytes, afterRead);
    }

    private byte[] readStream(InputStream stream) throws Exception {
        return readStream(stream, 8 * 1024);
    }

    private byte[] readStream(InputStream stream, int bufferSize) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }
}
