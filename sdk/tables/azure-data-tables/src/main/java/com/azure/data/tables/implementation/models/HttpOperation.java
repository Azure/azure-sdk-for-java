package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpRequest;

@Fluent
class HttpOperation {
    private final HttpRequest request;

    public HttpOperation(HttpRequest request) {
        this.request = request;
    }

    @Override
    public String toString() {
        return
            "Content-Type: application/http\n" +
            "Content-Transfer-Encoding: binary\n\n" +
            request.toString();
    }
}
