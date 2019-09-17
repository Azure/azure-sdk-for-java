package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageRange;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class SpecializedBlobClientBuilder {
    private final ClientLogger logger = new ClientLogger(SpecializedBlobClientBuilder.class);

    private HttpPipeline pipeline;
    private URL url;
    private String snapshot;
    private CpkInfo cpk;

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
        return new AppendBlobAsyncClient(constructImpl(), snapshot, cpk);
    }

    /**
     * Creates a {@link BlockBlobClient} based on options set in the Builder. BlockBlobClients are used to perform
     * generic upload operations such as {@link BlockBlobClient#uploadFromFile(String) upload from file} and block blob
     * specific operations such as {@link BlockBlobClient#stageBlock(String, InputStream, long) stage block} and {@link
     * BlockBlobClient#commitBlockList(List)}, only use this when the blob is known to be a block blob.
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
     * and block blob specific operations such as {@link BlockBlobAsyncClient#stageBlockWithResponse(String, Flux, long,
     * LeaseAccessConditions) stage block} and {@link BlockBlobAsyncClient#commitBlockList(List) commit block list},
     * only use this when the blob is known to be a block blob.
     *
     * @return a {@link BlockBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobAsyncClient buildBlockBlobAsyncClient() {
        return new BlockBlobAsyncClient(constructImpl(), snapshot, cpk);
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
        return new PageBlobAsyncClient(constructImpl(), snapshot, cpk);
    }

    private AzureBlobStorageImpl constructImpl() {
        return new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url.toString())
            .build();
    }

    public SpecializedBlobClientBuilder blobClient(BlobClientBase blobClient) {
        this.pipeline = blobClient.getHttpPipeline();
        this.url = blobClient.getBlobUrl();
        this.snapshot = blobClient.getSnapshotId();
        this.cpk = blobClient.getCpk();
        return this;
    }

    public SpecializedBlobClientBuilder blobAsyncClient(BlobAsyncClientBase blobAsyncClient) {
        this.pipeline = blobAsyncClient.getHttpPipeline();
        this.url = blobAsyncClient.getBlobUrl();
        this.snapshot = blobAsyncClient.getSnapshotId();
        this.cpk = blobAsyncClient.getCpk();
        return this;
    }

    public SpecializedBlobClientBuilder containerClient(ContainerClient containerClient, String blobName) {
        this.pipeline = containerClient.getHttpPipeline();
        this.url = addBlobToUrl(containerClient.getContainerUrl().toString(), blobName);
        this.cpk = containerClient.getCpk();
        return this;
    }

    public SpecializedBlobClientBuilder containerAsyncClient(ContainerAsyncClient containerAsyncClient,
        String blobName) {
        this.pipeline = containerAsyncClient.getHttpPipeline();
        this.url = addBlobToUrl(containerAsyncClient.getContainerUrl().toString(), blobName);
        this.cpk = containerAsyncClient.getCpk();
        return this;
    }

    public SpecializedBlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public SpecializedBlobClientBuilder cpk(CpkInfo cpk) {
        this.cpk = cpk;
        return this;
    }

    private URL addBlobToUrl(String url, String blobName) {
        try {
            return new URL(String.format("%s/%s", url, blobName));
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalStateException(ex));
        }
    }
}
