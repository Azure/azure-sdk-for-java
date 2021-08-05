// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayBackendHealthServerInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** A client-side representation of the health information of an application gateway backend server. */
@Fluent
public interface ApplicationGatewayBackendServerHealth
    extends HasInnerModel<ApplicationGatewayBackendHealthServerInner>,
        HasParent<ApplicationGatewayBackendHttpConfigurationHealth> {

    /** @return IP address of the server this health information pertains to */
    String ipAddress();

    /**
     * Gets the IP configuration of the network interface this health information pertains to.
     *
     * @return a network interface IP configuration
     */
    NicIpConfiguration getNetworkInterfaceIPConfiguration();

    /** @return the health status of the server */
    ApplicationGatewayBackendHealthStatus status();
}
