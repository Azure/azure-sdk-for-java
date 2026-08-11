// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import java.time.OffsetDateTime;

/**
 * Share item properties for items on a listing operation.
 */
public interface ShareFileItemProperties {

    /**
     * Gets the datetime this item was created.
     *
     * @return Datetime this item was created.
     */
    OffsetDateTime getCreatedOn();

    /**
     * Gets the datetime this item was last accessed.
     *
     * @return Datetime this item was last accessed.
     */
    OffsetDateTime getLastAccessedOn();

    /**
     * Gets the datetime this item was last written.
     *
     * @return Datetime this item was last written.
     */
    OffsetDateTime getLastWrittenOn();

    /**
     * Gets the datetime this item was last changed.
     *
     * @return Datetime this item was last changed.
     */
    OffsetDateTime getChangedOn();

    /**
     * Gets the datetime this item was last modified.
     *
     * @return Datetime this item was last modified.
     */
    OffsetDateTime getLastModified();

    /**
     * Gets the ETag of this item.
     *
     * @return ETag of this item.
     */
    String getETag();

    /**
     * Gets the owner user identifier (UID) for this item.
     *
     * @return The owner user identifier (UID), or {@code null} if not returned by the service.
     */
    default String getOwner() {
        return null;
    }

    /**
     * Gets the owning group identifier (GID) for this item.
     *
     * @return The owning group identifier (GID), or {@code null} if not returned by the service.
     */
    default String getGroup() {
        return null;
    }

    /**
     * Gets the POSIX file mode for this item.
     *
     * @return The POSIX file mode, or {@code null} if not returned by the service.
     */
    default String getFileMode() {
        return null;
    }
}
