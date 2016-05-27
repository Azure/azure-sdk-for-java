package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

public interface Deployments extends
        SupportsCreating<Deployment.DefinitionBlank>,
        SupportsListing<Deployment>,
        SupportsListingByGroup<Deployment>,
        SupportsGettingByName<Deployment>,
        SupportsGettingByGroup<Deployment>,
        SupportsDeleting,
        SupportsDeletingByGroup {
    boolean checkExistence(String deploymentName) throws IOException, CloudException;
    boolean checkExistence(String groupName, String deploymentName) throws IOException, CloudException;

    InGroup resourceGroup(ResourceGroup resourceGroup);

    interface InGroup extends
            SupportsListing<Deployment>,
            SupportsGettingByName<Deployment>,
            SupportsCreating<Deployment.DefinitionWithGroup>,
            SupportsDeleting {
        boolean checkExistence(String deploymentName) throws IOException, CloudException;
    }
}
