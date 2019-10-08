// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.StorageException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.blob.implementation.PostProcessor.postProcessResponse;

public final class BlobBatchAsyncClient {
    private final AzureBlobStorageImpl client;

    BlobBatchAsyncClient(String accountUrl, HttpPipeline pipeline) {
        this.client = new AzureBlobStorageBuilder()
            .url(accountUrl)
            .pipeline(pipeline)
            .build();
    }

    /**
     * Submits a batch operation.
     *
     * <p>If any request in a batch fails this will throw a {@link StorageException}.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.submitBatch#BlobBatch}
     *
     * @param batch Batch to submit.
     * @return An empty response indicating that the batch operation has completed.
     * @throws StorageException If any request in the {@link BlobBatch} failed or the batch request is malformed.
     */
    public Mono<Void> submitBatch(BlobBatch batch) {
        return withContext(context -> submitBatchWithResponse(batch, true, context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Submits a batch operation.
     *
     * <p>If {@code throwOnAnyFailure} is {@code true} a {@link StorageException} will be thrown if any request
     * fails.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.submitBatchWithResponse#BlobBatch-boolean}
     *
     * @param batch Batch to submit.
     * @param throwOnAnyFailure Flag to indicate if an exception should be thrown if any request in the batch fails.
     * @return A response only containing header and status code information, used to indicate that the batch operation
     * has completed.
     * @throws StorageException If {@code throwOnAnyFailure} is {@code true} and any request in the {@link BlobBatch}
     * failed or the batch request is malformed.
     */
    public Mono<Response<Void>> submitBatchWithResponse(BlobBatch batch, boolean throwOnAnyFailure) {
        return withContext(context -> submitBatchWithResponse(batch, throwOnAnyFailure, context));
    }

    Mono<Response<Void>> submitBatchWithResponse(BlobBatch batch, boolean throwOnAnyFailure, Context context) {
        return postProcessResponse(this.client.services().submitBatchWithRestResponseAsync(
            batch.getBody(), batch.getContentLength(), batch.getContentType(), context))
            .flatMap(response -> BlobBatchHelper.mapBatchResponse(batch, response, throwOnAnyFailure));
    }

    /**
     * Deletes the passed blobs.
     *
     * @param blobUrls Blobs to delete.
     * @return
     * @throws StorageException If the batch is malformed or any of the delete operations fail.
     */
    public Flux<Response<Void>> deleteBlobs(String... blobUrls) {
        return batchingHelper(BlobBatch::delete, blobUrls);
    }

    /**
     * Sets the {@link AccessTier} on the passed blobs.
     *
     * @param accessTier {@link AccessTier} to set on the blobs.
     * @param blobUrls Blobs to set the access tier.
     * @return
     * @throws StorageException If the batch is malformed or any of the set access tier operations fail.
     */
    public Flux<Response<Void>> setBlobsAccessTier(AccessTier accessTier, String... blobUrls) {
        return batchingHelper((batch, blobUrl) -> batch.setTier(blobUrl, accessTier), blobUrls);
    }

    private <T> Flux<Response<T>> batchingHelper(BiFunction<BlobBatch, String, Response<T>> generator,
        String... blobUrls) {
        BlobBatch batch = new BlobBatch(client.getUrl(), client.getHttpPipeline());

        List<Response<T>> responses = new ArrayList<>();
        for (String blobUrl : blobUrls) {
            responses.add(generator.apply(batch, blobUrl));
        }

        return withContext(context -> submitBatchWithResponse(batch, true, context))
            .thenMany(Flux.fromIterable(responses));
    }
}
