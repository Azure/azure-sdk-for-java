package com.azure.storage.queue.models;

import java.util.Arrays;
import java.util.List;

public final class QueuesSegmentOptions {
    private ListQueuesIncludeType[] includes;

    private String prefix;

    private int maxResults;

    public List<ListQueuesIncludeType> includes() {
        return Arrays.asList(includes);
    }

    public QueuesSegmentOptions includes(ListQueuesIncludeType... includes) {
        this.includes = includes;
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
