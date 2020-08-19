// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import reactor.core.publisher.Mono;

/**
 * Executor implementation that processes a list of operations.
 */
interface BatchAsyncBatcherExecutor {
    Mono<PartitionKeyRangeBatchExecutionResult> apply(PartitionKeyRangeServerBatchRequest request) throws Exception;
}
