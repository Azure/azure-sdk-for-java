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
 * A {@link RequestContent} implementation which is backed by a {@code byte[]}.
 */
public final class ArrayContent implements RequestContent {
    private final ClientLogger logger = new ClientLogger(ArrayContent.class);

    private final byte[] content;
    private final int offset;
    private final int length;

    /**
     * Creates a new instance of {@link ArrayContent}.
     *
     * @param content The {@code byte[]} content.
     * @param offset The offset in the array to begin reading data.
     * @param length The length of the content.
     */
    public ArrayContent(byte[] content, int offset, int length) {
        this.content = content;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public void writeTo(RequestOutbound requestOutbound) {
        try {
            requestOutbound.getRequestChannel().write(ByteBuffer.wrap(content, offset, length));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return Flux.defer(() -> Flux.just(ByteBuffer.wrap(content, offset, length)));
    }

    @Override
    public Long getLength() {
        return (long) length;
    }
}
