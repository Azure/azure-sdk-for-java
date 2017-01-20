/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;


/**
 * Entry point to network security group management.
 */
@Fluent()
public interface NetworkSecurityGroups extends
    SupportsCreating<NetworkSecurityGroup.DefinitionStages.Blank>,
    SupportsListing<NetworkSecurityGroup>,
    SupportsListingByGroup<NetworkSecurityGroup>,
    SupportsGettingByGroup<NetworkSecurityGroup>,
    SupportsGettingById<NetworkSecurityGroup>,
    SupportsDeletingById,
    SupportsDeletingByGroup,
    SupportsBatchCreation<NetworkSecurityGroup>,
    HasManager<NetworkManager> {
}
