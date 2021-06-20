// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
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
 * <p><strong>Samples to construct a sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.administration.keyVaultAccessControlClient.instantiation}
 * <p><strong>Samples to construct an async client</strong></p>
 * {@codesnippet com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.instantiation}
 *
 * @see KeyVaultAccessControlClient
 * @see KeyVaultAccessControlAsyncClient
 */
@ServiceClientBuilder(serviceClients = {KeyVaultAccessControlClient.class, KeyVaultAccessControlAsyncClient.class})
public final class KeyVaultAccessControlClientBuilder {
    // This is the properties file name.
    private static final String AZURE_KEY_VAULT_RBAC = "azure-key-vault-administration.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final ClientLogger logger = new ClientLogger(KeyVaultAccessControlClientBuilder.class);
    private final List<HttpPipelinePolicy> perCallPolicies;
    private final List<HttpPipelinePolicy> perRetryPolicies;
    private final Map<String, String> properties;

    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL vaultUrl;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private Configuration configuration;
    private ClientOptions clientOptions;
    private KeyVaultAdministrationServiceVersion serviceVersion;

    /**
     * Creates a {@link KeyVaultAccessControlClientBuilder} instance that is able to configure and construct
     * instances of {@link KeyVaultAccessControlClient} and {@link KeyVaultAccessControlAsyncClient}.
     */
    public KeyVaultAccessControlClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions();
        perCallPolicies = new ArrayList<>();
        perRetryPolicies = new ArrayList<>();
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
     *
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
     *
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

        serviceVersion = serviceVersion != null ? serviceVersion : KeyVaultAdministrationServiceVersion.getLatest();

        if (pipeline != null) {
            return new KeyVaultAccessControlAsyncClient(vaultUrl, pipeline, serviceVersion);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        httpLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, httpLogOptions), clientName,
            clientVersion, buildConfiguration));

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(header ->
                httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        // Add per call additional policies.
        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        // Add retry policy.
        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        policies.add(new KeyVaultCredentialPolicy(credential));

        // Add per retry additional policies.
        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline buildPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new KeyVaultAccessControlAsyncClient(vaultUrl, buildPipeline, serviceVersion);
    }

    /**
     * Sets the URL to the Key Vault on which the client operates. Appears as "DNS Name" in the Azure portal.
     *
     * @param vaultUrl The vault URL is used as destination on Azure to send requests to.
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     *
     * @throws IllegalArgumentException If {@code vaultUrl} cannot be parsed into a valid URL.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public KeyVaultAccessControlClientBuilder vaultUrl(String vaultUrl) {
        if (vaultUrl == null) {
            throw logger.logExceptionAsError(new NullPointerException("'vaultUrl' cannot be null."));
        }

        try {
            this.vaultUrl = new URL(vaultUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Key Vault URL is malformed.", e));
        }

        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public KeyVaultAccessControlClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.credential = credential;

        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     *
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
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public KeyVaultAccessControlClientBuilder addPolicy(HttpPipelinePolicy policy) {
        if (policy == null) {
            throw logger.logExceptionAsError(new NullPointerException("'policy' cannot be null."));
        }

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder httpClient(HttpClient client) {
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
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder pipeline(HttpPipeline pipeline) {
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
     *
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
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;

        return this;
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure
     * the {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core:
     * Telemetry policy</a>
     *
     * @param clientOptions the {@link ClientOptions} to be set on the client.
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }

    /**
     * Sets the {@link KeyVaultAdministrationServiceVersion} that is used when making API requests.
     *
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param serviceVersion {@link KeyVaultAdministrationServiceVersion} of the service API used when making requests.
     *
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     */
    public KeyVaultAccessControlClientBuilder serviceVersion(KeyVaultAdministrationServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;

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
