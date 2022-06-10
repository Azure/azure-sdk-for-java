// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point to packet captures management API in Azure. */
@Fluent
public interface PacketCaptures
    extends SupportsCreating<PacketCapture.DefinitionStages.WithTarget>,
        SupportsListing<PacketCapture>,
        SupportsGettingByName<PacketCapture>,
        SupportsDeletingByName {
}
