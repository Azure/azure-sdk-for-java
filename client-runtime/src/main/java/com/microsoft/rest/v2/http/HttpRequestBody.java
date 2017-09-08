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
     * Create an InputStream that contains the contents of this request body.
     * @return An InputStream that contains the contents of this request body.
     */
    InputStream createInputStream();
}
