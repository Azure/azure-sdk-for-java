// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@link Flux} of {@link ByteBuffer}.
 */
public final class FluxByteBufferContent extends BinaryDataContent {

    private final Flux<ByteBuffer> content;
    private final AtomicReference<byte[]> bytes = new AtomicReference<>();
    private final Long length;
    private final boolean isReplayable;

    /**
     * Creates an instance of {@link FluxByteBufferContent}.
     * @param content The content for this instance.
     * @throws NullPointerException if {@code content} is null.
     */
    public FluxByteBufferContent(Flux<ByteBuffer> content) {
        this(content, null);
    }

    /**
     * Creates an instance of {@link FluxByteBufferContent}.
     * @param content The content for this instance.
     * @param length The length of the content in bytes.
     * @throws NullPointerException if {@code content} is null.
     */
    public FluxByteBufferContent(Flux<ByteBuffer> content, Long length) {
        // There's currently no way to tell if Flux is replayable or not.
        // https://github.com/reactor/reactor-core/issues/1977
        this(content, length, false);
    }

    private FluxByteBufferContent(Flux<ByteBuffer> content, Long length, boolean isReplayable) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
        this.length = length;
        this.isReplayable = isReplayable;
    }

    @Override
    public Long getLength() {
        if (bytes.get() != null) {
            return (long) bytes.get().length;
        }
        return length;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        byte[] data = this.bytes.get();
        if (data == null) {
            bytes.set(getBytes());
            data = this.bytes.get();
        }
        return data;
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return new ByteArrayInputStream(toBytes());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return content;
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

        Flux<ByteBuffer> bufferedFlux = content
            .map(buffer -> {
                // deep copy direct buffers
                ByteBuffer copy = ByteBuffer.allocate(buffer.remaining());
                copy.put(buffer);
                copy.flip();
                return copy;
            })
            // collectList() uses ArrayList. We don't want to be bound by array capacity
            // and we don't need random access.
            .collect(LinkedList::new, (BiConsumer<LinkedList<ByteBuffer>, ByteBuffer>) LinkedList::add)
            .cache()
            .flatMapMany(
                // Duplicate buffers on re-subscription.
                listOfBuffers -> Flux.fromIterable(listOfBuffers).map(ByteBuffer::duplicate));
        return new FluxByteBufferContent(bufferedFlux, length, true);
    }

    @Override
    public Mono<BinaryDataContent> toReplayableContentAsync() {
        return Mono.fromCallable(this::toReplayableContent);
    }

    private byte[] getBytes() {
        return FluxUtil.collectBytesInByteBufferStream(content)
                // this doesn't seem to be working (newBoundedElastic() didn't work either)
                // .publishOn(Schedulers.boundedElastic())
                .share()
                .block();
    }
}
