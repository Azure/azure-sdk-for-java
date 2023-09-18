// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.FeedResponse;

public interface ChangeFeedProcessorContext<T> {
    String getLeaseToken();
    FeedResponse<T> getFeedResponse();
}
