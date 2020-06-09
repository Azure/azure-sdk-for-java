// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.msi.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.msi.MSIManager;
import com.azure.resourcemanager.msi.fluent.UserAssignedIdentitiesClient;
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
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/**
 * Entry point to Azure Managed Service Identity (MSI) Identity resource management API.
 */
@Fluent
public interface Identities extends
        SupportsListing<Identity>,
        SupportsListingByResourceGroup<Identity>,
        SupportsGettingByResourceGroup<Identity>,
        SupportsGettingById<Identity>,
        SupportsCreating<Identity.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<Identity>,
        SupportsBatchDeletion,
        HasManager<MSIManager>,
        HasInner<UserAssignedIdentitiesClient> {
}
