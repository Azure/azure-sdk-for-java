// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;

public final class BatchOperationResponse implements Response<Object> {
    private HttpRequest request;
    private int statusCode;
    private final HttpHeaders headers;
    private Object value;

    public BatchOperationResponse() {
        this.headers = new HttpHeaders();
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    public void putHeader(String name, String value) {
        this.headers.put(name, value);
    }

    @Override
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
