// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpRequest;

/**
 * Configuration for a REST request.
 */
@Immutable
public class RequestDataConfiguration {
    private final HttpRequest httpRequest;
    private final SwaggerMethodParser methodParser;
    private final boolean isJson;
    private final Object bodyContent;

    /**
     * Creates a new RequestDataConfiguration object.
     *
     * @param httpRequest The HTTP request.
     * @param swaggerMethodParser The Swagger method parser.
     * @param isJson Whether the request is JSON.
     * @param requestBodyContent The request body content.
     */
    public RequestDataConfiguration(HttpRequest httpRequest, SwaggerMethodParser swaggerMethodParser, boolean isJson,
        Object requestBodyContent) {

        this.httpRequest = httpRequest;
        this.methodParser = swaggerMethodParser;
        this.isJson = isJson;
        this.bodyContent = requestBodyContent;
    }

    /**
     * Gets the HTTP request.
     *
     * @return The HTTP request.
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Gets the Swagger method parser.
     *
     * @return The Swagger method parser.
     */
    public SwaggerMethodParser getMethodParser() {
        return methodParser;
    }

    /**
     * Gets whether the request is JSON.
     *
     * @return Whether the request is JSON.
     */
    public boolean isJson() {
        return isJson;
    }

    /**
     * Gets the request body content.
     *
     * @return The request body content.
     */
    public Object getBodyContent() {
        return bodyContent;
    }
}
