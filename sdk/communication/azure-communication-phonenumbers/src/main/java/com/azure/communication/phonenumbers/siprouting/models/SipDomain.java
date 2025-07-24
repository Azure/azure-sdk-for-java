// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.models;

import com.azure.core.annotation.Fluent;

/** Represents a SIP Domain for routing calls. See RFC 4904. */
@Fluent
public final class SipDomain {
    /*
     * Gets or sets FQDN of the trunk.
     */
    private final String fqdn;

    /*
     * Enabled flag
     */
    private final boolean enabled;

    /**
     * Constructor with required properties.
     * @param fqdn the FQDN of the trunk.
     * @param enabled the enabled flag.
     */
    public SipDomain(String fqdn, boolean enabled) {
        this.enabled = enabled;
        this.fqdn = fqdn;
    }

    /**
     * Get the enabled property: Enabled flag.
     * 
     * @return the enabled value.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Get the fqdn property: Gets or sets FQDN of the trunk.
     *
     * @return the fqdn value.
     */
    public String getFqdn() {
        return this.fqdn;
    }
}
