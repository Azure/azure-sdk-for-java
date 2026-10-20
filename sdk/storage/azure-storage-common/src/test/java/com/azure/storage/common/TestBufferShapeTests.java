// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TestBufferShape} and {@link ShapedByteBuffer}, so tests that use them can rely on each kind of
 * buffer having the properties it claims, and on the assertions detecting reads and writes outside the exposed bytes.
 */
public class TestBufferShapeTests {
    private static final int LENGTH = 100;

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void wrapExposesContent(TestBufferShape shape) {
        byte[] content = randomBytes(LENGTH, 1);
        ShapedByteBuffer shaped = shape.wrap(content);
        ByteBuffer buffer = shaped.getBuffer();

        assertEquals(shape, shaped.getShape());
        assertEquals(LENGTH, buffer.remaining());
        assertEquals(LENGTH, shaped.getLength());
        assertArraysEqual(content, shaped.getExposedBytes());
        assertEquals(shape.hasAccessibleArray(), buffer.hasArray());
        assertEquals(!shape.isWritable(), buffer.isReadOnly());
        shaped.assertNoBytesChanged();
        shaped.assertPositionAndLimit(shaped.getOriginalPosition(), shaped.getOriginalLimit());
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void wrapCreatesTheDescribedKindOfBuffer(TestBufferShape shape) {
        ByteBuffer buffer = shape.wrap(randomBytes(LENGTH, 1)).getBuffer();

        switch (shape) {
            case HEAP:
                assertEquals(0, buffer.position());
                assertEquals(0, buffer.arrayOffset());
                assertEquals(LENGTH, buffer.capacity());
                break;

            case POSITIONED:
            case PARTIALLY_READ:
                assertTrue(buffer.position() > 0);
                assertEquals(0, buffer.arrayOffset());
                break;

            case SLICED:
                assertEquals(0, buffer.position());
                assertTrue(buffer.arrayOffset() > 0);
                break;

            case SLICED_POSITIONED:
                assertTrue(buffer.position() > 0);
                assertTrue(buffer.arrayOffset() > 0);
                break;

            case DIRECT:
                assertTrue(buffer.isDirect());
                assertTrue(buffer.position() > 0);
                break;

            case READ_ONLY:
                assertTrue(buffer.position() > 0);
                break;

            default:
                throw new IllegalStateException("Untested buffer shape: " + shape);
        }

        // Every kind except HEAP has bytes after the exposed bytes, to detect reads and writes past the limit. A slice
        // ends at its limit, so for SLICED kinds those bytes are only in the backing array.
        if (shape == TestBufferShape.SLICED || shape == TestBufferShape.SLICED_POSITIONED) {
            assertEquals(buffer.limit(), buffer.capacity());
            assertTrue(buffer.arrayOffset() + buffer.limit() < buffer.array().length);
        } else if (shape != TestBufferShape.HEAP) {
            assertTrue(buffer.limit() < buffer.capacity());
        }
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void allocateExposesRequestedLength(TestBufferShape shape) {
        ShapedByteBuffer shaped = shape.allocate(LENGTH);

        assertEquals(LENGTH, shaped.getBuffer().remaining());
        assertEquals(LENGTH, shaped.getExposedBytes().length);
    }

    @Test
    public void writableShapesExcludesReadOnly() {
        List<TestBufferShape> writable = TestBufferShape.writableShapes();

        assertFalse(writable.contains(TestBufferShape.READ_ONLY));
        assertEquals(TestBufferShape.values().length - 1, writable.size());
    }

    @ParameterizedTest
    @EnumSource(value = TestBufferShape.class, names = "READ_ONLY", mode = EnumSource.Mode.EXCLUDE)
    public void writingOnlyExposedBytesIsAllowed(TestBufferShape shape) {
        ShapedByteBuffer shaped = shape.allocate(LENGTH);
        byte[] written = randomBytes(LENGTH, 2);

        shaped.getBuffer().put(written);

        assertArraysEqual(written, shaped.getExposedBytes());
        shaped.assertOnlyExposedBytesChanged();
        shaped.assertPositionAndLimit(shaped.getOriginalLimit(), shaped.getOriginalLimit());
        assertThrows(AssertionFailedError.class, shaped::assertNoBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(value = TestBufferShape.class, names = { "HEAP", "READ_ONLY" }, mode = EnumSource.Mode.EXCLUDE)
    public void writingPastLimitIsDetected(TestBufferShape shape) {
        ShapedByteBuffer shaped = shape.allocate(LENGTH);
        ByteBuffer buffer = shaped.getBuffer();

        if (buffer.capacity() > buffer.limit()) {
            // Absolute puts are bounded by the limit, so write past it through a view with a larger limit.
            ByteBuffer view = buffer.duplicate();
            view.limit(view.capacity());
            view.put(buffer.limit(), (byte) ~view.get(buffer.limit()));
        } else {
            // A slice ends at its limit, so the bytes after it are only in the backing array.
            byte[] array = buffer.array();
            int afterLimit = buffer.arrayOffset() + buffer.limit();
            array[afterLimit] = (byte) ~array[afterLimit];
        }

        assertThrows(AssertionFailedError.class, shaped::assertOnlyExposedBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(
        value = TestBufferShape.class,
        names = { "POSITIONED", "PARTIALLY_READ", "SLICED_POSITIONED", "DIRECT" })
    public void writingBeforePositionIsDetected(TestBufferShape shape) {
        ShapedByteBuffer shaped = shape.allocate(LENGTH);
        ByteBuffer buffer = shaped.getBuffer();
        int before = buffer.position() - 1;

        buffer.put(before, (byte) ~buffer.get(before));

        assertThrows(AssertionFailedError.class, shaped::assertOnlyExposedBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(value = TestBufferShape.class, names = { "SLICED", "SLICED_POSITIONED" })
    public void writingBackingArrayOutsideSliceIsDetected(TestBufferShape shape) {
        ShapedByteBuffer shaped = shape.allocate(LENGTH);
        ByteBuffer buffer = shaped.getBuffer();
        byte[] array = buffer.array();
        // The mistake of indexing the backing array with the buffer's position, ignoring arrayOffset(), which writes
        // before the region the buffer exposes.
        int wrongIndex = buffer.position();
        assertTrue(wrongIndex < buffer.arrayOffset() + buffer.position());

        array[wrongIndex] = (byte) ~array[wrongIndex];

        assertThrows(AssertionFailedError.class, shaped::assertOnlyExposedBytesChanged);
    }

    @Test
    public void mixedCoversContentWithEveryShape() {
        byte[] content = randomBytes(10 * TestBufferShape.values().length + 3, 3);
        int pieceLength = 5;

        List<ShapedByteBuffer> buffers = TestBufferShape.mixed(content, pieceLength);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        for (int i = 0; i < buffers.size(); i++) {
            ShapedByteBuffer shaped = buffers.get(i);
            assertEquals(TestBufferShape.values()[i % TestBufferShape.values().length], shaped.getShape());
            assertTrue(shaped.getLength() <= pieceLength);
            byte[] exposed = shaped.getExposedBytes();
            combined.write(exposed, 0, exposed.length);
        }
        assertArraysEqual(content, combined.toByteArray());
        // The last piece is partial.
        assertEquals(content.length % pieceLength, buffers.get(buffers.size() - 1).getLength());
    }

    @Test
    public void mixedRejectsInvalidPieceLength() {
        assertThrows(IllegalArgumentException.class, () -> TestBufferShape.mixed(randomBytes(LENGTH, 4), 0));
    }

    private static byte[] randomBytes(int length, long seed) {
        byte[] bytes = new byte[length];
        new Random(seed).nextBytes(bytes);
        return bytes;
    }
}
