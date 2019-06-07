// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

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
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.queue.credentials.SASTokenCredential;
import com.azure.storage.queue.credentials.SharedKeyCredential;
import com.azure.storage.queue.policy.SASTokenCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent builder for queue async clients.
 */
public final class QueueAsyncClientBuilder {
    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private String queueName;
    private SASTokenCredential sasTokenCredential;
    private SharedKeyCredential sharedKeyCredential;
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    QueueAsyncClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();

        configuration = ConfigurationManager.getConfiguration();
    }

    /**
     * Constructs an instance of QueueAsyncClient based on the configurations stored in the builder.
     * @return a new client instance
     */
    public QueueAsyncClient build() {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(sasTokenCredential);
        Objects.requireNonNull(queueName);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(QueueConfiguration.NAME, QueueConfiguration.VERSION, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());
        policies.add(new SASTokenCredentialPolicy(sasTokenCredential));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new QueueAsyncClient(endpoint, pipeline, queueName);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, queue name)
     * @param endpoint URL of the service
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            URL fullURL = new URL(endpoint);
            this.endpoint = new URL(fullURL.getProtocol() + "://" + fullURL.getHost());

            // Attempt to get the queue name from the URL passed
            String[] pathSegments = fullURL.getPath().split("/", 2);
            if (pathSegments.length == 2 && !ImplUtils.isNullOrEmpty(pathSegments[1])) {
                this.queueName = pathSegments[1];
            }

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential credential = QueueServiceAsyncClientBuilder.getCredentialFromQueryParam(fullURL.getQuery());
            if (credential != null) {
                this.sasTokenCredential = credential;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    /**
     * Sets the queue name
     * @param queueName Name of the queue
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder queueName(String queueName) {
        this.queueName = Objects.requireNonNull(queueName);
        return this;
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credentials
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder credentials(SASTokenCredential credentials) {
        this.sasTokenCredential = credentials;
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);
        this.sharedKeyCredential = QueueServiceAsyncClientBuilder.getSharedKeyFromConnectionString(connectionString);
        return this;
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to build the client with
     * when they are not set in the builder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated QueueAsyncClientBuilder object
     */
    public QueueAsyncClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
