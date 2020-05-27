package com.microsoft.azure.table;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

/** A builder for creating a new instance of the AzureTable type. */
@ServiceClientBuilder(serviceClients = {AzureTable.class})
public final class AzureTableBuilder {
    /*
     * The URL of the service account or table that is the targe of the desired
     * operation.
     */
    private String url;

    /**
     * Sets The URL of the service account or table that is the targe of the desired operation.
     *
     * @param url the url value.
     * @return the AzureTableBuilder.
     */
    public AzureTableBuilder url(String url) {
        this.url = url;
        return this;
    }

    /*
     * Specifies the version of the operation to use for this request.
     */
    private String version;

    /**
     * Sets Specifies the version of the operation to use for this request.
     *
     * @param version the version value.
     * @return the AzureTableBuilder.
     */
    public AzureTableBuilder version(String version) {
        this.version = version;
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
     * @return the AzureTableBuilder.
     */
    public AzureTableBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Builds an instance of AzureTable with the provided parameters.
     *
     * @return an instance of AzureTable.
     */
    public AzureTable buildClient() {
        if (pipeline == null) {
            this.pipeline =
                    new HttpPipelineBuilder()
                            .policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy())
                            .build();
        }
        AzureTable client = new AzureTable(pipeline);
        client.setUrl(this.url);
        client.setVersion(this.version);
        return client;
    }
}
