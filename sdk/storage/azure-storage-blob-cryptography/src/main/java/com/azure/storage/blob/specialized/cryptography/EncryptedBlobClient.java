// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.models.BlobQueryResponse;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
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
 * <a href=https://docs.microsoft.com/azure/storage/common/storage-client-side-encryption-java>Azure
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
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
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
     * Creates a new {@link EncryptedBlobClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link EncryptedBlobClient} with the specified {@code encryptionScope}.
     */
    @Override
    public EncryptedBlobClient getEncryptionScopeClient(String encryptionScope) {
        return new EncryptedBlobClient(encryptedBlobAsyncClient.getEncryptionScopeAsyncClient(encryptionScope));
    }

    /**
     * Creates a new {@link EncryptedBlobClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link EncryptedBlobClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public EncryptedBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        return new EncryptedBlobClient(encryptedBlobAsyncClient.getCustomerProvidedKeyAsyncClient(customerProvidedKey));
    }

    /**
     * Creates and opens an output stream to write data to the block blob.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream() {
        return getBlobOutputStream(false);
    }

    /**
     * Creates and opens an output stream to write data to the block blob.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(boolean overwrite) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            if (exists()) {
                throw logger.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
            }
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return getBlobOutputStream(null, null, null, null, requestConditions);
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service, it
     * will be overwritten.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobRequestConditions requestConditions) {
        return this.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
            .setTier(tier).setRequestConditions(requestConditions));
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service, it
     * will be overwritten.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @param options {@link BlockBlobOutputStreamOptions}
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(BlockBlobOutputStreamOptions options) {

        return BlobOutputStream.blockBlobOutputStream(encryptedBlobAsyncClient, options, null);
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
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the uploaded blob
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions,
        Duration timeout) throws UncheckedIOException {
        this.uploadFromFileWithResponse(new BlobUploadFromFileOptions(filePath)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setTier(tier).setRequestConditions(requestConditions), timeout,
            null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFileWithResponse#BlobUploadFromFileOptions-Duration-Context}
     *
     * @param options {@link BlobUploadFromFileOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws UncheckedIOException If an I/O error occurs
     * @return Information about the uploaded block blob.
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadFromFileWithResponse(BlobUploadFromFileOptions options,
        Duration timeout, Context context)
        throws UncheckedIOException {
        Mono<Response<BlockBlobItem>> upload =
            this.encryptedBlobAsyncClient.uploadFromFileWithResponse(options)
                .subscriberContext(FluxUtil.toReactorContext(context));

        try {
            return StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    /**
     * Unsupported.
     */
    @Override
    public AppendBlobClient getAppendBlobClient() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as an append"
            + " blob client"));
    }

    /**
     * Unsupported.
     */
    @Override
    public BlockBlobClient getBlockBlobClient() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as a block"
            + " blob client"));
    }

    /**
     * Unsupported.
     */
    @Override
    public PageBlobClient getPageBlobClient() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as an page"
            + " blob client"));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public InputStream openQueryInputStream(String expression) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public Response<InputStream> openQueryInputStreamWithResponse(BlobQueryOptions queryOptions) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public void query(OutputStream stream, String expression) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public BlobQueryResponse queryWithResponse(BlobQueryOptions queryOptions,
        Duration timeout, Context context) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

}
