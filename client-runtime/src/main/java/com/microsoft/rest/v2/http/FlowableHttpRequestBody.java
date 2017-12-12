package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;

import java.io.IOException;
import java.io.InputStream;

public final class FlowableHttpRequestBody implements HttpRequestBody {
    private final long contentLength;
    private final String contentType;
    private final Flowable<byte[]> content;

    /**
     * Create a new InputStreamHttpRequest body.
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

    /**
     * @return A Flowable which emits request content.
     */
    public Flowable<byte[]> content() {
        return content;
    }

    @Override
    public InputStream createInputStream() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public HttpRequestBody buffer() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
