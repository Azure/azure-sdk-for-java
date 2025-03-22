// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.traits.TokenCredentialTrait;
import com.azure.v2.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.keys.models.DeletedKey;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKeyIdentifier;
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
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.traits.ConfigurationTrait;
import io.clientcore.core.traits.EndpointTrait;
import io.clientcore.core.traits.HttpTrait;
import io.clientcore.core.utils.configuration.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static io.clientcore.core.utils.AuthUtils.isNullOrEmpty;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link KeyClient key client}, by calling {@link KeyClientBuilder#buildClient() buildClient}. It constructs an
 * instance of the desired client.
 *
 * <p>The {@link KeyClient} provides methods to manage {@link KeyVaultKey keys} in Azure Key Vault. The client supports
 * creating, retrieving, updating, deleting, purging, backing up, restoring, and listing {@link KeyVaultKey keys}. The
 * client also supports listing {@link DeletedKey deleted keys} for a soft-delete enabled
 * key vault.</p>
 *
 * <p> The minimal configuration options required by {@link KeyClientBuilder} to build a {@link KeyClient} are an
 * {@link String endpoint} and {@link TokenCredential credential}. </p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.instantiation -->
 *
 * <p>The {@link HttpInstrumentationOptions.HttpLogLevel log level}, multiple custom {@link HttpPipelinePolicy policies}
 * and custom {@link HttpClient HTTP client} can be optionally configured in the {@link KeyClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.instantiation.withHttpClient -->
 * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.instantiation.withHttpClient -->
 *
 * @see KeyClient
 */
@ServiceClientBuilder(serviceClients = KeyClient.class)
public final class KeyClientBuilder
    implements ConfigurationTrait<KeyClientBuilder>, EndpointTrait<KeyClientBuilder>, HttpTrait<KeyClientBuilder>,
    TokenCredentialTrait<KeyClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(KeyClientBuilder.class);

    private static final String SDK_NAME = "azure-security-keyvault-keys-v2";
    private static final String SDK_VERSION = "1.0.0-beta.1";
    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    //private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    private final List<HttpPipelinePolicy> policies;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private String endpoint;
    private HttpClient httpClient;
    private HttpInstrumentationOptions instrumentationOptions;
    private HttpRedirectOptions redirectOptions;
    private HttpRetryPolicy retryPolicy;
    private HttpRetryOptions retryOptions;
    private Configuration configuration;
    private KeyServiceVersion version;
    //private ClientOptions clientOptions;
    private boolean disableChallengeResourceVerification = false;

    /**
     * Creates a {@link KeyClientBuilder} that is used to configure and create {@link KeyClient} instances.
     */
    public KeyClientBuilder() {
        instrumentationOptions = new HttpInstrumentationOptions();
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link KeyClient} based on options set in the builder. Every time {@code buildClient()} is called, a
     * new instance of {@link KeyClient} is created.
     *
     * <p>If {@link KeyClientBuilder#httpPipeline(HttpPipeline)} is set, then the {@code pipeline} and
     * {@link KeyClientBuilder#endpoint(String) endpoint} are used to create the {@link KeyClientBuilder client}.
     * All other builder settings are ignored. If {@code pipeline} is not set, then a
     * {@link KeyClientBuilder#credential(TokenCredential) credential} and
     * {@link KeyClientBuilder#endpoint(String) endpoint} are required to build the {@link KeyClient client}.</p>
     *
     * @return A {@link KeyClient} based on options set in this builder.
     *
     * @throws IllegalStateException If an {@link KeyClientBuilder#endpoint(String) endpoint} has not been set or if
     * either of a {@link KeyClientBuilder#credential(TokenCredential) credential} or
     * {@link KeyClientBuilder#httpPipeline(HttpPipeline) pipeline} were not provided.
     * @throws IllegalStateException If both {@link #httpRetryOptions(HttpRetryOptions)} and
     * {@link #httpRetryPolicy(HttpRetryPolicy)} have been set.
     */
    public KeyClient buildClient() {
        return new KeyClient(buildImplClient(), endpoint);
    }

    private KeyClientImpl buildImplClient() {
        Configuration configuration = this.configuration == null
            ? Configuration.getGlobalConfiguration()
            : this.configuration;

        String endpoint = getEndpoint(configuration);

        if (endpoint == null) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(
                "An Azure Key Vault or Managed HSM endpoint is required. You can set one by using the"
                    + " KeyClientBuilder.vaultUrl() method or by setting the environment variable"
                    + " 'AZURE_KEYVAULT_ENDPOINT'."));
        }

        KeyServiceVersion version = this.version == null ? KeyServiceVersion.getLatest() : this.version;

        if (pipeline != null) {
            return new KeyClientImpl(pipeline, endpoint, version);
        }

        if (credential == null) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(
                "A credential object is required. You can set one by using the KeyClientBuilder.credential() method."));
        }

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(redirectOptions == null ? new HttpRedirectPolicy() : new HttpRedirectPolicy(redirectOptions));

        if (retryPolicy != null) {
            policies.add(retryPolicy);
        } else if (retryOptions != null) {
            policies.add(new HttpRetryPolicy(retryOptions));
        } else {
            policies.add(new HttpRetryPolicy());
        }

        //ClientOptions clientOptions = this.clientOptions == null ? DEFAULT_CLIENT_OPTIONS : this.clientOptions;

        /*policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, instrumentationOptions),
            clientName, clientVersion, buildConfiguration));*/

        //List<HttpHeader> httpHeaderList = new ArrayList<>();

        /*clientOptions.getHeaders()
            .forEach(header -> httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));

        policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));*/

        policies.addAll(this.policies);
        policies.add(new KeyVaultCredentialPolicy(credential, disableChallengeResourceVerification));

        HttpInstrumentationOptions instrumentationOptions = this.instrumentationOptions == null
            ? new HttpInstrumentationOptions()
            : this.instrumentationOptions;

        policies.add(new HttpInstrumentationPolicy(instrumentationOptions));

        HttpPipelineBuilder httpPipelineBuilder = new HttpPipelineBuilder();

        // Add all policies to the pipeline.
        policies.forEach(httpPipelineBuilder::addPolicy);

        HttpPipeline pipeline = httpPipelineBuilder.httpClient(httpClient)
            //.clientOptions(clientOptions)
            .build();

        return new KeyClientImpl(pipeline, this.endpoint, version);
    }

    /**
     * Sets the vault endpoint URL to send HTTP requests to. You should validate that this URL references a valid Key
     * Vault or Managed HSM resource. Refer to the following
     * <a href=https://aka.ms/azsdk/blog/vault-uri>documentation</a> for details.
     *
     * @param endpoint The endpoint is used as destination on Azure to send requests to. If you have a secret
     * identifier, create a new {@link KeyVaultKeyIdentifier} to parse it and obtain the {@code endpoint} and other
     * information via {@link KeyVaultKeyIdentifier#getEndpoint()}.
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws IllegalArgumentException If {@code vaultUrl} cannot be parsed into a valid URL.
     * @throws NullPointerException If {@code vaultUrl} is {@code null}.
     */
    public KeyClientBuilder endpoint(String endpoint) {
        if (endpoint == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException("'endpoint' cannot be null."));
        }

        try {
            URI url = new URI(endpoint);
            this.endpoint = url.toString();
        } catch (URISyntaxException e) {
            throw LOGGER.logThrowableAsError(
                new IllegalArgumentException("The Azure Key Vault endpoint is malformed.", e));
        }

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a> documentation for more details
     * on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public KeyClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException("'credential' cannot be null."));
        }

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
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param instrumentationOptions The {@link HttpInstrumentationOptions configuration} to use when recording
     * telemetry about HTTP requests sent to the service and responses received from it.
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder httpInstrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
        this.instrumentationOptions = instrumentationOptions;

        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public KeyClientBuilder addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {
        if (pipelinePolicy == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException("'pipelinePolicy' cannot be null."));
        }

        policies.add(pipelinePolicy);

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param client The {@link HttpClient} to use for requests.
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder httpClient(HttpClient client) {
        this.httpClient = client;

        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder httpPipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;

        return this;
    }

    /**
     * Sets the client-specific configuration used to retrieve client or global configuration properties when building a
     * client.
     *
     * @param configuration Configuration store used to retrieve client configurations.
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link KeyServiceVersion service version} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link KeyServiceVersion} of the service API used when making requests.
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder serviceVersion(KeyServiceVersion version) {
        this.version = version;

        return this;
    }

    /**
     * Sets the {@link HttpRetryPolicy} that is used when each request is sent. Setting this is mutually exclusive with
     * using {@link #httpRetryOptions(HttpRetryOptions)}.
     * <p>
     * The default retry policy will be used in the pipeline, if not provided.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder httpRetryPolicy(HttpRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;

        return this;
    }

    /**
     * Sets the {@link HttpRetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param retryOptions The {@link HttpRetryOptions} to use for all the requests made through the client.
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder httpRetryOptions(HttpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;

        return this;
    }

    /**
     * Sets the {@link HttpRedirectOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param redirectOptions The {@link HttpRedirectOptions} to use for all the requests made through the client.
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder httpRedirectOptions(HttpRedirectOptions redirectOptions) {
        this.redirectOptions = redirectOptions;

        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     * <p>
     * <strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @return The updated {@link KeyClientBuilder} object.
     */
    /*@Override
    public KeyClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }*/

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault or Managed HSM domain. This
     * verification is performed by default.
     *
     * @return The updated {@link KeyClientBuilder} object.
     */
    public KeyClientBuilder disableChallengeResourceVerification() {
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
