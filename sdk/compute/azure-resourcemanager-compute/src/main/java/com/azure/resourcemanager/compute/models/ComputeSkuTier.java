// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Compute resource sku tier. */
@Fluent
public class ComputeSkuTier extends ExpandableStringEnum<ComputeSkuTier> {
    /** Static value Basic for ComputeSkuTier. */
    public static final ComputeSkuTier BASIC = fromString("Basic");
    /** Static value Standard for ComputeSkuTier. */
    public static final ComputeSkuTier STANDARD = fromString("Standard");
    /** Static value Premium for ComputeSkuTier. */
    public static final ComputeSkuTier PREMIUM = fromString("Premium");

    /**
     * Creates a new instance of ComputeSkuTier value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ComputeSkuTier() {
    }

    /**
     * Creates or finds a ComputeSkuTier from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding ComputeSkuTier
     */
    public static ComputeSkuTier fromString(String name) {
        return fromString(name, ComputeSkuTier.class);
    }

    /**
     * Gets known ComputeSkuTier values.
     *
     * @return known ComputeSkuTier values
     */
    public static Collection<ComputeSkuTier> values() {
        return values(ComputeSkuTier.class);
    }
}
