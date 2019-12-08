// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by
 * the client are uploading and downloading, copying a blob, retrieving and setting metadata, retrieving and setting
 * HTTP headers, and deleting and un-deleting a blob.
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

    /**
     * The block size to use if none is specified in parallel operations.
     */
    public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;

    /**
     * The number of buffers to use if none is specied on the buffered upload method.
     */
    public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = BlobAsyncClient.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
    /**
     * If a blob is known to be greater than 100MB, using a larger block size will trigger some server-side
     * optimizations. If the block size is not set and the size of the blob is known to be greater than 100MB, this
     * value will be used.
     */
    public static final int BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE = BlobAsyncClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;

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
     * @return A {@link BlobClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobClient getSnapshotClient(String snapshot) {
        return new BlobClient(client.getSnapshotClient(snapshot));
    }

    /**
     * Creates a new {@link AppendBlobClient} associated with this blob.
     *
     * @return A {@link AppendBlobClient} associated with this blob.
     */
    public AppendBlobClient getAppendBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildAppendBlobClient();
    }

    /**
     * Creates a new {@link BlockBlobClient} associated with this blob.
     *
     * @return A {@link BlockBlobClient} associated with this blob.
     */
    public BlockBlobClient getBlockBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildBlockBlobClient();
    }

    /**
     * Creates a new {@link PageBlobClient} associated with this blob.
     *
     * @return A {@link PageBlobClient} associated with this blob.
     */
    public PageBlobClient getPageBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildPageBlobClient();
    }

    /**
     * Creates a new block blob. By default this method will not overwrite an existing blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadFromFile#String}
     *
     * @param filePath Path of the file to upload
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadFromFile#String-boolean}
     *
     * @param filePath Path of the file to upload
     * @param overwrite Whether or not to overwrite, should the blob already exist
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath, boolean overwrite) {
        BlobRequestConditions requestConditions = null;

        if (!overwrite) {
            // Note we only want to make the exists call if we will be uploading in stages. Otherwise it is superfluous.
            if (client.uploadInBlocks(filePath) && exists()) {
                throw logger.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
            }
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        uploadFromFile(filePath, null, null, null, null, requestConditions, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * 
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration}
     *
     * @param filePath Path of the file to upload
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the uploaded blob
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions,
        Duration timeout) {
        Mono<Void> upload = this.client.uploadFromFile(
            filePath, parallelTransferOptions, headers, metadata, tier, requestConditions);

        try {
            StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }
}
