// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import reactor.core.publisher.SynchronousSink;

/**
 * Utilities for handling individual responses emitted by Cosmos bulk operations.
 *
 * <p>A failed bulk operation can contain an exception without an item response. Converting the exception into a
 * stream error prevents callers from dereferencing a missing response. This utility is designed for Reactor's
 * {@code handle} operator so item responses pass through without creating an inner publisher.</p>
 */
final class CosmosBulkOperationResponseUtils {

    private CosmosBulkOperationResponseUtils() {
    }

    /**
     * Emits an item response or terminates the stream with the operation exception.
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
        } else {
            sink.next(response);
        }
    }
}
