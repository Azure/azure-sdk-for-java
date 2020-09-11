// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.ResourceSkusClient;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingByRegion;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point to compute service SKUs. */
@Fluent
public interface ComputeSkus
    extends SupportsListing<ComputeSku>,
        SupportsListingByRegion<ComputeSku>,
        HasInner<ResourceSkusClient>,
        HasManager<ComputeManager> {
    /**
     * Lists all the skus with the specified resource type.
     *
     * @param resourceType the compute resource type
     * @return the skus list
     */
    PagedIterable<ComputeSku> listByResourceType(ComputeResourceType resourceType);

    /**
     * Lists all the skus with the specified resource type.
     *
     * @param resourceType the compute resource type
     * @return an observable that emits skus
     */
    PagedFlux<ComputeSku> listByResourceTypeAsync(ComputeResourceType resourceType);

    /**
     * Lists all the skus with the specified resource type in the given region.
     *
     * @param region the region
     * @param resourceType the resource type
     * @return the skus list
     */
    PagedIterable<ComputeSku> listByRegionAndResourceType(Region region, ComputeResourceType resourceType);

    /**
     * Lists all the skus with the specified resource type in the given region.
     *
     * @param region the region
     * @param resourceType the resource type
     * @return an observable that emits skus
     */
    PagedFlux<ComputeSku> listByRegionAndResourceTypeAsync(Region region, ComputeResourceType resourceType);
}
