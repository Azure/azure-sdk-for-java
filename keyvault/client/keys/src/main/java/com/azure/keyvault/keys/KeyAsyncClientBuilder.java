// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.credentials.AsyncServiceClientCredentials;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.policy.AsyncCredentialsPolicy;
import com.azure.core.http.policy.RetryPolicy;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link KeyAsyncClient secret async client},
 * calling {@link KeyAsyncClientBuilder#build() build} constructs an instance of the client.
 *
 * <p> The minimal configuration options required by {@link KeyAsyncClientBuilder secretClientBuilder} to build {@link KeyAsyncClient}
 * are {@link String endpoint} and {@link AsyncServiceClientCredentials credentials}. </p>
 * <pre>
 * KeyAsyncClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credentials(keyVaultAsyncCredentials)
 *   .build();
 * </pre>
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link KeyAsyncClientBuilder}.</p>
 * <pre>
 * KeyAsyncClient secretAsyncClient = KeyAsyncClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credentials(keyVaultAsyncCredentials)
 *   .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
 *   .addPolicy(customPolicyOne)
 *   .addPolicy(customPolicyTwo)
 *   .httpClient(client)
 *   .build();
 * </pre>
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and {@link String endpoint}
 * can be specified. It provides finer control over the construction of {@link KeyAsyncClient client}</p>
 * <pre>
 * KeyAsyncClient.builder()
 *   .pipeline(new HttpPipeline(customPoliciesList))
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .build()
 * </pre>
 *
 * @see KeyAsyncClient
 */
public final class KeyAsyncClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private AsyncServiceClientCredentials credentials;
    private HttpPipeline pipeline;
    private URL endpoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private RetryPolicy retryPolicy;

    KeyAsyncClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link KeyAsyncClient} based on options set in the builder.
     * Every time {@code build()} is called, a new instance of {@link KeyAsyncClient} is created.
     *
     * <p>If {@link KeyAsyncClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link KeyAsyncClientBuilder#endpoint(String) serviceEndpoint} are used to create the
     * {@link KeyAsyncClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link KeyAsyncClientBuilder#credentials(AsyncServiceClientCredentials) key vault credentials and
     * {@link KeyAsyncClientBuilder#endpoint(String)} key vault endpoint are required to build the {@link KeyAsyncClient client}.}</p>
     *
     * @return A KeyAsyncClient with the options set from the builder.
     * @throws IllegalStateException If {@link KeyAsyncClientBuilder#credentials(AsyncServiceClientCredentials)} or
     * {@link KeyAsyncClientBuilder#endpoint(String)} have not been set.
     */
    public KeyAsyncClient build() {

        if (endpoint == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        }

        if (pipeline != null) {
            return new KeyAsyncClient(endpoint, pipeline);
        }

        if (credentials == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.CREDENTIALS_REQUIRED));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION));
        policies.add(retryPolicy);
        policies.add(new AsyncCredentialsPolicy(getAsyncTokenCredentials()));
        policies.addAll(this.policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = httpClient == null
            ? new HttpPipeline(policies)
            : new HttpPipeline(httpClient, policies);

        return new KeyAsyncClient(endpoint, pipeline);
    }

    /**
     * Sets the vault endpoint url to send HTTP requests to.
     *
     * @param endPoint The vault endpoint url is used as destination on Azure to send requests to.
     * @return the updated Builder object.
     * @throws IllegalStateException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public KeyAsyncClientBuilder endpoint(String endPoint) {
        try {
            this.endpoint = new URL(endPoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The Azure Key Vault endpoint url is malformed.");
        }
        return this;
    }

    /**
     * Sets the credentials to use when authenticating HTTP requests.
     *
     * @param credentials The credentials to use for authenticating HTTP requests.
     * @return the updated Builder object.
     * @throws NullPointerException if {@code credentials} is {@code null}.
     */
    public KeyAsyncClientBuilder credentials(AsyncServiceClientCredentials credentials) {
        Objects.requireNonNull(credentials);
        this.credentials = credentials;
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
    public KeyAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link KeyAsyncClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated Builder object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public KeyAsyncClientBuilder addPolicy(HttpPipelinePolicy policy) {
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
    public KeyAsyncClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link KeyAsyncClientBuilder#endpoint(String) endpoint} to build {@link KeyAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link KeyAsyncClientBuilder} object.
     */
    public KeyAsyncClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    private AsyncTokenCredentials getAsyncTokenCredentials() {
        AsyncTokenCredentials asyncTokenCredentials = credentials != null ? new AsyncTokenCredentials("Bearer", credentials.authorizationHeaderValueAsync(new HttpRequest(HttpMethod.POST, endpoint))) : null;
        return asyncTokenCredentials;
    }

    private class AsyncTokenCredentials implements AsyncServiceClientCredentials {

        private String scheme;
        private Mono<String> token;

        AsyncTokenCredentials(String scheme, Mono<String> token) {
            this.scheme = scheme;
            this.token = token;
        }

        @Override
        public Mono<String> authorizationHeaderValueAsync(HttpRequest httpRequest) {
            if (scheme == null) {
                scheme = "Bearer";
            }
            return token.flatMap(tokenValue -> Mono.just("Bearer " + tokenValue));
        }
    }
}
