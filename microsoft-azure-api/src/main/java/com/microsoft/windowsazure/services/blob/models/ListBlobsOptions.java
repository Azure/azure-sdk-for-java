package com.microsoft.windowsazure.services.blob.models;

public class ListBlobsOptions extends BlobServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private String delimiter;
    private boolean includeMetadata;
    private boolean includeSnapshots;
    private boolean includeUncommittedBlobs;

    @Override
    public ListBlobsOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ListBlobsOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMarker() {
        return marker;
    }

    public ListBlobsOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ListBlobsOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public ListBlobsOptions setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public ListBlobsOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    public boolean isIncludeSnapshots() {
        return includeSnapshots;
    }

    public ListBlobsOptions setIncludeSnapshots(boolean includeSnapshots) {
        this.includeSnapshots = includeSnapshots;
        return this;
    }

    public boolean isIncludeUncommittedBlobs() {
        return includeUncommittedBlobs;
    }

    public ListBlobsOptions setIncludeUncommittedBlobs(boolean includeUncommittedBlobs) {
        this.includeUncommittedBlobs = includeUncommittedBlobs;
        return this;
    }
}
