/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.io.InputStream;

/**
 * This class contains all of the details necessary for reacting to a HTTP response from a
 * HttpResponse.
 */
public abstract class HttpResponse {
    /**
     * Get this response object's HTTP status code.
     * @return This response object's HTTP status code.
     */
    public abstract int statusCode();

    /**
     * Get the header value for the provided header name, or null if the provided header name does
     * not appear in this HttpResponse's headers.
     * @param headerName The name of the header to lookup.
     * @return The value of the header, or null if the header doesn't exist in this HttpResponse.
     */
    public abstract String headerValue(String headerName);

    /**
     * Get all the headers on this HTTP response.
     * @return All headers on this HTTP response.
     */
    public abstract HttpHeaders headers();

    /**
     * Get this response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     * @return This response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     */
    public abstract Single<? extends InputStream> bodyAsInputStreamAsync();

    /**
     * Get this response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<byte[]> bodyAsByteArrayAsync();

    /**
     * Stream this response's body content.
     * @return This response's body as an asynchronous sequence of byte[].
     */
    public abstract Observable<byte[]> streamBodyAsync();

    /**
     * Get this response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<String> bodyAsStringAsync();

    /**
     * Buffers the HTTP response body into memory, allowing the content to be inspected and replayed.
     * @return This HTTP response, with body content buffered into memory.
     */
    public HttpResponse buffer() {
        return new BufferedHttpResponse(this);
    }
}
