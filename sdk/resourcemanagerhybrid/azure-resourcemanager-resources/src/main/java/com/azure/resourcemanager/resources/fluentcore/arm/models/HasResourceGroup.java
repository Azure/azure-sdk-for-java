// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.models;

import com.azure.core.annotation.Fluent;

/**
 * An interface representing a model that has a resource group name.
 */
@Fluent
public interface HasResourceGroup {
    /**
     * @return the name of the resource group
     */
    String resourceGroupName();
}
