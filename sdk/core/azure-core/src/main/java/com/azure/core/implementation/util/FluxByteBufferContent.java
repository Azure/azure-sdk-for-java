// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * A {@link RequestContent} implementation which is backed by a {@link Flux} of {@link ByteBuffer}.
 */
public class FluxByteBufferContent implements RequestContent {
    private final Flux<ByteBuffer> content;
    private final Long length;

    /**
     * Creates a new instance of {@link FluxByteBufferContent}.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     */
    public FluxByteBufferContent(Flux<ByteBuffer> content) {
        this(content, null);
    }

    /**
     * Creates a new instance of {@link FluxByteBufferContent}.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     * @param length The length of the content, may be null.
     */
    public FluxByteBufferContent(Flux<ByteBuffer> content, Long length) {
        this.content = content;
        this.length = length;
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return content;
    }

    @Override
    public Long getLength() {
        return length;
    }
}
