// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.PacketCaptureQueryStatusResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.time.OffsetDateTime;
import java.util.List;

/** Status of packet capture session. */
@Fluent
public interface PacketCaptureStatus extends HasInnerModel<PacketCaptureQueryStatusResultInner> {
    /**
     * Gets the name of the packet capture resource.
     *
     * @return the name of the packet capture resource
     */
    String name();

    /**
     * Gets the ID of the packet capture resource.
     *
     * @return the ID of the packet capture resource
     */
    String id();

    /**
     * Gets the start time of the packet capture session.
     *
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
     * Gets the reason the current packet capture session was stopped.
     *
     * @return the reason the current packet capture session was stopped
     */
    String stopReason();

    /**
     * Gets the list of errors of packet capture session.
     *
     * @return the list of errors of packet capture session
     */
    List<PcError> packetCaptureErrors();
}
