// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.client;

import com.azure.identity.v2.implementation.models.ClientOptions;
import com.azure.identity.v2.implementation.models.HttpPipelineOptions;
import com.azure.identity.v2.implementation.util.IdentityUtil;
import io.clientcore.core.http.pipeline.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ClientBase {

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
        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(clientOptions.getHttpPipelineOptions().getHttpRedirectOptions() == null
            ? new HttpRedirectPolicy()
            : new HttpRedirectPolicy(clientOptions.getHttpPipelineOptions().getHttpRedirectOptions()));

        if (clientOptions.getHttpPipelineOptions().getHttpRetryOptions() != null) {
            policies.add(new HttpRetryPolicy(clientOptions.getHttpPipelineOptions().getHttpRetryOptions()));
        } else {
            policies.add(new HttpRetryPolicy());
        }

        //TODO (g2vinay): Wire the ClientOptions and HeaderPolicy when available in Pipeline construction.

        policies.addAll(this.clientOptions.getHttpPipelineOptions().getHttpPipelinePolicy());

        HttpInstrumentationOptions instrumentationOptions
            = this.clientOptions.getHttpPipelineOptions().getHttpInstrumentationOptions() == null
                ? new HttpInstrumentationOptions()
                : this.clientOptions.getHttpPipelineOptions().getHttpInstrumentationOptions();

        policies.add(new HttpInstrumentationPolicy(instrumentationOptions));

        HttpPipelineBuilder httpPipelineBuilder = new HttpPipelineBuilder();

        // Add all policies to the pipeline.
        policies.forEach(httpPipelineBuilder::addPolicy);

        return httpPipelineBuilder.httpClient(getHttpPipelineOptions().getHttpClient()).build();
    }

    void initializeHttpPipelineAdapter() {
        httpPipelineAdapter = new HttpPipelineAdapter(getPipeline(), getHttpPipelineOptions());
    }

    HttpPipelineOptions getHttpPipelineOptions() {
        return clientOptions.getHttpPipelineOptions();
    }
}
