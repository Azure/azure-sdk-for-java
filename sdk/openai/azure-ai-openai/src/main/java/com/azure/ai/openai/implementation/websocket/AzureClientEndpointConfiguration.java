package com.azure.ai.openai.implementation.websocket;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpHeaderName;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.UUID;

public class AzureClientEndpointConfiguration  extends ClientEndpointConfiguration {

    // Path
    private final String path = "/openai/realtime";

    // Query parameters
    // TODO jpalvarezl expose as a constructor parameter
    private final OpenAIServiceVersion serviceVersion = OpenAIServiceVersion.V2024_10_01_PREVIEW;
    private final String deployment;

    // Headers
    private final String requestId = UUID.randomUUID().toString();
    private final KeyCredential keyCredential;
    // userAgent is already defined in the parent class


    public AzureClientEndpointConfiguration(String protocol, String userAgent, String deployment, KeyCredential keyCredential) {
        super(protocol, userAgent);
        this.deployment = deployment;
        this.keyCredential = keyCredential;
    }

    public HttpHeaders getHeaders() {
        return new DefaultHttpHeaders()
            .add("x-ms-client-request-id", requestId)
            .add("api-key", keyCredential.getKey())
            .add(HttpHeaderName.USER_AGENT.getCaseInsensitiveName(), super.getUserAgent());
    }
}
