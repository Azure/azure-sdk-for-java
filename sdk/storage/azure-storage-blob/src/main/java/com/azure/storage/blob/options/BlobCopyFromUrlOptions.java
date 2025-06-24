// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpAuthorization;
import com.azure.core.http.RequestConditions;
import com.azure.storage.blob.models.FileShareTokenIntent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobCopySourceTagsMode;
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
    private BlobCopySourceTagsMode copySourceTags;
    private FileShareTokenIntent sourceShareTokenIntent;

    /**
     * Creates a new instance of {@link BlobCopyFromUrlOptions}.
     *
     * @param copySource The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @throws NullPointerException If {@code copySource} is null.
     */
    public BlobCopyFromUrlOptions(String copySource) {
        StorageImplUtils.assertNotNull("copySource", copySource);
        this.copySource = copySource;
    }

    /**
     * Gets the source URL to copy from.
     *
     * @return The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public String getCopySource() {
        return this.copySource;
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
    public BlobCopyFromUrlOptions setMetadata(Map<String, String> metadata) {
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
    public BlobCopyFromUrlOptions setTags(Map<String, String> tags) {
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
    public BlobCopyFromUrlOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Gets the {@link RequestConditions} for the source.
     *
     * @return {@link RequestConditions} for the source.
     */
    public RequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * Sets the {@link RequestConditions} for the source.
     *
     * @param sourceRequestConditions {@link RequestConditions} for the source.
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setSourceRequestConditions(RequestConditions sourceRequestConditions) {
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
     * @param destinationRequestConditions {@link BlobRequestConditions} for the destination.
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * Gets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
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
     * Gets the {@link BlobImmutabilityPolicy}.
     *
     * @return {@link BlobImmutabilityPolicy}
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    /**
     * Sets the {@link BlobImmutabilityPolicy}.
     * <p>
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
     * Gets if a legal hold should be placed on the blob.
     *
     * @return If a legal hold should be placed on the blob.
     */
    public Boolean hasLegalHold() {
        return legalHold;
    }

    /**
     * Sets if a legal hold should be placed on the blob.
     * <p>
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

    /**
     * Gets the copy source tags mode.
     *
     * @return The copy source tags mode.
     */
    public BlobCopySourceTagsMode getCopySourceTagsMode() {
        return copySourceTags;
    }

    /**
     * Sets the copy source tags mode
     *
     * @param copySourceTags Indicates if a legal hold should be placed on the blob.
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setCopySourceTagsMode(BlobCopySourceTagsMode copySourceTags) {
        this.copySourceTags = copySourceTags;
        return this;
    }

    /**
     * Optional, only applicable (but required) when the source is Azure Storage Files and using token authentication.
     * Gets the intent of the request.
     *
     * @return the {@link FileShareTokenIntent} for the file share.
     */
    public FileShareTokenIntent getSourceShareTokenIntent() {
        return sourceShareTokenIntent;
    }

    /**
     * Optional, only applicable (but required) when the source is Azure Storage Files and using token authentication.
     * Sets the intent of the request.
     *
     * @param sourceShareTokenIntent Used to indicate the intent of the request.
     * @return The updated options.
     */
    public BlobCopyFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent) {
        this.sourceShareTokenIntent = sourceShareTokenIntent;
        return this;
    }

}
