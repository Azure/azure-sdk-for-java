// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler;

import com.azure.cosmos.implementation.changefeed.Lease;
import reactor.core.publisher.Flux;

/***
 * Handler to handle partition split or partition merge.
 */
public interface FeedRangeGoneHandler {
    Flux<Lease> handlePartitionGone();
    boolean shouldDeleteCurrentLease();
}
