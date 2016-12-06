/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * The reason for unavailability of traffic manager profile DNS name.
 */
@LangDefinition
public class ProfileDnsNameUnavailableReason {
    /** Static value Invalid for ProfileDnsNameUnavailableReason. */
    public static final ProfileDnsNameUnavailableReason INVALID = new ProfileDnsNameUnavailableReason("Invalid");

    /** Static value AlreadyExists for ProfileDnsNameUnavailableReason. */
    public static final ProfileDnsNameUnavailableReason ALREADYEXISTS = new ProfileDnsNameUnavailableReason("AlreadyExists");

    private String value;

    /**
     * Creates ProfileDnsNameUnavailableReason.
     *
     * @param value the reason
     */
    public ProfileDnsNameUnavailableReason(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        String value = this.toString();
        if (!(obj instanceof ProfileDnsNameUnavailableReason)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ProfileDnsNameUnavailableReason rhs = (ProfileDnsNameUnavailableReason) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
