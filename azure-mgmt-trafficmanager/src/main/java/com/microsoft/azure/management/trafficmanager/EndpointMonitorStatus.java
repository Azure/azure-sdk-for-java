/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Traffic manager profile endpoint monitor statuses.
 */
@LangDefinition
public class EndpointMonitorStatus {
    /** Static value Inactive for EndpointMonitorStatus. */
    public static final EndpointMonitorStatus INACTIVE = new EndpointMonitorStatus("Inactive");

    /** Static value Disabled for EndpointMonitorStatus. */
    public static final EndpointMonitorStatus DISABLED = new EndpointMonitorStatus("Disabled");

    /** Static value Online for EndpointMonitorStatus. */
    public static final EndpointMonitorStatus ONLINE = new EndpointMonitorStatus("Online");

    /** Static value Degraded for EndpointMonitorStatus. */
    public static final EndpointMonitorStatus DEGRADED = new EndpointMonitorStatus("Degraded");

    /** Static value CheckingEndpoint for EndpointMonitorStatus. */
    public static final EndpointMonitorStatus CHECKING_ENDPOINT = new EndpointMonitorStatus("CheckingEndpoint");

    /** Static value Stopped for EndpointMonitorStatus. */
    public static final EndpointMonitorStatus STOPPED = new EndpointMonitorStatus("Stopped");

    private String value;

    /**
     * Creates EndpointMonitorStatus.
     *
     * @param value the status
     */
    public EndpointMonitorStatus(String value) {
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
        if (!(obj instanceof EndpointMonitorStatus)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        EndpointMonitorStatus rhs = (EndpointMonitorStatus) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
