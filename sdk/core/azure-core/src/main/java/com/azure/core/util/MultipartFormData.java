package com.azure.core.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.InputStream;

public final class MultipartFormData {

    @JsonProperty(value = "content_type")
    private final String contentType;

    @JsonProperty(value = "request_body")
    private final InputStream requestBody;

    @JsonProperty(value = "content_length")
    private final long contentLength;

    @JsonCreator
    public MultipartFormData(@JsonProperty(value = "content_type") String contentType,
        @JsonProperty(value = "request_body") InputStream requestBody,
        @JsonProperty(value = "content_length") long contentLength) {
        this.contentType = contentType;
        this.requestBody = requestBody;
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getRequestBody() {
        return requestBody;
    }

    public long getContentLength() {
        return contentLength;
    }
}
