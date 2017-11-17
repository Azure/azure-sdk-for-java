/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.PacketCaptureStatus;
import com.microsoft.azure.management.network.PcError;
import com.microsoft.azure.management.network.PcStatus;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Implementation for {@link com.microsoft.azure.management.network.PacketCaptureStatus}.
 */
@LangDefinition
public class PacketCaptureStatusImpl extends WrapperImpl<PacketCaptureQueryStatusResultInner>
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
        return inner().id();
    }

    @Override
    public DateTime captureStartTime() {
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
