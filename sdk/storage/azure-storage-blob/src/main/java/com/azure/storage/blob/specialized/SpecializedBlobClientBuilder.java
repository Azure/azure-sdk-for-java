// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BaseBlobClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageRange;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of specialized Storage Blob
 * clients, {@link AppendBlobClient}, {@link AppendBlobAsyncClient}, {@link BlockBlobClient},
 * {@link BlockBlobAsyncClient}, {@link PageBlobClient}, and {@link PageBlobAsyncClient}. These clients are used to
 * perform operations that are specific to the blob type.
 *
 * @see AppendBlobClient
 * @see AppendBlobAsyncClient
 * @see BlockBlobClient
 * @see BlockBlobAsyncClient
 * @see PageBlobClient
 * @see PageBlobAsyncClient
 */
@ServiceClientBuilder(serviceClients = {
    AppendBlobClient.class, AppendBlobAsyncClient.class,
    BlockBlobClient.class, BlockBlobAsyncClient.class,
    PageBlobClient.class, PageBlobAsyncClient.class
})
public final class SpecializedBlobClientBuilder extends BaseBlobClientBuilder<SpecializedBlobClientBuilder> {
    private final ClientLogger logger = new ClientLogger(SpecializedBlobClientBuilder.class);

    private String containerName;
    private String blobName;
    private String snapshot;

    /**
     * Creates a {@link AppendBlobClient} based on options set in the Builder. AppendBlobClients are used to perform
     * append blob specific operations such as {@link AppendBlobClient#appendBlock(InputStream, long) append block},
     * only use this when the blob is known to be an append blob.
     *
     * @return a {@link AppendBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public AppendBlobClient buildAppendBlobClient() {
        return new AppendBlobClient(buildAppendBlobAsyncClient());
    }

    /**
     * Creates a {@link AppendBlobAsyncClient} based on options set in the Builder. AppendBlobAsyncClients are used to
     * perform append blob specific operations such as {@link AppendBlobAsyncClient#appendBlock(Flux, long) append
     * blob}, only use this when the blob is known to be an append blob.
     *
     * @return a {@link AppendBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public AppendBlobAsyncClient buildAppendBlobAsyncClient() {
        return new AppendBlobAsyncClient(constructImpl(), snapshot, customerProvidedKey, accountName);
    }

    /**
     * Creates a {@link BlockBlobClient} based on options set in the Builder. BlockBlobClients are used to perform
     * generic upload operations such as {@link BlockBlobClient#upload(InputStream, long) upload from file} and block
     * blob specific operations such as {@link BlockBlobClient#stageBlock(String, InputStream, long) stage block} and
     * {@link BlockBlobClient#commitBlockList(List)}, only use this when the blob is known to be a block blob.
     *
     * @return a {@link BlockBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobClient buildBlockBlobClient() {
        return new BlockBlobClient(buildBlockBlobAsyncClient());
    }

    /**
     * Creates a {@link BlockBlobAsyncClient} based on options set in the Builder. BlockBlobAsyncClients are used to
     * perform generic upload operations such as {@link BlockBlobAsyncClient#upload(Flux, long) upload from file}
     * and block blob specific operations such as {@link BlockBlobAsyncClient#stageBlockWithResponse(String, Flux, long,
     * LeaseAccessConditions) stage block} and {@link BlockBlobAsyncClient#commitBlockList(List) commit block list},
     * only use this when the blob is known to be a block blob.
     *
     * @return a {@link BlockBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobAsyncClient buildBlockBlobAsyncClient() {
        return new BlockBlobAsyncClient(constructImpl(), snapshot, customerProvidedKey, accountName);
    }

    /**
     * Creates a {@link PageBlobClient} based on options set in the Builder. PageBlobClients are used to perform page
     * blob specific operations such as {@link PageBlobClient#uploadPages(PageRange, InputStream) upload pages} and
     * {@link PageBlobClient#clearPages(PageRange) clear pages}, only use this when the blob is known to be a page
     * blob.
     *
     * @return a {@link PageBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobClient buildPageBlobClient() {
        return new PageBlobClient(buildPageBlobAsyncClient());
    }

    /**
     * Creates a {@link PageBlobAsyncClient} based on options set in the Builder. PageBlobAsyncClients are used to
     * perform page blob specific operations such as {@link PageBlobAsyncClient#uploadPages(PageRange, Flux) upload
     * pages} and {@link PageBlobAsyncClient#clearPages(PageRange) clear pages}, only use this when the blob is known to
     * be a page blob.
     *
     * @return a {@link PageBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobAsyncClient buildPageBlobAsyncClient() {
        return new PageBlobAsyncClient(constructImpl(), snapshot, customerProvidedKey, accountName);
    }

    private AzureBlobStorageImpl constructImpl() {
        return new AzureBlobStorageBuilder()
            .pipeline(getPipeline())
            .url(String.format("%s/%s/%s", endpoint, containerName, blobName))
            .build();
    }

    /**
     * Configures the builder based on the {@link BlobClientBase}.
     *
     * @param blobClient The {@code BlobClientBase} used to configure this builder.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder blobClient(BlobClientBase blobClient) {
        pipeline(blobClient.getHttpPipeline());
        endpoint(blobClient.getBlobUrl());
        this.snapshot = blobClient.getSnapshotId();
        this.customerProvidedKey = blobClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Configures the builder based on the {@link BlobAsyncClientBase}.
     *
     * @param blobAsyncClient The {@code BlobAsyncClientBase} used to configure this builder.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder blobAsyncClient(BlobAsyncClientBase blobAsyncClient) {
        pipeline(blobAsyncClient.getHttpPipeline());
        endpoint(blobAsyncClient.getBlobUrl());
        this.snapshot = blobAsyncClient.getSnapshotId();
        this.customerProvidedKey = blobAsyncClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Configures the builder based on the {@link BlobContainerClient} and appends the blob name to the container's URL.
     *
     * @param blobContainerClient The {@code ContainerClient} used to configure this builder.
     * @param blobName Name of the blob.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder containerClient(BlobContainerClient blobContainerClient, String blobName) {
        pipeline(blobContainerClient.getHttpPipeline());
        endpoint(blobContainerClient.getBlobContainerUrl());
        blobName(blobName);
        this.customerProvidedKey = blobContainerClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Configures the builder based on the {@link BlobContainerAsyncClient} and appends the blob name to the container's
     * URL.
     *
     * @param blobContainerAsyncClient The {@code ContainerAsyncClient} used to configure this builder.
     * @param blobName Name of the blob.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder containerAsyncClient(BlobContainerAsyncClient blobContainerAsyncClient,
        String blobName) {
        pipeline(blobContainerAsyncClient.getHttpPipeline());
        endpoint(blobContainerAsyncClient.getBlobContainerUrl());
        blobName(blobName);
        this.customerProvidedKey = blobContainerAsyncClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     *
     * @param endpoint URL of the service
     * @return the updated BlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public SpecializedBlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.containerName = parts.getBlobContainerName();
            this.blobName = parts.getBlobName();
            this.snapshot = parts.getSnapshot();

            String sasToken = parts.getSasQueryParameters().encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
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
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.Builder.containerName#String}
     *
     * @param containerName the name of the container
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code containerName} is {@code null}
     */
    public SpecializedBlobClientBuilder containerName(String containerName) {
        this.containerName = Objects.requireNonNull(containerName);
        return this;
    }

    /**
     * Sets the name of the blob this client is connecting to.
     *
     * @param blobName the name of the blob
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public SpecializedBlobClientBuilder blobName(String blobName) {
        this.blobName = Objects.requireNonNull(blobName);
        return this;
    }

    /**
     * Sets the snapshot of the blob this client is connecting to.
     *
     * @param snapshot the snapshot identifier for the blob
     * @return the updated BlobClientBuilder object
     */
    public SpecializedBlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    @Override
    protected Class<SpecializedBlobClientBuilder> getClazz() {
        return SpecializedBlobClientBuilder.class;
    }
}
