/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.changefeed.internal;

import com.microsoft.azure.cosmos.changefeed.ChangeFeedContextClient;
import com.microsoft.azure.cosmos.changefeed.LeaseContainer;
import com.microsoft.azure.cosmos.changefeed.RemainingPartitionWork;
import com.microsoft.azure.cosmos.changefeed.RemainingWorkEstimator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation for {@link RemainingWorkEstimator}.
 */
public class RemainingWorkEstimatorImpl implements RemainingWorkEstimator {
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
    public Mono<Long> getEstimatedRemainingWork() {
        return this.getEstimatedRemainingWorkPerPartition()
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
    public Flux<RemainingPartitionWork> getEstimatedRemainingWorkPerPartition() {
        return null;
    }
}
