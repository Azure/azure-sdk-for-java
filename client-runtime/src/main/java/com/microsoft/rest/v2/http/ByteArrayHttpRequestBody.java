/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A HTTP request body that contains a byte[].
 */
public class ByteArrayHttpRequestBody implements HttpRequestBody {
    private final byte[] contents;
    private final String contentType;

    /**
     * Create a new ByteArrayHttpRequestBody with the provided byte[].
     * @param contents The byte[] to store in this ByteArrayHttpRequestBody.
     * @param contentType The MIME Content-Type of this request body.
     */
    public ByteArrayHttpRequestBody(byte[] contents, String contentType) {
        this.contents = contents;
        this.contentType = contentType;
    }

    @Override
    public int contentLength() {
        return contents.length;
    }

    /**
     * @return the content of the request, in the format of a byte array.
     */
    public byte[] content() {
        return contents;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public InputStream createInputStream() {
        return new ByteArrayInputStream(contents);
    }
}
