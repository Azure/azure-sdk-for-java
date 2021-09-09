// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpAuthorization;
import com.azure.core.http.RequestConditions;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.Map;

/**
 * Extended options that may be passed when copying a blob.
 */
@Fluent
public class BlobCopyFromUrlOptions {
    private final String copySource;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private RequestConditions sourceRequestConditions;
    private BlobRequestConditions destinationRequestConditions;
    private HttpAuthorization sourceAuthorization;
    private BlobImmutabilityPolicy immutabilityPolicy;
    private Boolean legalHold;

    /**
     * @param copySource The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public BlobCopyFromUrlOptions(String copySource) {
        StorageImplUtils.assertNotNull("copySource", copySource);
        this.copySource = copySource;
    }

    /**
     * @return The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public String getCopySource() {
        return this.copySource;
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
    public BlobCopyFromUrlOptions setMetadata(Map<String, String> metadata) {
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
    public BlobCopyFromUrlOptions setTags(Map<String, String> tags) {
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
    public BlobCopyFromUrlOptions setTier(AccessTier tier) {
        this.tier = tier;
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
    public BlobCopyFromUrlOptions setSourceRequestConditions(RequestConditions sourceRequestConditions) {
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
     * @param destinationRequestConditions {@link BlobRequestConditions} for the destination.
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * @return auth header for access to source.
     */
    public HttpAuthorization getSourceAuthorization() {
        return sourceAuthorization;
    }

    /**
     * Sets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @param sourceAuthorization auth header for access to source.
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization) {
        this.sourceAuthorization = sourceAuthorization;
        return this;
    }

    /**
     * @return {@link BlobImmutabilityPolicy}
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    /**
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy}
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        this.immutabilityPolicy = immutabilityPolicy;
        return this;
    }

    /**
     * @return If a legal hold should be placed on the blob.
     */
    public Boolean isLegalHold() {
        return legalHold;
    }

    /**
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     *
     * @param legalHold Indicates if a legal hold should be placed on the blob.
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setLegalHold(Boolean legalHold) {
        this.legalHold = legalHold;
        return this;
    }
}
