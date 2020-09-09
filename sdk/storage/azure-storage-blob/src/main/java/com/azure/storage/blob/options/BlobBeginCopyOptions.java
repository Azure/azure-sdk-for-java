// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.time.Duration;
import java.util.Map;

/**
 * Extended options that may be passed when beginning a copy operation.
 */
@Fluent
public class BlobBeginCopyOptions {
    private final String sourceUrl;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private RehydratePriority rehydratePriority;
    private BlobBeginCopySourceRequestConditions sourceRequestConditions;
    private BlobRequestConditions destinationRequestConditions;
    private Duration pollInterval;
    private Boolean sealDestination;

    /**
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public BlobBeginCopyOptions(String sourceUrl) {
        StorageImplUtils.assertNotNull("sourceUrl", sourceUrl);
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return The source URL.
     */
    public String getSourceUrl() {
        return this.sourceUrl;
    }

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
     * @return {@link BlobBeginCopySourceRequestConditions} for the source.
     */
    public BlobBeginCopySourceRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * @param sourceRequestConditions {@link BlobBeginCopySourceRequestConditions} for the source.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setSourceRequestConditions(
        BlobBeginCopySourceRequestConditions sourceRequestConditions) {
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

    /**
     *  Only applicable for Append Blobs.
     * @return Whether or not the destination blob should be sealed.
     */
    public Boolean isSealDestination() {
        return sealDestination;
    }

    /**
     * Only applicable for Append Blobs.
     *
     * @param sealDestination Whether or not the destination blob should be sealed.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setSealDestination(Boolean sealDestination) {
        this.sealDestination = sealDestination;
        return this;
    }
}
