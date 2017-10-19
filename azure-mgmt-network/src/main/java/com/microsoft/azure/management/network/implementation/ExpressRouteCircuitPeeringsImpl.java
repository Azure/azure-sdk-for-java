/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeering;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeeringType;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeerings;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * Represents Express Route Circuit Peerings collection associated with Network Watcher.
 */
@LangDefinition
class ExpressRouteCircuitPeeringsImpl extends
        CreatableResourcesImpl<ExpressRouteCircuitPeering,
                        ExpressRouteCircuitPeeringImpl,
                        ExpressRouteCircuitPeeringInner>
        implements ExpressRouteCircuitPeerings {
    private final ExpressRouteCircuitImpl parent;
    private final ExpressRouteCircuitPeeringsInner innerCollection;

    /**
     * Creates a new ExpressRouteCircuitPeeringsImpl.
     *
     * @param parent the Network Watcher associated with ExpressRouteCircuitPeering
     */
    ExpressRouteCircuitPeeringsImpl(ExpressRouteCircuitPeeringsInner innerCollection, ExpressRouteCircuitImpl parent) {
        this.parent = parent;
        this.innerCollection = innerCollection;
    }

    @Override
    public final PagedList<ExpressRouteCircuitPeering> list() {
        return (new PagedListConverter<ExpressRouteCircuitPeeringInner, ExpressRouteCircuitPeering>() {
            @Override
            public ExpressRouteCircuitPeering typeConvert(ExpressRouteCircuitPeeringInner inner) {
                return wrapModel(inner);
            }
        }).convert(ReadableWrappersImpl.convertToPagedList(inner().list(parent.resourceGroupName(), parent.name())));
    }

    /**
     * @return an observable emits packet captures in this collection
     */
    @Override
    public Observable<ExpressRouteCircuitPeering> listAsync() {
        return wrapPageAsync(inner().listAsync(parent.resourceGroupName(), parent.name()));
    }

    @Override
    protected ExpressRouteCircuitPeeringImpl wrapModel(String name) {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), new ExpressRouteCircuitPeeringType(name));
    }

    protected ExpressRouteCircuitPeeringImpl wrapModel(ExpressRouteCircuitPeeringInner inner) {
        return (inner == null) ? null : new ExpressRouteCircuitPeeringImpl(parent, inner, inner(), inner.peeringType());
    }

    @Override
    public ExpressRouteCircuitPeeringImpl defineAzurePrivatePeering() {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRouteCircuitPeeringType.AZURE_PRIVATE_PEERING);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl defineAzurePublicPeering() {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRouteCircuitPeeringType.AZURE_PUBLIC_PEERING);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl defineMicrosoftPeering() {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRouteCircuitPeeringType.MICROSOFT_PEERING);
    }

    @Override
    public Observable<ExpressRouteCircuitPeering> getByNameAsync(String name) {
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name)
                .map(new Func1<ExpressRouteCircuitPeeringInner, ExpressRouteCircuitPeering>() {
                    @Override
                    public ExpressRouteCircuitPeering call(ExpressRouteCircuitPeeringInner inner) {
                        return wrapModel(inner);
                    }
                });
    }

    @Override
    public ExpressRouteCircuitPeering getByName(String name) {
        return getByNameAsync(name).toBlocking().last();
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

    @Override
    public ExpressRouteCircuitPeeringsInner inner() {
        return innerCollection;
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return this.inner().deleteAsync(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name()).toCompletable();
    }

    @Override
    public ExpressRouteCircuitPeering getById(String id) {
        return null;
    }

    @Override
    public Observable<ExpressRouteCircuitPeering> getByIdAsync(String id) {
        return null;
    }

    @Override
    public ServiceFuture<ExpressRouteCircuitPeering> getByIdAsync(String id, ServiceCallback<ExpressRouteCircuitPeering> callback) {
        return null;
    }

    @Override
    public ExpressRouteCircuitPeering parent() {
        return null;
    }
}