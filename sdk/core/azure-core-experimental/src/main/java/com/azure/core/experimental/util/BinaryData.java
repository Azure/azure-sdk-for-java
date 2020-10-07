// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import static com.azure.core.util.FluxUtil.monoError;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is an abstraction over many different ways that binary data can be represented. The {@link BinaryData}
 * can be created from {@link InputStream}, {@link Flux} of {@link ByteBuffer}, {@link String}, {@link Object} and byte
 * array. The data is collected from provided sources and stored into a byte array.
 * <p>
 * It also provides a way to serialize and deserialize an {@link Object} into {@link BinaryData} given an
 * {@link ObjectSerializer}. Code samples are explained below.
 *
 * <p><strong>Create an instance from Bytes</strong></p>
 * {@codesnippet com.azure.core.experimental.util.BinaryDocument.from#bytes}
 *
 * <p><strong>Create an instance from String</strong></p>
 * {@codesnippet com.azure.core.experimental.util.BinaryDocument.from#String}
 *
 * <p><strong>Create an instance from InputStream</strong></p>
 * {@codesnippet com.azure.core.experimental.util.BinaryDocument.from#Stream}
 *
 * <p><strong>Get an Object from {@link BinaryData}</strong></p>
 * {@codesnippet com.azure.core.experimental.util.BinaryDocument.to#ObjectAsync}
 *
 * @see ObjectSerializer
 */
public final class  BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(BinaryData.class);
    private final byte[] data;

    /**
     * Create instance of {@link BinaryData} given the data.
     * @param data to represent as bytes.
     * @throws NullPointerException If {@code data} is null.
     */
    BinaryData(byte[] data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Provides {@link InputStream} for the data represented by this {@link BinaryData} object.
     *
     * <p><strong>Get InputStream from BinaryData</strong></p>
     * {@codesnippet com.azure.core.experimental.util.BinaryDocument.to#Stream}
     *
     * @return {@link InputStream} representing the binary data.
     */
    public InputStream toStream() {
        return new ByteArrayInputStream(this.data);
    }

    /**
     * Provides {@link Mono} of {@link InputStream} for the data represented by this {@link BinaryData} object.
     *
     * @return {@link InputStream} representation of the {@link BinaryData}.
     */
    public Mono<InputStream> toStreamAsync() {
        return Mono.fromCallable(() -> toStream());
    }

    /**
     * Create {@link BinaryData} instance with given {@link InputStream} as source of data. The {@link InputStream} is
     * not closed by this function.
     *
     * <p><strong>Create an instance from InputStream</strong></p>
     * {@codesnippet com.azure.core.experimental.util.BinaryDocument.from#Stream}
     *
     * @param inputStream to read bytes from.
     * @throws UncheckedIOException If any error in reading from {@link InputStream}.
     * @throws NullPointerException if {@code inputStream} is null.
     * @return {@link BinaryData} representing the binary data.
     */
    public static BinaryData fromStream(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");

        final int bufferSize = 1024;
        try {
            ByteArrayOutputStream dataOutputBuffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[bufferSize];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }

            return fromBytes(dataOutputBuffer.toByteArray());
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /**
     * Asynchronously create {@link BinaryData} instance with given {@link InputStream} as source of data. The
     * {@link InputStream} is not closed by this function.
     *
     * @param inputStream to read bytes from.
     * @throws NullPointerException if {@code inputStream} is null.
     * @return {@link Mono} of {@link BinaryData} representing the binary data.
     */
    public static Mono<BinaryData> fromStreamAsync(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");

        return Mono.fromCallable(() -> fromStream(inputStream));
    }

    /**
     * Create {@link BinaryData} instance with given {@link Flux} of {@link ByteBuffer} as source of data. It will
     * collect all the bytes from {@link ByteBuffer} into {@link BinaryData}.
     *
     * <p><strong>Create an instance from String</strong></p>
     * {@codesnippet com.azure.core.experimental.util.BinaryDocument.from#Flux}
     *
     * @param data to use.
     * @throws NullPointerException if {@code inputStream} is null.
     * @return {@link Mono} of {@link BinaryData} representing binary data.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data) {
        if (Objects.isNull(data)) {
            return monoError(LOGGER, new NullPointerException("'data' cannot be null."));
        }

        return FluxUtil.collectBytesInByteBufferStream(data)
            .flatMap(bytes -> Mono.just(fromBytes(bytes)));
    }

    /**
     * Create {@link BinaryData} instance with given data and character set.
     *
     * <p><strong>Create an instance from String</strong></p>
     * {@codesnippet com.azure.core.experimental.util.BinaryDocument.from#String}
     *
     * @param data to use.
     * @param charSet to use.
     * @throws NullPointerException if {@code inputStream} is null.
     * @return {@link BinaryData} representing the binary data.
     */
    public static BinaryData fromString(String data, Charset charSet) {
        Objects.requireNonNull(data, "'data' cannot be null.");

        return new BinaryData(data.getBytes(charSet));
    }

    /**
     * Create {@link BinaryData} instance with given data. The {@link String} is converted into bytes  using
     * {@link StandardCharsets#UTF_8} character set.
     *
     * @param data to use.
     * @throws NullPointerException if {@code inputStream} is null.
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromString(String data) {
        Objects.requireNonNull(data, "'data' cannot be null.");

        return new BinaryData(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create {@link BinaryData} instance with given byte array data.
     *
     * @param data to use.
     * @return {@link BinaryData} representing the binary data.
     */
    public static BinaryData fromBytes(byte[] data) {
        return new BinaryData(data);
    }

    /**
     * Serialize the given {@link Object} into {@link BinaryData} using the provided {@link ObjectSerializer}.
     *
     * @param data The {@link Object} which needs to be serialized into bytes.
     * @param serializer to use for serializing the object.
     * @throws NullPointerException if {@code inputStream} or {@code serializer} is null.
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, data);
        return new BinaryData(outputStream.toByteArray());
    }

    /**
     * Serialize the given {@link Object} into {@link Mono} {@link BinaryData} using the provided
     * {@link ObjectSerializer}.
     *
     * @param data The {@link Object} which needs to be serialized into bytes.
     * @param serializer to use for serializing the object.
     * @throws NullPointerException if {@code inputStream} or {@code serializer} is null.
     * @return {@link Mono} of {@link BinaryData} representing the binary data.
     */
    public static Mono<BinaryData> fromObjectAsync(Object data, ObjectSerializer serializer) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        return Mono.fromCallable(() -> fromObject(data, serializer));

    }

    /**
     * Provides byte array representation of this {@link BinaryData} object.
     *
     * @return byte array representation of the the data.
     */
    public byte[] toBytes() {
        return Arrays.copyOf(this.data, this.data.length);
    }

    /**
     * Provides {@link String} representation of this {@link BinaryData} object. The bytes are converted into
     * {@link String} using {@link StandardCharsets#UTF_8} character set.
     *
     * @return {@link String} representation of the data.
     */
    public String toString() {
        return new String(this.data, StandardCharsets.UTF_8);
    }

    /**
     * Provides {@link String} representation of this {@link BinaryData} object given a character set.
     *
     * @param charSet to use to convert bytes into {@link String}.
     * @return {@link String} representation of the the binary data.
     */
    public String toString(Charset charSet) {
        return new String(this.data, charSet);
    }

    /**
     * Deserialize the bytes into the {@link Object} of given type by applying the provided {@link ObjectSerializer} on
     * the data.
     *
     * @param clazz representing the type of the Object.
     * @param serializer to use deserialize data into type.
     * @param <T> Generic type that the data is deserialized into.
     * @return The {@link Object} of given type after deserializing the bytes.
     */
    public <T> T toObject(Class<T> clazz, ObjectSerializer serializer) {
        Objects.requireNonNull(clazz, "'clazz' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        TypeReference<T>  ref = TypeReference.createInstance(clazz);
        InputStream jsonStream = new ByteArrayInputStream(this.data);
        return serializer.deserialize(jsonStream, ref);
    }

    /**
     * Return a {@link Mono} by deserialize the bytes into the {@link Object} of given type after applying the provided
     * {@link ObjectSerializer} on the {@link BinaryData}.
     *
     * <p><strong>Gets the specified object</strong></p>
     * {@codesnippet com.azure.core.experimental.util.BinaryDocument.to#ObjectAsync}
     *
     * @param clazz representing the type of the Object.
     * @param serializer to use deserialize data into type.
     * @param <T> Generic type that the data is deserialized into.
     * @throws NullPointerException if {@code clazz} or {@code serializer} is null.
     * @return The {@link Object} of given type after deserializing the bytes.
     */
    public  <T> Mono<T> toObjectAsync(Class<T> clazz, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> toObject(clazz, serializer));
    }
}
