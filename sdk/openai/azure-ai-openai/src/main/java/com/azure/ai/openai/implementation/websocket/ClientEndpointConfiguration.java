package com.azure.ai.openai.implementation.websocket;

public final class ClientEndpointConfiguration {

    private final String protocol;

    private final String userAgent;

    private static final MessageEncoder MESSAGE_ENCODER = new MessageEncoder();

    private static final MessageDecoder MESSAGE_DECODER = new MessageDecoder();

    public ClientEndpointConfiguration(String protocol, String userAgent) {
        this.protocol = protocol;
        this.userAgent = userAgent;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public MessageEncoder getMessageEncoder() {
        return MESSAGE_ENCODER;
    }

    public MessageDecoder getMessageDecoder() {
        return MESSAGE_DECODER;
    }
}
