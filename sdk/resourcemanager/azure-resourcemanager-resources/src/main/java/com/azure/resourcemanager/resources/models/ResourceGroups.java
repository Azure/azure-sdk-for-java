// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingByTag;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Entry point to resource group management API.
 */
@Fluent
public interface ResourceGroups extends
        SupportsListing<ResourceGroup>,
        SupportsListingByTag<ResourceGroup>,
        SupportsGettingByName<ResourceGroup>,
        SupportsCreating<ResourceGroup.DefinitionStages.Blank>,
        SupportsDeletingByName,
        //SupportsBeginDeletingByName,
        SupportsBatchCreation<ResourceGroup>,
        HasManager<ResourceManager> {

    /**
     * Checks whether resource group exists.
     *
     * @param name the name (case insensitive) of the resource group to check for
     * @return true of exists, otherwise false
     */
    boolean contain(String name);

    /**
     * Begins deleting a resource group from Azure, identifying it by its name.
     *
     * @param name the resource group name
     * @return the accepted deleting operation
     */
    Accepted<Void> beginDeleteByName(String name);

    /**
     * Begins deleting a resource group from Azure, identifying it by its name.
     *
     * @param name the resource group name
     * @param forceDeletionResourceTypes resource types for force deletion
     * @return the accepted deleting operation
     */
    Accepted<Void> beginDeleteByName(String name, Collection<ForceDeletionResourceType> forceDeletionResourceTypes);

    /**
     * Deletes a resource from Azure, identifying it by its resource name.
     *
     * @param name the name of the resource to delete
     * @param forceDeletionResourceTypes resource types for force deletion
     */
    void deleteByName(String name, Collection<ForceDeletionResourceType> forceDeletionResourceTypes);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its resource name.
     *
     * @param name the name of the resource to delete
     * @param forceDeletionResourceTypes resource types for force deletion
     * @return a handle to cancel the request
     */
    Mono<Void> deleteByNameAsync(String name, Collection<ForceDeletionResourceType> forceDeletionResourceTypes);
}
