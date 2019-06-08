/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.changefeed;

import com.microsoft.azure.cosmos.ChangeFeedProcessor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Represents a lease that is persisted as a document in the lease collection.
 * <p>
 * Leases are used to:
 * Keep track of the {@link ChangeFeedProcessor} progress for a particular Partition Key Range.
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
     * The Owner keeps track which {@link ChangeFeedProcessor} is currently processing that Partition Key Range.
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
     * Gets the custom lease properties which can be managed from {@link PartitionLoadBalancingStrategy}.
     *
     * @return the custom lease properties.
     */
    Map<String, String> getProperties();

    /**
     * Sets the host name owner of the lease.
     *
     * <p>
     * The Owner keeps track which {@link ChangeFeedProcessor} is currently processing that Partition Key Range.
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
     * Sets the custom lease properties which can be managed from {@link PartitionLoadBalancingStrategy}.
     *
     *
     * @param properties the custom lease properties.
     */
    void setProperties(Map<String,String> properties);
}
