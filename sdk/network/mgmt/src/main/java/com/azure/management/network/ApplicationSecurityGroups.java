/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.ApplicationSecurityGroupsInner;
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
 * Entry point to application security group management.
 */
@Fluent
public interface ApplicationSecurityGroups extends
        SupportsCreating<ApplicationSecurityGroup.DefinitionStages.Blank>,
        SupportsListing<ApplicationSecurityGroup>,
        SupportsListingByResourceGroup<ApplicationSecurityGroup>,
        SupportsGettingByResourceGroup<ApplicationSecurityGroup>,
        SupportsGettingById<ApplicationSecurityGroup>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<ApplicationSecurityGroup>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<ApplicationSecurityGroupsInner> {
}
