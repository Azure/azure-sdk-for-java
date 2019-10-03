// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Fluent ContainerClientBuilder for instantiating a {@link BlobContainerClient} or {@link BlobContainerAsyncClient}
 * using {@link BlobContainerClientBuilder#buildClient()} or {@link BlobContainerClientBuilder#buildAsyncClient()}
 * respectively.
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
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a {@link
 * BlobContainerClient} or {@code .buildAsyncClient()} to create a {@link BlobContainerAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {BlobContainerClient.class, BlobContainerAsyncClient.class})
public final class BlobContainerClientBuilder extends BaseBlobClientBuilder<BlobContainerClientBuilder> {

    private final ClientLogger logger = new ClientLogger(BlobContainerClientBuilder.class);

    private String containerName;

    /**
     * Creates a builder instance that is able to configure and construct {@link BlobContainerClient ContainerClients}
     * and {@link BlobContainerAsyncClient ContainerAsyncClients}.
     */
    public BlobContainerClientBuilder() {
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClientBuilder.buildClient}
     *
     * @return a {@link BlobContainerClient} created from the configurations in this builder.
     */
    public BlobContainerClient buildClient() {
        return new BlobContainerClient(buildAsyncClient());
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClientBuilder.buildAsyncClient}
     *
     * @return a {@link BlobContainerAsyncClient} created from the configurations in this builder.
     */
    public BlobContainerAsyncClient buildAsyncClient() {
        Objects.requireNonNull(containerName);

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        String url = String.format("%s/%s", endpoint, containerName);
        if (!ImplUtils.isNullOrEmpty(sasToken)) {
            url = url + "?" + sasToken;
        }

        return new BlobContainerAsyncClient(new AzureBlobStorageBuilder()
            .url(url)
            .pipeline(pipeline)
            .build(), customerProvidedKey);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name)
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClientBuilder.endpoint#String}
     *
     * @param endpoint URL of the service
     * @return the updated ContainerClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public BlobContainerClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobURLParts parts = BlobURLParts.parse(url);

            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.containerName = parts.getBlobContainerName();

            String sasToken = parts.getSasQueryParameters().encode();
            if (ImplUtils.isNullOrEmpty(sasToken)) {
                super.sasToken(sasToken);
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClientBuilder.containerName#String}
     *
     * @param containerName the name of the container
     * @return the updated ContainerClientBuilder object
     */
    public BlobContainerClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    @Override
    protected Class<BlobContainerClientBuilder> getClazz() {
        return BlobContainerClientBuilder.class;
    }

    String endpoint() {
        return this.endpoint;
    }
}
