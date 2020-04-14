// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.PublicIPAddressesInner;
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

/** Entry point to public IP address management. */
@Fluent()
public interface PublicIPAddresses
    extends SupportsListing<PublicIPAddress>,
        SupportsCreating<PublicIPAddress.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<PublicIPAddress>,
        SupportsGettingByResourceGroup<PublicIPAddress>,
        SupportsGettingById<PublicIPAddress>,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<PublicIPAddress>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<PublicIPAddressesInner> {
}
