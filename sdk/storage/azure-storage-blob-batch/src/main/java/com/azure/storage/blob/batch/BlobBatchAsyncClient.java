// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob batching.
 *
 * <p>This client offers the ability to delete and set access tier on multiple blobs at once and to submit a {@link
 * BlobBatch}.</p>
 *
 * @see BlobBatch
 * @see BlobBatchClientBuilder
 */
@ServiceClient(builder = BlobBatchClientBuilder.class, isAsync = true)
public final class BlobBatchAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobBatchAsyncClient.class);

    private final AzureBlobStorageImpl client;
    private final boolean containerScoped;
    private final BlobServiceVersion serviceVersion;

    BlobBatchAsyncClient(String clientUrl, HttpPipeline pipeline, BlobServiceVersion version, boolean containerScoped) {
        this.serviceVersion = version;
        this.client = new AzureBlobStorageImplBuilder()
            .url(clientUrl)
            .pipeline(pipeline)
            .version(version.getVersion())
            .buildClient();
        this.containerScoped = containerScoped;
    }

    /**
     * Gets a {@link BlobBatch} used to configure a batching operation to send to Azure Storage blobs.
     *
     * @return a new {@link BlobBatch} instance.
     */
    public BlobBatch getBlobBatch() {
        return new BlobBatch(client.getUrl(), client.getHttpPipeline(), serviceVersion);
    }

    /**
     * Submits a batch operation.
     *
     * <p>If any request in a batch fails this will throw a {@link BlobStorageException}.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchAsyncClient.submitBatch#BlobBatch}
     *
     * @param batch Batch to submit.
     * @return An empty response indicating that the batch operation has completed.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any request in the {@link BlobBatch} failed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> submitBatch(BlobBatch batch) {
        try {
            return withContext(context -> submitBatchWithResponse(batch, true, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Submits a batch operation.
     *
     * <p>If {@code throwOnAnyFailure} is {@code true} a {@link BlobStorageException} will be thrown if any request
     * fails.</p>
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchAsyncClient.submitBatch#BlobBatch-boolean}
     *
     * @param batch Batch to submit.
     * @param throwOnAnyFailure Flag to indicate if an exception should be thrown if any request in the batch fails.
     * @return A response only containing header and status code information, used to indicate that the batch operation
     * has completed.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If {@code throwOnAnyFailure} is {@code true} and any request in the {@link
     * BlobBatch} failed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> submitBatchWithResponse(BlobBatch batch, boolean throwOnAnyFailure) {
        try {
            return withContext(context -> submitBatchWithResponse(batch, throwOnAnyFailure,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> submitBatchWithResponse(BlobBatch batch, boolean throwOnAnyFailure, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        return batch.prepareBlobBatchSubmission()
            .flatMap(batchOperationInfo -> containerScoped
                ? client.getContainers().submitBatchWithResponseAsync(null,
                batchOperationInfo.getContentLength(), batchOperationInfo.getContentType(),
                Flux.fromIterable(batchOperationInfo.getBody()), null, null,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .flatMap(response ->
                    BlobBatchHelper.mapBatchResponse(batchOperationInfo, response, throwOnAnyFailure, logger))
                : client.getServices().submitBatchWithResponseAsync(batchOperationInfo.getContentLength(),
                batchOperationInfo.getContentType(), Flux.fromIterable(batchOperationInfo.getBody()), null, null,
                finalContext.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .flatMap(response ->
                    BlobBatchHelper.mapBatchResponse(batchOperationInfo, response, throwOnAnyFailure, logger)));
    }

    /**
     * Delete multiple blobs in a single request to the service.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchAsyncClient.deleteBlobs#List-DeleteSnapshotsOptionType}
     *
     * @param blobUrls Urls of the blobs to delete. Blob names must be encoded to UTF-8.
     * @param deleteOptions The deletion option for all blobs.
     * @return The status of each delete operation.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any of the delete operations fail.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Response<Void>> deleteBlobs(List<String> blobUrls, DeleteSnapshotsOptionType deleteOptions) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> submitDeleteBlobsBatch(blobUrls, deleteOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<Response<Void>> deleteBlobsWithTimeout(List<String> blobUrls, DeleteSnapshotsOptionType deleteOptions,
        Duration timeout, Context context) {
        return new PagedFlux<>(() ->
            StorageImplUtils.applyOptionalTimeout(submitDeleteBlobsBatch(blobUrls, deleteOptions, context), timeout));
    }

    private Mono<PagedResponse<Response<Void>>> submitDeleteBlobsBatch(List<String> blobUrls,
        DeleteSnapshotsOptionType deleteOptions, Context context) {
        return submitBatchHelper(blobUrls, (batch, blobUrl) -> batch.deleteBlob(blobUrl, deleteOptions, null), context);
    }

    /**
     * Set access tier on multiple blobs in a single request to the service.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchAsyncClient.setBlobsAccessTier#List-AccessTier}
     *
     * @param blobUrls Urls of the blobs to set their access tier. Blob names must be encoded to UTF-8.
     * @param accessTier {@link AccessTier} to set on each blob.
     * @return The status of each set tier operation.
     * @throws BlobStorageException If the batch request is malformed.
     * @throws BlobBatchStorageException If any of the set tier operations fail.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Response<Void>> setBlobsAccessTier(List<String> blobUrls, AccessTier accessTier) {
        try {
            return new PagedFlux<>(() -> withContext(context -> submitSetTierBatch(blobUrls, accessTier, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<Response<Void>> setBlobsAccessTierWithTimeout(List<String> blobUrls, AccessTier accessTier,
        Duration timeout, Context context) {
        return new PagedFlux<>(() ->
            StorageImplUtils.applyOptionalTimeout(submitSetTierBatch(blobUrls, accessTier, context), timeout));
    }

    private Mono<PagedResponse<Response<Void>>> submitSetTierBatch(List<String> blobUrls, AccessTier accessTier,
        Context context) {
        return submitBatchHelper(blobUrls, (batch, blobUrl) -> batch.setBlobAccessTier(blobUrl, accessTier), context);
    }

    /*
     * This helper method creates the batch request, applies the requested batching operation to each blob, sends the
     * request to the service, and returns the responses.
     */
    private <T> Mono<PagedResponse<Response<T>>> submitBatchHelper(List<String> blobUrls,
        BiFunction<BlobBatch, String, Response<T>> generator, Context context) {
        BlobBatch batch = getBlobBatch();

        List<Response<T>> responses = new ArrayList<>();
        for (String blobUrl : blobUrls) {
            responses.add(generator.apply(batch, blobUrl));
        }

        return submitBatchWithResponse(batch, true, context)
            .map(response -> initPagedResponse(responses, response));
    }

    private <T> PagedResponse<Response<T>> initPagedResponse(List<Response<T>> values, Response<?> response) {
        return new PagedResponse<Response<T>>() {
            @Override
            public IterableStream<Response<T>> getElements() {
                return new IterableStream<>(values);
            }

            @Override
            public String getContinuationToken() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return response.getStatusCode();
            }

            @Override
            public HttpHeaders getHeaders() {
                return response.getHeaders();
            }

            @Override
            public HttpRequest getRequest() {
                return response.getRequest();
            }

            @Override
            public void close() {
            }
        };
    }
}
