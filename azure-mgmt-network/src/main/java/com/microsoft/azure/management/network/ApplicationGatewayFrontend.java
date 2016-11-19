/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayFrontendIPConfigurationInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an application gateway frontend.
 */
@Fluent()
public interface ApplicationGatewayFrontend extends
    Wrapper<ApplicationGatewayFrontendIPConfigurationInner>,
    ChildResource<ApplicationGateway> {

    /**
     * @return true if the frontend is accessible via a public IP address
     */
    boolean isPublic();

    /**
     * @return true is the frontend is accessible via an private IP address
     */
    boolean isPrivate();
}
