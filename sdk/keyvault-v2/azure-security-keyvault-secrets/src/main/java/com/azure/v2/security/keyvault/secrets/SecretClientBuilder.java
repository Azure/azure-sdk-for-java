// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.traits.TokenCredentialTrait;
import com.azure.v2.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.secrets.implementation.SecretClientImpl;
import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecretIdentifier;
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
import io.clientcore.core.traits.EndpointTrait;
import io.clientcore.core.traits.HttpTrait;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link SecretClient secret client}, by calling {@link SecretClientBuilder#buildClient() buildClient}. It constructs
 * an instance of the desired client.
 *
 * <p>The {@link SecretClient} provides methods to manage {@link KeyVaultSecret secrets} in Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring, and listing
 * {@link KeyVaultSecret secrets}. The client also supports listing {@link DeletedSecret deleted secrets} for a
 * soft-delete enabled key vault.</p>
 *
 * <p>The minimal configuration options required by {@link SecretClientBuilder} to build a {@link SecretClient} are an
 * {@link String endpoint} and {@link TokenCredential credential}.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.secrets.SecretClient.instantiation -->
 * <pre>
 * SecretClient secretClient = new SecretClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .endpoint&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.secrets.SecretClient.instantiation -->
 *
 * <p>The {@link HttpInstrumentationOptions.HttpLogLevel log level}, multiple custom {@link HttpPipelinePolicy policies}
 * and custom {@link HttpClient HTTP client} can be optionally configured in the {@link SecretClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.secrets.SecretClient.instantiation.withHttpClient -->
 * <pre>
 * SecretClient secretClient = new SecretClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .httpInstrumentationOptions&#40;new HttpInstrumentationOptions&#40;&#41;
 *         .setHttpLogLevel&#40;HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS&#41;&#41;
 *     .httpClient&#40;HttpClient.getSharedInstance&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.secrets.SecretClient.instantiation.withHttpClient -->
 *
 * @see SecretClient
 */
@ServiceClientBuilder(serviceClients = SecretClient.class)
public final class SecretClientBuilder implements ConfigurationTrait<SecretClientBuilder>,
    EndpointTrait<SecretClientBuilder>, HttpTrait<SecretClientBuilder>, TokenCredentialTrait<SecretClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(SecretClientBuilder.class);
    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    // TODO (vcolin7): Figure out where to set when the tracing namespace.
    // private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-security-keyvault-secrets.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    private final List<HttpPipelinePolicy> pipelinePolicies;
    private TokenCredential credential;
    private String endpoint;
    private HttpClient httpClient;
    private HttpInstrumentationOptions instrumentationOptions;
    private HttpRedirectOptions redirectOptions;
    private HttpRetryOptions retryOptions;
    private Configuration configuration;
    private SecretServiceVersion version;
    private boolean disableChallengeResourceVerification = false;

    /**
     * Creates a {@link SecretClientBuilder} that is used to configure and create {@link SecretClient} instances.
     */
    public SecretClientBuilder() {
        pipelinePolicies = new ArrayList<>();
    }

    /**
     * Creates a {@link SecretClient} based on options set in the builder. Every time {@code buildClient()} is called,
     * a new instance of {@link SecretClient} is created.
     *
     * @return A {@link SecretClient} based on the options set in this builder.
     *
     * @throws IllegalStateException If an {@link SecretClientBuilder#endpoint(String) endpoint} has not been set or if
     * either of a {@link SecretClientBuilder#credential(TokenCredential) credential} was not provided.
     */
    public SecretClient buildClient() {
        return new SecretClient(buildImplClient());
    }

    private SecretClientImpl buildImplClient() {
        Configuration configuration
            = this.configuration == null ? Configuration.getGlobalConfiguration() : this.configuration;

        String endpoint = getEndpoint(configuration);

        if (endpoint == null) {
            throw LOGGER.throwableAtError()
                .log(
                    "An Azure Key Vault endpoint is required. You can set one by using the KeyClientBuilder.endpoint()"
                        + "method or by setting the environment variable 'AZURE_KEYVAULT_ENDPOINT'.",
                    IllegalStateException::new);
        }

        SecretServiceVersion version = this.version == null ? SecretServiceVersion.getLatest() : this.version;

        if (credential == null) {
            throw LOGGER.throwableAtError()
                .log(
                    "A credential object is required. You can set one by using the KeyClientBuilder.credential() method.",
                    IllegalStateException::new);
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

        return new SecretClientImpl(builtPipeline, endpoint, version);
    }

    /**
     * Sets the vault endpoint URL to send HTTP requests to. You should validate that this URL references a valid Key
     * Vault resource. Refer to the following <a href=https://aka.ms/azsdk/blog/vault-uri>documentation</a> for details.
     *
     * @param endpoint The endpoint is used as destination on Azure to send requests to. If you have a secret
     * identifier, create a new {@link KeyVaultSecretIdentifier} to parse it and obtain the {@code endpoint} and other
     * information via {@link KeyVaultSecretIdentifier#getEndpoint()}.
     * @return The updated {@link SecretClientBuilder} object.
     *
     * @throws IllegalArgumentException If {@code endpoint} isn't a valid URI.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     */
    @Override
    public SecretClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        try {
            URI uri = new URI(endpoint);
            this.endpoint = uri.toString();
        } catch (URISyntaxException e) {
            throw LOGGER.throwableAtError()
                .log("The Azure Key Vault endpoint is malformed.", e, IllegalArgumentException::new);
        }

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a> documentation for more details
     * on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link SecretClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public SecretClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");

        this.credential = credential;

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
     * @return The updated {@link SecretClientBuilder} object.
     */
    @Override
    public SecretClientBuilder httpInstrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
        this.instrumentationOptions = instrumentationOptions;

        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return The updated {@link SecretClientBuilder} object.
     *
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public SecretClientBuilder addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null.");
        pipelinePolicies.add(pipelinePolicy);

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * @param client The {@link HttpClient} to use for requests.
     * @return The updated {@link SecretClientBuilder} object.
     */
    @Override
    public SecretClientBuilder httpClient(HttpClient client) {
        this.httpClient = client;

        return this;
    }

    /**
     * Sets the client-specific configuration used to retrieve client or global configuration properties when building a
     * client.
     *
     * @param configuration Configuration store used to retrieve client configurations.
     * @return The updated {@link SecretClientBuilder} object.
     */
    @Override
    public SecretClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link SecretServiceVersion service version} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link SecretServiceVersion} of the service API used when making requests.
     * @return The updated {@link SecretClientBuilder} object.
     */
    public SecretClientBuilder serviceVersion(SecretServiceVersion version) {
        this.version = version;

        return this;
    }

    /**
     * Sets the {@link HttpRetryOptions} for all the requests made through the client.
     *
     * @param retryOptions The {@link HttpRetryOptions} to use for all the requests made through the client.
     * @return The updated {@link SecretClientBuilder} object.
     */
    @Override
    public SecretClientBuilder httpRetryOptions(HttpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;

        return this;
    }

    /**
     * Sets the {@link HttpRedirectOptions} for all the requests made through the client.
     *
     * @param redirectOptions The {@link HttpRedirectOptions} to use for all the requests made through the client.
     * @return The updated {@link SecretClientBuilder} object.
     */
    @Override
    public SecretClientBuilder httpRedirectOptions(HttpRedirectOptions redirectOptions) {
        this.redirectOptions = redirectOptions;

        return this;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault domain. This verification is
     * performed by default.
     *
     * @return The updated {@link SecretClientBuilder} object.
     */
    public SecretClientBuilder disableChallengeResourceVerification() {
        this.disableChallengeResourceVerification = true;

        return this;
    }

    private String getEndpoint(Configuration configuration) {
        if (endpoint != null) {
            return endpoint;
        }

        String configEndpoint = configuration.get("AZURE_KEYVAULT_ENDPOINT");

        if (isNullOrEmpty(configEndpoint)) {
            return null;
        }

        try {
            URI uri = new URI(configEndpoint);

            return uri.toString();
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}
