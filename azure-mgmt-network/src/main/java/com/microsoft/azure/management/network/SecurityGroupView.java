/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.SecurityGroupViewResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;

import java.util.Map;

/**
 * The information about security rules applied to the specified VM..
 */
@Fluent
@Beta
public interface SecurityGroupView extends HasInner<SecurityGroupViewResultInner>,
        HasParent<NetworkWatcher>,
        Refreshable<SecurityGroupView> {
    /**
     * @return network interfaces on the specified VM
     */
    Map<String, SecurityGroupNetworkInterface> networkInterfaces();

    /**
     * @return virtual machine id
     */
    String vmId();
}
