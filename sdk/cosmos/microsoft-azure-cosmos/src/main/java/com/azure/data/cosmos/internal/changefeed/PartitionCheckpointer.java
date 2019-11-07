// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * Checkpoint the given partition up to the given continuation token.
 */
public interface PartitionCheckpointer {
    /**
     * Checkpoints the given partition up to the given continuation token.
     *
     * @param сontinuationToken the continuation token.
     * @return a deferred operation of this call.
     */
    Mono<Lease> checkpointPartition(String сontinuationToken);
}
