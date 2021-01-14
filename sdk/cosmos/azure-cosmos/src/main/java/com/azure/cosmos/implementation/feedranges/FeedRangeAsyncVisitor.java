// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import reactor.core.publisher.Mono;

abstract class FeedRangeAsyncVisitor<TResult> {
    public abstract Mono<TResult> visit(FeedRangePartitionKeyImpl feedRange);

    public abstract Mono<TResult> visit(FeedRangePartitionKeyRangeImpl feedRange);

    public abstract Mono<TResult> visit(FeedRangeEpkImpl feedRange);
}