// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import reactor.core.publisher.Mono;

abstract class FeedRangeAsyncVisitor<TResult> {
    public abstract Mono<TResult> visitAsync(FeedRangePartitionKey feedRange);

    public abstract Mono<TResult> visitAsync(FeedRangePartitionKeyRange feedRange);

    public abstract Mono<TResult> visitAsync(FeedRangeEpk feedRange);
}