// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
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
import com.azure.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link KeyVaultSettingsAsyncClient} and {@link KeyVaultSettingsClient}, by calling
 * {@link KeyVaultSettingsClientBuilder#buildAsyncClient()} and {@link KeyVaultSettingsClientBuilder#buildImplClient()}
 * respectively. It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link KeyVaultSettingsClientBuilder} to build a client are
 * {@link String vaultUrl} and {@link TokenCredential credential}.</p>
 *
 * @see KeyVaultSettingsClient
 * @see KeyVaultSettingsAsyncClient
 */
@ServiceClientBuilder(serviceClients = {KeyVaultSettingsClient.class, KeyVaultSettingsAsyncClient.class})
public final class KeyVaultSettingsClientBuilder implements
    TokenCredentialTrait<KeyVaultSettingsClientBuilder>,
    HttpTrait<KeyVaultSettingsClientBuilder>,
    ConfigurationTrait<KeyVaultSettingsClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultSettingsClientBuilder.class);
    private static final String AZURE_KEY_VAULT_RBAC = "azure-key-vault-administration.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();

    private final List<HttpPipelinePolicy> pipelinePolicies;
    private final Map<String, String> properties;

    private TokenCredential credential;
    private HttpPipeline pipeline;
    private String vaultUrl;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private ClientOptions clientOptions;
    private KeyVaultAdministrationServiceVersion serviceVersion;
    private boolean disableChallengeResourceVerification = false;

    /**
     * Create an instance of the KeyVaultSettingsClientBuilder.
     */
    public KeyVaultSettingsClientBuilder() {
        this.httpLogOptions = new HttpLogOptions();
        this.pipelinePolicies = new ArrayList<>();
        this.properties = CoreUtils.getProperties(AZURE_KEY_VAULT_RBAC);
    }

    /**
     * Sets the URL to the Key Vault on which the client operates. Appears as "DNS Name" in the Azure portal. You should
     * validate that this URL references a valid Key Vault or Managed HSM resource.
     * Refer to the following  <a href=https://aka.ms/azsdk/blog/vault-uri>documentation</a> for details.
     *
     * @param vaultUrl The vault URL is used as destination on Azure to send requests to.
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     *
     * @throws IllegalArgumentException If {@code vaultUrl} is null or it cannot be parsed into a valid URL.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public KeyVaultSettingsClientBuilder vaultUrl(String vaultUrl) {
        if (vaultUrl == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'vaultUrl' cannot be null."));
        }

        try {
            URL url = new URL(vaultUrl);
            this.vaultUrl = url.toString();
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The Azure Key Vault URL is malformed.", e));
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
     * @return The updated {@link KeyVaultAccessControlClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public KeyVaultSettingsClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.credential = credential;

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
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    @Override
    public KeyVaultSettingsClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;

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
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    @Override
    public KeyVaultSettingsClientBuilder httpClient(HttpClient client) {
        this.httpClient = client;

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
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    @Override
    public KeyVaultSettingsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;

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
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     *
     * @see HttpClientOptions
     */
    @Override
    public KeyVaultSettingsClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

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
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    @Override
    public KeyVaultSettingsClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;

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
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public KeyVaultSettingsClientBuilder addPolicy(HttpPipelinePolicy policy) {
        if (policy == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'policy' cannot be null."));
        }

        this.pipelinePolicies.add(policy);

        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the
     * {@link Configuration#getGlobalConfiguration() global configuration store}, use {@link Configuration#NONE} to
     * bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to get configuration details.
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    @Override
    public KeyVaultSettingsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link KeyVaultAdministrationServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param serviceVersion {@link KeyVaultAdministrationServiceVersion} of the service API used when making requests.
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    public KeyVaultSettingsClientBuilder serviceVersion(KeyVaultAdministrationServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used in the pipeline, if not provided.
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy User's retry policy applied to each request.
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    public KeyVaultSettingsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;

        return this;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault or Managed HSM domain. This
     * verification is performed by default.
     *
     * @return The updated {@link KeyVaultSettingsClientBuilder} object.
     */
    public KeyVaultSettingsClientBuilder disableChallengeResourceVerification() {
        this.disableChallengeResourceVerification = true;

        return this;
    }

    /**
     * Builds an instance of KeyVaultSettingsClientImpl with the provided parameters.
     *
     * @return an instance of KeyVaultSettingsClientImpl.
     */
    private KeyVaultSettingsClientImpl buildImplClient() {
        HttpPipeline buildPipeline = (pipeline != null) ? pipeline : createHttpPipeline();
        KeyVaultAdministrationServiceVersion version = (serviceVersion != null)
            ? serviceVersion : KeyVaultAdministrationServiceVersion.getLatest();
        return new KeyVaultSettingsClientImpl(buildPipeline, version.getVersion());
    }

    private HttpPipeline createHttpPipeline() {
        if (pipeline != null) {
            return pipeline;
        }

        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        if (vaultUrl == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        }

        serviceVersion = serviceVersion != null ? serviceVersion : KeyVaultAdministrationServiceVersion.getLatest();

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        httpLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        ClientOptions localClientOptions = clientOptions != null ? clientOptions : DEFAULT_CLIENT_OPTIONS;

        String applicationId = CoreUtils.getApplicationId(localClientOptions, httpLogOptions);

        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersFromContextPolicy());

        HttpHeaders headers = new HttpHeaders();
        localClientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));
        if (headers.getSize() > 0) {
            policies.add(new AddHeadersPolicy(headers));
        }

        policies.addAll(
            this.pipelinePolicies.stream()
                .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)
                .collect(Collectors.toList()));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));
        policies.add(new KeyVaultCredentialPolicy(credential, disableChallengeResourceVerification));
        policies.add(new AddDatePolicy());
        policies.add(new CookiePolicy());
        policies.addAll(
            this.pipelinePolicies.stream()
                .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)
                .collect(Collectors.toList()));
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        TracingOptions tracingOptions = localClientOptions.getTracingOptions();
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer(clientName, clientVersion, KEYVAULT_TRACING_NAMESPACE_VALUE, tracingOptions);

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(localClientOptions)
            .tracer(tracer)
            .build();
    }

    /**
     * Builds an instance of KeyVaultSettingsAsyncClient class.
     *
     * @return an instance of KeyVaultSettingsAsyncClient.
     */
    public KeyVaultSettingsAsyncClient buildAsyncClient() {
        return new KeyVaultSettingsAsyncClient(vaultUrl, buildImplClient());
    }

    /**
     * Builds an instance of KeyVaultSettingsClient class.
     *
     * @return an instance of KeyVaultSettingsClient.
     */
    public KeyVaultSettingsClient buildClient() {
        return new KeyVaultSettingsClient(vaultUrl, buildImplClient());
    }
}
