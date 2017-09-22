/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A HTTP request body that contains a byte[].
 */
public class ByteArrayHttpRequestBody implements HttpRequestBody {
    private final byte[] contents;

    /**
     * Create a new ByteArrayHttpRequestBody with the provided byte[].
     * @param contents The byte[] to store in this ByteArrayHttpRequestBody.
     */
    public ByteArrayHttpRequestBody(byte[] contents) {
        this.contents = contents;
    }

    @Override
    public int contentLength() {
        return contents.length;
    }

    @Override
    public InputStream createInputStream() {
        return new ByteArrayInputStream(contents);
    }
}
