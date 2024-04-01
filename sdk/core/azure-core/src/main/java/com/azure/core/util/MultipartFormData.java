package com.azure.core.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class MultipartFormData {

    @JsonProperty(value = "content_type")
    private final String contentType;

    @JsonProperty(value = "request_body")
    private final BinaryData requestBody;

    @JsonCreator
    public MultipartFormData(@JsonProperty(value = "content_type") String contentType,
        @JsonProperty(value = "request_body") BinaryData requestBody) {
        this.contentType = contentType;
        this.requestBody = requestBody;
    }

    public String getContentType() {
        return contentType;
    }

    public BinaryData getRequestBody() {
        return requestBody;
    }

}
