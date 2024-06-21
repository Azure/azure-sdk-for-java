// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
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
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
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
 * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation -->
 * <pre>
 * CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder&#40;&#41;
 *     .keyIdentifier&#40;&quot;&lt;your-key-id&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation -->
 * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation -->
 * <pre>
 * JsonWebKey jsonWebKey = new JsonWebKey&#40;&#41;.setId&#40;&quot;SampleJsonWebKey&quot;&#41;;
 * CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder&#40;&#41;
 *     .jsonWebKey&#40;jsonWebKey&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation -->
 *
 * <p>When a {@link CryptographyAsyncClient} or {@link CryptographyClient} gets created using a
 * {@code Azure Key Vault key identifier}, the first time a cryptographic operation is attempted, the client will
 * attempt to retrieve the key material from the service, cache it, and perform all future cryptographic operations
 * locally, deferring to the service when that's not possible. If key retrieval and caching fails because of a
 * non-retryable error, the client will not make any further attempts and will fall back to performing all cryptographic
 * operations on the service side. Conversely, when a {@link CryptographyAsyncClient} or {@link CryptographyClient} gets
 * created using a {@link JsonWebKey JSON Web Key}, all cryptographic operations will be performed locally.</p>
 *
 * <p>To ensure correct behavior when performing operations such as {@code Decrypt}, {@code Unwrap} and
 * {@code Verify}, it is recommended to use a {@link CryptographyAsyncClient} or {@link CryptographyClient} created
 * for the specific key version that was used for the corresponding inverse operation: {@code Encrypt},
 * {@code Wrap}, or {@code Sign}, respectively.</p>
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and a custom
 * {@link HttpClient http client} can be optionally configured in the {@link CryptographyClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withHttpClient.instantiation -->
 * <pre>
 * CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder&#40;&#41;
 *     .keyIdentifier&#40;&quot;&lt;your-key-id&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .httpClient&#40;HttpClient.createDefault&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withHttpClient.instantiation -->
 *
 * @see CryptographyAsyncClient
 * @see CryptographyClient
 */
@ServiceClientBuilder(serviceClients = CryptographyClient.class)
public final class CryptographyClientBuilder implements
    TokenCredentialTrait<CryptographyClientBuilder>,
    HttpTrait<CryptographyClientBuilder>,
    ConfigurationTrait<CryptographyClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientBuilder.class);
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    private final List<HttpPipelinePolicy> perCallPolicies;
    private final List<HttpPipelinePolicy> perRetryPolicies;

    private ClientOptions clientOptions;
    private Configuration configuration;
    private CryptographyServiceVersion version;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private JsonWebKey jsonWebKey;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private String keyId;
    private TokenCredential credential;
    private boolean isChallengeResourceVerificationDisabled = false;
    private boolean isKeyCachingDisabled = false;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-key-vault-keys.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    /**
     * The constructor with defaults.
     */
    public CryptographyClientBuilder() {
        httpLogOptions = new HttpLogOptions();
        perCallPolicies = new ArrayList<>();
        perRetryPolicies = new ArrayList<>();
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
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public CryptographyClient buildClient() {
        if (jsonWebKey == null) {
            if (CoreUtils.isNullOrEmpty(keyId)) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "An Azure Key Vault key identifier is required to build the cryptography client if a JSON Web Key"
                        + " is not provided."));
            }

            CryptographyServiceVersion serviceVersion =
                version != null ? version : CryptographyServiceVersion.getLatest();

            if (pipeline != null) {
                return new CryptographyClient(keyId, pipeline, serviceVersion, isKeyCachingDisabled);
            }

            if (credential == null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Azure Key Vault credentials are required to build the cryptography client if a JSON Web Key is not"
                        + " provided."));
            }

            HttpPipeline pipeline = setupPipeline();

            return new CryptographyClient(keyId, pipeline, serviceVersion, isKeyCachingDisabled);
        } else {
            if (isKeyCachingDisabled) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Key caching cannot be disabled when using a JSON Web Key."));
            }

            return new CryptographyClient(jsonWebKey);
        }
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
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public CryptographyAsyncClient buildAsyncClient() {
        if (jsonWebKey == null) {
            if (CoreUtils.isNullOrEmpty(keyId)) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "An Azure Key Vault key identifier is required to build the cryptography client if a JSON Web Key"
                        + " is not provided."));
            }

            CryptographyServiceVersion serviceVersion =
                version != null ? version : CryptographyServiceVersion.getLatest();

            if (pipeline != null) {
                return new CryptographyAsyncClient(keyId, pipeline, serviceVersion, isKeyCachingDisabled);
            }

            if (credential == null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Azure Key Vault credentials are required to build the cryptography client if a JSON Web Key is not"
                        + " provided."));
            }

            HttpPipeline pipeline = setupPipeline();

            return new CryptographyAsyncClient(keyId, pipeline, serviceVersion, isKeyCachingDisabled);
        } else {
            if (isKeyCachingDisabled) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Key caching cannot be disabled when using a JSON Web Key."));
            }

            return new CryptographyAsyncClient(jsonWebKey);
        }
    }

    HttpPipeline setupPipeline() {
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        httpLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        ClientOptions localClientOptions = clientOptions != null ? clientOptions : new ClientOptions();

        policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(localClientOptions, httpLogOptions), CLIENT_NAME,
            CLIENT_VERSION, buildConfiguration));

        List<HttpHeader> httpHeaderList = new ArrayList<>();
        localClientOptions.getHeaders().forEach(header ->
            httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
        policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));

        // Add per call additional policies.
        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        // Add retry policy.
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        policies.add(new KeyVaultCredentialPolicy(credential, isChallengeResourceVerificationDisabled));

        // Add per retry additional policies.
        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        TracingOptions tracingOptions = localClientOptions.getTracingOptions();
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer(CLIENT_NAME, CLIENT_VERSION, KEYVAULT_TRACING_NAMESPACE_VALUE, tracingOptions);

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(tracer)
            .clientOptions(localClientOptions)
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
     * Sets the Azure Key Vault key identifier of the JSON Web Key to be used for cryptography operations. You should
     * validate that this URL references a valid Key Vault or Managed HSM resource. Refer to the following
     * <a href=https://aka.ms/azsdk/blog/vault-uri>documentation</a> for details.
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
            throw LOGGER.logExceptionAsError(new NullPointerException("'keyId' cannot be null."));
        }

        this.keyId = keyId;

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public CryptographyClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
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
            throw LOGGER.logExceptionAsError(new NullPointerException("'jsonWebKey' must not be null."));
        }

        this.jsonWebKey = jsonWebKey;

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
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;

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
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public CryptographyClientBuilder addPolicy(HttpPipelinePolicy policy) {
        if (policy == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'policy' cannot be null."));
        }

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

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
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder httpClient(HttpClient client) {
        this.httpClient = client;

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
     * <p>
     * The {@link #keyIdentifier(String) JSON Web Key identifier} is not ignored when
     * {@code pipeline} is set.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
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
    @Override
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
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
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
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
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
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault or Managed HSM domain. This
     * verification is performed by default.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder disableChallengeResourceVerification() {
        this.isChallengeResourceVerificationDisabled = true;

        return this;
    }

    /**
     * Disables local key caching and defers all cryptographic operations to the service.
     *
     * <p>This method will have no effect if used in conjunction with the
     * {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey)} method.</p>
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder disableKeyCaching() {
        this.isKeyCachingDisabled = true;

        return this;
    }
}
