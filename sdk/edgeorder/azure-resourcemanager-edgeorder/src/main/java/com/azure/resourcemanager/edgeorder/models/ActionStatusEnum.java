// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.edgeorder.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Describes whether the order item is deletable or not.
 */
public final class ActionStatusEnum extends ExpandableStringEnum<ActionStatusEnum> {
    /**
     * Static value Allowed for ActionStatusEnum.
     */
    public static final ActionStatusEnum ALLOWED = fromString("Allowed");

    /**
     * Static value NotAllowed for ActionStatusEnum.
     */
    public static final ActionStatusEnum NOT_ALLOWED = fromString("NotAllowed");

    /**
     * Creates a new instance of ActionStatusEnum value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ActionStatusEnum() {
    }

    /**
     * Creates or finds a ActionStatusEnum from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding ActionStatusEnum.
     */
    public static ActionStatusEnum fromString(String name) {
        return fromString(name, ActionStatusEnum.class);
    }

    /**
     * Gets known ActionStatusEnum values.
     * 
     * @return known ActionStatusEnum values.
     */
    public static Collection<ActionStatusEnum> values() {
        return values(ActionStatusEnum.class);
    }
}
