package com.azure.ai.openai.implementation.websocket;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.UUID;

public class AzureClientEndpointConfiguration  extends ClientEndpointConfiguration {

    // Path
    private static final String PATH = "/openai/realtime";

    // Query parameters
    private final OpenAIServiceVersion serviceVersion;
    private final String deployment;

    // Headers
    private final String requestId = UUID.randomUUID().toString();
    private final KeyCredential keyCredential;
    private final TokenCredential tokenCredential;
    // userAgent is already defined in the parent class


    public AzureClientEndpointConfiguration(String baseUrl, String userAgent, String deployment, OpenAIServiceVersion serviceVersion, KeyCredential keyCredential) {
        super(baseUrl, userAgent);
        this.serviceVersion = serviceVersion;
        this.deployment = deployment;
        this.keyCredential = keyCredential;
        this.tokenCredential = null;
    }

    public AzureClientEndpointConfiguration(String baseUrl, String userAgent, String deployment, OpenAIServiceVersion serviceVersion, TokenCredential tokenCredential) {
        super(baseUrl, userAgent);
        this.serviceVersion = serviceVersion;
        this.deployment = deployment;
        this.tokenCredential = tokenCredential;
        this.keyCredential = null;
    }

    @Override
    public HttpHeaders getHeaders() {
        return new DefaultHttpHeaders()
            .add("x-ms-client-request-id", requestId)
            .add("api-key", keyCredential.getKey())
            .add(HttpHeaderName.USER_AGENT.getCaseInsensitiveName(), super.userAgent);
    }

    @Override
    protected String getURIString() {
        return baseUrl + PATH + "?deployment=" + deployment + "&version=" + serviceVersion.getVersion();
    }

}
