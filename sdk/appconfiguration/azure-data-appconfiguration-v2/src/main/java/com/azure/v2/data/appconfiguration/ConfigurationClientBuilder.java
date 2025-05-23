// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.data.appconfiguration;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.http.pipeline.BearerTokenAuthenticationPolicy;
import com.azure.v2.core.traits.ConnectionStringTrait;
import com.azure.v2.core.traits.TokenCredentialTrait;
import com.azure.v2.data.appconfiguration.implementation.AzureAppConfigurationClientImpl;
import com.azure.v2.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.v2.data.appconfiguration.implementation.ConfigurationCredentialsPolicy;
import com.azure.v2.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.v2.data.appconfiguration.models.ConfigurationAudience;
import io.clientcore.core.annotations.ServiceClientBuilder;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
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
import io.clientcore.core.traits.ProxyTrait;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link ConfigurationClient ConfigurationClients}, call {@link #buildClient() buildClient} to construct an
 * instance of the desired client.
 *
 * <p>The client needs the service endpoint of the Azure App Configuration store and access credential.
 * {@link #connectionString(String) connectionString(String)} gives the builder the service endpoint and access
 * credential.</p>
 *
 * <p><strong>Instantiating a synchronous Configuration Client</strong></p>
 *
 * <!-- src_embed com.azure.v2.data.applicationconfig.configurationclient.instantiation -->
 * <pre>
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.data.applicationconfig.configurationclient.instantiation -->
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service but it doesn't contain the service endpoint. Set the pipeline
 * and set the service endpoint with {@link #endpoint(String) this}. Using a pipeline requires additional setup but
 * allows for finer control on how the {@link ConfigurationClient} is built.</p>
 *
 * <!-- src_embed com.azure.v2.data.applicationconfig.configurationclient.pipeline.instantiation -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .addPolicy&#40;new AddHeadersPolicy&#40;new HttpHeaders&#40;&#41;&#41;&#41;
 *     .build&#40;&#41;;
 *
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;dummy.azure.net&#47;&quot;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.data.applicationconfig.configurationclient.pipeline.instantiation -->
 *
 * @see ConfigurationClient
 */
@ServiceClientBuilder(serviceClients = { ConfigurationClient.class })
public final class ConfigurationClientBuilder
    implements HttpTrait<ConfigurationClientBuilder>, ProxyTrait<ConfigurationClientBuilder>,
    ConfigurationTrait<ConfigurationClientBuilder>, ConnectionStringTrait<ConfigurationClientBuilder>,
    TokenCredentialTrait<ConfigurationClientBuilder>, EndpointTrait<ConfigurationClientBuilder> {

    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";

    private static final String[] DEFAULT_SCOPES = new String[] { "https://azconfig.io/.default" };

    private static final Map<String, String> PROPERTIES
        = CoreUtils.getProperties("azure-v2-data-appconfiguration.properties");

    private final List<HttpPipelinePolicy> pipelinePolicies;

    /**
     * Create an instance of the ConfigurationClientBuilder.
     */
    public ConfigurationClientBuilder() {
        this.pipelinePolicies = new ArrayList<>();
    }

    /*
     * The HTTP client used to send the request.
     */
    private HttpClient httpClient;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /*
     * The retry options to configure retry policy for failed requests.
     */
    private HttpRetryOptions retryOptions;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder httpRetryOptions(HttpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder addHttpPipelinePolicy(HttpPipelinePolicy customPolicy) {
        Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null.");
        pipelinePolicies.add(customPolicy);
        return this;
    }

    /*
     * The redirect options to configure redirect policy
     */
    private HttpRedirectOptions redirectOptions;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder httpRedirectOptions(HttpRedirectOptions redirectOptions) {
        this.redirectOptions = redirectOptions;
        return this;
    }

    /*
     * The instrumentation configuration for HTTP requests and responses.
     */
    private HttpInstrumentationOptions httpInstrumentationOptions;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder
        httpInstrumentationOptions(HttpInstrumentationOptions httpInstrumentationOptions) {
        this.httpInstrumentationOptions = httpInstrumentationOptions;
        return this;
    }

    /*
     * The proxy options used during construction of the service client.
     */
    private ProxyOptions proxyOptions;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /*
     * The configuration store that is used during construction of the service client.
     */
    private Configuration configuration;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /*
     * The TokenCredential used for authentication.
     */
    private TokenCredential tokenCredential;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    private String connectionString;
    private ConfigurationClientCredentials connectionStringCredentials;

    /**
     * Sets the credential to use when authenticating HTTP requests. Also, sets the {@link #endpoint(String) endpoint}
     * for this ConfigurationClientBuilder.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};id={id_value};
     * secret={secret_value}"
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        this.connectionStringCredentials = new ConfigurationClientCredentials(connectionString);
        return this;
    }

    /*
     * The service endpoint
     */
    private String endpoint;

    /**
     * {@inheritDoc}.
     */
    @Override
    public ConfigurationClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /*
     * Service version
     */
    private ConfigurationServiceVersion serviceVersion;

    /**
     * Sets Service version.
     *
     * @param serviceVersion the serviceVersion value.
     * @return the ConfigurationClientBuilder.
     */
    public ConfigurationClientBuilder serviceVersion(ConfigurationServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    private ConfigurationAudience audience;

    /**
     * Gets the default scope for the given endpoint.
     *
     * @param endpoint The endpoint to get the default scope for.
     * @return The default scope for the given endpoint.
     */
    private String getDefaultScope(String endpoint) {
        String defaultValue = "/.default";
        if (audience == null || audience.toString().isEmpty()) {
            if (endpoint.endsWith("azconfig.azure.us") || endpoint.endsWith("appconfig.azure.us")) {
                return ConfigurationAudience.AZURE_GOVERNMENT + defaultValue;
            } else if (endpoint.endsWith("azconfig.azure.cn") || endpoint.endsWith("appconfig.azure.cn")) {
                return ConfigurationAudience.AZURE_CHINA + defaultValue;
            } else {
                return ConfigurationAudience.AZURE_PUBLIC_CLOUD + defaultValue;
            }
        }
        return audience + defaultValue;
    }

    /**
     * Sets the {@link ConfigurationAudience} to use for authentication with Microsoft Entra. The audience is not
     * considered when using a shared key.
     *
     * @param audience {@link ConfigurationAudience} of the service to be used when making requests.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder audience(ConfigurationAudience audience) {
        this.audience = audience;
        return this;
    }

    /**
     * Builds an instance of AzureAppConfigurationClientImpl with the provided parameters.
     *
     * @return an instance of AzureAppConfigurationClientImpl.
     */
    private AzureAppConfigurationClientImpl buildInnerClient() {
        // Manual changes start
        if (isNullOrEmpty(connectionString) && isNullOrEmpty(endpoint)) {
            throw LOGGER.throwableAtError()
                .log("'connectionString' or 'endpoint' cannot be null.", IllegalArgumentException::new);
        }

        if (!isNullOrEmpty(connectionString) && isNullOrEmpty(endpoint)) {
            ConfigurationClientCredentials credentialsLocal = new ConfigurationClientCredentials(connectionString);
            this.endpoint = credentialsLocal.getBaseUri();
        }
        // Manual changes end

        this.validateClient();
        ConfigurationServiceVersion localServiceVersion
            = (serviceVersion != null) ? serviceVersion : ConfigurationServiceVersion.getLatest();
        AzureAppConfigurationClientImpl client
            = new AzureAppConfigurationClientImpl(createHttpPipeline(), this.endpoint, localServiceVersion);
        return client;
    }

    private void validateClient() {
        // This method is invoked from 'buildInnerClient'/'buildClient' method.
        // Developer can customize this method, to validate that the necessary conditions are met for the new client.
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
    }

    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        HttpInstrumentationOptions localHttpInstrumentationOptions = this.httpInstrumentationOptions == null
            ? new HttpInstrumentationOptions()
            : this.httpInstrumentationOptions;
        String clientName = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");
        HttpPipelineBuilder httpPipelineBuilder = new HttpPipelineBuilder();
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(new UserAgentOptions().setSdkName(clientName).setSdkVersion(clientVersion)));
        policies.add(redirectOptions == null ? new HttpRedirectPolicy() : new HttpRedirectPolicy(redirectOptions));
        policies.add(retryOptions == null ? new HttpRetryPolicy() : new HttpRetryPolicy(retryOptions));
        this.pipelinePolicies.stream().forEach(p -> policies.add(p));
        if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, getDefaultScope(endpoint)));
            // Manual changes start
        } else if (connectionStringCredentials != null) {
            policies.add(new ConfigurationCredentialsPolicy(connectionStringCredentials));
        } else {
            throw LOGGER.throwableAtError()
                .log("Missing credential information while building a client.", IllegalArgumentException::new);
        }
        // Manual changes end

        policies.add(new HttpInstrumentationPolicy(localHttpInstrumentationOptions));
        policies.forEach(httpPipelineBuilder::addPolicy);
        return httpPipelineBuilder.httpClient(httpClient).build();
    }

    /**
     * Builds an instance of AzureAppConfigurationClient class.
     *
     * @return an instance of AzureAppConfigurationClient.
     */
    public ConfigurationClient buildClient() {
        return new ConfigurationClient(buildInnerClient(), new SyncTokenPolicy());
    }

    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationClientBuilder.class);
}
