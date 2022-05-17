// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.util.BinaryDataContent;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Objects;

/**
 * Base class for streamable request bodies.
 * @param <T> BinaryDataContent.
 */
public abstract class OkHttpStreamableRequestBody<T extends BinaryDataContent> extends RequestBody {
    protected final T content;
    /**
     * Content length or -1 if unspecified (i.e. chunked encoding)
     */
    protected final long effectiveContentLength;
    private final MediaType mediaType;

    public OkHttpStreamableRequestBody(T content, HttpHeaders httpHeaders, MediaType mediaType) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
        this.effectiveContentLength = getRequestContentLength(content,
            Objects.requireNonNull(httpHeaders, "'httpHeaders' cannot be null."));
        this.mediaType = mediaType;
    }

    private static long getRequestContentLength(BinaryDataContent content, HttpHeaders headers) {
        Long contentLength = content.getLength();
        if (contentLength == null) {
            String contentLengthHeaderValue = headers.getValue("Content-Length");
            if (contentLengthHeaderValue != null) {
                contentLength = Long.parseLong(contentLengthHeaderValue);
            } else {
                // -1 means that content length is unknown.
                contentLength = -1L;
            }
        }
        return contentLength;
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
