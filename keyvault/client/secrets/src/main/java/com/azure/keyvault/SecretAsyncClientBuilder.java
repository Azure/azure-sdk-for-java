// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.azure.core.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.TokenCredentialPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link SecretAsyncClient secret async client},
 * calling {@link SecretAsyncClientBuilder#build() build} constructs an instance of the client.
 *
 * <p> The minimal configuration options required by {@link SecretAsyncClientBuilder secretClientBuilder} to build {@link SecretAsyncClient}
 * are {@link String endpoint} and {@link TokenCredential credential}. </p>
 * <pre>
 * SecretAsyncClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credential(keyVaultAsyncCredential)
 *   .build();
 * </pre>
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link SecretAsyncClientBuilder}.</p>
 * <pre>
 * SecretAsyncClient secretAsyncClient = SecretAsyncClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credential(keyVaultAsyncCredential)
 *   .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
 *   .addPolicy(customPolicyOne)
 *   .addPolicy(customPolicyTwo)
 *   .httpClient(client)
 *   .build();
 * </pre>
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and {@link String endpoint}
 * can be specified. It provides finer control over the construction of {@link SecretAsyncClient client}</p>
 * <pre>
 * SecretAsyncClient.builder()
 *   .pipeline(new HttpPipeline(customPoliciesList))
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .build()
 * </pre>
 *
 * @see SecretAsyncClient
 */
public final class SecretAsyncClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL endpoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private RetryPolicy retryPolicy;

    SecretAsyncClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link SecretAsyncClient} based on options set in the builder.
     * Every time {@code build()} is called, a new instance of {@link SecretAsyncClient} is created.
     *
     * <p>If {@link SecretAsyncClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link SecretAsyncClientBuilder#endpoint(String) serviceEndpoint} are used to create the
     * {@link SecretAsyncClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link SecretAsyncClientBuilder#credential(TokenCredential) key vault credential and
     * {@link SecretAsyncClientBuilder#endpoint(String)} key vault endpoint are required to build the {@link SecretAsyncClient client}.}</p>
     *
     * @return A SecretAsyncClient with the options set from the builder.
     * @throws IllegalStateException If {@link SecretAsyncClientBuilder#credential(TokenCredential)} or
     * {@link SecretAsyncClientBuilder#endpoint(String)} have not been set.
     */
    public SecretAsyncClient build() {

        if (endpoint == null) {
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
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION, new Configuration()));
        policies.add(retryPolicy);
        policies.add(new TokenCredentialPolicy(credential));
        policies.addAll(this.policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();

        return new SecretAsyncClient(endpoint, pipeline);
    }

    /**
     * Sets the vault endpoint url to send HTTP requests to.
     *
     * @param endPoint The vault endpoint url is used as destination on Azure to send requests to.
     * @return the updated Builder object.
     * @throws IllegalArgumentException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public SecretAsyncClientBuilder endpoint(String endPoint) {
        try {
            this.endpoint = new URL(endPoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The Azure Key Vault endpoint url is malformed.");
        }
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated Builder object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public SecretAsyncClientBuilder credential(TokenCredential credential) {
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
     * @return the updated Builder object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public SecretAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link SecretAsyncClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated Builder object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public SecretAsyncClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated Builder object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public SecretAsyncClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link SecretAsyncClientBuilder#endpoint(String) endpoint} to build {@link SecretAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link SecretAsyncClientBuilder} object.
     */
    public SecretAsyncClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }
}
