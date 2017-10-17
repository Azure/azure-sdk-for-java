/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Map;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation of the health information of an application gateway backend HTTP settings configuration.
 */
@Fluent
@Beta(SinceVersion.V1_4_0)
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
