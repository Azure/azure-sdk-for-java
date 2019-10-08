// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.Utility;

import java.time.Duration;
import java.util.stream.Stream;

public final class BlobBatchClient {
    private final BlobBatchAsyncClient client;

    BlobBatchClient(BlobBatchAsyncClient client) {
        this.client = client;
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
    public Response<Void> submitBatchWithResponse(BlobBatch batch, boolean throwOnAnyFailure, Duration timeout,
        Context context) {
        return Utility.blockWithOptionalTimeout(client.submitBatchWithResponse(batch, throwOnAnyFailure, context),
            timeout);
    }

    public Stream<Response<Void>> deleteBlobs(String... blobUrls) {
        return client.deleteBlobs(blobUrls).toStream();
    }

    public Stream<Response<Void>> setBlobsAccessTier(AccessTier accessTier, String... blobUrls) {
        return client.setBlobsAccessTier(accessTier, blobUrls).toStream();
    }
}
