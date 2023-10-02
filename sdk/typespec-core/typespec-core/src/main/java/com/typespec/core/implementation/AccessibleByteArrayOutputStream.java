// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * This class is an extension of {@link ByteArrayOutputStream} which allows access to the backing {@code byte[]} without
 * requiring a copying of the data. The only use of this class is for internal purposes where we know it is safe to
 * directly access the {@code byte[]} without copying.
 * <p>
 * This class isn't meant to be thread-safe as usage should be internal to azure-core and should be guarded
 * appropriately when used.
 */
public class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {
    /**
     * Constructs an instance of {@link AccessibleByteArrayOutputStream} with the default initial capacity.
     */
    public AccessibleByteArrayOutputStream() {
        super();
    }

    /**
     * Constructs an instance of {@link AccessibleByteArrayOutputStream} with a specified initial capacity.
     *
     * @param initialCapacity The initial capacity.
     * @throws IllegalArgumentException If {@code initialCapacity} is less than 0.
     */
    public AccessibleByteArrayOutputStream(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

    // Commenting out as this isn't used but may be added back in the future.
//    /**
//     * Returns the internal {@code byte[]} without copying.
//     * <p>
//     * This will be the full {@code byte[]}, so if writing required it to be resized to 8192 bytes but only 6000 bytes
//     * were written the final 2192 bytes will be undefined data. If this is used in an API where a {@code byte[]} is
//     * accepted you must use the range based overload with {@link #count()}, if a range based overload isn't available
//     * use {@link #toByteArray()} which will copy the range of bytes written.
//     *
//     * @return A direct reference to the internal {@code byte[]} where data is being written.
//     */
//    public byte[] toByteArrayUnsafe() {
//        return buf;
//    }

    /**
     * Returns a {@link ByteBuffer} representation of the content written to this stream.
     * <p>
     * The {@link ByteBuffer} will use a direct reference to the internal {@code byte[]} being written, so any
     * modifications to the content already written will be reflected in the {@link ByteBuffer}. Given the direct
     * reference to the internal {@code byte[]} the {@link ByteBuffer} returned by the API will be read-only. Further
     * writing to this stream won't be reflected in the {@link ByteBuffer} as the ByteBuffer will be created using
     * {@code ByteBuffer.wrap(bytes, 0, count())}.
     *
     * @return A read-only {@link ByteBuffer} represented by the internal buffer being written to.
     */
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(buf, 0, count).asReadOnlyBuffer();
    }

    /**
     * The number of bytes that have been written to the stream.
     *
     * @return The number of byte written to the stream.
     */
    public int count() {
        return count;
    }

    /**
     * Gets the string representation of the stream.
     *
     * @param charset The {@link Charset} used to encode the String.
     * @return A string representation of the stream.
     */
    public String toString(Charset charset) {
        return new String(buf, 0, count, charset);
    }

    /**
     * Gets a BOM aware string representation of the stream.
     * <p>
     * This method is the equivalent of calling
     * {@code ImplUtils.bomAwareToString(toByteBufferUnsafe(), 0, count(), contentType)}.
     *
     * @param contentType The {@code Content-Type} header value.
     * @return A string representation of the stream encoded to the found encoding.
     */
    public String bomAwareToString(String contentType) {
        return ImplUtils.bomAwareToString(buf, 0, count, contentType);
    }
}
