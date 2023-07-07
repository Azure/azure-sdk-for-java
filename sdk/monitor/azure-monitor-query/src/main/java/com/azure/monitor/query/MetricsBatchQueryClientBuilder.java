package com.azure.monitor.query;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;

/**
 * This class provides a fluent builder API to help instantiation of {@link MetricsBatchQueryClient MetricsBatchQueryClients}
 */
@ServiceClientBuilder(serviceClients = {MetricsBatchQueryClient.class, MetricsBatchQueryAsyncClient.class})
public final class MetricsBatchQueryClientBuilder  implements EndpointTrait<MetricsBatchQueryClientBuilder>,
    HttpTrait<MetricsBatchQueryClientBuilder>, ConfigurationTrait<MetricsBatchQueryClientBuilder>, TokenCredentialTrait<MetricsBatchQueryClientBuilder> {

    @Override
    public MetricsBatchQueryClientBuilder configuration(Configuration configuration) {
        return null;
    }

    @Override
    public MetricsBatchQueryClientBuilder endpoint(String endpoint) {
        return null;
    }

    @Override
    public MetricsBatchQueryClientBuilder httpClient(HttpClient httpClient) {
        return null;
    }

    @Override
    public MetricsBatchQueryClientBuilder pipeline(HttpPipeline pipeline) {
        return null;
    }

    @Override
    public MetricsBatchQueryClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        return null;
    }

    @Override
    public MetricsBatchQueryClientBuilder retryOptions(RetryOptions retryOptions) {
        return null;
    }

    /**
     *
     * @param retryPolicy
     * @return
     */
    public MetricsBatchQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        return this;
    }

    @Override
    public MetricsBatchQueryClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        return null;
    }

    @Override
    public MetricsBatchQueryClientBuilder clientOptions(ClientOptions clientOptions) {
        return null;
    }

    @Override
    public MetricsBatchQueryClientBuilder credential(TokenCredential credential) {
        return null;
    }

    /**
     *
     * @param serviceVersion
     * @return
     */
    public MetricsBatchQueryClientBuilder serviceVersion(MetricsBatchQueryServiceVersion serviceVersion) {
        return this;
    }

    /**
     *
     * @return
     */
    public MetricsBatchQueryClient buildClient() {
        return null;
    }

    /**
     *
     * @return
     */
    public MetricsBatchQueryAsyncClient buildAsyncClient() {
        return null;
    }

}
