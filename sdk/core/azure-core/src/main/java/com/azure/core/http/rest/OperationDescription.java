// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpRequest;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

/**
 * Type that holds composes data from an originating operation
 * that can be used to resume the polling of the original operation.
 */
class OperationDescription implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Serializable pollStrategyData;
    private final Map<String, String> headers;
    private final String httpMethod;
    private final URL url;
    private final String fullyQualifiedMethodName;

    /**
     * Create OperationDescription.
     */
    OperationDescription() {
        this.fullyQualifiedMethodName = null;
        this.pollStrategyData = null;
        this.headers = null;
        this.url = null;
        this.httpMethod = null;
    }

    /**
     * Create a new Substitution.
     *
     * @param fullyQualifiedMethodName the fully qualified method name from the originating call
     * @param pollStrategyData the data for the originating methods polling strategy
     * @param originalHttpRequest the initial http request from the originating call
     */
    OperationDescription(String fullyQualifiedMethodName,
                                Serializable pollStrategyData,
                                HttpRequest originalHttpRequest) {
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.pollStrategyData = pollStrategyData;
        this.headers = originalHttpRequest.getHeaders().toMap();
        this.url = originalHttpRequest.getUrl();
        this.httpMethod = originalHttpRequest.getHttpMethod().toString();
    }

    /**
     * Get the Serializable poll strategy data.
     *
     * @return the Serializable poll strategy data
     */
    public Serializable getPollStrategyData() {
        return this.pollStrategyData;
    }

    /**
     * Get the originating requests url.
     *
     * @return the originating requests url
     */
    public URL getUrl() {
        return this.url;
    }

    /**
     * @return the originating requests http method.
     */
    public String getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * Get the originating requests headers.
     *
     * @return the originating requests headers
     */
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Get the originating method name.
     *
     * @return the originating method name
     */
    String getMethodName() {
        int lastIndex = this.fullyQualifiedMethodName.lastIndexOf(".");
        return this.fullyQualifiedMethodName.substring(lastIndex + 1);
    }
}
