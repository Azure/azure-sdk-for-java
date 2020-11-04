// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

abstract class FeedRangeVisitor {
    public abstract void visit(FeedRangeEpk feedRange);

    public abstract void visit(FeedRangePartitionKeyRange feedRange);

    public abstract void visit(FeedRangePartitionKey feedRange);
}