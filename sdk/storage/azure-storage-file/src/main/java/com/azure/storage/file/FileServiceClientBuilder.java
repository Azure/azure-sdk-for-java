// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link FileServiceClient FileServiceClients}
 * and {@link FileServiceAsyncClient FileServiceAsyncClients}, calling {@link FileServiceClientBuilder#buildClient() buildFileClient}
 * constructs an instance of FileServiceClient and calling {@link FileServiceClientBuilder#buildAsyncClient() buildFileAsyncClient}
 * constructs an instance of FileServiceAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage File service and authorization credential.
 * {@link FileServiceClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder a
 * {@link SASTokenCredential} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous FileService Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous FileService Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a {@code SASTokenCredential} they may
 * be set using {@link FileServiceClientBuilder#credential(SASTokenCredential) credential}.</p>
 *
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.credential}
 *
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage File service must be used. Set the SharedKeyCredential with
 * {@link FileServiceClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous FileService Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous FileService Client with connection string. </strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.connectionstring}
 *
 * @see FileServiceClient
 * @see FileServiceAsyncClient
 * @see SASTokenCredential
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {FileServiceClient.class, FileServiceAsyncClient.class})
public final class FileServiceClientBuilder extends BaseClientBuilder {
    private final ClientLogger logger = new ClientLogger(FileServiceClientBuilder.class);

    /**
     * Creates a builder instance that is able to configure and construct {@link FileServiceClient FileServiceClients}
     * and {@link FileServiceAsyncClient FileServiceAsyncClients}.
     */
    public FileServiceClientBuilder() {}

    private AzureFileStorageImpl constructImpl() {
        if (!super.hasCredential()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Credentials are required for authorization"));
        }

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new AzureFileStorageBuilder()
            .url(super.endpoint)
            .pipeline(pipeline)
            .build();
    }

    /**
     * Creates a {@link FileServiceAsyncClient} based on options set in the builder. Every time {@code buildFileAsyncClient()} is
     * called a new instance of {@link FileServiceAsyncClient} is created.
     *
     * <p>
     * If {@link FileServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link FileServiceClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link FileServiceAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A FileServiceAsyncClient with the options set from the builder.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public FileServiceAsyncClient buildAsyncClient() {
        return new FileServiceAsyncClient(constructImpl());
    }

    /**
     * Creates a {@link FileServiceClient} based on options set in the builder. Every time {@code buildFileClient()} is
     * called a new instance of {@link FileServiceClient} is created.
     *
     * <p>
     * If {@link FileServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link FileServiceClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link FileServiceClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A FileServiceClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public FileServiceClient buildClient() {
        return new FileServiceClient(buildAsyncClient());
    }

    /**
     * Sets the endpoint for the Azure Storage File instance that the client will interact with.
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQueryParameters(Map)} in an
     * attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage File instance to send service requests to and receive responses from.
     * @return the updated FileServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public FileServiceClientBuilder endpoint(String endpoint) {
        this.setEndpoint(endpoint);
        return this;
    }


    @Override
    protected void setEndpoint(String endpoint) {
        try {
            URL fullURL = new URL(endpoint);
            super.endpoint = fullURL.getProtocol() + "://" + fullURL.getHost();

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential sasTokenCredential = SASTokenCredential.fromQueryParameters(Utility.parseQueryString(fullURL.getQuery()));
            if (sasTokenCredential != null) {
                super.setCredential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage File Service endpoint url is malformed."));
        }
    }

    /**
     * Sets the {@link SASTokenCredential} used to authenticate requests sent to the Queue service.
     *
     * @param credential SAS token credential generated from the Storage account that authorizes requests
     * @return the updated FileServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public FileServiceClientBuilder credential(SASTokenCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Sets the {@link SharedKeyCredential} used to authenticate requests sent to the Queue service.
     *
     * @param credential Shared key credential generated from the Storage account that authorizes requests
     * @return the updated FileServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public FileServiceClientBuilder credential(SharedKeyCredential credential) {
        super.setCredential(credential);
        return this;
    }

    // File service does not support oauth, so the setter for a TokenCredential is not exposed.

    /**
     * Creates a {@link SharedKeyCredential} from the {@code connectionString} used to authenticate requests sent to the
     * File service.
     *
     * @param connectionString Connection string from the Access Keys section in the Storage account
     * @return the updated FileServiceClientBuilder object
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public FileServiceClientBuilder connectionString(String connectionString) {
        super.parseConnectionString(connectionString);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated FileServiceClientBuilder object.
     * @throws NullPointerException If {@code httpClient} is {@code null}.
     */
    public FileServiceClientBuilder httpClient(HttpClient httpClient) {
        super.setHttpClient(httpClient);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after the {@link RetryPolicy}.
     *
     * @param pipelinePolicy The retry policy for service requests.
     * @return The updated FileServiceClientBuilder object.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public FileServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        super.setAdditionalPolicy(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated FileServiceClientBuilder object.
     */
    public FileServiceClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        super.setHttpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link FileServiceClientBuilder#endpoint(String) endpoint}
     * when building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated FileServiceClientBuilder object.
     * @throws NullPointerException If {@code pipeline} is {@code null}.
     */
    public FileServiceClientBuilder pipeline(HttpPipeline pipeline) {
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
     * @return The updated FileServiceClientBuilder object.
     * @throws NullPointerException If {@code configuration} is {@code null}.
     */
    public FileServiceClientBuilder configuration(Configuration configuration) {
        super.setConfiguration(configuration);
        return this;
    }

    @Override
    protected UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(FileConfiguration.NAME, FileConfiguration.VERSION, super.getConfiguration());
    }

    @Override
    protected String getServiceUrlMidfix() {
        return "file";
    }
}
