// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.LeaseContainer;
import com.azure.data.cosmos.internal.changefeed.RemainingPartitionWork;
import com.azure.data.cosmos.internal.changefeed.RemainingWorkEstimator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation for {@link RemainingWorkEstimator}.
 */
class RemainingWorkEstimatorImpl implements RemainingWorkEstimator {
    private final char PKRangeIdSeparator = ':';
    private final char SegmentSeparator = '#';
    private final String LSNPropertyName = "_lsn";
    private final ChangeFeedContextClient feedDocumentClient;
    private final LeaseContainer leaseContainer;
    private final String collectionSelfLink;
    private final int degreeOfParallelism;

    public RemainingWorkEstimatorImpl(
        LeaseContainer leaseContainer,
        ChangeFeedContextClient feedDocumentClient,
        String collectionSelfLink,
        int degreeOfParallelism) {

        if (leaseContainer == null) throw new IllegalArgumentException("leaseContainer");
        if (collectionSelfLink == null || collectionSelfLink.isEmpty()) throw new IllegalArgumentException("collectionSelfLink");
        if (feedDocumentClient == null) throw new IllegalArgumentException("feedDocumentClient");
        if (degreeOfParallelism < 1) throw new IllegalArgumentException("degreeOfParallelism - Degree of parallelism is out of range");

        this.leaseContainer = leaseContainer;
        this.collectionSelfLink = collectionSelfLink;
        this.feedDocumentClient = feedDocumentClient;
        this.degreeOfParallelism = degreeOfParallelism;
    }

    @Override
    public Mono<Long> estimatedRemainingWork() {
        return this.estimatedRemainingWorkPerPartition()
            .map(RemainingPartitionWork::getRemainingWork)
            .collectList()
            .map(list -> {
                long sum;
                if (list.size() == 0) {
                    sum = 1;
                } else {
                    sum = 0;
                    for (long value : list) {
                        sum += value;
                    }
                }

                return sum;
            });
    }

    @Override
    public Flux<RemainingPartitionWork> estimatedRemainingWorkPerPartition() {
        return null;
    }
}
