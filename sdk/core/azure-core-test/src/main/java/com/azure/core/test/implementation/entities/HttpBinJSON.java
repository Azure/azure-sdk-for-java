// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON {
    @JsonProperty()
    private String url;

    @JsonProperty()
    private Map<String, List<String>> headers;

    @JsonProperty()
    private Object data;

    /**
     * Gets the URL associated with this request.
     *
     * @return he URL associated with the request.
     */
    public String url() {
        return url;
    }

    /**
     * Sets the URL associated with this request.
     *
     * @param url The URL associated with the request.
     */
    public void url(String url) {
        this.url = url;
    }

    /**
     * Gets the response headers.
     *
     * @return The response headers.
     */
    public Map<String, List<String>> headers() {
        return headers;
    }

    public String getHeaderValue(String name) {
        return headers == null ? null : headers.containsKey(name) ? headers.get(name).get(0) : null;
    }

    /**
     * Sets the response headers.
     *
     * @param headers The response headers.
     */
    public void headers(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * Gets the response body.
     *
     * @return The response body.
     */
    public Object data() {
        return data;
    }

    /**
     * Sets the response body.
     *
     * @param data The response body.
     */
    public void data(Object data) {
        this.data = data;
    }
}
