// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.util.ImplUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link SecretAsyncClient secret async client} and {@link SecretClient secret client},
 * by calling {@link SecretClientBuilder#buildAsyncClient() buildAsyncClient} and {@link SecretClientBuilder#buildClient() buildClient} respectively.
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link SecretClientBuilder secretClientBuilder} to build
 * {@link SecretAsyncClient} are {@link String endpoint} and {@link TokenCredential credential}. </p>
 *
 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.construct}
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.secretclient.sync.construct}
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link SecretClientBuilder}.</p>
 *
 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.withhttpclient.instantiation}
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and {@link String endpoint}
 * can be specified. It provides finer control over the construction of {@link SecretAsyncClient client}</p>

 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.pipeline.instantiation}
 *
 * @see SecretClient
 * @see SecretAsyncClient
 */
@ServiceClientBuilder(serviceClients = SecretClient.class)
public final class SecretClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL endpoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    /**
     * The constructor with defaults.
     */
    public SecretClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link SecretClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link SecretClient} is created.
     *
     * <p>If {@link SecretClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link SecretClientBuilder#endpoint(String) serviceEndpoint} are used to create the
     * {@link SecretClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link SecretClientBuilder#credential(TokenCredential) key vault credential and
     * {@link SecretClientBuilder#endpoint(String)} key vault endpoint are required to build the {@link SecretClient client}.}</p>
     *
     * @return A SecretClient with the options set from the builder.
     * @throws IllegalStateException If {@link SecretClientBuilder#credential(TokenCredential)} or
     * {@link SecretClientBuilder#endpoint(String)} have not been set.
     */
    public SecretClient buildClient() {
        return new SecretClient(buildAsyncClient());
    }

    /**
     * Creates a {@link SecretAsyncClient} based on options set in the builder.
     * Every time {@code buildAsyncClient()} is called, a new instance of {@link SecretAsyncClient} is created.
     *
     * <p>If {@link SecretClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link SecretClientBuilder#endpoint(String) serviceEndpoint} are used to create the
     * {@link SecretClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link SecretClientBuilder#credential(TokenCredential) key vault credential and
     * {@link SecretClientBuilder#endpoint(String)} key vault endpoint are required to build the {@link SecretAsyncClient client}.}</p>
     *
     * @return A SecretAsyncClient with the options set from the builder.
     * @throws IllegalStateException If {@link SecretClientBuilder#credential(TokenCredential)} or
     * {@link SecretClientBuilder#endpoint(String)} have not been set.
     */
    public SecretAsyncClient buildAsyncClient() {

        Configuration buildConfiguration = (configuration == null) ? ConfigurationManager.getConfiguration().clone() : configuration;
        URL buildEndpoint = getBuildEndpoint(buildConfiguration);

        if (buildEndpoint == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        }

        if (pipeline != null) {
            return new SecretAsyncClient(endpoint, pipeline);
        }

        if (credential == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.CREDENTIAL_REQUIRED));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION, buildConfiguration));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy);
        policies.add(new BearerTokenAuthenticationPolicy(credential, SecretAsyncClient.KEY_VAULT_SCOPE));
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();

        return new SecretAsyncClient(endpoint, pipeline);
    }

    /**
     * Sets the vault endpoint url to send HTTP requests to.
     *
     * @param endpoint The vault endpoint url is used as destination on Azure to send requests to.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws IllegalArgumentException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public SecretClientBuilder endpoint(String endpoint) {
        try {
            this.endpoint = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The Azure Key Vault endpoint url is malformed.");
        }
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public SecretClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.credential = credential;
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * <p>logLevel is optional. If not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public SecretClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link SecretAsyncClient} or {@link SecretClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public SecretClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public SecretClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link SecretClientBuilder#endpoint(String) endpoint} to build {@link SecretAsyncClient} or {@link SecretClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link SecretClientBuilder} object.
     */
    public SecretClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link ConfigurationManager#getConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated SecretClientBuilder object.
     */
    public SecretClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    private URL getBuildEndpoint(Configuration configuration) {
        if (endpoint != null) {
            return endpoint;
        }

        String configEndpoint = configuration.get("AZURE_KEYVAULT_ENDPOINT");
        if (ImplUtils.isNullOrEmpty(configEndpoint)) {
            return null;
        }

        try {
            return new URL(configEndpoint);
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}
