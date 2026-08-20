// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import reactor.core.publisher.SynchronousSink;

/**
 * Utilities for handling individual responses emitted by Cosmos bulk operations.
 *
 * <p>A failed bulk operation can contain an exception without an item response, a missing item response, or an
 * unsuccessful item response. Converting those response values into stream errors prevents callers from
 * dereferencing a missing response or silently skipping a failed operation. This utility is designed for Reactor's
 * {@code handle} operator so successful responses pass through without creating an inner publisher.</p>
 */
final class CosmosBulkOperationResponseUtils {

    private CosmosBulkOperationResponseUtils() {
    }

    /**
     * Emits a successful bulk response or terminates the stream with a Cosmos exception representing the failure.
     *
     * @param response the response emitted for an individual bulk operation
     * @param sink the synchronous sink used to emit the response or exception
     */
    static void emitErrorForFailedBulkOperation(
        CosmosBulkOperationResponse<?> response,
        SynchronousSink<CosmosBulkOperationResponse<?>> sink) {
        CosmosBulkItemResponse itemResponse = response.getResponse();
        if (response.getException() != null) {
            sink.error(response.getException());
        } else if (itemResponse == null) {
            sink.error(new IllegalStateException("Bulk operation completed without an item response or exception."));
        } else if (!itemResponse.isSuccessStatusCode()) {
            sink.error(createCosmosException(itemResponse));
        } else {
            sink.next(response);
        }
    }

    private static CosmosException createCosmosException(CosmosBulkItemResponse response) {
        CosmosException exception = BridgeInternal.createCosmosException(
                "Bulk operation failed with status code " + response.getStatusCode() + ":"
                    + response.getSubStatusCode() + ".",
                null,
                response.getResponseHeaders(),
                response.getStatusCode(),
                null);

        BridgeInternal.setSubStatusCode(exception, response.getSubStatusCode());
        BridgeInternal.setCosmosDiagnostics(exception, response.getCosmosDiagnostics());
        return exception;
    }
}
