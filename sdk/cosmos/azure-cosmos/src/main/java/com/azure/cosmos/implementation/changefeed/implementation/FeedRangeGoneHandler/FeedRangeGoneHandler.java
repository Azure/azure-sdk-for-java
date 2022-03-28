// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.FeedRangeGoneHandler;

import com.azure.cosmos.implementation.changefeed.Lease;
import reactor.core.publisher.Flux;

public interface FeedRangeGoneHandler {
    Flux<Lease> handlePartitionGone();
    boolean shouldRemoveGoneLease();
}
