// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.models;

import com.azure.storage.file.share.models.ShareFileItemProperties;

import java.time.OffsetDateTime;

public class InternalShareFileItemProperties implements ShareFileItemProperties {
    private final OffsetDateTime createdOn;
    private final OffsetDateTime lastAccessedOn;
    private final OffsetDateTime lastWrittenOn;
    private final OffsetDateTime changedOn;
    private final OffsetDateTime lastModified;
    private final String eTag;

    /**
     * Creates an instance of share item properties.
     *
     * @param createdOn Datetime the item was created.
     * @param lastAccessedOn Datetime the item was last accessed.
     * @param lastWrittenOn Datetime the item was last written.
     * @param changedOn Datetime the item was last changed.
     * @param lastModified Datetime the item was last modified.
     * @param eTag ETag of the item.
     */
    public InternalShareFileItemProperties(OffsetDateTime createdOn, OffsetDateTime lastAccessedOn,
        OffsetDateTime lastWrittenOn, OffsetDateTime changedOn, OffsetDateTime lastModified, String eTag) {
        this.createdOn = createdOn;
        this.lastAccessedOn = lastAccessedOn;
        this.lastWrittenOn = lastWrittenOn;
        this.changedOn = changedOn;
        this.lastModified = lastModified;
        this.eTag = eTag;
    }

    /**
     * @return Datetime this item was created.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * @return Datetime this item was last accessed.
     */
    public OffsetDateTime getLastAccessedOn() {
        return lastAccessedOn;
    }

    /**
     * @return Datetime this item was last written.
     */
    public OffsetDateTime getLastWrittenOn() {
        return lastWrittenOn;
    }

    /**
     * @return Datetime this item was last changed.
     */
    public OffsetDateTime getChangedOn() {
        return changedOn;
    }

    /**
     * @return Datetime this item was last modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return ETag of this item.
     */
    public String getETag() {
        return eTag;
    }
}
