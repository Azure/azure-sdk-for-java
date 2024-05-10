// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3.implementation;

import io.clientcore.core.util.binarydata.BinaryData;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Objects;

/**
 * Base class for streamable request bodies.
 *
 * @param <T> BinaryData.
 */
public abstract class OkHttpStreamableRequestBody<T extends BinaryData> extends RequestBody {
    protected final T content;

    /**
     * Content length or -1 if unspecified (i.e. chunked encoding)
     */
    protected final long effectiveContentLength;
    private final MediaType mediaType;

    public OkHttpStreamableRequestBody(T content, long effectiveContentLength, MediaType mediaType) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
        this.effectiveContentLength = effectiveContentLength;
        this.mediaType = mediaType;
    }

    @Override
    public boolean isOneShot() {
        return true;
    }

    @Override
    public final MediaType contentType() {
        return mediaType;
    }

    @Override
    public final long contentLength() {
        return effectiveContentLength;
    }
}
