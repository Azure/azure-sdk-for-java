/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * A subnet of a virtual network
 */
public interface Subnet extends 
    Wrapper<SubnetInner>,
    ChildResource {
    
    /**
     * @return the address space prefix, in CIDR notation, assigned to this subnet
     */
    String addressPrefix();
    //TODO: String networkSecurityGroup();
}
