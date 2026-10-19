// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import java.nio.ByteBuffer;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A {@link ByteBuffer} of a given {@link TestBufferShape}, which remembers its initial state so a test can check what
 * code under test read from it or wrote to it.
 * <p>
 * The <em>exposed</em> bytes are those between the buffer's position and limit when it was created. Code under test
 * may only read or write the exposed bytes. Every other byte of the buffer, and of its backing array when it has an
 * accessible one, must be left unchanged.
 */
public final class ShapedByteBuffer {
    private final TestBufferShape shape;
    private final ByteBuffer buffer;
    private final byte[] storage;
    private final byte[] originalStorage;
    private final int storageStart;
    private final int length;
    private final int originalPosition;
    private final int originalLimit;
    private final byte[] originalView;

    ShapedByteBuffer(TestBufferShape shape, ByteBuffer buffer, byte[] storage, int storageStart, int length) {
        this.shape = shape;
        this.buffer = buffer;
        // Direct and read-only buffers don't expose their storage, so only check their storage through the buffer.
        this.storage = buffer.hasArray() ? storage : null;
        this.originalStorage = storage.clone();
        this.storageStart = storageStart;
        this.length = length;
        this.originalPosition = buffer.position();
        this.originalLimit = buffer.limit();
        this.originalView = viewBytes(buffer, 0, buffer.capacity());

        assertEquals(length, buffer.remaining(), "Buffer of shape " + shape + " exposes the wrong number of bytes.");
    }

    /**
     * @return The kind of buffer.
     */
    public TestBufferShape getShape() {
        return shape;
    }

    /**
     * @return The buffer to pass to the code under test.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * @return The number of exposed bytes.
     */
    public int getLength() {
        return length;
    }

    /**
     * @return The buffer's position when it was created.
     */
    public int getOriginalPosition() {
        return originalPosition;
    }

    /**
     * @return The buffer's limit when it was created.
     */
    public int getOriginalLimit() {
        return originalLimit;
    }

    /**
     * Gets the exposed bytes as they are now, such as after code under test wrote to them. Doesn't modify the buffer.
     *
     * @return The exposed bytes.
     */
    public byte[] getExposedBytes() {
        return viewBytes(buffer, originalPosition, originalPosition + length);
    }

    /**
     * Asserts that every byte other than the exposed bytes is unchanged, both as seen through the buffer and, when it
     * has an accessible one, in its backing array. Use this after code under test wrote to the buffer.
     */
    public void assertOnlyExposedBytesChanged() {
        byte[] view = viewBytes(buffer, 0, buffer.capacity());
        int exposedEnd = originalPosition + length;
        assertArraysEqual(originalView, 0, view, 0, originalPosition);
        assertArraysEqual(originalView, exposedEnd, view, exposedEnd, view.length - exposedEnd);

        if (storage != null) {
            int storageEnd = storageStart + length;
            assertArraysEqual(originalStorage, 0, storage, 0, storageStart);
            assertArraysEqual(originalStorage, storageEnd, storage, storageEnd, storage.length - storageEnd);
        }
    }

    /**
     * Asserts that every byte, including the exposed bytes, is unchanged. Use this after code under test read from the
     * buffer, which must not modify the caller's data.
     */
    public void assertNoBytesChanged() {
        assertArraysEqual(originalView, viewBytes(buffer, 0, buffer.capacity()));
        if (storage != null) {
            assertArraysEqual(originalStorage, storage);
        }
    }

    /**
     * Asserts the buffer's position and limit, which code under test may advance as it reads or writes.
     *
     * @param expectedPosition The expected position.
     * @param expectedLimit The expected limit.
     */
    public void assertPositionAndLimit(int expectedPosition, int expectedLimit) {
        assertEquals(expectedPosition, buffer.position(), "position of buffer of shape " + shape);
        assertEquals(expectedLimit, buffer.limit(), "limit of buffer of shape " + shape);
    }

    @Override
    public String toString() {
        return shape + "[length=" + length + "]";
    }

    /*
     * Gets the bytes at the given absolute indexes of the buffer, without modifying it.
     */
    private static byte[] viewBytes(ByteBuffer buffer, int from, int to) {
        ByteBuffer view = buffer.duplicate();
        view.limit(to).position(from);
        byte[] bytes = new byte[to - from];
        view.get(bytes);
        return bytes;
    }
}
