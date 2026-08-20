// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class PartitionKeyRangeResolution<TOperation> {
    private final PartitionScopeThresholds thresholds;
    private final TOperation operation;
    private final Exception exception;

    private PartitionKeyRangeResolution(
        PartitionScopeThresholds thresholds,
        TOperation operation,
        Exception exception) {

        this.thresholds = thresholds;
        this.operation = checkNotNull(operation, "expected non-null operation");
        this.exception = exception;
    }

    static <TOperation> PartitionKeyRangeResolution<TOperation> success(
        PartitionScopeThresholds thresholds,
        TOperation operation) {

        return new PartitionKeyRangeResolution<>(
            checkNotNull(thresholds, "expected non-null thresholds"),
            operation,
            null);
    }

    static <TOperation> PartitionKeyRangeResolution<TOperation> failure(
        TOperation operation,
        Exception exception) {

        return new PartitionKeyRangeResolution<>(
            null,
            operation,
            checkNotNull(exception, "expected non-null exception"));
    }

    PartitionScopeThresholds getThresholds() {
        return this.thresholds;
    }

    TOperation getOperation() {
        return this.operation;
    }

    Exception getException() {
        return this.exception;
    }

    boolean isSuccess() {
        return this.exception == null;
    }
}
