// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * AsyncKeyEncryptionKey KeyRncryptionRey async client} and {@link KeyEncryptionKey KeyEncryptionKey sync client},
 * by calling {@link KeyEncryptionKeyClientBuilder#buildAsyncKeyEncryptionKey(String)} and {@link
 * KeyEncryptionKeyClientBuilder#buildKeyEncryptionKey(String)} respectively
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link KeyEncryptionKeyClientBuilder} to
 * build {@link AsyncKeyEncryptionKey} are {@link String identifier}) and
 * {@link TokenCredential credential}).</p>
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link KeyEncryptionKeyClientBuilder}.</p>
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies
 * can be specified. It provides finer control over the construction of {@link AsyncKeyEncryptionKey} and {@link
 * KeyEncryptionKey}</p>
 *
 * <p> The minimal configuration options required by {@link KeyEncryptionKeyClientBuilder keyEncryptionKeyClientBuilder}
 * to build {@link KeyEncryptionKey} are {@link String key identifier}) and
 * {@link TokenCredential credential}).</p>
 *
 * @see KeyEncryptionKeyAsyncClient
 * @see KeyEncryptionKeyClient
 */
@ServiceClientBuilder(serviceClients = {KeyEncryptionKeyClient.class, KeyEncryptionKeyAsyncClient.class})
public final class KeyEncryptionKeyClientBuilder extends CryptographyClientBuilder implements KeyEncryptionKeyResolver, AsyncKeyEncryptionKeyResolver {
    private final ClientLogger logger = new ClientLogger(KeyEncryptionKeyClientBuilder.class);

    /**
     * The constructor with defaults.
     */
    public KeyEncryptionKeyClientBuilder() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyEncryptionKey buildKeyEncryptionKey(String keyId) {
        return new KeyEncryptionKeyClient((KeyEncryptionKeyAsyncClient) buildAsyncKeyEncryptionKey(keyId).block());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(String keyId) {
        this.keyId = keyId;
        if (Strings.isNullOrEmpty(keyId)) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Json Web Key or jsonWebKey identifier are required to create key encryption key async client"));
        }

        if (pipeline != null) {
            return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, pipeline)));
        }

        if (credential == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Key Vault credentials are required to build the key encryption key async client"));
        }

        HttpPipeline pipeline = setupPipeline();

        return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, pipeline)));
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link KeyEncryptionKeyClientBuilder} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public KeyEncryptionKeyClientBuilder credential(TokenCredential credential) {
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
     * @return the updated {@link KeyEncryptionKeyClientBuilder} object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public KeyEncryptionKeyClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after {@link KeyEncryptionKeyAsyncClient} and {@link
     * KeyEncryptionKeyClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link KeyEncryptionKeyClientBuilder} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public KeyEncryptionKeyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link KeyEncryptionKeyClientBuilder} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public KeyEncryptionKeyClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored to build {@link AsyncKeyEncryptionKey} or
     * {@link KeyEncryptionKey}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    public KeyEncryptionKeyClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated KeyEncryptionKeyClientBuilder object.
     */
    public KeyEncryptionKeyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
