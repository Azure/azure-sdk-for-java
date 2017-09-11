/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import rx.Single;

import java.io.IOException;
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
     * Get this response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     * @return This response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     */
    public InputStream bodyAsInputStream() {
        return bodyAsInputStreamAsync().toBlocking().value();
    }

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
     * @throws IOException On network error.
     */
    public byte[] bodyAsByteArray() throws IOException {
        return bodyAsByteArrayAsync().toBlocking().value();
    }

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
     * @throws IOException On network or serialization error.
     */
    public String bodyAsString() throws IOException {
        return bodyAsStringAsync().toBlocking().value();
    }

    /**
     * Get this response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<String> bodyAsStringAsync();
}
