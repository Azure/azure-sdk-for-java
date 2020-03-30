/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;


import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.PacketCapturesInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to packet captures management API in Azure.
 */
@Fluent
public interface PacketCaptures extends
        SupportsCreating<PacketCapture.DefinitionStages.WithTarget>,
        SupportsListing<PacketCapture>,
        SupportsGettingByName<PacketCapture>,
        SupportsDeletingByName,
        HasInner<PacketCapturesInner> {
}
