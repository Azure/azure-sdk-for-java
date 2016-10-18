/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import java.util.List;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.Subnet;

/**
 * An interface representing a model's ability to reference a list of associated subnets.
 */
@Fluent()
public interface HasAssociatedSubnets  {
    /**
     * @return list of subnets associated with this resource
     */
    @Method
    List<Subnet> listAssociatedSubnets();
}
