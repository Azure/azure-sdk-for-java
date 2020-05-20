package com.azure.schemaregistry.client.rest;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

/** A builder for creating a new instance of the AzureSchemaRegistryRestService type. */
@ServiceClientBuilder(serviceClients = {AzureSchemaRegistryRestService.class})
public final class AzureSchemaRegistryRestServiceBuilder {
    /*
     * server parameter
     */
    private String host;

    /**
     * Sets server parameter.
     *
     * @param host the host value.
     * @return the AzureSchemaRegistryRestServiceBuilder.
     */
    public AzureSchemaRegistryRestServiceBuilder host(String host) {
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
     * @return the AzureSchemaRegistryRestServiceBuilder.
     */
    public AzureSchemaRegistryRestServiceBuilder pipeline(HttpPipeline pipeline) {
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
