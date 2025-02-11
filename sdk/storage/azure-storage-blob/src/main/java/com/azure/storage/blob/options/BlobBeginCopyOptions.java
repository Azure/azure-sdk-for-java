// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
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
    private BlobImmutabilityPolicy immutabilityPolicy;
    private Boolean legalHold;

    /**
     * Creates a new instance of {@link BlobBeginCopyOptions}.
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @throws NullPointerException If {@code sourceUrl} is null.
     */
    public BlobBeginCopyOptions(String sourceUrl) {
        StorageImplUtils.assertNotNull("sourceUrl", sourceUrl);
        this.sourceUrl = sourceUrl;
    }

    /**
     * Gets the source URL.
     *
     * @return The source URL.
     */
    public String getSourceUrl() {
        return this.sourceUrl;
    }

    /**
     * Gets the metadata to associate with the destination blob.
     *
     * @return The metadata to associate with the destination blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata to associate with the destination blob.
     *
     * @param metadata The metadata to associate with the destination blob.
     * @return The updated options
     */
    public BlobBeginCopyOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the tags to associate with the blob.
     *
     * @return The tags to associate with the blob.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Sets the tags to associate with the blob.
     *
     * @param tags The tags to associate with the blob.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the {@link AccessTier} for the destination blob.
     *
     * @return {@link AccessTier} for the destination blob.
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * Sets the {@link AccessTier} for the destination blob.
     *
     * @param tier {@link AccessTier} for the destination blob.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Gets the {@link RehydratePriority} for rehydrating the blob.
     *
     * @return {@link RehydratePriority} for rehydrating the blob.
     */
    public RehydratePriority getRehydratePriority() {
        return rehydratePriority;
    }

    /**
     * Sets the {@link RehydratePriority} for rehydrating the blob.
     *
     * @param rehydratePriority {@link RehydratePriority} for rehydrating the blob.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setRehydratePriority(RehydratePriority rehydratePriority) {
        this.rehydratePriority = rehydratePriority;
        return this;
    }

    /**
     * Gets the {@link BlobBeginCopySourceRequestConditions} for the source.
     *
     * @return {@link BlobBeginCopySourceRequestConditions} for the source.
     */
    public BlobBeginCopySourceRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * Sets the {@link BlobBeginCopySourceRequestConditions} for the source.
     *
     * @param sourceRequestConditions {@link BlobBeginCopySourceRequestConditions} for the source.
     * @return The updated options.
     */
    public BlobBeginCopyOptions
        setSourceRequestConditions(BlobBeginCopySourceRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions} for the destination.
     *
     * @return {@link BlobRequestConditions} for the destination.
     */
    public BlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions} for the destination.
     *
     * @param destinationRequestConditions {@link BlobRequestConditions} for the destination
     * @return The updated options.
     */
    public BlobBeginCopyOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * Gets the duration between each poll for the copy status.
     *
     * @return Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     */
    public Duration getPollInterval() {
        return pollInterval;
    }

    /**
     * Sets the duration between each poll for the copy status.
     *
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }

    /**
     * Gets whether the destination blob should be sealed (marked as read only).
     * <p>
     * Only applicable for Append Blobs.
     *
     * @return Whether the destination blob should be sealed (marked as read only).
     */
    public Boolean isSealDestination() {
        return sealDestination;
    }

    /**
     * Sets whether the destination blob should be sealed (marked as read only).
     * <p>
     * Only applicable for Append Blobs.
     *
     * @param sealDestination Whether the destination blob should be sealed (marked as read only).
     * @return The updated options.
     */
    public BlobBeginCopyOptions setSealDestination(Boolean sealDestination) {
        this.sealDestination = sealDestination;
        return this;
    }

    /**
     * Gets the {@link BlobImmutabilityPolicy} for the destination blob.
     *
     * @return {@link BlobImmutabilityPolicy}
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    /**
     * Sets the {@link BlobImmutabilityPolicy} for the destination blob.
     * <p>
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy}
     * @return The updated options.
     */
    public BlobBeginCopyOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        this.immutabilityPolicy = immutabilityPolicy;
        return this;
    }

    /**
     * Gets if a legal hold should be placed on the blob.
     *
     * @return If a legal hold should be placed on the blob.
     */
    public Boolean isLegalHold() {
        return legalHold;
    }

    /**
     * Sets if a legal hold should be placed on the blob.
     * <p>
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     * @param legalHold Indicates if a legal hold should be placed on the blob.
     * @return The updated options.
     */
    public BlobBeginCopyOptions setLegalHold(Boolean legalHold) {
        this.legalHold = legalHold;
        return this;
    }
}
