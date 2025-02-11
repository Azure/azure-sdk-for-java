// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.Map;

/**
 * A client-side representation of the health information of an application gateway backend HTTP settings configuration.
 */
@Fluent
public interface ApplicationGatewayBackendHttpConfigurationHealth extends
    HasInnerModel<ApplicationGatewayBackendHealthHttpSettings>, HasParent<ApplicationGatewayBackendHealth>, HasName {

    /**
     * Gets the associated application gateway backend HTTP configuration settings this health information pertains
     *     to.
     *
     * @return the associated application gateway backend HTTP configuration settings this health information pertains
     *     to
     */
    ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration();

    /**
     * Gets information about the health of each backend server.
     *
     * @return information about the health of each backend server, indexed by the server's IP address
     */
    Map<String, ApplicationGatewayBackendServerHealth> serverHealths();
}
