// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

public class FluxByteBufferContent implements RequestContent {
    private final Flux<ByteBuffer> content;
    private final Long length;

    public FluxByteBufferContent(Flux<ByteBuffer> content) {
        this(content, null);
    }

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
