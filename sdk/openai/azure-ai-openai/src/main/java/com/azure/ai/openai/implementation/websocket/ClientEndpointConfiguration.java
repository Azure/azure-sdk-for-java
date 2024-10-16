package com.azure.ai.openai.implementation.websocket;


import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
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

    public abstract HttpHeaders getHeaders();

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

    public static AzureClientEndpointConfiguration createAzureClientEndpointConfiguration(String baseUrl, String userAgent, String deployment, OpenAIServiceVersion serviceVersion, KeyCredential keyCredential) {
        return new AzureClientEndpointConfiguration(baseUrl, userAgent, deployment, serviceVersion, keyCredential);
    }

    public static AzureClientEndpointConfiguration createAzureClientEndpointConfiguration(String baseUrl, String userAgent, String deployment, OpenAIServiceVersion serviceVersion, TokenCredential tokenCredential) {
        return new AzureClientEndpointConfiguration(baseUrl, userAgent, deployment, serviceVersion, tokenCredential);
    }

    public static NonAzureClientEndpointConfiguration createNonAzureClientEndpointConfiguration(String baseUrl, String userAgent, String model, KeyCredential keyCredential) {
        return new NonAzureClientEndpointConfiguration(baseUrl, userAgent, model, keyCredential);
    }
}
