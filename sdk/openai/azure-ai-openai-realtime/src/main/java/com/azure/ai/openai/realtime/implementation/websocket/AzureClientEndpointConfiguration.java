// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.ai.openai.realtime.OpenAIRealtimeServiceVersion;
import com.azure.core.http.HttpHeaderName;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.UUID;

public class AzureClientEndpointConfiguration extends ClientEndpointConfiguration {

    // Path
    private static final String PATH = "/openai/realtime";

    // Query parameters
    private final OpenAIRealtimeServiceVersion serviceVersion;
    private final String deployment;

    // Headers
    private final String requestId = UUID.randomUUID().toString();
    // userAgent is already defined in the parent class

    public AzureClientEndpointConfiguration(String baseUrl, String userAgent, String deployment,
        OpenAIRealtimeServiceVersion serviceVersion) {
        super(baseUrl, userAgent);
        this.serviceVersion = serviceVersion;
        this.deployment = deployment;
    }

    @Override
    public HttpHeaders getHeaders(AuthenticationProvider.AuthenticationHeader authenticationHeader) {
        return new DefaultHttpHeaders().add("x-ms-client-request-id", requestId)
            .add(authenticationHeader.getHeaderName(), authenticationHeader.getHeaderValue())
            .add(HttpHeaderName.USER_AGENT.getCaseInsensitiveName(), super.userAgent);
    }

    @Override
    protected String getURIString() {
        return baseUrl + PATH + "?deployment=" + deployment + "&api-version=" + serviceVersion.getVersion();
    }

}
