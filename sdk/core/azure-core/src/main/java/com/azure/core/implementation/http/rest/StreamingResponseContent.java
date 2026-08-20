// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.implementation.FluxInputStream;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataContentType;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Non-replayable response content that exposes both reactive and synchronous streaming access.
 */
final class StreamingResponseContent extends BinaryDataContent {
    private final FluxByteBufferContent content;
    private final RestProxyBase.ResponseBodyOwner responseBodyOwner;

    StreamingResponseContent(Flux<ByteBuffer> content, Long length, RestProxyBase.ResponseBodyOwner responseBodyOwner) {
        this.content = new FluxByteBufferContent(content, length, false);
        this.responseBodyOwner = responseBodyOwner;
    }

    @Override
    public Long getLength() {
        return content.getLength();
    }

    @Override
    public String toString() {
        return content.toString();
    }

    @Override
    public byte[] toBytes() {
        return content.toBytes();
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return content.toObject(typeReference, serializer);
    }

    @Override
    public InputStream toStream() {
        return new FilterInputStream(new FluxInputStream(content.toFluxByteBuffer())) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    responseBodyOwner.close();
                }
            }
        };
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return content.toByteBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return content.toFluxByteBuffer();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        content.writeTo(outputStream);
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        content.writeTo(channel);
    }

    @Override
    public Mono<Void> writeTo(AsynchronousByteChannel channel) {
        return content.writeTo(channel);
    }

    @Override
    public void writeTo(JsonWriter jsonWriter) throws IOException {
        content.writeTo(jsonWriter);
    }

    @Override
    public boolean isReplayable() {
        return false;
    }

    @Override
    public BinaryDataContent toReplayableContent() {
        return content.toReplayableContent();
    }

    @Override
    public Mono<BinaryDataContent> toReplayableContentAsync() {
        return content.toReplayableContentAsync();
    }

    @Override
    public BinaryDataContentType getContentType() {
        return content.getContentType();
    }
}
