// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.data.tables.implementation.ModelHelper;

/**
 * The response of a REST sub-request contained within the response to a Batch request.
 */
public final class BatchOperationResponse implements Response<Object> {
    private HttpRequest request;
    private final int statusCode;
    private final HttpHeaders headers;
    private final Object value;

    static {
        ModelHelper.setBatchOperationResponseCreator(BatchOperationResponse::new);
        ModelHelper.setBatchOperationResponseUpdater(BatchOperationResponse::update);
    }

    /**
     * Creates a new empty {@link BatchOperationResponse}
     */
    private BatchOperationResponse(int statusCode, Object value) {
        this.headers = new HttpHeaders();
        this.statusCode = statusCode;
        this.value = value;
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
     * Gets the HTTP sub-response status code.
     *
     * @return The status code of the HTTP sub-response.
     */
    @Override
    public int getStatusCode() {
        return statusCode;
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
     * Gets the deserialized value of the HTTP sub-response, if present.
     *
     * @return The deserialized value of the HTTP sub-response, if present.
     */
    @Override
    public Object getValue() {
        return value;
    }

    private static void update(BatchOperationResponse subject, HttpRequest request) {
        subject.request = request;
    }
}
