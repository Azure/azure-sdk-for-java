/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Collection;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

/**
 * Application gateway backend health status.
 */
@Beta(SinceVersion.V1_4_0)
public class ApplicationGatewayBackendHealthStatus extends ExpandableStringEnum<ApplicationGatewayBackendHealthStatus> {
    /**
     * Unknown health status.
     */
    public static final ApplicationGatewayBackendHealthStatus UNKNOWN = fromString(ApplicationGatewayBackendHealthServerHealth.UNKNOWN.toString());

    /**
     * The server is up.
     */
    public static final ApplicationGatewayBackendHealthStatus UP = fromString(ApplicationGatewayBackendHealthServerHealth.UP.toString());

    /**
     * The server is down.
     */
    public static final ApplicationGatewayBackendHealthStatus DOWN = fromString(ApplicationGatewayBackendHealthServerHealth.DOWN.toString());

    /**
     * Partial health status.
     */
    public static final ApplicationGatewayBackendHealthStatus PARTIAL = fromString(ApplicationGatewayBackendHealthServerHealth.PARTIAL.toString());

    /**
     * The server is draining.
     */
    public static final ApplicationGatewayBackendHealthStatus DRAINING = fromString(ApplicationGatewayBackendHealthServerHealth.DRAINING.toString());

    /**
     * The server is unhealthy.
     */
    public static final ApplicationGatewayBackendHealthStatus UNHEALTHY = fromString("Unhealthy");

    /**
     * Finds or creates a backend health status based on the specified name.
     * @param name a name
     * @return an instance of ApplicationGatewayBackendHealthStatus
     */
    public static ApplicationGatewayBackendHealthStatus fromString(String name) {
        return fromString(name, ApplicationGatewayBackendHealthStatus.class);
    }

    /**
     * @return known application gateway backend health statuses
     */
    public static Collection<ApplicationGatewayBackendHealthStatus> values() {
        return values(ApplicationGatewayBackendHealthStatus.class);
    }
}
