// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

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
import com.azure.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.implementation.KeyVaultErrorCodeStrings;
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
 * <!-- src_embed com.azure.security.keyvault.keys.KeyAsyncClient.instantiation -->
 * <pre>
 * KeyAsyncClient keyAsyncClient = new KeyClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyAsyncClient.instantiation -->
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link KeyClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.KeyAsyncClient.instantiation.withHttpClient -->
 * <pre>
 * KeyAsyncClient keyAsyncClient = new KeyClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .httpClient&#40;HttpClient.createDefault&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyAsyncClient.instantiation.withHttpClient -->
 *
 * <p> The minimal configuration options required by {@link KeyClientBuilder keyClientBuilder} to build {@link
 * KeyClient} are {@link String vaultUrl} and {@link TokenCredential credential}. </p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.instantiation -->
 * <pre>
 * KeyClient keyClient = new KeyClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyClient.instantiation -->
 *
 * @see KeyAsyncClient
 * @see KeyClient
 */
@ServiceClientBuilder(serviceClients = KeyClient.class)
public final class KeyClientBuilder implements
    TokenCredentialTrait<KeyClientBuilder>,
    HttpTrait<KeyClientBuilder>,
    ConfigurationTrait<KeyClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(KeyClientBuilder.class);
    // This is properties file's name.
    private static final String AZURE_KEY_VAULT_KEYS = "azure-key-vault-keys.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();

    private final List<HttpPipelinePolicy> perCallPolicies;
    private final List<HttpPipelinePolicy> perRetryPolicies;
    private final Map<String, String> properties;

    private TokenCredential credential;
    private HttpPipeline pipeline;
    private String vaultUrl;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private KeyServiceVersion version;
    private ClientOptions clientOptions;
    private boolean disableChallengeResourceVerification = false;

    /**
     * The constructor with defaults.
     */
    public KeyClientBuilder() {
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
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public KeyClient buildClient() {
        return new KeyClient(buildInnerClient());
    }

    /**
     * Creates a {@link KeyAsyncClient} based on options set in the builder.
     * Every time {@code buildAsyncClient()} is called, a new instance of {@link KeyAsyncClient} is created.
     *
     * <p>If {@link KeyClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link KeyClientBuilder#vaultUrl(String) vaultUrl} are used to create the {@link KeyClientBuilder client}.
     * All other builder settings are ignored. If {@code pipeline} is not set, then {@link
     * KeyClientBuilder#credential(TokenCredential) key vault credential} and {@link KeyClientBuilder#vaultUrl(String)}
     * key vault url are required to build the {@link KeyAsyncClient client}.</p>
     *
     * @return A {@link KeyAsyncClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link KeyClientBuilder#credential(TokenCredential)} or
     * {@link KeyClientBuilder#vaultUrl(String)} have not been set.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public KeyAsyncClient buildAsyncClient() {
        return new KeyAsyncClient(buildInnerClient());
    }

    private KeyClientImpl buildInnerClient() {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;
        String buildEndpoint = getBuildEndpoint(buildConfiguration);

        if (buildEndpoint == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        }

        KeyServiceVersion serviceVersion = version != null ? version : KeyServiceVersion.getLatest();

        if (pipeline != null) {
            return new KeyClientImpl(vaultUrl, pipeline, serviceVersion);
        }

        if (credential == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(KeyVaultErrorCodeStrings.CREDENTIALS_REQUIRED));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        httpLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        ClientOptions localClientOptions = clientOptions != null ? clientOptions : DEFAULT_CLIENT_OPTIONS;

        policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(localClientOptions, httpLogOptions), clientName,
            clientVersion, buildConfiguration));

        List<HttpHeader> httpHeaderList = new ArrayList<>();
        localClientOptions.getHeaders().forEach(header ->
            httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
        policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));

        // Add per call additional policies.
        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        // Add retry policy.
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        policies.add(new KeyVaultCredentialPolicy(credential, disableChallengeResourceVerification));
        // Add per retry additional policies.
        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        TracingOptions tracingOptions = localClientOptions.getTracingOptions();
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer(clientName, clientVersion, KEYVAULT_TRACING_NAMESPACE_VALUE, tracingOptions);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(tracer)
            .clientOptions(localClientOptions)
            .build();

        return new KeyClientImpl(vaultUrl, pipeline, serviceVersion);
    }

    /**
     * Sets the vault endpoint URL to send HTTP requests to. You should validate that this URL references a valid Key
     * Vault or Managed HSM resource. Refer to the following
     * <a href=https://aka.ms/azsdk/blog/vault-uri>documentation</a> for details.
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
            throw LOGGER.logExceptionAsError(new NullPointerException("'vaultUrl' cannot be null."));
        }

        try {
            URL url = new URL(vaultUrl);
            this.vaultUrl = url.toString();
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "The Azure Key Vault url is malformed.", ex));
        }
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public KeyClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.credential = credential;

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
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder httpLogOptions(HttpLogOptions logOptions) {
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
     * @return The updated {@link KeyClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public KeyClientBuilder addPolicy(HttpPipelinePolicy policy) {
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
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * The {@link #vaultUrl(String) vaultUrl} is not ignored when
     * {@code pipeline} is set.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
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
    @Override
    public KeyClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * The default retry policy will be used in the pipeline, if not provided.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
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
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder retryOptions(RetryOptions retryOptions) {
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
     * @return The updated {@link KeyClientBuilder} object.
     */
    @Override
    public KeyClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }

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

    private String getBuildEndpoint(Configuration configuration) {
        if (vaultUrl != null) {
            return vaultUrl;
        }

        String configEndpoint = configuration.get("AZURE_KEYVAULT_ENDPOINT");
        if (CoreUtils.isNullOrEmpty(configEndpoint)) {
            return null;
        }

        try {
            URL url =  new URL(configEndpoint);
            return url.toString();
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}
