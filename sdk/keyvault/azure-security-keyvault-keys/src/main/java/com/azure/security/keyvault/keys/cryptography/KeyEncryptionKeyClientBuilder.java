// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * AsyncKeyEncryptionKey KeyEncryptionKey async client} and {@link KeyEncryptionKey KeyEncryptionKey sync client},
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
public final class KeyEncryptionKeyClientBuilder implements KeyEncryptionKeyResolver, AsyncKeyEncryptionKeyResolver {
    private final ClientLogger logger = new ClientLogger(KeyEncryptionKeyClientBuilder.class);
    private final CryptographyClientBuilder builder;

    /**
     * The constructor with defaults.
     */
    public KeyEncryptionKeyClientBuilder() {
        builder = new CryptographyClientBuilder();
    }

    /**
     * Creates a {@link KeyEncryptionKey} based on options set in the builder.
     * Every time {@code buildKeyEncryptionKey(String)} is called, a new instance of {@link KeyEncryptionKey}
     * is created.
     *
     * <p>If {@link KeyEncryptionKeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline}
     * and {@code keyId} are used to create the {@link KeyEncryptionKeyClient client}.
     *  All other builder settings are ignored. If {@code pipeline} is not set, then
     * {@link KeyEncryptionKeyClientBuilder#credential(TokenCredential) vault credential} and {@code keyId}
     * are required to build the {@link KeyEncryptionKeyClient client}.</p>
     *
     * @return A {@link KeyEncryptionKeyClient} with the options set from the builder.
     * @throws IllegalStateException If {@link KeyEncryptionKeyClientBuilder#credential(TokenCredential)} or
     * {@code keyId} have not been set.
     */
    @Override
    public KeyEncryptionKey buildKeyEncryptionKey(String keyId) {
        return new KeyEncryptionKeyClient((KeyEncryptionKeyAsyncClient) buildAsyncKeyEncryptionKey(keyId).block());
    }

    /**
     * Creates a {@link KeyEncryptionKeyAsyncClient} based on options set in the builder.
     * Every time {@code buildAsyncKeyEncryptionKey(String)} is called, a new instance of
     * {@link KeyEncryptionKeyAsyncClient} is created.
     *
     * <p>If {@link KeyEncryptionKeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline}
     * and {@code keyId} are used to create the {@link KeyEncryptionKeyAsyncClient async client}.
     * All other builder settings are ignored. If {@code pipeline} is not set, then
     * ({@link KeyEncryptionKeyClientBuilder#credential(TokenCredential) jsonWebKey vault credential} and
     * {@code keyId} are required to build the {@link KeyEncryptionKeyAsyncClient async client}.</p>
     *
     * @return A {@link KeyEncryptionKeyAsyncClient} with the options set from the builder.
     * @throws IllegalStateException If {@link KeyEncryptionKeyClientBuilder#credential(TokenCredential)} or
     * {@code keyId} have not been set.
     */
    @Override
    public Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(String keyId) {
        builder.keyIdentifier(keyId);
        if (Strings.isNullOrEmpty(keyId)) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Json Web Key or jsonWebKey identifier are required to create key encryption key async client"));
        }
        CryptographyServiceVersion serviceVersion = builder.getServiceVersion() != null ? builder.getServiceVersion() : CryptographyServiceVersion.getLatest();

        if (builder.getPipeline() != null) {
            return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, builder.getPipeline(), serviceVersion)));
        }

        if (builder.getCredential() == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Key Vault credentials are required to build the key encryption key async client"));
        }

        HttpPipeline pipeline = builder.setupPipeline();

        return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, pipeline, serviceVersion)));
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated builder object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public KeyEncryptionKeyClientBuilder credential(TokenCredential credential) {
        builder.credential(credential);
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated builder object.
     */
    public KeyEncryptionKeyClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        builder.httpLogOptions(logOptions);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after the client required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated builder object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public KeyEncryptionKeyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        builder.addPolicy(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated builder object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public KeyEncryptionKeyClientBuilder httpClient(HttpClient client) {
        builder.httpClient(client);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from jsonWebKey identifier
     * or jsonWebKey to build the clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated builder object.
     */
    public KeyEncryptionKeyClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return the updated builder object.
     */
    public KeyEncryptionKeyClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }


    /**
     * Sets the {@link CryptographyServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     * @return The updated builder object.
     */
    public KeyEncryptionKeyClientBuilder serviceVersion(CryptographyServiceVersion version) {
        builder.serviceVersion(version);
        return this;
    }
}
