/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * The body of an HTTP request.
 */
public interface HttpRequestBody {
    /**
     * The length of this request body in bytes.
     * @return The length of this request body in bytes.
     */
    int contentLength();

    /**
     * @return the MIME Content-Type of this request body.
     */
    String contentType();

    /**
     * Create an InputStream that contains the contents of this request body.
     * @return An InputStream that contains the contents of this request body.
     */
    InputStream createInputStream();

    /**
     * Get a buffered version of this HttpRequestBody. If this HttpRequestBody
     * can only be read once, then calling this method will consume this
     * HttpRequestBody and the resulting object should be used instead.
     * @return A buffered version of this HttpRequestBody.
     * @throws IOException if there is a problem buffering.
     */
    HttpRequestBody buffer() throws IOException;
}
