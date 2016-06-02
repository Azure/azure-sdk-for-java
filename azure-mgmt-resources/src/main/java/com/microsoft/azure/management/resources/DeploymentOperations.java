/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to deployment operation management API.
 */
public interface DeploymentOperations extends
        SupportsListing<DeploymentOperation>,
        SupportsGettingByName<DeploymentOperation> {

    /**
     * Filter the deployment operations by a specific resource group.
     *
     * @param resourceGroup the resource group to filter by
     * @return the entry point to deployment operation management API in the resource group
     */
    InGroup resourceGroup(ResourceGroup resourceGroup);

    /**
     * Entry point to deployment operation management API in a specific resource group.
     */
    interface InGroup extends
            SupportsListing<DeploymentOperation>,
            SupportsGettingByName<DeploymentOperation> {
    }
}
