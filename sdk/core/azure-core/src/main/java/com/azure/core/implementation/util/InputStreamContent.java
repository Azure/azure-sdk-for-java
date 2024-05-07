// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.io.IOUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A {@link BinaryDataContent} implementation which is backed by an {@link InputStream}.
 */
public final class InputStreamContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(InputStreamContent.class);
    private static final int INITIAL_BUFFER_CHUNK_SIZE = 8 * 1024;
    private static final int MAX_BUFFER_CHUNK_SIZE = 8 * 1024 * 1024;
    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;
    private final Supplier<InputStream> content;
    private final Long length;
    private final boolean isReplayable;
    private final List<ByteBuffer> bufferedContent;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<InputStreamContent, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(InputStreamContent.class, byte[].class, "bytes");

    /**
     * Creates an instance of {@link InputStreamContent}.
     *
     * @param inputStream The inputStream that is used as the content for this instance.
     * @param length The length of the content.
     * @throws NullPointerException if {@code content} is null.
     */
    public InputStreamContent(InputStream inputStream, Long length) {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");
        this.length = length;
        this.isReplayable = canMarkReset(inputStream, length);
        if (this.isReplayable) {
            inputStream.mark(length.intValue());
            this.content = () -> resettableContent(inputStream);
        } else {
            this.content = () -> inputStream;
        }
        this.bufferedContent = null;
    }

    private InputStreamContent(Supplier<InputStream> inputStreamSupplier, Long length,
        List<ByteBuffer> bufferedContent) {
        this.content = Objects.requireNonNull(inputStreamSupplier, "'inputStreamSupplier' cannot be null.");
        this.length = length;
        this.isReplayable = true;
        this.bufferedContent = bufferedContent;
    }

    @Override
    public Long getLength() {
        byte[] data = BYTES_UPDATER.get(this);
        if (data != null) {
            return (long) data.length;
        }
        return length;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return BYTES_UPDATER.updateAndGet(this, bytes -> bytes == null ? getBytes() : bytes);
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return this.content.get();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        if (bufferedContent != null) {
            return Flux.fromIterable(bufferedContent).map(ByteBuffer::asReadOnlyBuffer);
        } else {
            return FluxUtil.toFluxByteBuffer(this.content.get(), STREAM_READ_SIZE);
        }
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        writeTo(Channels.newChannel(outputStream));
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        InputStream inputStream = content.get();
        if (bufferedContent != null) {
            // InputStream has been buffered, access the buffered elements directly to reduce memory copying.
            for (ByteBuffer bb : bufferedContent) {
                ImplUtils.fullyWriteBuffer(bb.duplicate(), channel);
            }
        } else {
            // Otherwise use a generic write to.
            IOUtils.transfer(Channels.newChannel(inputStream), channel, length);
        }
    }

    @Override
    public Mono<Void> writeTo(AsynchronousByteChannel channel) {
        if (channel == null) {
            return monoError(LOGGER, new NullPointerException("'channel' cannot be null."));
        }

        return FluxUtil.writeToAsynchronousByteChannel(toFluxByteBuffer(), channel);
    }

    @Override
    public boolean isReplayable() {
        return isReplayable;
    }

    @Override
    public BinaryDataContent toReplayableContent() {
        if (isReplayable) {
            return this;
        }

        return readAndBuffer(this.content.get(), length);
    }

    @Override
    public Mono<BinaryDataContent> toReplayableContentAsync() {
        if (isReplayable) {
            return Mono.just(this);
        }

        InputStream inputStream = this.content.get();
        return Mono.just(inputStream)
            .publishOn(Schedulers.boundedElastic()) // reading stream can be blocking.
            .map(is -> readAndBuffer(is, length));
    }

    @Override
    public BinaryDataContentType getContentType() {
        return BinaryDataContentType.BINARY;
    }

    private static boolean canMarkReset(InputStream inputStream, Long length) {
        return length != null && length < MAX_ARRAY_LENGTH && inputStream.markSupported();
    }

    private static InputStream resettableContent(InputStream stream) {
        try {
            stream.reset();
            return stream;
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private static InputStreamContent readAndBuffer(InputStream inputStream, Long length) {
        try {
            Tuple2<Long, List<ByteBuffer>> streamRead = StreamUtil.readStreamToListOfByteBuffers(inputStream, length,
                INITIAL_BUFFER_CHUNK_SIZE, MAX_BUFFER_CHUNK_SIZE);
            long readLength = streamRead.getT1();
            List<ByteBuffer> byteBuffers = streamRead.getT2();

            // If the length was unknown or didn't match what was actually read use the length calculated during reading
            // of the stream.
            if (length == null || length != readLength) {
                return new InputStreamContent(() -> new IterableOfByteBuffersInputStream(byteBuffers), readLength,
                    byteBuffers);
            } else {
                return new InputStreamContent(() -> new IterableOfByteBuffersInputStream(byteBuffers), length,
                    byteBuffers);
            }
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private byte[] getBytes() {
        try {
            AccessibleByteArrayOutputStream dataOutputBuffer = (length == null || length < MAX_ARRAY_LENGTH)
                ? new AccessibleByteArrayOutputStream()
                : new AccessibleByteArrayOutputStream(length.intValue());
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            InputStream inputStream = this.content.get();
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            return dataOutputBuffer.toByteArray();
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }
}
