// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.AsynchronousByteChannel;
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
     * @param serializer The {@link ObjectSerializer} that will be used to deserialize the data.
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
     * Writes the contents of this {@link BinaryDataContent} to the given {@link OutputStream}.
     * <p>
     * This method does not close the {@link OutputStream}.
     * <p>
     * The contents of this {@link BinaryDataContent} will be written without buffering. If the underlying data source
     * isn't {@link #isReplayable()}, after this method is called the {@link BinaryDataContent} will be consumed and
     * can't be read again. If it needs to be read again, use {@link #toReplayableContent()} to create a replayable
     * copy.
     *
     * @param outputStream The {@link OutputStream} to write the contents of this {@link BinaryDataContent} to.
     * @throws NullPointerException If {@code outputStream} is null.
     * @throws IOException If an I/O error occurs.
     */
    public abstract void writeTo(OutputStream outputStream) throws IOException;

    /**
     * Writes the contents of this {@link BinaryDataContent} to the given {@link WritableByteChannel}.
     * <p>
     * This method does not close the {@link WritableByteChannel}.
     * <p>
     * The contents of this {@link BinaryDataContent} will be written without buffering. If the underlying data source
     * isn't {@link #isReplayable()}, after this method is called the {@link BinaryDataContent} will be consumed and
     * can't be read again. If it needs to be read again, use {@link #toReplayableContent()} to create a replayable
     * copy.
     *
     * @param channel The {@link WritableByteChannel} to write the contents of this {@link BinaryDataContent} to.
     * @throws NullPointerException If {@code channel} is null.
     * @throws IOException If an I/O error occurs.
     */
    public abstract void writeTo(WritableByteChannel channel) throws IOException;

    /**
     * Writes the contents of this {@link BinaryDataContent} to the given {@link AsynchronousByteChannel}.
     * <p>
     * This method does not close the {@link AsynchronousByteChannel}.
     * <p>
     * The contents of this {@link BinaryDataContent} will be written without buffering. If the underlying data source
     * isn't {@link #isReplayable()}, after this method is called the {@link BinaryDataContent} will be consumed and
     * can't be read again. If it needs to be read again, use {@link #toReplayableContentAsync()} to create a replayable
     * copy.
     *
     * @param channel The {@link AsynchronousByteChannel} to write the contents of this {@link BinaryDataContent} to.
     * @return A {@link Mono} the completes once content has been written or had an error writing.
     * @throws NullPointerException If {@code channel} is null.
     */
    public abstract Mono<Void> writeTo(AsynchronousByteChannel channel);

    /**
     * Writes the contents of this {@link BinaryDataContent} to the given {@link JsonWriter}.
     * <p>
     * This method does not close or flush the {@link JsonWriter}.
     * <p>
     * The contents of this {@link BinaryDataContent} will be written without buffering. If the underlying data source
     * isn't {@link #isReplayable()}, after this method is called the {@link BinaryDataContent} will be consumed and
     * can't be read again. If it needs to be read again, use {@link #toReplayableContent()} to create a replayable
     * copy.
     *
     * @param jsonWriter The {@link JsonWriter} to write the contents of this {@link BinaryDataContent} to.
     * @throws NullPointerException If {@code jsonWriter} is null.
     * @throws IOException If an I/O error occurs during writing.
     */
    public abstract void writeTo(JsonWriter jsonWriter) throws IOException;

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
     * Gets the {@link BinaryDataContent} content type.
     *
     * @return The {@link BinaryDataContent} content type.
     */
    public abstract BinaryDataContentType getContentType();
}
