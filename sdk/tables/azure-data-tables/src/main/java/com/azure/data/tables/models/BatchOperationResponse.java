// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;

/**
 * The response of a REST sub-request contained within the response to a Batch request.
 */
public final class BatchOperationResponse implements Response<Object> {
    private HttpRequest request;
    private int statusCode;
    private final HttpHeaders headers;
    private Object value;

    /**
     * Creates a new empty {@link BatchOperationResponse}
     */
    public BatchOperationResponse() {
        this.headers = new HttpHeaders();
    }

    /**
     * Gets the sub-request which resulted in this {@link BatchOperationResponse}.
     *
     * @return The sub-request which resulted in this {@link BatchOperationResponse}.
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Sets the sub-request which resulted in this {@link BatchOperationResponse}.
     */
    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    /**
     * Gets the HTTP sub-response status code.
     *
     * @return The status code of the HTTP sub-response.
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the HTTP sub-response status code.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets the headers from the HTTP sub-response.
     *
     * @return The HTTP sub-response headers.
     */
    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets a single header value of the HTTP sub-response.
     */
    public void putHeader(String name, String value) {
        this.headers.put(name, value);
    }

    /**
     * Gets the deserialized value of the HTTP sub-response, if present.
     *
     * @return The deserialized value of the HTTP sub-response, if present.
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Sets the deserialized value of the HTTP sub-response.
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
