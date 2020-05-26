// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.RequestConditions;

import java.time.Duration;
import java.util.Map;

/**
 * Extended options that may be passed when beginning a copy operation.
 */
@Fluent
public class BlobBeginCopyOptions {
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private RehydratePriority rehydratePriority;
    private RequestConditions sourceRequestConditions;
    private BlobRequestConditions destinationRequestConditions;
    private Duration pollInterval;

    /**
     * @return The metadata to associate with the destination blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata The metadata to associate with the destination blob.
     * @return The updated options
     */
    public BlobBeginCopyOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return The tags to associate with the blob.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags The tags to associate with the blob.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @return {@link AccessTier} for the destination blob.
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * @param tier {@link AccessTier} for the destination blob.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * @return {@link RehydratePriority} for rehydrating the blob.
     */
    public RehydratePriority getRehydratePriority() {
        return rehydratePriority;
    }

    /**
     * @param rehydratePriority {@link RehydratePriority} for rehydrating the blob.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setRehydratePriority(RehydratePriority rehydratePriority) {
        this.rehydratePriority = rehydratePriority;
        return this;
    }

    /**
     * @return {@link RequestConditions} for the source.
     */
    public RequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * @param sourceRequestConditions {@link RequestConditions} for the source.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setSourceRequestConditions(RequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * @return {@link BlobRequestConditions} for the destination.
     */
    public BlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * @param destinationRequestConditions {@link BlobRequestConditions} for the destination
     * @return The updated options.
     */
    public BlobBeginCopyOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * @return Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     */
    public Duration getPollInterval() {
        return pollInterval;
    }

    /**
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }
}
