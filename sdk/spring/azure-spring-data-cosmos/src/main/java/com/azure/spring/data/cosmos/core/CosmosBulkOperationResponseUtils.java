// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

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
     * Emits a successful bulk response or terminates the stream with the operation exception.
     *
     * @param response the response emitted for an individual bulk operation
     * @param sink the synchronous sink used to emit the response or exception
     * @param <TContext> the type of the bulk operation context
     */
    static <TContext> void emitErrorForFailedBulkOperation(
        CosmosBulkOperationResponse<TContext> response,
        SynchronousSink<CosmosBulkOperationResponse<TContext>> sink) {
        CosmosBulkItemResponse itemResponse = response.getResponse();
        if (response.getException() != null) {
            sink.error(response.getException());
        } else if (itemResponse == null) {
            sink.error(new IllegalStateException("Bulk operation completed without an item response or exception."));
        } else if (!itemResponse.isSuccessStatusCode()) {
            sink.error(new IllegalStateException(
                "Bulk operation failed with status code " + itemResponse.getStatusCode() + "."));
        } else {
            sink.next(response);
        }
    }
}
