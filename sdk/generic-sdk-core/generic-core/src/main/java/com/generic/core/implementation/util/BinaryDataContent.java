// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.util;

import com.generic.core.models.BinaryData;
import com.generic.core.models.TypeReference;
import com.generic.core.util.serializer.JsonSerializer;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

/**
 * An abstract internal representation of the content stored in {@link BinaryData}.
 */
public abstract class BinaryDataContent {
    public static final int STREAM_READ_SIZE = 8192;

    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    static final String TOO_LARGE_FOR_BYTE_ARRAY
        = "The content length is too large for a byte array. Content length is: ";

    /**
     * Gets the length of the {@link BinaryDataContent} if it can be calculated.
     * <p>
     * If the content length cannot be calculated, null will be returned.
     *
     * @return The length of the {@link BinaryDataContent} if it can be calculated, otherwise null.
     */
    public abstract Long getLength();

    /**
     * Returns a {@link String} representation of this {@link BinaryDataContent} by converting its data using the UTF-8
     * character set.
     *
     * @return A {@link String} representing this {@link BinaryDataContent}.
     */
    public abstract String toString();

    /**
     * Returns a byte array representation of this {@link BinaryDataContent}.
     *
     * @return A byte array representing this {@link BinaryDataContent}.
     */
    public abstract byte[] toBytes();


    /**
     * Returns an {@link Object} representation of this {@link BinaryDataContent} by deserializing its data using the
     * {@link ObjectSerializer}.
     *
     * <p><strong>Get a non-generic Object from the BinaryDataContent</strong></p>
     *
     * @param <T> Type of the deserialized Object.
     * @param typeReference The {@link Class} representing the Object's type.
     * @return An {@link Object} representing the JSON deserialized {@link BinaryDataContent}.
     * @throws NullPointerException If {@code typeReference} is null.
     * @see ObjectSerializer
     */
    public abstract <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer);

    /**
     * Returns an {@link InputStream} representation of this {@link BinaryDataContent}.
     *
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
     * Returns a flag indicating whether the content can be repeatedly consumed using all accessors including
     * {@link #toStream()}.
     *
     * <p>
     * Replayability does not imply thread-safety. The caller must not use data accessors simultaneously.
     * </p>
     *
     * @return a flag indicating whether the content can be repeatedly consumed.
     */
    public abstract boolean isReplayable();

    /**
     * Converts the {@link BinaryDataContent} into a {@link BinaryDataContent} that is replayable, i.e. content can be
     * consumed repeatedly using all accessors.
     *
     * <p>
     * A {@link BinaryDataContent} that is already replayable is returned as is. Otherwise techniques like marking and
     * resetting a stream or buffering in memory are employed to assure replayability.
     * </p>
     *
     * <p>
     * Replayability does not imply thread-safety. The caller must not use data accessors simultaneously.
     * </p>
     *
     * @return Replayable {@link BinaryDataContent}.
     */
    public abstract BinaryDataContent toReplayableContent();

    /**
     * Gets the {@link BinaryDataContent} content type.
     *
     * @return The {@link BinaryDataContent} content type.
     */
    public abstract BinaryDataContentType getContentType();
}
