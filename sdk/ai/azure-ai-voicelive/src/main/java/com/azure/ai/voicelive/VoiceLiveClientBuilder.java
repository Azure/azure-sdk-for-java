// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for creating instances of {@link VoiceLiveAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = { VoiceLiveAsyncClient.class })
public final class VoiceLiveClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveClientBuilder.class);
    private static final String DEFAULT_API_VERSION = "2024-10-01-preview";
    private static final String SDK_NAME = "azure-ai-voicelive";
    private static final String SDK_VERSION = "1.0.0-beta.1";

    private URI endpoint;
    private AzureKeyCredential keyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpPipeline pipeline;
    private HttpLogOptions httpLogOptions;
    private ClientOptions clientOptions;
    private RetryPolicy retryPolicy;
    private Configuration configuration;
    private String apiVersion;
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private final HttpHeaders additionalHeaders = new HttpHeaders();

    /**
     * Creates a new instance of VoiceLiveClientBuilder.
     */
    public VoiceLiveClientBuilder() {
        this.httpLogOptions = new HttpLogOptions();
        this.apiVersion = DEFAULT_API_VERSION;
    }

    /**
     * Sets the service endpoint.
     *
     * @param endpoint The service endpoint URL.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed as a URI.
     */
    public VoiceLiveClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        try {
            URI parsed = new URI(endpoint);
            this.endpoint = parsed;
        } catch (URISyntaxException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(String.format("Invalid endpoint URI: '%s'", endpoint), ex));
        }
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used for authentication.
     *
     * @param keyCredential The API key credential.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code keyCredential} is null.
     */
    public VoiceLiveClientBuilder credential(AzureKeyCredential keyCredential) {
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used for authentication.
     *
     * @param tokenCredential The token credential.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code tokenCredential} is null.
     */
    public VoiceLiveClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null");
        return this;
    }

    /**
     * Sets the HTTP client to use for sending requests.
     *
     * @param httpClient The HTTP client.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for sending requests.
     *
     * @param pipeline The HTTP pipeline.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions The HTTP log options.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets the client options.
     *
     * @param clientOptions The client options.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the retry policy.
     *
     * @param retryPolicy The retry policy.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the configuration store.
     *
     * @param configuration The configuration store.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the API version.
     *
     * @param apiVersion The API version.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Adds a policy to be applied before the retry policy.
     *
     * @param policy The policy to add.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null");
        this.perCallPolicies.add(policy);
        return this;
    }

    /**
     * Adds a header to be included in all requests.
     *
     * @param name The header name.
     * @param value The header value.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder addHeader(HttpHeaderName name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null");
        Objects.requireNonNull(value, "'value' cannot be null");
        this.additionalHeaders.set(name, value);
        return this;
    }

    /**
     * Builds a {@link VoiceLiveAsyncClient} instance with the configured options.
     *
     * @return A new VoiceLiveAsyncClient instance.
     * @throws NullPointerException if endpoint is not set.
     * @throws IllegalStateException if neither keyCredential nor tokenCredential is set.
     */
    public VoiceLiveAsyncClient buildClient() {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        if (keyCredential == null && tokenCredential == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Either 'keyCredential' or 'tokenCredential' must be set"));
        }

        HttpPipeline pipeline = this.pipeline;
        if (pipeline == null) {
            pipeline = createHttpPipeline();
        }

        String apiVersion = this.apiVersion != null ? this.apiVersion : DEFAULT_API_VERSION;

        if (keyCredential != null) {
            return new VoiceLiveAsyncClient(endpoint, keyCredential, pipeline, apiVersion, additionalHeaders);
        } else {
            return new VoiceLiveAsyncClient(endpoint, tokenCredential, pipeline, apiVersion, additionalHeaders);
        }
    }

    /**
     * Creates the HTTP pipeline with configured policies.
     *
     * @return The configured HTTP pipeline.
     */
    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        HttpLogOptions buildLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        ClientOptions buildClientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;

        String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

        // Build the list of policies
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        // Add per-call policies
        policies.addAll(perCallPolicies);

        // User agent policy
        policies.add(new UserAgentPolicy(applicationId, SDK_NAME, SDK_VERSION, buildConfiguration));

        // Add custom headers if any
        if (additionalHeaders.getSize() > 0) {
            policies.add(new AddHeadersPolicy(additionalHeaders));
        }

        // Retry policy
        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        // Add per-retry policies
        policies.addAll(perRetryPolicies);

        // Logging policy
        policies.add(new HttpLoggingPolicy(buildLogOptions));

        HttpPipelineBuilder pipelineBuilder
            = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]));

        if (httpClient != null) {
            pipelineBuilder.httpClient(httpClient);
        }

        return pipelineBuilder.build();
    }
}
