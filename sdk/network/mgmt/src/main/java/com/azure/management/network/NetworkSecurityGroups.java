/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.NetworkSecurityGroupsInner;
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
 * Entry point to network security group management.
 */
@Fluent()
public interface NetworkSecurityGroups extends
        SupportsCreating<NetworkSecurityGroup.DefinitionStages.Blank>,
        SupportsListing<NetworkSecurityGroup>,
        SupportsListingByResourceGroup<NetworkSecurityGroup>,
        SupportsGettingByResourceGroup<NetworkSecurityGroup>,
        SupportsGettingById<NetworkSecurityGroup>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<NetworkSecurityGroup>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<NetworkSecurityGroupsInner> {
}
