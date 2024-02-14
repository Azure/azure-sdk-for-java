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
 * The builder class that can be used to build instances of {@link AudioClient} and {@link AudioAsyncClient}.
 */
public final class AudioClientBuilder implements HttpTrait<AudioClientBuilder>,
    ConfigurationTrait<AudioClientBuilder>, TokenCredentialTrait<AudioClientBuilder>,
    KeyCredentialTrait<AudioClientBuilder>, EndpointTrait<AudioClientBuilder> {

    private final OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder();
    private String model;

    /**
     * Create an instance of the AudioClientBuilder.
     */
    public AudioClientBuilder() {
    }

    @Override
    public AudioClientBuilder pipeline(HttpPipeline pipeline) {
        this.openAIClientBuilder.pipeline(pipeline);
        return this;
    }

    @Override
    public AudioClientBuilder httpClient(HttpClient httpClient) {
        this.openAIClientBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.openAIClientBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder clientOptions(ClientOptions clientOptions) {
        this.openAIClientBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder retryOptions(RetryOptions retryOptions) {
        this.openAIClientBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.openAIClientBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder configuration(Configuration configuration) {
        this.openAIClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder credential(TokenCredential tokenCredential) {
        this.openAIClientBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder credential(KeyCredential keyCredential) {
        this.openAIClientBuilder.credential(keyCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public AudioClientBuilder endpoint(String endpoint) {
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
     * @return the AudioClientBuilder.
     */
    public AudioClientBuilder serviceVersion(OpenAIServiceVersion serviceVersion) {
        this.openAIClientBuilder.serviceVersion(serviceVersion);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the AudioClientBuilder.
     */
    public AudioClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.openAIClientBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Sets either the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI) to use for this client.
     * @param model the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI)
     * @return the AudioClientBuilder.
     */
    public AudioClientBuilder model(String model) {
        this.model = model;
        return this;
    }

    /**
     * Builds an instance of OpenAIAsyncClient class.
     *
     * @return an instance of OpenAIAsyncClient.
     */
    public AudioAsyncClient buildAsyncClient() {
        return new AudioAsyncClient(openAIClientBuilder.buildAsyncClient(), model);
    }

    /**
     * Builds an instance of OpenAIClient class.
     *
     * @return an instance of OpenAIClient.
     */
    public AudioClient buildClient() {
        return new AudioClient(openAIClientBuilder.buildClient(), model);
    }
}
