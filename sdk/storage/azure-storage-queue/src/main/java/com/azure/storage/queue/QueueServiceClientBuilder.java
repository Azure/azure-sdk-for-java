// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.implementation.AzureQueueStorageImplBuilder;
import com.azure.storage.queue.implementation.util.BuilderHelper;
import com.azure.storage.queue.models.QueueMessageDecodingError;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * QueueServiceClient queueServiceClients} and {@link QueueServiceAsyncClient queueServiceAsyncClients}, calling {@link
 * QueueServiceClientBuilder#buildClient() buildClient} constructs an instance of QueueServiceClient and calling {@link
 * QueueServiceClientBuilder#buildAsyncClient() buildAsyncClient} constructs an instance of QueueServiceAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage Queue service, name of the share, and authorization
 * credential. {@link QueueServiceClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give
 * the builder the a SAS token that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a SAS token they may be set using
 * {@link #sasToken(String) sasToken} together with endpoint.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link StorageSharedKeyCredential}. To create a
 * StorageSharedKeyCredential a connection string from the Storage Queue service must be used.
 * Set the StorageSharedKeyCredential with {@link QueueServiceClientBuilder#connectionString(String) connectionString}.
 * If the builder has both a SAS token and StorageSharedKeyCredential the StorageSharedKeyCredential will be preferred
 * when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring}
 *
 * @see QueueServiceClient
 * @see QueueServiceAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {QueueServiceClient.class, QueueServiceAsyncClient.class})
public final class QueueServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(QueueServiceClientBuilder.class);

    private String endpoint;
    private String accountName;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private AzureSasCredential azureSasCredential;
    private String sasToken;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private ClientOptions clientOptions = new ClientOptions();
    private Configuration configuration;
    private QueueServiceVersion version;

    private QueueMessageEncoding messageEncoding = QueueMessageEncoding.NONE;
    private Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler;
    private Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler;

    /**
     * Creates a builder instance that is able to configure and construct {@link QueueServiceClient QueueServiceClients}
     * and {@link QueueServiceAsyncClient QueueServiceAsyncClients}.
     */
    public QueueServiceClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
    }

    /**
     * Creates a {@link QueueServiceAsyncClient} based on options set in the builder. Every time this is called a new
     * instance of {@link QueueServiceAsyncClient} is created.
     *
     * <p>
     * If {@link QueueServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * QueueServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link QueueServiceAsyncClient
     * client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueServiceAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential} or
     * {@link #sasToken(String) SAS token} has been set.
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public QueueServiceAsyncClient buildAsyncClient() {
        if (processMessageDecodingErrorAsyncHandler != null && processMessageDecodingErrorHandler != null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Either processMessageDecodingError or processMessageDecodingAsyncError should be specified"
                    + "but not both.")
            );
        }

        QueueServiceVersion serviceVersion = version != null ? version : QueueServiceVersion.getLatest();
        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken,
            endpoint, retryOptions, logOptions,
            clientOptions, httpClient, perCallPolicies, perRetryPolicies, configuration, logger);

        AzureQueueStorageImpl azureQueueStorage = new AzureQueueStorageImplBuilder()
            .url(endpoint)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();

        return new QueueServiceAsyncClient(azureQueueStorage, accountName, serviceVersion,
            messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler);
    }

    /**
     * Creates a {@link QueueServiceClient} based on options set in the builder. Every time this is called a new
     * instance of {@link QueueServiceClient} is created.
     *
     * <p>
     * If {@link QueueServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * QueueServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link QueueServiceClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A QueueServiceClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public QueueServiceClient buildClient() {
        return new QueueServiceClient(buildAsyncClient());
    }


    /**
     * Sets the endpoint for the Azure Storage Queue instance that the client will interact with.
     *
     * <p>Query parameters of the endpoint will be parsed in an attempt to generate a SAS token to authenticate
     * requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage Queue instance to send service requests to and receive responses
     * from.
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     * @throws IllegalArgumentException If {@code endpoint} is a malformed URL.
     */
    public QueueServiceClientBuilder endpoint(String endpoint) {
        BuilderHelper.QueueUrlParts parts = BuilderHelper.parseEndpoint(endpoint, logger);
        this.endpoint = parts.getEndpoint();
        this.accountName = parts.getAccountName();
        if (!CoreUtils.isNullOrEmpty(parts.getSasToken())) {
            sasToken(parts.getSasToken());
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated QueueServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueServiceClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TokenCredential}.
     * @return the updated QueueServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueServiceClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests. This string should only be the query parameters
     * (with or without a leading '?') and not a full url.
     * @return the updated QueueServiceClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public QueueServiceClientBuilder sasToken(String sasToken) {
        this.sasToken = Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     * @return the updated QueueServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueServiceClientBuilder credential(AzureSasCredential credential) {
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'credential' cannot be null.");
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated QueueServiceClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} is invalid.
     */
    public QueueServiceClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
                = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getQueueEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw logger
                    .logExceptionAsError(new IllegalArgumentException(
                            "connectionString missing required settings to derive queue service endpoint."));
        }
        this.endpoint(endpoint.getPrimaryUri());
        if (storageConnectionString.getAccountName() != null) {
            this.accountName = storageConnectionString.getAccountName();
        }
        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        if (authSettings.getType() == StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY) {
            this.credential(new StorageSharedKeyCredential(authSettings.getAccount().getName(),
                    authSettings.getAccount().getAccessKey()));
        } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
            this.sasToken(authSettings.getSasToken());
        }
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If
     * the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public QueueServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null");
        if (pipelinePolicy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(pipelinePolicy);
        } else {
            perRetryPolicies.add(pipelinePolicy);
        }
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public QueueServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Gets the default Storage allowlist log headers and query parameters.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        return BuilderHelper.getDefaultHttpLogOptions();
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public QueueServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the client options for all the requests made through the client.
     *
     * @param clientOptions {@link ClientOptions}.
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    public QueueServiceClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the queue message encoding.
     *
     * @param messageEncoding {@link QueueMessageEncoding}.
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code messageEncoding} is {@code null}.
     */
    public QueueServiceClientBuilder messageEncoding(QueueMessageEncoding messageEncoding) {
        this.messageEncoding = Objects.requireNonNull(messageEncoding, "'messageEncoding' cannot be null.");
        return this;
    }

    /**
     * Sets the asynchronous handler that performs the tasks needed when a message is received or peaked from the queue
     * but cannot be decoded.
     * <p>
     * Such message can be received or peaked when queue is expecting certain {@link QueueMessageEncoding}
     * but there's another producer that is not encoding messages in expected way.
     * I.e. the queue contains messages with different encoding.
     * <p>
     * {@link QueueMessageDecodingError} contains {@link QueueAsyncClient} for the queue that has received
     * the message as well as {@link QueueMessageDecodingError#getQueueMessageItem()} or
     * {@link QueueMessageDecodingError#getPeekedMessageItem()}  with raw body, i.e. no decoding will be attempted
     * so that body can be inspected as has been received from the queue.
     * <p>
     * The handler won't attempt to remove the message from the queue. Therefore such handling should be included into
     * handler itself.
     * <p>
     * The handler will be shared by all queue clients that are created from {@link QueueServiceClient} or
     * {@link QueueServiceAsyncClient} built by this builder.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.QueueServiceClientBuilder#processMessageDecodingErrorAsyncHandler}
     *
     * @param processMessageDecodingErrorAsyncHandler the handler.
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder processMessageDecodingErrorAsync(
        Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler) {
        this.processMessageDecodingErrorAsyncHandler = processMessageDecodingErrorAsyncHandler;
        return this;
    }

    /**
     * Sets the handler that performs the tasks needed when a message is received or peaked from the queue
     * but cannot be decoded.
     * <p>
     * Such message can be received or peaked when queue is expecting certain {@link QueueMessageEncoding}
     * but there's another producer that is not encoding messages in expected way.
     * I.e. the queue contains messages with different encoding.
     * <p>
     * {@link QueueMessageDecodingError} contains {@link QueueAsyncClient} for the queue that has received
     * the message as well as {@link QueueMessageDecodingError#getQueueMessageItem()} or
     * {@link QueueMessageDecodingError#getPeekedMessageItem()}  with raw body, i.e. no decoding will be attempted
     * so that body can be inspected as has been received from the queue.
     * <p>
     * The handler won't attempt to remove the message from the queue. Therefore such handling should be included into
     * handler itself.
     * <p>
     * The handler will be shared by all queue clients that are created from {@link QueueServiceClient} or
     * {@link QueueServiceAsyncClient} built by this builder.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.QueueServiceClientBuilder#processMessageDecodingErrorHandler}
     *
     * @param processMessageDecodingErrorHandler the handler.
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder processMessageDecodingError(
        Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler) {
        this.processMessageDecodingErrorHandler = processMessageDecodingErrorHandler;
        return this;
    }

    /**
     * Sets the {@link QueueServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link QueueServiceVersion} of the service to be used when making requests.
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder serviceVersion(QueueServiceVersion version) {
        this.version = version;
        return this;
    }
}
