// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByParent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point for private dns zone groups management API. */
public interface PrivateDnsZoneGroups extends
    SupportsCreating<PrivateDnsZoneGroup.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsGettingById<PrivateDnsZoneGroup>,
    SupportsDeletingByParent,
    SupportsListing<PrivateDnsZoneGroup>,
    HasManager<NetworkManager> {
}
