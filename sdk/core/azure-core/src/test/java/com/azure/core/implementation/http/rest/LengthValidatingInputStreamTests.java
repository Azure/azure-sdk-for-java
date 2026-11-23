// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http.rest;

import com.azure.core.exception.UnexpectedLengthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static com.azure.core.CoreTestUtils.readStream;
import static com.azure.core.CoreTestUtils.readStreamByteByByte;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void oversizedReadDoesNotReturnBytesBeyondDeclaredLength() throws Exception {
        InputStream stream
            = new LengthValidatingInputStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5, 6 }), 4);
        byte[] buffer = new byte[] { -1, -1, -1, -1, -1, -1 };

        assertEquals(4, stream.read(buffer));
        assertEquals(-1, buffer[4]);
        assertEquals(-1, buffer[5]);
        assertThrows(UnexpectedLengthException.class, () -> stream.read(buffer));
    }

    @Test
    public void readSyncFlushedGzipDoesNotWaitForProducerToClose() throws Exception {
        byte[] data = new byte[] { 1, 2, 3, 4 };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        GZIPInputStream gzipInput = null;
        try (PipedInputStream input = new PipedInputStream();
            PipedOutputStream output = new PipedOutputStream(input);
            GZIPOutputStream gzipOutput = new GZIPOutputStream(output, true)) {
            gzipOutput.write(data);
            gzipOutput.flush();

            gzipInput = new GZIPInputStream(input);
            assertEquals(1, gzipInput.available());
            InputStream stream = new LengthValidatingInputStream(gzipInput, data.length);
            byte[] buffer = new byte[8];
            Future<Integer> read = executor.submit(() -> stream.read(buffer));

            assertEquals(data.length, read.get(5, TimeUnit.SECONDS).intValue());
            assertArraysEqual(data, 0, data.length, buffer, data.length);
        } finally {
            executor.shutdownNow();
            if (gzipInput != null) {
                gzipInput.close();
            }
        }
    }

    @Test
    public void readAtDeclaredLengthDoesNotBlockForUnavailableData() throws Exception {
        byte[] data = new byte[] { 1, 2, 3, 4 };
        InputStream inner = new InputStream() {
            private int position;

            @Override
            public int read() {
                throw new AssertionError("Single byte reads must not be used before the caller reads past the body.");
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (position == data.length) {
                    throw new AssertionError("Reads must not block waiting for data beyond the declared length.");
                }
                int read = Math.min(len, data.length - position);
                System.arraycopy(data, position, b, off, read);
                position += read;
                return read;
            }

            @Override
            public int available() {
                return 0;
            }
        };
        InputStream stream = new LengthValidatingInputStream(inner, data.length);
        byte[] buffer = new byte[8];

        assertEquals(data.length, stream.read(buffer));
        assertArraysEqual(data, 0, data.length, buffer, data.length);
    }

    @Test
    public void oversizedSingleByteReadFailsBeforeReturningByte() throws Exception {
        InputStream stream = new LengthValidatingInputStream(new ByteArrayInputStream(new byte[] { 1, 2 }), 1);

        assertEquals(1, stream.read());
        assertThrows(UnexpectedLengthException.class, stream::read);
    }

    @Test
    public void zeroLengthBodyRejectsFirstByte() {
        InputStream stream = new LengthValidatingInputStream(new ByteArrayInputStream(new byte[] { 1 }), 0);

        assertThrows(UnexpectedLengthException.class, stream::read);
    }

    @Test
    public void oversizedSkipFailsImmediately() {
        InputStream stream = new LengthValidatingInputStream(new ByteArrayInputStream(new byte[8]), 4);

        assertThrows(UnexpectedLengthException.class, () -> stream.skip(8));
    }

    @Test
    public void emptyReadDoesNotConsumeOrValidateData() throws Exception {
        InputStream stream = new LengthValidatingInputStream(new ByteArrayInputStream(new byte[] { 1 }), 0);

        assertEquals(0, stream.read(new byte[0]));
        assertThrows(UnexpectedLengthException.class, stream::read);
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
