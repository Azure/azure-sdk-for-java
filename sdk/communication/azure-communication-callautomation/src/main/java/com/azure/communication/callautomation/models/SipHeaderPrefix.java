// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.Collection;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Specifies the SipHeaderPrefix used for custom SIP headers in Call Automation.
 */
public final class SipHeaderPrefix extends ExpandableStringEnum<SipHeaderPrefix> {
    /**
    * Use the generic "X-" prefix.
    */
    public static final SipHeaderPrefix X = fromString("X");

    /**
     * Use the legacy "X-MS-Custom" prefix.
     */
    public static final SipHeaderPrefix X_MS_CUSTOM = fromString("XMSCustom");

    /**
     * Creates a new instance of SipHeaderPrefix value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public SipHeaderPrefix() {
    }

    /**
     * Creates or finds a SipHeaderPrefix from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding SipHeaderPrefix.
     */
    public static SipHeaderPrefix fromString(String name) {
        return fromString(name, SipHeaderPrefix.class);
    }

    /**
     * Gets known SipHeaderPrefix values.
     * 
     * @return known SipHeaderPrefix values.
     */
    public static Collection<SipHeaderPrefix> values() {
        return values(SipHeaderPrefix.class);
    }
}
