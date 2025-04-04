// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.costmanagement.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Kind of the recommendation scope.
 */
public final class Scope extends ExpandableStringEnum<Scope> {
    /**
     * Static value Single for Scope.
     */
    public static final Scope SINGLE = fromString("Single");

    /**
     * Static value Shared for Scope.
     */
    public static final Scope SHARED = fromString("Shared");

    /**
     * Creates a new instance of Scope value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public Scope() {
    }

    /**
     * Creates or finds a Scope from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding Scope.
     */
    public static Scope fromString(String name) {
        return fromString(name, Scope.class);
    }

    /**
     * Gets known Scope values.
     * 
     * @return known Scope values.
     */
    public static Collection<Scope> values() {
        return values(Scope.class);
    }
}
