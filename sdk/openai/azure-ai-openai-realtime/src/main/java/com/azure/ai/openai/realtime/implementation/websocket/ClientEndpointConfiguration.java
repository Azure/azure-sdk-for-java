// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.ai.openai.realtime.OpenAIRealtimeServiceVersion;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.URI;

public abstract class ClientEndpointConfiguration {

    protected final String baseUrl;

    protected final String userAgent;

    protected final String subProtocol = "realtime";

    private static final MessageEncoder MESSAGE_ENCODER = new MessageEncoder();

    private static final MessageDecoder MESSAGE_DECODER = new MessageDecoder();

    public ClientEndpointConfiguration(String baseUrl, String userAgent) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
    }

    public URI getUri() {
        return URI.create(getURIString());
    }

    public abstract HttpHeaders getHeaders(AuthenticationProvider.AuthenticationHeader authenticationHeader);

    public MessageEncoder getMessageEncoder() {
        return MESSAGE_ENCODER;
    }

    public MessageDecoder getMessageDecoder() {
        return MESSAGE_DECODER;
    }

    public String getSubProtocol() {
        return subProtocol;
    }

    protected abstract String getURIString();

    public static AzureClientEndpointConfiguration createAzureClientEndpointConfiguration(String baseUrl,
        String userAgent, String deployment, OpenAIRealtimeServiceVersion serviceVersion) {
        return new AzureClientEndpointConfiguration(baseUrl, userAgent, deployment, serviceVersion);
    }

    public static NonAzureClientEndpointConfiguration createNonAzureClientEndpointConfiguration(String baseUrl,
        String userAgent, String model) {
        return new NonAzureClientEndpointConfiguration(baseUrl, userAgent, model);
    }
}
