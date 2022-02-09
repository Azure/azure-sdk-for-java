// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * This class contains values which will restrict the successful operation of a variety of requests to the conditions
 * present. These conditions are entirely optional. The entire object or any of its properties may be set to null when
 * passed to a method to indicate that those conditions are not desired. Please refer to the type of each field for more
 * information on those particular access conditions.
 */
@Fluent
public class BlobRequestConditions extends BlobLeaseRequestConditions {
    private String leaseId;

    /**
     * Optionally limit requests to resources that match the passed ETag.
     *
     * @param ifMatch ETag that resources must match.
     * @return The updated BlobRequestConditions object.
     */
    @Override
    public BlobRequestConditions setIfMatch(String ifMatch) {
        super.setIfMatch(ifMatch);
        return this;
    }

    /**
     * Optionally limit requests to resources that do not match the passed ETag.
     *
     * @param ifNoneMatch ETag that resources must not match.
     * @return The updated BlobRequestConditions object.
     */
    @Override
    public BlobRequestConditions setIfNoneMatch(String ifNoneMatch) {
        super.setIfNoneMatch(ifNoneMatch);
        return this;
    }

    /**
     * Optionally limit requests to resources that have only been modified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifModifiedSince The datetime that resources must have been modified since.
     * @return The updated BlobRequestConditions object.
     */
    @Override
    public BlobRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        super.setIfModifiedSince(ifModifiedSince);
        return this;
    }

    /**
     * Optionally limit requests to resources that have remained unmodified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifUnmodifiedSince The datetime that resources must have remained unmodified since.
     * @return The updated BlobRequestConditions object.
     */
    @Override
    public BlobRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        super.setIfUnmodifiedSince(ifUnmodifiedSince);
        return this;
    }

    /**
     * Optionally applies the SQL statement to the tags of the blob.
     *
     * @param tagsConditions The SQL statement that apply to the tags of the blob.
     * @return The updated BlobRequestConditions object.
     */
    @Override
    public BlobRequestConditions setTagsConditions(String tagsConditions) {
        super.setTagsConditions(tagsConditions);
        return this;
    }

    /**
     * Gets the lease ID that blobs and containers must match.
     *
     * @return The lease ID that blobs and containers must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Optionally limits requests to blobs and containers that match the lease ID.
     *
     * @param leaseId Lease ID that blobs and containers must match.
     * @return The updated BlobRequestConditions object.
     */
    public BlobRequestConditions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }
}
