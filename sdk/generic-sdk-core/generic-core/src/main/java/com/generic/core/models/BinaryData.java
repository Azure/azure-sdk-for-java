// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.implementation.util.BinaryDataContent;
import com.generic.core.implementation.util.BinaryDataHelper;
import com.generic.core.implementation.util.ByteArrayContent;
import com.generic.core.util.serializer.JsonSerializer;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;

/**
 * BinaryData is a convenient data interchange class for use throughout the Core SDK for Java. Put simply, BinaryData
 * enables developers to bring data in from external sources, and read it back from services, in formats that appeal to
 * them. This leaves BinaryData, and the Core SDK for Java, the task of converting this data into appropriate formats
 * to be transferred to and from these external services. This enables developers to focus on their business logic, and
 * enables the Core SDK for Java to optimize operations for best performance.
 * <p>
 * BinaryData in its simplest form can be thought of as a container for content. Often this content is already in-memory
 * as a String, byte array, or an Object that can be serialized into a String or byte[]. When the BinaryData is about to
 * be sent to a service, this in-memory content is copied into the network request and sent to the service.
 * </p>
 * <p>
 * In more performance critical scenarios, where copying data into memory results in increased memory pressure, it is
 * possible to create a BinaryData instance from a stream of data. From this, BinaryData can be connected directly to
 * the outgoing network connection so that the stream is read directly to the network, without needing to first be read
 * into memory on the system. Similarly, it is possible to read a stream of data from a BinaryData returned from a
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
 * {@link BinaryData} can be created from an {@link InputStream}, a {@link ByteBuffer}, a {@link String},
 * an {@link Object}, a {@link Path file}, or a byte array.
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
 * <!-- src_embed com.azure.core.util.BinaryData.fromBytes#byte -->
 * <!-- end com.azure.core.util.BinaryData.fromBytes#byte -->
 *
 * <p><strong>Create an instance from a String</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromString#String -->
 * <!-- end com.azure.core.util.BinaryData.fromString#String -->
 *
 * <p><strong>Create an instance from an InputStream</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromStream#InputStream -->
 * <!-- end com.azure.core.util.BinaryData.fromStream#InputStream -->
 *
 * <p><strong>Create an instance from an Object</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromObject#Object -->
 * <!-- end com.azure.core.util.BinaryData.fromObject#Object -->
 *
 * <p><strong>Create an instance from a file</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromFile -->
 * <!-- end com.azure.core.util.BinaryData.fromFile -->
 * <!-- @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a> -->
 */
public final class BinaryData {
    static final JsonSerializer SERIALIZER = null;
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromStream#InputStream -->
     * <!-- end com.azure.core.util.BinaryData.fromStream#InputStream -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link InputStream}.
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static BinaryData fromStream(InputStream inputStream) {
        return null;
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromBytes#byte -->
     * <!-- end com.azure.core.util.BinaryData.fromBytes#byte -->
     *
     * @param data The byte array that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the byte array.
     * @throws NullPointerException If {@code data} is null.
     */
    public static BinaryData fromBytes(byte[] data) {
        return new BinaryData(new ByteArrayContent(data));
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromFile -->
     * <!-- end com.azure.core.util.BinaryData.fromFile -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @return A new {@link BinaryData}.
     * @throws NullPointerException If {@code file} is null.
     */
    public static BinaryData fromFile(Path file) {
        return null;
    }

    /**
     * Returns a byte array representation of this {@link BinaryData}.
     * <p>
     * This method returns a reference to the underlying byte array. Modifying the contents of the returned byte array
     * may change the content of this BinaryData instance. If the content source of this BinaryData instance is a file
     * or an {@link InputStream} the source is not modified. To safely update the byte array, it is recommended to make
     * a copy of the contents first.
     * <p>
     * If the {@link BinaryData} is larger than the maximum size allowed for a {@code byte[]} this will throw an
     * {@link IllegalStateException}.
     *
     * @return A byte array representing this {@link BinaryData}.
     * @throws IllegalStateException If the {@link BinaryData} is larger than the maximum size allowed for a
     * {@code byte[]}.
     */
    public byte[] toBytes() {
        return content.toBytes();
    }

    /**
     * Returns an {@link InputStream} representation of this {@link BinaryData}.
     *
     * <p><strong>Get an InputStream from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toStream -->
     * <!-- end com.azure.core.util.BinaryData.toStream -->
     *
     * @return An {@link InputStream} representing the {@link BinaryData}.
     */
    public InputStream toStream() {
        return content.toStream();
    }

    /**
     * Returns the length of the content, if it is known. The length can be {@code null} if the source did not specify
     * the length or the length cannot be determined without reading the whole content.
     *
     * @return the length of the content, if it is known.
     */
    public Long getLength() {
        return content.getLength();
    }
}
