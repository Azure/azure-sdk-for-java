// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

abstract class FeedRangeTransformer<TResult> {
    public abstract TResult visit(FeedRangePartitionKey feedRange);

    public abstract TResult visit(FeedRangePartitionKeyRange feedRange);

    public abstract TResult visit(FeedRangeEpk feedRange);
}
