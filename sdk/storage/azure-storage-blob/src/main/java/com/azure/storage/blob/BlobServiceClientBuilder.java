// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

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
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.common.Constants;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent BlobServiceClientBuilder for instantiating a {@link BlobServiceClient} or {@link BlobServiceAsyncClient}
 * using {@link BlobServiceClientBuilder#buildClient()} or {@link BlobServiceClientBuilder#buildAsyncClient()} respectively.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 *     <li>the endpoint through {@code .endpoint()}, in the format of {@code https://{accountName}.blob.core.windows.net}.
 *     <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link BlobServiceClient} or {@code .buildAsyncClient()} to create a {@link BlobServiceAsyncClient}.
 */
public final class BlobServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(BlobServiceClientBuilder.class);

    private final List<HttpPipelinePolicy> policies;

    private String endpoint;
    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private SASTokenCredential sasTokenCredential;
    private HttpClient httpClient;
    private HttpPipeline pipeline;
    private HttpLogDetailLevel logLevel;
    private RequestRetryOptions retryOptions;
    private Configuration configuration;

    /**
     * Creates a builder instance that is able to configure and construct {@link BlobServiceClient BlobServiceClients}
     * and {@link BlobServiceAsyncClient BlobServiceAsyncClients}.
     */
    public BlobServiceClientBuilder() {
        retryOptions = new RequestRetryOptions();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * @return a {@link BlobServiceClient} created from the configurations in this builder.
     */
    public BlobServiceClient buildClient() {
        return new BlobServiceClient(buildAsyncClient());
    }

    /**
     * @return a {@link BlobServiceAsyncClient} created from the configurations in this builder.
     */
    public BlobServiceAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint);

        if (pipeline != null) {
            return new BlobServiceAsyncClient(new AzureBlobStorageBuilder()
                .url(endpoint)
                .pipeline(pipeline)
                .build());
        }

        if (configuration == null) {
            configuration = ConfigurationManager.getConfiguration();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, configuration));
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

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new BlobServiceAsyncClient(new AzureBlobStorageBuilder()
            .url(endpoint)
            .pipeline(pipeline)
            .build());
    }

    /**
     * Sets the blob service endpoint, additionally parses it for information (SAS token, queue name)
     * @param endpoint URL of the service
     * @return the updated BlobServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public BlobServiceClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            this.endpoint = url.getProtocol() + "://" + url.getHost();

            this.sasTokenCredential = SASTokenCredential.fromSASTokenString(URLParser.parse(url).sasQueryParameters().encode());
            if (this.sasTokenCredential != null) {
                this.tokenCredential = null;
                this.sharedKeyCredential = null;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage endpoint url is malformed.");
        }

        return this;
    }

    String endpoint() {
        return this.endpoint;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobServiceClientBuilder credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential);
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobServiceClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential);
        this.sharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobServiceClientBuilder credential(SASTokenCredential credential) {
        this.sasTokenCredential = Objects.requireNonNull(credential);
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Clears the credential used to authorize requests sent to the service
     * @return the updated BlobServiceClientBuilder object
     */
    public BlobServiceClientBuilder anonymousCredential() {
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated BlobServiceClientBuilder object
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain AccountName or AccountKey.
     */
    public BlobServiceClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 2);
            connectionKVPs.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }

        String accountName = connectionKVPs.get(Constants.ConnectionStringConstants.ACCOUNT_NAME);
        String accountKey = connectionKVPs.get(Constants.ConnectionStringConstants.ACCOUNT_KEY);
        String endpointProtocol = connectionKVPs.get(Constants.ConnectionStringConstants.ENDPOINT_PROTOCOL);
        String endpointSuffix = connectionKVPs.get(Constants.ConnectionStringConstants.ENDPOINT_SUFFIX);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }

        if (!ImplUtils.isNullOrEmpty(endpointProtocol) && !ImplUtils.isNullOrEmpty(endpointSuffix)) {
            String endpoint = String.format("%s://%s.blob%s", endpointProtocol, accountName, endpointSuffix);
            endpoint(endpoint);
        }

        // Use accountName and accountKey to get the SAS token using the credential class.
        return credential(new SharedKeyCredential(accountName, accountKey));
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated BlobServiceClientBuilder object
     */
    public BlobServiceClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public BlobServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(Objects.requireNonNull(pipelinePolicy));
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code logLevel} is {@code null}.
     */
    public BlobServiceClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = Objects.requireNonNull(logLevel);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint} when
     * building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated QueueClientBuilder object.
     */
    public BlobServiceClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link ConfigurationManager#getConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated QueueClientBuilder object.
     */
    public BlobServiceClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     * @param retryOptions the options to configure retry behaviors
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public BlobServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions);
        return this;
    }
}
