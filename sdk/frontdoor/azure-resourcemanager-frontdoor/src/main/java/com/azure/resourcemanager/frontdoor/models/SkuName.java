// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.frontdoor.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for SkuName. */
public final class SkuName extends ExpandableStringEnum<SkuName> {
    /** Static value Classic_AzureFrontDoor for SkuName. */
    public static final SkuName CLASSIC_AZURE_FRONT_DOOR = fromString("Classic_AzureFrontDoor");

    /** Static value Standard_AzureFrontDoor for SkuName. */
    public static final SkuName STANDARD_AZURE_FRONT_DOOR = fromString("Standard_AzureFrontDoor");

    /** Static value Premium_AzureFrontDoor for SkuName. */
    public static final SkuName PREMIUM_AZURE_FRONT_DOOR = fromString("Premium_AzureFrontDoor");

    /**
     * Creates or finds a SkuName from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SkuName.
     */
    @JsonCreator
    public static SkuName fromString(String name) {
        return fromString(name, SkuName.class);
    }

    /** @return known SkuName values. */
    public static Collection<SkuName> values() {
        return values(SkuName.class);
    }
}
