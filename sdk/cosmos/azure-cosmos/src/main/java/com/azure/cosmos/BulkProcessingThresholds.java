// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import reactor.core.publisher.Flux;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  @deprecated forRemoval = true, since = "4.18"
 *  This class is not necessary anymore and will be removed. Please use one of the following overloads instead
 * - {@link CosmosAsyncContainer#processBulkOperations(Flux)}
 * - {@link CosmosAsyncContainer#processBulkOperations(Flux, BulkExecutionOptions)}
 * - {@link CosmosContainer#processBulkOperations(Iterable)}
 * - {@link CosmosContainer#processBulkOperations(Iterable, BulkExecutionOptions)}
 *  and to pass in a custom context use one of the {@link BulkOperations} factory methods allowing to provide
 *  an operation specific context
 *
 * Encapsulates internal state used to dynamically determine max micro batch size for bulk operations.
 * It allows passing this state for one `BulkProcessingOptions` to another in case bulk operations are
 * expected to have similar characteristics and the context for determining the micro batch size should be preserved.
 */
@Deprecated() //forRemoval = true, since = "4.18"
public final class BulkProcessingThresholds<TContext> {
    private final ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds;

    /**
     *  @deprecated forRemoval = true, since = "4.18"
     *  This class is not necessary anymore and will be removed. Please use one of the following overloads instead
     * - {@link CosmosAsyncContainer#processBulkOperations(Flux)}
     * - {@link CosmosAsyncContainer#processBulkOperations(Flux, BulkExecutionOptions)}
     * - {@link CosmosContainer#processBulkOperations(Iterable)}
     * - {@link CosmosContainer#processBulkOperations(Iterable, BulkExecutionOptions)}
     *  and to pass in a custom context use one of the {@link BulkOperations} factory methods allowing to provide
     *  an operation specific context
     *
     * Constructor
     */
    @Deprecated() //forRemoval = true, since = "4.18"
    public BulkProcessingThresholds() {
        this.partitionScopeThresholds = new ConcurrentHashMap<>();
    }

    ConcurrentMap<String, PartitionScopeThresholds> getPartitionScopeThresholds() {
        return this.partitionScopeThresholds;
    }
}
