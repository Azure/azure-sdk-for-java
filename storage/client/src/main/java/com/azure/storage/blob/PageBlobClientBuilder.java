// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.configuration.Configuration;
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
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent PageBlobClientBuilder for instantiating a {@link PageBlobClient} or {@link PageBlobAsyncClient}.
 *
 * <p>
 * An instance of this builder may only be created from static method {@link PageBlobClient#pageBlobClientBuilder()}.
 * The following information must be provided on this builder:
 *
 * <p><ul>
 *     <li>the endpoint through {@code .endpoint()}, including the container name and blob name, in the format of {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 *     <li>the credential through {@code .credentials()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link PageBlobClient} or {@code .buildAsyncClient()} to create a {@link PageBlobAsyncClient}.
 */
public final class PageBlobClientBuilder {
    private static final String ACCOUNT_NAME = "AccountName".toLowerCase();
    private static final String ACCOUNT_KEY = "AccountKey".toLowerCase();

    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    public PageBlobClientBuilder() {
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

        if (sharedKeyCredential != null) {
            policies.add(new SharedKeyCredentialPolicy(sharedKeyCredential));
        } else if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s://%s/.default", endpoint.getProtocol(), endpoint.getHost())));
        } else {
            policies.add(new AnonymousCredentialPolicy());
        }

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
     * @return a {@link PageBlobClient} created from the configurations in this builder.
     */
    public PageBlobClient buildClient() {
        return new PageBlobClient(buildAsyncClient());
    }

    /**
     * @return a {@link PageBlobAsyncClient} created from the configurations in this builder.
     */
    public PageBlobAsyncClient buildAsyncClient() {
        return new PageBlobAsyncClient(buildImpl());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name)
     * @param endpoint URL of the service
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder endpoint(String endpoint) {
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
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder credentials(SharedKeyCredential credentials) {
        this.sharedKeyCredential = credentials;
        return this;
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credentials
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder credentials(TokenCredential credentials) {
        this.tokenCredential = credentials;
        return this;
    }

    /**
     * Clears the credentials used to authorize requests sent to the service
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder anonymousCredentials() {
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder connectionString(String connectionString) {
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
        return credentials(new SharedKeyCredential(accountName, accountKey));
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client with
     * when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated PageBlobClientBuilder object
     */
    public PageBlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
