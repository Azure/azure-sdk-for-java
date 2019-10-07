// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

public class EncryptedBlockBlobClient extends BlobClient {
    private final ClientLogger logger = new ClientLogger(EncryptedBlockBlobClient.class);
    private final EncryptedBlockBlobAsyncClient encryptedBlockBlobAsyncClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     */
    EncryptedBlockBlobClient(EncryptedBlockBlobAsyncClient encryptedBlockBlobAsyncClient) {
        super(encryptedBlockBlobAsyncClient);
        this.encryptedBlockBlobAsyncClient = encryptedBlockBlobAsyncClient;
    }

    public BlockBlobClient getBlockBlobClient() {
        return new BlobClientBuilder()
            .pipeline(EncryptedBlockBlobAsyncClient.removeDecryptionPolicy(getHttpPipeline(),
                getHttpPipeline().getHttpClient()))
            .endpoint(getBlobUrl())
            .buildClient()
            .getBlockBlobClient();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobClient.uploadFromFile#String}
     *
     * @param filePath Path of the file to upload
     */
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, null, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration}
     *
     * @param filePath Path of the file to upload
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the uploaded blob
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHTTPHeaders headers, Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions,
        Duration timeout) throws UncheckedIOException {
        Mono<Void> upload = this.encryptedBlockBlobAsyncClient.uploadFromFile(filePath, parallelTransferOptions,
            headers, metadata, tier, accessConditions);

        try {
            Utility.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }

}
