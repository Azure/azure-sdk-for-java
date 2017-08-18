/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.containerinstance.implementation.ContainerGroupsInner;
import com.microsoft.azure.management.containerinstance.implementation.ContainerInstanceManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to the container instance management API.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_2_0)
public interface ContainerGroups extends
    SupportsCreating<ContainerGroup.DefinitionStages.Blank>,
    HasManager<ContainerInstanceManager>,
    HasInner<ContainerGroupsInner>,
    SupportsBatchCreation<ContainerGroup>,
    SupportsGettingById<ContainerGroup>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsListingByResourceGroup<ContainerGroup>,
    SupportsGettingByResourceGroup<ContainerGroup>,
    SupportsListing<ContainerGroup> {
}
