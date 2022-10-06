// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import java.util.Collection;

/** Defines values for ArchiveStatus. */
public final class ArchiveStatus extends ExpandableStringEnum<ArchiveStatus> {
    /** Static value rehydrate-pending-to-hot for ArchiveStatus. */
    public static final ArchiveStatus REHYDRATE_PENDING_TO_HOT = fromString("rehydrate-pending-to-hot");

    /** Static value rehydrate-pending-to-cool for ArchiveStatus. */
    public static final ArchiveStatus REHYDRATE_PENDING_TO_COOL = fromString("rehydrate-pending-to-cool");

    /**
     * Creates or finds a ArchiveStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ArchiveStatus.
     */
    public static ArchiveStatus fromString(String name) {
        return fromString(name, ArchiveStatus.class);
    }

    /**
     * Gets known ArchiveStatus values.
     *
     * @return known ArchiveStatus values.
     */
    public static Collection<ArchiveStatus> values() {
        return values(ArchiveStatus.class);
    }
}
