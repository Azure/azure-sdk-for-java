// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network.models;

import java.util.List;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.Subnet;

/**
 * An interface representing a model's ability to reference a list of associated subnets.
 */
@Fluent()
public interface HasAssociatedSubnets  {
    /**
     * @return list of subnets associated with this resource
     */
    List<Subnet> listAssociatedSubnets();
}
