// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import com.azure.core.util.RequestOutbound;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * A {@link RequestContent} implementation which is backed by a {@link ByteBuffer}.
 */
public final class ByteBufferContent implements RequestContent {
    private final ClientLogger logger = new ClientLogger(ByteBufferContent.class);

    private final ByteBuffer byteBuffer;

    /**
     * Creates a new instance of {@link ByteBufferContent}.
     *
     * @param byteBuffer The {@link ByteBuffer} content.
     */
    public ByteBufferContent(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public void writeTo(RequestOutbound requestOutbound) {
        try {
            requestOutbound.getRequestChannel().write(byteBuffer.duplicate());
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        // Duplicate the ByteBuffer so that each invocation of this method uses a fully readable ByteBuffer.
        return Flux.defer(() -> Flux.just(byteBuffer.duplicate()));
    }

    @Override
    public Long getLength() {
        return (long) byteBuffer.remaining();
    }
}
