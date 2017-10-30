/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

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
}
