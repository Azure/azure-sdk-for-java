// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.dns.DnsZoneManager;
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

/** Entry point to DNS zone management API in Azure. */
@Fluent
public interface DnsZones
    extends SupportsCreating<DnsZone.DefinitionStages.Blank>,
        SupportsListing<DnsZone>,
        SupportsListingByResourceGroup<DnsZone>,
        SupportsGettingByResourceGroup<DnsZone>,
        SupportsGettingById<DnsZone>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<DnsZone>,
        SupportsBatchDeletion,
        HasManager<DnsZoneManager> {
    /**
     * Asynchronously deletes the zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param zoneName the name of the zone
     * @param eTagValue the ETag value to set on IfMatch header for concurrency protection
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String zoneName, String eTagValue);

    /**
     * Asynchronously deletes the zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param zoneName the name of the zone
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String zoneName);

    /**
     * Asynchronously delete the zone from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @param eTagValue the ETag value to set on IfMatch header for concurrency protection
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByIdAsync(String id, String eTagValue);

    /**
     * Asynchronously delete the zone from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @return a representation of the deferred computation this delete call
     */
    Mono<Void> deleteByIdAsync(String id);

    /**
     * Deletes the zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param zoneName the name of the zone
     * @param eTagValue the ETag value to set on IfMatch header for concurrency protection
     */
    void deleteByResourceGroupName(String resourceGroupName, String zoneName, String eTagValue);

    /**
     * Deletes the zone from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param zoneName the name of the zone
     */
    void deleteByResourceGroupName(String resourceGroupName, String zoneName);

    /**
     * Deletes a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @param eTagValue the ETag value to set on IfMatch header for concurrency protection
     */
    void deleteById(String id, String eTagValue);

    /**
     * Deletes a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     */
    void deleteById(String id);
}
