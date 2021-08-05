// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlockListType;

/**
 * Extended options that may be passed when listing blocks for a block blob.
 */
@Fluent
public class BlockBlobListBlocksOptions {

    private final BlockListType type;
    private String leaseId;
    private String ifTagsMatch;

    /**
     * @param type Specifies which type of blocks to return.
     */
    public BlockBlobListBlocksOptions(BlockListType type) {
        this.type = type;
    }

    /**
     * Gets the type of blocks to list.
     *
     * @return The type of blocks to return.
     */
    public BlockListType getType() {
        return type;
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
     * @return The updated BlockBlobListBlocksOptions object.
     */
    public BlockBlobListBlocksOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the SQL statement that apply to the tags of the blob.
     *
     * @return The SQL statement that apply to the tags of the blob.
     */
    public String getIfTagsMatch() {
        return ifTagsMatch;
    }

    /**
     * Optionally applies the SQL statement to the tags of the blob.
     *
     * @param ifTagsMatch The SQL statement that apply to the tags of the blob.
     * @return The updated BlockBlobListBlocksOptions object.
     */
    public BlockBlobListBlocksOptions setIfTagsMatch(String ifTagsMatch) {
        this.ifTagsMatch = ifTagsMatch;
        return this;
    }

}
