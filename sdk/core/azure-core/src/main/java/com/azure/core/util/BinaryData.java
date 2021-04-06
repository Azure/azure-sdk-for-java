// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProvider;
import com.azure.core.util.serializer.JsonSerializerProviders;
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
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is an abstraction for the many ways binary data can be represented.
 * <p>
 * {@link BinaryData} can be created from an {@link InputStream}, a {@link Flux} of {@link ByteBuffer}, a {@link
 * String}, an {@link Object}, or a byte array.
 *
 * <p><strong>Immutable data</strong></p>
 *
 * {@link BinaryData} copies data on construction making it immutable. Various APIs are provided to get data out of
 * {@link BinaryData}, they all start with the {@code 'to'} prefix, for example {@link BinaryData#toBytes()}.
 *
 * <p><strong>Create an instance from a byte array</strong></p>
 *
 * {@codesnippet com.azure.core.util.BinaryData.fromBytes#byte}
 *
 * <p><strong>Create an instance from a String</strong></p>
 *
 * {@codesnippet com.azure.core.util.BinaryData.fromString#String}
 *
 * <p><strong>Create an instance from an InputStream</strong></p>
 *
 * {@codesnippet com.azure.core.util.BinaryData.fromStream#InputStream}
 *
 * <p><strong>Create an instance from an Object</strong></p>
 *
 * {@codesnippet com.azure.core.util.BinaryData.fromObject#Object}
 *
 * @see ObjectSerializer
 * @see JsonSerializer
 * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
 */
public final class BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(BinaryData.class);
    private static final BinaryData EMPTY_DATA = new BinaryData(new byte[0]);
    private static final int STREAM_READ_SIZE = 1024;

    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);

    private final byte[] data;

    private String dataAsStringCache;


    /**
     * Create an instance of {@link BinaryData} from the given byte array.
     *
     * @param data The byte array that {@link BinaryData} will represent.
     */
    BinaryData(byte[] data) {
        this.data = data;
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link InputStream}.
     * <p>
     * If {@code inputStream} is null or empty an empty {@link BinaryData} is returned.
     * <p>
     * <b>NOTE:</b> The {@link InputStream} is not closed by this function.
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromStream#InputStream}
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link InputStream}.
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     */
    public static BinaryData fromStream(InputStream inputStream) {
        if (Objects.isNull(inputStream)) {
            return EMPTY_DATA;
        }

        try {
            ByteArrayOutputStream dataOutputBuffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }

            return new BinaryData(dataOutputBuffer.toByteArray());

        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link InputStream}.
     * <p>
     * If {@code inputStream} is null or empty an empty {@link BinaryData} is returned.
     * <p>
     * <b>NOTE:</b> The {@link InputStream} is not closed by this function.
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromStreamAsync#InputStream}
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link InputStream}.
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     */
    public static Mono<BinaryData> fromStreamAsync(InputStream inputStream) {
        return Mono.fromCallable(() -> fromStream(inputStream));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link Flux} of {@link ByteBuffer}.
     * <p>
     * If the {@code data} is null an empty {@link BinaryData} will be returned.
     * <p>
     * <b>Note:</b> This will collect all bytes from the {@link ByteBuffer ByteBuffers} resulting in {@link
     * ByteBuffer#hasRemaining() hasRemaining} to return false.
     *
     * <p><strong>Create an instance from a Flux of ByteBuffer</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromFlux#Flux}
     *
     * @param data The {@link Flux} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link Flux} of {@link ByteBuffer}.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data) {
        if (Objects.isNull(data)) {
            return Mono.just(EMPTY_DATA);
        }

        return FluxUtil.collectBytesInByteBufferStream(data)
            .flatMap(bytes -> Mono.just(new BinaryData(bytes)));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link String}.
     * <p>
     * The {@link String} is converted into bytes using {@link String#getBytes(Charset)} passing {@link
     * StandardCharsets#UTF_8}.
     * <p>
     * If the {@code data} is null or a zero length string an empty {@link BinaryData} will be returned.
     *
     * <p><strong>Create an instance from a String</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromString#String}
     *
     * @param data The {@link String} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link String}.
     */
    public static BinaryData fromString(String data) {
        if (CoreUtils.isNullOrEmpty(data)) {
            return EMPTY_DATA;
        }

        return new BinaryData(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given byte array.
     * <p>
     * If the byte array is null or zero length an empty {@link BinaryData} will be returned.
     *
     * <p><strong>Create an instance from a byte array</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromBytes#byte}
     *
     * @param data The byte array that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the byte array.
     */
    public static BinaryData fromBytes(byte[] data) {
        if (Objects.isNull(data) || data.length == 0) {
            return EMPTY_DATA;
        }

        return new BinaryData(Arrays.copyOf(data, data.length));
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the default {@link
     * JsonSerializer}.
     * <p>
     * If {@code data} is null an empty {@link BinaryData} will be returned.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to serialize the object.
     *
     * <p><strong>Creating an instance from an Object</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromObject#Object}
     *
     * @param data The object that will be JSON serialized that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the JSON serialized object.
     * @see JsonSerializer
     */
    public static BinaryData fromObject(Object data) {
        return fromObject(data, SERIALIZER);
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the default {@link
     * JsonSerializer}.
     * <p>
     * If {@code data} is null an empty {@link BinaryData} will be returned.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to serialize the object.
     *
     * <p><strong>Creating an instance from an Object</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromObjectAsync#Object}
     *
     * @param data The object that will be JSON serialized that {@link BinaryData} will represent.
     * @return A {@link Mono} of {@link BinaryData} representing the JSON serialized object.
     * @see JsonSerializer
     */
    public static Mono<BinaryData> fromObjectAsync(Object data) {
        return fromObjectAsync(data, SERIALIZER);
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the passed {@link
     * ObjectSerializer}.
     * <p>
     * If {@code data} is null an empty {@link BinaryData} will be returned.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Create an instance from an Object</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromObject#Object-ObjectSerializer}
     *
     * @param data The object that will be serialized that {@link BinaryData} will represent.
     * @param serializer The {@link ObjectSerializer} used to serialize object.
     * @return A {@link BinaryData} representing the serialized object.
     * @throws NullPointerException If {@code serializer} is null and {@code data} is not null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        if (Objects.isNull(data)) {
            return EMPTY_DATA;
        }

        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        return new BinaryData(serializer.serializeToBytes(data));
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the passed {@link
     * ObjectSerializer}.
     * <p>
     * If {@code data} is null an empty {@link BinaryData} will be returned.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Create an instance from an Object</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromObjectAsync#Object-ObjectSerializer}
     *
     * @param data The object that will be serialized that {@link BinaryData} will represent.
     * @param serializer The {@link ObjectSerializer} used to serialize object.
     * @return A {@link Mono} of {@link BinaryData} representing the serialized object.
     * @throws NullPointerException If {@code serializer} is null and {@code data} is not null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static Mono<BinaryData> fromObjectAsync(Object data, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> fromObject(data, serializer));
    }

    /**
     * Returns a byte array representation of this {@link BinaryData}.
     *
     * @return A byte array representing this {@link BinaryData}.
     */
    public byte[] toBytes() {
        return Arrays.copyOf(this.data, this.data.length);
    }

    /**
     * Returns a {@link String} representation of this {@link BinaryData} by converting its data using the UTF-8
     * character set.
     *
     * @return A {@link String} representing this {@link BinaryData}.
     */
    public String toString() {
        if (this.dataAsStringCache == null) {
            this.dataAsStringCache = new String(this.data, StandardCharsets.UTF_8);
        }

        return this.dataAsStringCache;
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link JsonSerializer}.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use {@link
     * #toObject(TypeReference)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObject#Class}
     *
     * @param clazz The {@link Class} representing the Object's type.
     * @param <T> Type of the deserialized Object.
     * @return An {@link Object} representing the JSON deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code clazz} is null.
     * @see JsonSerializer
     */
    public <T> T toObject(Class<T> clazz) {
        return toObject(TypeReference.createInstance(clazz), SERIALIZER);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link JsonSerializer}.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use {@link
     * TypeReference#createInstance(Class)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference}
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference-generic}
     *
     * @param typeReference The {@link TypeReference} representing the Object's type.
     * @param <T> Type of the deserialized Object.
     * @return An {@link Object} representing the JSON deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code typeReference} is null.
     * @see JsonSerializer
     */
    public <T> T toObject(TypeReference<T> typeReference) {
        return toObject(typeReference, SERIALIZER);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the passed
     * {@link ObjectSerializer}.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use {@link
     * #toObject(TypeReference, ObjectSerializer)}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObject#Class-ObjectSerializer}
     *
     * @param clazz The {@link Class} representing the Object's type.
     * @param serializer The {@link ObjectSerializer} used to deserialize object.
     * @param <T> Type of the deserialized Object.
     * @return An {@link Object} representing the deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code clazz} or {@code serializer} is null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public <T> T toObject(Class<T> clazz, ObjectSerializer serializer) {
        return toObject(TypeReference.createInstance(clazz), serializer);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the passed
     * {@link ObjectSerializer}.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use {@link
     * TypeReference#createInstance(Class)}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer}
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic}
     *
     * @param typeReference The {@link TypeReference} representing the Object's type.
     * @param serializer The {@link ObjectSerializer} used to deserialize object.
     * @param <T> Type of the deserialized Object.
     * @return An {@link Object} representing the deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code typeReference} or {@code serializer} is null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        Objects.requireNonNull(typeReference, "'typeReference' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        return serializer.deserializeFromBytes(this.data, typeReference);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link JsonSerializer}.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use {@link
     * #toObject(TypeReference)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#Class}
     *
     * @param clazz The {@link Class} representing the Object's type.
     * @param <T> Type of the deserialized Object.
     * @return A {@link Mono} of {@link Object} representing the JSON deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code clazz} is null.
     * @see JsonSerializer
     */
    public <T> Mono<T> toObjectAsync(Class<T> clazz) {
        return toObjectAsync(TypeReference.createInstance(clazz), SERIALIZER);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link JsonSerializer}.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use {@link
     * TypeReference#createInstance(Class)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference}
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference-generic}
     *
     * @param typeReference The {@link TypeReference} representing the Object's type.
     * @param <T> Type of the deserialized Object.
     * @return A {@link Mono} of {@link Object} representing the JSON deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code typeReference} is null.
     * @see JsonSerializer
     */
    public <T> Mono<T> toObjectAsync(TypeReference<T> typeReference) {
        return toObjectAsync(typeReference, SERIALIZER);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the passed
     * {@link ObjectSerializer}.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use {@link
     * #toObject(TypeReference, ObjectSerializer)}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#Class-ObjectSerializer}
     *
     * @param clazz The {@link Class} representing the Object's type.
     * @param serializer The {@link ObjectSerializer} used to deserialize object.
     * @param <T> Type of the deserialized Object.
     * @return A {@link Mono} of {@link Object} representing the deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code clazz} or {@code serializer} is null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public <T> Mono<T> toObjectAsync(Class<T> clazz, ObjectSerializer serializer) {
        return toObjectAsync(TypeReference.createInstance(clazz), serializer);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the passed
     * {@link ObjectSerializer}.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use {@link
     * TypeReference#createInstance(Class)}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer}
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer-generic}
     *
     * @param typeReference The {@link TypeReference} representing the Object's type.
     * @param serializer The {@link ObjectSerializer} used to deserialize object.
     * @param <T> Type of the deserialized Object.
     * @return A {@link Mono} of {@link Object} representing the deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code typeReference} or {@code serializer} is null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public <T> Mono<T> toObjectAsync(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> toObject(typeReference, serializer));
    }

    /**
     * Returns an {@link InputStream} representation of this {@link BinaryData}.
     *
     * <p><strong>Get an InputStream from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.toStream}
     *
     * @return An {@link InputStream} representing the {@link BinaryData}.
     */
    public InputStream toStream() {
        return new ByteArrayInputStream(this.data);
    }

    /**
     * Returns a read-only {@link ByteBuffer} representation of this {@link BinaryData}.
     * <p>
     * Attempting to mutate the returned {@link ByteBuffer} will throw a {@link ReadOnlyBufferException}.
     *
     * <p><strong>Get a read-only ByteBuffer from the BinaryData</strong></p>
     *
     * {@codesnippet com.azure.util.BinaryData.toByteBuffer}
     *
     * @return A read-only {@link ByteBuffer} representing the {@link BinaryData}.
     */
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(this.data).asReadOnlyBuffer();
    }
}
