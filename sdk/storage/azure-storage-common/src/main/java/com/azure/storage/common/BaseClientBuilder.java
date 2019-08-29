// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * RESERVED FOR INTERNAL USE.
 * Base class for Storage client builders. Holds common code for managing resources and pipeline settings.
 */
public abstract class BaseClientBuilder<T extends BaseClientBuilder<T>> {

    private static final String ACCOUNT_NAME = "accountname";
    private static final String ACCOUNT_KEY = "accountkey";
    private static final String ENDPOINT_PROTOCOL = "defaultendpointsprotocol";
    private static final String ENDPOINT_SUFFIX = "endpointsuffix";

    private final ClientLogger logger = new ClientLogger(BaseClientBuilder.class);

    // for when a user wants to manage the pipeline themselves
    private HttpPipeline pipeline;

    // for when a user wants to add policies to our pre-constructed pipeline
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();

    protected String endpoint;
    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private SASTokenCredential sasTokenCredential;
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel = HttpLogDetailLevel.NONE;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private Configuration configuration;

    /**
     * Assembles the pipeline based on Storage's standard policies and any custom policies set by the user.
     *
     * @return The pipeline.
     */
    protected HttpPipeline buildPipeline() {
        Objects.requireNonNull(this.endpoint);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy());
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        if (sharedKeyCredential != null) {
            policies.add(new SharedKeyCredentialPolicy(sharedKeyCredential));
        } else if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint)));
        } else if (sasTokenCredential != null) {
            policies.add(new SASTokenCredentialPolicy(sasTokenCredential));
        }

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RequestRetryPolicy(retryOptions));

        policies.addAll(this.additionalPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Sets the blob service endpoint, additionally parses it for information (SAS token, path information, etc.)
     *
     * @param endpoint URL of the service
     * @return the updated builder
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public abstract T endpoint(String endpoint);

    /**
     * Sets the credential used to authorize requests sent to the service
     *
     * @param credential authorization credential
     * @return the updated builder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public final T credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential);
        this.tokenCredential = null;
        this.sasTokenCredential = null;

        return (T) this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     *
     * @param credential authorization credential
     * @return the updated builder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public T credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential);
        this.sharedKeyCredential = null;
        this.sasTokenCredential = null;

        return (T) this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     *
     * @param credential authorization credential
     * @return the updated builder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public final T credential(SASTokenCredential credential) {
        this.sasTokenCredential = Objects.requireNonNull(credential);
        this.sharedKeyCredential = null;
        this.tokenCredential = null;

        return (T) this;
    }

    /**
     * Clears the credential used to authorize requests sent to the service
     *
     * @return the updated buildr
     */
    @SuppressWarnings("unchecked")
    public T setAnonymousCredential() {
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;

        return (T) this;
    }

    /**
     * Whether or not this builder has a credential to use with the pipeline.
     *
     * @return The boolean value of the expression.
     */
    protected final boolean hasCredential() {
        return this.sharedKeyCredential != null
            || this.tokenCredential != null
            || this.sasTokenCredential != null;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     *
     * @param connectionString connection string from access keys section
     * @return the updated builder
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain AccountName or AccountKey.
     */
    @SuppressWarnings("unchecked")
    public final T connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 2);
            connectionKVPs.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }

        String accountName = connectionKVPs.get(ACCOUNT_NAME);
        String accountKey = connectionKVPs.get(ACCOUNT_KEY);
        String endpointProtocol = connectionKVPs.get(ENDPOINT_PROTOCOL);
        String endpointSuffix = connectionKVPs.get(ENDPOINT_SUFFIX);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'."));
        }

        if (!ImplUtils.isNullOrEmpty(endpointProtocol) && !ImplUtils.isNullOrEmpty(endpointSuffix)) {
            String endpoint = String.format("%s://%s.%s.%s", endpointProtocol, accountName, getServiceUrlMidfix(), endpointSuffix.replaceFirst("^\\.", ""));
            endpoint(endpoint);
        }

        // Use accountName and accountKey to get the SAS token using the credential class.
        credential(new SharedKeyCredential(accountName, accountKey));

        return (T) this;
    }

    /**
     * Gets the storage service segment to use for the URL hostname when assembling from a connection string.
     *
     * @return The midfix.
     */
    protected abstract String getServiceUrlMidfix();

    /**
     * Sets the http client used to send service requests. A default will be used if none is provided.
     * @param httpClient http client to send requests
     * @return the updated buildr
     */
    @SuppressWarnings("unchecked")
    public final T httpClient(HttpClient httpClient) {
        this.httpClient = httpClient; // builder implicitly handles default creation if null, so no null check
        return (T) this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated builder
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public final T addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy));
        return (T) this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated builder
     * @throws NullPointerException If {@code logLevel} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public final T httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = Objects.requireNonNull(logLevel);
        return (T) this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client with
     * when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated buildr
     */
    @SuppressWarnings("unchecked")
    public final T configuration(Configuration configuration) {
        this.configuration = configuration;
        return (T) this;
    }

    /**
     * Gets the configuration being used to construct the pipeline.
     *
     * @return The configuration.
     */
    protected final Configuration getConfiguration() {
        if (this.configuration == null) {
            this.configuration = ConfigurationManager.getConfiguration();
        }

        return this.configuration;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     * @param retryOptions the options to configure retry behaviors
     * @return the updated builder
     * @throws NullPointerException If {@code retryOptions} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public final T retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions);
        return (T) this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link BaseClientBuilder#endpoint(String) endpoint} when building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated builder.
     */
    @SuppressWarnings("unchecked")
    public final T pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return (T) this;
    }

    /**
     * Gets the optional custom pipeline to use in constructed clients.
     *
     * @return The pipeline. Null if the builder should construct one.
     */
    protected final HttpPipeline getPipeline() {
        return this.pipeline;
    }

    /**
     * Gets the user agent policy to use for pipelines constructed by this builder.
     *
     * @return The policy.
     */
    protected abstract UserAgentPolicy getUserAgentPolicy();
}
