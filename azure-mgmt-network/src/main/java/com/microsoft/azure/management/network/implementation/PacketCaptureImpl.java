/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.PacketCapture;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;

@LangDefinition
public class PacketCaptureImpl extends ExternalChildResourceImpl<PacketCapture,
        PacketCaptureResultInner,
        NetworkWatcherImpl,
        NetworkWatcher>
    implements PacketCapture {
    private final PacketCapturesInner client;

    protected PacketCaptureImpl(String name, NetworkWatcherImpl parent, PacketCaptureResultInner innerObject,
                                PacketCapturesInner client) {
        super(name, parent, innerObject);
        this.client = client;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public Observable createAsync() {
        return null;
    }

    @Override
    public Observable updateAsync() {
        return null;
    }

    @Override
    public Observable<Void> deleteAsync() {
        return null;
    }

    @Override
    protected Observable getInnerAsync() {
        return null;
    }
}
