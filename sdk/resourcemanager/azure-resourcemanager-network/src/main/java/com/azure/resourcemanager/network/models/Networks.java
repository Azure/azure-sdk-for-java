// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point to virtual network management API in Azure. */
@Fluent()
public interface Networks extends SupportsCreating<Network.DefinitionStages.Blank>, SupportsListing<Network>,
    SupportsListingByResourceGroup<Network>, SupportsGettingByResourceGroup<Network>, SupportsGettingById<Network>,
    SupportsDeletingById, SupportsDeletingByResourceGroup, SupportsBatchCreation<Network>, SupportsBatchDeletion,
    HasManager<NetworkManager> {
    /**
     * Gets the information about {@link Network} based on the resource name and the name of its resource group.
     *
     * @param resourceGroupName the name of the resource group the resource is in
     * @param name the name of the virtual network. (Note, this is not the ID)
     * @param context the {@link Context} of the request
     * @return an immutable representation of the resource
     */
    default Network getByResourceGroup(String resourceGroupName, String name, Context context) {
        throw new UnsupportedOperationException(
            "[getByResourceGroup(String, String, Context)] is not supported in " + getClass());
    }
}
