// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON {
    @JsonProperty()
    private String url;

    @JsonProperty()
    private Map<String, String> headers;

    @JsonProperty()
    private Object data;

    public String url() {
        return url;
    }

    public void url(String url) {
        this.url = url;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void headers(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object data() {
        return data;
    }

    public void data(Object data) {
        this.data = data;
    }
}
