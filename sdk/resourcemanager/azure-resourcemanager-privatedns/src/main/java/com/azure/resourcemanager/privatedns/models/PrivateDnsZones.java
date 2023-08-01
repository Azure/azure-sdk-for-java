// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.privatedns.PrivateDnsZoneManager;
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
import reactor.core.publisher.Mono;

/** Entry point to private DNS zone management API in Azure. */
@Fluent
public interface PrivateDnsZones
    extends SupportsCreating<PrivateDnsZone.DefinitionStages.Blank>,
        SupportsListing<PrivateDnsZone>,
        SupportsListingByResourceGroup<PrivateDnsZone>,
        SupportsGettingById<PrivateDnsZone>,
        SupportsGettingByResourceGroup<PrivateDnsZone>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<PrivateDnsZone>,
        SupportsBatchDeletion,
        HasManager<PrivateDnsZoneManager> {
    /**
     * Deletes a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     */
    void deleteById(String id);

    /**
     * Asynchronously delete the private zone from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByIdAsync(String id);

    /**
     * Deletes a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @param etagValue the ETag value to set on IfMatch header for concurrency protection
     */
    void deleteById(String id, String etagValue);

    /**
     * Asynchronously delete the private zone from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @param etagValue the ETag value to set on IfMatch header for concurrency protection
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByIdAsync(String id, String etagValue);

    /**
     * Deletes the private zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the name of the zone
     */
    void deleteByResourceGroupName(String resourceGroupName, String name);

    /**
     * Asynchronously deletes the private zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the name of the zone
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String name);

    /**
     * Deletes the private zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the name of the zone
     * @param etagValue the ETag value to set on IfMatch header for concurrency protection
     */
    void deleteByResourceGroupName(String resourceGroupName, String name, String etagValue);

    /**
     * Asynchronously deletes the private zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the name of the zone
     * @param etagValue the ETag value to set on IfMatch header for concurrency protection
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String name, String etagValue);
}
