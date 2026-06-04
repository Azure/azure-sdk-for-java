// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.cosmos.ai.implementation.InferenceService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for creating {@link CosmosAIAsyncClient} and {@link CosmosAIClient} instances.
 *
 * <p>Minimum configuration requires {@link #endpoint(String)} and {@link #credential(TokenCredential)}.
 * The endpoint can also be supplied via the system property {@code azure.cosmos.semanticReranker.inferenceEndpoint}
 * or the environment variable {@code AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT}.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * CosmosAIAsyncClient asyncClient = new CosmosAIClientBuilder()
 *     .endpoint("https://my-inference.dbinference.azure.com")
 *     .credential(new DefaultAzureCredentialBuilder().build())
 *     .buildAsyncClient();
 * </pre>
 */
public final class CosmosAIClientBuilder {

    private static final String INFERENCE_SCOPE = "https://dbinference.azure.com/.default";
    private static final String SDK_NAME = "azure-cosmos-ai";
    private static final String SDK_VERSION = "1.0.0-beta.1";

    private static final String INFERENCE_ENDPOINT_PROPERTY = "azure.cosmos.semanticReranker.inferenceEndpoint";
    private static final String INFERENCE_ENDPOINT_ENVIRONMENT_VARIABLE = "AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT";

    private String endpoint;
    private TokenCredential credential;
    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();

    /**
     * Creates a new instance of CosmosAIClientBuilder.
     */
    public CosmosAIClientBuilder() {
    }

    /**
     * Sets the inference service endpoint.
     *
     * <p>If not set, the builder falls back to the system property
     * {@code azure.cosmos.semanticReranker.inferenceEndpoint} or the environment variable
     * {@code AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT}.</p>
     *
     * @param endpoint The inference service endpoint URL.
     * @return this builder.
     */
    public CosmosAIClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the Azure AD token credential for authentication.
     *
     * @param credential The token credential.
     * @return this builder.
     */
    public CosmosAIClientBuilder credential(TokenCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Sets a custom HTTP pipeline.
     *
     * <p>When a pipeline is provided, the builder ignores {@link #credential(TokenCredential)},
     * {@link #httpClient(HttpClient)}, and any additional policies — the caller is fully in
     * control of the pipeline configuration.</p>
     *
     * @param httpPipeline The HTTP pipeline.
     * @return this builder.
     */
    public CosmosAIClientBuilder pipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the HTTP client implementation to use.
     *
     * @param httpClient The HTTP client.
     * @return this builder.
     */
    public CosmosAIClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the HTTP log options for request and response logging.
     *
     * @param httpLogOptions The HTTP log options.
     * @return this builder.
     */
    public CosmosAIClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Adds an additional pipeline policy.
     *
     * @param policy The policy to add.
     * @return this builder.
     */
    public CosmosAIClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' must not be null.");
        this.additionalPolicies.add(policy);
        return this;
    }

    /**
     * Builds a new {@link CosmosAIAsyncClient} instance.
     *
     * @return A new asynchronous client.
     * @throws IllegalStateException if the endpoint cannot be resolved or credential is missing.
     */
    public CosmosAIAsyncClient buildAsyncClient() {
        URI resolvedEndpoint = resolveEndpoint();
        HttpPipeline pipeline = buildPipeline();
        InferenceService inferenceService = new InferenceService(resolvedEndpoint, pipeline);
        return new CosmosAIAsyncClient(inferenceService);
    }

    /**
     * Builds a new {@link CosmosAIClient} instance.
     *
     * @return A new synchronous client.
     * @throws IllegalStateException if the endpoint cannot be resolved or credential is missing.
     */
    public CosmosAIClient buildClient() {
        return new CosmosAIClient(buildAsyncClient());
    }

    private URI resolveEndpoint() {
        if (endpoint != null && !endpoint.trim().isEmpty()) {
            return URI.create(endpoint);
        }

        String fromProperty = System.getProperty(INFERENCE_ENDPOINT_PROPERTY);
        if (fromProperty != null && !fromProperty.trim().isEmpty()) {
            return URI.create(fromProperty);
        }

        String fromEnv = System.getenv(INFERENCE_ENDPOINT_ENVIRONMENT_VARIABLE);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return URI.create(fromEnv);
        }

        throw new IllegalStateException(
            "Inference endpoint must be set via .endpoint(), system property '"
                + INFERENCE_ENDPOINT_PROPERTY + "', or environment variable '"
                + INFERENCE_ENDPOINT_ENVIRONMENT_VARIABLE + "'.");
    }

    private HttpPipeline buildPipeline() {
        if (this.httpPipeline != null) {
            return this.httpPipeline;
        }

        if (this.credential == null) {
            throw new IllegalStateException(
                "Semantic reranking requires AAD authentication. "
                    + "Provide a TokenCredential via .credential() or a pre-built pipeline via .pipeline().");
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, null));
        policies.add(new RequestIdPolicy());
        policies.add(new BearerTokenAuthenticationPolicy(this.credential, INFERENCE_SCOPE));
        policies.addAll(this.additionalPolicies);
        policies.add(new HttpLoggingPolicy(
            this.httpLogOptions != null ? this.httpLogOptions : new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE)));

        HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]));

        if (this.httpClient != null) {
            pipelineBuilder.httpClient(this.httpClient);
        }

        return pipelineBuilder.build();
    }
}
