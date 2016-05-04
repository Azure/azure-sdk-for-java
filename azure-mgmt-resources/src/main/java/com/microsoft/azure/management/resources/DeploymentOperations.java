package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

public interface DeploymentOperations extends
        SupportsListing<DeploymentOperation>,
        SupportsListingByGroup<DeploymentOperation>,
        SupportsGetting<DeploymentOperation>,
        SupportsGettingByGroup<DeploymentOperation> {
    InGroup resourceGroup(ResourceGroup resourceGroup);

    interface InGroup extends
            SupportsListing<DeploymentOperation>,
            SupportsGetting<DeploymentOperation> {
    }
}
