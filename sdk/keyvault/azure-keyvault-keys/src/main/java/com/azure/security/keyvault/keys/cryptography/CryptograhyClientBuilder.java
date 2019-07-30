// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.*;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.security.keyvault.keys.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link CryptographyClient cryptography client}
 * by calling {@link CryptograhyClientBuilder#buildClient() buildClient} respectively.
 *
 * <p> The minimal configuration options required by {@link CryptograhyClientBuilder} to build {@link CryptographyClient} is
 * {@link TokenCredential credential}. </p>
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link CryptograhyClientBuilder}.</p>
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and {@link String endpoint}
 * can be specified. It provides finer control over the construction of {@link CryptographyClient}</p>
 *
 * <p> The minimal configuration options required by {@link CryptograhyClientBuilder secretClientBuilder} to build {@link CryptographyClient}
 * are {@link String endpoint} and {@link TokenCredential credential}. </p>
 *
 * @see CryptographyClient
 */
@ServiceClientBuilder(serviceClients = CryptographyClient.class)
public final class CryptograhyClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;
    private String keyId;
    private JsonWebKey key;

    /**
     * The constructor with defaults.
     */
    public CryptograhyClientBuilder(String keyId) {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
        this.keyId = keyId;
    }

    /**
     * The constructor with defaults.
     */
    public CryptograhyClientBuilder(JsonWebKey jsonWebKey) {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
        this.key = jsonWebKey;
    }

    /**
     * Creates a {@link CryptographyClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link CryptographyClient} is created.
     *
     * <p>If {@link CryptograhyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * ({@link CryptograhyClientBuilder#key jsonWebKey} or {@link CryptograhyClientBuilder#keyId key Id}) are used to create the
     * {@link CryptograhyClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link CryptograhyClientBuilder#credential(TokenCredential) is required to build the {@link CryptographyClient client}.}</p>
     *
     * @return A {@link CryptographyClient} with the options set from the builder.
     * @throws IllegalStateException If {@link CryptograhyClientBuilder#credential(TokenCredential)} is not set.
     */
    public CryptographyClient buildClient() {
        Configuration buildConfiguration = (configuration == null) ? ConfigurationManager.getConfiguration().clone() : configuration;

        if (pipeline != null && keyId != null) {
            if(keyId != null){
                return new CryptographyClient(keyId, pipeline);
            } else {
                return new CryptographyClient(key, pipeline);
            }
        }

        if (credential == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.CREDENTIAL_REQUIRED));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION, buildConfiguration));
        policies.add(retryPolicy);
        policies.add(new KeyVaultCredentialPolicy(credential, CryptographyClient.KEY_VAULT_SCOPE));
        policies.addAll(this.policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();

        if (keyId != null) {
            return new CryptographyClient(keyId, pipeline);
        } else {
            return new CryptographyClient(key, pipeline);
        }
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link CryptograhyClientBuilder} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public CryptograhyClientBuilder credential(TokenCredential credential) {
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
     * @return the updated {@link CryptograhyClientBuilder} object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public CryptograhyClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after {@link CryptographyClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link CryptograhyClientBuilder} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public CryptograhyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link CryptograhyClientBuilder} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public CryptograhyClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignoredto build {@link CryptographyClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link CryptograhyClientBuilder} object.
     */
    public CryptograhyClientBuilder pipeline(HttpPipeline pipeline) {
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
     * @return The updated CryptograhyClientBuilder object.
     */
    public CryptograhyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
