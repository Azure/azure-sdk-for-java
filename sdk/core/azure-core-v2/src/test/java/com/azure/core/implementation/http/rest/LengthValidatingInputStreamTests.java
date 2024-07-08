// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http.rest;

import com.azure.core.v2.exception.UnexpectedLengthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static com.azure.core.CoreTestUtils.readStream;
import static com.azure.core.CoreTestUtils.readStreamByteByByte;
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
        assertThrows(UnexpectedLengthException.class, () -> readStreamByteByByte(validatorStream));
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.mark(Integer.MAX_VALUE);
            validatorStream.read(new byte[12]);
            validatorStream.reset();
            readStream(validatorStream);
        });
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.mark(Integer.MAX_VALUE);
            validatorStream.read(new byte[12]);
            validatorStream.reset();
            readStreamByteByByte(validatorStream);
        });
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.skip(10);
            readStream(validatorStream);
        });
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.skip(10);
            readStreamByteByByte(validatorStream);
        });
    }

    @Test
    public void innerStreamIsTooLarge() {
        InputStream inner = new ByteArrayInputStream(new byte[4097]);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        assertThrows(UnexpectedLengthException.class, () -> readStream(validatorStream));
        assertThrows(UnexpectedLengthException.class, () -> readStreamByteByByte(validatorStream));
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.mark(Integer.MAX_VALUE);
            validatorStream.read(new byte[12]);
            validatorStream.reset();
            readStream(validatorStream);
        });
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.mark(Integer.MAX_VALUE);
            validatorStream.read(new byte[12]);
            validatorStream.reset();
            readStreamByteByByte(validatorStream);
        });
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.skip(10);
            readStream(validatorStream);
        });
        assertThrows(UnexpectedLengthException.class, () -> {
            validatorStream.skip(10);
            readStreamByteByByte(validatorStream);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 4096, 4097 })
    public void canReadStream(int bufferSize) throws Exception {
        byte[] bytes = new byte[4096];
        fillArray(bytes);
        InputStream inner = new ByteArrayInputStream(bytes);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        byte[] afterRead = readStream(validatorStream, 4096);

        assertArraysEqual(bytes, afterRead);
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 4096, 4097 })
    public void canReadStreamWithReset(int bufferSize) throws Exception {
        byte[] bytes = new byte[4096];
        fillArray(bytes);
        InputStream inner = new ByteArrayInputStream(bytes);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        validatorStream.mark(Integer.MAX_VALUE);
        validatorStream.read(new byte[12]);
        validatorStream.reset();
        byte[] afterRead = readStream(validatorStream, 4096);

        assertArraysEqual(bytes, afterRead);
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 4096, 4097 })
    public void canReadStreamWithSkip(int bufferSize) throws Exception {
        byte[] bytes = new byte[4096];
        fillArray(bytes);
        InputStream inner = new ByteArrayInputStream(bytes);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        validatorStream.skip(10);
        byte[] afterRead = readStream(validatorStream, 4096);

        assertArraysEqual(bytes, 10, bytes.length - 10, afterRead);
    }

    @Test
    public void canReadStreamByteByByte() throws Exception {
        byte[] bytes = new byte[4096];
        fillArray(bytes);
        InputStream inner = new ByteArrayInputStream(bytes);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        byte[] afterRead = readStreamByteByByte(validatorStream);

        assertArraysEqual(bytes, afterRead);
    }

    @Test
    public void canReadStreamByteByByteWithReset() throws Exception {
        byte[] bytes = new byte[4096];
        fillArray(bytes);
        InputStream inner = new ByteArrayInputStream(bytes);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        validatorStream.mark(Integer.MAX_VALUE);
        validatorStream.read(new byte[12]);
        validatorStream.reset();
        byte[] afterRead = readStreamByteByByte(validatorStream);

        assertArraysEqual(bytes, afterRead);
    }

    @Test
    public void canReadStreamByteByByteWithSkip() throws Exception {
        byte[] bytes = new byte[4096];
        fillArray(bytes);
        InputStream inner = new ByteArrayInputStream(bytes);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        validatorStream.skip(10);
        byte[] afterRead = readStreamByteByByte(validatorStream);

        assertArraysEqual(bytes, 10, bytes.length - 10, afterRead);
    }
}
