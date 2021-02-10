// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.QueryMetrics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

class EncryptionFeedResponse<T> extends FeedResponse<T> {
    EncryptionFeedResponse(
        List<T> results,
        Map<String, String> headers,
        ConcurrentMap<String, QueryMetrics> queryMetricsMap,
        boolean useEtagAsContinuation,
        boolean isNoChanges) {

        super(results, headers, queryMetricsMap, useEtagAsContinuation, isNoChanges);
    }
}
