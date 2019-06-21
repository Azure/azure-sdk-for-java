// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;


/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link KeyClient key client},
 * calling {@link KeyClientBuilder#build() build} constructs an instance of the client.
 *
 * <p> The minimal configuration options required by {@link KeyClientBuilder keyClientBuilder} to build {@link KeyClient}
 * are {@link String endpoint} and {@link TokenCredential credential}. </p>
 * <pre>
 * KeyClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credential(keyVaultCredential)
 *   .build();
 * </pre>
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link KeyClientBuilder}.</p>
 * <pre>
 * KeyClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credential(keyVaultCredential)
 *   .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
 *   .addPolicy(customPolicyOne)
 *   .addPolicy(customPolicyTwo)
 *   .httpClient(client)
 *   .build();
 * </pre>
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and {@link String endpoint}
 * can be specified. It provides finer control over the construction of {@link KeyClient client}</p>
 * <pre>
 * KeyClient.builder()
 *   .pipeline(new HttpPipeline(customPoliciesList))
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .build()
 * </pre>
 *
 * @see KeyClient
 * */
public final class KeyClientBuilder {
    private KeyAsyncClientBuilder builder;

    KeyClientBuilder() {
        this.builder = KeyAsyncClient.builder();
    }

    /**
     * Creates a {@link KeyClient} based on options set in the builder.
     * Every time {@code build()} is called, a new instance of {@link KeyClient} is created.
     *
     * <p>If {@link KeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link KeyClientBuilder#endpoint(String) serviceEndpoint} are used to create the
     * {@link KeyClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link KeyClientBuilder#credential(TokenCredential) key vault credential and
     * {@link KeyClientBuilder#endpoint(String)} key vault endpoint are required to build the {@link KeyClient client}.}</p>
     *
     * @return A KeyClient with the options set from the builder.
     * @throws IllegalStateException If {@link KeyClientBuilder#credential(TokenCredential)} or
     * {@link KeyClientBuilder#endpoint(String)} have not been set.
     */
    public KeyClient build() {
        return new KeyClient(builder.build());
    }

    /**
     * Sets the vault endpoint url to send HTTP requests to.
     *
     * @param endpoint The vault endpoint url is used as destination on Azure to send requests to.
     * @return the updated Builder object.
     * @throws IllegalArgumentException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public KeyClientBuilder endpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated Builder object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public KeyClientBuilder credential(TokenCredential credential) {
        builder.credential(credential);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * <p>logLevel is optional. If not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return the updated Builder object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public KeyClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link KeyClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated Builder object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public KeyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        builder.addPolicy(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated Builder object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public KeyClientBuilder httpClient(HttpClient client) {
        builder.httpClient(client);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link KeyClientBuilder#endpoint(String) endpoint} to build {@link KeyClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);
        return this;
    }
}
