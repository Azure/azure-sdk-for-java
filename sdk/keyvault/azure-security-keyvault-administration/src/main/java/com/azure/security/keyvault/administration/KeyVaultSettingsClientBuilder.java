// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.HttpTrait;
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
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link KeyVaultSettingsAsyncClient} and {@link KeyVaultSettingsClient}, by calling
 * {@link KeyVaultSettingsClientBuilder#buildAsyncClient()} and {@link KeyVaultSettingsClientBuilder#buildClient()}
 * respectively. It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link KeyVaultSettingsClientBuilder} to build a client are
 * {@link String vaultUrl} and {@link TokenCredential credential}.</p>
 *
 * @see KeyVaultSettingsClient
 * @see KeyVaultSettingsAsyncClient
 */
@ServiceClientBuilder(serviceClients = {KeyVaultSettingsClient.class, KeyVaultSettingsAsyncClient.class})
public final class KeyVaultSettingsClientBuilder
        implements HttpTrait<KeyVaultSettingsClientBuilder>, ConfigurationTrait<KeyVaultSettingsClientBuilder> {
    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";

    private final Map<String, String> properties = new HashMap<>();

    private final List<HttpPipelinePolicy> pipelinePolicies;

    private final ClientLogger logger = new ClientLogger(KeyVaultBackupClientBuilder.class);

    /** Create an instance of the KeyVaultSettingsClientBuilder. */
    public KeyVaultSettingsClientBuilder() {
        this.pipelinePolicies = new ArrayList<>();
    }

    private String vaultUrl;

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
            throw logger.logExceptionAsError(new NullPointerException("'vaultUrl' cannot be null."));
        }

        try {
            URL url = new URL(vaultUrl);
            this.vaultUrl = url.toString();
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Key Vault URL is malformed.", e));
        }

        return this;
    }

    /*
     * The HTTP pipeline to send requests through.
     */
    private HttpPipeline pipeline;

    /** {@inheritDoc}. */
    @Override
    public KeyVaultSettingsClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;

        return this;
    }

    /*
     * The HTTP client used to send the request.
     */
    private HttpClient httpClient;

    /** {@inheritDoc}. */
    @Override
    public KeyVaultSettingsClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;

        return this;
    }

    /*
     * The logging configuration for HTTP requests and responses.
     */
    private HttpLogOptions httpLogOptions;

    /** {@inheritDoc}. */
    @Override
    public KeyVaultSettingsClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;

        return this;
    }

    /*
     * The client options such as application ID and custom headers to set on a request.
     */
    private ClientOptions clientOptions;

    /** {@inheritDoc}. */
    @Override
    public KeyVaultSettingsClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }

    /*
     * The retry options to configure retry policy for failed requests.
     */
    private RetryOptions retryOptions;

    /** {@inheritDoc}. */
    @Override
    public KeyVaultSettingsClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;

        return this;
    }

    /** {@inheritDoc}. */
    @Override
    public KeyVaultSettingsClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        pipelinePolicies.add(customPolicy);

        return this;
    }

    /*
     * The configuration store that is used during construction of the service client.
     */
    private Configuration configuration;

    /** {@inheritDoc}. */
    @Override
    public KeyVaultSettingsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /*
     * Api Version
     */
    private String apiVersion;

    /**
     * Sets Api Version.
     *
     * @param apiVersion the apiVersion value.
     * @return the KeyVaultSettingsClientBuilder.
     */
    public KeyVaultSettingsClientBuilder apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;

        return this;
    }

    /*
     * The serializer to serialize an object into a string
     */
    private SerializerAdapter serializerAdapter;

    /**
     * Sets The serializer to serialize an object into a string.
     *
     * @param serializerAdapter the serializerAdapter value.
     * @return the KeyVaultSettingsClientBuilder.
     */
    public KeyVaultSettingsClientBuilder serializerAdapter(SerializerAdapter serializerAdapter) {
        this.serializerAdapter = serializerAdapter;

        return this;
    }

    /*
     * The retry policy that will attempt to retry failed requests, if applicable.
     */
    private RetryPolicy retryPolicy;

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the KeyVaultSettingsClientBuilder.
     */
    public KeyVaultSettingsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;

        return this;
    }

    /**
     * Builds an instance of KeyVaultSettingsClientImpl with the provided parameters.
     *
     * @return an instance of KeyVaultSettingsClientImpl.
     */
    private KeyVaultSettingsClientImpl buildInnerClient() {
        HttpPipeline localPipeline = (pipeline != null) ? pipeline : createHttpPipeline();
        String localApiVersion = (apiVersion != null) ? apiVersion : "7.4-preview.1";
        SerializerAdapter localSerializerAdapter =
                (serializerAdapter != null) ? serializerAdapter : JacksonAdapter.createDefaultSerializerAdapter();
        KeyVaultSettingsClientImpl client =
                new KeyVaultSettingsClientImpl(localPipeline, localSerializerAdapter, localApiVersion);

        return client;
    }

    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration =
                (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        if (httpLogOptions == null) {
            httpLogOptions = new HttpLogOptions();
        }

        if (clientOptions == null) {
            clientOptions = new ClientOptions();
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersFromContextPolicy());

        HttpHeaders headers = new HttpHeaders();

        clientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));

        if (headers.getSize() > 0) {
            policies.add(new AddHeadersPolicy(headers));
        }

        policies.addAll(
                this.pipelinePolicies.stream()
                        .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)
                        .collect(Collectors.toList()));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));
        policies.add(new AddDatePolicy());
        policies.add(new CookiePolicy());
        policies.addAll(
                this.pipelinePolicies.stream()
                        .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)
                        .collect(Collectors.toList()));

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline httpPipeline =
                new HttpPipelineBuilder()
                        .policies(policies.toArray(new HttpPipelinePolicy[0]))
                        .httpClient(httpClient)
                        .clientOptions(clientOptions)
                        .build();

        return httpPipeline;
    }

    /**
     * Builds an instance of KeyVaultSettingsAsyncClient class.
     *
     * @return an instance of KeyVaultSettingsAsyncClient.
     */
    public KeyVaultSettingsAsyncClient buildAsyncClient() {
        return new KeyVaultSettingsAsyncClient(vaultUrl, buildInnerClient());
    }

    /**
     * Builds an instance of KeyVaultSettingsClient class.
     *
     * @return an instance of KeyVaultSettingsClient.
     */
    public KeyVaultSettingsClient buildClient() {
        return new KeyVaultSettingsClient(vaultUrl, buildInnerClient());
    }
}
