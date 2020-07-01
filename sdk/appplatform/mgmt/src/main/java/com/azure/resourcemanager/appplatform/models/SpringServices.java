// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.implementation.annotation.Beta;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.ServicesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point for Spring Service management API. */
@Fluent
@Beta
public interface SpringServices
    extends HasManager<AppPlatformManager>,
        HasInner<ServicesClient>,
        SupportsCreating<SpringService.DefinitionStages.Blank>,
        SupportsGettingById<SpringService>,
        SupportsGettingByResourceGroup<SpringService>,
        SupportsListing<SpringService>,
        SupportsListingByResourceGroup<SpringService>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup {
    /** @return all available sku. */
    PagedIterable<ResourceSku> listSkus();

    /** @return all available sku. */
    PagedFlux<ResourceSku> listSkusAsync();
}
