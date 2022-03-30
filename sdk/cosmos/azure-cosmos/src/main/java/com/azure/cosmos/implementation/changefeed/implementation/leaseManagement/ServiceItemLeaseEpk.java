// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;

import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

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
    public ChangeFeedState getContinuationState(String containerRid) {
        checkNotNull(containerRid, "Argument 'containerRid' must not be null.");

        return new ChangeFeedStateV1(
                containerRid,
                this.getFeedRange(),
                ChangeFeedMode.INCREMENTAL,
                ChangeFeedStartFromInternal.createFromETagAndFeedRange(this.getContinuationToken(), this.getFeedRange()),
                null);
    }

    @Override
    public ServiceItemLeaseVersion getVersion() {
        return ServiceItemLeaseVersion.EPKRangeBasedLease;
    }
}
