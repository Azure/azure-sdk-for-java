// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;

import java.net.URL;

public class RestClient {

    private HttpPipeline pipeline;

    private final URL baseUrl;

    /**
     * The original builder for this rest client.
     */
    private final RestClientBuilder builder;

    public RestClient(URL baseUrl, HttpPipeline pipeline, RestClientBuilder builder) {
        this.baseUrl = baseUrl;
        this.pipeline = pipeline;
        this.builder = builder;
    }

    /**
     * @return the current serializer adapter.
     */
    public SerializerAdapter getSerializerAdapter() {
        return builder.getSerializerAdapter();
    }

    public HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * @return the credentials attached to this REST client
     */
    public TokenCredential getCredential() {
        return builder.getCredential();
    }

    public URL getBaseUrl() {
        return this.baseUrl;
    }

    public RestClientBuilder newBuilder() {
        return builder.clone();
    }
}
