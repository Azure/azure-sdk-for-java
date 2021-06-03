// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * A {@link RequestContent} implementation which is backed by a {@link ByteBuffer}.
 */
public final class ByteBufferContent implements RequestContent {
    private final ByteBuffer byteBuffer;
    private final long length;

    /**
     * Creates a new instance of {@link ByteBufferContent}.
     *
     * @param byteBuffer The {@link ByteBuffer} content.
     */
    public ByteBufferContent(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.length = byteBuffer.remaining();
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        // Duplicate the ByteBuffer so that each invocation of this method uses a fully readable ByteBuffer.
        return Flux.defer(() -> Flux.just(byteBuffer.duplicate()));
    }

    @Override
    public Long getLength() {
        return length;
    }
}
