// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import reactor.core.publisher.Mono;

/**
 * Executor implementation that processes a list of operations.
 */
interface BatchAsyncBatcherExecutor {
    Mono<PartitionKeyRangeBatchExecutionResult> apply(PartitionKeyRangeServerBatchRequest request);
}
