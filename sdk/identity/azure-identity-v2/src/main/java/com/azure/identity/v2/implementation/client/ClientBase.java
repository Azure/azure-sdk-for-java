// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.client;

import com.azure.identity.v2.implementation.models.ClientOptionsBase;
import com.azure.identity.v2.implementation.models.HttpPipelineOptions;
import com.azure.identity.v2.implementation.models.MsalCommonOptions;
import com.azure.identity.v2.implementation.util.IdentityUtil;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class ClientBase {

    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final Map<String, String> properties = new HashMap<>();
    final ClientOptionsBase optionsBase;
    final String tenantId;
    final String clientId;
    HttpPipelineAdapter httpPipelineAdapter;
    HttpPipeline httpPipeline;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param options the options configuring the client.
     */
    public ClientBase(ClientOptionsBase options) {
        this.optionsBase = options;
        this.tenantId = getMsalOptions().getTenantId() == null ? IdentityUtil.DEFAULT_TENANT
            : getMsalOptions().getTenantId();
        this.clientId = getMsalOptions().getClientId();
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

    MsalCommonOptions getMsalOptions() {
        return optionsBase.getMsalCommonOptions();
    }

    HttpPipelineOptions getHttpPipelineOptions() {
        return optionsBase.getHttpPipelineOptions();
    }
}
