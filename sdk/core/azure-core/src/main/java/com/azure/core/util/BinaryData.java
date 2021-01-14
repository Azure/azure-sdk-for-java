// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.JsonSerializer;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is an abstraction over many different ways that binary data can be represented. The {@link BinaryData}
 * can be created from {@link InputStream}, {@link Flux} of {@link ByteBuffer}, {@link String}, {@link Object}, or byte
 * array.
 * <p><strong>Immutable data</strong></p>
 * {@link BinaryData} is constructed by copying the given data. Once {@link BinaryData} is instantiated, it can not be
 * changed. It provides various convenient APIs to get data out of {@link BinaryData}, they all start with the 'to'
 * prefix, for example {@link BinaryData#toBytes()}.
 * <p>
 * Code samples are presented below.
 *
 * <p><strong>Create an instance from Bytes</strong></p>
 * {@codesnippet com.azure.core.util.BinaryData.from#bytes}
 *
 * <p><strong>Create an instance from String</strong></p>
 * {@codesnippet com.azure.core.util.BinaryData.from#String}
 *
 * <p><strong>Create an instance from InputStream</strong></p>
 * {@codesnippet com.azure.core.util.BinaryData.from#Stream}
 *
 * <p><strong>Create an instance from Object</strong></p>
 * {@codesnippet com.azure.core.util.BinaryData.fromObject}
 *
 * @see ObjectSerializer
 * @see JsonSerializer
 * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
 */
public final class  BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(BinaryData.class);
    private static final BinaryData EMPTY_DATA = new BinaryData(new byte[0]);

    private static final Object LOCK = new Object();

    private final byte[] data;

    private static volatile JsonSerializer defaultJsonSerializer;

    /**
     * Create an instance of {@link BinaryData} from the given data.
     *
     * @param data to represent as bytes.
     */
    BinaryData(byte[] data) {
        this.data = data;
    }

    /**
     * Creates a {@link BinaryData} instance with given {@link InputStream} as source of data. The {@link InputStream}
     * is not closed by this function.
     *
     * <p><strong>Create an instance from InputStream</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.from#Stream}
     *
     * @param inputStream The {@link InputStream} to use as data backing the instance of {@link BinaryData}.
     * @throws UncheckedIOException If any error in reading from {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     * @return {@link BinaryData} representing the binary data.
     */
    public static BinaryData fromStream(InputStream inputStream) {
        if (Objects.isNull(inputStream)) {
            return EMPTY_DATA;
        }

        final int bufferSize = 1024;
        try {
            ByteArrayOutputStream dataOutputBuffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[bufferSize];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            dataOutputBuffer.flush();

            return new BinaryData(dataOutputBuffer.toByteArray());

        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /**
     * Asynchronously creates a {@link BinaryData} instance with the given {@link InputStream} as source of data. The
     * {@link InputStream} is not closed by this function. If the {@link InputStream} is {@code null}, an empty
     * {@link BinaryData} will be returned.
     *
     * @param inputStream The {@link InputStream} to use as data backing the instance of {@link BinaryData}.
     * @return {@link Mono} of {@link BinaryData} representing the binary data.
     */
    public static Mono<BinaryData> fromStreamAsync(InputStream inputStream) {
        return Mono.fromCallable(() -> fromStream(inputStream));
    }

    /**
     * Creates a {@link BinaryData} instance with given {@link Flux} of {@link ByteBuffer} as source of data. It will
     * collect all the bytes from {@link ByteBuffer} into {@link BinaryData}. If the {@link Flux} is {@code null}, an
     * empty {@link BinaryData} will be returned.
     *
     * <p><strong>Create an instance from String</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.from#Flux}
     *
     * @param data The byte buffer stream to use as data backing the instance of {@link BinaryData}.
     * @return {@link Mono} of {@link BinaryData} representing binary data.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data) {
        if (Objects.isNull(data)) {
            return Mono.just(EMPTY_DATA);
        }

        return FluxUtil.collectBytesInByteBufferStream(data)
            .flatMap(bytes -> Mono.just(new BinaryData(bytes)));
    }

    /**
     * Creates a {@link BinaryData} instance with given data. The {@link String} is converted into bytes using UTF_8
     * character set. If the String is {@code null}, an empty {@link BinaryData} will be returned.
     *
     * @param data The string to use as data backing the instance of {@link BinaryData}.
     * @return {@link BinaryData} representing binary data.
     */
    public static BinaryData fromString(String data) {
        if (Objects.isNull(data) || data.length() == 0) {
            return EMPTY_DATA;
        }

        return new BinaryData(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a {@link BinaryData} instance with given byte array data. If the byte array is {@code null}, an empty
     * {@link BinaryData} will be returned.
     *
     * @param data The byte array to use as data backing the instance of {@link BinaryData}.
     * @return {@link BinaryData} representing the binary data.
     */
    public static BinaryData fromBytes(byte[] data) {
        if (Objects.isNull(data) || data.length == 0) {
            return EMPTY_DATA;
        }

        return new BinaryData(Arrays.copyOf(data, data.length));
    }

    /**
     * Serialize the given {@link Object} into {@link BinaryData} using json serializer which is available on classpath.
     * The serializer on classpath must implement {@link JsonSerializer} interface. If the given Object is {@code null},
     * an empty {@link BinaryData} will be returned.
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.fromObject}

     * @param data The object to use as data backing the instance of {@link BinaryData}.
     * @throws IllegalStateException If a {@link JsonSerializer} cannot be found on the classpath.
     * @return {@link BinaryData} representing the JSON serialized object.
     *
     * @see JsonSerializer
     * @see <a href="ObjectSerializer" target="_blank">More about serialization</a>
     */
    public static BinaryData fromObject(Object data) {
        if (Objects.isNull(data)) {
            return EMPTY_DATA;
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getDefaultSerializer().serialize(outputStream, data);

        return new BinaryData(outputStream.toByteArray());
    }

    /**
     * Serialize the given {@link Object} into {@link BinaryData} using json serializer which is available on classpath.
     * The serializer on classpath must implement {@link JsonSerializer} interface. If the given Object is {@code null},
     * an empty {@link BinaryData} will be returned.
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.fromObjectAsync}

     * @param data The object to use as data backing the instance of {@link BinaryData}.
     * @throws IllegalStateException If a {@link JsonSerializer} cannot be found on the classpath.
     * @return {@link BinaryData} representing the JSON serialized object.
     *
     * @see JsonSerializer
     * @see <a href="ObjectSerializer" target="_blank">More about serialization</a>
     */
    public static Mono<BinaryData> fromObjectAsync(Object data) {
        return Mono.fromCallable(() -> fromObject(data));
    }

    /**
     * Serialize the given {@link Object} into {@link BinaryData} using the provided {@link ObjectSerializer}.
     * If the Object is {@code null}, an empty {@link BinaryData} will be returned.
     * <p>You can provide your custom implementation of {@link ObjectSerializer} interface or use one provided in Azure
     * SDK by adding them as dependency.
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">Gson serializer</a>.</li>
     * </ul>
     *
     * <p><strong>Create an instance from Object</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.fromObject#Object-ObjectSerializer}
     *
     * @param data The object to use as data backing the instance of {@link BinaryData}.
     * @param serializer to use for serializing the object.
     * @throws NullPointerException If {@code serializer} is null.
     * @return {@link BinaryData} representing binary data.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        if (Objects.isNull(data)) {
            return EMPTY_DATA;
        }

        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, data);
        return new BinaryData(outputStream.toByteArray());
    }

    /**
     * Serialize the given {@link Object} into {@link Mono} {@link BinaryData} using the provided
     * {@link ObjectSerializer}. If the Object is {@code null}, an empty {@link BinaryData} will be returned.
     *
     * <p>You can provide your custom implementation of {@link ObjectSerializer} interface or use one provided in zure
     * SDK by adding them as dependency.
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">Gson serializer</a>.</li>
     * </ul>
     *
     * @param data The object to use as data backing the instance of {@link BinaryData}.
     * @param serializer to use for serializing the object.
     * @throws NullPointerException If {@code serializer} is null.
     * @return {@link Mono} of {@link BinaryData} representing the binary data.
     * @see ObjectSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static Mono<BinaryData> fromObjectAsync(Object data, ObjectSerializer serializer) {
        if (Objects.isNull(serializer)) {
            return monoError(LOGGER, new NullPointerException("'serializer' cannot be null."));
        }
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
     * {@link String} using the UTF-8 character set.
     *
     * @return {@link String} representation of the data.
     */
    public String toString() {
        return new String(this.data, StandardCharsets.UTF_8);
    }

    /**
     * Deserialize the bytes into the {@link Object} of given type by applying the provided {@link ObjectSerializer} on
     * the data. The type, represented by {@link TypeReference}, can either be a regular class or a generic class that
     * retains the type information.
     *
     * <p>You can provide your custom implementation of {@link ObjectSerializer} interface or use one provided in zure
     * SDK by adding them as dependency.
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">Gson serializer</a>.</li>
     * </ul>
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing a regular class</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer}
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing generic types</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic}
     *
     * @param typeReference representing the {@link TypeReference type} of the Object.
     * @param serializer to use deserialize data into type.
     * @param <T> Generic type that the data is deserialized into.
     * @throws NullPointerException If {@code serializer} or {@code typeReference} is null.
     * @return The {@link Object} of given type after deserializing the bytes.
     */
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        Objects.requireNonNull(typeReference, "'typeReference' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        InputStream jsonStream = new ByteArrayInputStream(this.data);
        return serializer.deserialize(jsonStream, typeReference);
    }

    /**
     * Return a {@link Mono} by deserializing the bytes into the {@link Object} of given type after applying the
     * provided {@link ObjectSerializer} on the {@link BinaryData}. The type, represented by {@link TypeReference},
     * can either be a regular class or a generic class that retains the type information.
     *
     * <p>You can provide your custom implementation of {@link ObjectSerializer} interface or use one provided in zure
     * SDK by adding them as dependency.
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">Gson serializer</a>.</li>
     * </ul>
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing a regular class</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer}
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing generic types</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer-generic}
     *
     * @param typeReference representing the {@link TypeReference type} of the Object.
     * @param serializer to use deserialize data into type.
     * @param <T> Generic type that the data is deserialized into.
     * @throws NullPointerException If {@code typeReference} or {@code serializer} is null.
     * @return The {@link Object} of given type after deserializing the bytes.
     */
    public <T> Mono<T> toObjectAsync(TypeReference<T> typeReference, ObjectSerializer serializer) {

        if (Objects.isNull(typeReference)) {
            return monoError(LOGGER, new NullPointerException("'typeReference' cannot be null."));
        } else if (Objects.isNull(serializer)) {
            return monoError(LOGGER, new NullPointerException("'serializer' cannot be null."));
        }
        return Mono.fromCallable(() -> toObject(typeReference, serializer));
    }

    /**
     * Deserialize the bytes into the {@link Object} of given type by using json serializer which is available in
     * classpath. The type, represented by {@link TypeReference}, can either be a regular class or a generic class that
     * retains the type information. This method assumes the data to be in JSON format and will use a default
     * implementation of {@link JsonSerializer}.
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing a regular class</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference}
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing generic types</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference-generic}
     *
     * @param typeReference representing the {@link TypeReference type} of the Object.
     * @param <T> Generic type that the data is deserialized into.
     * @throws NullPointerException If {@code typeReference} is null.
     * @return The {@link Object} of given type after deserializing the bytes.
     */
    public <T> T toObject(TypeReference<T> typeReference) {
        Objects.requireNonNull(typeReference, "'typeReference' cannot be null.");

        InputStream jsonStream = new ByteArrayInputStream(this.data);
        return getDefaultSerializer().deserialize(jsonStream, typeReference);
    }

    /**
     * Return a {@link Mono} by deserializing the bytes into the {@link Object} of given type after applying the Json
     * serializer found on classpath. The type, represented by {@link TypeReference}, can either be a regular class
     * or a generic class that retains the type information. This method assumes the data to be in JSON format and will
     * use a default implementation of {@link JsonSerializer}.
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing a regular class</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference}
     *
     * <p><strong>Code sample to demonstrate serializing and deserializing generic types</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference-generic}
     *
     * @param typeReference representing the {@link TypeReference type} of the Object.
     * @param <T> Generic type that the data is deserialized into.
     * @throws NullPointerException If {@code typeReference} is null.
     * @return The {@link Object} of given type after deserializing the bytes.
     */
    public <T> Mono<T> toObjectAsync(TypeReference<T> typeReference) {
        if (Objects.isNull(typeReference)) {
            return monoError(LOGGER, new NullPointerException("'typeReference' cannot be null."));
        }
        return Mono.fromCallable(() -> toObject(typeReference));
    }

    /**
     * Provides {@link InputStream} for the data represented by this {@link BinaryData} object.
     *
     * <p><strong>Get InputStream from BinaryData</strong></p>
     * {@codesnippet com.azure.core.util.BinaryData.to#Stream}
     *
     * @return {@link InputStream} representing the binary data.
     */
    public InputStream toStream() {
        return new ByteArrayInputStream(this.data);
    }

    /* This will ensure lazy instantiation to avoid hard dependency on Json Serializer. */
    private static JsonSerializer getDefaultSerializer() {
        if (defaultJsonSerializer ==  null) {
            synchronized (LOCK) {
                if (defaultJsonSerializer == null) {
                    defaultJsonSerializer = JsonSerializerProviders.createInstance();
                }
            }
        }
        return defaultJsonSerializer;
    }
}
