// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import reactor.core.publisher.Mono;

/**
 * An object capable of asynchronously retrieving key encryption keys from a provided key identifier.
 */
public class AsyncKeyResolver implements AsyncKeyEncryptionKeyResolver {
    private final CryptographyClientBuilder cryptographyClientBuilder;

    /**
     * Creates an instance of Async Key Resolver.
     */
    public AsyncKeyResolver() {
        cryptographyClientBuilder = new CryptographyClientBuilder();
    }

    /**
     * Creates an instance of Async Key Resolver.
     * @param credential The credential used to create {@link KeyEncryptionKey}
     */
    public AsyncKeyResolver(TokenCredential credential) {
        cryptographyClientBuilder = new CryptographyClientBuilder();
        cryptographyClientBuilder.credential(credential);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AsyncKeyEncryptionKey> resolveKey(String keyId) {
        cryptographyClientBuilder.keyIdentifier(keyId);
        return Mono.just(cryptographyClientBuilder.buildAsyncClient());
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link AsyncKeyResolver} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public AsyncKeyResolver credential(TokenCredential credential) {
        cryptographyClientBuilder.credential(credential);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * <p>logLevel is optional. If not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return the updated {@link AsyncKeyResolver} object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public AsyncKeyResolver httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        cryptographyClientBuilder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after {@link KeyEncryptionKey} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link AsyncKeyResolver} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public AsyncKeyResolver addPolicy(HttpPipelinePolicy policy) {
        cryptographyClientBuilder.addPolicy(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link AsyncKeyResolver} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public AsyncKeyResolver httpClient(HttpClient client) {
        cryptographyClientBuilder.httpClient(client);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * key identifier to create {@link KeyEncryptionKey}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link AsyncKeyResolver} object.
     */
    public AsyncKeyResolver pipeline(HttpPipeline pipeline) {
        cryptographyClientBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the key encryption key.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link AsyncKeyResolver} object.
     */
    public AsyncKeyResolver configuration(Configuration configuration) {
        cryptographyClientBuilder.configuration(configuration);
        return this;
    }
}
