// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.DdosProtectionPlansInner;
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

/** Entry point to DDoS protection plans management. */
@Fluent
public interface DdosProtectionPlans
    extends SupportsCreating<DdosProtectionPlan.DefinitionStages.Blank>,
        SupportsListing<DdosProtectionPlan>,
        SupportsListingByResourceGroup<DdosProtectionPlan>,
        SupportsGettingByResourceGroup<DdosProtectionPlan>,
        SupportsGettingById<DdosProtectionPlan>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<DdosProtectionPlan>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<DdosProtectionPlansInner> {
}
