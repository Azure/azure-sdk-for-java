// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;

/**
 * Contains the information about a single {@link BlobBatch} operation.
 *
 * @param <T> Type that is returned by the operation.
 */
@Immutable
final class BlobBatchOperation<T> {
    private final BlobBatchOperationResponse<T> batchOperationResponse;
    private final Mono<Response<T>> response;
    private final String requestUrlPath;

    /*
     * Creates a {@link BlobBatchOperation} which contains all information that is needed to execute the individual
     * operation.
     *
     * @param batchOperationResponse {@link BlobBatchOperationResponse} which is returned to the caller of the batch
     * operation.
     * @param response Response which is returned from the API which the batch operation uses. This is used to generate
     * the request in a deferred manner.
     * @param requestUrlPath Relative path of the blob in the batch operation.
     */
    BlobBatchOperation(BlobBatchOperationResponse<T> batchOperationResponse, Mono<Response<T>> response,
        String requestUrlPath) {
        this.batchOperationResponse = batchOperationResponse;
        this.response = response;
        this.requestUrlPath = requestUrlPath;
    }

    /*
     * The {@link BlobBatchOperationResponse} which is returned to the caller of the batch operation.
     *
     * @return Response returned to the caller.
     */
    BlobBatchOperationResponse<T> getBatchOperationResponse() {
        return batchOperationResponse;
    }

    /*
     * Response which is returned from the API which the batch operation uses. This is used to generate the
     * request in a deferred manner.
     *
     * @return Response from the API which the batch operation uses.
     */
    Mono<Response<T>> getResponse() {
        return response;
    }

    /*
     * Relative path of the blob in the batch operation.
     *
     * @return Blob relative path.
     */
    String getRequestUrlPath() {
        return requestUrlPath;
    }
}
