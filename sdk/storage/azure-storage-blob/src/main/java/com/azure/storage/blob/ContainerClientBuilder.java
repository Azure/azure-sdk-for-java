// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.common.credentials.SASTokenCredential;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Fluent ContainerClientBuilder for instantiating a {@link ContainerClient} or {@link ContainerAsyncClient} using
 * {@link ContainerClientBuilder#buildClient()} or {@link ContainerClientBuilder#buildAsyncClient()} respectively.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the container name, in the format of {@code
 * https://{accountName}.blob.core.windows.net/{containerName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly
 * accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a {@link ContainerClient}
 * or {@code .buildAsyncClient()} to create a {@link ContainerAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {ContainerClient.class, ContainerAsyncClient.class})
public final class ContainerClientBuilder extends BaseBlobClientBuilder<ContainerClientBuilder> {

    private final ClientLogger logger = new ClientLogger(ContainerClientBuilder.class);

    private String containerName;

    /**
     * Creates a builder instance that is able to configure and construct {@link ContainerClient ContainerClients} and
     * {@link ContainerAsyncClient ContainerAsyncClients}.
     */
    public ContainerClientBuilder() {
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClientBuilder.buildClient}
     *
     * @return a {@link ContainerClient} created from the configurations in this builder.
     */
    public ContainerClient buildClient() {
        return new ContainerClient(buildAsyncClient());
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClientBuilder.buildAsyncClient}
     *
     * @return a {@link ContainerAsyncClient} created from the configurations in this builder.
     */
    public ContainerAsyncClient buildAsyncClient() {
        Objects.requireNonNull(containerName);

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new ContainerAsyncClient(new AzureBlobStorageBuilder()
            .url(String.format("%s/%s", endpoint, containerName))
            .pipeline(pipeline)
            .build(), cpk);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name)
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClientBuilder.endpoint#String}
     *
     * @param endpoint URL of the service
     * @return the updated ContainerClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public ContainerClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobURLParts parts = URLParser.parse(url);

            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.containerName = parts.getContainerName();

            SASTokenCredential sasTokenCredential = SASTokenCredential
                .fromSASTokenString(parts.getSasQueryParameters().encode());
            if (sasTokenCredential != null) {
                super.credential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the name of the container this client is connecting to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClientBuilder.containerName#String}
     *
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
}
