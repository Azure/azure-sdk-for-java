// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.models;

import com.azure.core.annotation.Immutable;

/** Represents a SIP Domain for routing calls. See RFC 4904. */
@Immutable
public final class SipDomain {
    /*
     * Enabled flag
     */
    private boolean enabled;

    /**
     * Creates an instance of SipDomain class.
     */
    public SipDomain() {
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
     * Set the enabled property: Enabled flag.
     * 
     * @param enabled the enabled value to set.
     * @return the SipDomain object itself.
     */
    public SipDomain setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
