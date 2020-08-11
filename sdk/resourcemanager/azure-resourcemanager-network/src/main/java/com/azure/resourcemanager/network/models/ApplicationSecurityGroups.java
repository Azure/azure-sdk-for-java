// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ApplicationSecurityGroupsClient;
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

/** Entry point to application security group management. */
@Fluent
public interface ApplicationSecurityGroups
    extends SupportsCreating<ApplicationSecurityGroup.DefinitionStages.Blank>,
        SupportsListing<ApplicationSecurityGroup>,
        SupportsListingByResourceGroup<ApplicationSecurityGroup>,
        SupportsGettingByResourceGroup<ApplicationSecurityGroup>,
        SupportsGettingById<ApplicationSecurityGroup>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<ApplicationSecurityGroup>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<ApplicationSecurityGroupsClient> {
}
