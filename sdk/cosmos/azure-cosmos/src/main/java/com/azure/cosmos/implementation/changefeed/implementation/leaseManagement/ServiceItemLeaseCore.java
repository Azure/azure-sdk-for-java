// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;

import java.util.Map;

/***
 * Document service lease base with PartitionKeyRange.
 */
public class ServiceItemLeaseCore extends ServiceItemLease {
    public ServiceItemLeaseCore(
            String id,
            String leaseToken,
            String owner,
            FeedRangeInternal feedRangeInternal,
            String continuationToken,
            String etag,
            Map<String, String> properties,
            String timestamp,
            String ts) {

        super(id, leaseToken, owner, feedRangeInternal, continuationToken, etag, properties, timestamp, ts);
    }

    @Override
    public ServiceItemLeaseVersion getVersion() {
        return ServiceItemLeaseVersion.PartitionKeyRangeBasedLease;
    }
}
