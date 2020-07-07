// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.PublicIpPrefixesClient;
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
 * Type representing PublicIpPrefixes.
 */
public interface PublicIpPrefixes extends
    SupportsListing<PublicIpPrefix>,
    SupportsCreating<PublicIpPrefix.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsListingByResourceGroup<PublicIpPrefix>,
    SupportsGettingByResourceGroup<PublicIpPrefix>,
    SupportsGettingById<PublicIpPrefix>,
    SupportsDeletingByResourceGroup,
    SupportsBatchCreation<PublicIpPrefix>,
    SupportsBatchDeletion,
    HasManager<NetworkManager>,
    HasInner<PublicIpPrefixesClient> {
}
