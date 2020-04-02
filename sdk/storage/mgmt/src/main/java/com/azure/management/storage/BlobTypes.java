/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage;

import java.util.Collection;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for GeoReplicationStatus.
 */
public final class BlobTypes extends ExpandableStringEnum<BlobTypes> {
    /** Static value blockBlob for BlobTypes. */
    public static final BlobTypes BLOCK_BLOB = fromString("blockBlob");

    /** Static value snapshot for BlobTypes. */
    public static final BlobTypes SNAPSHOT = fromString("snapshot");

    /**
     * Creates or finds a BlobType from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding BlobType
     */
    @JsonCreator
    public static BlobTypes fromString(String name) {
        return fromString(name, BlobTypes.class);
    }

    /**
     * @return known BlobType values
     */
    public static Collection<BlobTypes> values() {
        return values(BlobTypes.class);
    }
}
