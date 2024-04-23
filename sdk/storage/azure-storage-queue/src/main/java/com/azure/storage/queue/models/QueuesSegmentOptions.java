// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;

/**
 * A set of options for selecting queues from Storage Queue service.
 *
 * <ul>
 *     <li>
 *         Providing {@link QueuesSegmentOptions#getPrefix() prefix} will filter {@link QueueItem queues} that begin
 *         with the prefix.
 *     </li>
 *     <li>
 *         Providing {@link QueuesSegmentOptions#getMaxResultsPerPage() maxResultsPerPage} will limit the number of
 *         {@link QueueItem queues} returned in a single page.
 *     </li>
 *     <li>
 *         Setting {@link QueuesSegmentOptions#isIncludeMetadata() includeMetadata} to true will include the metadata
 *         of each {@link QueueItem queue}, if false {@link QueueItem#getMetadata()}  metadata} for each queue will be
 *         {@code null}.
 *     </li>
 * </ul>
 *
 * @see QueueServiceClient
 * @see QueueServiceAsyncClient
 */
@Fluent
public final class QueuesSegmentOptions {
    private boolean includeMetadata;

    private String prefix;

    private Integer maxResultsPerPage;

    /**
     * @return the status of including metadata when listing queues
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets the status of including queue metadata when listing queues
     *
     * @param includeMetadata Flag indicating if metadata should be included in the listing
     * @return An updated QueuesSegmentOptions object
     */
    public QueuesSegmentOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    /**
     * @return the prefix the queue name must match to be included in the listing
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix that a queue must match to be included in the listing
     *
     * @param prefix The prefix that queues must start with to pass the filter
     * @return An updated QueuesSegmentOptions object
     */
    public QueuesSegmentOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * @return the maximum number of queues to include in a single response
     */
    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    /**
     * Sets the maximum number of queues to include in a single response
     *
     * @param maxResultsPerPage Maximum number of results to include in a single response
     * @return An updated QueuesSegmentOptions object
     */
    public QueuesSegmentOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }
}
