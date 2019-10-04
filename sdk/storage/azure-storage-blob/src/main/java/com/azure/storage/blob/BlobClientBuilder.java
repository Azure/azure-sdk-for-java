// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.common.credentials.SasTokenCredential;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link BlobClient BlobClients} and {@link BlobAsyncClient BlobAsyncClients} when
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} as called
 * respectively.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the container name and blob name, in the format of
 * {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly
 * accessible.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {BlobClient.class, BlobAsyncClient.class })
public final class BlobClientBuilder extends BaseBlobClientBuilder<BlobClientBuilder> {

    private final ClientLogger logger = new ClientLogger(BlobClientBuilder.class);

    private String containerName;
    private String blobName;
    private String snapshot;

    /**
     * Creates a builder instance that is able to configure and construct {@link BlobClient BlobClients} and
     * {@link BlobAsyncClient BlobAsyncClients}.
     */
    public BlobClientBuilder() {
    }

    /**
     * Creates a {@link BlobClient} based on options set in the Builder. BlobClients are used to perform generic blob
     * methods such as {@link BlobClient#download(OutputStream) download} and {@link BlobClient#getProperties() get
     * properties}, use this when the blob type is unknown.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.Builder.buildClient}
     *
     * @return a {@link BlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlobClient buildClient() {
        return new BlobClient(buildAsyncClient());
    }

    /**
     * Creates a {@link BlobAsyncClient} based on options set in the Builder. BlobAsyncClients are used to perform
     * generic blob methods such as {@link BlobAsyncClient#download() download} and {@link
     * BlobAsyncClient#getProperties()}, use this when the blob type is unknown.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.Builder.buildAsyncClient}
     *
     * @return a {@link BlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlobAsyncClient buildAsyncClient() {
        Objects.requireNonNull(containerName, "'containerName' cannot be null.");
        Objects.requireNonNull(blobName, "'blobName' cannot be null.");

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new BlobAsyncClient(new AzureBlobStorageBuilder()
            .url(String.format("%s/%s/%s", endpoint, containerName, blobName))
            .pipeline(pipeline)
            .build(), snapshot, customerProvidedKey);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.Builder.endpoint#String}
     *
     * @param endpoint URL of the service
     * @return the updated BlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public BlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.containerName = parts.getBlobContainerName();
            this.blobName = parts.getBlobName();
            this.snapshot = parts.getSnapshot();

            SasTokenCredential sasTokenCredential =
                SasTokenCredential.fromSasTokenString(parts.getSasQueryParameters().encode());
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
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.Builder.containerName#String}
     *
     * @param containerName the name of the container
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code containerName} is {@code null}
     */
    public BlobClientBuilder containerName(String containerName) {
        this.containerName = Objects.requireNonNull(containerName, "'containerName' cannot be null.");
        return this;
    }

    /**
     * Sets the name of the blob this client is connecting to.
     *
     * @param blobName the name of the blob
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public BlobClientBuilder blobName(String blobName) {
        this.blobName = Objects.requireNonNull(blobName, "'blobName' cannot be null.");
        return this;
    }

    /**
     * Sets the snapshot of the blob this client is connecting to.
     *
     * @param snapshot the snapshot identifier for the blob
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    @Override
    protected Class<BlobClientBuilder> getClazz() {
        return BlobClientBuilder.class;
    }
}
