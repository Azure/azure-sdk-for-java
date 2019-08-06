// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.security.keyvault.keys.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.implementation.AzureKeyVaultConfiguration;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link CryptographyAsyncClient cryptography async client} and {@link CryptographyClient cryptography sync client},
 * by calling {@link CryptographyClientBuilder#buildAsyncClient() buildAsyncClient} and {@link CryptographyClientBuilder#buildClient() buildClient} respectively
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link CryptographyClientBuilder cryptographyClientBuilder} to build {@link CryptographyAsyncClient}
 * are ({@link JsonWebKey jsonWebKey} or {@link String jsonWebKey identifier}) and {@link TokenCredential credential}). </p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.instantiation}
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link CryptographyClientBuilder}.</p>

 * {@codesnippet com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.withhttpclient.instantiation}
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies
 * can be specified. It provides finer control over the construction of {@link CryptographyAsyncClient} and {@link CryptographyClient}</p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.pipeline.instantiation}
 *
 * <p> The minimal configuration options required by {@link CryptographyClientBuilder cryptographyClientBuilder} to build {@link CryptographyClient}
 * are {@link JsonWebKey jsonWebKey} ot {@link String jsonWebKey identifier}) and {@link TokenCredential credential}). </p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.instantiation}
 *
 * @see CryptographyAsyncClient
 * @see CryptographyClient
 */
@ServiceClientBuilder(serviceClients = CryptographyClient.class)
public final class CryptographyClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private JsonWebKey jsonWebKey;
    private String keyId;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    /**
     * The constructor with defaults.
     */
    public CryptographyClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link CryptographyClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link CryptographyClient} is created.
     *
     * <p>If {@link CryptographyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * ({@link CryptographyClientBuilder#keyIdentifier(String) jsonWebKey identifier} or {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) json web jsonWebKey})
     * are used to create the {@link CryptographyClient client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then ({@link CryptographyClientBuilder#credential(TokenCredential) jsonWebKey vault credential} and
     * ({@link CryptographyClientBuilder#keyIdentifier(String) jsonWebKey identifier} or {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) json web jsonWebKey})
     * are required to build the {@link CryptographyClient client}.</p>
     *
     * @return A {@link CryptographyClient} with the options set from the builder.
     * @throws IllegalStateException If {@link CryptographyClientBuilder#credential(TokenCredential)} or
     * either of ({@link CryptographyClientBuilder#keyIdentifier(String)} or {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey)} have not been set.
     */
    public CryptographyClient buildClient() {
        return new CryptographyClient(buildAsyncClient());
    }
    /**
     * Creates a {@link CryptographyAsyncClient} based on options set in the builder.
     * Every time {@code buildAsyncClient()} is called, a new instance of {@link CryptographyAsyncClient} is created.
     *
     * <p>If {@link CryptographyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * ({@link CryptographyClientBuilder#keyIdentifier(String) jsonWebKey identifier} or {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) json web jsonWebKey})
     * are used to create the {@link CryptographyAsyncClient async client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then ({@link CryptographyClientBuilder#credential(TokenCredential) jsonWebKey vault credential} and
     * ({@link CryptographyClientBuilder#keyIdentifier(String) jsonWebKey identifier} or {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) json web jsonWebKey})
     * are required to build the {@link CryptographyAsyncClient async client}.</p>
     *
     * @return A {@link CryptographyAsyncClient} with the options set from the builder.
     * @throws IllegalStateException If {@link CryptographyClientBuilder#credential(TokenCredential)} or
     * either of ({@link CryptographyClientBuilder#keyIdentifier(String)} or {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey)} have not been set.
     */
    public CryptographyAsyncClient buildAsyncClient() {
        Configuration buildConfiguration = (configuration == null) ? ConfigurationManager.getConfiguration().clone() : configuration;

        if (jsonWebKey == null && Strings.isNullOrEmpty(keyId)) {
            throw new IllegalStateException("Json Web Key or jsonWebKey identifier are required to create cryptography client");
        }

        if (pipeline != null) {
            if (jsonWebKey != null) {
                return new CryptographyAsyncClient(jsonWebKey, pipeline);
            } else {
                return new CryptographyAsyncClient(keyId, pipeline);
            }
        }

        if (credential == null) {
            throw new IllegalStateException("Key Vault credentials " + "are required to build the Cryptography async client");
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION, buildConfiguration));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy);
        policies.add(new KeyVaultCredentialPolicy(credential));
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();

        if (jsonWebKey != null) {
            return new CryptographyAsyncClient(jsonWebKey, pipeline);
        } else {
            return new CryptographyAsyncClient(keyId, pipeline);
        }
    }

    /**
     * Sets the identifier of the jsonWebKey from Azure Key Vault to be used for cryptography operations.
     *
     * <p>If {@code jsonWebKey} is provided then that takes precedence over key identifier and gets used for cryptography operations.</p>

     * @param keyId The jsonWebKey identifier representing the jsonWebKey stored in jsonWebKey vault.
     * @return the updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder keyIdentifier(String keyId) {
        this.keyId = keyId;
        return this;
    }

    /**
     * Sets the jsonWebKey to be used for cryptography operations.
     *
     * <p>If {@code jsonWebKey} is provided then it takes precedence over key identifier and gets used for cryptography operations.</p>
     *
     * @param jsonWebKey The Json web jsonWebKey to be used for cryptography operations.
     * @return the updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder jsonWebKey(JsonWebKey jsonWebKey) {
        this.jsonWebKey = jsonWebKey;
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link CryptographyClientBuilder} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public CryptographyClientBuilder credential(TokenCredential credential) {
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
     * @return the updated {@link CryptographyClientBuilder} object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public CryptographyClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after {@link CryptographyAsyncClient} and {@link CryptographyClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link CryptographyClientBuilder} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public CryptographyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link CryptographyClientBuilder} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public CryptographyClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * ({@link CryptographyClientBuilder#keyIdentifier(String) jsonWebKey identifier} or {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) json web jsonWebKey}
     * to build {@link CryptographyClient} or {@link CryptographyAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder pipeline(HttpPipeline pipeline) {
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
     * @return The updated CryptographyClientBuilder object.
     */
    public CryptographyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
