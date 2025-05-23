// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.carbonoptimization.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Enum for Access Decision.
 */
public final class AccessDecisionEnum extends ExpandableStringEnum<AccessDecisionEnum> {
    /**
     * Access allowed.
     */
    public static final AccessDecisionEnum ALLOWED = fromString("Allowed");

    /**
     * Access denied.
     */
    public static final AccessDecisionEnum DENIED = fromString("Denied");

    /**
     * Creates a new instance of AccessDecisionEnum value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public AccessDecisionEnum() {
    }

    /**
     * Creates or finds a AccessDecisionEnum from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding AccessDecisionEnum.
     */
    public static AccessDecisionEnum fromString(String name) {
        return fromString(name, AccessDecisionEnum.class);
    }

    /**
     * Gets known AccessDecisionEnum values.
     * 
     * @return known AccessDecisionEnum values.
     */
    public static Collection<AccessDecisionEnum> values() {
        return values(AccessDecisionEnum.class);
    }
}
