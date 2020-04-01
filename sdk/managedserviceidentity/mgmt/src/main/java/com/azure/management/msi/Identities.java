/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.msi;

import com.azure.core.annotation.Fluent;
import com.azure.management.msi.implementation.MSIManager;
import com.azure.management.msi.models.UserAssignedIdentitiesInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
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
        HasInner<UserAssignedIdentitiesInner> {
}
