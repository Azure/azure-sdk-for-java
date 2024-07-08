// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.TestByteArrayOutputStream;
import com.azure.core.v2.util.mocking.MockInputStream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SliceInputStreamTest {

    @Test
    public void ctorValidations() {
        assertThrows(NullPointerException.class, () -> new SliceInputStream(null, 0, 10));
        assertThrows(IllegalArgumentException.class,
            () -> new SliceInputStream(new ByteArrayInputStream(new byte[0]), -1, 10));
        assertThrows(IllegalArgumentException.class,
            () -> new SliceInputStream(new ByteArrayInputStream(new byte[0]), 0, -1));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void canReadByteByByte(byte[] expectedBytes, SliceInputStream sliceInputStream) throws IOException {

        TestByteArrayOutputStream os = new TestByteArrayOutputStream(expectedBytes.length);

        int b;
        while ((b = sliceInputStream.read()) >= 0) {
            os.write(b);
        }

        assertArraysEqual(expectedBytes, os.toByteArrayUnsafe());
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void canReadWithBuffer(byte[] expectedBytes, SliceInputStream sliceInputStream) throws IOException {

        TestByteArrayOutputStream os = new TestByteArrayOutputStream(expectedBytes.length);

        int nRead;
        byte[] buffer = new byte[1024];
        while ((nRead = sliceInputStream.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, nRead);
        }

        assertArraysEqual(expectedBytes, os.toByteArrayUnsafe());
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void canMarkAtTheBeginning(byte[] expectedBytes, SliceInputStream sliceInputStream) throws IOException {

        TestByteArrayOutputStream os = new TestByteArrayOutputStream(expectedBytes.length);

        sliceInputStream.mark(Integer.MAX_VALUE);

        int nRead;
        byte[] buffer = new byte[1024];
        do {
            nRead = sliceInputStream.read(buffer, 0, buffer.length);
        } while (nRead >= 0);

        sliceInputStream.reset();

        while ((nRead = sliceInputStream.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, nRead);
        }

        assertArraysEqual(expectedBytes, os.toByteArrayUnsafe());
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void canMarkInTheMiddle(byte[] expectedBytes, SliceInputStream sliceInputStream) throws IOException {

        TestByteArrayOutputStream os = new TestByteArrayOutputStream(expectedBytes.length);

        int nRead;
        byte[] buffer = new byte[8];
        nRead = sliceInputStream.read(buffer, 0, buffer.length);
        if (nRead > 0) {
            os.write(buffer, 0, nRead);
            sliceInputStream.mark(Integer.MAX_VALUE);

            do {
                nRead = sliceInputStream.read(buffer, 0, buffer.length);
            } while (nRead >= 0);

            sliceInputStream.reset();
        }
        while ((nRead = sliceInputStream.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, nRead);
        }

        assertArraysEqual(expectedBytes, os.toByteArrayUnsafe());
    }

    @Test
    public void delegatesMarkSupported() {
        AtomicBoolean markSupported = new AtomicBoolean(true);
        InputStream innerStream = new MockInputStream() {
            @Override
            public boolean markSupported() {
                return markSupported.compareAndSet(true, false);
            }
        };

        SliceInputStream sliceInputStream = new SliceInputStream(innerStream, 0, 10);

        assertTrue(sliceInputStream.markSupported());
        assertFalse(sliceInputStream.markSupported());
    }

    @Test
    public void delegatesClose() throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        InputStream innerStream = new MockInputStream() {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        SliceInputStream sliceInputStream = new SliceInputStream(innerStream, 0, 10);

        sliceInputStream.close();

        assertTrue(closed.get());
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void canSkip(byte[] expectedBytes, SliceInputStream sliceInputStream) throws IOException {

        TestByteArrayOutputStream os = new TestByteArrayOutputStream(expectedBytes.length);

        int toSkip = 10;

        long skipped = sliceInputStream.skip(toSkip);
        long expectedSkipped = Math.min(toSkip, expectedBytes.length);

        int b;
        while ((b = sliceInputStream.read()) >= 0) {
            os.write(b);
        }

        if (expectedBytes.length < toSkip) {
            expectedBytes = new byte[0];
        } else {
            expectedBytes = Arrays.copyOfRange(expectedBytes, toSkip, expectedBytes.length);
        }

        assertEquals(expectedSkipped, skipped);
        assertArraysEqual(expectedBytes, os.toByteArray());
    }

    private static Stream<Arguments> provideArguments() {
        return Stream.of(0, 1, 2, 5, 13, 145, 1024, 1024 + 114, 10 * 1024 * 1024 + 113).flatMap(dataSize -> {
            byte[] data = new byte[dataSize];
            fillArray(data);

            return Stream.of(0, 1, 2, 6, 15, 150, 998, 2048, 5 * 1024 * 1024, 50 * 1024 * 1024)
                .flatMap(offset -> Stream.of(0, 1, 2, 3, 5, 18, 211, 1568, 2098, 5 * 1024 * 1024, 50 * 1024 * 1024)
                    .map(count -> {
                        byte[] expectedBytes;
                        if (offset >= data.length) {
                            expectedBytes = new byte[0];
                        } else {
                            expectedBytes = Arrays.copyOfRange(data, offset, Math.min(data.length, offset + count));
                        }

                        InputStream innerStream = new ByteArrayInputStream(data);
                        SliceInputStream sliceInputStream = new SliceInputStream(innerStream, offset, count);

                        return Arguments.of(Named.named("expectedBytes", expectedBytes),
                            Named.named(
                                "sliceInputStream innerSize=" + data.length + " offset=" + offset + " count=" + count,
                                sliceInputStream));
                    }));
        });
    }
}
