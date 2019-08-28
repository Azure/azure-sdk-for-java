// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Fluent BlobServiceClientBuilder for instantiating a {@link BlobServiceClient} or {@link BlobServiceAsyncClient}
 * using {@link BlobServiceClientBuilder#buildClient()} or {@link BlobServiceClientBuilder#buildAsyncClient()} respectively.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 *     <li>the endpoint through {@code .endpoint()}, in the format of {@code https://{accountName}.blob.core.windows.net}.
 *     <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link BlobServiceClient} or {@code .buildAsyncClient()} to create a {@link BlobServiceAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {BlobServiceClient.class, BlobServiceAsyncClient.class})
public final class BlobServiceClientBuilder extends BaseClientBuilder {

    private final ClientLogger logger = new ClientLogger(BlobServiceClientBuilder.class);


    /**
     * Creates a builder instance that is able to configure and construct {@link BlobServiceClient BlobServiceClients}
     * and {@link BlobServiceAsyncClient BlobServiceAsyncClients}.
     */
    public BlobServiceClientBuilder() {}

    /**
     * @return a {@link BlobServiceClient} created from the configurations in this builder.
     */
    public BlobServiceClient buildClient() {
        return new BlobServiceClient(buildAsyncClient());
    }

    /**
     * @return a {@link BlobServiceAsyncClient} created from the configurations in this builder.
     */
    public BlobServiceAsyncClient buildAsyncClient() {
        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new BlobServiceAsyncClient(new AzureBlobStorageBuilder()
            .url(super.endpoint)
            .pipeline(pipeline)
            .build());
    }

    /**
     * Sets the blob service endpoint, additionally parses it for information (SAS token)
     *
     * @param endpoint URL of the service
     * @return the updated BlobServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public BlobServiceClientBuilder endpoint(String endpoint) {
        this.setEndpoint(endpoint);
        return this;
    }

    @Override
    protected void setEndpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            super.endpoint = url.getProtocol() + "://" + url.getAuthority();

            SASTokenCredential sasTokenCredential = SASTokenCredential.fromSASTokenString(URLParser.parse(url).sasQueryParameters().encode());
            if (sasTokenCredential != null) {
                super.setCredential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage endpoint url is malformed."));
        }
    }

    String endpoint() {
        return super.endpoint;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobServiceClientBuilder credential(SharedKeyCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobServiceClientBuilder credential(TokenCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobServiceClientBuilder credential(SASTokenCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Clears the credential used to authorize requests sent to the service
     * @return the updated BlobServiceClientBuilder object
     */
    public BlobServiceClientBuilder anonymousCredential() {
        super.setAnonymousCredential();
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated BlobServiceClientBuilder object
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain AccountName or AccountKey.
     */
    public BlobServiceClientBuilder connectionString(String connectionString) {
        super.parseConnectionString(connectionString);
        return this;
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code httpClient} is {@code null}.
     */
    public BlobServiceClientBuilder httpClient(HttpClient httpClient) {
        super.setHttpClient(httpClient);
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public BlobServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        super.setAdditionalPolicy(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated BlobServiceClientBuilder object
     */
    public BlobServiceClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        super.setHttpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client with
     * when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated BlobServiceClientBuilder object
     */
    public BlobServiceClientBuilder configuration(Configuration configuration) {
        super.setConfiguration(configuration);
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     * @param retryOptions the options to configure retry behaviors
     * @return the updated BlobServiceClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public BlobServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        super.setRetryOptions(retryOptions);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link BlobServiceClientBuilder#endpoint(String) endpoint} when building clients.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated BlobServiceClientBuilder object.
     */
    public BlobServiceClientBuilder pipeline(HttpPipeline pipeline) {
        super.setPipeline(pipeline);
        return this;
    }

    @Override
    protected UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, super.getConfiguration());
    }
}
