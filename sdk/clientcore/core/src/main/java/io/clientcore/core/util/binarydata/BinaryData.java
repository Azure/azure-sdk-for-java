// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.binarydata;

import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * BinaryData is a convenient data interchange class for use throughout the SDK for Java. Put simply, BinaryData enables
 * developers to bring data in from external sources, and read it back from services, in formats that appeal to them.
 * This leaves BinaryData, and the SDK for Java, the task of converting this data into appropriate formats to be
 * transferred to and from these external services. This enables developers to focus on their business logic, and
 * enables the SDK for Java to optimize operations for best performance.
 *
 * <p>BinaryData in its simplest form can be thought of as a container for content. Often this content is already in-memory
 * as a String, byte array, or an Object that can be serialized into a String or byte[]. When the BinaryData is about to
 * be sent to a service, this in-memory content is copied into the network request and sent to the service.</p>
 *
 * <p>In more performance critical scenarios, where copying data into memory results in increased memory pressure, it is
 * possible to create a BinaryData instance from a stream of data. From this, BinaryData can be connected directly to
 * the outgoing network connection so that the stream is read directly to the network, without needing to first be read
 * into memory on the system. Similarly, it is possible to read a stream of data from a BinaryData returned from an
 * service without it first being read into memory. In many situations, these streaming operations can drastically
 * reduce the memory pressure in applications, and so it is encouraged that all developers very carefully consider their
 * ability to use the most appropriate API in BinaryData whenever they encounter an client library that makes use of
 * BinaryData.</p>
 *
 * <p>Refer to the documentation of each method in the BinaryData class to better understand its performance
 * characteristics, and refer to the samples below to understand the common usage scenarios of this class.</p>
 *
 * {@link BinaryData} can be created from an {@link InputStream}, of {@link ByteBuffer}, a {@link String}, an
 * {@link Object}, a {@link Path file}, or a byte array.
 *
 * <p><strong>A note on data mutability</strong></p>
 *
 * {@link BinaryData} does not copy data on construction. BinaryData keeps a reference to the source content and is
 * accessed when a read request is made. So, any modifications to the underlying source before the content is read can
 * result in undefined behavior.
 *
 * <p>To create an instance of {@link BinaryData}, use the various static factory methods available. They all start with
 * {@code 'from'} prefix, for example {@link BinaryData#fromBytes(byte[])}.</p>
 *
 * <p><strong>Create an instance from a byte array</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.BinaryData.fromBytes#byte -->
 * <pre>
 * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
 * BinaryData binaryData = BinaryData.fromBytes&#40;data&#41;;
 * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.BinaryData.fromBytes#byte -->
 *
 * <p><strong>Create an instance from a String</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.BinaryData.fromString#String -->
 * <pre>
 * final String data = &quot;Some Data&quot;;
 * &#47;&#47; Following will use default character set as StandardCharsets.UTF_8
 * BinaryData binaryData = BinaryData.fromString&#40;data&#41;;
 * System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.BinaryData.fromString#String -->
 *
 * <p><strong>Create an instance from an InputStream</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.BinaryData.fromStream#InputStream -->
 * <pre>
 * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;&quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
 * BinaryData binaryData = BinaryData.fromStream&#40;inputStream&#41;;
 * System.out.println&#40;binaryData&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.BinaryData.fromStream#InputStream -->
 *
 * <p><strong>Create an instance from an Object</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.BinaryData.fromObject#Object -->
 * <pre>
 * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
 *
 * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
 * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-jackson or
 * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-gson
 * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
 *
 * System.out.println&#40;binaryData&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.BinaryData.fromObject#Object -->
 *
 * <p><strong>Create an instance from a file</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.BinaryData.fromFile -->
 * <pre>
 * BinaryData binaryData = BinaryData.fromFile&#40;new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;&#41;;
 * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.BinaryData.fromFile -->
 *
 * @see ObjectSerializer
 * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
 */
public abstract class BinaryData implements Closeable {
    private static final BinaryData EMPTY = BinaryData.fromBytes(new byte[0]);

    static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
    static final int STREAM_READ_SIZE = 8192;
    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    static final String TOO_LARGE_FOR_BYTE_ARRAY
        = "The content length is too large for a byte array. Content length is: ";

    /**
     * Creates a new instance of {@link BinaryData}.
     */
    public BinaryData() {
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link InputStream}. Depending on the type of
     * InputStream, the BinaryData instance created may or may not allow reading the content more than once. The stream
     * content is not cached if the stream is not read into a format that requires the content to be fully read into
     * memory.
     *
     * <p><b>NOTE:</b> The {@link InputStream} is not closed by this function.</p>
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromStream#InputStream -->
     * <pre>
     * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;&quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * BinaryData binaryData = BinaryData.fromStream&#40;inputStream&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromStream#InputStream -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link InputStream}.
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
     *
     * <p><b>NOTE:</b> The {@link InputStream} is not closed by this function.</p>
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromStream#InputStream-Long -->
     * <pre>
     * byte[] bytes = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;bytes&#41;;
     * BinaryData binaryData = BinaryData.fromStream&#40;inputStream, &#40;long&#41; bytes.length&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromStream#InputStream-Long -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @param length The length of {@code data} in bytes.
     * @return A {@link BinaryData} representing the {@link InputStream}.
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static BinaryData fromStream(InputStream inputStream, Long length) {
        return new InputStreamBinaryData(inputStream, length);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link String}.
     *
     * <p>The {@link String} is converted into bytes using {@link String#getBytes(Charset)} passing
     * {@link StandardCharsets#UTF_8}.</p>
     *
     * <p><strong>Create an instance from a String</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromString#String -->
     * <pre>
     * final String data = &quot;Some Data&quot;;
     * &#47;&#47; Following will use default character set as StandardCharsets.UTF_8
     * BinaryData binaryData = BinaryData.fromString&#40;data&#41;;
     * System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromString#String -->
     *
     * @param data The {@link String} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link String}.
     * @throws NullPointerException If {@code data} is null.
     */
    public static BinaryData fromString(String data) {
        return new StringBinaryData(data);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given byte array.
     *
     * <p>If the byte array is zero length an empty {@link BinaryData} will be returned. Note that the input byte array is
     * used as a reference by this instance of {@link BinaryData} and any changes to the byte array outside of this
     * instance will result in the contents of this BinaryData instance being updated as well. To safely update the byte
     * array without impacting the BinaryData instance, perform an array copy first.</p>
     *
     * <p><strong>Create an instance from a byte array</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromBytes#byte -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * BinaryData binaryData = BinaryData.fromBytes&#40;data&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromBytes#byte -->
     *
     * @param data The byte array that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the byte array.
     * @throws NullPointerException If {@code data} is null.
     */
    public static BinaryData fromBytes(byte[] data) {
        return new ByteArrayBinaryData(data);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link ByteBuffer}.
     *
     * <p>If the {@link ByteBuffer} is zero length an empty {@link BinaryData} will be returned. Note that the input
     * {@link ByteBuffer} is used as a reference by this instance of {@link BinaryData} and any changes to the
     * {@link ByteBuffer} outside of this instance will result in the contents of this BinaryData instance being updated
     * as well. To safely update the {@link ByteBuffer} without impacting the BinaryData instance, perform an array copy
     * first.</p>
     *
     * <p><strong>Create an instance from a ByteBuffer</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromByteBuffer#ByteBuffer -->
     * <pre>
     * final ByteBuffer data = ByteBuffer.wrap&#40;&quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * BinaryData binaryData = BinaryData.fromByteBuffer&#40;data&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromByteBuffer#ByteBuffer -->
     *
     * @param data The {@link ByteBuffer} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link ByteBuffer}.
     * @throws NullPointerException If {@code data} is null.
     */
    public static BinaryData fromByteBuffer(ByteBuffer data) {
        return new ByteBufferBinaryData(data);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link List} of {@link ByteBuffer}.
     *
     * <p>The input {@link ByteBuffer} instances are used as a reference by this instance of {@link BinaryData} and any
     * changes to a {@link ByteBuffer} outside of this instance will result in the contents of this BinaryData instance
     * being updated as well. To safely update the byte array without impacting the BinaryData instance, perform an
     * array copy first.</p>
     *
     * <p><strong>Create an instance from a List&lt;ByteBuffer&gt;</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromListByteBuffer#List -->
     * <pre>
     * final List&lt;ByteBuffer&gt; data = Stream.of&#40;&quot;Some &quot;, &quot;data&quot;&#41;
     *     .map&#40;s -&gt; ByteBuffer.wrap&#40;s.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;&#41;
     *     .collect&#40;Collectors.toList&#40;&#41;&#41;;
     * BinaryData binaryData = BinaryData.fromListByteBuffer&#40;data&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromListByteBuffer#List -->
     *
     * @param data The {@link List} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link List} of {@link ByteBuffer}.
     */
    public static BinaryData fromListByteBuffer(List<ByteBuffer> data) {
        return new ListByteBufferBinaryData(data);
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the default
     * {@link ObjectSerializer}.
     *
     * <p><strong>Creating an instance from an Object</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromObject#Object -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-gson
     * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
     *
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromObject#Object -->
     *
     * @param data The object that will be JSON serialized that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the JSON serialized object.
     * @throws NullPointerException If {@code data} is null.
     * @see ObjectSerializer
     */
    public static BinaryData fromObject(Object data) {
        return fromObject(data, SERIALIZER);
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the passed
     * {@link ObjectSerializer}.
     *
     * <p>The passed {@link ObjectSerializer} can either be one of the implementations offered by the SDK or your own
     * implementation.</p>
     *
     * <p><strong>Create an instance from an Object</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromObject#Object-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-gson
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;data, serializer&#41;;
     *
     * System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromObject#Object-ObjectSerializer -->
     *
     * @param data The object that will be serialized that {@link BinaryData} will represent. The {@code serializer}
     * determines how {@code null} data is serialized.
     * @param serializer The {@link ObjectSerializer} used to serialize object.
     * @return A {@link BinaryData} representing the serialized object.
     * @throws NullPointerException If {@code serializer} is null.
     * @see ObjectSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        return new SerializableBinaryData(data, serializer);
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
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromFile -->
     * <pre>
     * BinaryData binaryData = BinaryData.fromFile&#40;new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromFile -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @return A new {@link BinaryData}.
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
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromFile#Path-int -->
     * <pre>
     * BinaryData binaryData = BinaryData.fromFile&#40;new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;, 8092&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromFile#Path-int -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @param chunkSize The requested size for each read of the path.
     * @return A new {@link BinaryData}.
     * @throws NullPointerException If {@code file} is null.
     * @throws IllegalArgumentException If {@code offset} or {@code length} are negative or {@code offset} plus
     * {@code length} is greater than the file size or {@code chunkSize} is less than or equal to 0.
     * @throws UncheckedIOException if the file does not exist.
     */
    public static BinaryData fromFile(Path file, int chunkSize) {
        return new FileBinaryData(file, chunkSize, null, null);
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
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromFile#Path-Long-Long -->
     * <pre>
     * long position = 1024;
     * long length = 100 * 1048;
     * BinaryData binaryData = BinaryData.fromFile&#40;
     *     new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;, position, length&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromFile#Path-Long-Long -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @param position Position, or offset, within the path where reading begins.
     * @param length Maximum number of bytes to be read from the path.
     * @return A new {@link BinaryData}.
     * @throws NullPointerException If {@code file} is null.
     * @throws IllegalArgumentException If {@code offset} or {@code length} are negative or {@code offset} plus
     * {@code length} is greater than the file size or {@code chunkSize} is less than or equal to 0.
     * @throws UncheckedIOException if the file does not exist.
     */
    public static BinaryData fromFile(Path file, Long position, Long length) {
        return new FileBinaryData(file, STREAM_READ_SIZE, position, length);
    }

    /**
     * Creates a {@link BinaryData} that uses the content of the file at {@link Path file} as its data. This method
     * checks for the existence of the file at the time of creating an instance of {@link BinaryData}. The file,
     * however, is not read until there is an attempt to read the contents of the returned BinaryData instance.
     *
     * <p><strong>Create an instance from a file</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.fromFile#Path-Long-Long-int -->
     * <pre>
     * long position = 1024;
     * long length = 100 * 1048;
     * int chunkSize = 8092;
     * BinaryData binaryData = BinaryData.fromFile&#40;
     *     new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;, position, length, chunkSize&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.fromFile#Path-Long-Long-int -->
     *
     * @param file The {@link Path} that will be the {@link BinaryData} data.
     * @param position Position, or offset, within the path where reading begins.
     * @param length Maximum number of bytes to be read from the path.
     * @param chunkSize The requested size for each read of the path.
     * @return A new {@link BinaryData}.
     * @throws NullPointerException If {@code file} is null.
     * @throws IllegalArgumentException If {@code offset} or {@code length} are negative or {@code offset} plus
     * {@code length} is greater than the file size or {@code chunkSize} is less than or equal to 0.
     * @throws UncheckedIOException if the file does not exist.
     */
    public static BinaryData fromFile(Path file, Long position, Long length, int chunkSize) {
        return new FileBinaryData(file, chunkSize, position, length);
    }

    /**
     * Returns a byte array representation of this {@link BinaryData}.
     *
     * <p>This method returns a reference to the underlying byte array. Modifying the contents of the returned byte
     * array may change the content of this BinaryData instance. If the content source of this BinaryData instance is
     * a file, an {@link InputStream}, or a {@code Flux<ByteBuffer>} the source is not modified. To safely update the
     * byte array, it is recommended to make a copy of the contents first.</p>
     *
     * <p>If the {@link BinaryData} is larger than the maximum size allowed for a {@code byte[]} this will throw an
     * {@link IllegalStateException}.</p>
     *
     * @return A byte array representing this {@link BinaryData}.
     * @throws IllegalStateException If the {@link BinaryData} is larger than the maximum size allowed for a
     * {@code byte[]}.
     */
    public abstract byte[] toBytes();

    /**
     * Returns a {@link String} representation of this {@link BinaryData} by converting its data using the UTF-8
     * character set. A new instance of String is created each time this method is called.
     *
     * <p>If the {@link BinaryData} is larger than the maximum size allowed for a {@link String} this will throw an
     * {@link IllegalStateException}.</p>
     *
     * @return A {@link String} representing this {@link BinaryData}.
     * @throws IllegalStateException If the {@link BinaryData} is larger than the maximum size allowed for a
     * {@link String}.
     */
    public abstract String toString();

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     *
     * <p>The type, represented by {@link Type}, can either be a generic or non-generic type. If the type is generic
     * create a {@link ParameterizedType}, if the type is non-generic use a {@link Class}.</p>
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.toObject#Type -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Ensure your classpath have the Serializer to serialize the object which implement implement
     * &#47;&#47; io.clientcore.core.serializer.util.JsonSerializer interface.
     * &#47;&#47; Or use Azure provided libraries for this.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-gson
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
     *
     * Person person = binaryData.toObject&#40;Person.class&#41;;
     * System.out.println&#40;person.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.toObject#Type -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.toObject#Type-generic -->
     * <pre>
     * final Person person1 = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     * final Person person2 = new Person&#40;&#41;.setName&#40;&quot;Jack&quot;&#41;;
     *
     * List&lt;Person&gt; personList = new ArrayList&lt;&gt;&#40;&#41;;
     * personList.add&#40;person1&#41;;
     * personList.add&#40;person2&#41;;
     *
     * &#47;&#47; Ensure your classpath have the Serializer to serialize the object which implement implement
     * &#47;&#47; io.clientcore.core.serializer.util.JsonSerializer interface.
     * &#47;&#47; Or use Azure provided libraries for this.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-gson
     *
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;personList&#41;;
     *
     * &#47;&#47; Creation of the ParameterizedType could be replaced with a utility method that returns a Type based on the
     * &#47;&#47; type arguments and raw type passed.
     * List&lt;Person&gt; persons = binaryData.toObject&#40;new ParameterizedType&#40;&#41; &#123;
     *     &#64;Override
     *     public Type[] getActualTypeArguments&#40;&#41; &#123;
     *         return new Type[] &#123; Person.class &#125;;
     *     &#125;
     *
     *     &#64;Override
     *     public Type getRawType&#40;&#41; &#123;
     *         return List.class;
     *     &#125;
     *
     *     &#64;Override
     *     public Type getOwnerType&#40;&#41; &#123;
     *         return null;
     *     &#125;
     * &#125;&#41;;
     * persons.forEach&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.toObject#Type-generic -->
     *
     * @param type The {@link Type} representing the Object's type.
     * @param <T> Type of the deserialized Object.
     * @return An {@link Object} representing the JSON deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code type} is null.
     * @throws IOException If deserialization fails.
     * @see ObjectSerializer
     */
    public <T> T toObject(Type type) {
        return toObject(type, SERIALIZER);
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the passed
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     *
     * <p>The type, represented by {@link Type}, can either be a generic or non-generic type. If the type is generic
     * create a {@link ParameterizedType}, if the type is non-generic use a {@link Class}.</p>
     *
     * <p>The passed {@link ObjectSerializer} can either be one of the implementations offered by the SDKs or your own
     * implementation.</p>
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.toObject#Type-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;io.clientcore&#47;azure-core-serializer-json-gson
     *
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;data, serializer&#41;;
     *
     * Person person = binaryData.toObject&#40;Person.class, serializer&#41;;
     * System.out.println&#40;&quot;Name : &quot; + person.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.toObject#Type-ObjectSerializer -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.toObject#Type-ObjectSerializer-generic -->
     * <pre>
     * final Person person1 = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     * final Person person2 = new Person&#40;&#41;.setName&#40;&quot;Jack&quot;&#41;;
     *
     * List&lt;Person&gt; personList = new ArrayList&lt;&gt;&#40;&#41;;
     * personList.add&#40;person1&#41;;
     * personList.add&#40;person2&#41;;
     *
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;personList, serializer&#41;;
     *
     * &#47;&#47; Creation of the ParameterizedType could be replaced with a utility method that returns a Type based on the
     * &#47;&#47; type arguments and raw type passed.
     * List&lt;Person&gt; persons = binaryData.toObject&#40;new ParameterizedType&#40;&#41; &#123;
     *     &#64;Override
     *     public Type[] getActualTypeArguments&#40;&#41; &#123;
     *         return new Type[] &#123; Person.class &#125;;
     *     &#125;
     *
     *     &#64;Override
     *     public Type getRawType&#40;&#41; &#123;
     *         return List.class;
     *     &#125;
     *
     *     &#64;Override
     *     public Type getOwnerType&#40;&#41; &#123;
     *         return null;
     *     &#125;
     * &#125;, serializer&#41;;
     * persons.forEach&#40;person -&gt; System.out.println&#40;&quot;Name : &quot; + person.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.toObject#Type-ObjectSerializer-generic -->
     *
     * @param type The {@link Type} representing the Object's type.
     * @param serializer The {@link ObjectSerializer} used to deserialize the object.
     * @param <T> Type of the deserialized Object.
     * @return An {@link Object} representing the deserialized {@link BinaryData}.
     * @throws NullPointerException If {@code type} or {@code serializer} is null.
     * @throws IOException If deserialization fails.
     * @see ObjectSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public abstract <T> T toObject(Type type, ObjectSerializer serializer);

    /**
     * Returns an {@link InputStream} representation of this {@link BinaryData}.
     *
     * <p><strong>Get an InputStream from the BinaryData</strong></p>
     *
     * <!-- src_embed io.clientcore.core.util.BinaryData.toStream -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * BinaryData binaryData = BinaryData.fromStream&#40;new ByteArrayInputStream&#40;data&#41;, &#40;long&#41; data.length&#41;;
     * final byte[] bytes = new byte[data.length];
     * try &#40;InputStream inputStream = binaryData.toStream&#40;&#41;&#41; &#123;
     *     inputStream.read&#40;bytes, 0, data.length&#41;;
     *     System.out.println&#40;new String&#40;bytes&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end io.clientcore.core.util.BinaryData.toStream -->
     *
     * @return An {@link InputStream} representing the {@link BinaryData}.
     */
    public abstract InputStream toStream();

    /**
     * Writes the contents of this {@link BinaryData} to the given {@link OutputStream}.
     * <p>
     * This method does not close the {@link OutputStream}.
     * <p>
     * The contents of this {@link BinaryData} will be written without buffering. If the underlying data source isn't
     * {@link #isReplayable()}, after this method is called the {@link BinaryData} will be consumed and can't be read
     * again. If it needs to be read again, use {@link #toReplayableBinaryData()} to create a replayable copy.
     *
     * @param outputStream The {@link OutputStream} to write the contents of this {@link BinaryData} to.
     * @throws NullPointerException If {@code outputStream} is null.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(toBytes());
    }

    /**
     * Writes the contents of this {@link BinaryData} to the given {@link WritableByteChannel}.
     * <p>
     * This method does not close the {@link WritableByteChannel}.
     * <p>
     * The contents of this {@link BinaryData} will be written without buffering. If the underlying data source isn't
     * {@link #isReplayable()}, after this method is called the {@link BinaryData} will be consumed and can't be read
     * again. If it needs to be read again, use {@link #toReplayableBinaryData()} to create a replayable copy.
     *
     * @param channel The {@link WritableByteChannel} to write the contents of this {@link BinaryData} to.
     * @throws NullPointerException If {@code outputStream} is null.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTo(WritableByteChannel channel) throws IOException {
        ByteBuffer buffer = toByteBuffer().duplicate();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    /**
     * Returns a read-only {@link ByteBuffer} representation of this {@link BinaryData}.
     *
     * <p>Attempting to mutate the returned {@link ByteBuffer} will throw a {@link ReadOnlyBufferException}.</p>
     *
     * <p><strong>Get a read-only ByteBuffer from the BinaryData</strong></p>
     *
     * <!-- src_embed io.clientcore.coreutil.BinaryData.toByteBuffer -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * BinaryData binaryData = BinaryData.fromBytes&#40;data&#41;;
     * final byte[] bytes = new byte[data.length];
     * binaryData.toByteBuffer&#40;&#41;.get&#40;bytes, 0, data.length&#41;;
     * System.out.println&#40;new String&#40;bytes&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.coreutil.BinaryData.toByteBuffer -->
     *
     * @return A read-only {@link ByteBuffer} representing the {@link BinaryData}.
     */
    public abstract ByteBuffer toByteBuffer();

    /**
     * Returns the length of the content, if it is known. The length can be {@code null} if the source did not specify
     * the length or the length cannot be determined without reading the whole content.
     *
     * @return The length of the content, if it is known.
     */
    public abstract Long getLength();

    /**
     * Returns a flag indicating whether the content can be repeatedly consumed using all accessors including
     * {@link #toStream()}.
     *
     * <p>Replayability does not imply thread-safety. The caller must not use data accessors simultaneously regardless
     * of what this method returns.</p>
     *
     * <!-- src_embed io.clientcore.coreutil.BinaryData.replayability -->
     * <pre>
     * BinaryData binaryData = binaryDataProducer&#40;&#41;;
     *
     * if &#40;!binaryData.isReplayable&#40;&#41;&#41; &#123;
     *     binaryData = binaryData.toReplayableBinaryData&#40;&#41;;
     * &#125;
     *
     * streamConsumer&#40;binaryData.toStream&#40;&#41;&#41;;
     * streamConsumer&#40;binaryData.toStream&#40;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.coreutil.BinaryData.replayability -->
     *
     * @return A flag indicating whether the content can be repeatedly consumed using all accessors.
     */
    public abstract boolean isReplayable();

    /**
     * Converts the {@link BinaryData} into a {@link BinaryData} that is replayable, i.e. content can be consumed
     * repeatedly using all accessors including {@link #toStream()}.
     *
     * <p>A {@link BinaryData} that is already replayable is returned as is. Otherwise techniques like marking and
     * resetting a stream or buffering in memory are employed to assure replayability.</p>
     *
     * <p>Replayability does not imply thread-safety. The caller must not use data accessors of returned
     * {@link BinaryData} simultaneously.</p>
     *
     * <!-- src_embed io.clientcore.coreutil.BinaryData.replayability -->
     * <pre>
     * BinaryData binaryData = binaryDataProducer&#40;&#41;;
     *
     * if &#40;!binaryData.isReplayable&#40;&#41;&#41; &#123;
     *     binaryData = binaryData.toReplayableBinaryData&#40;&#41;;
     * &#125;
     *
     * streamConsumer&#40;binaryData.toStream&#40;&#41;&#41;;
     * streamConsumer&#40;binaryData.toStream&#40;&#41;&#41;;
     * </pre>
     * <!-- end io.clientcore.coreutil.BinaryData.replayability -->
     *
     * @return A replayable {@link BinaryData}.
     */
    public abstract BinaryData toReplayableBinaryData();

    /**
     * An empty {@link BinaryData} that is immutable, used in situations where there is no binary data, but a
     * {@link BinaryData} instance is expected.
     *
     * @return The singleton instance of an empty {@link BinaryData}.
     */
    public static BinaryData empty() {
        return BinaryData.EMPTY;
    }
}
