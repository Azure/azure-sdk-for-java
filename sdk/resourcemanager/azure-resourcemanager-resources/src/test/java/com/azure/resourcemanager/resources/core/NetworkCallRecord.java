// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class NetworkCallRecord {
    @JsonProperty("Method")
    private String method;
    @JsonProperty("Uri")
    private String uri;

    @JsonProperty("Headers")
    private Map<String, String> headers;
    @JsonProperty("Response")
    private Map<String, String> response;

    public String method() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String uri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> response() {
        return response;
    }

    public void setResponse(Map<String, String> response) {
        this.response = response;
    }
}
