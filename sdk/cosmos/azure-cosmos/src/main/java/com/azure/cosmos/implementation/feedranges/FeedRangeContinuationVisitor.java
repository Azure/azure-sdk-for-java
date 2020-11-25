// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

abstract class FeedRangeContinuationVisitor {
    public abstract void visit(FeedRangeCompositeContinuationImpl feedRangeCompositeContinuation);
}
