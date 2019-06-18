// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.configuration.Configuration;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent BlobClientBuilder for instantiating a {@link BlobClient} or {@link BlobAsyncClient}.
 *
 * <p>
 * An instance of this builder may only be created from static method {@link BlobClient#blobClientBuilder()}.
 * The following information must be provided on this builder:
 *
 * <p><ul>
 *     <li>the endpoint through {@code .endpoint()}, including the container name and blob name, in the format of {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 *     <li>the credential through {@code .credentials()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link BlobClient} or {@code .buildAsyncClient()} to create a {@link BlobAsyncClient}.
 */
public final class BlobClientBuilder {
    private static final String ACCOUNT_NAME = "AccountName".toLowerCase();
    private static final String ACCOUNT_KEY = "AccountKey".toLowerCase();

    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private ICredentials credentials = new AnonymousCredentials();
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    public BlobClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    private AzureBlobStorageBuilder buildImpl() {
        Objects.requireNonNull(endpoint);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());
        policies.add(credentials); // This needs to be a different credential type.

        policies.add(retryPolicy);

        policies.addAll(this.policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new AzureBlobStorageBuilder()
            .url(endpoint.toString())
            .pipeline(pipeline);
    }

    /**
     * @return a {@link BlobClient} created from the configurations in this builder.
     */
    public BlobClient buildClient() {
        return new BlobClient(buildAsyncClient());
    }

    /**
     * @return a {@link BlobAsyncClient} created from the configurations in this builder.
     */
    public BlobAsyncClient buildAsyncClient() {
        return new BlobAsyncClient(buildImpl());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     * @param endpoint URL of the service
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            this.endpoint = new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credentials
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder credentials(SharedKeyCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credentials
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder credentials(TokenCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Clears the credentials used to authorize requests sent to the service
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder anonymousCredentials() {
        this.credentials = new AnonymousCredentials();
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 2);
            connectionKVPs.put(kvp[0].toLowerCase(), kvp[1]);
        }

        String accountName = connectionKVPs.get(ACCOUNT_NAME);
        String accountKey = connectionKVPs.get(ACCOUNT_KEY);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }

        // Use accountName and accountKey to get the SAS token using the credential class.
        credentials = new SharedKeyCredentials(accountName, accountKey);

        return this;
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client with
     * when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
