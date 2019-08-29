// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.implementation.AzureQueueStorageBuilder;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link QueueClient QueueClients}
 * and {@link QueueAsyncClient QueueAsyncClients}, calling {@link QueueClientBuilder#buildClient() buildClient} constructs an
 * instance of QueueClient and calling {@link QueueClientBuilder#buildAsyncClient() buildAsyncClient} constructs an instance of
 * QueueAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage Queue service, name of the queue, and authorization credentials.
 * {@link QueueClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * {@link QueueClientBuilder#queueName(String) queueName} and a {@link SASTokenCredential} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the queue name or {@code SASTokenCredential} they may be set using
 * {@link QueueClientBuilder#queueName(String) queueName} and {@link QueueClientBuilder#credential(SASTokenCredential) credential}
 * together with endpoint..</p>
 *
 * <p><strong>Instantiating a synchronous Queue Client with credential</strong></p>
 * {@codesnippet com.azure.storage.queue.queueClient.instantiation.credential}
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with credential</strong></p>
 * {@codesnippet com.azure.storage.queue.queueAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage Queue service must be used. Set the SharedKeyCredential with
 * {@link QueueClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring}
 *
 * @see QueueClient
 * @see QueueAsyncClient
 * @see SASTokenCredential
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {QueueClient.class, QueueAsyncClient.class})
public final class QueueClientBuilder extends BaseClientBuilder {
    private final ClientLogger logger = new ClientLogger(QueueClientBuilder.class);
    private String queueName;

    /**
     * Creates a builder instance that is able to configure and construct {@link QueueClient QueueClients}
     * and {@link QueueAsyncClient QueueAsyncClients}.
     */
    public QueueClientBuilder() { }

    private AzureQueueStorageImpl constructImpl() {
        Objects.requireNonNull(queueName);

        if (!super.hasCredential()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Credentials are required for authorization"));
        }

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new AzureQueueStorageBuilder()
            .url(super.endpoint)
            .pipeline(pipeline)
            .build();
    }

    /**
     * Creates a {@link QueueClient} based on options set in the builder. Every time {@code buildClient()} is
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
    public QueueClient buildClient() {
        return new QueueClient(buildAsyncClient());
    }

    /**
     * Creates a {@link QueueAsyncClient} based on options set in the builder. Every time {@code buildAsyncClient()} is
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
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public QueueAsyncClient buildAsyncClient() {
        return new QueueAsyncClient(constructImpl(), queueName);
    }

    /**
     * Sets the endpoint for the Azure Storage Queue instance that the client will interact with.
     *
     * <p>The first path segment, if the endpoint contains path segments, will be assumed to be the name of the queue
     * that the client will interact with.</p>
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQueryParameters(Map)} in an
     * attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage Queue instance to send service requests to and receive responses from.
     * @return the updated QueueClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public QueueClientBuilder endpoint(String endpoint) {
        this.setEndpoint(endpoint);
        return this;
    }

    @Override
    protected void setEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            URL fullURL = new URL(endpoint);
            this.endpoint = fullURL.getProtocol() + "://" + fullURL.getHost();

            // Attempt to get the queue name from the URL passed
            String[] pathSegments = fullURL.getPath().split("/", 2);
            if (pathSegments.length == 2 && !ImplUtils.isNullOrEmpty(pathSegments[1])) {
                this.queueName = pathSegments[1];
            }

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential sasTokenCredential = SASTokenCredential.fromQueryParameters(Utility.parseQueryString(fullURL.getQuery()));
            if (sasTokenCredential != null) {
                super.setCredential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed. Endpoint: " + endpoint));
        }
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
     * Sets the {@link SASTokenCredential} used to authenticate requests sent to the Queue.
     *
     * @param credential SAS token credential generated from the Storage account that authorizes requests
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueClientBuilder credential(SASTokenCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Sets the {@link SharedKeyCredential} used to authenticate requests sent to the Queue.
     *
     * @param credential Shared key credential can retrieve from the Storage account that authorizes requests
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueClientBuilder credential(SharedKeyCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate requests sent to the Queue service.
     * @param credential authorization credential
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}
     */
    public QueueClientBuilder credential(TokenCredential credential) {
        super.setCredential(credential);
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
        super.parseConnectionString(connectionString);
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
        super.setHttpClient(httpClient);
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
        super.setAdditionalPolicy(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated QueueClientBuilder object.
     */
    public QueueClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        super.setHttpLogDetailLevel(logLevel);
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
        super.setPipeline(pipeline);
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
        super.setConfiguration(configuration);
        return this;
    }

    @Override
    protected String getServiceUrlMidfix() {
        return null;
    }

    @Override
    protected UserAgentPolicy getUserAgentPolicy() {
        return null;
    }
}
