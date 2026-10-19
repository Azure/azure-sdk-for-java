// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The kinds of {@link ByteBuffer} a caller may pass to an SDK method. Each kind exposes the same bytes between its
 * position and limit, but differs in how those bytes map onto its backing storage. Code that reads or writes caller
 * buffers must handle every kind; in particular it must not assume that a buffer's position is 0, that the buffer's
 * index 0 is the backing array's index 0 ({@link ByteBuffer#arrayOffset()} is 0), or that the buffer has an accessible
 * array.
 * <p>
 * Where the kind allows it, the exposed bytes are surrounded by other bytes in the backing storage, so a test can
 * detect code that reads or writes outside the region between the buffer's position and limit. See
 * {@link ShapedByteBuffer}.
 */
public enum TestBufferShape {
    /**
     * Backed by an array that holds exactly the exposed bytes. Position is 0 and {@code arrayOffset()} is 0.
     */
    HEAP(true, true),

    /**
     * Backed by an array, with a position that is not 0, as created by {@link ByteBuffer#wrap(byte[], int, int)}.
     * {@code arrayOffset()} is 0.
     */
    POSITIONED(true, true),

    /**
     * Backed by an array, with a position that is not 0 because bytes before it have already been read.
     * {@code arrayOffset()} is 0.
     */
    PARTIALLY_READ(true, true),

    /**
     * Created by {@link ByteBuffer#slice()}, so {@code arrayOffset()} is not 0. Position is 0.
     */
    SLICED(true, true),

    /**
     * Created by {@link ByteBuffer#slice()} and then positioned, so both {@code arrayOffset()} and position are not
     * 0.
     */
    SLICED_POSITIONED(true, true),

    /**
     * A direct buffer, which has no accessible array. Position is not 0.
     */
    DIRECT(false, true),

    /**
     * A read-only view of a heap buffer, which has no accessible array and cannot be written to. Position is not 0.
     */
    READ_ONLY(false, false);

    /**
     * The number of bytes in the backing storage before the exposed bytes, for kinds that have them. For SLICED kinds,
     * these are only in the backing array, not visible through the buffer.
     */
    static final int PREFIX_LENGTH = 7;

    /**
     * The number of bytes in the backing storage after the exposed bytes, for kinds that have them. For SLICED kinds,
     * these are only in the backing array, not visible through the buffer, because a slice ends at its limit.
     */
    static final int SUFFIX_LENGTH = 5;

    // How far into the backing storage a SLICED_POSITIONED slice starts. Its position is the rest of the prefix.
    private static final int SLICE_START = 3;

    private final boolean hasAccessibleArray;
    private final boolean writable;

    TestBufferShape(boolean hasAccessibleArray, boolean writable) {
        this.hasAccessibleArray = hasAccessibleArray;
        this.writable = writable;
    }

    /**
     * @return Whether buffers of this kind have an accessible backing array ({@link ByteBuffer#hasArray()}).
     */
    public boolean hasAccessibleArray() {
        return hasAccessibleArray;
    }

    /**
     * @return Whether buffers of this kind can be written to, such as by a download into the buffer.
     */
    public boolean isWritable() {
        return writable;
    }

    /**
     * @return The kinds of buffer that can be written to, for tests that read data into a buffer.
     */
    public static List<TestBufferShape> writableShapes() {
        List<TestBufferShape> shapes = new ArrayList<>();
        for (TestBufferShape shape : values()) {
            if (shape.writable) {
                shapes.add(shape);
            }
        }
        return Collections.unmodifiableList(shapes);
    }

    /**
     * Creates a buffer of this kind whose bytes between its position and limit are {@code content}. Use this for the
     * source of an upload or a write.
     *
     * @param content The bytes the buffer exposes. Not modified.
     * @return The buffer, with the means to check that only the exposed bytes were read or written.
     */
    public ShapedByteBuffer wrap(byte[] content) {
        if (this == HEAP) {
            byte[] storage = Arrays.copyOf(content, content.length);
            return new ShapedByteBuffer(this, ByteBuffer.wrap(storage), storage, 0, content.length);
        }

        byte[] storage = new byte[PREFIX_LENGTH + content.length + SUFFIX_LENGTH];
        // The surrounding bytes are random, so they are unlikely to be mistaken for content.
        new Random(31L * content.length + 17).nextBytes(storage);
        System.arraycopy(content, 0, storage, PREFIX_LENGTH, content.length);
        return new ShapedByteBuffer(this, create(storage, content.length), storage, PREFIX_LENGTH, content.length);
    }

    /**
     * Creates a buffer of this kind that exposes {@code length} bytes between its position and limit, with random
     * content. Use this as the destination of a download or a read.
     *
     * @param length The number of bytes the buffer exposes.
     * @return The buffer, with the means to check that only the exposed bytes were read or written.
     */
    public ShapedByteBuffer allocate(int length) {
        byte[] content = new byte[length];
        new Random(length + 1).nextBytes(content);
        return wrap(content);
    }

    /**
     * Creates buffers that together expose {@code content}, in pieces of at most {@code pieceLength} bytes, cycling
     * through every kind of buffer. Use this as the source of an upload that takes a sequence of buffers, to cover
     * every kind of buffer, and the boundaries between them, in a single upload.
     *
     * @param content The bytes the buffers expose, in order. Not modified.
     * @param pieceLength The maximum number of bytes each buffer exposes.
     * @return The buffers, in order.
     */
    public static List<ShapedByteBuffer> mixed(byte[] content, int pieceLength) {
        if (pieceLength < 1) {
            throw new IllegalArgumentException("'pieceLength' must be at least 1.");
        }

        List<ShapedByteBuffer> buffers = new ArrayList<>();
        TestBufferShape[] shapes = values();
        for (int offset = 0, i = 0; offset < content.length; offset += pieceLength, i++) {
            int end = Math.min(offset + pieceLength, content.length);
            buffers.add(shapes[i % shapes.length].wrap(Arrays.copyOfRange(content, offset, end)));
        }
        return buffers;
    }

    /*
     * Creates a buffer of this kind over storage, exposing length bytes starting at PREFIX_LENGTH.
     */
    private ByteBuffer create(byte[] storage, int length) {
        switch (this) {
            case POSITIONED:
                return ByteBuffer.wrap(storage, PREFIX_LENGTH, length);

            case PARTIALLY_READ:
                ByteBuffer partiallyRead = ByteBuffer.wrap(storage, 0, PREFIX_LENGTH + length);
                partiallyRead.get(new byte[PREFIX_LENGTH]);
                return partiallyRead;

            case SLICED:
                return ByteBuffer.wrap(storage, PREFIX_LENGTH, length).slice();

            case SLICED_POSITIONED:
                int positionInSlice = PREFIX_LENGTH - SLICE_START;
                ByteBuffer sliced = ByteBuffer.wrap(storage, SLICE_START, positionInSlice + length).slice();
                sliced.position(positionInSlice);
                return sliced;

            case DIRECT:
                ByteBuffer direct = ByteBuffer.allocateDirect(storage.length);
                direct.put(storage);
                direct.limit(PREFIX_LENGTH + length);
                direct.position(PREFIX_LENGTH);
                return direct;

            case READ_ONLY:
                return ByteBuffer.wrap(storage, PREFIX_LENGTH, length).asReadOnlyBuffer();

            default:
                throw new IllegalStateException("Unexpected buffer shape: " + this);
        }
    }
}
