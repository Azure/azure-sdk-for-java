// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.common.implementation.StorageImplUtils;
import java.time.Duration;
import java.util.List;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob batching.
 *
 * <p>This client offers the ability to delete and set access tier on multiple blobs at once and to submit a {@link
 * BlobBatch}.</p>
 *
 * @see BlobBatch
 * @see BlobBatchClientBuilder
 */
@ServiceClient(builder = BlobBatchClientBuilder.class)
public final class BlobBatchClient {
    private final BlobBatchAsyncClient client;

    BlobBatchClient(BlobBatchAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets a {@link BlobBatch} used to configure a batching operation to send to Azure Storage blobs.
     *
     * @return a new {@link BlobBatch} instance.
     */
    public BlobBatch getBlobBatch() {
        return client.getBlobBatch();
    }

    /**
     * Submits a batch operation.
     *
     * <p>If any request in a batch fails this will throw a {@link BlobStorageException}.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch}
     *
     * @param batch Batch to submit.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any request in the {@link BlobBatch} failed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void submitBatch(BlobBatch batch) {
        submitBatchWithResponse(batch, true, null, Context.NONE);
    }

    /**
     * Submits a batch operation.
     *
     * <p>If {@code throwOnAnyFailure} is {@code true} a {@link BlobStorageException} will be thrown if any request
     * fails.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch-boolean-Duration-Context}
     *
     * @param batch Batch to submit.
     * @param throwOnAnyFailure Flag to indicate if an exception should be thrown if any request in the batch fails.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response only containing header and status code information, used to indicate that the batch operation
     * has completed.
     * @throws RuntimeException If the {@code timeout} duration completes before a response is returned.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If {@code throwOnAnyFailure} is {@code true} and any request in the
     * {@link BlobBatch} failed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> submitBatchWithResponse(BlobBatch batch, boolean throwOnAnyFailure, Duration timeout,
        Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(client.submitBatchWithResponse(batch,
            throwOnAnyFailure, context), timeout);
    }

    /**
     * Delete multiple blobs in a single request to the service.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClient.deleteBlobs#List-DeleteSnapshotsOptionType}
     *
     * @param blobUrls Urls of the blobs to delete. Blob names must be encoded to UTF-8.
     * @param deleteOptions The deletion option for all blobs.
     * @return The status of each delete operation.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any of the delete operations fail.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Response<Void>> deleteBlobs(List<String> blobUrls, DeleteSnapshotsOptionType deleteOptions) {
        return new PagedIterable<>(client.deleteBlobs(blobUrls, deleteOptions));
    }

    /**
     * Delete multiple blobs in a single request to the service.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClient.deleteBlobs#List-DeleteSnapshotsOptionType-Duration-Context}
     *
     * @param blobUrls Urls of the blobs to delete. Blob names must be encoded to UTF-8.
     * @param deleteOptions The deletion option for all blobs.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The status of each delete operation.
     * @throws RuntimeException If the {@code timeout} duration completes before a response is returned.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any of the delete operations fail.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Response<Void>> deleteBlobs(List<String> blobUrls, DeleteSnapshotsOptionType deleteOptions,
        Duration timeout, Context context) {
        return new PagedIterable<>(client.deleteBlobsWithTimeout(blobUrls, deleteOptions, timeout, context));
    }

    /**
     * Set access tier on multiple blobs in a single request to the service.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClient.setBlobsAccessTier#List-AccessTier}
     *
     * @param blobUrls Urls of the blobs to set their access tier. Blob names must be encoded to UTF-8.
     * @param accessTier {@link AccessTier} to set on each blob.
     * @return The status of each set tier operation.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any of the set tier operations fail.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Response<Void>> setBlobsAccessTier(List<String> blobUrls, AccessTier accessTier) {
        return new PagedIterable<>(client.setBlobsAccessTier(blobUrls, accessTier));
    }

    /**
     * Set access tier on multiple blobs in a single request to the service.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClient.setBlobsAccessTier#List-AccessTier-Duration-Context}
     *
     * @param blobUrls Urls of the blobs to set their access tier. Blob names must be encoded to UTF-8.
     * @param accessTier {@link AccessTier} to set on each blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The status of each set tier operation.
     * @throws RuntimeException If the {@code timeout} duration completes before a response is returned.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any of the set tier operations fail.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Response<Void>> setBlobsAccessTier(List<String> blobUrls, AccessTier accessTier,
        Duration timeout, Context context) {
        return new PagedIterable<>(client.setBlobsAccessTierWithTimeout(blobUrls, accessTier, timeout, context));
    }
}
