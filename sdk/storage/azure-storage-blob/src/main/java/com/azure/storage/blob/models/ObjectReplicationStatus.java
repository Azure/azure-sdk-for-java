// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for ObjectReplicationStatus.
 */
public final class ObjectReplicationStatus extends ExpandableStringEnum<ObjectReplicationStatus> {
    /**
     * Static value completed for ObjectReplicationStatus.
     */
    public static final ObjectReplicationStatus COMPLETE = fromString("complete");

    /**
     * Static value failed for ObjectReplicationStatus.
     */
    public static final ObjectReplicationStatus FAILED = fromString("failed");

    /**
     * Creates a new instance of {@link ObjectReplicationStatus} with no string value.
     *
     * @deprecated Use {@link #fromString(String)} instead.
     */
    @Deprecated
    public ObjectReplicationStatus() {
    }

    /**
     * Creates or finds a ObjectReplicationStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ObjectReplicationStatus.
     */
    public static ObjectReplicationStatus fromString(String name) {
        return fromString(name, ObjectReplicationStatus.class);
    }

    /**
     * Gets known ObjectReplicationStatus values.
     *
     * @return known ObjectReplicationStatus values.
     */
    public static Collection<ObjectReplicationStatus> values() {
        return values(ObjectReplicationStatus.class);
    }
}
