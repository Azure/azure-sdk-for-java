// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

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
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.JsonWebKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link CryptographyAsyncClient} and {@link CryptographyClient}, by calling
 * {@link CryptographyClientBuilder#buildAsyncClient()} and {@link CryptographyClientBuilder#buildClient()} respectively
 * It constructs an instance of the desired client.
 *
 * <p>The minimal configuration options required by {@link CryptographyClientBuilder cryptographyClientBuilder} to build
 * a {@link CryptographyAsyncClient} or a {@link CryptographyClient} are a {@link TokenCredential credential} and either
 * a {@link JsonWebKey JSON Web Key} or a {@code Azure Key Vault key identifier}.</p>
 *
 * <p>To ensure correct behavior when performing operations such as {@code Decrypt}, {@code Unwrap} and
 * {@code Verify}, it is recommended to use a {@link CryptographyAsyncClient} or {@link CryptographyClient} created
 * for the specific key version that was used for the corresponding inverse operation: {@code Encrypt},
 * {@code Wrap}, or {@code Sign}, respectively.</p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation}
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation}
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and a custom
 * {@link HttpClient http client} can be optionally configured in the {@link CryptographyClientBuilder}.</p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withHttpClient.instantiation}
 *
 * <p>Alternatively, a custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies
 * can be specified. It provides finer control over the construction of {@link CryptographyAsyncClient} and
 * {@link CryptographyClient}</p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withPipeline.instantiation}
 *
 * <p>The minimal configuration options required by {@link CryptographyClientBuilder cryptographyClientBuilder} to
 * build {@link CryptographyClient} are {@link JsonWebKey jsonWebKey} or
 * {@link String Azure Key Vault key identifier} and {@link TokenCredential credential}.</p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.instantiation}
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.withJsonWebKey.instantiation}
 *
 * @see CryptographyAsyncClient
 * @see CryptographyClient
 */
@ServiceClientBuilder(serviceClients = CryptographyClient.class)
public final class CryptographyClientBuilder {
    private final ClientLogger logger = new ClientLogger(CryptographyClientBuilder.class);

    // This is properties file's name.
    private static final String AZURE_KEY_VAULT_KEYS = "azure-key-vault-keys.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final List<HttpPipelinePolicy> perCallPolicies;
    private final List<HttpPipelinePolicy> perRetryPolicies;
    private final Map<String, String> properties;

    private ClientOptions clientOptions;
    private Configuration configuration;
    private CryptographyServiceVersion version;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private JsonWebKey jsonWebKey;
    private RetryPolicy retryPolicy;
    private String keyId;
    private TokenCredential credential;

    /**
     * The constructor with defaults.
     */
    public CryptographyClientBuilder() {
        httpLogOptions = new HttpLogOptions();
        perCallPolicies = new ArrayList<>();
        perRetryPolicies = new ArrayList<>();
        properties = CoreUtils.getProperties(AZURE_KEY_VAULT_KEYS);
        retryPolicy = new RetryPolicy();
    }

    /**
     * Creates a {@link CryptographyClient} based on options set in the builder. Every time {@code buildClient()} is
     * called, a new instance of {@link CryptographyClient} is created.
     *
     * <p>If {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) jsonWebKey} is set, then all other builder
     * settings are ignored.</p>
     *
     * <p>If {@link CryptographyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link CryptographyClientBuilder#keyIdentifier(String) jsonWebKey identifier} are used to create the
     * {@link CryptographyClient client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then an {@link CryptographyClientBuilder#credential(TokenCredential) Azure Key Vault credential} and
     * {@link CryptographyClientBuilder#keyIdentifier(String) JSON Web Key identifier} are required to build the
     * {@link CryptographyClient client}.</p>
     *
     * @return A {@link CryptographyClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link CryptographyClientBuilder#credential(TokenCredential)} is {@code null} or
     * {@link CryptographyClientBuilder#keyIdentifier(String)} is empty or {@code null}.
     */
    public CryptographyClient buildClient() {
        return new CryptographyClient(buildAsyncClient());
    }

    /**
     * Creates a {@link CryptographyAsyncClient} based on options set in the builder. Every time
     * {@link #buildAsyncClient()} is called, a new instance of {@link CryptographyAsyncClient} is created.
     *
     * <p>If {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) jsonWebKey} is set, then all other builder
     * settings are ignored.</p>
     *
     * <p>If {@link CryptographyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link CryptographyClientBuilder#keyIdentifier(String) jsonWebKey identifier}) are used to create the
     * {@link CryptographyAsyncClient async client}. All other builder settings are ignored. If {@code pipeline} is
     * not set, then an {@link CryptographyClientBuilder#credential(TokenCredential) Azure Key Vault credential} and
     * {@link CryptographyClientBuilder#keyIdentifier(String) JSON Web Key identifier} are required to build the
     * {@link CryptographyAsyncClient async client}.</p>
     *
     * @return A {@link CryptographyAsyncClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link CryptographyClientBuilder#credential(TokenCredential)} is {@code null} or
     * {@link CryptographyClientBuilder#keyIdentifier(String)} is empty or {@code null}.
     */
    public CryptographyAsyncClient buildAsyncClient() {
        if (jsonWebKey == null) {
            if (Strings.isNullOrEmpty(keyId)) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "An Azure Key Vault key identifier is required to build the cryptography client if a JSON Web Key"
                        + " is not provided."));
            }

            CryptographyServiceVersion serviceVersion = version != null ? version : CryptographyServiceVersion.getLatest();

            if (pipeline != null) {
                return new CryptographyAsyncClient(keyId, pipeline, serviceVersion);
            }

            if (credential == null) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Azure Key Vault credentials are required to build the cryptography client if a JSON Web Key is not"
                        + " provided."));
            }

            HttpPipeline pipeline = setupPipeline();

            return new CryptographyAsyncClient(keyId, pipeline, serviceVersion);
        } else {
            return new CryptographyAsyncClient(jsonWebKey);
        }
    }

    HttpPipeline setupPipeline() {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;

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

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    TokenCredential getCredential() {
        return credential;
    }

    HttpPipeline getPipeline() {
        return pipeline;
    }

    CryptographyServiceVersion getServiceVersion() {
        return version;
    }

    /**
     * Sets the Azure Key Vault key identifier of the JSON Web Key to be used for cryptography operations.
     *
     * <p>To ensure correct behavior when performing operations such as {@code Decrypt}, {@code Unwrap} and
     * {@code Verify}, it is recommended to use a {@link CryptographyAsyncClient} or {@link CryptographyClient} created
     * for the specific key version that was used for the corresponding inverse operation: {@code Encrypt}
     * {@code Wrap}, or {@code Sign}, respectively.</p>
     *
     * @param keyId The Azure Key Vault key identifier of the JSON Web Key stored in the key vault.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code keyId} is {@code null}.
     */
    public CryptographyClientBuilder keyIdentifier(String keyId) {
        if (keyId == null) {
            throw logger.logExceptionAsError(new NullPointerException("'keyId' cannot be null."));
        }

        this.keyId = keyId;

        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public CryptographyClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.credential = credential;

        return this;
    }

    /**
     * Sets the {@link JsonWebKey} to be used for local cryptography operations.
     *
     * <p>If {@code jsonWebKey} is provided, then all other builder settings are ignored.</p>
     *
     * @param jsonWebKey The JSON Web Key to be used for local cryptography operations.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code jsonWebKey} is {@code null}.
     */
    public CryptographyClientBuilder jsonWebKey(JsonWebKey jsonWebKey) {
        if (jsonWebKey == null) {
            throw logger.logExceptionAsError(new NullPointerException("'jsonWebKey' must not be null."));
        }

        this.jsonWebKey = jsonWebKey;

        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p>If {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;

        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after the client required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public CryptographyClientBuilder addPolicy(HttpPipelinePolicy policy) {
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
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder httpClient(HttpClient client) {
        this.httpClient = client;

        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link CryptographyClientBuilder#keyIdentifier(String) JSON Web Key identifier}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;

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
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

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
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder serviceVersion(CryptographyServiceVersion version) {
        this.version = version;

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent. The default retry policy will be used in
     * the pipeline, if not provided.
     *
     * @param retryPolicy User's {@link RetryPolicy} applied to each request.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;

        return this;
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure the
     * {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core:
     * Telemetry policy</a>
     *
     * @param clientOptions The {@link ClientOptions} to be set on the client.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }
}
