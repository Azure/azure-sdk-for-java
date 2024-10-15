package com.azure.ai.openai.implementation.websocket;

import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpHeaderName;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

public class NonAzureClientEndpointConfiguration extends ClientEndpointConfiguration {

    // Path
    private final String path = "/v1/realtime";

    // Query parameters
    private final String model;

    // Headers
    private final KeyCredential keyCredential;
    private final String openaiBeta = "realtime=v1";

    public NonAzureClientEndpointConfiguration(String protocol, String userAgent, String model, KeyCredential keyCredential) {
        super(protocol, userAgent);
        this.model = model;
        this.keyCredential = keyCredential;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderName.AUTHORIZATION.getCaseInsensitiveName(), keyCredential.getKey());
        headers.add("openai-beta", openaiBeta);
        return headers;
    }

    @Override
    protected String getURIString() {
        return baseUrl + path + "?model=" + model;
    }
}
