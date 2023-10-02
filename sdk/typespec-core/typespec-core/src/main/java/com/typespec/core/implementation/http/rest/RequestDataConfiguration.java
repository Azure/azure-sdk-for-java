// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.http.HttpRequest;

public class RequestDataConfiguration {
    private HttpRequest httpRequest;
    private SwaggerMethodParser methodParser;
    private boolean isJson;
    private Object bodyContent;

    public RequestDataConfiguration(HttpRequest httpRequest, SwaggerMethodParser swaggerMethodParser,
                                    boolean isJson, Object requestBodyContent) {

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
