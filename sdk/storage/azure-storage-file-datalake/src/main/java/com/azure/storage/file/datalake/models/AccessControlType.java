// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for AccessControlType.
 */
public class AccessControlType extends ExpandableStringEnum<AccessControlType> {

    /**
     * Static value user for AccessControlType.
     */
    public static final AccessControlType USER = fromString("user");

    /**
     * Static value group for AccessControlType.
     */
    public static final AccessControlType GROUP = fromString("group");

    /**
     * Static value mask for AccessControlType.
     */
    public static final AccessControlType MASK = fromString("mask");

    /**
     * Static value other for AccessControlType.
     */
    public static final AccessControlType OTHER = fromString("other");

    /**
     * Creates or finds a ArchiveStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ArchiveStatus.
     */
    public static AccessControlType fromString(String name) {
        return fromString(name, AccessControlType.class);
    }

    /**
     * @return known ArchiveStatus values.
     */
    public static Collection<AccessControlType> values() {
        return values(AccessControlType.class);
    }
}
