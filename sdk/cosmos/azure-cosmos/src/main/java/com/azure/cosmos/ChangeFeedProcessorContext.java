// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.FeedResponse;

public abstract class ChangeFeedProcessorContext<T> {
    public abstract String getLeaseToken();
    public abstract FeedResponse<T> getFeedResponse();
}
