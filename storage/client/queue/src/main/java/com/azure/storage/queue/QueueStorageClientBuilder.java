// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

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
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link QueueStorageClient queueStoragesClients}
 * and {@link QueueStorageAsyncClient queueStorageAsyncClients}, calling {@link QueueStorageClientBuilder#buildClient() buildClient}
 * constructs an instance of QueueStorageClient and calling {@link QueueStorageClientBuilder#buildAsyncClient() buildAsyncClient}
 * constructs an instance of QueueStorageAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage Queue service, name of the share, and authorization credential.
 * {@link QueueStorageClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * A {@link SASTokenCredential} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueStorageClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueStorageAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain {@code SASTokenCredential} they may be set using
 * {@link QueueClientBuilder#credential(SASTokenCredential) credential}.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueStorageAsyncClient.instantiation.credential}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.queue.queueStorageAsyncClient.instantiation.credential}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a {@code SASTokenCredential} they may
 * be set using {@link QueueStorageClientBuilder#credential(SASTokenCredential) credential}.</p>
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage Queue service must be used. Set the SharedKeyCredential with
 * {@link QueueStorageClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous Queue Service Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueStorageClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.queue.queueStorageAsyncClient.instantiation.connectionstring}
 *
 * @see QueueStorageClient
 * @see QueueStorageAsyncClient
 * @see SASTokenCredential
 * @see SharedKeyCredential
 */
public final class QueueStorageClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(QueueStorageClientBuilder.class);
    private static final String ACCOUNT_NAME = "accountname";
    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private SASTokenCredential sasTokenCredential;
    private SharedKeyCredential sharedKeyCredential;
    private HttpClient httpClient;
    private HttpPipeline pipeline;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    /**
     * Creates a builder instance that is able to configure and construct {@link QueueStorageClient QueueStorageClients}
     * and {@link QueueStorageAsyncClient QueueStorageAsyncClients}.
     */
    public QueueStorageClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
        configuration = ConfigurationManager.getConfiguration();
    }

    /**
     * Creates a {@link QueueStorageAsyncClient} based on options set in the builder. Every time {@code buildAsyncClient()} is
     * called a new instance of {@link QueueStorageAsyncClient} is created.
     *
     * <p>
     * If {@link QueueStorageClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link QueueStorageClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link QueueStorageAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueStorageAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public QueueStorageAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint);

        if (sasTokenCredential == null && sharedKeyCredential == null) {
            LOGGER.asError().log("Credentials are required for authorization");
            throw new IllegalArgumentException("Credentials are required for authorization");
        }

        if (pipeline != null) {
            return new QueueStorageAsyncClient(endpoint, pipeline);
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

        return new QueueStorageAsyncClient(endpoint, pipeline);
    }

    /**
     * Creates a {@link QueueStorageClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link QueueStorageClient} is created.
     *
     * <p>
     * If {@link QueueStorageClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link QueueStorageClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link QueueStorageClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueStorageClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public QueueStorageClient buildClient() {
        return new QueueStorageClient(buildAsyncClient());
    }


    /**
     * Sets the endpoint for the Azure Storage Queue instance that the client will interact with.
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQuery(String) fromQuery} in an
     * attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage Queue instance to send service requests to and receive responses from.
     * @return the updated QueueStorageClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public QueueStorageClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            URL fullURL = new URL(endpoint);
            this.endpoint = new URL(fullURL.getProtocol() + "://" + fullURL.getHost());

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential credential = SASTokenCredential.fromQuery(fullURL.getQuery());
            if (credential != null) {
                this.sasTokenCredential = credential;
            }
        } catch (MalformedURLException ex) {
            LOGGER.asError().log("The Azure Storage Queue endpoint url is malformed.");
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    /**
     * Sets the {@link SASTokenCredential} used to authenticate requests sent to the Queue service.
     *
     * @param credential SAS token credential generated from the Storage account that authorizes requests
     * @return the updated QueueStorageClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueStorageClientBuilder credential(SASTokenCredential credential) {
        this.sasTokenCredential = Objects.requireNonNull(credential);
        return this;
    }

    /**
     * Sets the {@link SharedKeyCredential} used to authenticate requests sent to the Queue service.
     *
     * @param credential Shared key credential can retrieve from the Storage account that authorizes requests
     * @return the updated QueueStorageClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueStorageClientBuilder credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential);
        return this;
    }


    /**
     * Creates a {@link SharedKeyCredential} from the {@code connectionString} used to authenticate requests sent to the
     * Queue service.
     *
     * @param connectionString Connection string from the Access Keys section in the Storage account
     * @return the updated QueueStorageClientBuilder object
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public QueueStorageClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);
        this.sharedKeyCredential = SharedKeyCredential.fromConnectionString(connectionString);
        getEndPointFromConnectionString(connectionString);
        return this;
    }

    private void getEndPointFromConnectionString(String connectionString) {
        Map<String, String> connectionStringPieces = new HashMap<>();
        for (String connectionStringPiece : connectionString.split(";")) {
            String[] kvp = connectionStringPiece.split("=", 2);
            connectionStringPieces.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }
        String accountName = connectionStringPieces.get(ACCOUNT_NAME);
        try {
            this.endpoint = new URL(String.format("https://%s.queue.core.windows.net", accountName));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("There is no valid endpoint for the connection string. "
                + "Connection String: %s", connectionString));
        }
    }
    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated QueueStorageClientBuilder object.
     * @throws NullPointerException If {@code httpClient} is {@code null}.
     */
    public QueueStorageClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after the {@link RetryPolicy}.
     *
     * @param pipelinePolicy The retry policy for service requests.
     * @return The updated QueueStorageClientBuilder object.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public QueueStorageClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated QueueStorageClientBuilder object.
     */
    public QueueStorageClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link QueueStorageClientBuilder#endpoint(String) endpoint}
     * when building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated QueueStorageClientBuilder object.
     * @throws NullPointerException If {@code pipeline} is {@code null}.
     */
    public QueueStorageClientBuilder pipeline(HttpPipeline pipeline) {
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
     * @return The updated QueueStorageClientBuilder object.
     */
    public QueueStorageClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
