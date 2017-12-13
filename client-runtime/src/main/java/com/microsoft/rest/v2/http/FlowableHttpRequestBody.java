/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;

import java.io.IOException;

/**
 * An HTTP request body which is given by subscribing to a Flowable.
 */
public final class FlowableHttpRequestBody implements HttpRequestBody {
    private final long contentLength;
    private final String contentType;

    private boolean isBuffered = false;
    private Flowable<byte[]> content;

    /**
     * Create a new FlowableHttpRequestBody.
     * @param contentLength The number of bytes in the content within the provided InputStream.
     * @param contentType The MIME type of the content.
     * @param content The InputStream content.
     */
    public FlowableHttpRequestBody(long contentLength, String contentType, Flowable<byte[]> content) {
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public Flowable<byte[]> content() {
        return content;
    }

    @Override
    public HttpRequestBody buffer() throws IOException {
        if (!isBuffered) {
            content = content.replay().autoConnect();
            isBuffered = true;
        }
        return this;
    }
}
