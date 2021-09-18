// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
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
import com.azure.security.keyvault.keys.models.KeyVaultKeyIdentifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link KeyAsyncClient
 * secret async client} and {@link KeyClient secret sync client}, by calling
 * {@link KeyClientBuilder#buildAsyncClient() buildAsyncClient} and {@link KeyClientBuilder#buildClient() buildClient}
 * respectively. It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link KeyClientBuilder} to build {@link KeyAsyncClient} are
 * {@link String vaultUrl} and {@link TokenCredential credential}. </p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.instantiation}
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link KeyClientBuilder}.</p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.withhttpclient.instantiation}
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and
 * {@link String vaultUrl} can be specified. It provides finer control over the construction of {@link KeyAsyncClient}
 * and {@link KeyClient}</p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.pipeline.instantiation}
 *
 * <p> The minimal configuration options required by {@link KeyClientBuilder secretClientBuilder} to build {@link
 * KeyClient} are {@link String vaultUrl} and {@link TokenCredential credential}. </p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.keyclient.instantiation}
 *
 * @see KeyAsyncClient
 * @see KeyClient
 */
@ServiceClientBuilder(serviceClients = KeyClient.class)
public final class KeyClientBuilder {
    private final ClientLogger logger = new ClientLogger(KeyClientBuilder.class);
    // This is properties file's name.
    private static final String AZURE_KEY_VAULT_KEYS = "azure-key-vault-keys.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

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
    private KeyServiceVersion version;
    private ClientOptions clientOptions;

    /**
     * The constructor with defaults.
     */
    public KeyClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions();
        perCallPolicies = new ArrayList<>();
        perRetryPolicies = new ArrayList<>();
        properties = CoreUtils.getProperties(AZURE_KEY_VAULT_KEYS);
    }

    /**
     * Creates a {@link KeyClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link KeyClient} is created.
     *
     * <p>If {@link KeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link KeyClientBuilder#vaultUrl(String) vaultUrl} are used to create the {@link KeyClientBuilder client}.
     * All other builder settings are ignored. If {@code pipeline} is not set, then {@link
     * KeyClientBuilder#credential(TokenCredential) key vault credential} and {@link
     * KeyClientBuilder#vaultUrl(String) key vault url} are required to build the {@link KeyClient client}.</p>
     *
     * @return A {@link KeyClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link KeyClientBuilder#credential(TokenCredential)} or
     * {@link KeyClientBuilder#vaultUrl(String)} have not been set.
     */
    public KeyClient buildClient() {
        return new KeyClient(buildAsyncClient());
    }

    /**
     * Creates a {@link KeyAsyncClient} based on options set in the builder.
     * Every time {@code buildAsyncClient()} is called, a new instance of {@link KeyAsyncClient} is created.
     *
     * <p>If {@link KeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link KeyClientBuilder#vaultUrl(String) vaultUrl} are used to create the {@link KeyClientBuilder client}.
     * All other builder settings are ignored. If {@code pipeline} is not set, then {@link
     * KeyClientBuilder#credential(TokenCredential) key vault credential and {@link KeyClientBuilder#vaultUrl(String)}
     * key vault url are required to build the {@link KeyAsyncClient client}.}</p>
     *
     * @return A {@link KeyAsyncClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link KeyClientBuilder#credential(TokenCredential)} or
     * {@link KeyClientBuilder#vaultUrl(String)} have not been set.
     */
    public KeyAsyncClient buildAsyncClient() {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;
        URL buildEndpoint = getBuildEndpoint(buildConfiguration);

        if (buildEndpoint == null) {
            throw logger
                .logExceptionAsError(new IllegalStateException(KeyVaultErrorCodeStrings
                    .getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED)));
        }

        KeyServiceVersion serviceVersion = version != null ? version : KeyServiceVersion.getLatest();

        if (pipeline != null) {
            return new KeyAsyncClient(vaultUrl, pipeline, serviceVersion);
        }

        if (credential == null) {
            throw logger.logExceptionAsError(
                new IllegalStateException(KeyVaultErrorCodeStrings
                    .getErrorString(KeyVaultErrorCodeStrings.CREDENTIAL_REQUIRED)));
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

        policies.add(new BearerTokenAuthenticationPolicy(credential));

        // Add per retry additional policies.
        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new KeyAsyncClient(vaultUrl, pipeline, serviceVersion);
    }

    /**
     * Sets the vault endpoint URL to send HTTP requests to.
     *
     * @param vaultUrl The vault url is used as destination on Azure to send requests to. If you have a key identifier,
     * create a new {@link KeyVaultKeyIdentifier} to parse it and obtain the {@code vaultUrl} and other
     * information.
     *
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws IllegalArgumentException If {@code vaultUrl} cannot be parsed into a valid URL.
     * @throws NullPointerException If {@code vaultUrl} is {@code null}.
     */
    public KeyClientBuilder vaultUrl(String vaultUrl) {
        if (vaultUrl == null) {
            throw logger.logExceptionAsError(new NullPointerException("'vaultUrl' cannot be null."));
        }

        try {
            this.vaultUrl = new URL(vaultUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "The Azure Key Vault url is malformed.", ex));
        }
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     *
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public KeyClientBuilder credential(TokenCredential credential) {
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
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;

        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after {@link KeyAsyncClient} and {@link
     * KeyClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     *
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public KeyClientBuilder addPolicy(HttpPipelinePolicy policy) {
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
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder httpClient(HttpClient client) {
        this.httpClient = client;

        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link KeyClientBuilder#vaultUrl(String) vaultUrl} to build {@link KeyClient} or {@link KeyAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     *
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;

        return this;
    }

    /**
     * Sets the {@link KeyServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link KeyServiceVersion} of the service to be used when making requests.
     *
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder serviceVersion(KeyServiceVersion version) {
        this.version = version;

        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to get configuration details.
     *
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * The default retry policy will be used in the pipeline, if not provided.
     *
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

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
