// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by
 * the client are downloading and copying a blob, retrieving and setting metadata, retrieving and setting HTTP headers,
 * and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via
 * {@link BlobContainerClient#getBlobClient(String) getBlobClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobClient() getAppendBlobClient}, {@link #getBlockBlobClient() getBlockBlobClient}, or
 * {@link #getPageBlobClient() getPageBlobClient} to construct a client that allows blob specific operations.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = BlobClientBuilder.class)
public class BlobClient extends BlobClientBase {
    private final ClientLogger logger = new ClientLogger(BlobClient.class);
    private final BlobAsyncClient client;

    /**
     * Protected constructor for use by {@link BlobClientBuilder}.
     * @param client the async blob client
     */
    protected BlobClient(BlobAsyncClient client) {
        super(client);
        this.client = client;
    }

    /**
     * Creates a new {@link BlobClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobClient getSnapshotClient(String snapshot) {
        return new BlobClient(client.getSnapshotClient(snapshot));
    }

    /**
     * Creates a new {@link AppendBlobClient} associated to this blob.
     *
     * @return a {@link AppendBlobClient} associated to this blob.
     */
    public AppendBlobClient getAppendBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildAppendBlobClient();
    }

    /**
     * Creates a new {@link BlockBlobClient} associated to this blob.
     *
     * @return a {@link BlockBlobClient} associated to this blob.
     */
    public BlockBlobClient getBlockBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildBlockBlobClient();
    }

    /**
     * Creates a new {@link PageBlobClient} associated to this blob.
     *
     * @return a {@link PageBlobClient} associated to this blob.
     */
    public PageBlobClient getPageBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildPageBlobClient();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadFromFile#String}
     *
     * @param filePath Path of the file to upload
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, null, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions-Duration}
     *
     * @param filePath Path of the file to upload
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the uploaded blob
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions,
        Duration timeout) {
        Mono<Void> upload = this.client.uploadFromFile(
            filePath, parallelTransferOptions, headers, metadata, tier, accessConditions);

        try {
            StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }
}
