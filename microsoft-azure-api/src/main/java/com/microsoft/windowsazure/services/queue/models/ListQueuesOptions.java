package com.microsoft.windowsazure.services.queue.models;

public class ListQueuesOptions extends QueueServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private boolean includeMetadata;

    @Override
    public ListQueuesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ListQueuesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMarker() {
        return marker;
    }

    public ListQueuesOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ListQueuesOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public ListQueuesOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }
}