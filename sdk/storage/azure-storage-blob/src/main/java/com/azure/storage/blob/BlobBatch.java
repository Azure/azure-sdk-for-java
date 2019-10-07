// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

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
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.common.Constants;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class provides
 */
public final class BlobBatch {
    private static final String BATCH_REQUEST_CONTENT_ID = "Batch-Request-Content-Id";
    private static final String BATCH_REQUEST_URL_PATH = "Batch-Request-Url-Path";
    private static final String CONTENT_ID = "Content-Id";
    private static final String BATCH_BOUNDARY_TEMPLATE = "batch_%s";
    private static final String CONTENT_TYPE = "Content-Type: application/http";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: binary";
    private static final String CONTENT_ID_TEMPLATE = "Content-ID: %d";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String OPERATION_TEMPLATE = "%s %s %s";
    private static final String HEADER_TEMPLATE = "%s: %s";

    private static final int[] EXPECTED_DELETE_STATUS_CODES = { 202 };
    private static final int[] EXPECTED_SET_TIER_STATUS_CODES = { 200, 202 };

    private final ClientLogger logger = new ClientLogger(BlobBatch.class);

    private final BlobAsyncClient batchClient;

    private final Deque<Mono<? extends Response>> batchOperationQueue;
    private final List<ByteBuffer> batchRequest;
    private final Map<Integer, BlobBatchOperationResponse> batchMapping;

    private final AtomicInteger contentId;
    private final String batchBoundary;

    private BlobBatchType batchType;

    /**
     * Constructs a {@link BlobBatch} using the {@link BlobServiceClient#getHttpPipeline()
     * BlobServiceClient's HttpPipeline} to build a modified {@link HttpPipeline} that is used to prepare the requests
     * in the batch.
     *
     * @param client {@link BlobServiceClient} used to construct the batch.
     */
    public BlobBatch(BlobServiceClient client) {
        this(client.getAccountUrl(), client.getHttpPipeline());
    }

    /**
     * Constructs a {@link BlobBatch} using the {@link BlobServiceAsyncClient#getHttpPipeline()
     * BlobServiceAsyncClient's HttpPipeline} to build a modified {@link HttpPipeline} that is used to prepare the
     * requests in the batch.
     *
     * @param client {@link BlobServiceAsyncClient} used to construct the batch.
     */
    public BlobBatch(BlobServiceAsyncClient client) {
        this(client.getAccountUrl(), client.getHttpPipeline());
    }

    BlobBatch(String accountUrl, HttpPipeline pipeline) {
        this.contentId = new AtomicInteger(0);
        this.batchBoundary = String.format(BATCH_BOUNDARY_TEMPLATE, UUID.randomUUID());

        boolean batchHeadersPolicySet = false;
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy policy = pipeline.getPolicy(i);

            if (policy instanceof SharedKeyCredentialPolicy) {
                batchHeadersPolicySet = true;
                // The batch policy needs to be added before the SharedKey policy to run preparation cleanup.
                policies.add(this::cleanseHeaders);
            }

            policies.add(pipeline.getPolicy(i));
        }

        if (!batchHeadersPolicySet) {
            policies.add(this::cleanseHeaders);
        }

        this.batchClient = new BlobClientBuilder()
            .endpoint(accountUrl)
            .containerName("dummy")
            .blobName("dummy")
            .pipeline(new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(this::setupBatchOperation)
                .build())
            .buildAsyncClient();

        this.batchOperationQueue = new ConcurrentLinkedDeque<>();
        this.batchRequest = new ArrayList<>();
        this.batchMapping = new ConcurrentHashMap<>();
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> delete(String containerName, String blobName) {
        return deleteHelper(String.format("%s/%s", containerName, blobName), null, null);
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
        return deleteHelper(String.format("%s/%s", containerName, blobName), deleteOptions, blobAccessConditions);
    }

    /**
     * Adds a delete blob operation to the batch.
     *
     * @param blobUrl URI of the blob.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> delete(String blobUrl) {
        return deleteHelper(getUrlPath(blobUrl), null, null);
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
    public Response<Void> delete(String blobUrl, DeleteSnapshotsOptionType deleteOptions,
        BlobAccessConditions blobAccessConditions) {
        return deleteHelper(getUrlPath(blobUrl), deleteOptions, blobAccessConditions);
    }

    private Response<Void> deleteHelper(String urlPath, DeleteSnapshotsOptionType deleteOptions,
        BlobAccessConditions blobAccessConditions) {
        setBatchType(BlobBatchType.DELETE);
        return createBatchOperation(batchClient.deleteWithResponse(deleteOptions, blobAccessConditions),
            urlPath, EXPECTED_DELETE_STATUS_CODES);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * @param containerName The container of the blob.
     * @param blobName The name of the blob.
     * @param accessTier The tier to set on the blob.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setTier(String containerName, String blobName, AccessTier accessTier) {
        return setTierHelper(String.format("%s/%s", containerName, blobName), accessTier, null);
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
        return setTierHelper(String.format("%s/%s", containerName, blobName), accessTier, leaseAccessConditions);
    }

    /**
     * Adds a set tier operation to the batch.
     *
     * @param blobUrl URI of the blob.
     * @param accessTier The tier to set on the blob.
     * @return a {@link Response} that will be used to associate this operation to the response when the batch is
     * submitted.
     * @throws UnsupportedOperationException If this batch has already added an operation of another type.
     */
    public Response<Void> setTier(String blobUrl, AccessTier accessTier) {
        return setTierHelper(getUrlPath(blobUrl), accessTier, null);
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
    public Response<Void> setTier(String blobUrl, AccessTier accessTier, LeaseAccessConditions leaseAccessConditions) {
        return setTierHelper(getUrlPath(blobUrl), accessTier, leaseAccessConditions);
    }

    private Response<Void> setTierHelper(String urlPath, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {
        setBatchType(BlobBatchType.SET_TIER);
        return createBatchOperation(batchClient.setAccessTierWithResponse(accessTier, null, leaseAccessConditions),
            urlPath, EXPECTED_SET_TIER_STATUS_CODES);
    }

    private <T> Response<T> createBatchOperation(Mono<Response<T>> response, String urlPath,
        int... expectedStatusCodes) {
        int contentId = this.contentId.getAndIncrement();
        this.batchOperationQueue.add(response
            .subscriberContext(Context.of(BATCH_REQUEST_CONTENT_ID, contentId, BATCH_REQUEST_URL_PATH, urlPath)));

        BlobBatchOperationResponse<T> batchOperationResponse = new BlobBatchOperationResponse<>(expectedStatusCodes);
        this.batchMapping.put(contentId, batchOperationResponse);
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

        Disposable disposable = Flux.fromStream(batchOperationQueue.stream())
            .flatMap(batchOperation -> batchOperation)
            .subscribe();

        while (!disposable.isDisposed()) {
            // Wait until the batch operation has processed in the pipeline.
            // This is used as opposed to block as it won't trigger an exception if ran in a Reactor thread.
        }

        this.batchRequest.add(ByteBuffer
            .wrap(String.format("--%s--\r\n", batchBoundary).getBytes(StandardCharsets.UTF_8)));

        return Flux.fromIterable(this.batchRequest);
    }

    long getContentLength() {
        long contentLength = 0;

        for (ByteBuffer request : batchRequest) {
            contentLength += request.remaining();
        }

        return contentLength;
    }

    String getContentType() {
        return String.format("multipart/mixed; boundary=%s", batchBoundary);
    }

    BlobBatchOperationResponse getBatchRequest(int contentId) {
        return this.batchMapping.get(contentId);
    }

    /*
     * Enum class that indicates which type of operation this batch is using.
     */
    private enum BlobBatchType {
        DELETE,
        SET_TIER
    }

    /*
     * This performs a cleanup operation that would be handled when the request is sent through Netty or OkHttp.
     * Additionally, it removes the "x-ms-version" header from the request as batch operation requests cannot have this
     * and it adds the header "Content-Id" that allows the request to be mapped to the response.
     */
    private Mono<HttpResponse> cleanseHeaders(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Remove the "x-ms-header" as it shouldn't be included in the batch operation request.
        context.getHttpRequest().getHeaders().remove(Constants.HeaderConstants.VERSION);

        // Remove any null headers (this is done in Netty and OkHttp normally).
        Map<String, String> headers = context.getHttpRequest().getHeaders().toMap();
        headers.entrySet().removeIf(header -> header.getValue() == null);

        context.getHttpRequest().setHeaders(new HttpHeaders(headers));

        // Add the "Content-Id" header which allows this request to be mapped to the response.
        context.getHttpRequest().setHeader(CONTENT_ID, context.getData(BATCH_REQUEST_CONTENT_ID).get().toString());

        // Set the request URL to the correct endpoint.
        try {
            UrlBuilder requestUrl = UrlBuilder.parse(context.getHttpRequest().getUrl());
            requestUrl.setPath(context.getData(BATCH_REQUEST_URL_PATH).get().toString());
            context.getHttpRequest().setUrl(requestUrl.toURL());
        } catch (MalformedURLException ex) {
            throw Exceptions.propagate(logger.logExceptionAsError(new IllegalStateException(ex)));
        }

        return next.process();
    }

    /*
     * This will "send" the batch operation request when triggered, it simply acts as a way to build and write the
     * batch operation into the overall request and then ends.
     */
    private Mono<HttpResponse> setupBatchOperation(HttpRequest request) {
        int contentId = Integer.parseInt(request.getHeaders().remove(CONTENT_ID).getValue());

        StringBuilder batchRequestBuilder = new StringBuilder();
        appendWithNewline(batchRequestBuilder, "--" + batchBoundary);
        appendWithNewline(batchRequestBuilder, CONTENT_TYPE);
        appendWithNewline(batchRequestBuilder, CONTENT_TRANSFER_ENCODING);
        appendWithNewline(batchRequestBuilder, String.format(CONTENT_ID_TEMPLATE, contentId));
        batchRequestBuilder.append("\r\n");

        String method = request.getHttpMethod().toString();
        String urlPath = request.getUrl().getPath();
        String urlQuery = request.getUrl().getQuery();
        if (!ImplUtils.isNullOrEmpty(urlQuery)) {
            urlPath = urlPath + "?" + urlQuery;
        }
        appendWithNewline(batchRequestBuilder, String.format(OPERATION_TEMPLATE, method, urlPath, HTTP_VERSION));

        request.getHeaders().stream()
            .filter(header -> !Constants.HeaderConstants.VERSION.equalsIgnoreCase(header.getName()))
            .forEach(header -> appendWithNewline(batchRequestBuilder,
                String.format(HEADER_TEMPLATE, header.getName(), header.getValue())));

        batchRequestBuilder.append("\r\n");

        batchRequest.add(ByteBuffer.wrap(batchRequestBuilder.toString().getBytes(StandardCharsets.UTF_8)));

        this.batchMapping.get(contentId).setRequest(request);

        return Mono.empty();
    }

    private void appendWithNewline(StringBuilder stringBuilder, String value) {
        stringBuilder.append(value).append("\r\n");
    }
}
