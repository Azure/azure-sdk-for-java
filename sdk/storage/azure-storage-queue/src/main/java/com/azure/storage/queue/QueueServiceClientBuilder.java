// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.logging.ClientLogger;
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
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link QueueServiceClient queueServiceClients}
 * and {@link QueueServiceAsyncClient queueServiceAsyncClients}, calling {@link QueueServiceClientBuilder#buildClient() buildClient}
 * constructs an instance of QueueServiceClient and calling {@link QueueServiceClientBuilder#buildAsyncClient() buildAsyncClient}
 * constructs an instance of QueueServiceAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage Queue service, name of the share, and authorization credential.
 * {@link QueueServiceClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * A {@link SASTokenCredential} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a {@code SASTokenCredential} they may
 * be set using {@link QueueServiceClientBuilder#credential(SASTokenCredential) credential} together with endpoint.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage Queue service must be used. Set the SharedKeyCredential with
 * {@link QueueServiceClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring}
 *
 * @see QueueServiceClient
 * @see QueueServiceAsyncClient
 * @see SASTokenCredential
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {QueueServiceClient.class, QueueServiceAsyncClient.class})
public final class QueueServiceClientBuilder extends BaseQueueClientBuilder<QueueServiceClientBuilder> {
    private final ClientLogger logger = new ClientLogger(QueueServiceClientBuilder.class);

    /**
     * Creates a builder instance that is able to configure and construct {@link QueueServiceClient QueueServiceClients}
     * and {@link QueueServiceAsyncClient QueueServiceAsyncClients}.
     */
    public QueueServiceClientBuilder() { }

    private AzureQueueStorageImpl constructImpl() {
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
     * Creates a {@link QueueServiceAsyncClient} based on options set in the builder. Every time {@code buildAsyncClient()} is
     * called a new instance of {@link QueueServiceAsyncClient} is created.
     *
     * <p>
     * If {@link QueueServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link QueueServiceClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link QueueServiceAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueServiceAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public QueueServiceAsyncClient buildAsyncClient() {
        return new QueueServiceAsyncClient(constructImpl());
    }

    /**
     * Creates a {@link QueueServiceClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link QueueServiceClient} is created.
     *
     * <p>
     * If {@link QueueServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link QueueServiceClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link QueueServiceClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueServiceClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public QueueServiceClient buildClient() {
        return new QueueServiceClient(buildAsyncClient());
    }


    /**
     * Sets the endpoint for the Azure Storage Queue instance that the client will interact with.
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQueryParameters(Map)} in an
     * attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage Queue instance to send service requests to and receive responses from.
     * @return the updated QueueServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    @Override
    public QueueServiceClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            URL fullURL = new URL(endpoint);
            super.endpoint = fullURL.getProtocol() + "://" + fullURL.getHost();

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential sasTokenCredential = SASTokenCredential.fromQueryParameters(Utility.parseQueryString(fullURL.getQuery()));
            if (sasTokenCredential != null) {
                super.credential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed."));
        }

        return this;
    }
}
