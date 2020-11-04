// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.routing.Range;
import reactor.core.publisher.Mono;

import java.util.List;

final class FeedRangePartitionKeyRangeExtractor extends FeedRangeAsyncVisitor<List<Range<String>>> {
    @Override
    public Mono<List<Range<String>>> visitAsync(FeedRangePartitionKey feedRange) {
        // TODO fabianm - Implement
        return null;
    }

    @Override
    public Mono<List<Range<String>>> visitAsync(FeedRangePartitionKeyRange feedRange) {
        // TODO fabianm - Implement
        return null;
    }

    @Override
    public Mono<List<Range<String>>> visitAsync(FeedRangeEpk feedRange) {
        // TODO fabianm - Implement
        return null;
    }
}
