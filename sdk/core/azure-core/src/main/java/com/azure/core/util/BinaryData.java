// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.implementation.BinaryDataContent;
import com.azure.core.util.implementation.ByteArrayContent;
import com.azure.core.util.implementation.FileContent;
import com.azure.core.util.implementation.FluxByteBufferContent;
import com.azure.core.util.implementation.InputStreamContent;
import com.azure.core.util.implementation.SerializableContent;
import com.azure.core.util.implementation.StringContent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProvider;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * BinaryData is a convenient data interchange class for use throughout the Azure SDK for Java. Put simply, BinaryData
 * enables developers to bring data in from external sources, and read it back from Azure services, in formats that
 * appeal to them. This leaves BinaryData, and the Azure SDK for Java, the task of converting this data into appropriate
 * formats to be transferred to and from these external services. This enables developers to focus on their business
 * logic, and enables the Azure SDK for Java to optimize operations for best performance.
 * <p>
 * BinaryData in its simplest form can be thought of as a container for content. Often this content is already in-memory
 * as a String, byte array, or an Object that can be serialized into a String or byte[] (for example, if the Object has
 * appropriate annotations for a library such as Jackson). When the BinaryData is about to be sent to an Azure Service,
 * this in-memory content is copied into the network request and sent to the service.
 * </p>
 * <p>
 * In more performance critical scenarios, where copying data into memory results in increased memory pressure, it is
 * possible to create a BinaryData instance from a stream of data. From this, BinaryData can be connected directly to
 * the outgoing network connection so that the stream is read directly to the network, without needing to first be read
 * into memory on the system. Similarly, it is possible to read a stream of data from a BinaryData returned from an
 * Azure Service without it first being read into memory. In many situations, these streaming operations can drastically
 * reduce the memory pressure in applications, and so it is encouraged that all developers very carefully consider their
 * ability to use the most appropriate API in BinaryData whenever they encounter an client library that makes use of
 * BinaryData.
 * </p>
 * <p>
 * Refer to the documentation of each method in the BinaryData class to better understand its performance
 * characteristics, and refer to the samples below to understand the common usage scenarios of this class.
 * </p>
 *
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
    private static final int CHUNK_SIZE = 8092;
    static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);
    private final BinaryDataContent content;

    BinaryData(BinaryDataContent content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
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
        return new BinaryData(new InputStreamContent(inputStream));
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
     *
     * <p><strong>Create an instance from a Flux of ByteBuffer</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromFlux#Flux}
     *
     * @param data The {@link Flux} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link Flux} of {@link ByteBuffer}.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data) {
        Objects.requireNonNull(data, "'content' cannot be null.");
        return Mono.just(new BinaryData(new FluxByteBufferContent(data)));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link Flux} of {@link ByteBuffer}.
     *
     * <p><strong>Create an instance from a Flux of ByteBuffer</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromFlux#Flux}
     *
     * @param data The {@link Flux} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @param length The length of {@code data} in bytes.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link Flux} of {@link ByteBuffer}.
     * @throws IllegalArgumentException if the length is less than zero.
     * @throws NullPointerException if {@code data} is null.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data, Long length) {
        Objects.requireNonNull(data, "'content' cannot be null.");
        if (length < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'length' cannot be less than 0."));
        }
        return Mono.just(new BinaryData(new FluxByteBufferContent(data, length)));
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
        return new BinaryData(new StringContent(data));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given byte array.
     * <p>
     * If the byte array is null or zero length an empty {@link BinaryData} will be returned. Note that the input
     * byte array is used as a reference by this instance of {@link BinaryData} and any changes to the byte array
     * outside of this instance will result in the contents of this BinaryData instance to be updated as well. To
     * safely update the byte array, it is recommended to make a copy of the contents first.
     *
     * <p><strong>Create an instance from a byte array</strong></p>
     *
     * {@codesnippet com.azure.core.util.BinaryData.fromBytes#byte}
     *
     * @param data The byte array that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the byte array.
     */
    public static BinaryData fromBytes(byte[] data) {
        return new BinaryData(new ByteArrayContent(data));
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
        return new BinaryData(new SerializableContent(data, serializer));
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
     * Creates a {@link BinaryData} that uses {@link Path} as its data.
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @return A new {@link BinaryData}.
     * @throws NullPointerException If {@code file} is null.
     */
    public static BinaryData fromFile(Path file) {
        return fromFile(file, CHUNK_SIZE);
    }

    /**
     * Creates a {@link BinaryData} that uses {@link Path} as its data.
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @param chunkSize The requested size for each read of the path.
     * @return A new {@link BinaryData}.
     * @throws NullPointerException If {@code file} is null.
     * @throws IllegalArgumentException If {@code offset} or {@code length} are negative or {@code offset} plus {@code
     * length} is greater than the file size or {@code chunkSize} is less than or equal to 0.
     */
    public static BinaryData fromFile(Path file, int chunkSize) {
        Objects.requireNonNull(file, "'file' cannot be null.");
        if (chunkSize <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "'chunkSize' cannot be less than or equal to 0."));
        }
        return new BinaryData(new FileContent(file, chunkSize));
    }

    /**
     * Returns a byte array representation of this {@link BinaryData}. This method returns a reference to the
     * underlying byte array. Modifying the contents of the returned byte array will also change the content of this
     * BinaryData instance. To safely update the byte array, it is recommended to make a copy of the contents first.
     *
     * @return A byte array representing this {@link BinaryData}.
     */
    public byte[] toBytes() {
        return content.toBytes();
    }

    /**
     * Returns a {@link String} representation of this {@link BinaryData} by converting its data using the UTF-8
     * character set.
     *
     * @return A {@link String} representing this {@link BinaryData}.
     */
    public String toString() {
        return content.toString();
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
     * @param <T> Type of the deserialized Object.
     * @param clazz The {@link Class} representing the Object's type.
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

        return content.toObject(typeReference, serializer);
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
        return content.toStream();
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
        return content.toByteBuffer();
    }

    /**
     * Returns the content of this {@link BinaryData} instance as a flux of {@link ByteBuffer ByteBuffers}.
     *
     * @return the content of this {@link BinaryData} instance as a flux of {@link ByteBuffer ByteBuffers}.
     */
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return content.toFluxByteBuffer();
    }

    /**
     * Returns the length of the content, if it is known. The length can be {@code null} if the source did not
     * specify the length or the length cannot be determined without reading the whole content.
     * @return the length of the content, if it is known.
     */
    public Long getLength() {
        return content.getLength();
    }
}
