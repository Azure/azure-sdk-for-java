// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link KeyVaultAccessControlAsyncClient} and {@link KeyVaultAccessControlClient}, by calling
 * {@link KeyVaultAccessControlClientBuilder#buildAsyncClient()} and
 * {@link KeyVaultAccessControlClientBuilder#buildClient()} respectively. It constructs an instance of the desired
 * client.
 *
 * <p> The minimal configuration options required by {@link KeyVaultAccessControlClientBuilder} to build an
 * an {@link KeyVaultAccessControlAsyncClient} are {@link String vaultUrl} and {@link TokenCredential credential}.</p>
 *
 * <p>The {@link HttpLogDetailLevel}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient} can be optionally configured in the {@link KeyVaultAccessControlClientBuilder}.</p>
 *
 * <p>Alternatively, a custom {@link HttpPipeline} with custom {@link HttpPipelinePolicy} policies and {@link String
 * vaultUrl} can be specified. It provides finer control over the construction of
 * {@link KeyVaultAccessControlAsyncClient} and {@link KeyVaultAccessControlClient} instances.</p>
 *
 * <p> The minimal configuration options required by {@link KeyVaultAccessControlClientBuilder} to build an
 * {@link KeyVaultAccessControlClient} are {@link String vaultUrl} and {@link TokenCredential credential}. </p>
 *
 * @see KeyVaultAccessControlAsyncClient
 * @see KeyVaultAccessControlClient
 */
@ServiceClientBuilder(serviceClients = {KeyVaultAccessControlClient.class, KeyVaultAccessControlAsyncClient.class})
public final class KeyVaultAccessControlClientBuilder {
    // This is the properties file name.
    private static final String AZURE_KEY_VAULT_RBAC = "azure-key-vault-administration.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final ClientLogger logger = new ClientLogger(KeyVaultAccessControlClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;
    private final Map<String, String> properties;

    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL vaultUrl;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    /**
     * Creates a {@link KeyVaultAccessControlClientBuilder} instance that is able to configure and construct
     * instances of {@link KeyVaultAccessControlClient} and {@link KeyVaultAccessControlAsyncClient}.
     */
    public KeyVaultAccessControlClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions();
        policies = new ArrayList<>();
        properties = CoreUtils.getProperties(AZURE_KEY_VAULT_RBAC);
    }

    /**
     * Creates an {@link KeyVaultAccessControlClient} based on options set in the Builder. Every time {@code
     * buildClient()} is called a new instance of {@link KeyVaultAccessControlClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline} and
     * {@link #vaultUrl(String) vaultUrl} are used to create the {@link KeyVaultAccessControlClient client}. All other
     * builder settings are ignored.
     *
     * @return An {@link KeyVaultAccessControlClient} with the options set from the builder.
     * @throws NullPointerException If {@code vaultUrl} is {@code null}.
     */
    public KeyVaultAccessControlClient buildClient() {
        return new KeyVaultAccessControlClient(buildAsyncClient());
    }

    /**
     * Creates a {@link KeyVaultAccessControlAsyncClient} based on options set in the Builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link KeyVaultAccessControlAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline} and
     * {@link #vaultUrl(String) endpoint} are used to create the {@link KeyVaultAccessControlAsyncClient client}. All
     * other builder settings are ignored.
     *
     * @return An {@link KeyVaultAccessControlAsyncClient} with the options set from the builder.
     * @throws NullPointerException If {@code vaultUrl} is {@code null}.
     */
    public KeyVaultAccessControlAsyncClient buildAsyncClient() {
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone()
            : configuration;

        URL buildEndpoint = getBuildEndpoint(buildConfiguration);

        if (buildEndpoint == null) {
            throw logger.logExceptionAsError(
                new IllegalStateException(
                    KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED)));
        }

        if (pipeline != null) {
            return new KeyVaultAccessControlAsyncClient(vaultUrl, pipeline);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
        this.policies.add(new KeyVaultCredentialPolicy(credential));
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline buildPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new KeyVaultAccessControlAsyncClient(vaultUrl, buildPipeline);
    }

    /**
     * Sets the URL to the Key Vault on which the client operates. Appears as "DNS Name" in the Azure portal.
     *
     * @param vaultUrl The vault URL is used as destination on Azure to send requests to.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     * @throws IllegalArgumentException If {@code vaultUrl} is null or it cannot be parsed into a valid URL.
     */
    public KeyVaultAccessControlClientBuilder vaultUrl(String vaultUrl) {
        try {
            this.vaultUrl = new URL(vaultUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("The Azure Key Vault URL is malformed.", e));
        }

        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public KeyVaultAccessControlClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential);

        this.credential = credential;

        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;

        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after and {@link KeyVaultAccessControlClient}
     * {@link KeyVaultAccessControlAsyncClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public KeyVaultAccessControlClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);

        policies.add(policy);

        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public KeyVaultAccessControlClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);

        this.httpClient = client;

        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link KeyVaultAccessControlClientBuilder#vaultUrl(String) vaultUrl} to build {@link KeyVaultAccessControlClient}
     * or {@link KeyVaultAccessControlAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to get configuration details.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used in the pipeline, if not provided.
     *
     * @param retryPolicy User's retry policy applied to each request.
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     * @throws NullPointerException If the specified {@code retryPolicy} is null.
     */
    public KeyVaultAccessControlClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        Objects.requireNonNull(retryPolicy, "The retry policy cannot be bull");

        this.retryPolicy = retryPolicy;

        return this;
    }

    private URL getBuildEndpoint(Configuration configuration) {
        if (vaultUrl != null) {
            return vaultUrl;
        }

        String configEndpoint = configuration.get("AZURE_KEYVAULT_ENDPOINT");

        if (CoreUtils.isNullOrEmpty(configEndpoint)) {
            return null;
        }

        try {
            return new URL(configEndpoint);
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}
