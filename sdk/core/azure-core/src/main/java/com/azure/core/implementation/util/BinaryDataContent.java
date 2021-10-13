// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

/**
 * An abstract internal representation of the content stored in {@link BinaryData}.
 */
public abstract class BinaryDataContent {

    public static final int STREAM_READ_SIZE = 8092;

    /**
     * Gets the length of the {@link BinaryDataContent} if it is able to be calculated.
     * <p>
     * If the content length isn't able to be calculated null will be returned.
     * @return The length of the {@link BinaryDataContent} if it is able to be calculated, otherwise null.
     */
    public abstract Long getLength();

    /**
     * Returns a {@link String} representation of this {@link BinaryDataContent} by converting its data using the UTF-8
     * character set.
     * @return A {@link String} representing this {@link BinaryDataContent}.
     */
    public abstract String toString();

    /**
     * Returns a byte array representation of this {@link BinaryDataContent}.
     * @return A byte array representing this {@link BinaryDataContent}.
     */
    public abstract byte[] toBytes();

    /**
     * Returns an {@link Object} representation of this {@link BinaryDataContent} by deserializing its data using the
     * {@link JsonSerializer}.
     *
     * <p><strong>Get a non-generic Object from the BinaryDataContent</strong></p>
     * @param <T> Type of the deserialized Object.
     * @param typeReference The {@link Class} representing the Object's type.
     * @return An {@link Object} representing the JSON deserialized {@link BinaryDataContent}.
     * @throws NullPointerException If {@code typeReference} is null.
     * @see JsonSerializer
     */
    public abstract <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer);

    /**
     * Returns an {@link InputStream} representation of this {@link BinaryDataContent}.
     * @return An {@link InputStream} representing the {@link BinaryDataContent}.
     */
    public abstract InputStream toStream();

    /**
     * Returns a read-only {@link ByteBuffer} representation of this {@link BinaryDataContent}.
     * <p>
     * Attempting to mutate the returned {@link ByteBuffer} will throw a {@link ReadOnlyBufferException}.
     *
     * @return A read-only {@link ByteBuffer} representing the {@link BinaryDataContent}.
     */
    public abstract ByteBuffer toByteBuffer();

    /**
     * Converts the {@link BinaryDataContent} into a {@code Flux<ByteBuffer>} for use in reactive streams.
     * @return The {@link BinaryDataContent} as a {@code Flux<ByteBuffer>}.
     */
    public abstract Flux<ByteBuffer> toFluxByteBuffer();
}
