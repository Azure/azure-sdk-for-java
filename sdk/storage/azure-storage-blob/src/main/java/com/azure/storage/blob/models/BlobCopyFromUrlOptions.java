// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.RequestConditions;

import java.util.Map;

public class BlobCopyFromUrlOptions {
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private RequestConditions sourceRequestConditions;
    private BlobRequestConditions destinationRequestConditions;

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public BlobCopyFromUrlOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public BlobCopyFromUrlOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public AccessTier getTier() {
        return tier;
    }

    public BlobCopyFromUrlOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    public RequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    public BlobCopyFromUrlOptions setSourceRequestConditions(RequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    public BlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    public BlobCopyFromUrlOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }
}
