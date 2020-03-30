/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;


import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.SecurityGroupViewResultInner;
import com.azure.management.resources.fluentcore.arm.models.HasParent;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Refreshable;

import java.util.Map;

/**
 * The information about security rules applied to the specified VM..
 */
@Fluent
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
