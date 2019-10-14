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
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * RESERVED FOR INTERNAL USE. Base class for Storage client builders. Holds common code for managing resources and
 * pipeline settings.
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

    protected String accountName;
    protected String endpoint;
    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private SasTokenCredential sasTokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private Configuration configuration;

    /**
     * Assembles the pipeline based on Storage's standard policies and any custom policies set by the user.
     *
     * @return The pipeline.
     */
    protected HttpPipeline buildPipeline() {
        Objects.requireNonNull(this.endpoint, "'endpoint' cannot be null.");

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        addOptionalEncryptionPolicy(policies);
        policies.add(getUserAgentPolicy());
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        if (sharedKeyCredential != null) {
            policies.add(new SharedKeyCredentialPolicy(sharedKeyCredential));
        } else if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint)));
        } else if (sasTokenCredential != null) {
            policies.add(new SasTokenCredentialPolicy(sasTokenCredential));
        }

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RequestRetryPolicy(retryOptions));

        policies.addAll(this.additionalPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(makeValidationPolicy());

        policies.add(new HttpLoggingPolicy(httpLogOptions));

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Creates a policy that makes assertions on HTTP responses. These assertions are general purpose; method-specific
     * validations should be performed in the convenience layer.
     *
     * @return The validation policy.
     */
    private HttpPipelinePolicy makeValidationPolicy() {
        ResponseValidationPolicyBuilder builder = new ResponseValidationPolicyBuilder()
            .addOptionalEcho(Constants.HeaderConstants.CLIENT_REQUEST_ID); // echo client request id

        applyServiceSpecificValidations(builder);

        return builder.build();
    }

    /**
     * Adds an optional encryption policy that decrypts encrypted blobs.
     * @param policies The list of policies to add an optional encryption policy to.
     */
    protected void addOptionalEncryptionPolicy(List<HttpPipelinePolicy> policies) {
    }

    /**
     * Applies validation of general-purpose requests to builder. Method-specific validations should be performed in the
     * convenience layer.
     *
     * @param builder Builder to assemble assertions together.
     */
    protected abstract void applyServiceSpecificValidations(ResponseValidationPolicyBuilder builder);

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
    public final T credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;

        return getClazz().cast(this);
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     *
     * @param credential authorization credential
     * @return the updated builder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public T credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.sharedKeyCredential = null;
        this.sasTokenCredential = null;

        return getClazz().cast(this);
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service
     *
     * @param sasToken authorization credential
     * @return the updated builder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public final T sasToken(String sasToken) {
        this.sasTokenCredential = SasTokenCredential
            .fromSasTokenString(Objects.requireNonNull(sasToken, "'sasToken' cannot be null."));
        this.sharedKeyCredential = null;
        this.tokenCredential = null;

        return getClazz().cast(this);
    }

    /**
     * Clears the credential used to authorize requests sent to the service
     *
     * @return the updated buildr
     */
    public T setAnonymousCredential() {
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;

        return getClazz().cast(this);
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
    public final T connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 2);
            connectionKVPs.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }

        accountName = connectionKVPs.get(ACCOUNT_NAME);
        String accountKey = connectionKVPs.get(ACCOUNT_KEY);
        String endpointProtocol = connectionKVPs.get(ENDPOINT_PROTOCOL);
        String endpointSuffix = connectionKVPs.get(ENDPOINT_SUFFIX);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'."));
        }

        if (!ImplUtils.isNullOrEmpty(endpointProtocol) && !ImplUtils.isNullOrEmpty(endpointSuffix)) {
            String endpoint = String.format("%s://%s.%s.%s", endpointProtocol, accountName, getServiceUrlMidfix(),
                endpointSuffix.replaceFirst("^\\.", ""));
            endpoint(endpoint);
        }

        // Use accountName and accountKey to get the SAS token using the credential class.
        credential(new SharedKeyCredential(accountName, accountKey));

        return getClazz().cast(this);
    }

    /**
     * Gets the storage service segment to use for the URL hostname when assembling from a connection string.
     *
     * @return The midfix.
     */
    protected abstract String getServiceUrlMidfix();

    /**
     * Sets the http client used to send service requests. A default will be used if none is provided.
     *
     * @param httpClient http client to send requests
     * @return the updated buildr
     */
    public final T httpClient(HttpClient httpClient) {
        this.httpClient = httpClient; // builder implicitly handles default creation if null, so no null check
        return getClazz().cast(this);
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated builder
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}
     */
    public final T addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return getClazz().cast(this);
    }

    /**
     * Sets the logging configuration for service requests
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated builder
     */
    public final T httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return getClazz().cast(this);
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client
     * with when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     *
     * @param configuration configuration store
     * @return the updated buildr
     */
    public final T configuration(Configuration configuration) {
        this.configuration = configuration;
        return getClazz().cast(this);
    }

    /**
     * Gets the configuration being used to construct the pipeline.
     *
     * @return The configuration.
     */
    protected final Configuration getConfiguration() {
        if (this.configuration == null) {
            this.configuration = Configuration.getGlobalConfiguration().clone();
        }

        return this.configuration;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions the options to configure retry behaviors
     * @return the updated builder
     * @throws NullPointerException If {@code retryOptions} is {@code null}
     */
    public final T retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return getClazz().cast(this);
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link BaseClientBuilder#endpoint(String)
     * endpoint} when building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated builder.
     */
    public final T pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return getClazz().cast(this);
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

    /**
     * Gets the implementing client builder class.
     *
     * @return the implementing client builder class.
     */
    protected abstract Class<T> getClazz();
}
