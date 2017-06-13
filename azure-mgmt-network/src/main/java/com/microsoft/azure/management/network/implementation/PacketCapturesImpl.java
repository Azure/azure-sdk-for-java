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
import com.microsoft.azure.management.network.PacketCaptures;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Represents Packet Captures collection associated with Network Watcher.
 */
@LangDefinition
class PacketCapturesImpl extends
        IndependentChildResourcesImpl<PacketCapture,
                        PacketCaptureImpl,
                        PacketCaptureResultInner,
                        PacketCapturesInner,
                        NetworkManager,
                        NetworkWatcher>
        implements PacketCaptures {
    private final NetworkWatcherImpl parent;

    /**
     * Creates a new PacketCapturesImpl.
     *
     * @param parent the Network Watcher associated with Packet Captures
     */
    protected PacketCapturesImpl(PacketCapturesInner client, NetworkWatcherImpl parent) {
        super(client, parent.manager());
        this.parent = parent;
    }

    @Override
    public final PagedList<PacketCapture> list() {
        return (new PagedListConverter<PacketCaptureResultInner, PacketCapture>() {
            @Override
            public PacketCapture typeConvert(PacketCaptureResultInner inner) {
                return wrapModel(inner);
            }
        }).convert(ReadableWrappersImpl.convertToPagedList(inner().list(parent.resourceGroupName(), parent.name())));
    }

    /**
     * @return an observable emits packet captures in this collection
     */
    @Override
    public Observable<PacketCapture> listAsync() {
        Observable<List<PacketCaptureResultInner>> list = inner().listAsync(parent.resourceGroupName(), parent.name());
        return ReadableWrappersImpl.convertListToInnerAsync(list).map(new Func1<PacketCaptureResultInner, PacketCapture>() {
            @Override
            public PacketCapture call(PacketCaptureResultInner inner) {
                return wrapModel(inner);
            }
        });
    }

    @Override
    protected PacketCaptureImpl wrapModel(String name) {
        return null;
    }

    @Override
    protected PacketCaptureImpl wrapModel(PacketCaptureResultInner inner) {
        return (inner == null) ? null : new PacketCaptureImpl(inner.name(), parent, inner, inner());
    }

    /**
     * Starts an extension definition chain.
     *
     * @param name the reference name of the extension to be added
     * @return the extension
     */
    public PacketCaptureImpl define(String name) {
        return new PacketCaptureImpl(name, parent, new PacketCaptureResultInner(), inner());
    }

    @Override
    public Observable<PacketCapture> getByNameAsync(String name) {
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name)
                .map(new Func1<PacketCaptureResultInner, PacketCapture>() {
                    @Override
                    public PacketCapture call(PacketCaptureResultInner inner) {
                        return wrapModel(inner);
                    }
                });
    }

    @Override
    public PacketCapture getByName(String name) {
        return getByNameAsync(name).toBlocking().last();
    }

    @Override
    public PagedList listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).await();
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.inner().deleteAsync(parent.resourceGroupName(),
                parent.name(),
                name,
                callback);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.inner().deleteAsync(parent.resourceGroupName(),
                parent.name(),
                name).toCompletable();
    }
}
