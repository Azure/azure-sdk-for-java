// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.binarydata;

import io.clientcore.core.implementation.utils.ByteBufferUtils;
import io.clientcore.core.implementation.utils.InputStreamUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the IO regression tests against the packaged multi-release JAR.
 */
@EnabledForJreRange(min = JRE.JAVA_11)
public class BinaryDataIOTestsIT extends BinaryDataIOTests {
    @Test
    public void selectsVersionedStreamOperations() {
        assertTrue(
            InputStreamUtils.class.getResource("InputStreamUtils.class").toString().contains("META-INF/versions/11/"));
        assertEquals("ByteBufferInputStream",
            InputStreamUtils.createInputStream(Collections.emptyList()).getClass().getSimpleName());
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_13)
    public void selectsAbsoluteBufferCopies() {
        assertTrue(
            ByteBufferUtils.class.getResource("ByteBufferUtils.class").toString().contains("META-INF/versions/13/"));
    }

    @Test
    public void transferReportsTheRemainingLength() throws Exception {
        byte[] bytes = payload(35);
        InputStream input = InputStreamUtils.createInputStream(buffers(bytes, "heap"));
        input.read();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Object transferred = InputStream.class.getMethod("transferTo", OutputStream.class).invoke(input, output);
        assertEquals(Long.valueOf(bytes.length - 1), transferred);
        assertEquals(bytes.length - 1, output.size());
    }

    @Test
    public void oversizedBulkReadFailsBeforeAllocatingTheResult() {
        InputStream input
            = InputStreamUtils.createInputStream(Collections.nCopies(2049, ByteBuffer.allocate(1024 * 1024)));
        assertEquals("ByteBufferInputStream", input.getClass().getSimpleName());
        assertThrows(OutOfMemoryError.class, () -> InputStreamUtils.readAllBytes(input));
    }

    @Test
    public void usesSpecializedReadsWithoutHoldingTheInputMonitorDuringWrites() throws IOException {
        byte[] expected = payload(35);
        BulkInputStream input = new BulkInputStream(expected);
        assertArrayEquals(expected, BinaryData.fromStream(input).toBytes());
        assertTrue(input.bulkRead);
        input = new BulkInputStream(expected);
        ByteArrayOutputStream output = new MonitorCheckingOutputStream(input);
        BinaryData.fromStream(input).writeTo(output);
        assertFalse(input.bulkTransfer);
        assertArrayEquals(expected, output.toByteArray());
    }

    @Test
    public void largeInMemoryTransfersKeepScratchStorageBounded() {
        byte[] expected = payload(32785);
        BoundedOutputStream output = new BoundedOutputStream();
        BinaryData.fromStream(new ByteArrayInputStream(expected)).writeTo(output);
        assertArrayEquals(expected, output.toByteArray());
        assertTrue(output.largestWrite <= 8192);
    }

    private static final class BulkInputStream extends ByteArrayInputStream {
        private boolean bulkRead;
        private boolean bulkTransfer;

        private BulkInputStream(byte[] bytes) {
            super(bytes);
        }

        public byte[] readAllBytes() {
            bulkRead = true;
            byte[] result = Arrays.copyOfRange(buf, pos, count);
            pos = count;
            return result;
        }

        public long transferTo(OutputStream output) throws IOException {
            bulkTransfer = true;
            int remaining = count - pos;
            output.write(buf, pos, remaining);
            pos = count;
            return remaining;
        }
    }

    private static final class BoundedOutputStream extends ByteArrayOutputStream {
        private int largestWrite;

        @Override
        public void write(byte[] bytes, int offset, int length) {
            largestWrite = Math.max(largestWrite, length);
            super.write(bytes, offset, length);
        }
    }
}
