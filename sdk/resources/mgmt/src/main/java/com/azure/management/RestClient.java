// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;

import java.net.URL;

/**
 * A rest client.
 */
public class RestClient {

    private final HttpPipeline pipeline;

    private final URL baseUrl;

    /**
     * The original builder for this rest client.
     */
    private final RestClientBuilder builder;

    /**
     * Creae RestClient.
     *
     * @param baseUrl baseUrl
     * @param pipeline http pipeline
     * @param builder rest client builder
     */
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

    /**
     * @return the http pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * @return the credentials attached to this REST client
     */
    public TokenCredential getCredential() {
        return builder.getCredential();
    }

    /**
     * @return the base url
     */
    public URL getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * @return a new rest client builder with same parameter
     */
    public RestClientBuilder newBuilder() {
        return builder.clone();
    }
}
