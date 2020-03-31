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
 * A client-side representation of the health information of an application gateway backend.
 */
@Fluent
public interface ApplicationGatewayBackendHealth extends
        HasInner<ApplicationGatewayBackendHealthPool>,
        HasName,
        HasParent<ApplicationGateway> {

    /**
     * @return the application gateway backend address pool that is health information pertains to
     */
    ApplicationGatewayBackend backend();

    /**
     * @return the health information about each associated backend HTTP settings configuration, indexed by its name
     */
    Map<String, ApplicationGatewayBackendHttpConfigurationHealth> httpConfigurationHealths();
}
