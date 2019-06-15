// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.models;

/**
 * This model class contains the options for listing queues.
 */
public final class QueuesSegmentOptions {
    private boolean includeMetadata;

    private String prefix;

    private Integer maxResults;

    public boolean includeMetadata() {
        return includeMetadata;
    }

    public QueuesSegmentOptions includeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    public String prefix() {
        return prefix;
    }

    public QueuesSegmentOptions prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Integer maxResults() {
        return maxResults;
    }

    public QueuesSegmentOptions maxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }
}
