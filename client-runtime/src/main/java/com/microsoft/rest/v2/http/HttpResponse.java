/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;
import io.reactivex.Single;

import java.io.Closeable;

/**
 * This class contains all of the details necessary for reacting to a HTTP response from a
 * HttpResponse.
 */
public abstract class HttpResponse implements Closeable {
    private Object deserializedHeaders;
    private Object deserializedBody;

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
     * Stream this response's body content.
     * @return This response's body as an asynchronous sequence of byte[].
     */
    public abstract Flowable<byte[]> streamBodyAsync();

    /**
     * Get this response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<byte[]> bodyAsByteArrayAsync();

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
    public BufferedHttpResponse buffer() {
        return new BufferedHttpResponse(this);
    }

    /**
     * Closes the stream providing this HttpResponse's content, if any.
     */
    public void close() {
        // no-op
    }

    /**
     * @return the deserialized headers, if present. Otherwise, null.
     */
    public Object deserializedHeaders() {
        return deserializedHeaders;
    }

    /**
     * Set the deserialized headers on this HttpResponse.
     * @param deserializedHeaders the deserialized headers
     * @return this HTTP repsonse
     */
    public HttpResponse withDeserializedHeaders(Object deserializedHeaders) {
        this.deserializedHeaders = deserializedHeaders;
        return this;
    }

    /**
     * @return the deserialized body, if present. Otherwise, null.
     */
    public Object deserializedBody() {
        return deserializedBody;
    }

    /**
     * Sets the deserialized body on this HttpResponse.
     * @param deserializedBody the deserialized body
     * @return this HTTP response
     */
    public HttpResponse withDeserializedBody(Object deserializedBody) {
        this.deserializedBody = deserializedBody;
        return this;
    }
}
