// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.implementation.util.BinaryDataContent;
import com.generic.core.implementation.util.BinaryDataHelper;
import com.generic.core.implementation.util.ByteArrayContent;
import com.generic.core.implementation.util.ByteBufferContent;
import com.generic.core.implementation.util.FileContent;
import com.generic.core.implementation.util.InputStreamContent;
import com.generic.core.implementation.util.ListByteBufferContent;
import com.generic.core.implementation.util.SerializableContent;
import com.generic.core.implementation.util.StringContent;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.generic.core.implementation.util.BinaryDataContent.STREAM_READ_SIZE;

/**
 * BinaryData is a convenient data interchange class for use throughout the SDK for Java. Put simply, BinaryData enables
 * developers to bring data in from external sources, and read it back from services, in formats that appeal to them.
 * This leaves BinaryData, and the SDK for Java, the task of converting this data into appropriate formats to be
 * transferred to and from these external services. This enables developers to focus on their business logic, and
 * enables the SDK for Java to optimize operations for best performance.
 * <p>
 * BinaryData in its simplest form can be thought of as a container for content. Often this content is already in-memory
 * as a String, byte array, or an Object that can be serialized into a String or byte[]. When the BinaryData is about to
 * be sent to a service, this in-memory content is copied into the network request and sent to the service.
 * </p>
 * <p>
 * In more performance critical scenarios, where copying data into memory results in increased memory pressure, it is
 * possible to create a BinaryData instance from a stream of data. From this, BinaryData can be connected directly to
 * the outgoing network connection so that the stream is read directly to the network, without needing to first be read
 * into memory on the system. Similarly, it is possible to read a stream of data from a BinaryData returned from an
 * service without it first being read into memory. In many situations, these streaming operations can drastically
 * reduce the memory pressure in applications, and so it is encouraged that all developers very carefully consider their
 * ability to use the most appropriate API in BinaryData whenever they encounter an client library that makes use of
 * BinaryData.
 * </p>
 * <p>
 * Refer to the documentation of each method in the BinaryData class to better understand its performance
 * characteristics, and refer to the samples below to understand the common usage scenarios of this class.
 * </p>
 *
 * {@link BinaryData} can be created from an {@link InputStream}, of {@link ByteBuffer}, a
 * {@link String}, an {@link Object}, a {@link Path file}, or a byte array.
 *
 * <p><strong>A note on data mutability</strong></p>
 *
 * {@link BinaryData} does not copy data on construction. BinaryData keeps a reference to the source content and is
 * accessed when a read request is made. So, any modifications to the underlying source before the content is read can
 * result in undefined behavior.
 * <p>
 * To create an instance of  {@link BinaryData}, use the various static factory methods available. They all start with
 * {@code 'from'} prefix, for example {@link BinaryData#fromBytes(byte[])}.
 * </p>
 *
 * <p><strong>Create an instance from a byte array</strong></p>
 *
 * <!-- src_embed com.generic.core.util.BinaryData.fromBytes#byte -->
 * <!-- end com.generic.core.util.BinaryData.fromBytes#byte -->
 *
 * <p><strong>Create an instance from a String</strong></p>
 *
 * <!-- src_embed com.generic.core.util.BinaryData.fromString#String -->
 * <!-- end com.generic.core.util.BinaryData.fromString#String -->
 *
 * <p><strong>Create an instance from an InputStream</strong></p>
 *
 * <!-- src_embed com.generic.core.util.BinaryData.fromStream#InputStream -->
 * <!-- end com.generic.core.util.BinaryData.fromStream#InputStream -->
 *
 * <p><strong>Create an instance from an Object</strong></p>
 *
 * <!-- src_embed com.generic.core.util.BinaryData.fromObject#Object -->
 * <!-- end com.generic.core.util.BinaryData.fromObject#Object -->
 *
 * <p><strong>Create an instance from a file</strong></p>
 *
 * <!-- src_embed com.generic.core.util.BinaryData.fromFile -->
 * <!-- end com.generic.core.util.BinaryData.fromFile -->
 *
 * @see ObjectSerializer
 * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
 */
public final class BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(BinaryData.class);
    static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
    private final BinaryDataContent content;

    BinaryData(BinaryDataContent content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
    }

    static {
        BinaryDataHelper.setAccessor(new BinaryDataHelper.BinaryDataAccessor() {
            @Override
            public BinaryData createBinaryData(BinaryDataContent content) {
                return new BinaryData(content);
            }

            @Override
            public BinaryDataContent getContent(BinaryData binaryData) {
                return binaryData.content;
            }
        });
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link InputStream}. Depending on the type of
     * inputStream, the BinaryData instance created may or may not allow reading the content more than once. The stream
     * content is not cached if the stream is not read into a format that requires the content to be fully read into
     * memory.
     * <p>
     * <b>NOTE:</b> The {@link InputStream} is not closed by this function.
     * </p>
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromStream#InputStream -->
     * <!-- end com.generic.core.util.BinaryData.fromStream#InputStream -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     *
     * @return A {@link BinaryData} representing the {@link InputStream}.
     *
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static BinaryData fromStream(InputStream inputStream) {
        return fromStream(inputStream, null);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link InputStream}. Depending on the type of
     * inputStream, the BinaryData instance created may or may not allow reading the content more than once. The stream
     * content is not cached if the stream is not read into a format that requires the content to be fully read into
     * memory.
     * <p>
     * <b>NOTE:</b> The {@link InputStream} is not closed by this function.
     * </p>
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromStream#InputStream-Long -->
     * <!-- end com.generic.core.util.BinaryData.fromStream#InputStream-Long -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @param length The length of {@code data} in bytes.
     *
     * @return A {@link BinaryData} representing the {@link InputStream}.
     *
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static BinaryData fromStream(InputStream inputStream, Long length) {
        return new BinaryData(new InputStreamContent(inputStream, length));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link String}.
     * <p>
     * The {@link String} is converted into bytes using {@link String#getBytes(Charset)} passing
     * {@link StandardCharsets#UTF_8}.
     * </p>
     * <p><strong>Create an instance from a String</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromString#String -->
     * <!-- end com.generic.core.util.BinaryData.fromString#String -->
     *
     * @param data The {@link String} that {@link BinaryData} will represent.
     *
     * @return A {@link BinaryData} representing the {@link String}.
     *
     * @throws NullPointerException If {@code data} is null.
     */
    public static BinaryData fromString(String data) {
        return new BinaryData(new StringContent(data));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given byte array.
     * <p>
     * If the byte array is zero length an empty {@link BinaryData} will be returned. Note that the input byte array is
     * used as a reference by this instance of {@link BinaryData} and any changes to the byte array outside of this
     * instance will result in the contents of this BinaryData instance being updated as well. To safely update the byte
     * array without impacting the BinaryData instance, perform an array copy first.
     * </p>
     *
     * <p><strong>Create an instance from a byte array</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromBytes#byte -->
     * <!-- end com.generic.core.util.BinaryData.fromBytes#byte -->
     *
     * @param data The byte array that {@link BinaryData} will represent.
     *
     * @return A {@link BinaryData} representing the byte array.
     *
     * @throws NullPointerException If {@code data} is null.
     */
    public static BinaryData fromBytes(byte[] data) {
        return new BinaryData(new ByteArrayContent(data));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link ByteBuffer}.
     * <p>
     * If the {@link ByteBuffer} is zero length an empty {@link BinaryData} will be returned. Note that the input
     * {@link ByteBuffer} is used as a reference by this instance of {@link BinaryData} and any changes to the
     * {@link ByteBuffer} outside of this instance will result in the contents of this BinaryData instance being updated
     * as well. To safely update the {@link ByteBuffer} without impacting the BinaryData instance, perform an array copy
     * first.
     * </p>
     *
     * <p><strong>Create an instance from a ByteBuffer</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromByteBuffer#ByteBuffer -->
     * <!-- end com.generic.core.util.BinaryData.fromByteBuffer#ByteBuffer -->
     *
     * @param data The {@link ByteBuffer} that {@link BinaryData} will represent.
     *
     * @return A {@link BinaryData} representing the {@link ByteBuffer}.
     *
     * @throws NullPointerException If {@code data} is null.
     */
    public static BinaryData fromByteBuffer(ByteBuffer data) {
        return new BinaryData(new ByteBufferContent(data));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link List} of {@link ByteBuffer}.
     *
     * <p>
     * The input {@link ByteBuffer} instances are used as a reference by this instance of {@link BinaryData} and any
     * changes to a {@link ByteBuffer} outside of this instance will result in the contents of this BinaryData instance
     * being updated as well. To safely update the byte array without impacting the BinaryData instance, perform an
     * array copy first.
     * </p>
     *
     * <p><strong>Create an instance from a List&lt;ByteBuffer&gt;</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromListByteBuffer#List -->
     * <!-- end com.generic.core.util.BinaryData.fromListByteBuffer#List -->
     *
     * @param data The {@link List} of {@link ByteBuffer} that {@link BinaryData} will represent.
     *
     * @return A {@link BinaryData} representing the {@link List} of {@link ByteBuffer}.
     */
    public static BinaryData fromListByteBuffer(List<ByteBuffer> data) {
        return new BinaryData(new ListByteBufferContent(data));
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the default
     * {@link ObjectSerializer}.
     *
     * <p><strong>Creating an instance from an Object</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromObject#Object -->
     * <!-- end com.generic.core.util.BinaryData.fromObject#Object -->
     *
     * @param data The object that will be JSON serialized that {@link BinaryData} will represent.
     *
     * @return A {@link BinaryData} representing the JSON serialized object.
     *
     * @throws NullPointerException If {@code data} is null.
     *
     * @see ObjectSerializer
     */
    public static BinaryData fromObject(Object data) {
        return fromObject(data, SERIALIZER);
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the passed
     * {@link ObjectSerializer}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the SDK or your own
     * implementation.
     * </p>
     *
     * <p><strong>Create an instance from an Object</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromObject#Object-ObjectSerializer -->
     * <!-- end com.generic.core.util.BinaryData.fromObject#Object-ObjectSerializer -->
     *
     * @param data The object that will be serialized that {@link BinaryData} will represent. The {@code serializer}
     * determines how {@code null} data is serialized.
     * @param serializer The {@link ObjectSerializer} used to serialize object.
     *
     * @return A {@link BinaryData} representing the serialized object.
     *
     * @throws NullPointerException If {@code serializer} is null.
     *
     * @see ObjectSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        return new BinaryData(new SerializableContent(data, serializer));
    }

    /**
     * Creates a {@link BinaryData} that uses the content of the file at {@link Path} as its data. This method checks
     * for the existence of the file at the time of creating an instance of {@link BinaryData}. The file, however, is
     * not read until there is an attempt to read the contents of the returned BinaryData instance.
     *
     * <p><strong>Create an instance from a file</strong></p>
     *
     * <p>The {@link BinaryData} returned from this method uses 8KB chunk size when reading file content.</p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromFile -->
     * <!-- end com.generic.core.util.BinaryData.fromFile -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     *
     * @return A new {@link BinaryData}.
     *
     * @throws NullPointerException If {@code file} is null.
     */
    public static BinaryData fromFile(Path file) {
        return fromFile(file, STREAM_READ_SIZE);
    }

    /**
     * Creates a {@link BinaryData} that uses the content of the file at {@link Path file} as its data. This method
     * checks for the existence of the file at the time of creating an instance of {@link BinaryData}. The file,
     * however, is not read until there is an attempt to read the contents of the returned BinaryData instance.
     *
     * <p><strong>Create an instance from a file</strong></p>
     *
     * <!-- src_embed BinaryData.fromFile#Path-int -->
     * <!-- end BinaryData.fromFile#Path-int -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @param chunkSize The requested size for each read of the path.
     *
     * @return A new {@link BinaryData}.
     *
     * @throws NullPointerException If {@code file} is null.
     * @throws IllegalArgumentException If {@code offset} or {@code length} are negative or {@code offset} plus
     * {@code length} is greater than the file size or {@code chunkSize} is less than or equal to 0.
     * @throws UncheckedIOException if the file does not exist.
     */
    public static BinaryData fromFile(Path file, int chunkSize) {
        return new BinaryData(new FileContent(file, chunkSize, null, null));
    }

    /**
     * Creates a {@link BinaryData} that uses the content of the file at {@link Path file} as its data. This method
     * checks for the existence of the file at the time of creating an instance of {@link BinaryData}. The file,
     * however, is not read until there is an attempt to read the contents of the returned BinaryData instance.
     *
     * <p><strong>Create an instance from a file</strong></p>
     *
     * <p>The {@link BinaryData} returned from this method uses 8KB chunk size when reading file content.</p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromFile#Path-Long-Long -->
     * <!-- end com.generic.core.util.BinaryData.fromFile#Path-Long-Long -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @param position Position, or offset, within the path where reading begins.
     * @param length Maximum number of bytes to be read from the path.
     *
     * @return A new {@link BinaryData}.
     *
     * @throws NullPointerException If {@code file} is null.
     * @throws IllegalArgumentException If {@code offset} or {@code length} are negative or {@code offset} plus
     * {@code length} is greater than the file size or {@code chunkSize} is less than or equal to 0.
     * @throws UncheckedIOException if the file does not exist.
     */
    public static BinaryData fromFile(Path file, Long position, Long length) {
        return new BinaryData(new FileContent(file, STREAM_READ_SIZE, position, length));
    }

    /**
     * Creates a {@link BinaryData} that uses the content of the file at {@link Path file} as its data. This method
     * checks for the existence of the file at the time of creating an instance of {@link BinaryData}. The file,
     * however, is not read until there is an attempt to read the contents of the returned BinaryData instance.
     *
     * <p><strong>Create an instance from a file</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.fromFile#Path-Long-Long-int -->
     * <!-- end com.generic.core.util.BinaryData.fromFile#Path-Long-Long-int -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @param position Position, or offset, within the path where reading begins.
     * @param length Maximum number of bytes to be read from the path.
     * @param chunkSize The requested size for each read of the path.
     *
     * @return A new {@link BinaryData}.
     *
     * @throws NullPointerException If {@code file} is null.
     * @throws IllegalArgumentException If {@code offset} or {@code length} are negative or {@code offset} plus
     * {@code length} is greater than the file size or {@code chunkSize} is less than or equal to 0.
     * @throws UncheckedIOException if the file does not exist.
     */
    public static BinaryData fromFile(Path file, Long position, Long length, int chunkSize) {
        return new BinaryData(new FileContent(file, chunkSize, position, length));
    }

    /**
     * Returns a byte array representation of this {@link BinaryData}.
     * <p>
     * This method returns a reference to the underlying byte array. Modifying the contents of the returned byte array
     * may change the content of this BinaryData instance. If the content source of this BinaryData instance is a file,
     * an {@link InputStream}, or a {@code Flux<ByteBuffer>} the source is not modified. To safely update the byte
     * array, it is recommended to make a copy of the contents first.
     * <p>
     * If the {@link BinaryData} is larger than the maximum size allowed for a {@code byte[]} this will throw an
     * {@link IllegalStateException}.
     *
     * @return A byte array representing this {@link BinaryData}.
     *
     * @throws IllegalStateException If the {@link BinaryData} is larger than the maximum size allowed for a
     * {@code byte[]}.
     */
    public byte[] toBytes() {
        return content.toBytes();
    }

    /**
     * Returns a {@link String} representation of this {@link BinaryData} by converting its data using the UTF-8
     * character set. A new instance of String is created each time this method is called.
     * <p>
     * If the {@link BinaryData} is larger than the maximum size allowed for a {@link String} this will throw an
     * {@link IllegalStateException}.
     *
     * @return A {@link String} representing this {@link BinaryData}.
     *
     * @throws IllegalStateException If the {@link BinaryData} is larger than the maximum size allowed for a
     * {@link String}.
     */
    public String toString() {
        return content.toString();
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use
     * {@link #toObject(TypeReference)}.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.toObject#Class -->
     * <!-- end com.generic.core.util.BinaryData.toObject#Class -->
     *
     * @param <T> Type of the deserialized Object.
     * @param clazz The {@link Class} representing the Object's type.
     *
     * @return An {@link Object} representing the JSON deserialized {@link BinaryData}.
     *
     * @throws NullPointerException If {@code clazz} is null.
     *
     * @see ObjectSerializer
     */
    public <T> T toObject(Class<T> clazz) {
        return toObject(TypeReference.createInstance(clazz), SERIALIZER);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a subtype of {@link TypeReference}, if the type is non-generic use
     * {@link TypeReference#createInstance(Class)}.
     * <p>
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.toObject#TypeReference -->
     * <!-- end com.generic.core.util.BinaryData.toObject#TypeReference -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.toObject#TypeReference-generic -->
     * <!-- end com.generic.core.util.BinaryData.toObject#TypeReference-generic -->
     *
     * @param typeReference The {@link TypeReference} representing the Object's type.
     * @param <T> Type of the deserialized Object.
     *
     * @return An {@link Object} representing the JSON deserialized {@link BinaryData}.
     *
     * @throws NullPointerException If {@code typeReference} is null.
     *
     * @see ObjectSerializer
     */
    public <T> T toObject(TypeReference<T> typeReference) {
        return toObject(typeReference, SERIALIZER);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the passed
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use
     * {@link #toObject(TypeReference, ObjectSerializer)}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the SDKs or your own
     * implementation.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.toObject#Class-ObjectSerializer -->
     * <!-- end com.generic.core.util.BinaryData.toObject#Class-ObjectSerializer -->
     *
     * @param clazz The {@link Class} representing the Object's type.
     * @param serializer The {@link ObjectSerializer} used to deserialize object.
     * @param <T> Type of the deserialized Object.
     *
     * @return An {@link Object} representing the deserialized {@link BinaryData}.
     *
     * @throws NullPointerException If {@code clazz} or {@code serializer} is null.
     *
     * @see ObjectSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public <T> T toObject(Class<T> clazz, ObjectSerializer serializer) {
        return toObject(TypeReference.createInstance(clazz), serializer);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the passed
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a subtype of {@link TypeReference}, if the type is non-generic use
     * {@link TypeReference#createInstance(Class)}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the SDKs or your own
     * implementation.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer -->
     * <!-- end com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic -->
     * <!-- end com.generic.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic -->
     *
     * @param typeReference The {@link TypeReference} representing the Object's type.
     * @param serializer The {@link ObjectSerializer} used to deserialize the object.
     * @param <T> Type of the deserialized Object.
     *
     * @return An {@link Object} representing the deserialized {@link BinaryData}.
     *
     * @throws NullPointerException If {@code typeReference} or {@code serializer} is null.
     *
     * @see ObjectSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        Objects.requireNonNull(typeReference, "'typeReference' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");

        return content.toObject(typeReference, serializer);
    }

    /**
     * Returns an {@link InputStream} representation of this {@link BinaryData}.
     *
     * <p><strong>Get an InputStream from the BinaryData</strong></p>
     *
     * <!-- src_embed com.generic.core.util.BinaryData.toStream -->
     * <!-- end com.generic.core.util.BinaryData.toStream -->
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
     * <!-- src_embed com.generic.util.BinaryData.toByteBuffer -->
     * <!-- end com.generic.util.BinaryData.toByteBuffer -->
     *
     * @return A read-only {@link ByteBuffer} representing the {@link BinaryData}.
     */
    public ByteBuffer toByteBuffer() {
        return content.toByteBuffer();
    }

    /**
     * Returns the length of the content, if it is known. The length can be {@code null} if the source did not specify
     * the length or the length cannot be determined without reading the whole content.
     *
     * @return The length of the content, if it is known.
     */
    public Long getLength() {
        return content.getLength();
    }

    /**
     * Returns a flag indicating whether the content can be repeatedly consumed using all accessors including
     * {@link #toStream()}.
     *
     * <p>
     * Replayability does not imply thread-safety. The caller must not use data accessors simultaneously regardless of
     * what this method returns.
     * </p>
     *
     * <!-- src_embed com.generic.util.BinaryData.replayability -->
     * <!-- end com.generic.util.BinaryData.replayability -->
     *
     * <!-- src_embed com.generic.util.BinaryData.replayabilityAsync -->
     * <!-- end com.generic.util.BinaryData.replayabilityAsync -->
     *
     * @return A flag indicating whether the content can be repeatedly consumed using all accessors.
     */
    public boolean isReplayable() {
        return content.isReplayable();
    }

    /**
     * Converts the {@link BinaryData} into a {@link BinaryData} that is replayable, i.e. content can be consumed
     * repeatedly using all accessors including {@link #toStream()}.
     *
     * <p>
     * A {@link BinaryData} that is already replayable is returned as is. Otherwise techniques like marking and
     * resetting a stream or buffering in memory are employed to assure replayability.
     * </p>
     *
     * <p>
     * Replayability does not imply thread-safety. The caller must not use data accessors of returned {@link BinaryData}
     * simultaneously.
     * </p>
     *
     * <!-- src_embed com.generic.util.BinaryData.replayability -->
     * <!-- end com.generic.util.BinaryData.replayability -->
     *
     * @return A replayable {@link BinaryData}.
     */
    public BinaryData toReplayableBinaryData() {
        if (this.isReplayable()) {
            return this;
        } else {
            return new BinaryData(content.toReplayableContent());
        }
    }
}
