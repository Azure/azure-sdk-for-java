/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

/**
 * A HttpRequestBody that uses an InputStream as its content.
 */
public class InputStreamHttpRequestBody implements HttpRequestBody {
    private final int contentLength;
    private final String contentType;
    private final InputStream content;

    /**
     * Create a new InputStreamHttpRequest body.
     * @param contentLength The number of bytes in the content within the provided InputStream.
     * @param contentType The MIME type of the content.
     * @param content The InputStream content.
     */
    public InputStreamHttpRequestBody(int contentLength, String contentType, InputStream content) {
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public int contentLength() {
        return contentLength;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public InputStream createInputStream() {
        return content;
    }

    @Override
    public HttpRequestBody buffer() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(content);
        return new ByteArrayHttpRequestBody(bytes, contentType);
    }
}
