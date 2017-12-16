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

    private boolean isReplayable;
    private Flowable<byte[]> content;

    /**
     * Create a new FlowableHttpRequestBody.
     * @param contentLength the number of bytes in the content emitted by the provided Flowable
     * @param contentType the MIME type of the content
     * @param content the Flowable content
     * @param isReplayable indicates whether the content Flowable allows multiple subscription
     */
    public FlowableHttpRequestBody(long contentLength, String contentType, Flowable<byte[]> content, boolean isReplayable) {
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.content = content;
        this.isReplayable = isReplayable;
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
        if (!isReplayable) {
            content = content.replay().autoConnect();
            isReplayable = true;
        }
        return this;
    }
}
