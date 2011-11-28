package com.microsoft.windowsazure.services.blob.models;

public class ListContainersOptions extends BlobServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private boolean includeMetadata;

    public String getPrefix() {
        return prefix;
    }

    public ListContainersOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMarker() {
        return marker;
    }

    public ListContainersOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ListContainersOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public ListContainersOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }
}