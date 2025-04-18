// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridcontainerservice.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The extended location type. Allowed value: 'CustomLocation'.
 */
public final class ExtendedLocationTypes extends ExpandableStringEnum<ExtendedLocationTypes> {
    /**
     * Static value CustomLocation for ExtendedLocationTypes.
     */
    public static final ExtendedLocationTypes CUSTOM_LOCATION = fromString("CustomLocation");

    /**
     * Creates a new instance of ExtendedLocationTypes value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ExtendedLocationTypes() {
    }

    /**
     * Creates or finds a ExtendedLocationTypes from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding ExtendedLocationTypes.
     */
    public static ExtendedLocationTypes fromString(String name) {
        return fromString(name, ExtendedLocationTypes.class);
    }

    /**
     * Gets known ExtendedLocationTypes values.
     * 
     * @return known ExtendedLocationTypes values.
     */
    public static Collection<ExtendedLocationTypes> values() {
        return values(ExtendedLocationTypes.class);
    }
}
