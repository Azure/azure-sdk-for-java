// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for GeoReplicationStatus. */
public final class BlobTypes extends ExpandableStringEnum<BlobTypes> {
    /** Static value blockBlob for BlobTypes. */
    public static final BlobTypes BLOCK_BLOB = fromString("blockBlob");

    /** Static value snapshot for BlobTypes. */
    public static final BlobTypes SNAPSHOT = fromString("snapshot");

    /**
     * Creates a new instance of BlobTypes value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public BlobTypes() {
    }

    /**
     * Creates or finds a BlobType from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding BlobType
     */
    public static BlobTypes fromString(String name) {
        return fromString(name, BlobTypes.class);
    }

    /**
     * Gets known BlobType values.
     *
     * @return known BlobType values
     */
    public static Collection<BlobTypes> values() {
        return values(BlobTypes.class);
    }
}
