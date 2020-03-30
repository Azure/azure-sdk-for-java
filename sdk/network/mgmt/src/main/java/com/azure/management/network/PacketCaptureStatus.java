/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.PacketCaptureQueryStatusResultInner;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Status of packet capture session.
 */
@Fluent
public interface PacketCaptureStatus extends HasInner<PacketCaptureQueryStatusResultInner> {
    /**
     * @return the name of the packet capture resource
     */
    String name();

    /**
     * @return the ID of the packet capture resource
     */
    String id();

    /**
     * @return the start time of the packet capture session
     */
    OffsetDateTime captureStartTime();

    /**
     * Get the status of the packet capture session.
     *
     * @return the packetCaptureStatus value
     */
    PcStatus packetCaptureStatus();

    /**
     * @return the reason the current packet capture session was stopped
     */
    String stopReason();

    /**
     * @return the list of errors of packet capture session
     */
    List<PcError> packetCaptureErrors();
}
