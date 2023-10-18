// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@code byte[]}.
 */
public final class ByteArrayContent extends BinaryDataContent {
    private final byte[] content;

    /**
     * Creates a new instance of {@link ByteArrayContent}.
     *
     * @param content The byte array content.
     * @throws NullPointerException if {@code content} is null.
     */
    public ByteArrayContent(byte[] content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null");
    }

    @Override
    public Long getLength() {
        return (long) this.content.length;
    }

    @Override
    public String toString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return content;
    }

    @Override
    public InputStream toStream() {
        return null;
    }

    @Override
    public BinaryDataContentType getContentType() {
        return BinaryDataContentType.BINARY;
    }
}
