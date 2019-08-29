// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.credentials.SASTokenCredential;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation Storage Blob clients.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 *     <li>the endpoint through {@code .endpoint()}, including the container name and blob name, in the format of {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 *     <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder use the following mapping to construct the given client:
 * <ul>
 *     <li>{@link BlobClientBuilder#buildBlobClient()} - {@link BlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildBlobAsyncClient()} - {@link BlobAsyncClient}</li>
 *     <li>{@link BlobClientBuilder#buildAppendBlobClient()} - {@link AppendBlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildAppendBlobAsyncClient()} - {@link AppendBlobAsyncClient}</li>
 *     <li>{@link BlobClientBuilder#buildBlockBlobClient()} - {@link BlockBlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildBlockBlobAsyncClient()} - {@link BlockBlobAsyncClient}</li>
 *     <li>{@link BlobClientBuilder#buildPageBlobClient()} - {@link PageBlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildPageBlobAsyncClient()} - {@link PageBlobAsyncClient}</li>
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {BlobClient.class, BlobAsyncClient.class, AppendBlobClient.class,
    AppendBlobAsyncClient.class, BlockBlobClient.class, BlockBlobAsyncClient.class, PageBlobClient.class,
    PageBlobAsyncClient.class})
public final class BlobClientBuilder extends BaseBlobClientBuilder<BlobClientBuilder> {

    private final ClientLogger logger = new ClientLogger(BlobClientBuilder.class);

    private String containerName;
    private String blobName;
    private String snapshot;

    /**
     * Creates a builder instance that is able to configure and construct Storage Blob clients.
     */
    public BlobClientBuilder() { }

    private AzureBlobStorageImpl constructImpl() {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new AzureBlobStorageBuilder()
            .url(String.format("%s/%s/%s", endpoint, containerName, blobName))
            .pipeline(pipeline)
            .build();
    }

    /**
     * Creates a {@link BlobClient} based on options set in the Builder. BlobClients are used to perform generic blob
     * methods such as {@link BlobClient#download(OutputStream) download} and
     * {@link BlobClient#getProperties() get properties}, use this when the blob type is unknown.
     *
     * @return a {@link BlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlobClient buildBlobClient() {
        return new BlobClient(buildBlobAsyncClient());
    }

    /**
     * Creates a {@link BlobAsyncClient} based on options set in the Builder. BlobAsyncClients are used to perform
     * generic blob methods such as {@link BlobAsyncClient#download() download} and
     * {@link BlobAsyncClient#getProperties()}, use this when the blob type is unknown.
     *
     * @return a {@link BlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlobAsyncClient buildBlobAsyncClient() {
        return new BlobAsyncClient(constructImpl(), snapshot);
    }

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
     * perform append blob specific operations such as {@link AppendBlobAsyncClient#appendBlock(Flux, long) append blob},
     * only use this when the blob is known to be an append blob.
     *
     * @return a {@link AppendBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public AppendBlobAsyncClient buildAppendBlobAsyncClient() {
        return new AppendBlobAsyncClient(constructImpl(), snapshot);
    }

    /**
     * Creates a {@link BlockBlobClient} based on options set in the Builder. BlockBlobClients are used to perform
     * generic upload operations such as {@link BlockBlobClient#uploadFromFile(String) upload from file} and block
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
     * perform generic upload operations such as {@link BlockBlobAsyncClient#uploadFromFile(String) upload from file}
     * and block blob specific operations such as {@link BlockBlobAsyncClient#stageBlockWithResponse(String, Flux, long, LeaseAccessConditions) stage block}
     * and {@link BlockBlobAsyncClient#commitBlockList(List) commit block list}, only use this when the blob is known to
     * be a block blob.
     *
     * @return a {@link BlockBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobAsyncClient buildBlockBlobAsyncClient() {
        return new BlockBlobAsyncClient(constructImpl(), snapshot);
    }

    /**
     * Creates a {@link PageBlobClient} based on options set in the Builder. PageBlobClients are used to perform page
     * blob specific operations such as {@link PageBlobClient#uploadPages(PageRange, InputStream) upload pages} and
     * {@link PageBlobClient#clearPages(PageRange) clear pages}, only use this when the blob is known to be a page blob.
     *
     * @return a {@link PageBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobClient buildPageBlobClient() {
        return new PageBlobClient(buildPageBlobAsyncClient());
    }

    /**
     * Creates a {@link PageBlobAsyncClient} based on options set in the Builder. PageBlobAsyncClients are used to
     * perform page blob specific operations such as {@link PageBlobAsyncClient#uploadPages(PageRange, Flux) upload pages}
     * and {@link PageBlobAsyncClient#clearPages(PageRange) clear pages}, only use this when the blob is known to be a
     * page blob.
     *
     * @return a {@link PageBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobAsyncClient buildPageBlobAsyncClient() {
        return new PageBlobAsyncClient(constructImpl(), snapshot);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     * @param endpoint URL of the service
     * @return the updated BlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public BlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobURLParts parts = URLParser.parse(url);

            this.endpoint = parts.scheme() + "://" + parts.host();
            this.containerName = parts.containerName();
            this.blobName = parts.blobName();
            this.snapshot = parts.snapshot();

            SASTokenCredential sasTokenCredential = SASTokenCredential.fromSASTokenString(parts.sasQueryParameters().encode());
            if (sasTokenCredential != null) {
                super.credential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }
        return this;
    }

    /**
     * Sets the name of the container this client is connecting to.
     * @param containerName the name of the container
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code containerName} is {@code null}
     */
    public BlobClientBuilder containerName(String containerName) {
        this.containerName = Objects.requireNonNull(containerName);
        return this;
    }

    /**
     * Sets the name of the blob this client is connecting to.
     * @param blobName the name of the blob
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public BlobClientBuilder blobName(String blobName) {
        this.blobName = Objects.requireNonNull(blobName);
        return this;
    }

    /**
     * Sets the snapshot of the blob this client is connecting to.
     * @param snapshot the snapshot identifier for the blob
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }
}
