// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import reactor.core.publisher.Mono;

abstract class FeedRangeAsyncVisitorWithArg<TResult, TArg> {
    public abstract Mono<TResult> visitAsync(FeedRangePartitionKeyImpl feedRange, TArg argument);

    public abstract Mono<TResult> visitAsync(FeedRangePartitionKeyRangeImpl feedRange,
                                             TArg argument);

    public abstract Mono<TResult> visitAsync(FeedRangeEpkImpl feedRange, TArg argument);
}