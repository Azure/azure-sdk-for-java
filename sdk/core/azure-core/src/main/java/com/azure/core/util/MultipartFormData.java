package com.azure.core.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class MultipartFormData {

    @JsonProperty(value = "content_type")
    private final String contentType;

    @JsonProperty(value = "request_body")
    private final byte[] requestBody;

    @JsonCreator
    public MultipartFormData(@JsonProperty(value = "content_type") String contentType,
        @JsonProperty(value = "request_body") byte[] requestBody) {
        this.contentType = contentType;
        this.requestBody = requestBody;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getRequestBody() {
        return requestBody;
    }

}
