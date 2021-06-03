// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.ArrayContent;
import com.azure.core.implementation.util.ByteBufferContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents the content sent as part of a request.
 */
public interface RequestContent {
    /**
     * Converts the {@link RequestContent} into a {@code Flux<ByteBuffer>} for use in reactive streams.
     *
     * @return The {@link RequestContent} as a {@code Flux<ByteBuffer>}.
     */
    Flux<ByteBuffer> asFluxByteBuffer();

    /**
     * Gets the length of the {@link RequestContent} if it is able to be calculated.
     * <p>
     * If the content length isn't able to be calculated null will be returned.
     *
     * @return The length of the {@link RequestContent} if it is able to be calculated, otherwise null.
     */
    Long getLength();

    /**
     * Creates a {@link RequestContent} that uses {@code byte[]} as its data.
     *
     * @param bytes The bytes that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code bytes} is null.
     */
    static RequestContent fromBytes(byte[] bytes) {
        Objects.requireNonNull(bytes, "'bytes' cannot be null.");
        return fromBytes(bytes, 0, bytes.length);
    }

    /**
     * Creates a {@link RequestContent} that uses {@code byte[]} as its data.
     *
     * @param bytes The bytes that will be the {@link RequestContent} data.
     * @param offset Offset in the bytes where the data will begin.
     * @param length Length of the data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code bytes} is null.
     */
    static RequestContent fromBytes(byte[] bytes, int offset, int length) {
        Objects.requireNonNull(bytes, "'bytes' cannot be null.");
        return new ArrayContent(bytes, offset, length);
    }

    /**
     * Creates a {@link RequestContent} that uses {@link String} as its data.
     * <p>
     * The passed {@link String} is converted using {@link StandardCharsets#UTF_8}, if another character set is required
     * use {@link #fromBytes(byte[])} and pass {@link String#getBytes(Charset)} using the required character set.
     *
     * @param content The string that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code content} is null.
     */
    static RequestContent fromString(String content) {
        Objects.requireNonNull(content, "'content' cannot be null.");
        return fromBytes(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a {@link RequestContent} that uses {@link BinaryData} as its data.
     *
     * @param content The {@link BinaryData} that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code content} is null.
     */
    static RequestContent fromBinaryData(BinaryData content) {
        Objects.requireNonNull(content, "'content' cannot be null.");
        return new ByteBufferContent(content.toByteBuffer());
    }

    /**
     * Creates a {@link RequestContent} that uses {@link Path} as its data.
     *
     * @param file The {@link Path} that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code file} is null.
     * @throws UncheckedIOException If the size of the {@code file} cannot be determined.
     */
    static RequestContent fromFile(Path file) {
        Objects.requireNonNull(file, "'file' cannot be null.");
        try {
            return fromFile(file, 0, Files.size(file));
        } catch (IOException e) {
            throw new ClientLogger(RequestContent.class).logExceptionAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Creates a {@link RequestContent} that uses {@link Path} as its data.
     *
     * @param file The {@link Path} that will be the {@link RequestContent} data.
     * @param offset Offset in the {@link Path} where the data will begin.
     * @param length Length of the data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code file} is null.
     */
    static RequestContent fromFile(Path file, long offset, long length) {
        Objects.requireNonNull(file, "'file' cannot be null.");
        return new FileContent(file, offset, length);
    }

    /**
     * Creates a {@link RequestContent} that uses a serialized {@link Object} as its data.
     * <p>
     * This uses an {@link ObjectSerializer} found on the classpath.
     *
     * @param serializable An {@link Object} that will be serialized to be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent fromObject(Object serializable) {
        return fromObject(serializable, JsonSerializerProviders.createInstance(true));
    }

    /**
     * Creates a {@link RequestContent} that uses a serialized {@link Object} as its data.
     *
     * @param serializable An {@link Object} that will be serialized to be the {@link RequestContent} data.
     * @param serializer The {@link ObjectSerializer} that will serialize the {@link Object}.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code serializer} is null.
     */
    static RequestContent fromObject(Object serializable, ObjectSerializer serializer) {
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        return new SerializableContent(serializable, serializer);
    }

    /**
     * Creates a {@link RequestContent} that uses a {@link Flux} of {@link ByteBuffer} as its data.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code content} is null.
     */
    static RequestContent fromFlux(Flux<ByteBuffer> content) {
        Objects.requireNonNull(content, "'content' cannot be null.");
        return new FluxByteBufferContent(content);
    }

    /**
     * Creates a {@link RequestContent} that uses a {@link Flux} of {@link ByteBuffer} as its data.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} that will be the {@link RequestContent} data.
     * @param length The length of the content.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code content} is null.
     * @throws IllegalStateException If {@code length} is less than 0.
     */
    static RequestContent fromFlux(Flux<ByteBuffer> content, long length) {
        Objects.requireNonNull(content, "'content' cannot be null.");
        if (length < 0) {
            throw new ClientLogger(RequestContent.class).logExceptionAsError(new IllegalArgumentException(
                "'length' cannot be less than 0."));
        }

        return new FluxByteBufferContent(content, length);
    }

    /**
     * Creates a {@link RequestContent} that uses a {@link BufferedFluxByteBuffer} as its data.
     *
     * @param content The {@link BufferedFluxByteBuffer} that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code content} is null.
     */
    static RequestContent fromBufferedFlux(BufferedFluxByteBuffer content) {
        Objects.requireNonNull(content, "'content' cannot be null.");
        return new FluxByteBufferContent(content);
    }

    /**
     * Creates a {@link RequestContent} that uses a {@link BufferedFluxByteBuffer} as its data.
     *
     * @param content The {@link BufferedFluxByteBuffer} that will be the {@link RequestContent} data.
     * @param length The length of the content.
     * @return A new {@link RequestContent}.
     * @throws NullPointerException If {@code content} is null.
     * @throws IllegalStateException If {@code length} is less than 0.
     */
    static RequestContent fromBufferedFlux(BufferedFluxByteBuffer content, long length) {
        Objects.requireNonNull(content, "'content' cannot be null.");
        if (length < 0) {
            throw new ClientLogger(RequestContent.class).logExceptionAsError(new IllegalArgumentException(
                "'length' cannot be less than 0."));
        }

        return new FluxByteBufferContent(content, length);
    }
}
