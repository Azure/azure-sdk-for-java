// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.common.Utility;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final String BATCH_REQUEST_CONTENT_ID = "Batch-Request-Content-Id";
    private static final String BATCH_REQUEST_URL_PATH = "Batch-Request-Url-Path";
    private static final String CONTENT_ID = "Content-Id";
    private static final String BATCH_BOUNDARY_TEMPLATE = "batch_%s";
    private static final String REQUEST_CONTENT_TYPE_TEMPLATE = "multipart/mixed; boundary=%s";
    private static final String BATCH_OPERATION_CONTENT_TYPE = "Content-Type: application/http";
    private static final String BATCH_OPERATION_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: binary";
    private static final String BATCH_OPERATION_CONTENT_ID_TEMPLATE = "Content-ID: %d";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String OPERATION_TEMPLATE = "%s %s %s";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String PATH_TEMPLATE = "%s/%s";

    /*
     * Track the status codes expected for the batching operations here as the batch body does not get parsed in
     * Azure Core where this information is maintained.
     */
    private static final int[] EXPECTED_DELETE_STATUS_CODES = {202};
    private static final int[] EXPECTED_SET_TIER_STATUS_CODES = {200, 202};

    private final ClientLogger logger = new ClientLogger(BlobBatch.class);

    private final BlobAsyncClient blobAsyncClient;

    private final Deque<Mono<? extends Response<?>>> batchOperationQueue;
    private final Queue<ByteBuffer> batchRequest;
    private final Map<Integer, BlobBatchOperationResponse<?>> batchMapping;

    private final AtomicInteger contentId;
    private final String batchBoundary;
    private final String contentType;

    private BlobBatchType batchType;

    BlobBatch(String accountUrl, HttpPipeline pipeline) {
        this.contentId = new AtomicInteger();
        this.batchBoundary = String.format(BATCH_BOUNDARY_TEMPLATE, UUID.randomUUID());
        this.contentType = String.format(REQUEST_CONTENT_TYPE_TEMPLATE, batchBoundary);

        boolean batchHeadersPolicySet = false;
        HttpPipelineBuilder batchPipelineBuilder = new HttpPipelineBuilder().httpClient(this::setupBatchOperation);
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

        this.blobAsyncClient = new BlobClientBuilder()
            .endpoint(accountUrl)
            .blobName("")
            .pipeline(batchPipelineBuilder.build())
            .buildAsyncClient();

        this.batchOperationQueue = new ConcurrentLinkedDeque<>();
        this.batchRequest = new ConcurrentLinkedQueue<>();
        this.batchMapping = new ConcurrentHashMap<>();
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
            Utility.urlEncode(Utility.urlDecode(blobName))), accessTier, null);
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
            Utility.urlEncode(Utility.urlDecode(blobName))), accessTier, leaseId);
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
        return setBlobAccessTierHelper(getUrlPath(blobUrl), accessTier, null);
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
        return setBlobAccessTierHelper(getUrlPath(blobUrl), accessTier, leaseId);
    }

    private Response<Void> setBlobAccessTierHelper(String urlPath, AccessTier accessTier, String leaseId) {
        setBatchType(BlobBatchType.SET_TIER);
        return createBatchOperation(blobAsyncClient.setAccessTierWithResponse(accessTier, null, leaseId),
            urlPath, EXPECTED_SET_TIER_STATUS_CODES);
    }

    private <T> Response<T> createBatchOperation(Mono<Response<T>> response, String urlPath,
        int... expectedStatusCodes) {
        int id = contentId.getAndIncrement();
        batchOperationQueue.add(response
            .subscriberContext(Context.of(BATCH_REQUEST_CONTENT_ID, id, BATCH_REQUEST_URL_PATH, urlPath)));

        BlobBatchOperationResponse<T> batchOperationResponse = new BlobBatchOperationResponse<>(expectedStatusCodes);
        batchMapping.put(id, batchOperationResponse);
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

    Flux<ByteBuffer> getBody() {
        if (batchOperationQueue.isEmpty()) {
            throw logger.logExceptionAsError(new UnsupportedOperationException("Empty batch requests aren't allowed."));
        }

        // 'flatMap' the requests to trigger them to run through the pipeline.
        Disposable disposable = Flux.fromStream(batchOperationQueue.stream())
            .flatMap(batchOperation -> batchOperation)
            .subscribe();

        /* Wait until the 'Flux' is disposed of (aka complete) instead of blocking as this will prevent Reactor from
         * throwing an exception if this was ran in a Reactor thread.
         */
        while (!disposable.isDisposed()) {
            // This is used as opposed to block as it won't trigger an exception if ran in a Reactor thread.
        }

        this.batchRequest.add(ByteBuffer.wrap(
            String.format("--%s--%s", batchBoundary, BlobBatchHelper.HTTP_NEWLINE).getBytes(StandardCharsets.UTF_8)));

        return Flux.fromIterable(batchRequest);
    }

    long getContentLength() {
        long contentLength = 0;

        for (ByteBuffer request : batchRequest) {
            contentLength += request.remaining();
        }

        return contentLength;
    }

    String getContentType() {
        return contentType;
    }

    BlobBatchOperationResponse<?> getBatchRequest(int contentId) {
        return batchMapping.get(contentId);
    }

    int getOperationCount() {
        return batchMapping.size();
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
        Map<String, String> headers = context.getHttpRequest().getHeaders().toMap();
        headers.entrySet().removeIf(header -> header.getValue() == null);

        context.getHttpRequest().setHeaders(new HttpHeaders(headers));

        // Add the "Content-Id" header which allows this request to be mapped to the response.
        context.getHttpRequest().setHeader(CONTENT_ID, context.getData(BATCH_REQUEST_CONTENT_ID).get().toString());

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
            context.getHttpRequest().setUrl(requestUrl.toURL());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(new IllegalStateException(ex)));
        }

        return next.process();
    }

    /*
     * This will "send" the batch operation request when triggered, it simply acts as a way to build and write the
     * batch operation into the overall request and then returns nothing as the response.
     */
    private Mono<HttpResponse> setupBatchOperation(HttpRequest request) {
        return Mono.fromRunnable(() -> {
            int contentId = Integer.parseInt(request.getHeaders().remove(CONTENT_ID).getValue());

            StringBuilder batchRequestBuilder = new StringBuilder();
            appendWithNewline(batchRequestBuilder, "--" + batchBoundary);
            appendWithNewline(batchRequestBuilder, BATCH_OPERATION_CONTENT_TYPE);
            appendWithNewline(batchRequestBuilder, BATCH_OPERATION_CONTENT_TRANSFER_ENCODING);
            appendWithNewline(batchRequestBuilder, String.format(BATCH_OPERATION_CONTENT_ID_TEMPLATE, contentId));
            batchRequestBuilder.append(BlobBatchHelper.HTTP_NEWLINE);

            String method = request.getHttpMethod().toString();
            String urlPath = request.getUrl().getPath();
            String urlQuery = request.getUrl().getQuery();
            if (!ImplUtils.isNullOrEmpty(urlQuery)) {
                urlPath = urlPath + "?" + urlQuery;
            }
            appendWithNewline(batchRequestBuilder, String.format(OPERATION_TEMPLATE, method, urlPath, HTTP_VERSION));

            request.getHeaders().stream()
                .filter(header -> !X_MS_VERSION.equalsIgnoreCase(header.getName()))
                .forEach(header -> appendWithNewline(batchRequestBuilder,
                    String.format(HEADER_TEMPLATE, header.getName(), header.getValue())));

            batchRequestBuilder.append(BlobBatchHelper.HTTP_NEWLINE);

            batchRequest.add(ByteBuffer.wrap(batchRequestBuilder.toString().getBytes(StandardCharsets.UTF_8)));
            batchMapping.get(contentId).setRequest(request);
        });
    }

    private void appendWithNewline(StringBuilder stringBuilder, String value) {
        stringBuilder.append(value).append(BlobBatchHelper.HTTP_NEWLINE);
    }
}
