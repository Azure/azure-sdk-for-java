// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.test.shared.ByteBufferState;
import com.azure.storage.common.test.shared.TestByteBufferVariant;

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
 * Tests for {@link TestByteBufferVariant} and {@link ByteBufferState}, so tests that use them can rely on each kind of
 * buffer having the properties it claims, and on the assertions detecting reads and writes outside the exposed bytes.
 */
public class TestByteBufferVariantTests {
    private static final int LENGTH = 100;

    @ParameterizedTest
    @EnumSource(TestByteBufferVariant.class)
    public void wrapExposesContent(TestByteBufferVariant variant) {
        byte[] content = randomBytes(LENGTH, 1);
        ByteBufferState state = variant.wrap(content);
        ByteBuffer buffer = state.getBuffer();

        assertEquals(variant, state.getVariant());
        assertEquals(LENGTH, buffer.remaining());
        assertEquals(LENGTH, state.getLength());
        assertArraysEqual(content, state.getExposedBytes());
        assertEquals(variant.hasAccessibleArray(), buffer.hasArray());
        assertEquals(!variant.isWritable(), buffer.isReadOnly());
        state.assertNoBytesChanged();
        state.assertPositionAndLimit(state.getOriginalPosition(), state.getOriginalLimit());
    }

    @ParameterizedTest
    @EnumSource(TestByteBufferVariant.class)
    public void wrapCreatesTheDescribedKindOfBuffer(TestByteBufferVariant variant) {
        ByteBuffer buffer = variant.wrap(randomBytes(LENGTH, 1)).getBuffer();

        switch (variant) {
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
                throw new IllegalStateException("Untested buffer variant: " + variant);
        }

        // Every kind except HEAP has bytes after the exposed bytes, to detect reads and writes past the limit. A slice
        // ends at its limit, so for SLICED kinds those bytes are only in the backing array.
        if (variant == TestByteBufferVariant.SLICED || variant == TestByteBufferVariant.SLICED_POSITIONED) {
            assertEquals(buffer.limit(), buffer.capacity());
            assertTrue(buffer.arrayOffset() + buffer.limit() < buffer.array().length);
        } else if (variant != TestByteBufferVariant.HEAP) {
            assertTrue(buffer.limit() < buffer.capacity());
        }
    }

    @ParameterizedTest
    @EnumSource(TestByteBufferVariant.class)
    public void allocateExposesRequestedLength(TestByteBufferVariant variant) {
        ByteBufferState state = variant.allocate(LENGTH);

        assertEquals(LENGTH, state.getBuffer().remaining());
        assertEquals(LENGTH, state.getExposedBytes().length);
    }

    @Test
    public void writableVariantsExcludesReadOnly() {
        List<TestByteBufferVariant> writable = TestByteBufferVariant.writableVariants();

        assertFalse(writable.contains(TestByteBufferVariant.READ_ONLY));
        assertEquals(TestByteBufferVariant.values().length - 1, writable.size());
    }

    @ParameterizedTest
    @EnumSource(value = TestByteBufferVariant.class, names = "READ_ONLY", mode = EnumSource.Mode.EXCLUDE)
    public void writingOnlyExposedBytesIsAllowed(TestByteBufferVariant variant) {
        ByteBufferState state = variant.allocate(LENGTH);
        byte[] written = randomBytes(LENGTH, 2);

        state.getBuffer().put(written);

        assertArraysEqual(written, state.getExposedBytes());
        state.assertOnlyExposedBytesChanged();
        state.assertPositionAndLimit(state.getOriginalLimit(), state.getOriginalLimit());
        assertThrows(AssertionFailedError.class, state::assertNoBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(value = TestByteBufferVariant.class, names = { "HEAP", "READ_ONLY" }, mode = EnumSource.Mode.EXCLUDE)
    public void writingPastLimitIsDetected(TestByteBufferVariant variant) {
        ByteBufferState state = variant.allocate(LENGTH);
        ByteBuffer buffer = state.getBuffer();

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

        assertThrows(AssertionFailedError.class, state::assertOnlyExposedBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(
        value = TestByteBufferVariant.class,
        names = { "POSITIONED", "PARTIALLY_READ", "SLICED_POSITIONED", "DIRECT" })
    public void writingBeforePositionIsDetected(TestByteBufferVariant variant) {
        ByteBufferState state = variant.allocate(LENGTH);
        ByteBuffer buffer = state.getBuffer();
        int before = buffer.position() - 1;

        buffer.put(before, (byte) ~buffer.get(before));

        assertThrows(AssertionFailedError.class, state::assertOnlyExposedBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(value = TestByteBufferVariant.class, names = { "SLICED", "SLICED_POSITIONED" })
    public void writingBackingArrayOutsideSliceIsDetected(TestByteBufferVariant variant) {
        ByteBufferState state = variant.allocate(LENGTH);
        ByteBuffer buffer = state.getBuffer();
        byte[] array = buffer.array();
        // The mistake of indexing the backing array with the buffer's position, ignoring arrayOffset(), which writes
        // before the region the buffer exposes.
        int wrongIndex = buffer.position();
        assertTrue(wrongIndex < buffer.arrayOffset() + buffer.position());

        array[wrongIndex] = (byte) ~array[wrongIndex];

        assertThrows(AssertionFailedError.class, state::assertOnlyExposedBytesChanged);
    }

    @Test
    public void mixedCoversContentWithEveryVariant() {
        byte[] content = randomBytes(10 * TestByteBufferVariant.values().length + 3, 3);
        int pieceLength = 5;

        List<ByteBufferState> buffers = TestByteBufferVariant.mixed(content, pieceLength);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        for (int i = 0; i < buffers.size(); i++) {
            ByteBufferState state = buffers.get(i);
            assertEquals(TestByteBufferVariant.values()[i % TestByteBufferVariant.values().length], state.getVariant());
            assertTrue(state.getLength() <= pieceLength);
            byte[] exposed = state.getExposedBytes();
            combined.write(exposed, 0, exposed.length);
        }
        assertArraysEqual(content, combined.toByteArray());
        // The last piece is partial.
        assertEquals(content.length % pieceLength, buffers.get(buffers.size() - 1).getLength());
    }

    @Test
    public void mixedRejectsInvalidPieceLength() {
        assertThrows(IllegalArgumentException.class, () -> TestByteBufferVariant.mixed(randomBytes(LENGTH, 4), 0));
    }

    private static byte[] randomBytes(int length, long seed) {
        byte[] bytes = new byte[length];
        new Random(seed).nextBytes(bytes);
        return bytes;
    }
}
