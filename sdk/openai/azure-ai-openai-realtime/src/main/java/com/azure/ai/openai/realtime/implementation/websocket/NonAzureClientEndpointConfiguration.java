// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

public class NonAzureClientEndpointConfiguration extends ClientEndpointConfiguration {

    // Path
    private static final String PATH = "/v1/realtime";

    // Query parameters
    private final String model;

    // Headers
    private static final String OPENAI_BETA = "realtime=v1";

    public NonAzureClientEndpointConfiguration(String baseUrl, String userAgent, String model) {
        super(baseUrl, userAgent);
        this.model = model;
    }

    @Override
    public HttpHeaders getHeaders(AuthenticationProvider.AuthenticationHeader authenticationHeader) {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(authenticationHeader.getHeaderName(), authenticationHeader.getHeaderValue());
        headers.add("openai-beta", OPENAI_BETA);
        return headers;
    }

    @Override
    protected String getURIString() {
        return baseUrl + PATH + "?model=" + model;
    }
}
