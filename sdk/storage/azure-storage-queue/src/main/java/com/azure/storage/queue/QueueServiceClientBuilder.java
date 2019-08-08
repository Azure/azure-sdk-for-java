// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Constants;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * QueueServiceClient queueServiceClients} and {@link QueueServiceAsyncClient queueServiceAsyncClients}, calling {@link
 * QueueServiceClientBuilder#buildClient() buildClient} constructs an instance of QueueServiceClient and calling {@link
 * QueueServiceClientBuilder#buildAsyncClient() buildAsyncClient} constructs an instance of QueueServiceAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage Queue service, name of the share, and authorization
 * credential.
 * {@link QueueServiceClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder
 * the A {@link SASTokenCredential} that authorizes the client.</p>
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
 * a connection string from the Storage Queue service must be used. Set the SharedKeyCredential with {@link
 * QueueServiceClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential
 * and SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
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
public final class QueueServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(QueueServiceClientBuilder.class);

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
     * Creates a builder instance that is able to configure and construct {@link QueueServiceClient QueueServiceClients}
     * and {@link QueueServiceAsyncClient QueueServiceAsyncClients}.
     */
    public QueueServiceClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
        configuration = ConfigurationManager.getConfiguration();
    }

    /**
     * Creates a {@link QueueServiceAsyncClient} based on options set in the builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link QueueServiceAsyncClient} is created.
     *
     * <p>
     * If {@link QueueServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * QueueServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link QueueServiceAsyncClient
     * client}. All other builder settings are ignored.
     * </p>
     *
     * @return A QueueServiceAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} have not been set.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been
     * set.
     */
    public QueueServiceAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint);

        if (pipeline != null) {
            return new QueueServiceAsyncClient(endpoint, pipeline);
        }

        if (sasTokenCredential == null && sharedKeyCredential == null) {
            logger.error("Credentials are required for authorization");
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

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new QueueServiceAsyncClient(endpoint, pipeline);
    }

    /**
     * Creates a {@link QueueServiceClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link QueueServiceClient} is created.
     *
     * <p>
     * If {@link QueueServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * QueueServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link QueueServiceClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A QueueServiceClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code queueName} have not been set.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been
     * set.
     */
    public QueueServiceClient buildClient() {
        return new QueueServiceClient(buildAsyncClient());
    }


    /**
     * Sets the endpoint for the Azure Storage Queue instance that the client will interact with.
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQueryParameters(Map)} in
     * an attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage Queue instance to send service requests to and receive responses
     * from.
     * @return the updated QueueServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public QueueServiceClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            URL fullURL = new URL(endpoint);
            this.endpoint = new URL(fullURL.getProtocol() + "://" + fullURL.getHost());

            // Attempt to get the SAS token from the URL passed
            this.sasTokenCredential = SASTokenCredential.fromQueryParameters(Utility.parseQueryString(fullURL.getQuery()));
            if (this.sasTokenCredential != null) {
                this.sharedKeyCredential = null;
            }
        } catch (MalformedURLException ex) {
            logger.error("The Azure Storage Queue endpoint url is malformed.");
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    /**
     * Sets the {@link SASTokenCredential} used to authenticate requests sent to the Queue service.
     *
     * @param credential SAS token credential generated from the Storage account that authorizes requests
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueServiceClientBuilder credential(SASTokenCredential credential) {
        this.sasTokenCredential = Objects.requireNonNull(credential);
        this.sharedKeyCredential = null;
        return this;
    }

    /**
     * Sets the {@link SharedKeyCredential} used to authenticate requests sent to the Queue service.
     *
     * @param credential Shared key credential can retrieve from the Storage account that authorizes requests
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public QueueServiceClientBuilder credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential);
        this.sasTokenCredential = null;
        return this;
    }


    /**
     * Creates a {@link SharedKeyCredential} from the {@code connectionString} used to authenticate requests sent to the
     * Queue service.
     *
     * @param connectionString Connection string from the Access Keys section in the Storage account
     * @return the updated QueueServiceClientBuilder object
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain AccountName or AccountKey.
     */
    public QueueServiceClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        Map<String, String> connectionStringParts = Utility.parseConnectionString(connectionString);

        String accountName = connectionStringParts.get(Constants.ConnectionStringConstants.ACCOUNT_NAME);
        String accountKey = connectionStringParts.get(Constants.ConnectionStringConstants.ACCOUNT_KEY);
        String endpointProtocol = connectionStringParts.get(Constants.ConnectionStringConstants.ENDPOINT_PROTOCOL);
        String endpointSuffix = connectionStringParts.get(Constants.ConnectionStringConstants.ENDPOINT_SUFFIX);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey");
        }

        if (!ImplUtils.isNullOrEmpty(endpointProtocol) && !ImplUtils.isNullOrEmpty(endpointSuffix)) {
            endpoint(String.format("%s://%s.file%s", endpointProtocol, accountName, endpointSuffix));
        }

        return credential(new SharedKeyCredential(accountName, accountKey));
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated QueueServiceClientBuilder object.
     */
    public QueueServiceClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after the {@link RetryPolicy}.
     *
     * @param pipelinePolicy The retry policy for service requests.
     * @return The updated QueueServiceClientBuilder object.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public QueueServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(Objects.requireNonNull(pipelinePolicy));
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated QueueServiceClientBuilder object.
     * @throws NullPointerException If {@code logLevel} is {@code null}.
     */
    public QueueServiceClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = Objects.requireNonNull(logLevel);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link
     * QueueServiceClientBuilder#endpoint(String) endpoint} when building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated QueueServiceClientBuilder object.
     */
    public QueueServiceClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

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
     * @return The updated QueueServiceClientBuilder object.
     */
    public QueueServiceClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
