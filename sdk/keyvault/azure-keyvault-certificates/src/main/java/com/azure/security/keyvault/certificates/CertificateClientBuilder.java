// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link CertificateAsyncClient certificate async client} and {@link CertificateClient certificate sync client},
 * by calling {@link CertificateClientBuilder#buildAsyncClient() buildAsyncClient} and {@link CertificateClientBuilder#buildClient() buildClient} respectively
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link CertificateClientBuilder} to build {@link CertificateAsyncClient}
 * are {@link String endpoint} and {@link TokenCredential credential}. </p>
 *
 * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation}
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link CertificateClientBuilder}.</p>

 * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.withhttpclient.instantiation}
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and {@link String endpoint}
 * can be specified. It provides finer control over the construction of {@link CertificateAsyncClient} and {@link CertificateClient}</p>
 *
 * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.pipeline.instantiation}
 *
 * <p> The minimal configuration options required by {@link CertificateClientBuilder certificateClientBuilder} to build {@link CertificateClient}
 * are {@link String endpoint} and {@link TokenCredential credential}. </p>
 *
 * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.instantiation}
 *
 * @see CertificateAsyncClient
 * @see CertificateClient
 */
@ServiceClientBuilder(serviceClients = {CertificateClient.class, CertificateAsyncClient.class})
public final class CertificateClientBuilder {
    private final ClientLogger logger = new ClientLogger(CertificateClientBuilder.class);

    private final List<HttpPipelinePolicy> policies;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private final RetryPolicy retryPolicy;
    private Configuration configuration;

    /**
     * The constructor with defaults.
     */
    public CertificateClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions();
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link CertificateClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link CertificateClient} is created.
     *
     * <p>If {@link CertificateClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link CertificateClientBuilder#endpoint(String) serviceEndpoint} are used to create the
     * {@link CertificateClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link CertificateClientBuilder#credential(TokenCredential) key vault credential}  and
     * {@link CertificateClientBuilder#endpoint(String) key vault endpoint} are required to build the {@link CertificateClient client}.</p>
     *
     * @return A {@link CertificateClient} with the options set from the builder.
     * @throws IllegalStateException If {@link CertificateClientBuilder#credential(TokenCredential)} or
     * {@link CertificateClientBuilder#endpoint(String)} have not been set.
     */
    public CertificateClient buildClient() {
        return new CertificateClient(buildAsyncClient());
    }

    /**
     * Creates a {@link CertificateAsyncClient} based on options set in the builder.
     * Every time {@code buildAsyncClient()} is called, a new instance of {@link CertificateAsyncClient} is created.
     *
     * <p>If {@link CertificateClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link CertificateClientBuilder#endpoint(String) serviceEndpoint} are used to create the
     * {@link CertificateClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link CertificateClientBuilder#credential(TokenCredential) key vault credential and
     * {@link CertificateClientBuilder#endpoint(String)} key vault endpoint are required to build the {@link CertificateAsyncClient client}.}</p>
     *
     * @return A {@link CertificateAsyncClient} with the options set from the builder.
     * @throws IllegalStateException If {@link CertificateClientBuilder#credential(TokenCredential)} or
     * {@link CertificateClientBuilder#endpoint(String)} have not been set.
     */
    public CertificateAsyncClient buildAsyncClient() {
        Configuration buildConfiguration = (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;
        URL buildEndpoint = getBuildEndpoint(buildConfiguration);

        if (buildEndpoint == null) {
            throw logger.logExceptionAsError(new IllegalStateException(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED)));
        }

        if (pipeline != null) {
            return new CertificateAsyncClient(endpoint, pipeline);
        }

        if (credential == null) {
            throw logger.logExceptionAsError(new IllegalStateException(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.CREDENTIALS_REQUIRED)));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION, buildConfiguration));
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

        return new CertificateAsyncClient(endpoint, pipeline);
    }

    /**
     * Sets the vault endpoint url to send HTTP requests to.
     *
     * @param endpoint The vault endpoint url is used as destination on Azure to send requests to.
     * @return the updated ServiceClientBuilder object.
     * @throws IllegalArgumentException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public CertificateClientBuilder endpoint(String endpoint) {
        try {
            this.endpoint = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Key Vault endpoint url is malformed."));
        }
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link CertificateClientBuilder} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public CertificateClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credential = credential;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated {@link CertificateClientBuilder} object.
     */
    public CertificateClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after {@link CertificateAsyncClient} and {@link CertificateClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link CertificateClientBuilder} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public CertificateClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link CertificateClientBuilder} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public CertificateClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client, "'client' cannot be null.");
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link CertificateClientBuilder#endpoint(String) endpoint} to build {@link CertificateClient} or {@link CertificateAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link CertificateClientBuilder} object.
     */
    public CertificateClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
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
     * @return The updated CertificateClientBuilder object.
     */
    public CertificateClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    private URL getBuildEndpoint(Configuration configuration) {
        if (endpoint != null) {
            return endpoint;
        }

        String configEndpoint = configuration.get("AZURE_KEYVAULT_ENDPOINT");
        if (ImplUtils.isNullOrEmpty(configEndpoint)) {
            return null;
        }

        try {
            return new URL(configEndpoint);
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}
