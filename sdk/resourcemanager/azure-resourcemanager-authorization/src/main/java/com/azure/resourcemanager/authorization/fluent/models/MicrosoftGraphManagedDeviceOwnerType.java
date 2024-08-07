// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * managedDeviceOwnerType.
 */
public final class MicrosoftGraphManagedDeviceOwnerType
    extends ExpandableStringEnum<MicrosoftGraphManagedDeviceOwnerType> {
    /**
     * Static value unknown for MicrosoftGraphManagedDeviceOwnerType.
     */
    public static final MicrosoftGraphManagedDeviceOwnerType UNKNOWN = fromString("unknown");

    /**
     * Static value company for MicrosoftGraphManagedDeviceOwnerType.
     */
    public static final MicrosoftGraphManagedDeviceOwnerType COMPANY = fromString("company");

    /**
     * Static value personal for MicrosoftGraphManagedDeviceOwnerType.
     */
    public static final MicrosoftGraphManagedDeviceOwnerType PERSONAL = fromString("personal");

    /**
     * Creates a new instance of MicrosoftGraphManagedDeviceOwnerType value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public MicrosoftGraphManagedDeviceOwnerType() {
    }

    /**
     * Creates or finds a MicrosoftGraphManagedDeviceOwnerType from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding MicrosoftGraphManagedDeviceOwnerType.
     */
    public static MicrosoftGraphManagedDeviceOwnerType fromString(String name) {
        return fromString(name, MicrosoftGraphManagedDeviceOwnerType.class);
    }

    /**
     * Gets known MicrosoftGraphManagedDeviceOwnerType values.
     * 
     * @return known MicrosoftGraphManagedDeviceOwnerType values.
     */
    public static Collection<MicrosoftGraphManagedDeviceOwnerType> values() {
        return values(MicrosoftGraphManagedDeviceOwnerType.class);
    }
}
