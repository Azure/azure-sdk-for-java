package com.azure.ai.openai.implementation.websocket;


import io.netty.handler.codec.http.HttpHeaders;

import java.net.URI;

public abstract class ClientEndpointConfiguration {

    protected final String baseUrl;

    protected final String userAgent;

    private static final MessageEncoder MESSAGE_ENCODER = new MessageEncoder();

    private static final MessageDecoder MESSAGE_DECODER = new MessageDecoder();

    public ClientEndpointConfiguration(String baseUrl, String userAgent) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
    }

    public URI getUri() {
        return URI.create(getURIString());
    }

    public abstract HttpHeaders getHeaders();

    public MessageEncoder getMessageEncoder() {
        return MESSAGE_ENCODER;
    }

    public MessageDecoder getMessageDecoder() {
        return MESSAGE_DECODER;
    }

    protected abstract String getURIString();
}
