/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 * Defines an interface for accessing deployments in Azure.
 */
public interface Deployments extends
        SupportsCreating<Deployment.DefinitionBlank>,
        SupportsListing<Deployment>,
        SupportsListingByGroup<Deployment>,
        SupportsGetting<Deployment>,
        SupportsGettingByGroup<Deployment>,
        SupportsDeleting,
        SupportsDeletingByGroup {
    /**
     * Checks if a deployment exists in the subscription.
     *
     * @param deploymentName the deployment's name.
     * @return true if the deployment exists; false otherwise.
     * @throws IOException serialization failures
     * @throws CloudException failures thrown from Azure
     */
    boolean checkExistence(String deploymentName) throws IOException, CloudException;

    /**
     * Checks if a deployment exists in a resource group.
     *
     * @param groupName the resource group's name.
     * @param deploymentName the deployment's name.
     * @return true if the deployment exists; false otherwise.
     * @throws IOException serialization failures
     * @throws CloudException failures thrown from Azure
     */
    boolean checkExistence(String groupName, String deploymentName) throws IOException, CloudException;

    /**
     * Filters deployments by a resource group.
     *
     * @param resourceGroup the resource group to filter by.
     * @return the instance for accessing deployments in a resource group.
     */
    InGroup resourceGroup(ResourceGroup resourceGroup);

    /**
     * Defines an interface for accessing deployments in a resource group.
     */
    interface InGroup extends
            SupportsListing<Deployment>,
            SupportsGetting<Deployment>,
            SupportsCreating<Deployment.DefinitionWithTemplate>,
            SupportsDeleting {
        boolean checkExistence(String deploymentName) throws IOException, CloudException;
    }
}
