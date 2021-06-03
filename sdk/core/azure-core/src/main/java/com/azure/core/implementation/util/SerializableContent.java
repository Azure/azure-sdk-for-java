// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import com.azure.core.util.serializer.ObjectSerializer;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * A {@link RequestContent} implementation which is backed by a serializable object.
 */
public final class SerializableContent implements RequestContent {
    private final Object serializable;
    private final ObjectSerializer objectSerializer;

    /**
     * Creates a new instance of {@link SerializableContent}.
     *
     * @param serializable The serializable {@link Object} content.
     * @param objectSerializer The {@link ObjectSerializer} that will serialize the {@link Object} content.
     */
    public SerializableContent(Object serializable, ObjectSerializer objectSerializer) {
        this.serializable = serializable;
        this.objectSerializer = objectSerializer;
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return Flux.defer(() -> objectSerializer.serializeToBytesAsync(serializable).map(ByteBuffer::wrap));
    }

    @Override
    public Long getLength() {
        return null;
    }
}
