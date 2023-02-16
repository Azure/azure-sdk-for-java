// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains the information necessary for submitting a batch.
 */
@Immutable
final class BlobBatchOperationInfo {
    private static final String X_MS_VERSION = "x-ms-version";
    private static final String BATCH_OPERATION_CONTENT_TYPE = "Content-Type: application/http";
    private static final String BATCH_OPERATION_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: binary";
    private static final String HTTP_VERSION = "HTTP/1.1";

    private final AtomicInteger contentId;
    private final String batchBoundary;
    private final String contentType;
    private final Collection<ByteBuffer> batchOperations;
    private final Map<Integer, BlobBatchOperationResponse<?>> batchOperationResponseMap;

    /**
     * Creates a {@link BlobBatchOperationInfo} which contains all information necessary for submitting a batch
     * operation.
     */
    BlobBatchOperationInfo() {
        this.contentId = new AtomicInteger();
        this.batchBoundary = "batch_" + UUID.randomUUID();
        this.contentType = "multipart/mixed; boundary=" + batchBoundary;
        this.batchOperations = new ConcurrentLinkedQueue<>();
        this.batchOperationResponseMap = new ConcurrentHashMap<>();
    }

    /*
     * Gets the body for the batch operation.
     *
     * @return Request body.
     */
    Collection<ByteBuffer> getBody() {
        return batchOperations;
    }

    /*
     * Gets the size of the batch operation request.
     *
     * @return Size of the request body.
     */
    long getContentLength() {
        return batchOperations.stream().map(buffer -> (long) buffer.remaining()).reduce(0L, Long::sum);
    }

    /*
     * Gets the Content-Type header for the batch operation request.
     *
     * @return Content-Type header for the request.
     */
    String getContentType() {
        return contentType;
    }

    /*
     * Adds an operation to the operation set being submitted in the batch.
     *
     * @param batchOperation Operation to add to the batch.
     * @param request The {@link HttpRequest} for the operation.
     */
    void addBatchOperation(BlobBatchOperationResponse<?> batchOperation, HttpRequest request) {
        int contentId = this.contentId.getAndIncrement();

        StringBuilder batchRequestBuilder = new StringBuilder();
        appendWithNewline(batchRequestBuilder, "--" + batchBoundary);
        appendWithNewline(batchRequestBuilder, BATCH_OPERATION_CONTENT_TYPE);
        appendWithNewline(batchRequestBuilder, BATCH_OPERATION_CONTENT_TRANSFER_ENCODING);
        appendWithNewline(batchRequestBuilder, "Content-ID: " + contentId);
        batchRequestBuilder.append(BlobBatchHelper.HTTP_NEWLINE);

        String method = request.getHttpMethod().toString();
        String urlPath = request.getUrl().getPath();
        String urlQuery = request.getUrl().getQuery();
        if (!CoreUtils.isNullOrEmpty(urlQuery)) {
            urlPath = urlPath + "?" + urlQuery;
        }
        appendWithNewline(batchRequestBuilder, method + " " + urlPath + " " + HTTP_VERSION);

        /*
         * The 'x-ms-version' header is removed from batch operations as all batch operations will use the
         * 'x-ms-version' used in the batch request. This header is illegal and will fail the batch request if present
         * in any operation.
         */
        request.getHeaders().stream()
            .filter(header -> !X_MS_VERSION.equalsIgnoreCase(header.getName()))
            .forEach(header -> appendWithNewline(batchRequestBuilder, header.getName() + ": " + header.getValue()));

        batchRequestBuilder.append(BlobBatchHelper.HTTP_NEWLINE);

        batchOperationResponseMap.put(contentId, batchOperation.setRequest(request));
        batchOperations.add(ByteBuffer.wrap(batchRequestBuilder.toString().getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Completes the batch by adding the final boundary identifier to the request body.
     */
    void finalizeBatchOperations() {
        batchOperations.add(ByteBuffer.wrap(
            ("--" + batchBoundary + "--" + BlobBatchHelper.HTTP_NEWLINE).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Gets the batch operation with the passed Content-ID.
     *
     * @param contentId Content-ID of the operation.
     * @return The {@link BlobBatchOperationResponse} correlated to the passed Content-ID.
     */
    BlobBatchOperationResponse<?> getBatchRequest(int contentId) {
        return batchOperationResponseMap.get(contentId);
    }

    /*
     * Gets the number of operations contained in the batch.
     *
     * @return Number of operations in the batch.
     */
    int getOperationCount() {
        return batchOperationResponseMap.size();
    }

    private static void appendWithNewline(StringBuilder stringBuilder, String value) {
        stringBuilder.append(value).append(BlobBatchHelper.HTTP_NEWLINE);
    }
}
