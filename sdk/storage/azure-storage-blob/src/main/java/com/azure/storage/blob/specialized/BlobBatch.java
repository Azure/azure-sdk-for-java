// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public final class BlobBatch {
    private static final String BATCH_BOUNDARY_TEMPLATE = "batch_%s";
    private static final String CONTENT_TYPE = "Content-Type: application/http";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: binary";
    private static final String CONTENT_ID_TEMPLATE = "Content-ID: %d";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String OPERATION_TEMPLATE = "%s %s %s";
    private static final String HEADER_TEMPLATE = "%s: %s";

    private final ClientLogger logger = new ClientLogger(BlobBatch.class);

    private final URL accountUrl;
    private final HttpPipeline batchPipeline;

    private final Deque<BlobBatchOperationResponse> batchOperationQueue;
    private final List<ByteBuffer> batchRequest;

    private final AtomicInteger contentId;
    private final String batchBoundary;

    private BlobBatchType batchType;

    public BlobBatch(BlobServiceClient client) {
        this(client.getAccountUrl(), client.getHttpPipeline());
    }

    public BlobBatch(BlobServiceAsyncClient client) {
        this(client.getAccountUrl(), client.getHttpPipeline());
    }

    BlobBatch(URL accountUrl, HttpPipeline pipeline) {
        this.contentId = new AtomicInteger(0);
        this.batchBoundary = String.format(BATCH_BOUNDARY_TEMPLATE, UUID.randomUUID());

        this.accountUrl = accountUrl;

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy policy = pipeline.getPolicy(i);

            if (policy instanceof HttpLoggingPolicy || policy instanceof RetryPolicy) {
                continue;
            }

            policies.add(pipeline.getPolicy(i));
        }

        this.batchPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(new BatchClient(this::sendCallback))
            .build();

        this.batchOperationQueue = new ConcurrentLinkedDeque<>();
        this.batchRequest = new ArrayList<>();
    }

    private void sendCallback(HttpRequest request) {
        StringBuilder batchRequestBuilder = new StringBuilder();
        appendWithNewline(batchRequestBuilder, "--" + batchBoundary);
        appendWithNewline(batchRequestBuilder, CONTENT_TYPE);
        appendWithNewline(batchRequestBuilder, CONTENT_TRANSFER_ENCODING);
        appendWithNewline(batchRequestBuilder, String.format(CONTENT_ID_TEMPLATE, contentId.getAndIncrement()));
        batchRequestBuilder.append("\n");

        String method = request.getHttpMethod().toString();
        String urlPath = request.getUrl().getPath();
        appendWithNewline(batchRequestBuilder, String.format(OPERATION_TEMPLATE, method, urlPath, HTTP_VERSION));

        request.getHeaders().stream()
            .filter(header -> !"x-ms-version".equalsIgnoreCase(header.getName()) &&
                !ImplUtils.isNullOrEmpty(header.getValue()))
            .forEach(header -> appendWithNewline(batchRequestBuilder,
                String.format(HEADER_TEMPLATE, header.getName(), header.getValue())));

        batchRequest.add(ByteBuffer.wrap(batchRequestBuilder.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void appendWithNewline(StringBuilder stringBuilder, String value) {
        stringBuilder.append(value).append("\n");
    }

    public Flux<ByteBuffer> getBody() {
        while (!batchOperationQueue.isEmpty()) {
            BlobBatchOperationResponse batchOperation = batchOperationQueue.pop();
            batchOperation.setContentId(contentId.get());
            batchOperation.getResponse().block();
        }

        this.batchRequest.add(ByteBuffer.wrap(String.format("--%s--", batchBoundary).getBytes(StandardCharsets.UTF_8)));

        return Flux.fromIterable(this.batchRequest);
    }

    public long getContentLength() {
        long contentLength = 0;

        for (ByteBuffer request : batchRequest) {
            contentLength += request.remaining();
        }

        return contentLength;
    }

    public String getContentType() {
        return String.format("multipart/mixed; boundary=%s", batchBoundary);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param deleteOptions Delete options for the blob and its snapshots.
     * @param blobAccessConditions Additional access conditions that must be met to allow this operation.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> delete(String containerName, String blobName,
        DeleteSnapshotsOptionType deleteOptions, BlobAccessConditions blobAccessConditions) {
        return deleteHelper(buildClient(containerName, blobName), deleteOptions, blobAccessConditions);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * @param blobUrl URI of the blob.
     * @param deleteOptions Delete options for the blob and its snapshots.
     * @param blobAccessConditions Additional access conditions that must be met to allow this operation.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> delete(URL blobUrl, DeleteSnapshotsOptionType deleteOptions,
        BlobAccessConditions blobAccessConditions) {
        return deleteHelper(buildClient(blobUrl), deleteOptions, blobAccessConditions);
    }

    private Response<Void> deleteHelper(BlobAsyncClientBase client, DeleteSnapshotsOptionType deleteOptions,
        BlobAccessConditions blobAccessConditions) {
        if (this.batchType == null) {
            this.batchType = BlobBatchType.DELETE;
        } else if (this.batchType != BlobBatchType.DELETE) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(String.format(Locale.ROOT,
                "'BlobBatch' only supports homogeneous operations and is a %s batch.", this.batchType)));
        }

        return createAndReturnBatchOperation(client.deleteWithResponse(deleteOptions, blobAccessConditions));
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param accessTier The tier to set on the blob.
     * @param leaseAccessConditions Lease access conditions that must be met to allow this operation.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setTier(String containerName, String blobName, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {
        return setTierHelper(buildClient(containerName, blobName), accessTier, leaseAccessConditions);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * @param blobUrl URI of the blob.
     * @param accessTier The tier to set on the blob.
     * @param leaseAccessConditions Lease access conditions that must be met to allow this operation.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setTier(URL blobUrl, AccessTier accessTier, LeaseAccessConditions leaseAccessConditions) {
        return setTierHelper(buildClient(blobUrl), accessTier, leaseAccessConditions);
    }

    private Response<Void> setTierHelper(BlobAsyncClientBase client, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {
        if (this.batchType == null) {
            this.batchType = BlobBatchType.SET_TIER;
        } else if (this.batchType != BlobBatchType.SET_TIER) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(String.format(Locale.ROOT,
                "'BlobBatch' only supports homogeneous operations and is a %s batch.", this.batchType)));
        }

        return createAndReturnBatchOperation(client.setTierWithResponse(accessTier, null, leaseAccessConditions));
    }

    private <T> Response<T> createAndReturnBatchOperation(Mono<Response<T>> response) {
        BlobBatchOperationResponse<T> batchOperation = new BlobBatchOperationResponse<>(response);
        this.batchOperationQueue.push(batchOperation);
        return batchOperation;
    }

    private BlobAsyncClientBase buildClient(String containerName, String blobName) {
        return new BlobClientBuilder()
            .endpoint(accountUrl.toString())
            .containerName(containerName)
            .blobName(blobName)
            .pipeline(batchPipeline)
            .buildBlobAsyncClient();
    }

    private BlobAsyncClientBase buildClient(URL blobUrl) {
        return new BlobClientBuilder()
            .endpoint(blobUrl.toString())
            .pipeline(batchPipeline)
            .buildBlobAsyncClient();
    }

    private enum BlobBatchType {
        DELETE,
        SET_TIER;
    }
}
