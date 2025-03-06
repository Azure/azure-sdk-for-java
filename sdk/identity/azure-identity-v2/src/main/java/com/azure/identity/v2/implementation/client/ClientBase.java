// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.client;

import com.azure.identity.v2.implementation.models.ClientOptions;
import com.azure.identity.v2.implementation.models.HttpPipelineOptions;
import com.azure.identity.v2.implementation.util.IdentityUtil;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;

import java.util.*;

public abstract class ClientBase {

    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final Map<String, String> properties = new HashMap<>();
    final ClientOptions clientOptions;
    final String tenantId;
    final String clientId;
    HttpPipelineAdapter httpPipelineAdapter;
    HttpPipeline httpPipeline;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param options the options configuring the client.
     */
    public ClientBase(ClientOptions options) {
        Objects.requireNonNull(options, "The Client options cannot be null.");
        this.clientOptions = options;
        this.tenantId = clientOptions.getTenantId() == null ? IdentityUtil.DEFAULT_TENANT : clientOptions.getTenantId();
        this.clientId = clientOptions.getClientId();
    }

    HttpPipeline getPipeline() {
        // if we've already initialized, return the pipeline
        if (this.httpPipeline != null) {
            return httpPipeline;
        }

        // if the user has supplied a pipeline, use it
        HttpPipeline httpPipeline = getHttpPipelineOptions().getHttpPipeline();
        if (httpPipeline != null) {
            this.httpPipeline = httpPipeline;
            return this.httpPipeline;
        }

        // setupPipeline will use the user's HttpClient and HttpClientOptions if they're set
        // otherwise it will use defaults.
        this.httpPipeline = setupPipeline();
        return this.httpPipeline;
    }

    HttpPipeline setupPipeline() {
        //TODO g2vinay: Wire the HttpPipelineOptions in Pipeline construction.
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new HttpRetryPolicy());
        HttpPipeline httpPipeline = new HttpPipelineBuilder().addPolicy(policies.get(0))
            .httpClient(getHttpPipelineOptions().getHttpClient())
            .build();
        return httpPipeline;
    }

    void initializeHttpPipelineAdapter() {
        httpPipelineAdapter = new HttpPipelineAdapter(getPipeline(), getHttpPipelineOptions());
    }

    HttpPipelineOptions getHttpPipelineOptions() {
        return clientOptions.getHttpPipelineOptions();
    }
}
