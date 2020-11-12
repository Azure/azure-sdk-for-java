// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

abstract class GenericFeedRangeVisitor<TInput> {
    public abstract void visit(FeedRangeEpkImpl feedRange, TInput input);

    public abstract void visit(FeedRangePartitionKeyRangeImpl feedRange, TInput input);

    public abstract void visit(FeedRangePartitionKeyImpl feedRange, TInput input);
}