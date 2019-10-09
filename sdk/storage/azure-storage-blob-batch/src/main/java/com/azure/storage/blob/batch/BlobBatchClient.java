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
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.Utility;

import java.time.Duration;

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
     * <p>If any request in a batch fails this will throw a {@link StorageException}.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.submitBatch#BlobBatch}
     *
     * @param batch Batch to submit.
     * @throws StorageException If any request in the {@link BlobBatch} failed or the batch request is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void submitBatch(BlobBatch batch) {
        submitBatchWithResponse(batch, true, null, Context.NONE);
    }

    /**
     * Submits a batch operation.
     *
     * <p>If {@code throwOnAnyFailure} is {@code true} a {@link StorageException} will be thrown if any request
     * fails.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet {@codesnippet com.azure.storage.blob.BlobServiceClient.submitBatchWithResponse#BlobBatch-boolean-Duration-Context}}
     *
     * @param batch Batch to submit.
     * @param throwOnAnyFailure Flag to indicate if an exception should be thrown if any request in the batch fails.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response only containing header and status code information, used to indicate that the batch operation
     * has completed.
     * @throws RuntimeException If the {@code timeout} duration completes before a response is returned.
     * @throws StorageException If {@code throwOnAnyFailure} is {@code true} and any request in the {@link BlobBatch}
     * failed or the batch request is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> submitBatchWithResponse(BlobBatch batch, boolean throwOnAnyFailure, Duration timeout,
        Context context) {
        return Utility.blockWithOptionalTimeout(client.submitBatchWithResponse(batch, throwOnAnyFailure, context),
            timeout);
    }

    /**
     * Delete multiple blobs in a single request to the service.
     *
     * <p>This will delete the blob and all of its snapshots.</p>
     *
     * @param blobUrls Urls of the blobs to delete.
     * @return The status of each delete operation.
     * @throws StorageException If any of the delete operations fail or the request is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Response<Void>> deleteBlobs(String... blobUrls) {
        return new PagedIterable<>(client.deleteBlobs(blobUrls));
    }

    /**
     * Set access tier on multiple blobs in a single request to the service.
     *
     * @param accessTier {@link AccessTier} to set on each blob.
     * @param blobUrls Urls of the blobs to set their access tier.
     * @return The status of each set tier operation.
     * @throws StorageException If any of the set tier operations fail or the request is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Response<Void>> setBlobsAccessTier(AccessTier accessTier, String... blobUrls) {
        return new PagedIterable<>(client.setBlobsAccessTier(accessTier, blobUrls));
    }
}
