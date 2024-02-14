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
 * The builder class that can be used to build instances of {@link LegacyCompletionClient} and {@link LegacyCompletionAsyncClient}.
 */
public final class LegacyCompletionClientBuilder implements HttpTrait<LegacyCompletionClientBuilder>,
    ConfigurationTrait<LegacyCompletionClientBuilder>, TokenCredentialTrait<LegacyCompletionClientBuilder>,
    KeyCredentialTrait<LegacyCompletionClientBuilder>, EndpointTrait<LegacyCompletionClientBuilder> {

    private final OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder();
    private String model;

    /**
     * Create an instance of the LegacyCompletionClientBuilder.
     */
    public LegacyCompletionClientBuilder() {
    }

    @Override
    public LegacyCompletionClientBuilder pipeline(HttpPipeline pipeline) {
        this.openAIClientBuilder.pipeline(pipeline);
        return this;
    }

    @Override
    public LegacyCompletionClientBuilder httpClient(HttpClient httpClient) {
        this.openAIClientBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.openAIClientBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder clientOptions(ClientOptions clientOptions) {
        this.openAIClientBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder retryOptions(RetryOptions retryOptions) {
        this.openAIClientBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.openAIClientBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder configuration(Configuration configuration) {
        this.openAIClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder credential(TokenCredential tokenCredential) {
        this.openAIClientBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder credential(KeyCredential keyCredential) {
        this.openAIClientBuilder.credential(keyCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public LegacyCompletionClientBuilder endpoint(String endpoint) {
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
     * @return the LegacyCompletionClientBuilder.
     */
    public LegacyCompletionClientBuilder serviceVersion(OpenAIServiceVersion serviceVersion) {
        this.openAIClientBuilder.serviceVersion(serviceVersion);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the LegacyCompletionClientBuilder.
     */
    public LegacyCompletionClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.openAIClientBuilder.retryPolicy(retryPolicy);
        return this;
    }


    /**
     * Sets either the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI) to use for this client.
     * @param model the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI)
     * @return the LegacyCompletionClientBuilder.
     */
    public LegacyCompletionClientBuilder model(String model) {
        this.model = model;
        return this;
    }


    /**
     * Builds an instance of LegacyCompletionAsyncClient class.
     *
     * @return an instance of LegacyCompletionAsyncClient.
     */
    public LegacyCompletionAsyncClient buildAsyncClient() {
        return new LegacyCompletionAsyncClient(openAIClientBuilder.buildAsyncClient(), model);
    }

    /**
     * Builds an instance of LegacyCompletionClient class.
     *
     * @return an instance of LegacyCompletionClient.
     */
    public LegacyCompletionClient buildClient() {
        return new LegacyCompletionClient(openAIClientBuilder.buildClient(), model);
    }
}
