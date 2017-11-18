/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.PacketCaptureQueryStatusResultInner;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Status of packet capture session.
 */
@Fluent
@Beta
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
    DateTime captureStartTime();

    /**
     * Get the status of the packet capture session. Possible values include:
     * 'NotStarted', 'Running', 'Stopped', 'Error', 'Unknown'.
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
