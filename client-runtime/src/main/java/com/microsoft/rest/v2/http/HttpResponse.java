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
     * Get whether or not this response object has a body.
     * @return Whether or not this response object has a body.
     */
    public abstract boolean hasBody();

    /**
     * Get this response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     * @return This response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     */
    public InputStream getBodyAsInputStream() {
        return getBodyAsInputStreamAsync().toBlocking().value();
    }

    /**
     * Get this response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     * @return This response object's body as an InputStream. If this response object doesn't have a
     * body, then null will be returned.
     */
    public abstract Single<? extends InputStream> getBodyAsInputStreamAsync();

    /**
     * Get this response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @throws IOException On network error.
     */
    public byte[] getBodyAsByteArray() throws IOException {
        return getBodyAsByteArrayAsync().toBlocking().value();
    }

    /**
     * Get this response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<byte[]> getBodyAsByteArrayAsync();

    /**
     * Get this response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @throws IOException On network or serialization error.
     */
    public String getBodyAsString() throws IOException {
        return getBodyAsStringAsync().toBlocking().value();
    }

    /**
     * Get this response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<String> getBodyAsStringAsync();
}
