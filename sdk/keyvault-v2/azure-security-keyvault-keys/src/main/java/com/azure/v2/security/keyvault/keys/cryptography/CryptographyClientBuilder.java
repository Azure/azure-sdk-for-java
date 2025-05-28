// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.traits.TokenCredentialTrait;
import com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyClientImpl;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import io.clientcore.core.annotations.ServiceClientBuilder;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRedirectOptions;
import io.clientcore.core.http.pipeline.HttpRedirectPolicy;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.http.pipeline.UserAgentOptions;
import io.clientcore.core.http.pipeline.UserAgentPolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.traits.ConfigurationTrait;
import io.clientcore.core.traits.HttpTrait;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link CryptographyClient} by calling {@link CryptographyClientBuilder#buildClient()}. It constructs an instance of
 * the desired client.
 *
 * <p>The {@link CryptographyClient} provides methods to perform cryptographic operations using asymmetric and symmetric
 * keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify operations using the configured
 * key.</p>
 *
 * <p>The minimal configuration options required by {@link CryptographyClientBuilder} to build a
 * {@link CryptographyClient} are a {@link TokenCredential credential} and either a {@link JsonWebKey JSON Web Key} or
 * an {@code Azure Key Vault key identifier}.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 * <pre>
 * CryptographyClient cryptographyClient = new CryptographyClientBuilder&#40;&#41;
 *     .keyIdentifier&#40;&quot;&lt;your-key-id-from-keyvault&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 *
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withJsonWebKey -->
 * <pre>
 * CryptographyClient cryptographyClient = new CryptographyClientBuilder&#40;&#41;
 *     .jsonWebKey&#40;myJsonWebKey&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withJsonWebKey -->
 *
 * <p>When a {@link CryptographyClient} gets created using a {@code Azure Key Vault key identifier}, the first time a
 * cryptographic operation is attempted, the client will attempt to retrieve the key material from the service, cache
 * it, and perform all future cryptographic operations locally, deferring to the service when that's not possible. If
 * key retrieval and caching fails because of a non-retryable error, the client will not make any further attempts and
 * will fall back to performing all cryptographic operations on the service side. Conversely, when a
 * {@link CryptographyClient} gets created using a {@link JsonWebKey JSON Web Key}, all cryptographic operations will be
 * performed locally.</p>
 *
 * <p>To ensure correct behavior when performing operations such as {@code Decrypt}, {@code Unwrap} and
 * {@code Verify}, it is recommended to use a {@link CryptographyClient} created for the specific key version that was
 * used for the corresponding inverse operation: {@code Encrypt}, {@code Wrap}, or {@code Sign}, respectively.</p>
 *
 * <p>The {@link HttpInstrumentationOptions.HttpLogLevel log level}, multiple custom {@link HttpPipelinePolicy policies}
 * and custom {@link HttpClient HTTP client} can be optionally configured in the {@link CryptographyClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withHttpClient -->
 * <pre>
 * CryptographyClient cryptographyClient = new CryptographyClientBuilder&#40;&#41;
 *     .keyIdentifier&#40;&quot;&lt;your-key-id-from-keyvault&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .httpInstrumentationOptions&#40;new HttpInstrumentationOptions&#40;&#41;
 *         .setHttpLogLevel&#40;HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS&#41;&#41;
 *     .httpClient&#40;HttpClient.getSharedInstance&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withHttpClient -->
 *
 * @see CryptographyClient
 */
@ServiceClientBuilder(serviceClients = CryptographyClient.class)
public final class CryptographyClientBuilder implements ConfigurationTrait<CryptographyClientBuilder>,
    HttpTrait<CryptographyClientBuilder>, TokenCredentialTrait<CryptographyClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientBuilder.class);
    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    // TODO (vcolin7): Figure out where to set when the tracing namespace.
    // private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-security-keyvault-keys.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    private final List<HttpPipelinePolicy> pipelinePolicies;

    private TokenCredential credential;
    private HttpClient httpClient;
    private HttpInstrumentationOptions instrumentationOptions;
    private HttpRedirectOptions redirectOptions;
    private HttpRetryOptions retryOptions;
    private Configuration configuration;
    private CryptographyServiceVersion version;
    private JsonWebKey jsonWebKey;
    private String keyId;
    private boolean disableChallengeResourceVerification = false;
    private boolean isKeyCachingDisabled = false;

    /**
     * Creates a {@link CryptographyClientBuilder} that is used to configure and create {@link CryptographyClient}
     * instances.
     */
    public CryptographyClientBuilder() {
        pipelinePolicies = new ArrayList<>();
    }

    /**
     * Creates a {@link CryptographyClient} based on options set in the builder. Every time {@code buildClient()} is
     * called, a new instance of {@link CryptographyClient} is created.
     *
     * <p>If a {@link CryptographyClientBuilder#jsonWebKey(JsonWebKey) jsonWebKey} is set, then all other builder
     * settings are ignored.</p>
     *
     * @return A {@link CryptographyClient} based on the options set in this builder.
     *
     * @throws IllegalStateException If a {@link CryptographyClientBuilder#keyIdentifier(String) key identifier} has not
     * been set or if either of a {@link CryptographyClientBuilder#credential(TokenCredential) credential} was not
     * provided.
     */
    public CryptographyClient buildClient() {
        if (jsonWebKey != null) {
            if (isKeyCachingDisabled) {
                throw LOGGER.throwableAtError()
                    .log("Key caching cannot be disabled when using a JSON Web Key.", IllegalStateException::new);
            }

            return new CryptographyClient(jsonWebKey);
        }

        if (isNullOrEmpty(keyId)) {
            throw LOGGER.throwableAtError()
                .log(
                    "An Azure Key Vault or Managed HSM key identifier is required to build the cryptography client if a"
                        + " JSON Web Key is not provided.",
                    IllegalStateException::new);
        }

        CryptographyServiceVersion serviceVersion = version != null ? version : CryptographyServiceVersion.getLatest();

        if (credential == null) {
            throw LOGGER.throwableAtError()
                .log("A credential object is required. You can set one by using the"
                    + " CryptographyClientBuilder.credential() method.", IllegalStateException::new);
        }

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        Configuration buildConfiguration
            = configuration == null ? Configuration.getGlobalConfiguration() : configuration;
        UserAgentOptions userAgentOptions
            = new UserAgentOptions().setApplicationId(buildConfiguration.get("application.id"))
                .setSdkName(CLIENT_NAME)
                .setSdkVersion(CLIENT_VERSION);

        policies.add(new UserAgentPolicy(userAgentOptions));
        policies.add(redirectOptions == null ? new HttpRedirectPolicy() : new HttpRedirectPolicy(redirectOptions));
        policies.add(retryOptions == null ? new HttpRetryPolicy() : new HttpRetryPolicy(retryOptions));
        policies.addAll(pipelinePolicies);
        policies.add(new KeyVaultCredentialPolicy(credential, disableChallengeResourceVerification));

        HttpInstrumentationOptions instrumentationOptions
            = this.instrumentationOptions == null ? new HttpInstrumentationOptions() : this.instrumentationOptions;

        policies.add(new HttpInstrumentationPolicy(instrumentationOptions));

        HttpPipelineBuilder httpPipelineBuilder = new HttpPipelineBuilder();

        // Add all policies to the pipeline.
        policies.forEach(httpPipelineBuilder::addPolicy);

        HttpPipeline builtPipeline = httpPipelineBuilder.httpClient(httpClient).build();

        return new CryptographyClient(new CryptographyClientImpl(keyId, builtPipeline, serviceVersion),
            isKeyCachingDisabled);
    }

    TokenCredential getCredential() {
        return credential;
    }

    CryptographyServiceVersion getServiceVersion() {
        return version;
    }

    /**
     * Sets the Azure Key Vault or Managed HSM  key identifier of the JSON Web Key to be used for cryptography
     * operations. You should validate that this URL references a valid Key Vault or Managed HSM resource. Refer to the
     * following <a href=https://aka.ms/azsdk/blog/vault-uri>documentation</a> for details.
     *
     * <p>To ensure correct behavior when performing operations such as {@code Decrypt}, {@code Unwrap} and
     * {@code Verify}, it is recommended to use a {@link CryptographyClient} created for the specific key version that
     * was used for the corresponding inverse operation: {@code Encrypt} {@code Wrap}, or {@code Sign}, respectively.
     * </p>
     *
     * @param keyId The Azure Key Vault key identifier of the JSON Web Key stored in the key vault.
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws IllegalArgumentException If {@code keyId} is {@code null}.
     */
    public CryptographyClientBuilder keyIdentifier(String keyId) {
        if (isNullOrEmpty(keyId)) {
            throw LOGGER.throwableAtError().log("'keyId' cannot be null or empty.", IllegalArgumentException::new);
        }

        this.keyId = keyId;

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a> documentation for more details
     * on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public CryptographyClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
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
        Objects.requireNonNull(jsonWebKey, "'jsonWebKey' cannot be null.");
        this.jsonWebKey = jsonWebKey;

        return this;
    }

    /**
     * Sets the {@link HttpInstrumentationOptions instrumentation configuration} to use when recording telemetry about
     * HTTP requests sent to the service and responses received from it.
     * <p>
     * By default, when instrumentation options are not provided (explicitly or via environment variables), the
     * following defaults are used:
     * <ul>
     *     <li>Detailed HTTP logging about requests and responses is disabled</li>
     *     <li>Distributed tracing is enabled. If OpenTelemetry is found on the classpath, HTTP requests are
     *     captured as OpenTelemetry spans.
     *     If OpenTelemetry is not found on the classpath, the same information is captured in logs.
     *     HTTP request spans contain basic information about the request, such as the HTTP method, URL, status code and
     *     duration.
     *     See {@link HttpInstrumentationPolicy} for
     *     the details.</li>
     * </ul>
     *
     * @param instrumentationOptions The {@link HttpInstrumentationOptions configuration} to use when recording
     * telemetry about HTTP requests sent to the service and responses received from it.
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder httpInstrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
        this.instrumentationOptions = instrumentationOptions;

        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return The updated {@link CryptographyClientBuilder} object.
     *
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public CryptographyClientBuilder addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null.");
        pipelinePolicies.add(pipelinePolicy);

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
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
     * Sets the {@link CryptographyServiceVersion service version} that is used when making API requests.
     *
     * <p>If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.</p>
     *
     * @param version {@link CryptographyServiceVersion} of the service API used when making requests.
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder serviceVersion(CryptographyServiceVersion version) {
        this.version = version;

        return this;
    }

    /**
     * Sets the {@link HttpRetryOptions} for all the requests made through the client.
     *
     * @param retryOptions The {@link HttpRetryOptions} to use for all the requests made through the client.
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder httpRetryOptions(HttpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;

        return this;
    }

    /**
     * Sets the client-specific configuration used to retrieve client or global configuration properties when building a
     * client.
     *
     * @param configuration Configuration store used to retrieve client configurations.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link HttpRedirectOptions} for all the requests made through the client.
     *
     * @param redirectOptions The {@link HttpRedirectOptions} to use for all the requests made through the client.
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    @Override
    public CryptographyClientBuilder httpRedirectOptions(HttpRedirectOptions redirectOptions) {
        this.redirectOptions = redirectOptions;

        return this;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault domain. This verification is
     * performed by default.
     *
     * @return The updated {@link CryptographyClientBuilder} object.
     */
    public CryptographyClientBuilder disableChallengeResourceVerification() {
        this.disableChallengeResourceVerification = true;

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
