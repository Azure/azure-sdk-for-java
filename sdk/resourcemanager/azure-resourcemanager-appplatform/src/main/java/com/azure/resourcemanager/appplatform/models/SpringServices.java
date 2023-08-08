// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

/** Entry point for Spring Service management API. */
@Fluent
public interface SpringServices
    extends HasManager<AppPlatformManager>,
        SupportsCreating<SpringService.DefinitionStages.Blank>,
        SupportsGettingById<SpringService>,
        SupportsGettingByResourceGroup<SpringService>,
        SupportsListing<SpringService>,
        SupportsListingByResourceGroup<SpringService>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup {
    /**
     * Checks the name of the service is available in specific region or not.
     *
     * @param name the service name
     * @param region the region of the service
     * @return the service name is available or not.
     */
    NameAvailability checkNameAvailability(String name, Region region);

    /**
     * Checks the name of the service is available in specific region or not.
     *
     * @param name the service name
     * @param region the region of the service
     * @return the service name is available or not.
     */
    Mono<NameAvailability> checkNameAvailabilityAsync(String name, Region region);

    /** @return all available sku. */
    PagedIterable<ResourceSku> listSkus();

    /** @return all available sku. */
    PagedFlux<ResourceSku> listSkusAsync();
}
