// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

public final class ListSharesOptions {
    private String prefix;

    private String marker;

    private Integer maxResults;

    private boolean includeMetadata;

    private boolean includeSnapshots;

    public ListSharesOptions prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String prefix() {
        return prefix;
    }

    public ListSharesOptions marker(String marker) {
        this.marker = marker;
        return this;
    }

    public String marker() {
        return marker;
    }

    public ListSharesOptions maxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public Integer maxResults() {
        return maxResults;
    }

    public ListSharesOptions includeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    public boolean includeMetadata() {
        return includeMetadata;
    }

    public ListSharesOptions includeSnapshots(boolean includeSnapshots) {
        this.includeSnapshots = includeSnapshots;
        return this;
    }

    public boolean includeSnapshots() {
        return includeSnapshots;
    }
}
