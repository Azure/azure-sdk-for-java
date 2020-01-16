// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client side encryption client that contains generic blob operations for Azure Storage Blobs.
 * Operations allowed by the client are uploading, downloading and copying a blob, retrieving and setting metadata,
 * retrieving and setting HTTP headers, and deleting and un-deleting a blob. The upload and download operation allow for
 * encryption and decryption of the data client side. Note: setting metadata in particular is unsafe and should only be
 * done so with caution.
 * <p> Please refer to the
 * <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-client-side-encryption-java>Azure
 * Docs For Client-Side Encryption</a> for more information.
 *
 * <p>
 * This client is instantiated through {@link EncryptedBlobClientBuilder}
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobClient() getAppendBlobClient}, {@link #getBlockBlobClient()
 * getBlockBlobClient}, or {@link #getPageBlobClient() getPageBlobAsyncClient} to construct a client that
 * allows blob specific operations. Note, these types do not support client-side encryption, though decryption is
 * possible in case the associated block/page/append blob contains encrypted data.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = EncryptedBlobClientBuilder.class)
public class EncryptedBlobClient extends BlobClient {
    private final ClientLogger logger = new ClientLogger(EncryptedBlobClient.class);
    private final EncryptedBlobAsyncClient encryptedBlobAsyncClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     */
    EncryptedBlobClient(EncryptedBlobAsyncClient encryptedBlobAsyncClient) {
        super(encryptedBlobAsyncClient);
        this.encryptedBlobAsyncClient = encryptedBlobAsyncClient;
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String}
     *
     * @param filePath Path of the file to upload
     */
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-boolean}
     *
     * @param filePath Path of the file to upload
     * @param overwrite Whether or not to overwrite should data already exist on the blob
     */
    public void uploadFromFile(String filePath, boolean overwrite) {
        if (!overwrite && exists()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
        }
        uploadFromFile(filePath, null, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration}
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
        Duration timeout) throws UncheckedIOException {
        Mono<Void> upload = this.encryptedBlobAsyncClient.uploadFromFile(filePath, parallelTransferOptions,
            headers, metadata, tier, requestConditions);

        try {
            StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }

}
