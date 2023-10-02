// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Keeps track of network call records from each unit test session.
 */
public class NetworkCallRecord {
    @JsonProperty("Method")
    private String method;

    @JsonProperty("Uri")
    private String uri;

    @JsonProperty("Headers")
    private Map<String, String> headers;

    @JsonProperty("Response")
    private Map<String, String> response;

    @JsonProperty("Exception")
    private NetworkCallError exception;

    /**
     * Gets the HTTP method for with this network call
     *
     * @return The HTTP method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the HTTP method for with this network call
     *
     * @param method HTTP method for this network call.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the URL for this network call.
     *
     * @return The URL for this network call.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URL for this network call.
     *
     * @param uri The URL for this network call.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the HTTP headers for the network call.
     *
     * @return The HTTP headers for the network call.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets the HTTP headers for the network call.
     *
     * @param headers The HTTP headers for the network call.
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Gets the contents of the HTTP response as a map of its HTTP headers and response body. The HTTP response body is
     * mapped under key "Body".
     *
     * @return Contents of the HTTP response.
     */
    public Map<String, String> getResponse() {
        return response;
    }

    /**
     * Sets the contents of the HTTP response as a map of its HTTP headers and response body. The HTTP response body is
     * mapped under key "body".
     *
     * @param response Contents of the HTTP response.
     */
    public void setResponse(Map<String, String> response) {
        this.response = response;
    }

    /**
     * Gets the throwable thrown during evaluation of the network call.
     *
     * @return Throwable thrown during the network call.
     */
    public NetworkCallError getException() {
        return exception;
    }

    /**
     * Sets the throwable thrown during evaluation of the network call.
     *
     * @param exception Throwable thrown during the network call.
     */
    public void setException(NetworkCallError exception) {
        this.exception = exception;
    }
}
