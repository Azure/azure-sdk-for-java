// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Used to estimate the pending work remaining to be read in the Change Feed. Calculates the sum of pending work
 *   based on the difference between the latest status of the feed and the status of each existing lease.
 */
public interface RemainingWorkEstimator {
    /**
     * Calculates an estimate of the pending work remaining to be read in the Change Feed in amount of documents in the whole collection.
     *
     * @return an estimation of pending work in amount of documents.
     */
    Mono<Long> estimatedRemainingWork();

    /**
     * Calculates an estimate of the pending work remaining to be read in the Change Feed in amount of documents per partition.
     *
     * @return an estimation of pending work in amount of documents per partitions.
     */
    Flux<RemainingPartitionWork> estimatedRemainingWorkPerPartition();
}
