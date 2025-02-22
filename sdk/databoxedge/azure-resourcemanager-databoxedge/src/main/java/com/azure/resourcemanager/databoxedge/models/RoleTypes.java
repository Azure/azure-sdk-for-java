// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.databoxedge.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Defines values for RoleTypes.
 */
public final class RoleTypes extends ExpandableStringEnum<RoleTypes> {
    /**
     * Static value IOT for RoleTypes.
     */
    public static final RoleTypes IOT = fromString("IOT");

    /**
     * Static value ASA for RoleTypes.
     */
    public static final RoleTypes ASA = fromString("ASA");

    /**
     * Static value Functions for RoleTypes.
     */
    public static final RoleTypes FUNCTIONS = fromString("Functions");

    /**
     * Static value Cognitive for RoleTypes.
     */
    public static final RoleTypes COGNITIVE = fromString("Cognitive");

    /**
     * Creates a new instance of RoleTypes value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public RoleTypes() {
    }

    /**
     * Creates or finds a RoleTypes from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding RoleTypes.
     */
    public static RoleTypes fromString(String name) {
        return fromString(name, RoleTypes.class);
    }

    /**
     * Gets known RoleTypes values.
     * 
     * @return known RoleTypes values.
     */
    public static Collection<RoleTypes> values() {
        return values(RoleTypes.class);
    }
}
