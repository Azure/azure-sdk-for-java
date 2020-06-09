// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

/** A builder for creating a new instance of the AzureSchemaRegistryRestService type. */
@ServiceClientBuilder(serviceClients = {AzureSchemaRegistryRestService.class})
public final class AzureSchemaRegistryRestServiceClientBuilder {
    /*
     * server parameter
     */
    private String host;

    /**
     * Sets server parameter.
     *
     * @param host the host value.
     * @return the AzureSchemaRegistryRestServiceClientBuilder.
     */
    public AzureSchemaRegistryRestServiceClientBuilder host(String host) {
        this.host = host;
        return this;
    }

    /*
     * The HTTP pipeline to send requests through
     */
    private HttpPipeline pipeline;

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the AzureSchemaRegistryRestServiceClientBuilder.
     */
    public AzureSchemaRegistryRestServiceClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Builds an instance of AzureSchemaRegistryRestService with the provided parameters.
     *
     * @return an instance of AzureSchemaRegistryRestService.
     */
    public AzureSchemaRegistryRestService buildClient() {
        if (host == null) {
            this.host = "";
        }
        if (pipeline == null) {
            this.pipeline =
                    new HttpPipelineBuilder()
                            .policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy())
                            .build();
        }
        AzureSchemaRegistryRestService client = new AzureSchemaRegistryRestService(pipeline);
        client.setHost(this.host);
        return client;
    }
}
