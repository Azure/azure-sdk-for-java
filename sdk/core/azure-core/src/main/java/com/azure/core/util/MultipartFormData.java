// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.InputStream;

/**
 * Represents a multipart form data.
 */
public final class MultipartFormData {

    @JsonProperty(value = "content_type")
    private final String contentType;

    @JsonProperty(value = "request_body")
    private final InputStream requestBody;

    @JsonProperty(value = "content_length")
    private final long contentLength;

    /**
     * Creates a new instance of MultipartFormData.
     *
     * @param contentType The content type of the request.
     * @param requestBody The request body.
     * @param contentLength The content length of the request.
     */
    @JsonCreator
    public MultipartFormData(@JsonProperty(value = "content_type") String contentType,
        @JsonProperty(value = "request_body") InputStream requestBody,
        @JsonProperty(value = "content_length") long contentLength) {
        this.contentType = contentType;
        this.requestBody = requestBody;
        this.contentLength = contentLength;
    }

    /**
     * Get the content type of the request.
     *
     * @return The content type of the request.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the request body.
     *
     * @return The request body.
     */
    public InputStream getRequestBody() {
        return requestBody;
    }

    /**
     * Get the content length of the request.
     *
     * @return The content length of the request.
     */
    public long getContentLength() {
        return contentLength;
    }
}
