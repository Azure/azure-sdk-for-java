// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.BinaryData;
import com.azure.core.util.io.IOUtils;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.WritableByteChannel;

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
     * If the content length isn't able to be calculated null will be returned.
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
     * {@link JsonSerializer}.
     *
     * <p><strong>Get a non-generic Object from the BinaryDataContent</strong></p>
     *
     * @param <T> Type of the deserialized Object.
     * @param typeReference The {@link Class} representing the Object's type.
     * @return An {@link Object} representing the JSON deserialized {@link BinaryDataContent}.
     * @throws NullPointerException If {@code typeReference} is null.
     * @see JsonSerializer
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
     * Converts the {@link BinaryDataContent} into a {@code Flux<ByteBuffer>} for use in reactive streams.
     *
     * @return The {@link BinaryDataContent} as a {@code Flux<ByteBuffer>}.
     */
    public abstract Flux<ByteBuffer> toFluxByteBuffer();

    /**
     * Returns a flag indicating whether the content can be repeatedly consumed using all accessors including
     * {@link #toStream()} and {@link #toFluxByteBuffer()}
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
     * @return Mono that emits replayable {@link BinaryDataContent}.
     */
    public abstract Mono<BinaryDataContent> toReplayableContentAsync();

    /**
     * Asynchronously writes the contents of this {@link BinaryDataContent} to the {@link AsynchronousByteChannel}.
     * <p>
     * {@link IOUtils#toAsynchronousByteChannel(AsynchronousFileChannel, long)} can be used to convert an
     * {@link AsynchronousFileChannel} to an {@link AsynchronousByteChannel} if writing to a file is required.
     * <p>
     * If the contents of this {@link BinaryDataContent} isn't {@link #isReplayable() replayable} calling this method
     * will consume the contents in completion. If the contents may need to be replayed later call
     * {@link #toReplayableContentAsync()} before calling this method.
     *
     * @param channel The {@link AsynchronousByteChannel} where the contents of this {@link BinaryDataContent} is being
     * written.
     * @return A {@link Mono} indicating completion of writing the contents, or the error that occurred during writing.
     * @throws NullPointerException If {@code channel} is null.
     */
    public abstract Mono<Void> writeToAsync(AsynchronousByteChannel channel);

    /**
     * Synchronously writes the contents of this {@link BinaryDataContent} to the {@link WritableByteChannel}.
     * <p>
     * If the contents of this {@link BinaryDataContent} isn't {@link #isReplayable() replayable} calling this method
     * will consume the contents in completion. If the contents may need to be replayed later call
     * {@link #toReplayableContent()} before calling this method.
     *
     * @param channel The {@link WritableByteChannel} where the contents of this {@link BinaryDataContent} is being
     * written.
     * @throws NullPointerException If {@code channel} is null.
     * @throws IOException If writing to the {@link WritableByteChannel} fails.
     */
    public abstract void writeTo(WritableByteChannel channel) throws IOException;
}
