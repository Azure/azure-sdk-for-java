// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.implementation;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BinaryDataContent} implementation which is backed by a serializable object.
 */
public class SerializableContent extends BinaryDataContent {

    private final Object content;
    private final ObjectSerializer serializer;

    private final AtomicReference<byte[]> bytes = new AtomicReference<>();

    /**
     * Creates a new instance of {@link SerializableContent}.
     * @param content The serializable object that forms the content of this instance.
     * @param serializer The serializer that serializes the {@code content}.
     */
    public SerializableContent(Object content, ObjectSerializer serializer) {
        this.content = content;
        if (content == null) {
            bytes.set(ZERO_BYTE_ARRAY);
            this.serializer = serializer;
            return;
        }
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        this.serializer = serializer;
    }

    @Override
    public Long getLength() {
        return null;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        byte[] retVal = this.bytes.get();
        if (retVal == null) {
            bytes.set(getBytes());
            retVal = this.bytes.get();
        }
        return retVal;
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        if (content == null) {
            return null;
        }
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return null;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Flux.defer(() -> Flux.just(ByteBuffer.wrap(toBytes())));
    }

    private byte[] getBytes() {
        // if (content == ZERO_BYTE_ARRAY) {
        //     return ZERO_BYTE_ARRAY;
        // }
        return serializer.serializeToBytes(content);
    }


}
