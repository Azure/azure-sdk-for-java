// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import com.azure.storage.queue.implementation.AzureQueueStorageBuilder;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link QueueClient
 * QueueClients} and {@link QueueAsyncClient QueueAsyncClients}, calling {@link QueueClientBuilder#buildClient()
 * buildClient} constructs an instance of QueueClient and calling {@link QueueClientBuilder#buildAsyncClient()
 * buildAsyncClient} constructs an instance of QueueAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage Queue service, name of the queue, and authorization
 * credentials.
 * {@link QueueClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * {@link QueueClientBuilder#queueName(String) queueName} and a {@link #sasToken(String) SAS token} that authorizes the
 * client.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the queue name or {@code SAS token} they may be set using
 * {@link QueueClientBuilder#queueName(String) queueName} and {@link QueueClientBuilder#sasToken(String) SAS token}.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Client with credential</strong></p>
 * {@codesnippet com.azure.storage.queue.queueClient.instantiation.credential}
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with credential</strong></p>
 * {@codesnippet com.azure.storage.queue.queueAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage Queue service must be used. Set the SharedKeyCredential with {@link
 * QueueClientBuilder#connectionString(String) connectionString}. If the builder has both a SAS token and
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
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {QueueClient.class, QueueAsyncClient.class})
public final class QueueClientBuilder {
    private final ClientLogger logger = new ClientLogger(QueueClientBuilder.class);

    private String endpoint;
    private String accountName;
    private String queueName;

    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;

    /**
     * Creates a builder instance that is able to configure and construct {@link QueueClient QueueClients} and {@link
     * QueueAsyncClient QueueAsyncClients}.
     */
    public QueueClientBuilder() {
    }

    private AzureQueueStorageImpl constructImpl() {
        Objects.requireNonNull(queueName, "'queueName' cannot be null.");

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (sharedKeyCredential != null) {
                return new SharedKeyCredentialPolicy(sharedKeyCredential);
            } else if (tokenCredential != null) {
                return new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint));
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                return null;
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration);

        return new AzureQueueStorageBuilder()
            .url(endpoint)
            .pipeline(pipeline)
            .build();
    }

    /**
     * Creates a {@link QueueClient} based on options set in the builder. Every time {@code buildClient()} is called a
     * new instance of {@link QueueClient} is created.
     *
     * <p>
     * If {@link QueueClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline}, {@link
     * QueueClientBuilder#endpoint(String) endpoint}, and {@link QueueClientBuilder#queueName(String) queueName} are
     * used to create the {@link QueueAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public QueueClient buildClient() {
        return new QueueClient(buildAsyncClient());
    }

    /**
     * Creates a {@link QueueAsyncClient} based on options set in the builder. Every time {@code buildAsyncClient()} is
     * called a new instance of {@link QueueAsyncClient} is created.
     *
     * <p>
     * If {@link QueueClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline}, {@link
     * QueueClientBuilder#endpoint(String) endpoint}, and {@link QueueClientBuilder#queueName(String) queueName} are
     * used to create the {@link QueueAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public QueueAsyncClient buildAsyncClient() {
        return new QueueAsyncClient(constructImpl(), queueName, accountName);
    }

    /**
     * Sets the endpoint for the Azure Storage Queue instance that the client will interact with.
     *
     * <p>The first path segment, if the endpoint contains path segments, will be assumed to be the name of the queue
     * that the client will interact with.</p>
     *
     * <p>Query parameters of the endpoint will be parsed in an attempt to generate a
     * {@link #sasToken(String) SAS token} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage Queue instance to send service requests to and receive responses
     * from.
     * @return the updated QueueClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public QueueClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        try {
            URL fullURL = new URL(endpoint);
            this.endpoint = fullURL.getProtocol() + "://" + fullURL.getHost();

            this.accountName = Utility.getAccountName(fullURL);

            // Attempt to get the queue name from the URL passed
            String[] pathSegments = fullURL.getPath().split("/", 2);
            if (pathSegments.length == 2 && !ImplUtils.isNullOrEmpty(pathSegments[1])) {
                this.queueName = pathSegments[1];
            }

            // Attempt to get the SAS token from the URL passed
            String sasToken = new QueueServiceSasQueryParameters(Utility
                .parseQueryStringSplitValues(fullURL.getQuery()), false).encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed. Endpoint: "
                    + endpoint));
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
        this.queueName = Objects.requireNonNull(queueName, "'queueName' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link SharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueClientBuilder credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.sharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public QueueClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Constructs a {@link SharedKeyCredential} used to authorize requests sent to the service. Additionally, if the
     * connection string contains `DefaultEndpointsProtocol` and `EndpointSuffix` it will set the {@link
     * #endpoint(String) endpoint}.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated QueueClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain `AccountName` or `AccountKey`.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public QueueClientBuilder connectionString(String connectionString) {
        BuilderHelper.configureConnectionString(connectionString, (accountName) -> this.accountName = accountName,
            this::credential, this::endpoint, logger);

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public QueueClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public QueueClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public QueueClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }
}
