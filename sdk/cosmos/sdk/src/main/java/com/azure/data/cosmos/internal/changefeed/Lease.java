// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.ChangeFeedProcessor;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Represents a lease that is persisted as a document in the lease collection.
 * <p>
 * Leases are used to:
 * Keep track of the {@link ChangeFeedProcessor} progress for a particular Partition Key RANGE.
 * Distribute load between different instances of {@link ChangeFeedProcessor}.
 * Ensure reliable recovery for cases when an instance of {@link ChangeFeedProcessor} gets disconnected, hangs or crashes.
 */
public interface Lease {
    /**
     * Gets the partition associated with the lease.
     *
     * @return the partition associated with the lease.
     */
    String getLeaseToken();

    /**
     * Gets the host name owner of the lease.
     *
     * <p>
     * The Owner keeps track which {@link ChangeFeedProcessor} is currently processing that Partition Key RANGE.
     *
     * @return the host name owner of the lease.
     */
    String getOwner();

    /**
     * Gets the timestamp of the lease.
     *
     * @return the timestamp of the lease.
     */
    String getTimestamp();

    /**
     * Gets the continuation token used to determine the last processed point of the Change Feed.
     *
     * @return the continuation token used to determine the last processed point of the Change Feed.
     */
    String getContinuationToken();

    /**
     * Sets the continuation token used to determine the last processed point of the Change Feed.
     *
     *
     * @param continuationToken the continuation token used to determine the last processed point of the Change Feed.
     */
    void setContinuationToken(String continuationToken);

    /**
     * Gets the lease ID.
     *
     * @return the lease ID.
     */
    String getId();

    /**
     * Gets the concurrency token.
     *
     * @return the concurrency token.
     */
    String getConcurrencyToken();

    /**
     * Gets the custom lease item which can be managed from {@link PartitionLoadBalancingStrategy}.
     *
     * @return the custom lease item.
     */
    Map<String, String> getProperties();

    /**
     * Sets the host name owner of the lease.
     *
     * <p>
     * The Owner keeps track which {@link ChangeFeedProcessor} is currently processing that Partition Key RANGE.
     *
     * @param owner the host name owner of the lease.
     */
    void setOwner(String owner);

    /**
     * Sets the timestamp of the lease.
     *
     * <p>
     * The timestamp is used to determine lease expiration.
     *
     * @param timestamp the timestamp of the lease.
     */
    void setTimestamp(ZonedDateTime timestamp);

    /**
     * Sets the lease ID.
     *
     *
     * @param id the lease ID.
     */
    void setId(String id);

    /**
     * Sets the concurrency token.
     *
     *
     * @param concurrencyToken the concurrency token.
     */
    void setConcurrencyToken(String concurrencyToken);

    /**
     * Sets the custom lease item which can be managed from {@link PartitionLoadBalancingStrategy}.
     *
     *
     * @param properties the custom lease item.
     */
    void setProperties(Map<String,String> properties);
}
