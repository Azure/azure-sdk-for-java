// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.PacketCaptureStatus;
import com.azure.resourcemanager.network.PcError;
import com.azure.resourcemanager.network.PcStatus;
import com.azure.resourcemanager.network.models.PacketCaptureQueryStatusResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.time.OffsetDateTime;
import java.util.List;

/** Implementation for {@link PacketCaptureStatus}. */
class PacketCaptureStatusImpl extends WrapperImpl<PacketCaptureQueryStatusResultInner> implements PacketCaptureStatus {
    PacketCaptureStatusImpl(PacketCaptureQueryStatusResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String id() {
        return inner().id();
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
