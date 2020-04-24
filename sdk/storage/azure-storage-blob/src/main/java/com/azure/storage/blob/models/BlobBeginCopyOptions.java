// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.RequestConditions;

import java.time.Duration;
import java.util.Map;

public class BlobBeginCopyOptions {
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private RehydratePriority rehydratePriority;
    private RequestConditions sourceRequestConditions;
    private BlobRequestConditions destinationRequestConditions;
    private Duration pollInterval;

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public BlobBeginCopyOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public BlobBeginCopyOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public AccessTier getTier() {
        return tier;
    }

    public BlobBeginCopyOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    public RehydratePriority getRehydratePriority() {
        return rehydratePriority;
    }

    public BlobBeginCopyOptions setRehydratePriority(RehydratePriority rehydratePriority) {
        this.rehydratePriority = rehydratePriority;
        return this;
    }

    public RequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    public BlobBeginCopyOptions setSourceRequestConditions(RequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    public BlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    public BlobBeginCopyOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public BlobBeginCopyOptions setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }
}
