// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.batch.options.BlobBatchSetBlobAccessTierOptions;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class allows for batching of multiple Azure Storage operations in a single request via {@link
 * BlobBatchClient#submitBatch(BlobBatch)} or {@link BlobBatchAsyncClient#submitBatch(BlobBatch)}.
 *
 * <p>Azure Storage Blob batches are homogeneous which means a {@link #deleteBlob(String) delete} and {@link
 * #setBlobAccessTier(String, AccessTier) set tier} are not allowed to be in the same batch.</p>
 *
 * {@codesnippet com.azure.storage.blob.batch.BlobBatch.illegalBatchOperation}
 *
 * <p>Please refer to the <a href="https://docs.microsoft.com/rest/api/storageservices/blob-batch">Azure Docs</a>
 * for more information.</p>
 */
public final class BlobBatch {
    private static final String X_MS_VERSION = "x-ms-version";
    private static final String BATCH_REQUEST_URL_PATH = "Batch-Request-Url-Path";
    private static final String BATCH_OPERATION_RESPONSE = "Batch-Operation-Response";
    private static final String BATCH_OPERATION_INFO = "Batch-Operation-Info";
    private static final String PATH_TEMPLATE = "%s/%s";

    /*
     * Track the status codes expected for the batching operations here as the batch body does not get parsed in
     * Azure Core where this information is maintained.
     */
    private static final int[] EXPECTED_DELETE_STATUS_CODES = {202};
    private static final int[] EXPECTED_SET_TIER_STATUS_CODES = {200, 202};

    private final ClientLogger logger = new ClientLogger(BlobBatch.class);

    private final BlobAsyncClient blobAsyncClient;

    private Deque<BlobBatchOperation<?>> batchOperationQueue;
    private BlobBatchType batchType;

    BlobBatch(String accountUrl, HttpPipeline pipeline, BlobServiceVersion serviceVersion) {
        boolean batchHeadersPolicySet = false;
        HttpPipelineBuilder batchPipelineBuilder = new HttpPipelineBuilder();
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy policy = pipeline.getPolicy(i);

            if (policy instanceof StorageSharedKeyCredentialPolicy) {
                batchHeadersPolicySet = true;
                // The batch policy needs to be added before the SharedKey policy to run preparation cleanup.
                batchPipelineBuilder.policies(this::cleanseHeaders, this::setRequestUrl);
            }

            batchPipelineBuilder.policies(policy);
        }

        if (!batchHeadersPolicySet) {
            batchPipelineBuilder.policies(this::cleanseHeaders, this::setRequestUrl);
        }

        batchPipelineBuilder.policies(this::buildBatchOperation);

        batchPipelineBuilder.httpClient(pipeline.getHttpClient());

        this.blobAsyncClient = new BlobClientBuilder()
            .endpoint(accountUrl)
            .blobName("")
            .serviceVersion(serviceVersion)
            .pipeline(batchPipelineBuilder.build())
            .buildAsyncClient();

        this.batchOperationQueue = new ConcurrentLinkedDeque<>();
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String}
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> deleteBlob(String containerName, String blobName) {
        return deleteBlobHelper(String.format(PATH_TEMPLATE, containerName,
            Utility.urlEncode(Utility.urlDecode(blobName))), null, null);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String-DeleteSnapshotsOptionType-BlobRequestConditions}
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param deleteOptions Delete options for the blob and its snapshots.
     * @param blobRequestConditions Additional access conditions that must be met to allow this operation.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> deleteBlob(String containerName, String blobName,
        DeleteSnapshotsOptionType deleteOptions, BlobRequestConditions blobRequestConditions) {
        return deleteBlobHelper(String.format(PATH_TEMPLATE, containerName,
            Utility.urlEncode(Utility.urlDecode(blobName))), deleteOptions, blobRequestConditions);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.deleteBlob#String}
     *
     * @param blobUrl URL of the blob. Blob name must be encoded to UTF-8.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> deleteBlob(String blobUrl) {
        return deleteBlobHelper(getUrlPath(blobUrl), null, null);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-DeleteSnapshotsOptionType-BlobRequestConditions}
     *
     * @param blobUrl URL of the blob. Blob name must be encoded to UTF-8.
     * @param deleteOptions Delete options for the blob and its snapshots.
     * @param blobRequestConditions Additional access conditions that must be met to allow this operation.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> deleteBlob(String blobUrl, DeleteSnapshotsOptionType deleteOptions,
        BlobRequestConditions blobRequestConditions) {
        return deleteBlobHelper(getUrlPath(blobUrl), deleteOptions, blobRequestConditions);
    }

    private Response<Void> deleteBlobHelper(String urlPath, DeleteSnapshotsOptionType deleteOptions,
        BlobRequestConditions blobRequestConditions) {
        setBatchType(BlobBatchType.DELETE);
        return createBatchOperation(blobAsyncClient.deleteWithResponse(deleteOptions, blobRequestConditions),
            urlPath, EXPECTED_DELETE_STATUS_CODES);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier}
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param accessTier The tier to set on the blob.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setBlobAccessTier(String containerName, String blobName, AccessTier accessTier) {
        return setBlobAccessTierHelper(String.format(PATH_TEMPLATE, containerName,
            Utility.urlEncode(Utility.urlDecode(blobName))), accessTier, null, null, null);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier-String}
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param accessTier The tier to set on the blob.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setBlobAccessTier(String containerName, String blobName, AccessTier accessTier,
        String leaseId) {
        return setBlobAccessTierHelper(String.format(PATH_TEMPLATE, containerName,
            Utility.urlEncode(Utility.urlDecode(blobName))), accessTier, null, leaseId, null);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier}
     *
     * @param blobUrl URL of the blob. Blob name must be encoded to UTF-8.
     * @param accessTier The tier to set on the blob.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setBlobAccessTier(String blobUrl, AccessTier accessTier) {
        return setBlobAccessTierHelper(getUrlPath(blobUrl), accessTier, null, null, null);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier-String}
     *
     * @param blobUrl URL of the blob. Blob name must be encoded to UTF-8.
     * @param accessTier The tier to set on the blob.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setBlobAccessTier(String blobUrl, AccessTier accessTier, String leaseId) {
        return setBlobAccessTierHelper(getUrlPath(blobUrl), accessTier, null, leaseId, null);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#BlobBatchSetBlobAccessTierOptions}
     *
     * @param options {@link BlobBatchSetBlobAccessTierOptions}
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setBlobAccessTier(BlobBatchSetBlobAccessTierOptions options) {
        StorageImplUtils.assertNotNull("options", options);
        return setBlobAccessTierHelper(options.getBlobIdentifier(), options.getTier(), options.getPriority(),
            options.getLeaseId(), options.getTagsConditions());
    }

    private Response<Void> setBlobAccessTierHelper(String blobPath, AccessTier tier, RehydratePriority priority,
        String leaseId, String tagsConditions) {
        setBatchType(BlobBatchType.SET_TIER);
        return createBatchOperation(blobAsyncClient.setAccessTierWithResponse(
            new BlobSetAccessTierOptions(tier)
                .setLeaseId(leaseId)
                .setPriority(priority)
                .setTagsConditions(tagsConditions)),
            blobPath, EXPECTED_SET_TIER_STATUS_CODES);
    }

    private <T> Response<T> createBatchOperation(Mono<Response<T>> response, String urlPath,
        int... expectedStatusCodes) {
        BlobBatchOperationResponse<T> batchOperationResponse = new BlobBatchOperationResponse<>(expectedStatusCodes);
        batchOperationQueue.add(new BlobBatchOperation<>(batchOperationResponse, response, urlPath));

        return batchOperationResponse;
    }

    private String getUrlPath(String url) {
        return UrlBuilder.parse(url).getPath();
    }

    private void setBatchType(BlobBatchType batchType) {
        if (this.batchType == null) {
            this.batchType = batchType;
        } else if (this.batchType != batchType) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(String.format(Locale.ROOT,
                "'BlobBatch' only supports homogeneous operations and is a %s batch.", this.batchType)));
        }
    }

    Mono<BlobBatchOperationInfo> prepareBlobBatchSubmission() {
        if (batchOperationQueue.isEmpty()) {
            return monoError(logger, new UnsupportedOperationException("Empty batch requests aren't allowed."));
        }

        BlobBatchOperationInfo operationInfo = new BlobBatchOperationInfo();
        Deque<BlobBatchOperation<?>> operations = batchOperationQueue;

        // Begin a new batch.
        batchOperationQueue = new ConcurrentLinkedDeque<>();

        List<Mono<? extends Response<?>>> batchOperationResponses = new ArrayList<>();
        while (!operations.isEmpty()) {
            BlobBatchOperation<?> batchOperation = operations.pop();

            batchOperationResponses.add(batchOperation.getResponse()
                .subscriberContext(Context.of(BATCH_REQUEST_URL_PATH, batchOperation.getRequestUrlPath(),
                    BATCH_OPERATION_RESPONSE, batchOperation.getBatchOperationResponse(),
                    BATCH_OPERATION_INFO, operationInfo)));
        }

        /*
         * Mono.when is more robust and safer to use than the previous implementation, using Flux.generate, as it is
         * fulfilled/complete once all publishers comprising it are completed whereas Flux.generate will complete once
         * the sink completes. Certain authorization methods, such as AAD, may have deferred processing where the sink
         * would trigger completion before the request bodies are added into the batch, leading to a state where the
         * request would believe it had a different size than it actually had, Mono.when bypasses this issue as it must
         * wait until the deferred processing has completed to trigger the `thenReturn` operator.
         */
        return Mono.when(batchOperationResponses)
            .doOnSuccess(ignored -> operationInfo.finalizeBatchOperations())
            .thenReturn(operationInfo);
    }

    /*
     * This performs a cleanup operation that would be handled when the request is sent through Netty or OkHttp.
     * Additionally, it removes the "x-ms-version" header from the request as batch operation requests cannot have this
     * and it adds the header "Content-Id" that allows the request to be mapped to the response.
     */
    private Mono<HttpResponse> cleanseHeaders(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Remove the "x-ms-version" as it shouldn't be included in the batch operation request.
        context.getHttpRequest().getHeaders().remove(X_MS_VERSION);

        // Remove any null headers (this is done in Netty and OkHttp normally).
        for (HttpHeader hdr : context.getHttpRequest().getHeaders()) {
            if (hdr.getValue() == null) {
                context.getHttpRequest().getHeaders().remove(hdr.getName());
            }
        }

        return next.process();
    }

    /*
     * This performs changing the request URL to the value passed through the pipeline context. This policy is used in
     * place of constructing a new client for each batch request that is being sent.
     */
    private Mono<HttpResponse> setRequestUrl(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Set the request URL to the correct endpoint.
        try {
            UrlBuilder requestUrl = UrlBuilder.parse(context.getHttpRequest().getUrl());
            requestUrl.setPath(context.getData(BATCH_REQUEST_URL_PATH).get().toString());
            context.getHttpRequest().setUrl(requestUrl.toUrl());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(new IllegalStateException(ex)));
        }

        return next.process();
    }

    /*
     * This will "send" the batch operation request when triggered, it simply acts as a way to build and write the
     * batch operation into the overall request and then returns nothing as the response.
     */
    private Mono<HttpResponse> buildBatchOperation(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        BlobBatchOperationInfo operationInfo = (BlobBatchOperationInfo) context.getData(BATCH_OPERATION_INFO).get();
        BlobBatchOperationResponse<?> batchOperationResponse =
            (BlobBatchOperationResponse<?>) context.getData(BATCH_OPERATION_RESPONSE).get();
        operationInfo.addBatchOperation(batchOperationResponse, context.getHttpRequest());

        return Mono.empty();
    }
}
