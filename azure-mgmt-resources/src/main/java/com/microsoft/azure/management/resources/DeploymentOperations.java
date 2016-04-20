package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

public interface DeploymentOperations extends
        SupportsListing<DeploymentOperation>,
        SupportsGetting<DeploymentOperation> {
    InGroup resourceGroup(String resourceGroupName);

    interface InGroup extends
            SupportsListing<DeploymentOperation>,
            SupportsGetting<DeploymentOperation> {
    }
}
