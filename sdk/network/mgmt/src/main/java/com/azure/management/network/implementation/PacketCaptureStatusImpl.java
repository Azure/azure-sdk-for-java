/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.PacketCaptureStatus;
import com.azure.management.network.PcError;
import com.azure.management.network.PcStatus;
import com.azure.management.network.models.PacketCaptureQueryStatusResultInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Implementation for {@link PacketCaptureStatus}.
 */
class PacketCaptureStatusImpl extends WrapperImpl<PacketCaptureQueryStatusResultInner>
        implements PacketCaptureStatus {
    PacketCaptureStatusImpl(PacketCaptureQueryStatusResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String id() {
        return inner().getId();
    }

    @Override
    public OffsetDateTime captureStartTime() {
        return inner().captureStartTime();
    }

    @Override
    public PcStatus packetCaptureStatus() {
        return inner().packetCaptureStatus();
    }

    @Override
    public String stopReason() {
        return inner().stopReason();
    }

    @Override
    public List<PcError> packetCaptureErrors() {
        return inner().packetCaptureError();
    }
}
