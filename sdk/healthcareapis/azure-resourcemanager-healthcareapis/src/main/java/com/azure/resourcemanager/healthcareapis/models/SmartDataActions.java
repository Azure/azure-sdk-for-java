// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.healthcareapis.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The Data Actions that can be enabled for a Smart Identity Provider Application.
 */
public final class SmartDataActions extends ExpandableStringEnum<SmartDataActions> {
    /**
     * Static value Read for SmartDataActions.
     */
    public static final SmartDataActions READ = fromString("Read");

    /**
     * Creates a new instance of SmartDataActions value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public SmartDataActions() {
    }

    /**
     * Creates or finds a SmartDataActions from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding SmartDataActions.
     */
    public static SmartDataActions fromString(String name) {
        return fromString(name, SmartDataActions.class);
    }

    /**
     * Gets known SmartDataActions values.
     * 
     * @return known SmartDataActions values.
     */
    public static Collection<SmartDataActions> values() {
        return values(SmartDataActions.class);
    }
}
