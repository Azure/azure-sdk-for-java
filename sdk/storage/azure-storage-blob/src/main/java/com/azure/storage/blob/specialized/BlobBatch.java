// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.BatchOperation;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class BlobBatch {
    private static final String BATCH_BOUNDARY_TEMPLATE = "batch_%s";
    private static final String CONTENT_TYPE = "Content-Type: application/http";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: binary";
    private static final String CONTENT_ID_TEMPLATE = "Content-ID: %d";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String OPERATION_TEMPLATE = "%s %s %s";

    private final URL accountUrl;
    private final HttpPipeline batchPipeline;

    private final List<Mono<? extends Response>> batchRequestQueue;
    private final List<ByteBuffer> batchRequest;

    private final AtomicInteger contentId;
    private final String batchBoundary;

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

        HttpPipelinePolicy[] policies = new HttpLoggingPolicy[pipeline.getPolicyCount()];
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            policies[i] = pipeline.getPolicy(i);
        }

        this.batchPipeline = new HttpPipelineBuilder()
            .policies(policies)
            .httpClient(new BatchClient(this::sendCallback))
            .build();

        this.batchRequestQueue = new ArrayList<>();
        this.batchRequest = new ArrayList<>();
    }

    private void sendCallback(HttpRequest request) {
        StringBuilder batchRequestBuilder = new StringBuilder();
        batchRequestBuilder
            .append("--")
            .append(batchBoundary)
            .append(CONTENT_TYPE)
            .append(CONTENT_TRANSFER_ENCODING)
            .append(String.format(CONTENT_ID_TEMPLATE, contentId.getAndIncrement()))
            .append('\n');

        String method = request.getHttpMethod().toString();
        String urlPath = request.getUrl().getPath();
        batchRequestBuilder.append(String.format(OPERATION_TEMPLATE, method, urlPath, HTTP_VERSION));

        request.getHeaders().stream().forEach(header -> batchRequestBuilder
            .append(String.format("%s: %s", header.getName(), header.getValue())));

        batchRequest.add(ByteBuffer.wrap(batchRequestBuilder.toString().getBytes(StandardCharsets.UTF_8)));
    }

    Flux<ByteBuffer> generateRequestBody() {
        List<ByteBuffer> requestBody = new ArrayList<>();

        for (Mono<? extends Response> batchRequest : batchRequestQueue) {
            batchRequest.block();
        }

        return Flux.fromIterable(this.batchRequest);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param deleteOptions Delete options for the blob and its snapshots.
     * @param blobAccessConditions Additional access conditions that must be met to allow this operation.
     * @return a {@link BatchOperation} that will be used to associate this operation to the response when the batch is
     * submitted.
     */
    public BatchOperation<Void> delete(String containerName, String blobName,
        DeleteSnapshotsOptionType deleteOptions, BlobAccessConditions blobAccessConditions) {
        return deleteHelper(buildClient(containerName, blobName), deleteOptions, blobAccessConditions);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * @param blobUrl URI of the blob.
     * @param deleteOptions Delete options for the blob and its snapshots.
     * @param blobAccessConditions Additional access conditions that must be met to allow this operation.
     * @return a {@link BatchOperation} that will be used to associate this operation to the response when the batch is
     * submitted.
     */
    public BatchOperation<Void> delete(URL blobUrl, DeleteSnapshotsOptionType deleteOptions,
        BlobAccessConditions blobAccessConditions) {
        return deleteHelper(buildClient(blobUrl), deleteOptions, blobAccessConditions);
    }

    private BatchOperation<Void> deleteHelper(BlobAsyncClientBase client, DeleteSnapshotsOptionType deleteOptions,
        BlobAccessConditions blobAccessConditions) {
        Mono<VoidResponse> deleteResponse = client.deleteWithResponse(deleteOptions, blobAccessConditions);
        this.batchRequestQueue.add(deleteResponse);
        return new BlobBatchOperation<>(deleteResponse);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param accessTier The tier to set on the blob.
     * @param leaseAccessConditions Lease access conditions that must be met to allow this operation.
     * @return a {@link BatchOperation} that will be used to associate this operation to the response when the batch is
     * submitted.
     */
    public BatchOperation<Void> setTier(String containerName, String blobName, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {
        return setTierHelper(buildClient(containerName, blobName), accessTier, leaseAccessConditions);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * @param blobUrl URI of the blob.
     * @param accessTier The tier to set on the blob.
     * @param leaseAccessConditions Lease access conditions that must be met to allow this operation.
     * @return a {@link BatchOperation} that will be used to associate this operation to the response when the batch is
     * submitted.
     */
    public BatchOperation<Void> setTier(URL blobUrl, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {
        return setTierHelper(buildClient(blobUrl), accessTier, leaseAccessConditions);
    }

    private BatchOperation<Void> setTierHelper(BlobAsyncClientBase client, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {
        Mono<VoidResponse> setTierResponse = client.setTierWithResponse(accessTier, null, leaseAccessConditions);
        this.batchRequestQueue.add(setTierResponse);
        return new BlobBatchOperation<>(setTierResponse);
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
}
