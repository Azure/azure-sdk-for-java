package com.microsoft.rest.v2.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayHttpRequestBody implements HttpRequestBody {
    private final byte[] contents;

    public ByteArrayHttpRequestBody(byte[] contents) {
        this.contents = contents;
    }

    @Override
    public int contentLength() {
        return contents.length;
    }

    @Override
    public InputStream createInputStream() {
        return new ByteArrayInputStream(contents);
    }
}
