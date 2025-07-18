// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.dashboard.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Defines values for DeterministicOutboundIp.
 */
public final class DeterministicOutboundIp extends ExpandableStringEnum<DeterministicOutboundIp> {
    /**
     * Static value Disabled for DeterministicOutboundIp.
     */
    public static final DeterministicOutboundIp DISABLED = fromString("Disabled");

    /**
     * Static value Enabled for DeterministicOutboundIp.
     */
    public static final DeterministicOutboundIp ENABLED = fromString("Enabled");

    /**
     * Creates a new instance of DeterministicOutboundIp value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public DeterministicOutboundIp() {
    }

    /**
     * Creates or finds a DeterministicOutboundIp from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding DeterministicOutboundIp.
     */
    public static DeterministicOutboundIp fromString(String name) {
        return fromString(name, DeterministicOutboundIp.class);
    }

    /**
     * Gets known DeterministicOutboundIp values.
     * 
     * @return known DeterministicOutboundIp values.
     */
    public static Collection<DeterministicOutboundIp> values() {
        return values(DeterministicOutboundIp.class);
    }
}
