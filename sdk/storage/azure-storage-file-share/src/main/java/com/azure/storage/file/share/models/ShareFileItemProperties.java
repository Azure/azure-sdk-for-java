// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import java.time.OffsetDateTime;

/**
 * Share item properties for items on a listing operation.
 */
public interface ShareFileItemProperties {

    /**
     * @return Datetime this item was created.
     */
    OffsetDateTime getCreatedOn();

    /**
     * @return Datetime this item was last accessed.
     */
    OffsetDateTime getLastAccessedOn();

    /**
     * @return Datetime this item was last written.
     */
    OffsetDateTime getLastWrittenOn();

    /**
     * @return Datetime this item was last changed.
     */
    OffsetDateTime getChangedOn();

    /**
     * @return Datetime this item was last modified.
     */
    OffsetDateTime getLastModified();

    /**
     * @return ETag of this item.
     */
    String getETag();
}
