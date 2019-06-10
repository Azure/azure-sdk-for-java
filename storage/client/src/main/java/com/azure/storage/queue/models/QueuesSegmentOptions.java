package com.azure.storage.queue.models;

public final class QueuesSegmentOptions {
    private boolean includeMetadata;

    private String prefix;

    private int maxResults;

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
