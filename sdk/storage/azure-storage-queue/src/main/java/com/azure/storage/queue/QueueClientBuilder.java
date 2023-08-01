// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureNamedKeyCredentialTrait;
import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
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
 * <!-- src_embed com.azure.storage.queue.queueClient.instantiation.sastoken -->
 * <pre>
 * QueueClient client = new QueueClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.queue.core.windows.net?$&#123;SASToken&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueClient.instantiation.sastoken -->
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with SAS token</strong></p>
 * <!-- src_embed com.azure.storage.queue.queueAsyncClient.instantiation.sastoken -->
 * <pre>
 * QueueAsyncClient queueAsyncClient = new QueueClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;&#123;accountName&#125;.queue.core.windows.net?&#123;SASToken&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueAsyncClient.instantiation.sastoken -->
 *
 * <p>If the {@code endpoint} doesn't contain the queue name or {@code SAS token} they may be set using
 * {@link QueueClientBuilder#queueName(String) queueName} and {@link QueueClientBuilder#sasToken(String) SAS token}.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Client with credential</strong></p>
 * <!-- src_embed com.azure.storage.queue.queueClient.instantiation.credential -->
 * <pre>
 * QueueClient client = new QueueClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.queue.core.windows.net&quot;&#41;
 *     .queueName&#40;&quot;myqueue&quot;&#41;
 *     .sasToken&#40;&quot;&#123;SASTokenQueryParams&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueClient.instantiation.credential -->
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with credential</strong></p>
 * <!-- src_embed com.azure.storage.queue.queueAsyncClient.instantiation.credential -->
 * <pre>
 * QueueAsyncClient queueAsyncClient = new QueueClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;&#123;accountName&#125;.queue.core.windows.net&quot;&#41;
 *     .queueName&#40;&quot;myqueue&quot;&#41;
 *     .sasToken&#40;&quot;&#123;SASTokenQueryParams&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueAsyncClient.instantiation.credential -->
 *
 * <p>Another way to authenticate the client is using a {@link StorageSharedKeyCredential}. To create a
 * StorageSharedKeyCredential a connection string from the Storage Queue service must be used.
 * Set the StorageSharedKeyCredential with {@link QueueClientBuilder#connectionString(String) connectionString}.
 * If the builder has both a SAS token and StorageSharedKeyCredential the StorageSharedKeyCredential will be preferred
 * when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Client with connection string.</strong></p>
 * <!-- src_embed com.azure.storage.queue.queueClient.instantiation.connectionstring -->
 * <pre>
 * String connectionString = &quot;DefaultEndpointsProtocol=https;AccountName=&#123;name&#125;;&quot;
 *     + &quot;AccountKey=&#123;key&#125;;EndpointSuffix=&#123;core.windows.net&#125;&quot;;
 * QueueClient client = new QueueClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueClient.instantiation.connectionstring -->
 *
 * <p><strong>Instantiating an Asynchronous Queue Client with connection string.</strong></p>
 * <!-- src_embed com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring -->
 * <pre>
 * String connectionString = &quot;DefaultEndpointsProtocol=https;AccountName=&#123;name&#125;;&quot;
 *     + &quot;AccountKey=&#123;key&#125;;EndpointSuffix=&#123;core.windows.net&#125;&quot;;
 * QueueAsyncClient queueAsyncClient = new QueueClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring -->
 *
 * @see QueueClient
 * @see QueueAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {QueueClient.class, QueueAsyncClient.class})
public final class QueueClientBuilder implements
    TokenCredentialTrait<QueueClientBuilder>,
    ConnectionStringTrait<QueueClientBuilder>,
    AzureNamedKeyCredentialTrait<QueueClientBuilder>,
    AzureSasCredentialTrait<QueueClientBuilder>,
    HttpTrait<QueueClientBuilder>,
    ConfigurationTrait<QueueClientBuilder>,
    EndpointTrait<QueueClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(QueueClientBuilder.class);

    private String endpoint;
    private String accountName;
    private String queueName;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private AzureSasCredential azureSasCredential;
    private String sasToken;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions;
    private RetryOptions coreRetryOptions;
    private HttpPipeline httpPipeline;

    private ClientOptions clientOptions = new ClientOptions();
    private Configuration configuration;
    private QueueServiceVersion version;

    private QueueMessageEncoding messageEncoding = QueueMessageEncoding.NONE;
    private Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler;
    private Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler;

    /**
     * Creates a builder instance that is able to configure and construct {@link QueueClient QueueClients} and {@link
     * QueueAsyncClient QueueAsyncClients}.
     */
    public QueueClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
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
     * @throws IllegalStateException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public QueueClient buildClient() {
        StorageImplUtils.assertNotNull("queueName", queueName);
        if (processMessageDecodingErrorAsyncHandler != null && processMessageDecodingErrorHandler != null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Either processMessageDecodingError or processMessageDecodingAsyncError should be specified"
                    + "but not both.")
            );
        }
        if (processMessageDecodingErrorAsyncHandler != null) {
            LOGGER.warning("Please use processMessageDecodingErrorHandler for QueueClient.");
        }
        QueueServiceVersion serviceVersion = version != null ? version : QueueServiceVersion.getLatest();
        AzureQueueStorageImpl queueStorage = createAzureQueueStorageImpl(serviceVersion);
        QueueAsyncClient asyncClient = new QueueAsyncClient(queueStorage, queueName, accountName, serviceVersion,
            messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler, null);
        return new QueueClient(createAzureQueueStorageImpl(serviceVersion), queueName, accountName, serviceVersion,
            messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler, asyncClient);
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
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public QueueAsyncClient buildAsyncClient() {
        StorageImplUtils.assertNotNull("queueName", queueName);
        if (processMessageDecodingErrorAsyncHandler != null && processMessageDecodingErrorHandler != null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Either processMessageDecodingError or processMessageDecodingAsyncError should be specified"
                    + "but not both.")
            );
        }
        if (processMessageDecodingErrorHandler != null) {
            LOGGER.warning("Please use processMessageDecodingErrorAsyncHandler for QueueAsyncClient.");
        }
        QueueServiceVersion serviceVersion = version != null ? version : QueueServiceVersion.getLatest();
        AzureQueueStorageImpl queueStorage = createAzureQueueStorageImpl(serviceVersion);
        QueueClient queueClient = new QueueClient(queueStorage, queueName, accountName, serviceVersion,
            messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler, null);
        return new QueueAsyncClient(createAzureQueueStorageImpl(serviceVersion), queueName, accountName, serviceVersion,
            messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler, queueClient);
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
    @Override
    public QueueClientBuilder endpoint(String endpoint) {
        BuilderHelper.QueueUrlParts parts = BuilderHelper.parseEndpoint(endpoint, LOGGER);
        this.endpoint = parts.getEndpoint();
        this.accountName = parts.getAccountName();
        this.queueName = parts.getQueueName() == null ? this.queueName : parts.getQueueName();

        if (!CoreUtils.isNullOrEmpty(parts.getSasToken())) {
            sasToken(parts.getSasToken());
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
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureNamedKeyCredential}.
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public QueueClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        return credential(StorageSharedKeyCredential.fromAzureNamedKeyCredential(credential));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public QueueClientBuilder credential(TokenCredential credential) {
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
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public QueueClientBuilder sasToken(String sasToken) {
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
     * @return the updated QueueClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public QueueClientBuilder credential(AzureSasCredential credential) {
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'credential' cannot be null.");
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated QueueClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} is invalid.
     */
    @Override
    public QueueClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
                = StorageConnectionString.create(connectionString, LOGGER);
        StorageEndpoint endpoint = storageConnectionString.getQueueEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw LOGGER
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
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return the updated QueueClientBuilder object
     */
    @Override
    public QueueClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            LOGGER.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public QueueClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null");
        if (pipelinePolicy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(pipelinePolicy);
        } else {
            perRetryPolicies.add(pipelinePolicy);
        }
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    @Override
    public QueueClientBuilder httpLogOptions(HttpLogOptions logOptions) {
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
     * @return the updated QueueClientBuilder object
     */
    @Override
    public QueueClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated QueueClientBuilder object.
     */
    public QueueClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RequestRetryOptions)}.
     * Consider using {@link #retryOptions(RequestRetryOptions)} to also set storage specific options.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return the updated QueueClientBuilder object
     */
    @Override
    public QueueClientBuilder retryOptions(RetryOptions retryOptions) {
        this.coreRetryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * The {@link #endpoint(String) endpoint} is not ignored when {@code pipeline} is set.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return the updated QueueClientBuilder object
     */
    @Override
    public QueueClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @see HttpClientOptions
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    @Override
    public QueueClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the queue message encoding.
     *
     * @param messageEncoding {@link QueueMessageEncoding}.
     * @return the updated QueueClientBuilder object
     * @throws NullPointerException If {@code messageEncoding} is {@code null}.
     */
    public QueueClientBuilder messageEncoding(QueueMessageEncoding messageEncoding) {
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
     * The handler won't attempt to remove the message from the queue. Therefore, such handling should be included into
     * handler itself.
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueClientBuilder#processMessageDecodingErrorAsyncHandler -->
     * <pre>
     * String connectionString = &quot;DefaultEndpointsProtocol=https;AccountName=&#123;name&#125;;&quot;
     *     + &quot;AccountKey=&#123;key&#125;;EndpointSuffix=&#123;core.windows.net&#125;&quot;;
     *
     * Function&lt;QueueMessageDecodingError, Mono&lt;Void&gt;&gt; processMessageDecodingErrorHandler =
     *     &#40;queueMessageDecodingFailure&#41; -&gt; &#123;
     *         QueueMessageItem queueMessageItem = queueMessageDecodingFailure.getQueueMessageItem&#40;&#41;;
     *         PeekedMessageItem peekedMessageItem = queueMessageDecodingFailure.getPeekedMessageItem&#40;&#41;;
     *         if &#40;queueMessageItem != null&#41; &#123;
     *             System.out.printf&#40;&quot;Received badly encoded message, messageId=%s, messageBody=%s&quot;,
     *                 queueMessageItem.getMessageId&#40;&#41;,
     *                 queueMessageItem.getBody&#40;&#41;.toString&#40;&#41;&#41;;
     *             return queueMessageDecodingFailure
     *                 .getQueueAsyncClient&#40;&#41;
     *                 .deleteMessage&#40;queueMessageItem.getMessageId&#40;&#41;, queueMessageItem.getPopReceipt&#40;&#41;&#41;;
     *         &#125; else if &#40;peekedMessageItem != null&#41; &#123;
     *             System.out.printf&#40;&quot;Peeked badly encoded message, messageId=%s, messageBody=%s&quot;,
     *                 peekedMessageItem.getMessageId&#40;&#41;,
     *                 peekedMessageItem.getBody&#40;&#41;.toString&#40;&#41;&#41;;
     *             return Mono.empty&#40;&#41;;
     *         &#125; else &#123;
     *             return Mono.empty&#40;&#41;;
     *         &#125;
     *     &#125;;
     *
     * QueueClient client = new QueueClientBuilder&#40;&#41;
     *     .connectionString&#40;connectionString&#41;
     *     .processMessageDecodingErrorAsync&#40;processMessageDecodingErrorHandler&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueClientBuilder#processMessageDecodingErrorAsyncHandler -->
     *
     * @param processMessageDecodingErrorAsyncHandler the handler.
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder processMessageDecodingErrorAsync(
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
     * The handler won't attempt to remove the message from the queue. Therefore, such handling should be included into
     * handler itself.
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueClientBuilder#processMessageDecodingErrorHandler -->
     * <pre>
     * String connectionString = &quot;DefaultEndpointsProtocol=https;AccountName=&#123;name&#125;;&quot;
     *     + &quot;AccountKey=&#123;key&#125;;EndpointSuffix=&#123;core.windows.net&#125;&quot;;
     *
     * Consumer&lt;QueueMessageDecodingError&gt; processMessageDecodingErrorHandler =
     *     &#40;queueMessageDecodingFailure&#41; -&gt; &#123;
     *         QueueMessageItem queueMessageItem = queueMessageDecodingFailure.getQueueMessageItem&#40;&#41;;
     *         PeekedMessageItem peekedMessageItem = queueMessageDecodingFailure.getPeekedMessageItem&#40;&#41;;
     *         if &#40;queueMessageItem != null&#41; &#123;
     *             System.out.printf&#40;&quot;Received badly encoded message, messageId=%s, messageBody=%s&quot;,
     *                 queueMessageItem.getMessageId&#40;&#41;,
     *                 queueMessageItem.getBody&#40;&#41;.toString&#40;&#41;&#41;;
     *             queueMessageDecodingFailure
     *                 .getQueueClient&#40;&#41;
     *                 .deleteMessage&#40;queueMessageItem.getMessageId&#40;&#41;, queueMessageItem.getPopReceipt&#40;&#41;&#41;;
     *         &#125; else if &#40;peekedMessageItem != null&#41; &#123;
     *             System.out.printf&#40;&quot;Peeked badly encoded message, messageId=%s, messageBody=%s&quot;,
     *                 peekedMessageItem.getMessageId&#40;&#41;,
     *                 peekedMessageItem.getBody&#40;&#41;.toString&#40;&#41;&#41;;
     *         &#125;
     *     &#125;;
     *
     * QueueClient client = new QueueClientBuilder&#40;&#41;
     *     .connectionString&#40;connectionString&#41;
     *     .processMessageDecodingError&#40;processMessageDecodingErrorHandler&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueClientBuilder#processMessageDecodingErrorHandler -->
     *
     * @param processMessageDecodingErrorHandler the handler.
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder processMessageDecodingError(
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
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder serviceVersion(QueueServiceVersion version) {
        this.version = version;
        return this;
    }

    private AzureQueueStorageImpl createAzureQueueStorageImpl(QueueServiceVersion version) {
        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken,
            endpoint, retryOptions, coreRetryOptions, logOptions,
            clientOptions, httpClient, perCallPolicies, perRetryPolicies, configuration, LOGGER);

        return new AzureQueueStorageImplBuilder()
            .url(endpoint)
            .pipeline(pipeline)
            .version(version.getVersion())
            .buildClient();
    }
}
