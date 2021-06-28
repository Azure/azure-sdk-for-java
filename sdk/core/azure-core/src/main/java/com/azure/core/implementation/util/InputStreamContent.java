// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.RequestContent;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * A {@link RequestContent} implementation which is backed by an {@link InputStream}.
 */
public class InputStreamContent extends RequestContent {
    private final InputStream content;
    private final Long length;
    private final int chunkSize;

    /**
     * Creates a new instance of {@link InputStreamContent}.
     *
     * @param content The {@link InputStream} content.
     * @param length The length of the content, may be null.
     * @param chunkSize The requested size for each {@link InputStream#read(byte[])}.
     */
    public InputStreamContent(InputStream content, Long length, int chunkSize) {
        this.content = content;
        this.length = length;
        this.chunkSize = chunkSize;
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return FluxUtil.toFluxByteBuffer(content, chunkSize);
    }

    @Override
    public Long getLength() {
        return length;
    }
}
