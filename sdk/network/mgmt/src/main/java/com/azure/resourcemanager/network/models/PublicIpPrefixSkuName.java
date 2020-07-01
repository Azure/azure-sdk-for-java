// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for PublicIpPrefixSkuName. */
public final class PublicIpPrefixSkuName extends ExpandableStringEnum<PublicIpPrefixSkuName> {
    /** Static value Standard for PublicIPPrefixSkuName. */
    public static final PublicIpPrefixSkuName STANDARD = fromString("Standard");

    /**
     * Creates or finds a PublicIPPrefixSkuName from its string representation.
     * @param name a name to look for
     * @return the corresponding PublicIPPrefixSkuName
     */
    @JsonCreator
    public static PublicIpPrefixSkuName fromString(String name) {
        return fromString(name, PublicIpPrefixSkuName.class);
    }

    /**
     * @return known PublicIPPrefixSkuName values
     */
    public static Collection<PublicIpPrefixSkuName> values() {
        return values(PublicIpPrefixSkuName.class);
    }
}
