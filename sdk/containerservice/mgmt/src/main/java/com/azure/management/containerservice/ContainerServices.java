/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice;

import com.azure.core.annotation.Fluent;
import com.azure.management.containerservice.implementation.ContainerServiceManager;
import com.azure.management.containerservice.models.ContainerServicesInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 *  Entry point to container service management API.
 */
@Fluent()
public interface ContainerServices extends
        SupportsCreating<ContainerService.DefinitionStages.Blank>,
        HasManager<ContainerServiceManager>,
        HasInner<ContainerServicesInner>,
        SupportsBatchCreation<ContainerService>,
        SupportsListing<ContainerService>,
        SupportsGettingById<ContainerService>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsListingByResourceGroup<ContainerService>,
        SupportsGettingByResourceGroup<ContainerService> {
}
