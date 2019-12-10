// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.util.Configuration;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * SecretAsyncClient secret async client} and {@link SecretClient secret client},
 * by calling {@link SecretClientBuilder#buildAsyncClient() buildAsyncClient} and {@link
 * SecretClientBuilder#buildClient() buildClient} respectively.
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link SecretClientBuilder secretClientBuilder} to build
 * {@link SecretAsyncClient} are {@link String vaultUrl} and {@link TokenCredential credential}. </p>
 *
 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.construct}
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.secretclient.sync.construct}
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link SecretClientBuilder}.</p>
 *
 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.withhttpclient.instantiation}
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and
 * {@link String vaultUrl}
 * can be specified. It provides finer control over the construction of {@link SecretAsyncClient client}</p>
 *
 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.pipeline.instantiation}
 *
 * @see SecretClient
 * @see SecretAsyncClient
 */
@ServiceClientBuilder(serviceClients = SecretClient.class)
public final class SecretClientBuilder {
    private final ClientLogger logger = new ClientLogger(SecretClientBuilder.class);
    // This is properties file's name.
    private static final String AZURE_KEY_VAULT_SECRETS = "azure-key-vault-secrets.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final List<HttpPipelinePolicy> policies;
    final Map<String, String> properties;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL vaultUrl;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private final RetryPolicy retryPolicy;
    private Configuration configuration;
    private SecretServiceVersion version;

    /**
     * The constructor with defaults.
     */
    public SecretClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions();
        policies = new ArrayList<>();
        properties = CoreUtils.getProperties(AZURE_KEY_VAULT_SECRETS);
    }

    /**
     * Creates a {@link SecretClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link SecretClient} is created.
     *
     * <p>If {@link SecretClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link SecretClientBuilder#vaultUrl(String) serviceEndpoint} are used to create the
     * {@link SecretClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link SecretClientBuilder#credential(TokenCredential) key vault credential}, and
     * {@link SecretClientBuilder#vaultUrl(String)} key vault url are required to build the {@link SecretClient
     * client}.</p>
     *
     * @return A {@link SecretClient} with the options set from the builder.
     * @throws IllegalStateException If {@link SecretClientBuilder#credential(TokenCredential)} or
     *     {@link SecretClientBuilder#vaultUrl(String)} have not been set.
     */
    public SecretClient buildClient() {
        return new SecretClient(buildAsyncClient());
    }

    /**
     * Creates a {@link SecretAsyncClient} based on options set in the builder.
     * Every time {@code buildAsyncClient()} is called, a new instance of {@link SecretAsyncClient} is created.
     *
     * <p>If {@link SecretClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link SecretClientBuilder#vaultUrl(String) serviceEndpoint} are used to create the
     * {@link SecretClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link SecretClientBuilder#credential(TokenCredential) key vault credential}, and
     * {@link SecretClientBuilder#vaultUrl(String)} key vault url are required to build the {@link
     * SecretAsyncClient client}.</p>
     *
     * @return A {@link SecretAsyncClient} with the options set from the builder.
     * @throws IllegalStateException If {@link SecretClientBuilder#credential(TokenCredential)} or
     *     {@link SecretClientBuilder#vaultUrl(String)} have not been set.
     */
    public SecretAsyncClient buildAsyncClient() {

        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;
        URL buildEndpoint = getBuildEndpoint(buildConfiguration);

        if (buildEndpoint == null) {
            throw logger.logExceptionAsError(
                new IllegalStateException(
                    KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED)));
        }
        SecretServiceVersion serviceVersion = version != null ? version : SecretServiceVersion.getLatest();

        if (pipeline != null) {
            return new SecretAsyncClient(vaultUrl, pipeline, serviceVersion);
        }

        if (credential == null) {
            throw logger.logExceptionAsError(
                new IllegalStateException(
                    KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.CREDENTIAL_REQUIRED)));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy);
        policies.add(new KeyVaultCredentialPolicy(credential));
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new SecretAsyncClient(vaultUrl, pipeline, serviceVersion);
    }

    /**
     * Sets the vault url to send HTTP requests to.
     *
     * @param vaultUrl The vault url is used as destination on Azure to send requests to.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws IllegalArgumentException if {@code vaultUrl} is null or it cannot be parsed into a valid URL.
     */
    public SecretClientBuilder vaultUrl(String vaultUrl) {
        try {
            this.vaultUrl = new URL(vaultUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "The Azure Key Vault url is malformed.", e));
        }
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public SecretClientBuilder credential(TokenCredential credential) {
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
     * @return the updated {@link SecretClientBuilder} object.
     */
    public SecretClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link SecretAsyncClient} or {@link SecretClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public SecretClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link SecretClientBuilder} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public SecretClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link SecretClientBuilder#vaultUrl(String) vaultUrl} to build {@link SecretAsyncClient} or {@link SecretClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link SecretClientBuilder} object.
     */
    public SecretClientBuilder pipeline(HttpPipeline pipeline) {
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
     * @return The updated SecretClientBuilder object.
     */
    public SecretClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link SecretServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link SecretServiceVersion} of the service API used when making requests.
     * @return The updated SecretClientBuilder object.
     */
    public SecretClientBuilder serviceVersion(SecretServiceVersion version) {
        this.version = version;
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
