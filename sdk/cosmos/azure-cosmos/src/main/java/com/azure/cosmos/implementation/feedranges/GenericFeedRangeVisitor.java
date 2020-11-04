// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

abstract class GenericFeedRangeVisitor<TInput> {
    public abstract void visit(FeedRangeEpk feedRange, TInput input);

    public abstract void visit(FeedRangePartitionKeyRange feedRange, TInput input);

    public abstract void visit(FeedRangePartitionKey feedRange, TInput input);
}