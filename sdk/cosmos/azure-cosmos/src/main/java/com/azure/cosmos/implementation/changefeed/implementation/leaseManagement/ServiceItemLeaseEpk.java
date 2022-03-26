// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;

import java.util.Map;

/***
 * Lease implementation for EPK based leases.
 */
public class ServiceItemLeaseEpk extends ServiceItemLease {

    public ServiceItemLeaseEpk(
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
        return ServiceItemLeaseVersion.EPKRangeBasedLease;
    }
}
