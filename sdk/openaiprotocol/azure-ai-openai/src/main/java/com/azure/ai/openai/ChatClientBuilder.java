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
 * The ChatClientBuilder class provides a fluent builder API to help aid the configuration and instantiation of the
 */
public final class ChatClientBuilder implements HttpTrait<ChatClientBuilder>,
    ConfigurationTrait<ChatClientBuilder>, TokenCredentialTrait<ChatClientBuilder>,
    KeyCredentialTrait<ChatClientBuilder>, EndpointTrait<ChatClientBuilder> {

    private final OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder();
    private String model;

    /**
     * Create an instance of the ChatClientBuilder.
     */
    public ChatClientBuilder() {
    }

    @Override
    public ChatClientBuilder pipeline(HttpPipeline pipeline) {
        this.openAIClientBuilder.pipeline(pipeline);
        return this;
    }

    @Override
    public ChatClientBuilder httpClient(HttpClient httpClient) {
        this.openAIClientBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.openAIClientBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder clientOptions(ClientOptions clientOptions) {
        this.openAIClientBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder retryOptions(RetryOptions retryOptions) {
        this.openAIClientBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.openAIClientBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder configuration(Configuration configuration) {
        this.openAIClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder credential(TokenCredential tokenCredential) {
        this.openAIClientBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder credential(KeyCredential keyCredential) {
        this.openAIClientBuilder.credential(keyCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ChatClientBuilder endpoint(String endpoint) {
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
     * @return the ChatClientBuilder.
     */
    public ChatClientBuilder serviceVersion(OpenAIServiceVersion serviceVersion) {
        this.openAIClientBuilder.serviceVersion(serviceVersion);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the ChatClientBuilder.
     */
    public ChatClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.openAIClientBuilder.retryPolicy(retryPolicy);
        return this;
    }


    /**
     * Sets either the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI) to use for this client.
     * @param model the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI)
     * @return the ChatClientBuilder.
     */
    public ChatClientBuilder model(String model) {
        this.model = model;
        return this;
    }


    /**
     * Builds an instance of ChatAsyncClient class.
     *
     * @return an instance of ChatAsyncClient.
     */
    public ChatAsyncClient buildAsyncClient() {
        return new ChatAsyncClient(openAIClientBuilder.buildAsyncClient(), model);
    }

    /**
     * Builds an instance of ChatClient class.
     *
     * @return an instance of ChatClient.
     */
    public ChatClient buildClient() {
        return new ChatClient(openAIClientBuilder.buildClient(), model);
    }
}
