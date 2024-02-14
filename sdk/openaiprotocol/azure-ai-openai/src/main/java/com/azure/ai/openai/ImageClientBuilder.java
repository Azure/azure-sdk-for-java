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
 * The ImageClientBuilder class provides a fluent builder API to help aid the configuration and instantiation of the
 */
public final class ImageClientBuilder implements HttpTrait<ImageClientBuilder>,
    ConfigurationTrait<ImageClientBuilder>, TokenCredentialTrait<ImageClientBuilder>,
    KeyCredentialTrait<ImageClientBuilder>, EndpointTrait<ImageClientBuilder> {

    private final OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder();
    private String model;

    /**
     * Create an instance of the ImageClientBuilder.
     */
    public ImageClientBuilder() {
    }

    @Override
    public ImageClientBuilder pipeline(HttpPipeline pipeline) {
        this.openAIClientBuilder.pipeline(pipeline);
        return this;
    }

    @Override
    public ImageClientBuilder httpClient(HttpClient httpClient) {
        this.openAIClientBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.openAIClientBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder clientOptions(ClientOptions clientOptions) {
        this.openAIClientBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder retryOptions(RetryOptions retryOptions) {
        this.openAIClientBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.openAIClientBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder configuration(Configuration configuration) {
        this.openAIClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder credential(TokenCredential tokenCredential) {
        this.openAIClientBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder credential(KeyCredential keyCredential) {
        this.openAIClientBuilder.credential(keyCredential);
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ImageClientBuilder endpoint(String endpoint) {
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
     * @return the ImageClientBuilder.
     */
    public ImageClientBuilder serviceVersion(OpenAIServiceVersion serviceVersion) {
        this.openAIClientBuilder.serviceVersion(serviceVersion);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the ImageClientBuilder.
     */
    public ImageClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.openAIClientBuilder.retryPolicy(retryPolicy);
        return this;
    }


    /**
     * Sets either the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI) to use for this client.
     * @param model the model deployment name (when using Azure OpenAI) or model name
     * (when using non-Azure OpenAI)
     * @return the ImageClientBuilder.
     */
    public ImageClientBuilder model(String model) {
        this.model = model;
        return this;
    }


    /**
     * Builds an instance of ImageAsyncClient class.
     *
     * @return an instance of ImageAsyncClient.
     */
    public ImageAsyncClient buildAsyncClient() {
        return new ImageAsyncClient(openAIClientBuilder.buildAsyncClient(), model);
    }

    /**
     * Builds an instance of ImageClient class.
     *
     * @return an instance of ImageClient.
     */
    public ImageClient buildClient() {
        return new ImageClient(openAIClientBuilder.buildClient(), model);
    }
}
