// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import java.util.concurrent.CompletableFuture;

/**
 * Delegate to process a request for retry an operation
 */
interface BatchAsyncBatcherRetrier {
    CompletableFuture<Void> apply(ItemBatchOperation<?> request);
}
