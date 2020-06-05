// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.implementation.NetworkManager;
import com.azure.resourcemanager.network.models.PublicIpAddressesInner;
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

/** Entry point to public IP address management. */
@Fluent()
public interface PublicIpAddresses
    extends SupportsListing<PublicIpAddress>,
        SupportsCreating<PublicIpAddress.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<PublicIpAddress>,
        SupportsGettingByResourceGroup<PublicIpAddress>,
        SupportsGettingById<PublicIpAddress>,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<PublicIpAddress>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<PublicIpAddressesInner> {
}
