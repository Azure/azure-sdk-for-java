// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

abstract class FeedRangeVisitor {
    public abstract void visit(FeedRangeEpkImpl feedRange);

    public abstract void visit(FeedRangePartitionKeyRangeImpl feedRange);

    public abstract void visit(FeedRangePartitionKeyImpl feedRange);
}