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
import java.util.Objects;

/**
 * Fluent ContainerClientBuilder for instantiating a {@link ContainerClient} or {@link ContainerAsyncClient}
 * using {@link ContainerClientBuilder#buildClient()} or {@link ContainerClientBuilder#buildAsyncClient()} respectively.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 *     <li>the endpoint through {@code .endpoint()}, including the container name, in the format of {@code https://{accountName}.blob.core.windows.net/{containerName}}.
 *     <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link ContainerClient} or {@code .buildAsyncClient()} to create a {@link ContainerAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {ContainerClient.class, ContainerAsyncClient.class})
public final class ContainerClientBuilder extends BaseClientBuilder {

    private final ClientLogger logger = new ClientLogger(ContainerClientBuilder.class);

    private String containerName;

    /**
     * Creates a builder instance that is able to configure and construct {@link ContainerClient ContainerClients}
     * and {@link ContainerAsyncClient ContainerAsyncClients}.
     */
    public ContainerClientBuilder() {}

    /**
     * @return a {@link ContainerClient} created from the configurations in this builder.
     */
    public ContainerClient buildClient() {
        return new ContainerClient(buildAsyncClient());
    }

    /**
     * @return a {@link ContainerAsyncClient} created from the configurations in this builder.
     */
    public ContainerAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(containerName);

        HttpPipeline pipeline = buildPipeline();

        return new ContainerAsyncClient(new AzureBlobStorageBuilder()
            .url(String.format("%s/%s", endpoint, containerName))
            .pipeline(pipeline)
            .build());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name)
     * @param endpoint URL of the service
     * @return the updated ContainerClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public ContainerClientBuilder endpoint(String endpoint) {
        this.setEndpoint(endpoint);
        return this;
    }

    @Override
    protected void setEndpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobURLParts parts = URLParser.parse(url);

            this.endpoint = parts.scheme() + "://" + parts.host();
            this.containerName = parts.containerName();

            this.sasTokenCredential = SASTokenCredential.fromSASTokenString(parts.sasQueryParameters().encode());
            if (this.sasTokenCredential != null) {
                this.tokenCredential = null;
                this.sharedKeyCredential = null;
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }
    }

    /**
     * Sets the name of the container this client is connecting to.
     * @param containerName the name of the container
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    String endpoint() {
        return this.endpoint;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ContainerClientBuilder credential(SharedKeyCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ContainerClientBuilder credential(TokenCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ContainerClientBuilder credential(SASTokenCredential credential) {
        super.setCredential(credential);
        return this;
    }

    /**
     * Clears the credential used to authorize requests sent to the service
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder anonymousCredential() {
        super.setAnonymousCredential();
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated ContainerClientBuilder object
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain AccountName or AccountKey
     */
    public ContainerClientBuilder connectionString(String connectionString) {
        super.parseConnectionString(connectionString);
        return this;
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code httpClient} is {@code null}.
     */
    public ContainerClientBuilder httpClient(HttpClient httpClient) {
        super.httpClient = Objects.requireNonNull(httpClient);
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public ContainerClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        super.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy));
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        super.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client with
     * when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder configuration(Configuration configuration) {
        super.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     * @param retryOptions the options to configure retry behaviors
     * @return the updated ContainerClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public ContainerClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        super.retryOptions = Objects.requireNonNull(retryOptions);
        return this;
    }

    @Override
    protected UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, configuration);
    }
}
