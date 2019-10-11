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
        ServiceVersion serviceVersion = this.version != null ? this.version : ServiceVersion.getLatest();

        if (pipeline != null) {
            return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, pipeline, serviceVersion)));
        }

        if (credential == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Key Vault credentials are required to build the key encryption key async client"));
        }

        HttpPipeline pipeline = setupPipeline(serviceVersion);

        return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, pipeline, serviceVersion)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyEncryptionKeyClientBuilder credential(TokenCredential credential) {
        super.credential(credential);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyEncryptionKeyClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        super.httpLogOptions(logOptions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyEncryptionKeyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        super.addPolicy(policy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public KeyEncryptionKeyClientBuilder httpClient(HttpClient client) {
        super.httpClient(client);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public KeyEncryptionKeyClientBuilder pipeline(HttpPipeline pipeline) {
        super.pipeline(pipeline);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyEncryptionKeyClientBuilder configuration(Configuration configuration) {
        super.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyEncryptionKeyClientBuilder serviceVersion(ServiceVersion version) {
        super.serviceVersion(version);
        return this;
    }
}
