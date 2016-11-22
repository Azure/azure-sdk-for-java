/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Traffic manager profile statuses.
 */
@LangDefinition
public class ProfileMonitorStatus {
    /** Static value Inactive for ProfileMonitorStatus. */
    public static final ProfileMonitorStatus INACTIVE = new ProfileMonitorStatus("Inactive");

    /** Static value Disabled for ProfileMonitorStatus. */
    public static final ProfileMonitorStatus DISABLED = new ProfileMonitorStatus("Disabled");

    /** Static value Online for ProfileMonitorStatus. */
    public static final ProfileMonitorStatus ONLINE = new ProfileMonitorStatus("Online");

    /** Static value Degraded for ProfileMonitorStatus. */
    public static final ProfileMonitorStatus DEGRADED = new ProfileMonitorStatus("Degraded");

    /** Static value CheckingEndpoint for ProfileMonitorStatus. */
    public static final ProfileMonitorStatus CHECKING_ENDPOINT = new ProfileMonitorStatus("CheckingEndpoint");

    private String value;

    /**
     * Creates ProfileMonitorStatus.
     *
     * @param value the status
     */
    public ProfileMonitorStatus(String value) {
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
        if (!(obj instanceof ProfileMonitorStatus)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ProfileMonitorStatus rhs = (ProfileMonitorStatus) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
