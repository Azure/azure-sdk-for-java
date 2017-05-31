/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.PacketCaptureResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

@Fluent
@Beta
public interface PacketCapture extends
        ExternalChildResource<PacketCapture, NetworkWatcher>,
        HasInner<PacketCaptureResultInner> {
    /**
     * Grouping of Packet Capture definition stages.
     */
    interface DefinitionStages {
    }
}
