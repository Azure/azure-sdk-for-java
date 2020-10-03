// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
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

/**
 * This class is an abstraction over many different ways that binary data can be represented. For example
 * {@link InputStream}, {@link Flux} of {@link ByteBuffer} , {@link String} and byte array. One of the important API it
 * provides is to serialize and deserialize an {@link Object} into {@link BinaryData} given an {@link ObjectSerializer}.
 * Following are some examples.
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
 * @see ObjectSerializer
 */
public final class BinaryData {
    private final byte[] data;

    BinaryData() {
        // This exists, so no one is able to create instance, user need to use static function to create instances.
        this.data = null;
    }

    /**
     * Create instance of {@link BinaryData} given the data.
     * @param data to represent as bytes.
     */
    BinaryData(byte[] data) {
        this.data = data;
    }

    /**
     * Provides {@Link InputStream} for the data represented by this {@link BinaryData} object.
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
     * Provides {@Link Mono<InputStream>} for the data represented by this {@link BinaryData} object.
     *
     * @return {@link InputStream} representation of the data.
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
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromStream(InputStream inputStream) {
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
            ClientLogger logger = new ClientLogger(BinaryData.class);
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /**
     * Asynchronously create {@link BinaryData} instance with given {@link InputStream} as source of data. The
     * {@link InputStream} is not closed by this function.
     *
     * @param inputStream to read bytes from.
     * @return {@link Mono} of {@link BinaryData} representing binary data.
     */
    public static Mono<BinaryData> fromStreamAsync(InputStream inputStream) {
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
     * @return {@link Mono} of {@link BinaryData} representing binary data.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data) {
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
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromString(String data, Charset charSet) {
        return new BinaryData(data.getBytes(charSet));
    }

    /**
     * Create {@link BinaryData} instance with given data. The {@link String} is converted into bytes  using
     * {@link StandardCharsets#UTF_8} character set.
     *
     * @param data to use.
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromString(String data) {
        return new BinaryData(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create {@link BinaryData} instance with given byte array data.
     *
     * @param data to use.
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromBytes(byte[] data) {
        return new BinaryData(data);
    }

    /**
     * Serialize the given {@link Object} into {@link BinaryData} using the provided {@link ObjectSerializer}.
     *
     * @param data The {@link Object} which needs to be serialized into bytes.
     * @param serializer to use for serializing the object.
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, data);
        return new BinaryData(outputStream.toByteArray());
    }

    /**
     * Serialize the given {@link Object} into {@link Mono} {@link BinaryData} using the provided
     * {@link ObjectSerializer}.
     *
     * @param data The {@link Object} which needs to be serialized into bytes.
     * @param serializer to use for serializing the object.
     * @return {@link Mono} of {@link BinaryData} representing binary data.
     */
    public static Mono<BinaryData> fromObjectAsync(Object data, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> fromObject(data, serializer));

    }

    /**
     * Provides byte array representation of this {@link BinaryData} object.
     *
     * @return byte array representation of the data.
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
     * @return {@link String} representation of the data.
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
     * @return The {@link Object} of given type after deserializing the bytes.
     */
    public  <T> Mono<T> toObjectAsync(Class<T> clazz, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> toObject(clazz, serializer));
    }
}
