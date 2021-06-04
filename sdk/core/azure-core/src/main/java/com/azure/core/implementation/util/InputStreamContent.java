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
public class InputStreamContent implements RequestContent {
    private final InputStream content;
    private final Long length;

    /**
     * Creates a new instance of {@link InputStreamContent}.
     *
     * @param content The {@link InputStream} content.
     */
    public InputStreamContent(InputStream content) {
        this(content, null);
    }

    /**
     * Creates a new instance of {@link InputStreamContent}.
     *
     * @param content The {@link InputStream} content.
     * @param length The length of the content, may be null.
     */
    public InputStreamContent(InputStream content, Long length) {
        this.content = content;
        this.length = length;
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return FluxUtil.toFluxByteBuffer(content);
    }

    @Override
    public Long getLength() {
        return length;
    }
}
