/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.PacketCapture;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Represents Packet Captures collection associated with Network Watcher
 */
@LangDefinition
class PacketCapturesImpl extends
        ExternalChildResourcesNonCachedImpl<PacketCaptureImpl,
                PacketCapture,
                PacketCaptureResultInner,
                NetworkWatcherImpl,
                NetworkWatcher>
        implements SupportsListing<PacketCapture> {
    private final PacketCapturesInner client;

    /**
     * Creates a new PacketCapturesImpl.
     *
     * @param parent the Network Watcher associated with Packet Captures
     */
    protected PacketCapturesImpl(PacketCapturesInner client, NetworkWatcherImpl parent) {
        super(parent, "PacketCapture");
        this.client = client;
    }

    @Override
    public final PagedList<PacketCapture> list() {
        return (new PagedListConverter<PacketCaptureResultInner, PacketCapture>() {
            @Override
            public PacketCapture typeConvert(PacketCaptureResultInner inner) {
                return wrapModel(inner);
            }
        }).convert(ReadableWrappersImpl.convertToPagedList(client.list(parent().resourceGroupName(), parent().name())));
    }

    /**
     * @return an observable emits packet captures in this collection
     */
    @Override
    public Observable<PacketCapture> listAsync() {
        Observable<List<PacketCaptureResultInner>> list = this.client.listAsync(parent().resourceGroupName(), parent().name());
        return ReadableWrappersImpl.convertListToInnerAsync(list).map(new Func1<PacketCaptureResultInner, PacketCapture>() {
            @Override
            public PacketCapture call(PacketCaptureResultInner inner) {
                return wrapModel(inner);
            }
        });
    }

    protected PacketCaptureImpl wrapModel(PacketCaptureResultInner inner) {
        return (inner == null) ? null : new PacketCaptureImpl("PacketCapture", parent(), inner, client);
    }
}
