// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.mysqlflexibleserver.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * If is the configuration pending restart or not.
 */
public final class IsConfigPendingRestart extends ExpandableStringEnum<IsConfigPendingRestart> {
    /**
     * Static value True for IsConfigPendingRestart.
     */
    public static final IsConfigPendingRestart TRUE = fromString("True");

    /**
     * Static value False for IsConfigPendingRestart.
     */
    public static final IsConfigPendingRestart FALSE = fromString("False");

    /**
     * Creates a new instance of IsConfigPendingRestart value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public IsConfigPendingRestart() {
    }

    /**
     * Creates or finds a IsConfigPendingRestart from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding IsConfigPendingRestart.
     */
    public static IsConfigPendingRestart fromString(String name) {
        return fromString(name, IsConfigPendingRestart.class);
    }

    /**
     * Gets known IsConfigPendingRestart values.
     * 
     * @return known IsConfigPendingRestart values.
     */
    public static Collection<IsConfigPendingRestart> values() {
        return values(IsConfigPendingRestart.class);
    }
}
