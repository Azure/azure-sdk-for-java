package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.models.Deployment;

import java.io.IOException;

public interface Deployments extends
        SupportsListing<Deployment>,
        SupportsGetting<Deployment>,
        SupportsCreating<Deployment.DefinitionBlank>,
        SupportsDeleting {
    boolean checkExistence(String deploymentName) throws IOException, CloudException;

    InGroup resourceGroup(String resourceGroupName);

    interface InGroup extends
            SupportsListing<Deployment>,
            SupportsGetting<Deployment>,
            SupportsCreating<Deployment.DefinitionWithGroup>,
            SupportsDeleting {
        boolean checkExistence(String deploymentName) throws IOException, CloudException;
    }
}
