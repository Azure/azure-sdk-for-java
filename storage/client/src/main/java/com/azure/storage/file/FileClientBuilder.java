// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private final RetryPolicy retryPolicy;

    private HttpLogDetailLevel logLevel;
    private Configuration configuration;
    private URL endpoint;
    private String shareName;
    private String filePath;
    private SASTokenCredential sasTokenCredential;
    private SharedKeyCredential sharedKeyCredential;
    private HttpClient httpClient;
    private String shareSnapshot;

    FileClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();

        configuration = ConfigurationManager.getConfiguration();
    }

    /**
     * @return a new instance of QueueServiceAsyncClient constructed with options stored in the builder
     * @throws IllegalArgumentException If the builder doesn't have credential
     */
    public FileAsyncClient buildAsync() {
        Objects.requireNonNull(endpoint);

        if (sasTokenCredential == null && sharedKeyCredential == null) {
            throw new IllegalArgumentException("Credentials are required for authorization");
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(FileConfiguration.NAME, FileConfiguration.VERSION, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        if (sharedKeyCredential != null) {
            policies.add(new SharedKeyCredentialPolicy(sharedKeyCredential));
        } else {
            policies.add(new SASTokenCredentialPolicy(sasTokenCredential));
        }

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
                                    .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                    .httpClient(httpClient)
                                    .build();

        return new FileAsyncClient(endpoint, pipeline, shareName, filePath, shareSnapshot);
    }

    public FileClient build() {
        return new FileClient(this.buildAsync());
    }

    public FileClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            UrlBuilder urlBuilder = UrlBuilder.parse(endpoint);
            this.endpoint = new UrlBuilder().withScheme(urlBuilder.scheme()).withHost(urlBuilder.host()).toURL();

            // Attempt to get the share name and file path from the URL passed
            String[] pathSegments = urlBuilder.path().split("/");
            int length = pathSegments.length;
            this.shareName = length >= 2 ? pathSegments[1] : null;
            String[] filePathParams = length >= 3 ? Arrays.copyOfRange(pathSegments, 2, length) : null;
            this.filePath = filePathParams != null ? String.join("/", filePathParams) : null;

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential credential = SASTokenCredential.fromQuery(urlBuilder.query());
            if (credential != null) {
                this.sasTokenCredential = credential;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage File endpoint url is malformed.");
        }

        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated QueueServiceAsyncClientBuilder object
     */
    public FileClientBuilder credential(SASTokenCredential credential) {
        this.sasTokenCredential = credential;
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated QueueServiceAsyncClientBuilder object
     */
    public FileClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);
        this.sharedKeyCredential = SharedKeyCredential.fromConnectionString(connectionString);
        return this;
    }

    public FileClientBuilder shareName (String shareName) {
        this.shareName = shareName;
        return this;
    }

    public FileClientBuilder filePath(String filePath) {
        this.filePath = filePath;
        return this;
    }
    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated QueueServiceAsyncClientBuilder object
     */
    public FileClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated QueueServiceAsyncClientBuilder object
     */
    public FileClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated QueueServiceAsyncClientBuilder object
     */
    public FileClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to build the client with
     * when they are not set in the builder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated QueueServiceAsyncClientBuilder object
     */
    public FileClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public FileClientBuilder shareSnapshot(String shareSnapshot) {
        this.shareSnapshot = shareSnapshot;
        return this;
    }
}
