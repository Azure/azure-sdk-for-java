// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.implementation.util.BinaryDataContent;
import com.typespec.core.implementation.util.BinaryDataHelper;
import com.typespec.core.implementation.util.ByteArrayContent;
import com.typespec.core.implementation.util.ByteBufferContent;
import com.typespec.core.implementation.util.FileContent;
import com.typespec.core.implementation.util.FluxByteBufferContent;
import com.typespec.core.implementation.util.InputStreamContent;
import com.typespec.core.implementation.util.ListByteBufferContent;
import com.typespec.core.implementation.util.SerializableContent;
import com.typespec.core.implementation.util.StringContent;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.JsonSerializer;
import com.typespec.core.util.serializer.JsonSerializerProvider;
import com.typespec.core.util.serializer.JsonSerializerProviders;
import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import static com.typespec.core.implementation.util.BinaryDataContent.STREAM_READ_SIZE;
import static com.typespec.core.util.FluxUtil.monoError;

/**
 * BinaryData is a convenient data interchange class for use throughout the Azure SDK for Java. Put simply, BinaryData
 * enables developers to bring data in from external sources, and read it back from Azure services, in formats that
 * appeal to them. This leaves BinaryData, and the Azure SDK for Java, the task of converting this data into appropriate
 * formats to be transferred to and from these external services. This enables developers to focus on their business
 * logic, and enables the Azure SDK for Java to optimize operations for best performance.
 * <p>
 * BinaryData in its simplest form can be thought of as a container for content. Often this content is already in-memory
 * as a String, byte array, or an Object that can be serialized into a String or byte[]. When the BinaryData is about to
 * be sent to an Azure Service, this in-memory content is copied into the network request and sent to the service.
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
 * {@link BinaryData} can be created from an {@link InputStream}, a {@link Flux} of {@link ByteBuffer}, a
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
 * <!-- src_embed com.azure.core.util.BinaryData.fromBytes#byte -->
 * <pre>
 * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
 * BinaryData binaryData = BinaryData.fromBytes&#40;data&#41;;
 * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.util.BinaryData.fromBytes#byte -->
 *
 * <p><strong>Create an instance from a String</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromString#String -->
 * <pre>
 * final String data = &quot;Some Data&quot;;
 * &#47;&#47; Following will use default character set as StandardCharsets.UTF_8
 * BinaryData binaryData = BinaryData.fromString&#40;data&#41;;
 * System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.util.BinaryData.fromString#String -->
 *
 * <p><strong>Create an instance from an InputStream</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromStream#InputStream -->
 * <pre>
 * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;&quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
 * BinaryData binaryData = BinaryData.fromStream&#40;inputStream&#41;;
 * System.out.println&#40;binaryData&#41;;
 * </pre>
 * <!-- end com.azure.core.util.BinaryData.fromStream#InputStream -->
 *
 * <p><strong>Create an instance from an Object</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromObject#Object -->
 * <pre>
 * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
 *
 * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
 * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
 * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
 * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
 *
 * System.out.println&#40;binaryData&#41;;
 * </pre>
 * <!-- end com.azure.core.util.BinaryData.fromObject#Object -->
 *
 * <p><strong>Create an instance from {@code Flux<ByteBuffer>}</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromFlux#Flux -->
 * <pre>
 * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
 * final Flux&lt;ByteBuffer&gt; dataFlux = Flux.just&#40;ByteBuffer.wrap&#40;data&#41;&#41;;
 *
 * Mono&lt;BinaryData&gt; binaryDataMono = BinaryData.fromFlux&#40;dataFlux&#41;;
 *
 * Disposable subscriber = binaryDataMono
 *     .map&#40;binaryData -&gt; &#123;
 *         System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
 *         return true;
 *     &#125;&#41;
 *     .subscribe&#40;&#41;;
 *
 * &#47;&#47; So that your program wait for above subscribe to complete.
 * TimeUnit.SECONDS.sleep&#40;5&#41;;
 * subscriber.dispose&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.util.BinaryData.fromFlux#Flux -->
 *
 * <p><strong>Create an instance from a file</strong></p>
 *
 * <!-- src_embed com.azure.core.util.BinaryData.fromFile -->
 * <pre>
 * BinaryData binaryData = BinaryData.fromFile&#40;new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;&#41;;
 * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.util.BinaryData.fromFile -->
 *
 * @see ObjectSerializer
 * @see JsonSerializer
 * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
 */
public final class BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(BinaryData.class);
    static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);
    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
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
     * <pre>
     * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;&quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * BinaryData binaryData = BinaryData.fromStream&#40;inputStream&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromStream#InputStream -->
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
     * <p>
     * <b>NOTE:</b> The {@link InputStream} is not closed by this function.
     * </p>
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromStream#InputStream-Long -->
     * <pre>
     * byte[] bytes = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;bytes&#41;;
     * BinaryData binaryData = BinaryData.fromStream&#40;inputStream, &#40;long&#41; bytes.length&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromStream#InputStream-Long -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @param length The length of {@code data} in bytes.
     * @return A {@link BinaryData} representing the {@link InputStream}.
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static BinaryData fromStream(InputStream inputStream, Long length) {
        return new BinaryData(new InputStreamContent(inputStream, length));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link InputStream}.
     * <b>NOTE:</b> The {@link InputStream} is not closed by this function.
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromStreamAsync#InputStream -->
     * <pre>
     * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;&quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     *
     * Mono&lt;BinaryData&gt; binaryDataMono = BinaryData.fromStreamAsync&#40;inputStream&#41;;
     *
     * Disposable subscriber = binaryDataMono
     *     .map&#40;binaryData -&gt; &#123;
     *         System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     *         return true;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromStreamAsync#InputStream -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link InputStream}.
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static Mono<BinaryData> fromStreamAsync(InputStream inputStream) {
        return fromStreamAsync(inputStream, null);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link InputStream}.
     * <b>NOTE:</b> The {@link InputStream} is not closed by this function.
     *
     * <p><strong>Create an instance from an InputStream</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromStreamAsync#InputStream-Long -->
     * <pre>
     * byte[] bytes = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * final ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;bytes&#41;;
     *
     * Mono&lt;BinaryData&gt; binaryDataMono = BinaryData.fromStreamAsync&#40;inputStream, &#40;long&#41; bytes.length&#41;;
     *
     * Disposable subscriber = binaryDataMono
     *     .map&#40;binaryData -&gt; &#123;
     *         System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     *         return true;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromStreamAsync#InputStream-Long -->
     *
     * @param inputStream The {@link InputStream} that {@link BinaryData} will represent.
     * @param length The length of {@code data} in bytes.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link InputStream}.
     * @throws UncheckedIOException If any error happens while reading the {@link InputStream}.
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static Mono<BinaryData> fromStreamAsync(InputStream inputStream, Long length) {
        return Mono.fromCallable(() -> fromStream(inputStream, length));
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link Flux} of {@link ByteBuffer}.
     *
     * <p><strong>Create an instance from a Flux of ByteBuffer</strong></p>
     *
     * <p>This method aggregates data into single byte array.</p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromFlux#Flux -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * final Flux&lt;ByteBuffer&gt; dataFlux = Flux.just&#40;ByteBuffer.wrap&#40;data&#41;&#41;;
     *
     * Mono&lt;BinaryData&gt; binaryDataMono = BinaryData.fromFlux&#40;dataFlux&#41;;
     *
     * Disposable subscriber = binaryDataMono
     *     .map&#40;binaryData -&gt; &#123;
     *         System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     *         return true;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromFlux#Flux -->
     *
     * @param data The {@link Flux} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link Flux} of {@link ByteBuffer}.
     * @throws NullPointerException If {@code data} is null.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data) {
        return fromFlux(data, null);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link Flux} of {@link ByteBuffer}.
     *
     * <p><strong>Create an instance from a Flux of ByteBuffer</strong></p>
     *
     * <p>This method aggregates data into single byte array.</p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromFlux#Flux-Long -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * final long length = data.length;
     * final Flux&lt;ByteBuffer&gt; dataFlux = Flux.just&#40;ByteBuffer.wrap&#40;data&#41;&#41;;
     *
     * Mono&lt;BinaryData&gt; binaryDataMono = BinaryData.fromFlux&#40;dataFlux, length&#41;;
     *
     * Disposable subscriber = binaryDataMono
     *     .map&#40;binaryData -&gt; &#123;
     *         System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     *         return true;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromFlux#Flux-Long -->
     *
     * @param data The {@link Flux} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @param length The length of {@code data} in bytes.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link Flux} of {@link ByteBuffer}.
     * @throws IllegalArgumentException if the length is less than zero.
     * @throws NullPointerException if {@code data} is null.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data, Long length) {
        return fromFlux(data, length, true);
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link Flux} of {@link ByteBuffer}.
     * <p>
     * If {@code bufferContent} is true and {@code length} is null the length of the returned {@link BinaryData} will be
     * based on the length calculated by buffering. If {@code length} is non-null it will always be used as the
     * {@link BinaryData} length even if buffering determines a different length.
     *
     * <p><strong>Create an instance from a Flux of ByteBuffer</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromFlux#Flux-Long-boolean -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * final long length = data.length;
     * final boolean shouldAggregateData = false;
     * final Flux&lt;ByteBuffer&gt; dataFlux = Flux.just&#40;ByteBuffer.wrap&#40;data&#41;&#41;;
     *
     * Mono&lt;BinaryData&gt; binaryDataMono = BinaryData.fromFlux&#40;dataFlux, length, shouldAggregateData&#41;;
     *
     * Disposable subscriber = binaryDataMono
     *     .map&#40;binaryData -&gt; &#123;
     *         System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     *         return true;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromFlux#Flux-Long-boolean -->
     *
     * @param data The {@link Flux} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @param length The length of {@code data} in bytes.
     * @param bufferContent A flag indicating whether {@link Flux} should be buffered eagerly or consumption deferred.
     * @return A {@link Mono} of {@link BinaryData} representing the {@link Flux} of {@link ByteBuffer}.
     * @throws IllegalArgumentException if the length is less than zero.
     * @throws NullPointerException if {@code data} is null.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data, Long length, boolean bufferContent) {
        if (data == null) {
            return monoError(LOGGER, new NullPointerException("'data' cannot be null."));
        }
        if (length != null && length < 0) {
            return monoError(LOGGER, new IllegalArgumentException("'length' cannot be less than 0."));
        }

        if (!bufferContent) {
            return Mono.just(new BinaryData(new FluxByteBufferContent(data, length)));
        }

        // Buffer the Flux<ByteBuffer> content using ByteBuffers of equal size to the original ByteBuffer.
        // Previously this was using FluxUtil.collectBytesInByteBufferStream which runs into two issues:
        //
        // 1. The content is limited in size as it collects into a byte array which is limited to ~2GB in size.
        // 2. This could lead to a very large chunk of data existing which can cause pauses when allocating large
        //    arrays.
        long[] trueLength = new long[]{0};
        return data.map(buffer -> {
            int bufferSize = buffer.remaining();
            ByteBuffer copy = ByteBuffer.allocate(bufferSize);
            trueLength[0] += bufferSize;
            copy.put(buffer);
            copy.flip();

            return copy;
        })
        .collect(LinkedList::new, (BiConsumer<LinkedList<ByteBuffer>, ByteBuffer>) LinkedList::add)
        .map(buffers -> {
            // TODO (alzimmer): What should be done when length != null but it differs from the true length
            //  seen when doing the buffering.
            return new BinaryData(new FluxByteBufferContent(Flux.fromIterable(buffers).map(ByteBuffer::duplicate),
                (length != null) ? length : trueLength[0], true));
        });
    }

    /**
     * Creates an instance of {@link BinaryData} from the given {@link String}.
     * <p>
     * The {@link String} is converted into bytes using {@link String#getBytes(Charset)} passing
     * {@link StandardCharsets#UTF_8}.
     * </p>
     * <p><strong>Create an instance from a String</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromString#String -->
     * <pre>
     * final String data = &quot;Some Data&quot;;
     * &#47;&#47; Following will use default character set as StandardCharsets.UTF_8
     * BinaryData binaryData = BinaryData.fromString&#40;data&#41;;
     * System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromString#String -->
     *
     * @param data The {@link String} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link String}.
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromBytes#byte -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * BinaryData binaryData = BinaryData.fromBytes&#40;data&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromByteBuffer#ByteBuffer -->
     * <pre>
     * final ByteBuffer data = ByteBuffer.wrap&#40;&quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * BinaryData binaryData = BinaryData.fromByteBuffer&#40;data&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromByteBuffer#ByteBuffer -->
     *
     * @param data The {@link ByteBuffer} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link ByteBuffer}.
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromListByteBuffer#List -->
     * <pre>
     * final List&lt;ByteBuffer&gt; data = Stream.of&#40;&quot;Some &quot;, &quot;data&quot;&#41;
     *     .map&#40;s -&gt; ByteBuffer.wrap&#40;s.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;&#41;
     *     .collect&#40;Collectors.toList&#40;&#41;&#41;;
     * BinaryData binaryData = BinaryData.fromListByteBuffer&#40;data&#41;;
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromListByteBuffer#List -->
     *
     * @param data The {@link List} of {@link ByteBuffer} that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the {@link List} of {@link ByteBuffer}.
     */
    public static BinaryData fromListByteBuffer(List<ByteBuffer> data) {
        return new BinaryData(new ListByteBufferContent(data));
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the default
     * {@link JsonSerializer}.
     *
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to serialize the object.
     * </p>
     * <p><strong>Creating an instance from an Object</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromObject#Object -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
     *
     * System.out.println&#40;binaryData&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromObject#Object -->
     *
     * @param data The object that will be JSON serialized that {@link BinaryData} will represent.
     * @return A {@link BinaryData} representing the JSON serialized object.
     * @throws NullPointerException If {@code data} is null.
     * @see JsonSerializer
     */
    public static BinaryData fromObject(Object data) {
        return fromObject(data, SERIALIZER);
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the default
     * {@link JsonSerializer}.
     *
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to serialize the object.
     * </p>
     * <p><strong>Creating an instance from an Object</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromObjectAsync#Object -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     * Disposable subscriber = BinaryData.fromObjectAsync&#40;data&#41;
     *     .subscribe&#40;binaryData -&gt; System.out.println&#40;binaryData.toString&#40;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromObjectAsync#Object -->
     *
     * @param data The object that will be JSON serialized that {@link BinaryData} will represent.
     * @return A {@link Mono} of {@link BinaryData} representing the JSON serialized object.
     * @see JsonSerializer
     */
    public static Mono<BinaryData> fromObjectAsync(Object data) {
        return fromObjectAsync(data, SERIALIZER);
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the passed
     * {@link ObjectSerializer}.
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     * </p>
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Create an instance from an Object</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromObject#Object-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;data, serializer&#41;;
     *
     * System.out.println&#40;binaryData.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromObject#Object-ObjectSerializer -->
     *
     * @param data The object that will be serialized that {@link BinaryData} will represent. The {@code serializer}
     * determines how {@code null} data is serialized.
     * @param serializer The {@link ObjectSerializer} used to serialize object.
     * @return A {@link BinaryData} representing the serialized object.
     * @throws NullPointerException If {@code serializer} is null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        return new BinaryData(new SerializableContent(data, serializer));
    }

    /**
     * Creates an instance of {@link BinaryData} by serializing the {@link Object} using the passed
     * {@link ObjectSerializer}.
     *
     * <p>
     * The passed {@link ObjectSerializer} can either be one of the implementations offered by the Azure SDKs or your
     * own implementation.
     * </p>
     *
     * <p><strong>Azure SDK implementations</strong></p>
     * <ul>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson" target="_blank">Jackson JSON serializer</a></li>
     * <li><a href="https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson" target="_blank">GSON JSON serializer</a></li>
     * </ul>
     *
     * <p><strong>Create an instance from an Object</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromObjectAsync#Object-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * Disposable subscriber = BinaryData.fromObjectAsync&#40;data, serializer&#41;
     *     .subscribe&#40;binaryData -&gt; System.out.println&#40;binaryData.toString&#40;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromObjectAsync#Object-ObjectSerializer -->
     *
     * @param data The object that will be serialized that {@link BinaryData} will represent. The {@code serializer}
     * determines how {@code null} data is serialized.
     * @param serializer The {@link ObjectSerializer} used to serialize object.
     * @return A {@link Mono} of {@link BinaryData} representing the serialized object.
     * @throws NullPointerException If {@code serializer} is null.
     * @see ObjectSerializer
     * @see JsonSerializer
     * @see <a href="https://aka.ms/azsdk/java/docs/serialization" target="_blank">More about serialization</a>
     */
    public static Mono<BinaryData> fromObjectAsync(Object data, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> fromObject(data, serializer));
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
     * <pre>
     * BinaryData binaryData = BinaryData.fromFile&#40;new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromFile -->
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromFile#Path-int -->
     * <pre>
     * BinaryData binaryData = BinaryData.fromFile&#40;new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;, 8092&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromFile#Path-int -->
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
     * <!-- src_embed com.azure.core.util.BinaryData.fromFile#Path-Long-Long -->
     * <pre>
     * long position = 1024;
     * long length = 100 * 1048;
     * BinaryData binaryData = BinaryData.fromFile&#40;
     *     new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;, position, length&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromFile#Path-Long-Long -->
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
        return new BinaryData(new FileContent(file, STREAM_READ_SIZE, position, length));
    }

    /**
     * Creates a {@link BinaryData} that uses the content of the file at {@link Path file} as its data. This method
     * checks for the existence of the file at the time of creating an instance of {@link BinaryData}. The file,
     * however, is not read until there is an attempt to read the contents of the returned BinaryData instance.
     *
     * <p><strong>Create an instance from a file</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.fromFile#Path-Long-Long-int -->
     * <pre>
     * long position = 1024;
     * long length = 100 * 1048;
     * int chunkSize = 8092;
     * BinaryData binaryData = BinaryData.fromFile&#40;
     *     new File&#40;&quot;path&#47;to&#47;file&quot;&#41;.toPath&#40;&#41;, position, length, chunkSize&#41;;
     * System.out.println&#40;new String&#40;binaryData.toBytes&#40;&#41;, StandardCharsets.UTF_8&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.fromFile#Path-Long-Long-int -->
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
     * @throws IllegalStateException If the {@link BinaryData} is larger than the maximum size allowed for a
     * {@link String}.
     */
    public String toString() {
        return content.toString();
    }

    /**
     * Returns an {@link Object} representation of this {@link BinaryData} by deserializing its data using the default
     * {@link JsonSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use
     * {@link #toObject(TypeReference)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObject#Class -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Ensure your classpath have the Serializer to serialize the object which implement implement
     * &#47;&#47; com.azure.core.util.serializer.JsonSerializer interface.
     * &#47;&#47; Or use Azure provided libraries for this.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
     *
     * Person person = binaryData.toObject&#40;Person.class&#41;;
     * System.out.println&#40;person.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObject#Class -->
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
     * {@link JsonSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use
     * {@link TypeReference#createInstance(Class)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObject#TypeReference -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Ensure your classpath have the Serializer to serialize the object which implement implement
     * &#47;&#47; com.azure.core.util.serializer.JsonSerializer interface.
     * &#47;&#47; Or use Azure provided libraries for this.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
     *
     * Person person = binaryData.toObject&#40;TypeReference.createInstance&#40;Person.class&#41;&#41;;
     * System.out.println&#40;person.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObject#TypeReference -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObject#TypeReference-generic -->
     * <pre>
     * final Person person1 = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     * final Person person2 = new Person&#40;&#41;.setName&#40;&quot;Jack&quot;&#41;;
     *
     * List&lt;Person&gt; personList = new ArrayList&lt;&gt;&#40;&#41;;
     * personList.add&#40;person1&#41;;
     * personList.add&#40;person2&#41;;
     *
     * &#47;&#47; Ensure your classpath have the Serializer to serialize the object which implement implement
     * &#47;&#47; com.azure.core.util.serializer.JsonSerializer interface.
     * &#47;&#47; Or use Azure provided libraries for this.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;personList&#41;;
     *
     * List&lt;Person&gt; persons = binaryData.toObject&#40;new TypeReference&lt;List&lt;Person&gt;&gt;&#40;&#41; &#123; &#125;&#41;;
     * persons.forEach&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObject#TypeReference-generic -->
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
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use
     * {@link #toObject(TypeReference, ObjectSerializer)}.
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
     * <!-- src_embed com.azure.core.util.BinaryData.toObject#Class-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;data, serializer&#41;;
     *
     * Person person = binaryData.toObject&#40;Person.class, serializer&#41;;
     * System.out.println&#40;&quot;Name : &quot; + person.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObject#Class-ObjectSerializer -->
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
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use
     * {@link TypeReference#createInstance(Class)}.
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
     * <!-- src_embed com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;data, serializer&#41;;
     *
     * Person person = binaryData.toObject&#40;TypeReference.createInstance&#40;Person.class&#41;, serializer&#41;;
     * System.out.println&#40;&quot;Name : &quot; + person.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic -->
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
     * &#47;&#47; Retains the type of the list when deserializing
     * List&lt;Person&gt; persons = binaryData.toObject&#40;new TypeReference&lt;List&lt;Person&gt;&gt;&#40;&#41; &#123; &#125;, serializer&#41;;
     * persons.forEach&#40;person -&gt; System.out.println&#40;&quot;Name : &quot; + person.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObject#TypeReference-ObjectSerializer-generic -->
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
     * {@link JsonSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use
     * {@link #toObject(TypeReference)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObjectAsync#Class -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Ensure your classpath have the Serializer to serialize the object which implement implement
     * &#47;&#47; com.azure.core.util.serializer.JsonSerializer interface.
     * &#47;&#47; Or use Azure provided libraries for this.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
     *
     * Disposable subscriber = binaryData.toObjectAsync&#40;Person.class&#41;
     *     .subscribe&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObjectAsync#Class -->
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
     * {@link JsonSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use
     * {@link TypeReference#createInstance(Class)}.
     * <p>
     * <b>Note:</b> This method first looks for a {@link JsonSerializerProvider} implementation on the classpath. If no
     * implementation is found, a default Jackson-based implementation will be used to deserialize the object.
     *
     * <p><strong>Get a non-generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObjectAsync#TypeReference -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Ensure your classpath have the Serializer to serialize the object which implement implement
     * &#47;&#47; com.azure.core.util.serializer.JsonSerializer interface.
     * &#47;&#47; Or use Azure provided libraries for this.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;data&#41;;
     *
     * Disposable subscriber = binaryData.toObjectAsync&#40;TypeReference.createInstance&#40;Person.class&#41;&#41;
     *     .subscribe&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObjectAsync#TypeReference -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObjectAsync#TypeReference-generic -->
     * <pre>
     * final Person person1 = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     * final Person person2 = new Person&#40;&#41;.setName&#40;&quot;Jack&quot;&#41;;
     *
     * List&lt;Person&gt; personList = new ArrayList&lt;&gt;&#40;&#41;;
     * personList.add&#40;person1&#41;;
     * personList.add&#40;person2&#41;;
     *
     * BinaryData binaryData = BinaryData.fromObject&#40;personList&#41;;
     *
     * Disposable subscriber = binaryData.toObjectAsync&#40;new TypeReference&lt;List&lt;Person&gt;&gt;&#40;&#41; &#123; &#125;&#41;
     *     .subscribe&#40;persons -&gt; persons.forEach&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObjectAsync#TypeReference-generic -->
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
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link Class}, should be a non-generic class, for generic classes use
     * {@link #toObject(TypeReference, ObjectSerializer)}.
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
     * <!-- src_embed com.azure.core.util.BinaryData.toObjectAsync#Class-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;data, serializer&#41;;
     *
     * Disposable subscriber = binaryData.toObjectAsync&#40;Person.class, serializer&#41;
     *     .subscribe&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObjectAsync#Class-ObjectSerializer -->
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
     * {@link ObjectSerializer}. Each time this method is called, the content is deserialized and a new instance of type
     * {@code T} is returned. So, calling this method repeatedly to convert the underlying data source into the same
     * type is not recommended.
     * <p>
     * The type, represented by {@link TypeReference}, can either be a generic or non-generic type. If the type is
     * generic create a sub-type of {@link TypeReference}, if the type is non-generic use
     * {@link TypeReference#createInstance(Class)}.
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
     * <!-- src_embed com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer -->
     * <pre>
     * final Person data = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;central.sonatype.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;data, serializer&#41;;
     *
     * Disposable subscriber = binaryData
     *     .toObjectAsync&#40;TypeReference.createInstance&#40;Person.class&#41;, serializer&#41;
     *     .subscribe&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer -->
     *
     * <p><strong>Get a generic Object from the BinaryData</strong></p>
     *
     * <!-- src_embed com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer-generic -->
     * <pre>
     * final Person person1 = new Person&#40;&#41;.setName&#40;&quot;John&quot;&#41;;
     * final Person person2 = new Person&#40;&#41;.setName&#40;&quot;Jack&quot;&#41;;
     *
     * List&lt;Person&gt; personList = new ArrayList&lt;&gt;&#40;&#41;;
     * personList.add&#40;person1&#41;;
     * personList.add&#40;person2&#41;;
     *
     * &#47;&#47; Provide your custom serializer or use Azure provided serializers.
     * &#47;&#47; https:&#47;&#47;mvnrepository.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-jackson or
     * &#47;&#47; https:&#47;&#47;mvnrepository.com&#47;artifact&#47;com.azure&#47;azure-core-serializer-json-gson
     *
     * final ObjectSerializer serializer = new MyJsonSerializer&#40;&#41;; &#47;&#47; Replace this with your Serializer
     * BinaryData binaryData = BinaryData.fromObject&#40;personList, serializer&#41;;
     *
     * Disposable subscriber = binaryData
     *     .toObjectAsync&#40;new TypeReference&lt;List&lt;Person&gt;&gt;&#40;&#41; &#123; &#125;, serializer&#41; &#47;&#47; retains the generic type information
     *     .subscribe&#40;persons -&gt; persons.forEach&#40;person -&gt; System.out.println&#40;person.getName&#40;&#41;&#41;&#41;&#41;;
     *
     * &#47;&#47; So that your program wait for above subscribe to complete.
     * TimeUnit.SECONDS.sleep&#40;5&#41;;
     * subscriber.dispose&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toObjectAsync#TypeReference-ObjectSerializer-generic -->
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
     * <!-- src_embed com.azure.core.util.BinaryData.toStream -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * BinaryData binaryData = BinaryData.fromStream&#40;new ByteArrayInputStream&#40;data&#41;, &#40;long&#41; data.length&#41;;
     * final byte[] bytes = new byte[data.length];
     * try &#40;InputStream inputStream = binaryData.toStream&#40;&#41;&#41; &#123;
     *     inputStream.read&#40;bytes, 0, data.length&#41;;
     *     System.out.println&#40;new String&#40;bytes&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.core.util.BinaryData.toStream -->
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
     * <!-- src_embed com.azure.util.BinaryData.toByteBuffer -->
     * <pre>
     * final byte[] data = &quot;Some Data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * BinaryData binaryData = BinaryData.fromBytes&#40;data&#41;;
     * final byte[] bytes = new byte[data.length];
     * binaryData.toByteBuffer&#40;&#41;.get&#40;bytes, 0, data.length&#41;;
     * System.out.println&#40;new String&#40;bytes&#41;&#41;;
     * </pre>
     * <!-- end com.azure.util.BinaryData.toByteBuffer -->
     *
     * @return A read-only {@link ByteBuffer} representing the {@link BinaryData}.
     */
    public ByteBuffer toByteBuffer() {
        return content.toByteBuffer();
    }

    /**
     * Returns the content of this {@link BinaryData} instance as a flux of {@link ByteBuffer ByteBuffers}. The content
     * is not read from the underlying data source until the {@link Flux} is subscribed to.
     *
     * @return the content of this {@link BinaryData} instance as a flux of {@link ByteBuffer ByteBuffers}.
     */
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return content.toFluxByteBuffer();
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

    /**
     * Returns a flag indicating whether the content can be repeatedly consumed using all accessors including
     * {@link #toStream()} and {@link #toFluxByteBuffer()}
     *
     * <p>
     * Replayability does not imply thread-safety. The caller must not use data accessors simultaneously regardless of
     * what this method returns.
     * </p>
     *
     * <!-- src_embed com.azure.util.BinaryData.replayability -->
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
     * <!-- end com.azure.util.BinaryData.replayability -->
     *
     * <!-- src_embed com.azure.util.BinaryData.replayabilityAsync -->
     * <pre>
     * Mono.fromCallable&#40;this::binaryDataProducer&#41;
     *     .flatMap&#40;binaryData -&gt; &#123;
     *         if &#40;binaryData.isReplayable&#40;&#41;&#41; &#123;
     *             return Mono.just&#40;binaryData&#41;;
     *         &#125; else  &#123;
     *             return binaryData.toReplayableBinaryDataAsync&#40;&#41;;
     *         &#125;
     *     &#125;&#41;
     *     .flatMap&#40;replayableBinaryData -&gt;
     *         fluxConsumer&#40;replayableBinaryData.toFluxByteBuffer&#40;&#41;&#41;
     *             .then&#40;fluxConsumer&#40;replayableBinaryData.toFluxByteBuffer&#40;&#41;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.util.BinaryData.replayabilityAsync -->
     *
     * @return a flag indicating whether the content can be repeatedly consumed using all accessors.
     */
    public boolean isReplayable() {
        return content.isReplayable();
    }

    /**
     * Converts the {@link BinaryData} into a {@link BinaryData} that is replayable, i.e. content can be consumed
     * repeatedly using all accessors including {@link #toStream()} and {@link #toFluxByteBuffer()}
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
     * <!-- src_embed com.azure.util.BinaryData.replayability -->
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
     * <!-- end com.azure.util.BinaryData.replayability -->
     *
     * @return Replayable {@link BinaryData}.
     */
    public BinaryData toReplayableBinaryData() {
        if (this.isReplayable()) {
            return this;
        } else {
            return new BinaryData(content.toReplayableContent());
        }
    }

    /**
     * Converts the {@link BinaryData} into a {@link BinaryData} that is replayable, i.e. content can be consumed
     * repeatedly using all accessors including {@link #toStream()} and {@link #toFluxByteBuffer()}
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
     * <!-- src_embed com.azure.util.BinaryData.replayabilityAsync -->
     * <pre>
     * Mono.fromCallable&#40;this::binaryDataProducer&#41;
     *     .flatMap&#40;binaryData -&gt; &#123;
     *         if &#40;binaryData.isReplayable&#40;&#41;&#41; &#123;
     *             return Mono.just&#40;binaryData&#41;;
     *         &#125; else  &#123;
     *             return binaryData.toReplayableBinaryDataAsync&#40;&#41;;
     *         &#125;
     *     &#125;&#41;
     *     .flatMap&#40;replayableBinaryData -&gt;
     *         fluxConsumer&#40;replayableBinaryData.toFluxByteBuffer&#40;&#41;&#41;
     *             .then&#40;fluxConsumer&#40;replayableBinaryData.toFluxByteBuffer&#40;&#41;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.util.BinaryData.replayabilityAsync -->
     *
     * @return A {@link Mono} of {@link BinaryData} representing the replayable {@link BinaryData}.
     */
    public Mono<BinaryData> toReplayableBinaryDataAsync() {
        if (isReplayable()) {
            return Mono.just(this);
        } else {
            return content.toReplayableContentAsync().map(BinaryData::new);
        }
    }
}
