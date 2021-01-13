// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

abstract class FeedRangeTransformer<TResult> {
    public abstract TResult visit(FeedRangePartitionKeyImpl feedRange);

    public abstract TResult visit(FeedRangePartitionKeyRangeImpl feedRange);

    public abstract TResult visit(FeedRangeEpkImpl feedRange);
}
