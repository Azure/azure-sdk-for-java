/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.arm.models.HasParent;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.Map;


/**
 * A client-side representation of the health information of an application gateway backend HTTP settings configuration.
 */
@Fluent
public interface ApplicationGatewayBackendHttpConfigurationHealth extends
        HasInner<ApplicationGatewayBackendHealthHttpSettings>,
        HasParent<ApplicationGatewayBackendHealth>,
        HasName {

    /**
     * @return the associated application gateway backend HTTP configuration settings this health information pertains to
     */
    ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration();

    /**
     * @return information about the health of each backend server, indexed by the server's IP address
     */
    Map<String, ApplicationGatewayBackendServerHealth> serverHealths();
}
