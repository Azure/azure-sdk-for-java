package com.microsoft.windowsazure.services.blob.models;

/**
 * Specifies which items to include when listing a set of blobs.
 * <p>
 * By default, committed blocks are always returned. Use the values in this enum to include snapshots, metadata, and/or
 * uncommitted blocks.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum BlobListingDetails {
    /**
     * Specifies including no additional details.
     */
    NONE(0),

    /**
     * Specifies listing committed blobs and blob snapshots.
     */
    SNAPSHOTS(1),

    /**
     * Specifies listing blob metadata for each blob returned in the listing.
     */
    METADATA(2),

    /**
     * Specifies listing uncommitted blobs.
     */
    UNCOMMITTED_BLOBS(4);

    /**
     * Returns the value of this enum.
     */
    public int value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *            The value being assigned.
     */
    BlobListingDetails(int val) {
        this.value = val;
    }
}
