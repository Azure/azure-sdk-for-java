// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

/** Entry point to virtual network link management API in Azure. */
@Fluent
public interface VirtualNetworkLinks
    extends SupportsGettingById<VirtualNetworkLink>,
        SupportsGettingByName<VirtualNetworkLink>,
        SupportsListing<VirtualNetworkLink>,
        HasParent<PrivateDnsZone> {
    /**
     * Lists all the virtual network links, with number of entries in each page limited to given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return list of virtual network links
     */
    PagedIterable<VirtualNetworkLink> list(int pageSize);

    /**
     * Lists all the virtual network links, with number of entries in each page limited to given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return A {@link PagedFlux} of virtual network links
     */
    PagedFlux<VirtualNetworkLink> listAsync(int pageSize);

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
