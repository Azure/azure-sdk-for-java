// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.PacketCaptureStatus;
import com.azure.resourcemanager.network.models.PcError;
import com.azure.resourcemanager.network.models.PcStatus;
import com.azure.resourcemanager.network.fluent.models.PacketCaptureQueryStatusResultInner;
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
        return innerModel().name();
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public OffsetDateTime captureStartTime() {
        return innerModel().captureStartTime();
    }

    @Override
    public PcStatus packetCaptureStatus() {
        return innerModel().packetCaptureStatus();
    }

    @Override
    public String stopReason() {
        return innerModel().stopReason();
    }

    @Override
    public List<PcError> packetCaptureErrors() {
        return innerModel().packetCaptureError();
    }
}
