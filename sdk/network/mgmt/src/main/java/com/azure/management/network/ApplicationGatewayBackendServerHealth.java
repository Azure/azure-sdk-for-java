/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;


import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.ApplicationGatewayBackendHealthServerInner;
import com.azure.management.resources.fluentcore.arm.models.HasParent;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation of the health information of an application gateway backend server.
 */
@Fluent
public interface ApplicationGatewayBackendServerHealth extends
        HasInner<ApplicationGatewayBackendHealthServerInner>,
        HasParent<ApplicationGatewayBackendHttpConfigurationHealth> {

    /**
     * @return IP address of the server this health information pertains to
     */
    String ipAddress();

    /**
     * Gets the IP configuration of the network interface this health information pertains to.
     *
     * @return a network interface IP configuration
     */
    NicIPConfiguration getNetworkInterfaceIPConfiguration();

    /**
     * @return the health status of the server
     */
    ApplicationGatewayBackendHealthStatus status();
}
