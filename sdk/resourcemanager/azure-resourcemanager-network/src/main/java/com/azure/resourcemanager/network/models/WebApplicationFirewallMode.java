// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.network.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The mode of the policy.
 */
public final class WebApplicationFirewallMode extends ExpandableStringEnum<WebApplicationFirewallMode> {
    /**
     * Static value Prevention for WebApplicationFirewallMode.
     */
    public static final WebApplicationFirewallMode PREVENTION = fromString("Prevention");

    /**
     * Static value Detection for WebApplicationFirewallMode.
     */
    public static final WebApplicationFirewallMode DETECTION = fromString("Detection");

    /**
     * Creates a new instance of WebApplicationFirewallMode value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public WebApplicationFirewallMode() {
    }

    /**
     * Creates or finds a WebApplicationFirewallMode from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding WebApplicationFirewallMode.
     */
    public static WebApplicationFirewallMode fromString(String name) {
        return fromString(name, WebApplicationFirewallMode.class);
    }

    /**
     * Gets known WebApplicationFirewallMode values.
     * 
     * @return known WebApplicationFirewallMode values.
     */
    public static Collection<WebApplicationFirewallMode> values() {
        return values(WebApplicationFirewallMode.class);
    }
}
