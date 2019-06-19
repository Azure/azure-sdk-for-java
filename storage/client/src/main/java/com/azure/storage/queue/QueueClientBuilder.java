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
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link QueueClient QueueClients}
 * and {@link QueueAsyncClient QueueAsyncClients}, calling {@link QueueClientBuilder#buildSync() buildSync} constructs an
 * instance of QueueClient and calling {@link QueueClientBuilder#buildAsync() buildAsync} constructs an instance of
 * QueueAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage Queue service, name of the queue, and authorization credentials.
 * {@link QueueClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * {@link QueueClientBuilder#queueName(String) queueName} and a {@link SASTokenCredential} that authorizes the client.</p>
 *
 * <pre>
 * QueueClient client = QueueClient.builder()
 *     .endpoint(endpointWithQueueNameAndSASTokenQueryParams)
 *     .buildSync();
 * </pre>
 *
 * <pre>
 * QueueAsyncClient client = QueueAsyncClient.builder()
 *     .endpoint(endpointWithQueueNameAndSASTokenQueryParams)
 *     .buildAsync();
 * </pre>
 *
 * <p>If the {@code endpoint} doesn't contain the queue name or {@code SASTokenCredential} they may be set using
 * {@link QueueClientBuilder#queueName(String) queueName} and {@link QueueClientBuilder#credential(SASTokenCredential) credential}.</p>
 *
 * <pre>
 * QueueClient client = QueueClient.builder()
 *     .endpoint(endpointWithoutQueueNameOrSASTokenQueryParams)
 *     .queueName(queueName)
 *     .credential(SASTokenCredential.fromQuery(SASTokenQueryParams))
 *     .buildSync();
 * </pre>
 *
 * <pre>
 * QueueAsyncClient client = QueueAsyncClient.builder()
 *     .endpoint(endpointWithoutQueueNameOrSASTokenQueryParams)
 *     .queueName(queueName)
 *     .credential(SASTokenCredential.fromQuery(SASTokenQueryParams))
 *     .buildAsync();
 * </pre>
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage Queue service must be used. Set the SharedKeyCredential with
 * {@link QueueClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <pre>
 * QueueClient client = QueueClient().builder()
 *     .endpoint(endpoint)
 *     .queueName(queueName)
 *     .connectionString(connectionString)
 *     .buildSync();
 * </pre>
 *
 * <pre>
 * QueueAsyncClient client = QueueAsyncClient().builder()
 *     .endpoint(endpoint)
 *     .queueName(queueName)
 *     .connectionString(connectionString)
 *     .buildAsync();
 * </pre>
 *
 * @see QueueClient
 * @see QueueAsyncClient
 * @see SASTokenCredential
 * @see SharedKeyCredential
 */
public final class QueueClientBuilder {
    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private String queueName;
    private SASTokenCredential sasTokenCredential;
    private SharedKeyCredential sharedKeyCredential;
    private HttpClient httpClient;
    private HttpPipeline pipeline;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    QueueClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();

        configuration = ConfigurationManager.getConfiguration();
    }

    /**
     * Creates a {@link QueueAsyncClient} based on options set in the builder. Every time {@code buildAsync()} is
     * called a new instance of {@link QueueAsyncClient} is created.
     *
     * <p>
     * If {@link QueueClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline},
     * {@link QueueClientBuilder#endpoint(String) endpoint}, and
     * {@link QueueClientBuilder#queueName(String) queueName} are used to create the {@link QueueAsyncClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A QueueAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public QueueAsyncClient buildAsync() {
        return build();
    }

    /**
     * Creates a {@link QueueClient} based on options set in the builder. Every time {@code buildSync()} is
     * called a new instance of {@link QueueClient} is created.
     *
     * <p>
     * If {@link QueueClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline},
     * {@link QueueClientBuilder#endpoint(String) endpoint}, and
     * {@link QueueClientBuilder#queueName(String) queueName} are used to create the {@link QueueAsyncClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A QueueClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public QueueClient buildSync() {
        return new QueueClient(build());
    }

    /*
     * Constructs an instance of QueueAsyncClient based on the configurations stored in the builder.
     * @return a new client instance
     */
    private QueueAsyncClient build() {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(queueName);

        if (pipeline != null) {
            return new QueueAsyncClient(endpoint, pipeline, queueName);
        }

        if (sasTokenCredential == null && sharedKeyCredential == null) {
            throw new IllegalArgumentException("Credentials are required for authorization");
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(QueueConfiguration.NAME, QueueConfiguration.VERSION, configuration));
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

        return new QueueAsyncClient(endpoint, pipeline, queueName);
    }

    /**
     * Sets the endpoint for the Azure Storage Queue instance that the client will interact with.
     *
     * <p>The first path segment, if the endpoint contains path segments, will be assumed to be the name of the queue
     * that the client will interact with.</p>
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQuery(String) fromQuery} in an
     * attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage Queue instance to send service requests to and receive responses from.
     * @return the updated QueueClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public QueueClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            UrlBuilder urlBuilder = UrlBuilder.parse(endpoint);
            URL fullURL = new URL(endpoint);
            this.endpoint = new URL(fullURL.getProtocol() + "://" + fullURL.getHost());

            // Attempt to get the queue name from the URL passed
            String[] pathSegments = fullURL.getPath().split("/", 2);
            if (pathSegments.length == 2 && !ImplUtils.isNullOrEmpty(pathSegments[1])) {
                this.queueName = pathSegments[1];
            }

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential credential = SASTokenCredential.fromQuery(fullURL.getQuery());
            if (credential != null) {
                this.sasTokenCredential = credential;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    /**
     * Sets the name of the queue that the client will interact with.
     *
     * @param queueName Name of the queue
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code queueName} is {@code null}.
     */
    public QueueClientBuilder queueName(String queueName) {
        this.queueName = Objects.requireNonNull(queueName);
        return this;
    }

    /**
     * Sets the {@link SASTokenCredential} used to authenticate requests sent to the Queue service.
     *
     * @param credential SAS token credential generated from the Storage account that authorizes requests
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueClientBuilder credential(SASTokenCredential credential) {
        this.sasTokenCredential = Objects.requireNonNull(credential);
        return this;
    }

    /**
     * Creates a {@link SharedKeyCredential} from the {@code connectionString} used to authenticate requests sent to the
     * Queue service.
     *
     * @param connectionString Connection string from the Access Keys section in the Storage account
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public QueueClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);
        this.sharedKeyCredential = SharedKeyCredential.fromConnectionString(connectionString);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated QueueClientBuilder object.
     * @throws NullPointerException If {@code httpClient} is {@code null}.
     */
    public QueueClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after the {@link RetryPolicy}.
     *
     * @param pipelinePolicy The retry policy for service requests.
     * @return The updated QueueClientBuilder object.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public QueueClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy);
        this.policies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated QueueClientBuilder object.
     */
    public QueueClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link QueueClientBuilder#endpoint(String) endpoint}
     * and {@link QueueClientBuilder#queueName(String) queueName} when building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated QueueClientBuilder object.
     * @throws NullPointerException If {@code pipeline} is {@code null}.
     */
    public QueueClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
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
    public QueueClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
