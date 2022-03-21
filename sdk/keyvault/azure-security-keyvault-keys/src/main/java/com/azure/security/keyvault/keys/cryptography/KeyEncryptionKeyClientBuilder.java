// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import reactor.core.publisher.Mono;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link AsyncKeyEncryptionKey KeyEncryptionKey async client} and
 * {@link KeyEncryptionKey KeyEncryptionKey sync client}, by calling
 * {@link KeyEncryptionKeyClientBuilder#buildAsyncKeyEncryptionKey(String)} and
 * {@link KeyEncryptionKeyClientBuilder#buildKeyEncryptionKey(String)} respectively. It constructs an instance of the
 * desired client.
 *
 * <p>The minimal configuration options required by {@link KeyEncryptionKeyClientBuilder} to build
 * {@link AsyncKeyEncryptionKey} are {@link JsonWebKey jsonWebKey} or {@link String Azure Key Vault key identifier}
 * and {@link TokenCredential credential}.</p>
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link KeyEncryptionKeyClientBuilder}.</p>
 *
 * <p>Alternatively, a custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies
 * can be specified. It provides finer control over the construction of {@link AsyncKeyEncryptionKey} and
 * {@link KeyEncryptionKey}</p>
 *
 * <p> The minimal configuration options required by {@link KeyEncryptionKeyClientBuilder keyEncryptionKeyClientBuilder}
 * to build {@link KeyEncryptionKey} are {@link JsonWebKey jsonWebKey} or
 * {@link String Azure Key Vault key identifier} and {@link TokenCredential credential}.</p>
 *
 * @see KeyEncryptionKeyAsyncClient
 * @see KeyEncryptionKeyClient
 */
@ServiceClientBuilder(serviceClients = {KeyEncryptionKeyClient.class, KeyEncryptionKeyAsyncClient.class})
public final class KeyEncryptionKeyClientBuilder implements KeyEncryptionKeyResolver, AsyncKeyEncryptionKeyResolver,
    TokenCredentialTrait<KeyEncryptionKeyClientBuilder>,
    HttpTrait<KeyEncryptionKeyClientBuilder>,
    ConfigurationTrait<KeyEncryptionKeyClientBuilder> {
    private final ClientLogger logger = new ClientLogger(KeyEncryptionKeyClientBuilder.class);
    private final CryptographyClientBuilder builder;

    /**
     * The constructor with defaults.
     */
    public KeyEncryptionKeyClientBuilder() {
        builder = new CryptographyClientBuilder();
    }

    /**
     * Creates a {@link KeyEncryptionKey} based on options set in the builder. Every time
     * {@code buildKeyEncryptionKey(String)} is called, a new instance of {@link KeyEncryptionKey} is created.
     *
     * <p>If {@link KeyEncryptionKeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline}
     * and {@code keyId} are used to create the {@link KeyEncryptionKeyClient client}. All other builder settings are
     * ignored. If {@code pipeline} is not set, then an
     * {@link KeyEncryptionKeyClientBuilder#credential(TokenCredential) Azure Key Vault credential} and {@code keyId}
     * are required to build the {@link KeyEncryptionKeyClient client}.</p>
     *
     * @return A {@link KeyEncryptionKeyClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link KeyEncryptionKeyClientBuilder#credential(TokenCredential)} or
     * {@code keyId} have not been set.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    @Override
    public KeyEncryptionKey buildKeyEncryptionKey(String keyId) {
        return new KeyEncryptionKeyClient((KeyEncryptionKeyAsyncClient) buildAsyncKeyEncryptionKey(keyId).block());
    }

    /**
     * Creates a local {@link KeyEncryptionKeyClient} for a given JSON Web Key. Every time
     * {@code buildKeyEncryptionKey(JsonWebKey)} is called, a new instance of {@link KeyEncryptionKey} is created.
     * For local clients, all other builder settings are ignored.
     *
     * <p>The {@code key} is required to build the {@link KeyEncryptionKeyClient client}.</p>
     *
     * @param key The {@link JsonWebKey} to be used for cryptography operations.
     *
     * @return A {@link KeyEncryptionKeyClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {{@code key} is not set.
     */
    public KeyEncryptionKey buildKeyEncryptionKey(JsonWebKey key) {
        return new KeyEncryptionKeyClient((KeyEncryptionKeyAsyncClient) buildAsyncKeyEncryptionKey(key).block());
    }

    /**
     * Creates a {@link KeyEncryptionKeyAsyncClient} based on options set in the builder. Every time
     * {@code buildAsyncKeyEncryptionKey(String)} is called, a new instance of {@link KeyEncryptionKeyAsyncClient} is
     * created.
     *
     * <p>If {@link KeyEncryptionKeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline}
     * and {@code keyId} are used to create the {@link KeyEncryptionKeyAsyncClient async client}. All other builder
     * settings are ignored. If {@code pipeline} is not set, then an
     * {@link KeyEncryptionKeyClientBuilder#credential(TokenCredential) Azure Key Vault credentials} and
     * {@code keyId} are required to build the {@link KeyEncryptionKeyAsyncClient async client}.</p>
     *
     * @return A {@link KeyEncryptionKeyAsyncClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link KeyEncryptionKeyClientBuilder#credential(TokenCredential)} is
     * {@code null} or {@code keyId} is empty or {@code null}.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    @Override
    public Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(String keyId) {
        builder.keyIdentifier(keyId);

        if (Strings.isNullOrEmpty(keyId)) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "An Azure Key Vault key identifier cannot be null and is required to build the key encryption key "
                    + "client."));
        }

        CryptographyServiceVersion serviceVersion = builder.getServiceVersion() != null ? builder.getServiceVersion() : CryptographyServiceVersion.getLatest();

        if (builder.getPipeline() != null) {
            return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, builder.getPipeline(), serviceVersion)));
        }

        if (builder.getCredential() == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Azure Key Vault credentials cannot be null and are required to build a key encryption key client."));
        }

        HttpPipeline pipeline = builder.setupPipeline();

        return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(keyId, pipeline, serviceVersion)));
    }

    /**
     * Creates a local {@link KeyEncryptionKeyAsyncClient} based on options set in the builder. Every time
     * {@code buildAsyncKeyEncryptionKey(String)} is called, a new instance of
     * {@link KeyEncryptionKeyAsyncClient} is created. For local clients, all other builder settings are ignored.
     *
     * <p>The {@code key} is required to build the {@link KeyEncryptionKeyAsyncClient client}.</p>
     *
     * @param key The key to be used for cryptography operations.
     *
     * @return A {@link KeyEncryptionKeyAsyncClient} with the options set from the builder.
     *
     * @throws IllegalArgumentException If {@code key} has no id.
     * @throws IllegalStateException If {@code key} is {@code null}.
     */
    public Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(JsonWebKey key) {
        if (key == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "JSON Web Key cannot be null and is required to build a local key encryption key async client."));
        } else if (key.getId() == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "JSON Web Key's id property is not configured."));
        }

        return Mono.defer(() -> Mono.just(new KeyEncryptionKeyAsyncClient(key)));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public KeyEncryptionKeyClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        builder.credential(credential);

        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    @Override
    public KeyEncryptionKeyClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        builder.httpLogOptions(logOptions);

        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param policy A {@link HttpPipelinePolicy pipeline policy}.
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public KeyEncryptionKeyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        if (policy == null) {
            throw logger.logExceptionAsError(new NullPointerException("'policy' cannot be null."));
        }

        builder.addPolicy(policy);

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param client The {@link HttpClient} to use for requests.
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    @Override
    public KeyEncryptionKeyClientBuilder httpClient(HttpClient client) {
        builder.httpClient(client);

        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    @Override
    public KeyEncryptionKeyClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);

        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the
     * {@link Configuration#getGlobalConfiguration() global configuration store}, use {@link Configuration#NONE} to
     * bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to get configuration details.
     *
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    @Override
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
     *
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    public KeyEncryptionKeyClientBuilder serviceVersion(CryptographyServiceVersion version) {
        builder.serviceVersion(version);

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent. The default retry policy will be used in
     * the pipeline, if not provided.
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy User's retry policy applied to each request.
     *
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    public KeyEncryptionKeyClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        builder.retryPolicy(retryPolicy);

        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryPolicy(RetryPolicy)}.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    @Override
    public KeyEncryptionKeyClientBuilder retryOptions(RetryOptions retryOptions) {
        builder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @see HttpClientOptions
     * @return The updated {@link KeyEncryptionKeyClientBuilder} object.
     */
    @Override
    public KeyEncryptionKeyClientBuilder clientOptions(ClientOptions clientOptions) {
        builder.clientOptions(clientOptions);

        return this;
    }
}
