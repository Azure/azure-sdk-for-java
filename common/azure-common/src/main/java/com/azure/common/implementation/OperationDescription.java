/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation;

import com.azure.common.http.HttpRequest;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

/**
 * Type that holds composes data from an originating operation
 * that can be used to resume the polling of the original operation.
 */
public class OperationDescription implements Serializable {
    private Serializable pollStrategyData;
    private Map<String, String> headers;
    private String httpMethod;
    private URL url;
    private String fullyQualifiedMethodName;

    /**
     * Create OperationDescription.
     */
    public OperationDescription() {
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
    public OperationDescription(String fullyQualifiedMethodName,
                                Serializable pollStrategyData,
                                HttpRequest originalHttpRequest) {
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.pollStrategyData = pollStrategyData;
        this.headers = originalHttpRequest.headers().toMap();
        this.url = originalHttpRequest.url();
        this.httpMethod = originalHttpRequest.httpMethod().toString();
    }

    /**
     * Get the Serializable poll strategy data.
     *
     * @return the Serializable poll strategy data
     */
    public Serializable pollStrategyData() {
        return this.pollStrategyData;
    }

    /**
     * Get the originating requests url.
     *
     * @return the originating requests url
     */
    public URL url() {
        return this.url;
    }

    /**
     * @return the originating requests http method.
     */
    public String httpMethod() {
        return this.httpMethod;
    }

    /**
     * Get the originating requests headers.
     *
     * @return the originating requests headers
     */
    public Map<String, String> headers() {
        return this.headers;
    }

    /**
     * Get the originating method name.
     *
     * @return the originating method name
     */
    String methodName() {
        int lastIndex = this.fullyQualifiedMethodName.lastIndexOf(".");
        return this.fullyQualifiedMethodName.substring(lastIndex + 1);
    }
}
