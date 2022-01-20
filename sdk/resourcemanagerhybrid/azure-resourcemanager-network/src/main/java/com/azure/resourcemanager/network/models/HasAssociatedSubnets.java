// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/** An interface representing a model's ability to reference a list of associated subnets. */
@Fluent()
public interface HasAssociatedSubnets {
    /** @return list of subnets associated with this resource */
    List<Subnet> listAssociatedSubnets();
}
