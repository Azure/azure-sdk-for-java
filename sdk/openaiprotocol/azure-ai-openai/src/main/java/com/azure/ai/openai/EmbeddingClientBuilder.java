package com.azure.ai.openai;

import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.KeyCredentialTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.KeyCredential;
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
 * The {@link EmbeddingClientBuilder} class builds an instance of {@link EmbeddingAsyncClient} and {@link EmbeddingClient}
 */
public final class EmbeddingClientBuilder implements HttpTrait<EmbeddingClientBuilder>,
    ConfigurationTrait<EmbeddingClientBuilder>, TokenCredentialTrait<EmbeddingClientBuilder>,
    KeyCredentialTrait<EmbeddingClientBuilder>, EndpointTrait<EmbeddingClientBuilder> {

    private final OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder();
    private String model;

    /**
     * Create an instance of the EmbeddingClientBuilder.
     */
    public EmbeddingClientBuilder() {
    }

    @Override
    public EmbeddingClientBuilder pipeline(HttpPipeline pipeline) {
        this.openAIClientBuilder.pipeline(pipeline);
        return this;
    }

    @Override
    public EmbeddingClientBuilder httpClient(HttpClient httpClient) {
        this.openAIClientBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.openAIClientBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder clientOptions(ClientOptions clientOptions) {
        this.openAIClientBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder retryOptions(RetryOptions retryOptions) {
        this.openAIClientBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.openAIClientBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder configuration(Configuration configuration) {
        this.openAIClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder credential(TokenCredential tokenCredential) {
        this.openAIClientBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder credential(KeyCredential keyCredential) {
        this.openAIClientBuilder.credential(keyCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public EmbeddingClientBuilder endpoint(String endpoint) {
        this.openAIClientBuilder.endpoint(endpoint);
        return this;
    }

    /*
     * Service version
     */
    private OpenAIServiceVersion serviceVersion;

    /**
     * Sets Service version.
     *
     * @param serviceVersion the serviceVersion value.
     * @return the EmbeddingClientBuilder.
     */
    public EmbeddingClientBuilder serviceVersion(OpenAIServiceVersion serviceVersion) {
        this.openAIClientBuilder.serviceVersion(serviceVersion);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the EmbeddingClientBuilder.
     */
    public EmbeddingClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.openAIClientBuilder.retryPolicy(retryPolicy);
        return this;
    }


    /**
     * Sets either the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI) to use for this client.
     * @param model the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI)
     * @return the EmbeddingClientBuilder.
     */
    public EmbeddingClientBuilder model(String model) {
        this.model = model;
        return this;
    }


    /**
     * Builds an instance of EmbeddingAsyncClient class.
     *
     * @return an instance of EmbeddingAsyncClient.
     */
    public EmbeddingAsyncClient buildAsyncClient() {
        return new EmbeddingAsyncClient(openAIClientBuilder.buildAsyncClient(), model);
    }

    /**
     * Builds an instance of EmbeddingClient class.
     *
     * @return an instance of EmbeddingClient.
     */
    public EmbeddingClient buildClient() {
        return new EmbeddingClient(openAIClientBuilder.buildClient(), model);
    }

}
