// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import reactor.core.publisher.Mono;

/**
 * Delegate to process a request for retry an operation
 */
interface BatchAsyncBatcherRetrier {
    Mono<Void> apply(ItemBatchOperation<?> request);
}
