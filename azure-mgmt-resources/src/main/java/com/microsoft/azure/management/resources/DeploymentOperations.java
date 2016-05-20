package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Defines the interface for accessing deployments in Azure.
 */
public interface DeploymentOperations extends
        SupportsListing<DeploymentOperation>,
        SupportsListingByGroup<DeploymentOperation>,
        SupportsGetting<DeploymentOperation>,
        SupportsGettingByGroup<DeploymentOperation> {

    /**
     * Filter the deployment operations by a specific resource group.
     *
     * @param resourceGroup the resource group to filter by.
     * @return the interface for accessing deployment operations in the resource group.
     */
    InGroup resourceGroup(ResourceGroup resourceGroup);

    /**
     * Defines the interface for accessing deployment operations in a resource
     * group.
     */
    interface InGroup extends
            SupportsListing<DeploymentOperation>,
            SupportsGetting<DeploymentOperation> {
    }
}
