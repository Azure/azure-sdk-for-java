package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.models.Deployment;

import java.io.IOException;

public interface Deployments extends
        SupportsListing<Deployment>,
        SupportsGetting<Deployment>,
        SupportsCreating<Deployment> {
    boolean checkExistence(String resourceGroupName, String deploymentName) throws IOException, CloudException;
}
