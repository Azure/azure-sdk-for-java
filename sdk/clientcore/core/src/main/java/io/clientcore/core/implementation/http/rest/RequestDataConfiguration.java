// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpRequest;

public class RequestDataConfiguration {
    private final HttpRequest httpRequest;
    private final SwaggerMethodParser methodParser;
    private final boolean isJson;
    private final Object bodyContent;

    public RequestDataConfiguration(HttpRequest httpRequest, SwaggerMethodParser swaggerMethodParser, boolean isJson,
        Object requestBodyContent) {

        this.httpRequest = httpRequest;
        this.methodParser = swaggerMethodParser;
        this.isJson = isJson;
        this.bodyContent = requestBodyContent;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public SwaggerMethodParser getMethodParser() {
        return methodParser;
    }

    public boolean isJson() {
        return isJson;
    }

    public Object getBodyContent() {
        return bodyContent;
    }
}
