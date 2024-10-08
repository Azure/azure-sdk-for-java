// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Access mode types.
 */
public final class AccessMode extends ExpandableStringEnum<AccessMode> {
    /**
     * Static value Open for AccessMode.
     */
    public static final AccessMode OPEN = fromString("Open");

    /**
     * Static value PrivateOnly for AccessMode.
     */
    public static final AccessMode PRIVATE_ONLY = fromString("PrivateOnly");

    /**
     * Creates a new instance of AccessMode value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public AccessMode() {
    }

    /**
     * Creates or finds a AccessMode from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding AccessMode.
     */
    public static AccessMode fromString(String name) {
        return fromString(name, AccessMode.class);
    }

    /**
     * Gets known AccessMode values.
     * 
     * @return known AccessMode values.
     */
    public static Collection<AccessMode> values() {
        return values(AccessMode.class);
    }
}
